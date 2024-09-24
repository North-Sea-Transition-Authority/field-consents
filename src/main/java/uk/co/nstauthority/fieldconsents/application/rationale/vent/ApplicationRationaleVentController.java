package uk.co.nstauthority.fieldconsents.application.rationale.vent;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.fds.searchselector.RestSearchItem.EMPTY_REST_SEARCH_ITEM;

import java.util.List;
import java.util.Optional;
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
import uk.co.nstauthority.fieldconsents.application.rationale.ApplicationRationaleService;
import uk.co.nstauthority.fieldconsents.application.rationale.ApplicationRationaleType;
import uk.co.nstauthority.fieldconsents.application.rationale.emissions.ApplicationRationaleEmissionService;
import uk.co.nstauthority.fieldconsents.application.rationale.emissions.ApplicationRationaleForm;
import uk.co.nstauthority.fieldconsents.application.rationale.emissions.ApplicationRationaleFormValidator;
import uk.co.nstauthority.fieldconsents.application.tasklist.shared.ApplicationTaskListController;
import uk.co.nstauthority.fieldconsents.assets.AssetKey;
import uk.co.nstauthority.fieldconsents.assets.AssetRestController;
import uk.co.nstauthority.fieldconsents.assets.AssetService;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationPermission;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationStatus;
import uk.co.nstauthority.fieldconsents.fds.searchselector.RestSearchItem;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("applications/{applicationId}/application-rationale/vent")
@HasApplicationStatus(statuses = ApplicationVersionStatus.IN_PROGRESS)
@HasApplicationPermission(permissions = RolePermission.EDIT_FCS_APPLICATIONS)
public class ApplicationRationaleVentController {

  private final ApplicationRationaleVentService applicationRationaleVentService;
  private final ApplicationAssetService applicationAssetService;
  private final ApplicationVersionService applicationVersionService;
  private final ApplicationRationaleFormValidator validator;
  private final ApplicationRationaleService applicationRationaleService;
  private final AssetService assetService;
  private final ApplicationRationaleEmissionService applicationRationaleEmissionService;

  ApplicationRationaleVentController(
      ApplicationRationaleVentService applicationRationaleVentService,
      ApplicationAssetService applicationAssetService,
      ApplicationVersionService applicationVersionService,
      ApplicationRationaleFormValidator validator,
      ApplicationRationaleService applicationRationaleService,
      AssetService assetService,
      ApplicationRationaleEmissionService applicationRationaleEmissionService
  ) {
    this.applicationRationaleVentService = applicationRationaleVentService;
    this.applicationAssetService = applicationAssetService;
    this.applicationVersionService = applicationVersionService;
    this.validator = validator;
    this.applicationRationaleService = applicationRationaleService;
    this.assetService = assetService;
    this.applicationRationaleEmissionService = applicationRationaleEmissionService;
  }

  @GetMapping
  public ModelAndView getForm(@PathVariable Integer applicationId) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    var form = applicationRationaleService
        .findByApplicationVersion(applicationVersion)
        .map(ApplicationRationaleForm::from)
        .orElseGet(ApplicationRationaleForm::empty);

    return getModelAndView(
        applicationVersion,
        applicationRationaleService.getLocations(applicationVersion).stream().map(ApplicationAssetView::from).toList(),
        applicationRationaleService.getHostLocation(applicationVersion).map(RestSearchItem::from).orElse(EMPTY_REST_SEARCH_ITEM),
        form
    );
  }

  @PostMapping
  ModelAndView saveForm(@PathVariable Integer applicationId,
                        @ModelAttribute("form") ApplicationRationaleForm form,
                        BindingResult bindingResult) {
    validator.validate(form, bindingResult);
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    if (bindingResult.hasErrors()) {
      var flaringLocations = getFlaringLocationsFromForm(form);
      var hostLocation = getHostLocationFromForm(form);
      return getModelAndView(applicationVersion, flaringLocations, hostLocation, form);
    }

    var rationaleType = form.rationaleType();
    String comment = switch (rationaleType) {
      case INCREASE -> form.increaseComment().getInputValue();
      case DECREASE -> form.decreaseComment().getInputValue();
      default -> null;
    };

    applicationRationaleVentService.saveApplicationRationale(
        applicationVersion,
        rationaleType,
        comment,
        form.locationAssetKeys(),
        form.hostLocationAssetKey()
    );

    return ReverseRouter.redirect(on(ApplicationTaskListController.class).getTaskList(applicationId, null));
  }

  private ModelAndView getModelAndView(
      ApplicationVersion applicationVersion,
      List<ApplicationAssetView> ventingLocations,
      RestSearchItem hostLocation,
      ApplicationRationaleForm form
  ) {
    var applicationId = applicationVersion.getApplication().getId();
    var isTerminal = applicationAssetService.getPrimaryAsset(applicationVersion).isTerminal();
    var assetSearchRestUrl = getAssetSearchUrl(isTerminal);

    var modelAndView = new ModelAndView("fcs/application/application-rationale/vent-form")
        .addObject("form", form)
        .addObject("increaseRadio", ApplicationRationaleType.INCREASE)
        .addObject("decreaseRadio", ApplicationRationaleType.DECREASE)
        .addObject("noChangeRadio", ApplicationRationaleType.NO_CHANGE)
        .addObject("ventingLocations", ventingLocations)
        .addObject("hostLocation", hostLocation)
        .addObject("ventingLocationSearchUrl", assetSearchRestUrl)
        .addObject("hostLocationSearchUrl", assetSearchRestUrl)
        .addObject("cancelUrl",
            ReverseRouter.route(on(ApplicationTaskListController.class).getTaskList(applicationId, null)));

    applicationRationaleEmissionService.findEmissionDailyAverage(applicationVersion).ifPresent(emissionDailyAverage ->
        modelAndView.addObject("emissionDailyAverage", emissionDailyAverage)
    );

    return modelAndView;
  }

  private String getAssetSearchUrl(boolean isTerminal) {
    var restUrl = isTerminal
        ? ReverseRouter.route(on(AssetRestController.class).searchTerminalAssets(null))
        : ReverseRouter.route(on(AssetRestController.class).searchAllAssets(null));

    return restUrl.replace("?term", "");
  }

  private List<ApplicationAssetView> getFlaringLocationsFromForm(ApplicationRationaleForm form) {
    return form.locationAssetKeys()
        .stream()
        .map(AssetKey::parse)
        .flatMap(Optional::stream)
        .map(assetKey -> assetService.getAsset(assetKey, "prefilling venting locations for application rationale"))
        .flatMap(Optional::stream)
        .map(ApplicationAssetView::from)
        .toList();
  }

  private RestSearchItem getHostLocationFromForm(ApplicationRationaleForm form) {
    return AssetKey.parse(form.hostLocationAssetKey())
        .flatMap(assetKey -> assetService.getAsset(assetKey, "prefilling host location for application rationale"))
        .map(RestSearchItem::from)
        .orElse(EMPTY_REST_SEARCH_ITEM);
  }

}
