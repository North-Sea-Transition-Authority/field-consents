package uk.co.nstauthority.fieldconsents.application.caseprocessing.casenotes;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEvent;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType;

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
}
