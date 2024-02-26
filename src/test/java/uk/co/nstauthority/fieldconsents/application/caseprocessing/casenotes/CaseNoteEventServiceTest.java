package uk.co.nstauthority.fieldconsents.application.caseprocessing.casenotes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Collections;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.fileuploadlibrary.core.UploadedFile;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.CaseHistoryEventTestUtil;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEvent;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType;
import uk.co.nstauthority.fieldconsents.file.FieldConsentsFileService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.summary.SummaryFileView;

@ExtendWith(MockitoExtension.class)
class CaseNoteEventServiceTest {

  private static final int CASE_NOTE_ID = 1;

  @Mock
  private CaseNotesService caseNotesService;

  @Mock
  private FieldConsentsFileService fieldConsentsFileService;

  @InjectMocks
  private CaseNoteEventService caseNoteEventService;

  private ApplicationVersion applicationVersion;

  private CaseNote firstCaseNote;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.FLARE);
    firstCaseNote = CaseHistoryEventTestUtil.getCaseNote(applicationVersion);
    firstCaseNote.setId(CASE_NOTE_ID);
  }

  @Test
  void getCaseEvents_withNoCaseNote() {
    when(caseNotesService.getCaseNotesByApplication(applicationVersion.getApplication())).thenReturn(
        Collections.emptyList());
    assertThat(caseNoteEventService.getCaseEvents(applicationVersion.getApplication())).isEmpty();
  }

  @Test
  void getCaseEvents() {
    var uploadedFile = new UploadedFile();
    uploadedFile.setId(UUID.randomUUID());
    uploadedFile.setName("my-document.pdf");
    uploadedFile.setDescription("my document description");

    when(fieldConsentsFileService.getUploadedFiles(CaseNoteFileUsage.fromCaseNote(firstCaseNote)))
        .thenReturn(Collections.singletonList(uploadedFile));
    when(caseNotesService.getCaseNotesByApplication(applicationVersion.getApplication()))
        .thenReturn(Collections.singletonList(firstCaseNote));

    var summaryFileView = new SummaryFileView(
        uploadedFile.getName(),
        uploadedFile.getDescription(),
        ReverseRouter.route(on(CaseNoteFileController.class).download(
            applicationVersion.getApplication().getId(),
            CASE_NOTE_ID,
            uploadedFile.getId(),
            null
        ))
    );

    var caseEvents = caseNoteEventService.getCaseEvents(applicationVersion.getApplication());
    assertThat(caseEvents)
        .hasSize(1)
        .first()
        .extracting(
            CaseEvent::applicationVersion,
            CaseEvent::eventType,
            CaseEvent::mainEventUserWuaId,
            CaseEvent::otherEventUserWuaId,
            CaseEvent::eventDateTime,
            CaseEvent::eventText,
            CaseEvent::summaryFileViews
        )
        .containsExactly(
            firstCaseNote.getApplicationVersion(),
            CaseEventType.CASE_NOTE_ADDED,
            firstCaseNote.getAddedByWuaId(),
            null,
            firstCaseNote.getAddedDateTime(),
            firstCaseNote.getCaseNoteText(),
            Collections.singletonList(summaryFileView)
        );
  }

}
