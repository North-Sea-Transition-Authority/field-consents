package uk.co.nstauthority.fieldconsents.assets;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.tasklist.shared.ApplicationTaskListController;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@Controller
@RequestMapping("applications/{applicationId}/additional-assets")
public class ApplicationAssetController {

  public static final String PAGE_NAME_SUMMARY = "Additional fields and licences";
  public static final String PAGE_NAME_ADD = "Add field";
  public static final String PAGE_TITLE_ATTR_NAME = "pageTitle";
  public static final String PAGE_NAME_DELETE = "Delete field";

  private final AssetService assetService;

  private final AssetSummaryService assetSummaryService;

  private final ApplicationAssetService applicationAssetService;

  private final ApplicationVersionService applicationVersionService;

  private final AdditionalAssetsFormValidator additionalAssetsFormValidator;

  @Autowired
  public ApplicationAssetController(AssetService assetService, AssetSummaryService assetSummaryService,
                                    ApplicationAssetService applicationAssetService,
                                    ApplicationVersionService applicationVersionService,
                                    AdditionalAssetsFormValidator additionalAssetsFormValidator) {
    this.assetService = assetService;
    this.assetSummaryService = assetSummaryService;
    this.applicationAssetService = applicationAssetService;
    this.applicationVersionService = applicationVersionService;
    this.additionalAssetsFormValidator = additionalAssetsFormValidator;
  }

  @GetMapping("/new")
  public ModelAndView addAdditionalAsset(@PathVariable Integer applicationId) {
    ModelAndView modelAndView = getNewAdditionalAssetModelAndView(applicationId);
    modelAndView.addObject("form", new AssetSelectionForm());

    return modelAndView;

  }

  @PostMapping("/new")
  public ModelAndView saveNewAsset(@PathVariable Integer applicationId,
                                   @Valid @ModelAttribute("form") AssetSelectionForm form,
                                   BindingResult bindingResult) {

    if (bindingResult.hasErrors()) {
      return getNewAdditionalAssetModelAndView(applicationId);
    }

    AssetJson assetJson = assetService.getAsset(form.getAssetKey());

    applicationAssetService.saveAdditionalAsset(
        applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId),
        assetJson
    );

    return ReverseRouter.redirect(on(ApplicationAssetController.class).viewAdditionalAssetsSummary(applicationId));
  }

  private ModelAndView getNewAdditionalAssetModelAndView(Integer applicationId) {
    ModelAndView modelAndView = new ModelAndView("fcs/assets/additionalAsset");
    modelAndView.addObject(PAGE_TITLE_ATTR_NAME, ApplicationAssetController.PAGE_NAME_ADD)
        .addObject("cancelUrl",
            ReverseRouter.route(on(ApplicationAssetController.class).viewAdditionalAssetsSummary(applicationId)));

    return modelAndView;
  }


  @GetMapping("/summary")
  public ModelAndView viewAdditionalAssetsSummary(@PathVariable Integer applicationId) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    // if there are no assets already on the application form then go to the task list
    if (!applicationAssetService.additionalAssetsExistForApplicationVersion(applicationVersion)) {
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
            ReverseRouter.route(on(ApplicationAssetController.class).saveAssetsSummary(applicationId, null, null)));

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
      return ReverseRouter.redirect(on(ApplicationAssetController.class).addAdditionalAsset(applicationId));
    }

    // no other assets to add so go to the task list
    return ReverseRouter.redirect(on(ApplicationTaskListController.class).getTaskList(applicationId));
  }

  @GetMapping("/{assetNo}/delete")
  public ModelAndView deleteAssetConfirm(@PathVariable Integer applicationId,
                                         @PathVariable Integer assetNo) {
    // Find the asset or error
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    var asset = applicationAssetService.getAsset(applicationVersion, assetNo);

    // show the asset delete confirm page
    return new ModelAndView("fcs/assets/deleteAsset")
        .addObject(PAGE_TITLE_ATTR_NAME, PAGE_NAME_DELETE)
        .addObject("assetView", AssetView.from(asset, 1))
        .addObject("submitUrl",
            ReverseRouter.route(on(ApplicationAssetController.class).deleteAsset(applicationId, null, assetNo)))
        .addObject("cancelUrl",
            ReverseRouter.route(on(ApplicationAssetController.class).viewAdditionalAssetsSummary(applicationId))
        );
  }

  @PostMapping("/{assetNo}/delete")
  public ModelAndView deleteAsset(@PathVariable Integer applicationId,
                                 RedirectAttributes redirectAttributes,
                                 @PathVariable Integer assetNo) {
    // Find the asset or error
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    var asset = applicationAssetService.getAsset(applicationVersion, assetNo);

    // delete the asset
    applicationAssetService.deleteAsset(asset);

    redirectAttributes.addFlashAttribute("successfulDeleteBanner", "Asset has been successfully deleted.");
    if (applicationAssetService.additionalAssetsExistForApplicationVersion(applicationVersion)) {
      return ReverseRouter.redirect(on(ApplicationAssetController.class).viewAdditionalAssetsSummary(applicationId));
    }
    return ReverseRouter.redirect(on(ApplicationTaskListController.class).getTaskList(applicationId));
  }
}
