package uk.co.nstauthority.fieldconsents.application.caseprocessing.casenotes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.fileupload.FileUploadTestUtil.CASE_NOTE_USAGE_TYPE;
import static uk.co.nstauthority.fieldconsents.fileupload.FileUploadTestUtil.DOCUMENT_TYPE;
import static uk.co.nstauthority.fieldconsents.fileupload.FileUploadTestUtil.FILE_DESCRIPTION_1;
import static uk.co.nstauthority.fieldconsents.fileupload.FileUploadTestUtil.FILE_ID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.fivium.fileuploadlibrary.core.FileUsage;
import uk.co.fivium.fileuploadlibrary.core.UploadedFile;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;

@ExtendWith(MockitoExtension.class)
class CaseNotesFileServiceTest {
  private static final int CASE_NOTE_ID = 1;
  @Mock
  private FileService fileService;

  @InjectMocks
  private CaseNotesFileService caseNotesFileService;

  @Captor
  private ArgumentCaptor<Function<FileUsage.Builder, FileUsage>> fileUsageFunctionCaptor;

  private CaseNote caseNote;

  @BeforeEach
  void setUp() {
    this.caseNote = new CaseNote(CASE_NOTE_ID);
  }

  @Test
  void saveDocuments() {
    var documentForms = new ArrayList<UploadedFileForm>();

    var uploadedFile = new UploadedFile();
    uploadedFile.setId(FILE_ID);
    when(fileService.findAll(Collections.singletonList(FILE_ID))).thenReturn(Collections.singletonList(uploadedFile));

    var form = new UploadedFileForm();
    form.setFileId(FILE_ID);
    form.setFileDescription(FILE_DESCRIPTION_1);
    documentForms.add(form);

    caseNotesFileService.saveDocuments(caseNote, documentForms, DOCUMENT_TYPE);

    verify(fileService).updateUsageAndDescription(
        eq(uploadedFile),
        fileUsageFunctionCaptor.capture(),
        eq(FILE_DESCRIPTION_1)
    );
    var fileUsage = fileUsageFunctionCaptor.getValue().apply(FileUsage.newBuilder());
    assertThat(fileUsage)
        .extracting(
            FileUsage::usageId,
            FileUsage::usageType,
            FileUsage::documentType
        )
        .containsExactly(
            caseNote.getId().toString(),
            CASE_NOTE_USAGE_TYPE,
            DOCUMENT_TYPE
        );
  }

  @Test
  void saveDocuments_withFilesLinkedToAnotherApplication() {
    var documentForms = new ArrayList<UploadedFileForm>();

    var uploadedFile = new UploadedFile();
    uploadedFile.setId(FILE_ID);
    uploadedFile.setUsageId(String.valueOf(caseNote.getId() + 1));
    when(fileService.findAll(Collections.singletonList(FILE_ID))).thenReturn(Collections.singletonList(uploadedFile));

    var form = new UploadedFileForm();
    form.setFileId(FILE_ID);
    form.setFileDescription(FILE_DESCRIPTION_1);
    documentForms.add(form);

    assertThatThrownBy(
        () -> caseNotesFileService.saveDocuments(caseNote, documentForms, DOCUMENT_TYPE))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("File %s does not exist for case note with id %s".formatted(FILE_ID, caseNote.getId()));
  }

  @Test
  void getUploadedFileForms() {
    var uploadedFile = new UploadedFile();
    uploadedFile.setId(FILE_ID);

    when(fileService.findAll(Collections.singleton(FILE_ID))).thenReturn(Collections.singletonList(uploadedFile));

    assertThat(caseNotesFileService.getUploadedFileForms(Collections.singleton(FILE_ID)))
        .hasSize(1)
        .first()
        .extracting(UploadedFileForm::getFileId).isEqualTo(FILE_ID);
  }

  @Test
  void getFileNotFoundException() {
    var fileId = UUID.randomUUID();
    assertThat(caseNotesFileService.getFileNotFoundException(fileId, caseNote))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("File %s does not exist for case note with id 1".formatted(fileId));
  }

  @Test
  void throwIfFileDoesNotBelongToCaseNote_nullUsage() {
    var uploadedFile = new UploadedFile();

    assertDoesNotThrow(() ->
        caseNotesFileService.throwIfFileDoesNotBelongToCaseNote(uploadedFile, caseNote, DOCUMENT_TYPE)
    );
  }

  @Test
  void throwIfFileDoesNotBelongToCaseNote_differentUsageId() {
    var uploadedFile = new UploadedFile();
    uploadedFile.setId(FILE_ID);
    uploadedFile.setUsageId(String.valueOf(caseNote.getId()));
    uploadedFile.setUsageType(CASE_NOTE_USAGE_TYPE);
    uploadedFile.setDocumentType(DOCUMENT_TYPE);

    assertThatThrownBy(() ->
        caseNotesFileService.throwIfFileDoesNotBelongToCaseNote(uploadedFile, caseNote, DOCUMENT_TYPE)
    )
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("File %s does not exist".formatted(FILE_ID));
  }

  @Test
  void throwIfFileDoesNotBelongToCaseNote_differentUsageType() {
    var uploadedFile = new UploadedFile();
    uploadedFile.setId(FILE_ID);
    uploadedFile.setUsageId(String.valueOf(String.valueOf(caseNote.getId())));
    uploadedFile.setUsageType(CASE_NOTE_USAGE_TYPE + "_different");
    uploadedFile.setDocumentType(DOCUMENT_TYPE);

    assertThatThrownBy(() ->
        caseNotesFileService.throwIfFileDoesNotBelongToCaseNote(uploadedFile, caseNote, DOCUMENT_TYPE)
    )
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("File %s does not exist".formatted(FILE_ID));
  }

  @Test
  void throwIfFileDoesNotBelongToCaseNote_differentDocumentType() {
    var uploadedFile = new UploadedFile();
    uploadedFile.setId(FILE_ID);
    uploadedFile.setUsageId(String.valueOf(caseNote.getId()));
    uploadedFile.setUsageType(CASE_NOTE_USAGE_TYPE);
    uploadedFile.setDocumentType(DOCUMENT_TYPE + "_different");

    assertThatThrownBy(() ->
        caseNotesFileService.throwIfFileDoesNotBelongToCaseNote(uploadedFile, caseNote, DOCUMENT_TYPE)
    )
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("File %s does not exist".formatted(FILE_ID));
  }
}
