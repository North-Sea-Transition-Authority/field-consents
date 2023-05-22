package uk.co.nstauthority.fieldconsents.application.assets;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.flags.ApplicationFlagService;
import uk.co.nstauthority.fieldconsents.application.flags.ApplicationFlagType;
import uk.co.nstauthority.fieldconsents.application.tasklist.shared.ApplicationTaskListController;
import uk.co.nstauthority.fieldconsents.assets.AssetJson;
import uk.co.nstauthority.fieldconsents.assets.AssetSelectionForm;
import uk.co.nstauthority.fieldconsents.assets.AssetService;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationPermission;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationStatus;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("applications/{applicationId}/additional-assets")
@HasApplicationStatus(statuses = ApplicationVersionStatus.IN_PROGRESS)
@HasApplicationPermission(permissions = RolePermission.EDIT_FCS_APPLICATIONS)
public class AdditionalAssetsController {

  public static final String PAGE_NAME_SUMMARY = "Additional fields and licences";
  public static final String PAGE_NAME_ADD = "Add field";
  public static final String PAGE_TITLE_ATTR_NAME = "pageTitle";
  public static final String PAGE_NAME_DELETE = "Delete field";
  public static final String CANCEL_URL_ATTR_NAME = "cancelUrl";

  private final AssetService assetService;

  private final FieldService fieldService;

  private final AssetSummaryService assetSummaryService;

  private final ApplicationAssetService applicationAssetService;

  private final ApplicationVersionService applicationVersionService;

  private final AdditionalAssetsFormValidator additionalAssetsFormValidator;

  private final AdditionalAssetSelectionFormValidator additionalAssetSelectionFormValidator;

  private final AdditionalAssetsService additionalAssetsService;

  private final ApplicationFlagService applicationFlagService;

  @Autowired
  public AdditionalAssetsController(AssetService assetService,
                                    FieldService fieldService,
                                    AssetSummaryService assetSummaryService,
                                    ApplicationAssetService applicationAssetService,
                                    ApplicationVersionService applicationVersionService,
                                    AdditionalAssetsFormValidator additionalAssetsFormValidator,
                                    AdditionalAssetSelectionFormValidator additionalAssetSelectionFormValidator,
                                    AdditionalAssetsService additionalAssetsService,
                                    ApplicationFlagService applicationFlagService) {
    this.assetService = assetService;
    this.fieldService = fieldService;
    this.assetSummaryService = assetSummaryService;
    this.applicationAssetService = applicationAssetService;
    this.applicationVersionService = applicationVersionService;
    this.additionalAssetsFormValidator = additionalAssetsFormValidator;
    this.additionalAssetSelectionFormValidator = additionalAssetSelectionFormValidator;
    this.additionalAssetsService = additionalAssetsService;
    this.applicationFlagService = applicationFlagService;
  }

  @GetMapping("/required")
  public ModelAndView getAdditionalAssetsRequiredForm(@PathVariable Integer applicationId) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    return getAdditionalAssetsRequiredModelAndView(applicationId)
        .addObject("form", applicationAssetService.getAdditionalAssetsSetupForm(applicationVersion));
  }

  private ModelAndView getAdditionalAssetsRequiredModelAndView(Integer applicationId) {
    return new ModelAndView("fcs/assets/additionalAssetsRequired")
        .addObject(CANCEL_URL_ATTR_NAME,
            ReverseRouter.route(on(ApplicationTaskListController.class).getTaskList(applicationId)));
  }

  @PostMapping("/required")
  public ModelAndView saveAdditionalAssetsRequiredForm(@PathVariable Integer applicationId,
                                               @Valid @ModelAttribute("form") AdditionalAssetsSetupForm form,
                                               BindingResult bindingResult) {
    if (bindingResult.hasErrors()) {
      return getAdditionalAssetsRequiredModelAndView(applicationId);
    }

    ApplicationVersion applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    applicationFlagService.deleteApplicationFlag(applicationVersion, ApplicationFlagType.HAS_SECONDARY_ASSETS);
    applicationFlagService.saveApplicationFlag(
        applicationVersion,
        ApplicationFlagType.HAS_SECONDARY_ASSETS,
        form.getOtherAssetsRequired()
    );

    if (Boolean.TRUE.equals(form.getOtherAssetsRequired())) {
      return ReverseRouter.redirect(on(AdditionalAssetsController.class).addAdditionalAsset(applicationId));
    }

    return ReverseRouter.redirect(on(ApplicationTaskListController.class).getTaskList(applicationId));
  }

  @GetMapping("/new")
  public ModelAndView addAdditionalAsset(@PathVariable Integer applicationId) {
    ModelAndView modelAndView = getNewAdditionalAssetModelAndView(applicationId);
    modelAndView.addObject("form", new AssetSelectionForm());

    return modelAndView;
  }

  @PostMapping("/new")
  public ModelAndView saveNewAsset(@PathVariable Integer applicationId,
                                   @ModelAttribute("form") AssetSelectionForm form,
                                   BindingResult bindingResult) {
    ApplicationVersion applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    form.setApplicationVersion(applicationVersion);

    additionalAssetSelectionFormValidator.validate(form, bindingResult);

    if (bindingResult.hasErrors()) {
      return getNewAdditionalAssetModelAndView(applicationId);
    }

    AssetJson assetJson = assetService.getAsset(form.getAssetKey());

    if (assetJson.getAssetType() == AssetType.TERMINAL) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Cannot add additional assets to a terminal application");
    }

    var field = fieldService.getFieldWithOperatorAndLicences(assetJson.getId(),
        "Lookup field information for additional asset");

    additionalAssetsService.saveAdditionalAsset(
        applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId),
        field
    );

    return ReverseRouter.redirect(on(AdditionalAssetsController.class).viewAdditionalAssetsSummary(applicationId));
  }

  private ModelAndView getNewAdditionalAssetModelAndView(Integer applicationId) {
    ModelAndView modelAndView = new ModelAndView("fcs/assets/additionalAsset");
    modelAndView
        .addObject(PAGE_TITLE_ATTR_NAME, AdditionalAssetsController.PAGE_NAME_ADD)
        .addObject(CANCEL_URL_ATTR_NAME,
            ReverseRouter.route(on(AdditionalAssetsController.class).viewAdditionalAssetsSummary(applicationId)));

    return modelAndView;
  }


  @GetMapping("/summary")
  public ModelAndView viewAdditionalAssetsSummary(@PathVariable Integer applicationId) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    // if there are no assets already on the application form then go to the task list
    if (!applicationAssetService.secondaryAssetsExist(applicationVersion)) {
      return ReverseRouter.redirect(on(ApplicationTaskListController.class).getTaskList(applicationId));
    }

    ModelAndView modelAndView = getAdditionalAssetsSummaryModelAndView(applicationId);
    modelAndView.addObject("form", new AdditionalAssetsForm());

    return modelAndView;
  }

  private ModelAndView getAdditionalAssetsSummaryModelAndView(Integer applicationId) {
    ModelAndView modelAndView = new ModelAndView("fcs/assets/assetsSummaryForm");
    modelAndView.addObject(PAGE_TITLE_ATTR_NAME, PAGE_NAME_SUMMARY)
        .addObject("assetViews", assetSummaryService.getSummaryViews(
            applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId)))
        .addObject("submitUrl",
            ReverseRouter.route(on(AdditionalAssetsController.class).saveAssetsSummary(applicationId, null, null)));

    return modelAndView;
  }

  @PostMapping("/summary")
  public ModelAndView saveAssetsSummary(@PathVariable Integer applicationId,
                                        @ModelAttribute("form") AdditionalAssetsForm form,
                                        BindingResult bindingResult) {

    additionalAssetsFormValidator.validate(form, bindingResult);

    if (bindingResult.hasErrors()) {
      return getAdditionalAssetsSummaryModelAndView(applicationId);
    }

    // other assets to add so go to the add asset screen
    if (Boolean.TRUE.equals(form.getHasOtherAssetsToAdd())) {
      return ReverseRouter.redirect(on(AdditionalAssetsController.class).addAdditionalAsset(applicationId));
    }

    // no other assets to add so go to the task list
    return ReverseRouter.redirect(on(ApplicationTaskListController.class).getTaskList(applicationId));
  }

  @GetMapping("/{assetNo}/delete")
  public ModelAndView deleteAssetConfirm(@PathVariable Integer applicationId,
                                         @PathVariable Integer assetNo) {
    // Find the asset or error
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    var asset = applicationAssetService.getSecondaryAsset(applicationVersion, assetNo);

    // show the asset delete confirm page
    return new ModelAndView("fcs/assets/deleteAsset")
        .addObject(PAGE_TITLE_ATTR_NAME, PAGE_NAME_DELETE)
        .addObject("assetView", assetSummaryService.getSummaryView(asset))
        .addObject("submitUrl",
            ReverseRouter.route(on(AdditionalAssetsController.class).deleteAsset(applicationId, null, assetNo)))
        .addObject(CANCEL_URL_ATTR_NAME,
            ReverseRouter.route(on(AdditionalAssetsController.class).viewAdditionalAssetsSummary(applicationId))
        );
  }

  @PostMapping("/{assetNo}/delete")
  public ModelAndView deleteAsset(@PathVariable Integer applicationId,
                                  RedirectAttributes redirectAttributes,
                                  @PathVariable Integer assetNo) {
    // Find the asset or error
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    var asset = applicationAssetService.getSecondaryAsset(applicationVersion, assetNo);

    // delete the asset and associated licences
    additionalAssetsService.deleteAdditionalAsset(asset);

    redirectAttributes.addFlashAttribute("successfulDeleteBanner", "Field has been successfully deleted.");
    if (applicationAssetService.secondaryAssetsExist(applicationVersion)) {
      return ReverseRouter.redirect(on(AdditionalAssetsController.class).viewAdditionalAssetsSummary(applicationId));
    }
    return ReverseRouter.redirect(on(AdditionalAssetsController.class).getAdditionalAssetsRequiredForm(applicationId));
  }
}
