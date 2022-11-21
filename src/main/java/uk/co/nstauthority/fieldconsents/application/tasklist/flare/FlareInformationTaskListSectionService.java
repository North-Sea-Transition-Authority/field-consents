package uk.co.nstauthority.fieldconsents.application.tasklist.flare;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.flarevent.flare.FlareController;
import uk.co.nstauthority.fieldconsents.flarevent.flare.FlareService;
import uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport.FlareReportController;
import uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport.FlareReportPeriodController;
import uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport.FlareReportPeriodService;
import uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport.FlareReportService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListItem;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListLabel;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListSection;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListSectionService;

@Service
public class FlareInformationTaskListSectionService implements TaskListSectionService<ApplicationVersion> {

  private final FlareService flareService;

  private final FlareReportPeriodService flareReportPeriodService;

  private final FlareReportService flareReportService;

  private final ConsentLengthService consentLengthService;

  @Autowired
  FlareInformationTaskListSectionService(FlareService flareService,
                                         FlareReportPeriodService flareReportPeriodService,
                                         FlareReportService flareReportService,
                                         ConsentLengthService consentLengthService) {
    this.flareService = flareService;
    this.flareReportPeriodService = flareReportPeriodService;
    this.flareReportService = flareReportService;
    this.consentLengthService = consentLengthService;
  }

  @Override
  public Optional<TaskListSection> getSection(ApplicationVersion applicationVersion) {

    if (applicationVersion.getApplication().getType() != ApplicationType.FLARE) {
      return Optional.empty();
    }

    var items = List.of(
        getFlaresTaskListItem(applicationVersion),
        getFlareReportTaskListItem(applicationVersion)
    );

    return Optional.of(new TaskListSection("Flare information", 20, items));
  }

  TaskListItem getFlaresTaskListItem(ApplicationVersion applicationVersion) {

    var applicationId = applicationVersion.getApplication().getId();

    var flares = flareService.getFlaresForApplicationVersion(applicationVersion);

    var flaresUrl = flares.isEmpty()
        ? ReverseRouter.route(on(FlareController.class).addFlare(applicationId))
        : ReverseRouter.route(on(FlareController.class).viewFlaresSummary(applicationId));

    return new TaskListItem("Flares",
        TaskListLabel.readyOrCompleteByCollection(flares),
        flaresUrl);
  }

  TaskListItem getFlareReportTaskListItem(ApplicationVersion applicationVersion) {

    var applicationId = applicationVersion.getApplication().getId();

    boolean flareReportPeriodExists = flareReportPeriodService.flareReportPeriodExists(applicationVersion);

    var flareReportUrl = flareReportPeriodExists
        ? ReverseRouter.route(on(FlareReportController.class).getFlareReportForm(applicationId))
        : ReverseRouter.route(on(FlareReportPeriodController.class).getFlareReportPeriodForm(applicationId));

    TaskListLabel flareReportLabel;
    if (consentLengthService.findConsentLengthDetails(applicationVersion).isEmpty()) {
      flareReportLabel = TaskListLabel.BLOCKED;
    } else if (flareReportService.flareReportComplete(applicationVersion)) {
      flareReportLabel = TaskListLabel.COMPLETED;
    } else if (flareReportPeriodExists) {
      flareReportLabel = TaskListLabel.IN_PROGRESS;
    } else {
      flareReportLabel = TaskListLabel.NOT_STARTED;
    }

    return new TaskListItem("Flare report", flareReportLabel, flareReportUrl);
  }

}
