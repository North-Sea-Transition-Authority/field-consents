package uk.co.nstauthority.fieldconsents.application.supportinginformation;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Collection;
import java.util.List;
import org.springframework.stereotype.Service;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.fivium.fileuploadlibrary.core.UploadedFile;
import uk.co.fivium.fileuploadlibrary.fds.FileUploadComponentAttributes;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionFileService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@Service
public class SupportingInformationDocumentService {

  private static final String DOCUMENT_TYPE = "supporting-document";

  private final FileService fileService;
  private final ApplicationVersionFileService applicationVersionFileService;

  SupportingInformationDocumentService(FileService fileService,
                                       ApplicationVersionFileService applicationVersionFileService) {
    this.fileService = fileService;
    this.applicationVersionFileService = applicationVersionFileService;
  }

  void saveDocuments(ApplicationVersion applicationVersion, Collection<UploadedFileForm> uploadedFileForms) {
    applicationVersionFileService.saveDocuments(applicationVersion, uploadedFileForms, DOCUMENT_TYPE);
  }

  void throwIfFileDoesNotBelongToApplicationVersion(UploadedFile uploadedFile, ApplicationVersion applicationVersion) {
    applicationVersionFileService.throwIfFileDoesNotBelongToApplicationVersion(
        uploadedFile,
        applicationVersion,
        DOCUMENT_TYPE
    );
  }

  List<UploadedFile> getUploadedFiles(ApplicationVersion applicationVersion) {
    return applicationVersionFileService.getUploadedFiles(applicationVersion, DOCUMENT_TYPE);
  }

  FileUploadComponentAttributes fileUploadComponentAttributes(
      ApplicationVersion applicationVersion,
      List<UploadedFileForm> existingFiles
  ) {
    var controller = SupportingInformationDocumentController.class;
    var applicationId = applicationVersion.getApplication().getId();

    return fileService.getFileUploadAttributes()
        .withPath("form.supportingDocuments")
        .withUploadUrl(ReverseRouter.route(on(controller).upload(applicationId, null, null)))
        .withDownloadUrl(ReverseRouter.route(on(controller).download(applicationId, null)))
        .withDeleteUrl(ReverseRouter.route(on(controller).delete(applicationId, null)))
        .withExistingFiles(existingFiles)
        .build();
  }

  public void copyUploadedFiles(ApplicationVersion sourceApplicationVersion,
                                ApplicationVersion targetApplicationVersion) {
    applicationVersionFileService.copyUploadedFiles(sourceApplicationVersion, targetApplicationVersion, DOCUMENT_TYPE);
  }
}
