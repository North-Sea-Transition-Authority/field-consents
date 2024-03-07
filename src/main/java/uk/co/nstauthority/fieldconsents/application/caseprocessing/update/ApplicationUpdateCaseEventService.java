package uk.co.nstauthority.fieldconsents.application.caseprocessing.update;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

      if (ApplicationUpdateStatus.CLOSED.equals(applicationUpdate.getApplicationUpdateStatus())
          && applicationUpdate.getResponseApplicationVersion() != null // null check to cope with migrated data
      ) {
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
    // null check to cope with migrated data
    var deadlineText = Optional.ofNullable(applicationUpdate.getDeadlineDateTime())
        .map(deadlineDateTime -> DateUtils.format(deadlineDateTime, DateUtils.DATE_TIME))
        .map("Deadline: %s."::formatted)
        .orElse("");

    // null check to cope with migrated data
    var requestText = Optional.ofNullable(applicationUpdate.getRequestText())
        .map("Update request details: %s"::formatted)
        .orElse("");

    return "%s %s".formatted(deadlineText, requestText).strip();
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
    // null check to cope with migrated data
    var responseTypeText = Optional.ofNullable(applicationUpdate.getResponseType())
        .map(responseType -> "Update type: %s.".formatted(responseType.getDisplayName()))
        .orElse("");

    var responseText = Optional.ofNullable(applicationUpdate.getResponseText())
        .map("Update description: %s"::formatted)
        .orElse("");

    return "%s %s".formatted(responseTypeText, responseText).strip();
  }
}
