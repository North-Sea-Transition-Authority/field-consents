package uk.co.nstauthority.fieldconsents.application.tasklist.shared;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationTypeFeature;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthController;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.assets.ApplicationAssetController;
import uk.co.nstauthority.fieldconsents.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListItem;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListLabel;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListSection;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListSectionService;

@Service
public class ConsentDetailsTaskListSectionService implements TaskListSectionService<ApplicationVersion> {

  private final ConsentLengthService consentLengthService;

  private final ApplicationAssetService applicationAssetService;

  ConsentDetailsTaskListSectionService(ConsentLengthService consentLengthService,
                                       ApplicationAssetService applicationAssetService) {
    this.consentLengthService = consentLengthService;
    this.applicationAssetService = applicationAssetService;
  }

  @Override
  public Optional<TaskListSection> getSection(ApplicationVersion applicationVersion) {

    List<TaskListItem> items = new ArrayList<>();
    items.add(
        new TaskListItem("Consent duration",
            TaskListLabel.notStartedOrCompleteByOptional(consentLengthService.findConsentLengthDetails(applicationVersion)),
            ReverseRouter.route(on(ConsentLengthController.class).getConsentLengthForm(applicationVersion.getId())))
    );

    var primaryAsset = applicationAssetService.getPrimaryApplicationAsset(applicationVersion);
    var applicationType =  applicationVersion.getApplication().getType();

    // Additional assets and licences can be added to Field consent applications only for Flares and Vents types
    if (ApplicationTypeFeature.SECONDARY_ASSETS.allowed(applicationType) && primaryAsset.isField()) {
      items.add(getAdditionalAssetsTaskListItem(applicationVersion));
    }

    return Optional.of(new TaskListSection("Consent details", 10, items));
  }

  private TaskListItem getAdditionalAssetsTaskListItem(ApplicationVersion applicationVersion) {
    var additionalAssets = applicationAssetService.getAdditionalAssetsForApplicationVersion(applicationVersion);

    var additionalAssetsUrl = additionalAssets.isEmpty()
        ? ReverseRouter.route(on(ApplicationAssetController.class)
          .addAdditionalAsset(applicationVersion.getApplication().getId()))
        : ReverseRouter.route(on(ApplicationAssetController.class)
          .viewAdditionalAssetsSummary(applicationVersion.getApplication().getId()));

    return new TaskListItem("Additional fields and licences",
        TaskListLabel.readyOrCompleteByCollection(additionalAssets),
        additionalAssetsUrl);
  }
}
