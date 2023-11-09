package uk.co.nstauthority.fieldconsents.application.tasklist.shared;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.assets.AssetRole.HOST;
import static uk.co.nstauthority.fieldconsents.application.assets.AssetRole.LOCATION;
import static uk.co.nstauthority.fieldconsents.application.flags.ApplicationFlagType.HAS_SECONDARY_ASSETS;
import static uk.co.nstauthority.fieldconsents.application.flags.ApplicationFlagType.WILL_GAS_BE_INJECTED;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationTypeFeature;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.assets.AdditionalAssetsController;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAsset;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthController;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.application.flags.ApplicationFlagService;
import uk.co.nstauthority.fieldconsents.application.rationale.ApplicationRationaleService;
import uk.co.nstauthority.fieldconsents.application.rationale.flare.ApplicationRationaleFlareController;
import uk.co.nstauthority.fieldconsents.application.rationale.production.ApplicationRationaleProductionController;
import uk.co.nstauthority.fieldconsents.application.rationale.vent.ApplicationRationaleVentController;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.production.gasinjection.GasInjectionController;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListItem;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListLabel;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListSection;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListSectionService;

@Service
public class ConsentDetailsTaskListSectionService implements TaskListSectionService<ApplicationVersion> {

  private final ConsentLengthService consentLengthService;

  private final ApplicationAssetService applicationAssetService;

  private final ApplicationFlagService applicationFlagService;

  private final ApplicationRationaleService applicationRationaleService;

  ConsentDetailsTaskListSectionService(ConsentLengthService consentLengthService,
                                       ApplicationAssetService applicationAssetService,
                                       ApplicationFlagService applicationFlagService,
                                       ApplicationRationaleService applicationRationaleService) {
    this.consentLengthService = consentLengthService;
    this.applicationAssetService = applicationAssetService;
    this.applicationFlagService = applicationFlagService;
    this.applicationRationaleService = applicationRationaleService;
  }

  @Override
  public Optional<TaskListSection> getSection(ApplicationVersion applicationVersion) {
    var items = new ArrayList<TaskListItem>();

    getApplicationRationaleTaskListItem(applicationVersion).ifPresent(items::add);
    getConsentDurationTaskListItem(applicationVersion).ifPresent(items::add);
    getAdditionalAssetsTaskListItem(applicationVersion).ifPresent(items::add);
    getGasInjectionTaskListItem(applicationVersion).ifPresent(items::add);

    if (items.isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(new TaskListSection("Application details", 10, items));
  }

  Optional<TaskListItem> getApplicationRationaleTaskListItem(ApplicationVersion applicationVersion) {
    var application = applicationVersion.getApplication();
    var taskListLabel = getApplicationRationaleTaskListLabel(applicationVersion);

    var url = switch (application.getType()) {
      case FLARE -> ReverseRouter.route(on(ApplicationRationaleFlareController.class).getForm(application.getId()));
      case VENT -> ReverseRouter.route(on(ApplicationRationaleVentController.class).getForm(application.getId()));
      case PRODUCTION -> ReverseRouter.route(on(ApplicationRationaleProductionController.class).getForm(application.getId()));
    };

    return Optional.of(new TaskListItem("Application rationale", taskListLabel, url));
  }

  private TaskListLabel getApplicationRationaleTaskListLabel(ApplicationVersion applicationVersion) {
    var applicationRationaleExists = applicationRationaleService.doesApplicationRationaleExistFor(applicationVersion);
    var hasFlaringLocation = applicationAssetService.assetExistsForApplicationVersionAndAssetRole(applicationVersion, LOCATION);
    var hasHostLocation = applicationAssetService.assetExistsForApplicationVersionAndAssetRole(applicationVersion, HOST);

    if (!applicationRationaleExists && !hasFlaringLocation && !hasHostLocation) {
      return TaskListLabel.NOT_STARTED;
    }

    if (applicationRationaleExists && hasFlaringLocation && hasHostLocation) {
      return TaskListLabel.COMPLETED;
    }

    return TaskListLabel.IN_PROGRESS;
  }

  Optional<TaskListItem> getConsentDurationTaskListItem(ApplicationVersion applicationVersion) {
    var applicationId = applicationVersion.getApplication().getId();

    return Optional.of(new TaskListItem(
        "Consent duration",
        TaskListLabel.notStartedOrCompleteByOptional(consentLengthService.findConsentLengthDetails(applicationVersion)),
        ReverseRouter.route(on(ConsentLengthController.class).getConsentLengthForm(applicationId)))
    );
  }

  Optional<TaskListItem> getAdditionalAssetsTaskListItem(ApplicationVersion applicationVersion) {
    var application = applicationVersion.getApplication();

    if (!ApplicationTypeFeature.SECONDARY_ASSETS.allowed(application.getType())) {
      return Optional.empty();
    }

    if (!applicationAssetService.getPrimaryAsset(applicationVersion).isField()) {
      return Optional.empty();
    }

    var applicationId = application.getId();
    var secondaryAssets = applicationAssetService.getSecondaryAssets(applicationVersion);
    var secondaryAssetsUrl = secondaryAssets.isEmpty()
        ? ReverseRouter.route(on(AdditionalAssetsController.class).getAdditionalAssetsRequiredForm(applicationId))
        : ReverseRouter.route(on(AdditionalAssetsController.class).viewAdditionalAssetsSummary(applicationId));

    var taskListLabel = getAdditionalAssetsTaskListLabel(applicationVersion, secondaryAssets);

    return Optional.of(new TaskListItem(
        "Additional fields and licences",
        taskListLabel,
        secondaryAssetsUrl
    ));
  }

  private TaskListLabel getAdditionalAssetsTaskListLabel(
      ApplicationVersion applicationVersion,
      List<ApplicationAsset> additionalAssets
  ) {
    var hasSecondaryAssetsFlag = applicationFlagService.findFlagValue(applicationVersion, HAS_SECONDARY_ASSETS);

    if (hasSecondaryAssetsFlag.isEmpty()) {
      return TaskListLabel.NOT_STARTED;
    }

    if (additionalAssets.isEmpty() && Boolean.TRUE.equals(hasSecondaryAssetsFlag.get())) {
      return TaskListLabel.IN_PROGRESS;
    }

    return TaskListLabel.COMPLETED;
  }

  Optional<TaskListItem> getGasInjectionTaskListItem(ApplicationVersion applicationVersion) {
    var application = applicationVersion.getApplication();

    if (!ApplicationTypeFeature.GAS_INJECTION.allowed(application.getType())) {
      return Optional.empty();
    }

    var flagValueOptional = applicationFlagService.findFlagValue(applicationVersion, WILL_GAS_BE_INJECTED);
    return Optional.of(new TaskListItem(
        "Gas injection",
        TaskListLabel.notStartedOrCompleteByOptional(flagValueOptional),
        ReverseRouter.route(on(GasInjectionController.class).getGasInjectionForm(application.getId()))
    ));
  }
}
