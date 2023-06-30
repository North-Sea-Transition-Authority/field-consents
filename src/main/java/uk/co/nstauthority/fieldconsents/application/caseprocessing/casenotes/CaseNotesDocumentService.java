package uk.co.nstauthority.fieldconsents.application.caseprocessing.casenotes;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Collection;
import java.util.List;
import org.springframework.stereotype.Service;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.fivium.fileuploadlibrary.core.UploadedFile;
import uk.co.fivium.fileuploadlibrary.fds.FileUploadComponentAttributes;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@Service
public class CaseNotesDocumentService {

  private static final String DOCUMENT_TYPE = "case-note-document";

  private final FileService fileService;

  private final CaseNotesFileService caseNotesFileService;

  CaseNotesDocumentService(FileService fileService,
                           CaseNotesFileService caseNotesFileService) {
    this.fileService = fileService;
    this.caseNotesFileService = caseNotesFileService;
  }

  void saveDocuments(CaseNote caseNote, Collection<UploadedFileForm> uploadedFileForms) {
    caseNotesFileService.saveDocuments(caseNote, uploadedFileForms, DOCUMENT_TYPE);
  }

  void throwIfFileDoesNotBelongToCaseNote(UploadedFile uploadedFile, CaseNote caseNote) {
    caseNotesFileService.throwIfFileDoesNotBelongToCaseNote(
        uploadedFile,
        caseNote,
        DOCUMENT_TYPE
    );
  }

  FileUploadComponentAttributes fileUploadComponentAttributes(
      ApplicationVersion applicationVersion,
      List<UploadedFileForm> existingFiles
  ) {
    var controller = CaseNotesDocumentController.class;
    var applicationId = applicationVersion.getApplication().getId();

    return fileService.getFileUploadAttributes()
        .withPath("form.caseNoteDocuments")
        .withUploadUrl(ReverseRouter.route(on(controller).upload(applicationId, null, null)))
        .withDownloadUrl(ReverseRouter.route(on(controller).download(applicationId, null)))
        .withDeleteUrl(ReverseRouter.route(on(controller).delete(applicationId, null)))
        .withExistingFiles(existingFiles)
        .build();
  }
}
