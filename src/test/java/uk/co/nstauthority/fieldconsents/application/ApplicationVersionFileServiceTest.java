package uk.co.nstauthority.fieldconsents.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.fileupload.FileUploadTestUtil.DOCUMENT_TYPE;
import static uk.co.nstauthority.fieldconsents.fileupload.FileUploadTestUtil.FILE_DESCRIPTION_1;
import static uk.co.nstauthority.fieldconsents.fileupload.FileUploadTestUtil.FILE_ID;
import static uk.co.nstauthority.fieldconsents.fileupload.FileUploadTestUtil.USAGE_TYPE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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
class ApplicationVersionFileServiceTest {

  @Mock
  private FileService fileService;

  @InjectMocks
  private ApplicationVersionFileService applicationVersionFileService;

  @Captor
  private ArgumentCaptor<Function<FileUsage.Builder, FileUsage>> fileUsageFunctionCaptor;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    this.applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
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

    applicationVersionFileService.saveDocuments(applicationVersion, documentForms, DOCUMENT_TYPE);

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
            applicationVersion.getId().toString(),
            USAGE_TYPE,
            DOCUMENT_TYPE
        );
  }

  @Test
  void saveDocuments_withFilesLinkedToAnotherApplication() {
    var documentForms = new ArrayList<UploadedFileForm>();

    var uploadedFile = new UploadedFile();
    uploadedFile.setId(FILE_ID);
    uploadedFile.setUsageId(String.valueOf(applicationVersion.getId() + 1));
    when(fileService.findAll(Collections.singletonList(FILE_ID))).thenReturn(Collections.singletonList(uploadedFile));

    var form = new UploadedFileForm();
    form.setFileId(FILE_ID);
    form.setFileDescription(FILE_DESCRIPTION_1);
    documentForms.add(form);

    assertThatThrownBy(
        () -> applicationVersionFileService.saveDocuments(applicationVersion, documentForms, DOCUMENT_TYPE))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("File %s does not exist for application version %s".formatted(FILE_ID, applicationVersion.getId()));
  }

  @Test
  void getUploadedFileForms() {
    var uploadedFile = new UploadedFile();
    uploadedFile.setId(FILE_ID);

    when(fileService.findAll(Collections.singleton(FILE_ID))).thenReturn(Collections.singletonList(uploadedFile));

    assertThat(applicationVersionFileService.getUploadedFileForms(Collections.singleton(FILE_ID)))
        .hasSize(1)
        .first()
        .extracting(UploadedFileForm::getFileId).isEqualTo(FILE_ID);
  }

  @Test
  void getUploadedFiles() {
    var usageId = applicationVersion.getId().toString();

    when(fileService.findAll(usageId, USAGE_TYPE, DOCUMENT_TYPE)).thenReturn(Collections.emptyList());

    applicationVersionFileService.getUploadedFiles(applicationVersion, DOCUMENT_TYPE);

    verify(fileService).findAll(usageId, USAGE_TYPE, DOCUMENT_TYPE);
  }

  @Test
  void getFileNotFoundException() {
    var fileId = UUID.randomUUID();
    assertThat(applicationVersionFileService.getFileNotFoundException(fileId, applicationVersion))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("File %s does not exist for application version 1".formatted(fileId));
  }

  @ParameterizedTest
  @EnumSource(ApplicationType.class)
  void throwIfFileDoesNotBelongToApplicationVersion_nullUsage(ApplicationType applicationType) {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(applicationType);
    var uploadedFile = new UploadedFile();

    assertDoesNotThrow(() ->
        applicationVersionFileService.throwIfFileDoesNotBelongToApplicationVersion(uploadedFile, applicationVersion,
            DOCUMENT_TYPE)
    );
  }

  @ParameterizedTest
  @EnumSource(ApplicationType.class)
  void throwIfFileDoesNotBelongToApplicationVersion_fileDoesBelong(ApplicationType applicationType) {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(applicationType);

    var uploadedFile = new UploadedFile();
    uploadedFile.setUsageId(applicationVersion.getApplication().getId().toString());
    uploadedFile.setUsageType(USAGE_TYPE);
    uploadedFile.setDocumentType(DOCUMENT_TYPE);

    assertDoesNotThrow(() ->
        applicationVersionFileService.throwIfFileDoesNotBelongToApplicationVersion(uploadedFile, applicationVersion,
            DOCUMENT_TYPE)
    );
  }

  @ParameterizedTest
  @EnumSource(ApplicationType.class)
  void throwIfFileDoesNotBelongToApplicationVersion_differentUsageId(ApplicationType applicationType) {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(applicationType);

    var applicationVersionId = applicationVersion.getApplication().getId() + 1;

    var uploadedFile = new UploadedFile();
    uploadedFile.setId(FILE_ID);
    uploadedFile.setUsageId(String.valueOf(applicationVersionId));
    uploadedFile.setUsageType(USAGE_TYPE);
    uploadedFile.setDocumentType(DOCUMENT_TYPE);

    assertThatThrownBy(() ->
        applicationVersionFileService.throwIfFileDoesNotBelongToApplicationVersion(uploadedFile, applicationVersion,
            DOCUMENT_TYPE)
    )
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("File %s does not exist".formatted(FILE_ID));
  }

  @ParameterizedTest
  @EnumSource(ApplicationType.class)
  void throwIfFileDoesNotBelongToApplicationVersion_differentUsageType(ApplicationType applicationType) {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(applicationType);

    var applicationVersionId = applicationVersion.getApplication().getId();

    var uploadedFile = new UploadedFile();
    uploadedFile.setId(FILE_ID);
    uploadedFile.setUsageId(String.valueOf(applicationVersionId));
    uploadedFile.setUsageType(USAGE_TYPE + "_different");
    uploadedFile.setDocumentType(DOCUMENT_TYPE);

    assertThatThrownBy(() ->
        applicationVersionFileService.throwIfFileDoesNotBelongToApplicationVersion(uploadedFile, applicationVersion,
            DOCUMENT_TYPE)
    )
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("File %s does not exist".formatted(FILE_ID));
  }

  @ParameterizedTest
  @EnumSource(ApplicationType.class)
  void throwIfFileDoesNotBelongToApplicationVersion_differentDocumentType(ApplicationType applicationType) {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(applicationType);

    var applicationVersionId = applicationVersion.getApplication().getId();

    var uploadedFile = new UploadedFile();
    uploadedFile.setId(FILE_ID);
    uploadedFile.setUsageId(String.valueOf(applicationVersionId));
    uploadedFile.setUsageType(USAGE_TYPE);
    uploadedFile.setDocumentType(DOCUMENT_TYPE + "_different");

    assertThatThrownBy(() ->
        applicationVersionFileService.throwIfFileDoesNotBelongToApplicationVersion(uploadedFile, applicationVersion,
            DOCUMENT_TYPE)
    )
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("File %s does not exist".formatted(FILE_ID));
  }

}
