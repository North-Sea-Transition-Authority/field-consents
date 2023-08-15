package uk.co.nstauthority.fieldconsents.application.caseprocessing.update;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEvent;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;

@Service
public class ApplicationUpdateCaseEventService implements CaseEventService<Application> {

  private final ApplicationUpdateService applicationUpdateService;

  ApplicationUpdateCaseEventService(ApplicationUpdateService applicationUpdateService) {
    this.applicationUpdateService = applicationUpdateService;
  }

  @Override
  public List<CaseEvent> getCaseEvents(Application application) {
    var caseEvents = new ArrayList<CaseEvent>();
    var applicationUpdates = applicationUpdateService.getApplicationUpdatesByApplication(application);

    for (ApplicationUpdate applicationUpdate : applicationUpdates) {
      caseEvents.add(
          getApplicationUpdateRequestedEvent(applicationUpdate)
      );

      if (ApplicationUpdateStatus.CLOSED.equals(applicationUpdate.getApplicationUpdateStatus())) {
        caseEvents.add(
            getApplicationUpdateCompletedEvent(applicationUpdate)
        );
      }
    }

    return caseEvents;
  }

  private CaseEvent getApplicationUpdateRequestedEvent(ApplicationUpdate applicationUpdate) {
    return CaseEvent.builder(applicationUpdate.getApplicationVersion())
        .withEventType(CaseEventType.APPLICATION_UPDATE_REQUESTED)
        .withMainEventUserWuaId(applicationUpdate.getRequestedByWuaId())
        .withEventDateTime(applicationUpdate.getRequestedDateTime())
        .withEventText(getApplicationUpdateRequestText(applicationUpdate))
        .build();
  }

  private String getApplicationUpdateRequestText(ApplicationUpdate applicationUpdate) {
    return String.format("Deadline: %s. Update request details: %s",
        DateUtils.format(applicationUpdate.getDeadlineDateTime(), DateUtils.DATE_TIME),
        applicationUpdate.getRequestText()
    );
  }

  private CaseEvent getApplicationUpdateCompletedEvent(ApplicationUpdate applicationUpdate) {
    return CaseEvent.builder(applicationUpdate.getResponseApplicationVersion())
        .withEventType(CaseEventType.APPLICATION_UPDATE_SUBMITTED)
        .withMainEventUserWuaId(applicationUpdate.getRespondedByWuaId())
        .withEventDateTime(applicationUpdate.getRespondedDateTime())
        .withEventText(getApplicationUpdateResponseText(applicationUpdate))
        .build();
  }

  private String getApplicationUpdateResponseText(ApplicationUpdate applicationUpdate) {
    var responseTypeText = "Update type: " + applicationUpdate.getResponseType().getDisplayName();

    var responseText = Objects.nonNull(applicationUpdate.getResponseText())
        ? ". Update description: %s".formatted(applicationUpdate.getResponseText())
        : "";

    return responseTypeText + responseText;
  }
}
