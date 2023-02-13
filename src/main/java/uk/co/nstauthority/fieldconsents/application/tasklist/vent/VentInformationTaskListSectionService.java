package uk.co.nstauthority.fieldconsents.application.tasklist.vent;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthDetails;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.flarevent.vent.annual.VentAnnualController;
import uk.co.nstauthority.fieldconsents.flarevent.vent.annual.VentAnnualService;
import uk.co.nstauthority.fieldconsents.flarevent.vent.shortterm.VentShortTermController;
import uk.co.nstauthority.fieldconsents.flarevent.vent.shortterm.VentShortTermService;
import uk.co.nstauthority.fieldconsents.flarevent.vent.ventreport.VentReportController;
import uk.co.nstauthority.fieldconsents.flarevent.vent.ventreport.VentReportPeriodController;
import uk.co.nstauthority.fieldconsents.flarevent.vent.ventreport.VentReportPeriodService;
import uk.co.nstauthority.fieldconsents.flarevent.vent.ventreport.VentReportService;
import uk.co.nstauthority.fieldconsents.flarevent.vent.ventreportgas.VentReportGasDataController;
import uk.co.nstauthority.fieldconsents.flarevent.vent.ventreportgas.VentReportGasDataService;
import uk.co.nstauthority.fieldconsents.flarevent.vent.vents.VentController;
import uk.co.nstauthority.fieldconsents.flarevent.vent.vents.VentService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListItem;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListLabel;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListSection;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListSectionService;

@Service
public class VentInformationTaskListSectionService implements TaskListSectionService<ApplicationVersion> {

  private final VentService ventService;

  private final ConsentLengthService consentLengthService;

  private final VentAnnualService ventAnnualService;

  private final VentShortTermService ventShortTermService;

  private final VentReportPeriodService ventReportPeriodService;

  private final VentReportService ventReportService;

  private final VentReportGasDataService ventReportGasDataService;

  public VentInformationTaskListSectionService(VentService ventService,
                                               ConsentLengthService consentLengthService,
                                               VentAnnualService ventAnnualService,
                                               VentShortTermService ventShortTermService,
                                               VentReportPeriodService ventReportPeriodService,
                                               VentReportService ventReportService,
                                               VentReportGasDataService ventReportGasDataService) {
    this.ventService = ventService;
    this.consentLengthService = consentLengthService;
    this.ventAnnualService = ventAnnualService;
    this.ventShortTermService = ventShortTermService;
    this.ventReportPeriodService = ventReportPeriodService;
    this.ventReportService = ventReportService;
    this.ventReportGasDataService = ventReportGasDataService;
  }

  @Override
  public Optional<TaskListSection> getSection(ApplicationVersion applicationVersion) {

    if (applicationVersion.getApplication().getType() != ApplicationType.VENT) {
      return Optional.empty();
    }

    var consentLengthDetailsOptional =
        consentLengthService.findConsentLengthDetails(applicationVersion);

    if (consentLengthDetailsOptional.isEmpty()) {
      return Optional.empty();
    }

    ConsentLengthDetails consentLengthDetails = consentLengthDetailsOptional.get();

    var items = List.of(
        getVentsTaskListItem(applicationVersion),
        getVentReportTaskListItem(applicationVersion),
        getVentReportGasDataTaskListItem(applicationVersion),
        getVentConsentTaskListItem(applicationVersion, consentLengthDetails)
    );

    return Optional.of(new TaskListSection("Vent information", 20, items));
  }

  TaskListItem getVentsTaskListItem(ApplicationVersion applicationVersion) {

    var applicationId = applicationVersion.getApplication().getId();

    var vents = ventService.getVentsForApplicationVersion(applicationVersion);

    var ventsUrl = vents.isEmpty()
        ? ReverseRouter.route(on(VentController.class).addVent(applicationId))
        : ReverseRouter.route(on(VentController.class).viewVentsSummary(applicationId));

    return new TaskListItem("Vents",
        TaskListLabel.readyOrCompleteByCollection(vents),
        ventsUrl);
  }

  TaskListItem getVentReportTaskListItem(ApplicationVersion applicationVersion) {

    var applicationId = applicationVersion.getApplication().getId();

    boolean ventReportPeriodExists = ventReportPeriodService.ventReportPeriodExists(applicationVersion);

    var ventReportUrl = ventReportPeriodExists
        ? ReverseRouter.route(on(VentReportController.class).getVentReportForm(applicationId))
        : ReverseRouter.route(on(VentReportPeriodController.class).getVentReportPeriodForm(applicationId));

    TaskListLabel ventReportLabel = TaskListLabel.getTaskListLabelFor(applicationVersion,
        ventReportService::ventReportMonthsComplete,
        ventReportPeriodService::ventReportPeriodExists
    );

    return new TaskListItem("Vent report", ventReportLabel, ventReportUrl);
  }

  TaskListItem getVentReportGasDataTaskListItem(ApplicationVersion applicationVersion) {
    var applicationId = applicationVersion.getApplication().getId();

    TaskListLabel ventReportGasDataLabel = ventReportPeriodService.ventReportPeriodExists(applicationVersion)
        ? TaskListLabel.notStartedOrCompleteByOptional(ventReportGasDataService.findVentReportGasData(applicationVersion))
        : TaskListLabel.BLOCKED;

    return new TaskListItem("Vent report gas properties", ventReportGasDataLabel,
        ReverseRouter.route(on(VentReportGasDataController.class).getVentReportGasDataForm(applicationId)));
  }

  TaskListItem getVentConsentTaskListItem(ApplicationVersion applicationVersion,
                                           ConsentLengthDetails consentLengthDetails) {

    var applicationId = applicationVersion.getApplication().getId();

    var consentLengthType = consentLengthDetails.getConsentLength();

    return switch (consentLengthType) {
      case SHORT_TERM ->
          new TaskListItem(consentLengthType.getDisplayName(),
              TaskListLabel.getTaskListLabelFor(applicationVersion,
                  ventShortTermService::ventShortTermMonthsComplete,
                  ventShortTermService::ventShortTermMonthsExist
              ),
              ReverseRouter.route(on(VentShortTermController.class)
                  .getVentShortTermForm(applicationId)));
      case ANNUAL ->
          new TaskListItem(consentLengthType.getDisplayName(),
              TaskListLabel.getTaskListLabelFor(applicationVersion,
                  ventAnnualService::ventAnnualMonthsComplete,
                  ventAnnualService::ventAnnualMonthsExist
                  ),
              ReverseRouter.route(on(VentAnnualController.class)
                  .getVentAnnualForm(applicationId)));
      default ->
          throw new RuntimeException("Incorrect consent length type: " + consentLengthType);
    };
  }
}
