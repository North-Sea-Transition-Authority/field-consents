package uk.co.nstauthority.fieldconsents.application.tasklist.shared;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

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
import uk.co.nstauthority.fieldconsents.application.flags.ApplicationFlagType;
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

  ConsentDetailsTaskListSectionService(ConsentLengthService consentLengthService,
                                       ApplicationAssetService applicationAssetService,
                                       ApplicationFlagService applicationFlagService) {
    this.consentLengthService = consentLengthService;
    this.applicationAssetService = applicationAssetService;
    this.applicationFlagService = applicationFlagService;
  }

  @Override
  public Optional<TaskListSection> getSection(ApplicationVersion applicationVersion) {

    List<TaskListItem> items = new ArrayList<>();
    items.add(
        new TaskListItem("Consent duration",
            TaskListLabel.notStartedOrCompleteByOptional(consentLengthService.findConsentLengthDetails(applicationVersion)),
            ReverseRouter.route(on(ConsentLengthController.class)
                .getConsentLengthForm(applicationVersion.getApplication().getId())))
    );

    var primaryAsset = applicationAssetService.getPrimaryAsset(applicationVersion);
    var applicationType = applicationVersion.getApplication().getType();

    // Additional assets and licences can be added to Field consent applications only for Flares and Vents types
    if (ApplicationTypeFeature.SECONDARY_ASSETS.allowed(applicationType) && primaryAsset.isField()) {
      items.add(getAdditionalAssetsTaskListItem(applicationVersion));
    }

    // Gas injection section only available for Production applications
    if (ApplicationTypeFeature.GAS_INJECTION.allowed(applicationType)) {
      items.add(getGasInjectionTaskListItem(applicationVersion));
    }

    return Optional.of(new TaskListSection("Consent details", 10, items));
  }

  private TaskListItem getAdditionalAssetsTaskListItem(ApplicationVersion applicationVersion) {
    var additionalAssets = applicationAssetService.getSecondaryAssets(applicationVersion);
    var hasSecondaryAssetsFlag = applicationFlagService.findFlagValue(
        applicationVersion,
        ApplicationFlagType.HAS_SECONDARY_ASSETS
    );

    var additionalAssetsUrl = additionalAssets.isEmpty()
        ? ReverseRouter.route(on(AdditionalAssetsController.class)
          .getAdditionalAssetsRequiredForm(applicationVersion.getApplication().getId()))
        : ReverseRouter.route(on(AdditionalAssetsController.class)
          .viewAdditionalAssetsSummary(applicationVersion.getApplication().getId()));

    return new TaskListItem("Additional fields and licences",
        getAdditionalAssetsTaskListLabel(additionalAssets, hasSecondaryAssetsFlag),
        additionalAssetsUrl);
  }

  private TaskListLabel getAdditionalAssetsTaskListLabel(List<ApplicationAsset> additionalAssets,
                                                         Optional<Boolean> secondaryAssetsRequired) {
    TaskListLabel additionalAssetsLabel;
    if (secondaryAssetsRequired.isEmpty()) {
      additionalAssetsLabel = TaskListLabel.NOT_STARTED;
    } else if (additionalAssets.isEmpty() && Boolean.TRUE.equals(secondaryAssetsRequired.get())) {
      additionalAssetsLabel = TaskListLabel.IN_PROGRESS;
    } else {
      additionalAssetsLabel = TaskListLabel.COMPLETED;
    }
    return additionalAssetsLabel;
  }

  private TaskListItem getGasInjectionTaskListItem(ApplicationVersion applicationVersion) {
    return new TaskListItem("Gas injection",
        TaskListLabel.notStartedOrCompleteByOptional(
            applicationFlagService.findFlagValue(applicationVersion, ApplicationFlagType.WILL_GAS_BE_INJECTED)),
        ReverseRouter.route(on(GasInjectionController.class)
            .getGasInjectionForm(applicationVersion.getApplication().getId()))
    );
  }
}