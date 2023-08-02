package uk.co.nstauthority.fieldconsents.application.caseprocessing.casenotes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityNotFoundException;
import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.file.FieldConsentsFileService;
import uk.co.nstauthority.fieldconsents.fileupload.FileUploadTestUtil;

@ExtendWith(MockitoExtension.class)
class CaseNotesServiceTest {

  @Mock
  private Clock clock;
  
  @Mock
  private CaseNotesRepository caseNotesRepository;
  
  @Mock
  private FieldConsentsFileService fieldConsentsFileService;

  @InjectMocks
  private CaseNotesService caseNotesService;

  @Captor
  private ArgumentCaptor<CaseNote> caseNoteCaptor;

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
    var caseNoteId = 1;

    when(clock.instant()).thenReturn(currentTime);
    doAnswer(invocation -> {
      var caseNote = invocation.getArgument(0, CaseNote.class);
      caseNote.setId(caseNoteId); // the id is needed for the file usage
      return caseNote;
    })
        .when(caseNotesRepository)
        .save(any(CaseNote.class));

    var caseNoteText = "case note text";
    var caseNoteDocuments = FileUploadTestUtil.validDocumentForms;
    var caseNote = getCaseNote(currentTime, caseNoteText);
    caseNote.setId(caseNoteId);

    caseNotesService.saveCaseNote(applicationVersion, caseNoteText, caseNoteDocuments, user);

    var fileUsage = CaseNoteFileUsage.fromCaseNote(caseNote);
    verify(fieldConsentsFileService).saveDocuments(fileUsage, caseNoteDocuments);
    verify(caseNotesRepository).save(caseNoteCaptor.capture());
    var actualCaseNote = caseNoteCaptor.getValue();

    assertThat(actualCaseNote).usingRecursiveComparison().isEqualTo(caseNote);
  }

  @Test
  void getCaseNotesByApplication_whenNoNote() {
    when(caseNotesRepository.findByApplicationVersion_Application(applicationVersion.getApplication())).thenReturn(
        Collections.emptyList()
    );

    assertThat(caseNotesService.getCaseNotesByApplication(applicationVersion.getApplication())).isEmpty();
  }

  @Test
  void getCaseNotesByApplication_whenNoteExists() {
    var caseNote1 = new CaseNote();
    caseNote1.setCaseNoteText("This is a case note.");
    caseNote1.setAddedByWuaId(user.wuaId());
    caseNote1.setAddedDateTime(Instant.now());
    caseNote1.setApplicationVersion(applicationVersion);

    var caseNote2 = new CaseNote();
    caseNote2.setCaseNoteText("This is another case note.");
    caseNote2.setAddedByWuaId(user.wuaId());
    caseNote2.setAddedDateTime(Instant.now());
    caseNote2.setApplicationVersion(applicationVersion);

    when(caseNotesRepository.findByApplicationVersion_Application(applicationVersion.getApplication()))
        .thenReturn(List.of(caseNote1, caseNote2)
    );

    assertThat(caseNotesService.getCaseNotesByApplication(applicationVersion.getApplication()))
        .containsExactly(caseNote1, caseNote2);
  }

  @Test
  void getByIdAndApplication() {
    var application = applicationVersion.getApplication();
    var caseNote = new CaseNote();
    var caseNoteId = 1;

    when(caseNotesRepository.findByIdAndApplicationVersion_Application(caseNoteId, application))
        .thenReturn(Optional.of(caseNote));

    assertThat(caseNotesService.getCaseNoteByIdAndApplication(caseNoteId, application))
        .isEqualTo(caseNote);
  }

  @Test
  void getByIdAndApplication_notFound() {
    var application = applicationVersion.getApplication();
    var caseNoteId = 1;

    when(caseNotesRepository.findByIdAndApplicationVersion_Application(caseNoteId, application))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> caseNotesService.getCaseNoteByIdAndApplication(caseNoteId, application))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("Case note [%s] not found for application [%s]".formatted(caseNoteId, application.getId()));
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
