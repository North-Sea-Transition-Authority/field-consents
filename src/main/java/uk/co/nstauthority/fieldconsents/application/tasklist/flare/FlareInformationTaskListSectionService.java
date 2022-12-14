package uk.co.nstauthority.fieldconsents.application.tasklist.flare;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthDetails;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.flarevent.flare.annual.FlareAnnualController;
import uk.co.nstauthority.fieldconsents.flarevent.flare.annual.FlareAnnualService;
import uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport.FlareReportController;
import uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport.FlareReportPeriodController;
import uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport.FlareReportPeriodService;
import uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport.FlareReportService;
import uk.co.nstauthority.fieldconsents.flarevent.flare.flares.FlareController;
import uk.co.nstauthority.fieldconsents.flarevent.flare.flares.FlareService;
import uk.co.nstauthority.fieldconsents.flarevent.flare.shortterm.FlareShortTermController;
import uk.co.nstauthority.fieldconsents.flarevent.flare.shortterm.FlareShortTermService;
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

  private final FlareAnnualService flareAnnualService;

  private final FlareShortTermService flareShortTermService;

  @Autowired
  FlareInformationTaskListSectionService(FlareService flareService,
                                         FlareReportPeriodService flareReportPeriodService,
                                         FlareReportService flareReportService,
                                         ConsentLengthService consentLengthService,
                                         FlareAnnualService flareAnnualService,
                                         FlareShortTermService flareShortTermService) {
    this.flareService = flareService;
    this.flareReportPeriodService = flareReportPeriodService;
    this.flareReportService = flareReportService;
    this.consentLengthService = consentLengthService;
    this.flareAnnualService = flareAnnualService;
    this.flareShortTermService = flareShortTermService;
  }

  @Override
  public Optional<TaskListSection> getSection(ApplicationVersion applicationVersion) {

    if (applicationVersion.getApplication().getType() != ApplicationType.FLARE) {
      return Optional.empty();
    }

    var consentLengthDetailsOptional =
        consentLengthService.findConsentLengthDetails(applicationVersion);

    if (consentLengthDetailsOptional.isEmpty()) {
      return Optional.empty();
    }

    ConsentLengthDetails consentLengthDetails = consentLengthDetailsOptional.get();

    var items = List.of(
        getFlaresTaskListItem(applicationVersion),
        getFlareReportTaskListItem(applicationVersion),
        getFlareConsentTaskListItem(applicationVersion, consentLengthDetails)
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
    if (flareReportService.flareReportMonthsComplete(applicationVersion)) {
      flareReportLabel = TaskListLabel.COMPLETED;
    } else if (flareReportPeriodExists) {
      flareReportLabel = TaskListLabel.IN_PROGRESS;
    } else {
      flareReportLabel = TaskListLabel.NOT_STARTED;
    }

    return new TaskListItem("Flare report", flareReportLabel, flareReportUrl);
  }

  TaskListItem getFlareConsentTaskListItem(ApplicationVersion applicationVersion,
                                           ConsentLengthDetails consentLengthDetails) {

    var applicationId = applicationVersion.getApplication().getId();

    var consentLengthType = consentLengthDetails.getConsentLength();

    return switch (consentLengthType) {
      case SHORT_TERM ->
          new TaskListItem(consentLengthType.getDisplayName(),
              getFlareShortTermTaskListLabel(applicationVersion),
              ReverseRouter.route(on(FlareShortTermController.class)
                  .getFlareShortTermForm(applicationId)));
      case ANNUAL ->
          new TaskListItem(consentLengthType.getDisplayName(),
              getFlareAnnualTaskListLabel(applicationVersion),
              ReverseRouter.route(on(FlareAnnualController.class)
                  .getFlareAnnualForm(applicationId)));
      default ->
          throw new RuntimeException("Incorrect consent length type: " + consentLengthType);
    };
  }

  private TaskListLabel getFlareAnnualTaskListLabel(ApplicationVersion applicationVersion) {
    if (!flareAnnualService.flareAnnualMonthsExist(applicationVersion)) {
      return TaskListLabel.NOT_STARTED;
    } else if (flareAnnualService.flareAnnualMonthsComplete(applicationVersion)) {
      return TaskListLabel.COMPLETED;
    } else {
      // we know here that flare annual months data exists, but it isn't complete
      return TaskListLabel.IN_PROGRESS;
    }
  }

  private TaskListLabel getFlareShortTermTaskListLabel(ApplicationVersion applicationVersion) {
    if (!flareShortTermService.flareShortTermMonthsExist(applicationVersion)) {
      return TaskListLabel.NOT_STARTED;
    } else if (flareShortTermService.flareShortTermMonthsComplete(applicationVersion)) {
      return TaskListLabel.COMPLETED;
    } else {
      // we know here that flare short term months data exists, but it isn't complete
      return TaskListLabel.IN_PROGRESS;
    }
  }

}
