package uk.co.nstauthority.fieldconsents.application.caseprocessing.casenotes;

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
public class CaseNoteEventService implements CaseEventService<Application> {

  private final CaseNotesService caseNotesService;

  public CaseNoteEventService(CaseNotesService caseNotesService) {
    this.caseNotesService = caseNotesService;
  }

  @Override
  public List<CaseEvent> getCaseEvents(Application application) {

    var caseEvents = new ArrayList<CaseEvent>();
    var caseNotes = caseNotesService.getCaseNotesByApplication(application);

    for (CaseNote caseNote : caseNotes) {
      caseEvents.add(
          CaseEvent.builder(caseNote.getApplicationVersion())
              .withEventType(CaseEventType.CASE_NOTE_ADDED)
              .withMainEventUserWuaId(caseNote.getAddedByWuaId())
              .withEventDateTime(caseNote.getAddedDateTime())
              .withEventText(caseNote.getCaseNoteText())
              .build()
      );
    }
    return caseEvents;
  }

  public CaseEventView getCaseEventViewForNewCaseNote(CaseEvent caseEvent,
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
