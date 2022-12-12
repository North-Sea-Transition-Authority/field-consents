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

  public VentInformationTaskListSectionService(VentService ventService,
                                               ConsentLengthService consentLengthService,
                                               VentAnnualService ventAnnualService) {
    this.ventService = ventService;
    this.consentLengthService = consentLengthService;
    this.ventAnnualService = ventAnnualService;
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

  TaskListItem getVentConsentTaskListItem(ApplicationVersion applicationVersion,
                                           ConsentLengthDetails consentLengthDetails) {

    var applicationId = applicationVersion.getApplication().getId();

    var consentLengthType = consentLengthDetails.getConsentLength();

    return switch (consentLengthType) {
      case SHORT_TERM ->
          new TaskListItem(consentLengthType.getDisplayName(),
              TaskListLabel.BLOCKED,
              null);
      case ANNUAL ->
          new TaskListItem(consentLengthType.getDisplayName(),
              getVentAnnualTaskListLabel(applicationVersion),
              ReverseRouter.route(on(VentAnnualController.class)
                  .getVentAnnualForm(applicationId)));
      default ->
          throw new RuntimeException("Incorrect consent length type: " + consentLengthType);
    };
  }

  private TaskListLabel getVentAnnualTaskListLabel(ApplicationVersion applicationVersion) {
    if (!ventAnnualService.ventAnnualMonthsExist(applicationVersion)) {
      return TaskListLabel.NOT_STARTED;
    } else if (ventAnnualService.ventAnnualMonthsComplete(applicationVersion)) {
      return TaskListLabel.COMPLETED;
    } else {
      // we know here that vent annual months data exists, but it isn't complete
      return TaskListLabel.IN_PROGRESS;
    }
  }

}
