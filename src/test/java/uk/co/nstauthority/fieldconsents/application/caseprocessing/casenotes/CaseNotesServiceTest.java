package uk.co.nstauthority.fieldconsents.application.caseprocessing.casenotes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.fileupload.FileUploadTestUtil;

@ExtendWith(MockitoExtension.class)
class CaseNotesServiceTest {

  @Mock
  private Clock clock;
  
  @Mock
  private CaseNotesRepository caseNotesRepository;
  
  @Mock
  private CaseNotesDocumentService caseNotesDocumentService;

  @InjectMocks
  private CaseNotesService caseNotesService;

  private ApplicationVersion applicationVersion;

  private ServiceUserDetail user;


  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.FLARE);
    user = ServiceUserDetailTestUtil.Builder().build();
  }

  @Test
  void saveCaseNoteForm() {
    var currentTime = Instant.now();
    when(clock.instant()).thenReturn(currentTime);

    var caseNoteText = "case note text";
    var caseNoteDocuments = FileUploadTestUtil.validDocumentForms;
    var caseNote = getCaseNote(currentTime, caseNoteText);
    var caseNoteForm = getCaseNoteForm(caseNoteText, caseNoteDocuments);

    caseNotesService.saveCaseNote(applicationVersion, caseNoteText, caseNoteDocuments, user);

    ArgumentCaptor<CaseNote> caseNoteArgumentCaptor = ArgumentCaptor.forClass(CaseNote.class);
    verify(caseNotesDocumentService).saveDocuments(any(CaseNote.class), anyList());
    verify(caseNotesRepository, times(1)).save(caseNoteArgumentCaptor.capture());
    CaseNote actualCaseNote = caseNoteArgumentCaptor.getValue();

    assertThat(actualCaseNote).usingRecursiveComparison().isEqualTo(caseNote);
  }

  @NotNull
  private CaseNoteForm getCaseNoteForm(String caseNoteText, List<UploadedFileForm> caseNoteDocuments) {
    var caseNoteForm = new CaseNoteForm();
    caseNoteForm.setCaseNoteText(caseNoteText);
    caseNoteForm.setCaseNoteDocuments(caseNoteDocuments);
    return caseNoteForm;
  }

  @NotNull
  private CaseNote getCaseNote(Instant currentTime, String caseNoteText) {
    var caseNote = new CaseNote();
    caseNote.setCaseNoteText(caseNoteText);
    caseNote.setAddedByWuaId(user.wuaId());
    caseNote.setAddedDateTime(currentTime);
    caseNote.setApplicationVersion(applicationVersion);
    return caseNote;
  }
}
