package uk.co.nstauthority.fieldconsents.application.supportinginformation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.unit.DataSize;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.fivium.fileuploadlibrary.core.UploadedFile;
import uk.co.fivium.fileuploadlibrary.fds.FileUploadComponentAttributes;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionFileService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@ExtendWith(MockitoExtension.class)
class SupportingInformationDocumentServiceTest {

  private static final String DOCUMENT_TYPE = "supporting-document";

  @Mock
  private FileService fileService;

  @Mock
  private ApplicationVersionFileService applicationVersionFileService;

  @InjectMocks
  private SupportingInformationDocumentService supportingInformationDocumentService;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    this.applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
  }

  @Test
  void saveDocuments() {
    var forms = Collections.<UploadedFileForm>emptyList();
    supportingInformationDocumentService.saveDocuments(applicationVersion, forms);
    verify(applicationVersionFileService).saveDocuments(applicationVersion, forms, DOCUMENT_TYPE);
  }

  @Test
  void throwIfFileDoesNotBelongToApplicationVersion() {
    assertDoesNotThrow(() ->
        supportingInformationDocumentService.throwIfFileDoesNotBelongToApplicationVersion(new UploadedFile(), applicationVersion)
    );
  }

  @Test
  void throwIfFileDoesNotBelongToApplicationVersion_doesThrow() {
    var uploadedFile = new UploadedFile();

    doThrow(new RuntimeException("exception message"))
        .when(applicationVersionFileService)
        .throwIfFileDoesNotBelongToApplicationVersion(uploadedFile, applicationVersion, DOCUMENT_TYPE);

    assertThatThrownBy(() -> supportingInformationDocumentService
        .throwIfFileDoesNotBelongToApplicationVersion(uploadedFile, applicationVersion))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("exception message");
  }

  @Test
  void getUploadedFiles() {
    var uploadedFiles = Collections.<UploadedFile>emptyList();
    when(applicationVersionFileService.getUploadedFiles(applicationVersion, DOCUMENT_TYPE))
        .thenReturn(uploadedFiles);

    assertThat(supportingInformationDocumentService.getUploadedFiles(applicationVersion))
        .isEqualTo(uploadedFiles);
  }

  @Test
  void fileUploadComponentAttributes() {
    var existingFiles = Collections.<UploadedFileForm>emptyList();
    var applicationId = applicationVersion.getApplication().getId();

    var attributesBuilder = FileUploadComponentAttributes.newBuilder()
        .withMaximumSize(DataSize.ofMegabytes(50))
        .withAllowedExtensions(Collections.singleton("pdf"));
    when(fileService.getFileUploadAttributes()).thenReturn(attributesBuilder);

    assertThat(supportingInformationDocumentService.fileUploadComponentAttributes(applicationVersion, existingFiles))
        .extracting(
            FileUploadComponentAttributes::path,
            FileUploadComponentAttributes::uploadUrl,
            FileUploadComponentAttributes::downloadUrl,
            FileUploadComponentAttributes::deleteUrl,
            FileUploadComponentAttributes::existingFiles
        )
        .containsExactly(
            "form.supportingDocuments",
            ReverseRouter.route(on(SupportingInformationDocumentController.class).upload(applicationId, null, null)),
            ReverseRouter.route(on(SupportingInformationDocumentController.class).download(applicationId, null)),
            ReverseRouter.route(on(SupportingInformationDocumentController.class).delete(applicationId, null)),
            existingFiles
        );
  }

  @Test
  void copyUploadedFiles() {
    var targetApplicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
    supportingInformationDocumentService.copyUploadedFiles(applicationVersion, targetApplicationVersion);

    verify(applicationVersionFileService, times(1))
        .copyUploadedFiles(applicationVersion, targetApplicationVersion, DOCUMENT_TYPE);
  }
}
