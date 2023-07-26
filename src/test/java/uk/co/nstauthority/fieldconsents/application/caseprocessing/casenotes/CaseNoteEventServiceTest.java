package uk.co.nstauthority.fieldconsents.application.caseprocessing.casenotes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.CaseHistoryEventTestUtil.getPortalUsersDtosMap;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType.CASE_NOTE_ADDED;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.CaseHistoryEventTestUtil;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEvent;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventView;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;

@ExtendWith(MockitoExtension.class)
class CaseNoteEventServiceTest {

  @Mock
  private CaseNotesService caseNotesService;

  @InjectMocks
  private CaseNoteEventService caseNoteEventService;

  private ApplicationVersion applicationVersion;

  private CaseNote firstCaseNote;

  private CaseEvent firstCaseNoteAddedEvent;


  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.FLARE);
    firstCaseNote = CaseHistoryEventTestUtil.getCaseNote(applicationVersion);
    firstCaseNoteAddedEvent = CaseHistoryEventTestUtil.getCaseEventForCaseNoteAdded(firstCaseNote);
  }

  @Test
  void getCaseEvents_withNoCaseNote() {
    when(caseNotesService.getCaseNotesByApplication(applicationVersion.getApplication()))
        .thenReturn(Collections.emptyList());

    assertThat(
        caseNoteEventService.getCaseEvents(applicationVersion.getApplication())
    ).isEmpty();
  }

  @Test
  void getCaseEvents() {
    CaseNote secondCaseNote = CaseHistoryEventTestUtil.getCaseNote(applicationVersion);

    when(caseNotesService.getCaseNotesByApplication(applicationVersion.getApplication()))
        .thenReturn(
            List.of(
                firstCaseNote,
                secondCaseNote
            )
        );

    var secondCaseNoteAddedEvent = CaseHistoryEventTestUtil.getCaseEventForCaseNoteAdded(secondCaseNote);

    List<CaseEvent> caseEvents = caseNoteEventService.getCaseEvents(applicationVersion.getApplication());

    assertThat(caseEvents)
        .containsExactly(
            firstCaseNoteAddedEvent,
            secondCaseNoteAddedEvent
        );
  }
}
