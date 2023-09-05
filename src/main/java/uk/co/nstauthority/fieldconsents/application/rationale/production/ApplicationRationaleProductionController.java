package uk.co.nstauthority.fieldconsents.application.rationale.production;

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
import uk.co.nstauthority.fieldconsents.application.tasklist.shared.ApplicationTaskListController;
import uk.co.nstauthority.fieldconsents.assets.AssetKey;
import uk.co.nstauthority.fieldconsents.assets.AssetRestController;
import uk.co.nstauthority.fieldconsents.assets.AssetService;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationStatus;
import uk.co.nstauthority.fieldconsents.fds.searchselector.RestSearchItem;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@Controller
@RequestMapping("applications/{applicationId}/application-rationale/production")
@HasApplicationStatus(statuses = ApplicationVersionStatus.IN_PROGRESS)
public class ApplicationRationaleProductionController {

  private final ApplicationRationaleProductionService applicationRationaleProductionService;
  private final ApplicationAssetService applicationAssetService;
  private final ApplicationVersionService applicationVersionService;
  private final ApplicationRationaleProductionFormValidator validator;
  private final ApplicationRationaleService applicationRationaleService;
  private final AssetService assetService;

  ApplicationRationaleProductionController(
      ApplicationRationaleProductionService applicationRationaleProductionService,
      ApplicationAssetService applicationAssetService,
      ApplicationVersionService applicationVersionService,
      ApplicationRationaleProductionFormValidator validator,
      ApplicationRationaleService applicationRationaleService,
      AssetService assetService
  ) {
    this.applicationRationaleProductionService = applicationRationaleProductionService;
    this.applicationAssetService = applicationAssetService;
    this.applicationVersionService = applicationVersionService;
    this.validator = validator;
    this.applicationRationaleService = applicationRationaleService;
    this.assetService = assetService;
  }

  @GetMapping
  public ModelAndView getForm(@PathVariable Integer applicationId) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    var form = applicationRationaleService
        .findByApplicationVersion(applicationVersion)
        .map(ApplicationRationaleProductionForm::from)
        .orElseGet(ApplicationRationaleProductionForm::empty);

    return getModelAndView(
        applicationVersion,
        applicationRationaleService.getLocations(applicationVersion).stream().map(ApplicationAssetView::from).toList(),
        applicationRationaleService.getHostLocation(applicationVersion).map(RestSearchItem::from).orElse(EMPTY_REST_SEARCH_ITEM),
        form
    );
  }

  @PostMapping
  ModelAndView saveForm(@PathVariable Integer applicationId,
                        @ModelAttribute("form") ApplicationRationaleProductionForm form,
                        BindingResult bindingResult) {
    validator.validate(form, bindingResult);
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    if (bindingResult.hasErrors()) {
      var productionLocations = getProductionLocationsFromForm(form);
      var hostLocation = getHostLocationFromForm(form);
      return getModelAndView(applicationVersion, productionLocations, hostLocation, form);
    }

    applicationRationaleProductionService.saveApplicationRationale(
        applicationVersion,
        form.rationaleType(),
        form.extensionComment().getInputValue(),
        form.otherComment().getInputValue(),
        form.productionLocationAssetKeys(),
        form.hostLocationAssetKey()
    );

    return ReverseRouter.redirect(on(ApplicationTaskListController.class).getTaskList(applicationId));
  }

  private ModelAndView getModelAndView(
      ApplicationVersion applicationVersion,
      List<ApplicationAssetView> productionLocations,
      RestSearchItem hostLocation,
      ApplicationRationaleProductionForm form
  ) {
    var applicationId = applicationVersion.getApplication().getId();
    var isTerminal = applicationAssetService.getPrimaryAsset(applicationVersion).isTerminal();
    var assetSearchRestUrl = getAssetSearchUrl(isTerminal);

    return new ModelAndView("fcs/application/application-rationale/production-form")
        .addObject("form", form)
        .addObject("increaseRadio", ApplicationRationaleType.INCREASE)
        .addObject("decreaseRadio", ApplicationRationaleType.DECREASE)
        .addObject("extensionRadio", ApplicationRationaleType.EXTENSION)
        .addObject("otherRadio", ApplicationRationaleType.OTHER)
        .addObject("productionLocations", productionLocations)
        .addObject("hostLocation", hostLocation)
        .addObject("productionLocationSearchUrl", assetSearchRestUrl)
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

  private List<ApplicationAssetView> getProductionLocationsFromForm(ApplicationRationaleProductionForm form) {
    return form.productionLocationAssetKeys()
        .stream()
        .map(AssetKey::parse)
        .flatMap(Optional::stream)
        .map(assetKey -> assetService.getAsset(assetKey, "prefilling production locations for application rationale"))
        .flatMap(Optional::stream)
        .map(ApplicationAssetView::from)
        .toList();
  }

  private RestSearchItem getHostLocationFromForm(ApplicationRationaleProductionForm form) {
    return AssetKey.parse(form.hostLocationAssetKey())
        .flatMap(assetKey -> assetService.getAsset(assetKey, "prefilling host location for application rationale"))
        .map(RestSearchItem::from)
        .orElse(RestSearchItem.EMPTY_REST_SEARCH_ITEM);
  }

}
