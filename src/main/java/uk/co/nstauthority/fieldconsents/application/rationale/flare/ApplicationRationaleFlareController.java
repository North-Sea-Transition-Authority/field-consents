package uk.co.nstauthority.fieldconsents.application.rationale.flare;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetView;
import uk.co.nstauthority.fieldconsents.application.rationale.ApplicationRationaleFormService;
import uk.co.nstauthority.fieldconsents.application.rationale.ApplicationRationaleType;
import uk.co.nstauthority.fieldconsents.application.tasklist.shared.ApplicationTaskListController;
import uk.co.nstauthority.fieldconsents.assets.AssetRestController;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationStatus;
import uk.co.nstauthority.fieldconsents.fds.searchselector.RestSearchItem;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@Controller
@RequestMapping("applications/{applicationId}/application-rationale/flare")
@HasApplicationStatus(statuses = ApplicationVersionStatus.IN_PROGRESS)
public class ApplicationRationaleFlareController {

  private final ApplicationRationaleFlareService applicationRationaleFlareService;
  private final ApplicationRationaleFormService applicationRationaleFormService;
  private final ApplicationAssetService applicationAssetService;
  private final ApplicationVersionService applicationVersionService;
  private final ApplicationRationaleFlareFormValidator validator;

  ApplicationRationaleFlareController(
      ApplicationRationaleFlareService applicationRationaleFlareService,
      ApplicationRationaleFormService applicationRationaleFormService,
      ApplicationAssetService applicationAssetService,
      ApplicationVersionService applicationVersionService,
      ApplicationRationaleFlareFormValidator validator
  ) {
    this.applicationRationaleFlareService = applicationRationaleFlareService;
    this.applicationRationaleFormService = applicationRationaleFormService;
    this.applicationAssetService = applicationAssetService;
    this.applicationVersionService = applicationVersionService;
    this.validator = validator;
  }

  @GetMapping
  public ModelAndView getForm(@PathVariable Integer applicationId) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    var form = applicationRationaleFlareService
        .findByApplicationVersion(applicationVersion)
        .map(ApplicationRationaleFlareForm::from)
        .orElseGet(ApplicationRationaleFlareForm::empty);

    return getModelAndView(
        applicationVersion,
        applicationRationaleFlareService.getFlaringLocations(applicationVersion),
        applicationRationaleFlareService.getHostLocation(applicationVersion),
        form
    );
  }

  @PostMapping
  ModelAndView saveForm(@PathVariable Integer applicationId,
                        @ModelAttribute("form") ApplicationRationaleFlareForm form,
                        BindingResult bindingResult) {
    validator.validate(form, bindingResult);
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    if (bindingResult.hasErrors()) {
      var flaringLocations = applicationRationaleFormService.getFlaringLocationsFromForm(form);
      var hostLocation = applicationRationaleFormService.getHostLocationFormForm(form);
      return getModelAndView(applicationVersion, flaringLocations, hostLocation, form);
    }

    var isIncrease = ApplicationRationaleType.INCREASE.equals(form.rationaleType());
    applicationRationaleFlareService.saveApplicationRationale(
        applicationVersion,
        form.rationaleType(),
        isIncrease ? form.increaseComment().getInputValue() : null,
        form.flaringLocationAssetKeys(),
        form.hostLocationAssetKey()
    );

    return ReverseRouter.redirect(on(ApplicationTaskListController.class).getTaskList(applicationId));
  }

  private ModelAndView getModelAndView(
      ApplicationVersion applicationVersion,
      List<ApplicationAssetView> flaringLocations,
      RestSearchItem hostLocation,
      ApplicationRationaleFlareForm form
  ) {
    var applicationId = applicationVersion.getApplication().getId();
    var isTerminal = applicationAssetService.getPrimaryAsset(applicationVersion).isTerminal();
    var assetSearchRestUrl = getAssetSearchUrl(isTerminal);

    return new ModelAndView("fcs/application/application-rationale/flare-form")
        .addObject("form", form)
        .addObject("increaseRadio", ApplicationRationaleType.INCREASE)
        .addObject("decreaseRadio", ApplicationRationaleType.DECREASE)
        .addObject("noChangeRadio", ApplicationRationaleType.NO_CHANGE)
        .addObject("flaringLocations", flaringLocations)
        .addObject("hostLocation", hostLocation)
        .addObject("flaringLocationSearchUrl", assetSearchRestUrl)
        .addObject("hostLocationSearchUrl", assetSearchRestUrl)
        .addObject("cancelUrl",
            ReverseRouter.route(on(ApplicationTaskListController.class).getTaskList(applicationId)));
  }

  private String getAssetSearchUrl(boolean isTerminal) {
    var restUrl = isTerminal
        ? ReverseRouter.route(on(AssetRestController.class).searchTerminalAssets(null))
        : ReverseRouter.route(on(AssetRestController.class).searchAllAssets(null));

    return restUrl.replace("?term", "");
  }

}
