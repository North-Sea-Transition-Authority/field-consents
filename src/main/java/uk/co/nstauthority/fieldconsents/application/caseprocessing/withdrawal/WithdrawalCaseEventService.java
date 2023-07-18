package uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal;

import static uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal.WithdrawalStatus.REJECTED;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEvent;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventView;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;

@Service
public class WithdrawalCaseEventService implements CaseEventService<Application> {

  private final ApplicationWithdrawalService applicationWithdrawalService;

  public WithdrawalCaseEventService(ApplicationWithdrawalService applicationWithdrawalService) {
    this.applicationWithdrawalService = applicationWithdrawalService;
  }

  @Override
  public List<CaseEvent> getCaseEvents(Application application) {
    var caseEvents = new ArrayList<CaseEvent>();
    var applicationWithdrawals = applicationWithdrawalService.getApplicationWithdrawalsByApplication(application);

    for (ApplicationWithdrawal applicationWithdrawal : applicationWithdrawals) {
      caseEvents.add(
          getApplicationWithdrawalRequestedEvent(applicationWithdrawal)
      );

      if (!WithdrawalStatus.OPEN.equals(applicationWithdrawal.getWithdrawalStatus())) {
        caseEvents.add(
            getApplicationWithdrawalRespondedEvent(applicationWithdrawal)
        );
      }
    }

    return caseEvents;
  }

  private CaseEvent getApplicationWithdrawalRequestedEvent(ApplicationWithdrawal applicationWithdrawal) {
    return CaseEvent.builder(applicationWithdrawal.getApplicationVersion())
        .withEventType(CaseEventType.APPLICATION_WITHDRAWAL_REQUESTED)
        .withMainEventUserWuaId(applicationWithdrawal.getRequestedByWuaId())
        .withEventDateTime(applicationWithdrawal.getRequestedDateTime())
        .withEventText(applicationWithdrawal.getRequestText())
        .build();
  }

  private CaseEvent getApplicationWithdrawalRespondedEvent(ApplicationWithdrawal applicationWithdrawal) {
    var withdrawalStatus = applicationWithdrawal.getWithdrawalStatus();
    return CaseEvent.builder(applicationWithdrawal.getApplicationVersion())
        .withEventType(CaseEventType.APPLICATION_WITHDRAWAL_RESPONDED)
        .withMainEventUserWuaId(applicationWithdrawal.getRespondedByWuaId())
        .withEventDateTime(applicationWithdrawal.getRespondedDateTime())
        .withEventText(
            REJECTED.equals(withdrawalStatus)
                ? "Response: %s. Reject reason: %s"
                  .formatted(withdrawalStatus.getDisplayName(),
                      applicationWithdrawal.getResponseText())
                : "Response: %s.".formatted(withdrawalStatus.getDisplayName())
            )
        .build();
  }

  public CaseEventView getCaseEventViewForApplicationWithdrawal(CaseEvent caseEvent,
                                                                Map<Long, EnergyPortalUserDto> portalUserDtosMap) {
    var eventType = caseEvent.eventType();
    return CaseEventView.builder()
        .withApplicationVersionNumber(String.valueOf(caseEvent.applicationVersion().getVersion()))
        .withHeaderText(eventType.getCaseEventHeader())
        .withMainUserInvolvedLabel(eventType.getCaseEventUserLabel())
        .withMainUserInvolvedFullName(portalUserDtosMap.get(caseEvent.mainEventUserWuaId()).displayName())
        .withEventDateTimeLabel(eventType.getCaseEventDateTimeLabel())
        .withEventDateTimeText(DateUtils.format(caseEvent.eventDateTime(), DateUtils.DATE_TIME))
        .withEventTextLabel(eventType.getCaseEventTextLabel())
        .withEventText(caseEvent.eventText())
        .build();
  }
}
