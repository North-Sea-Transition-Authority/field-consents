package uk.co.nstauthority.fieldconsents.application.caseprocessing.casenotes;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uk.co.fivium.fileuploadlibrary.FileUploadLibraryUtils;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.fivium.fileuploadlibrary.core.FileUsage;
import uk.co.fivium.fileuploadlibrary.core.UploadedFile;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;

@Service
public class CaseNotesFileService {

  private final FileService fileService;

  CaseNotesFileService(FileService fileService) {
    this.fileService = fileService;
  }

  public void saveDocuments(
      CaseNote caseNote,
      Collection<UploadedFileForm> uploadedFileForms,
      String documentType
  ) {
    var fileIds = uploadedFileForms.stream().map(UploadedFileForm::getFileId).toList();
    var uploadedFiles = fileService.findAll(fileIds);

    uploadedFiles.forEach(uploadedFile ->
        throwIfFileDoesNotBelongToCaseNote(uploadedFile, caseNote, documentType));

    var descriptions = FileUploadLibraryUtils.getFileDescriptionsByFileId(uploadedFileForms);

    for (var uploadedFile : uploadedFiles) {
      fileService.updateUsageAndDescription(
          uploadedFile,
          usageBuilder -> buildFileUsage(usageBuilder, caseNote, documentType),
          descriptions.get(uploadedFile.getId())
      );
    }
  }

  public List<UploadedFileForm> getUploadedFileForms(Collection<UUID> fileIds) {
    return fileService
        .findAll(fileIds)
        .stream()
        .map(FileUploadLibraryUtils::asForm)
        .toList();
  }

  public ResponseStatusException getFileNotFoundException(UUID fileId, CaseNote caseNote) {
    return new ResponseStatusException(NOT_FOUND, "File %s does not exist for case note with id %s"
        .formatted(fileId, caseNote.getId()));
  }

  public void throwIfFileDoesNotBelongToCaseNote(
      UploadedFile uploadedFile,
      CaseNote caseNote,
      String documentType
  ) {
    if (Objects.isNull(uploadedFile.getUsageId())
        && Objects.isNull(uploadedFile.getUsageType())
        && Objects.isNull(uploadedFile.getDocumentType())) {
      return;
    }

    // TODO: For FCS-354: Case history, when docs are viewed, we need to make sure a case note has been saved already.
    //       Look at the ApplicationVersionFileService pattern
    throw getFileNotFoundException(uploadedFile.getId(), caseNote);
  }

  private FileUsage buildFileUsage(
      FileUsage.Builder fileUsageBuilder,
      CaseNote caseNote,
      String documentType
  ) {
    return fileUsageBuilder
        .withUsageId(getUsageId(caseNote))
        .withUsageType(getUsageType(caseNote))
        .withDocumentType(documentType)
        .build();
  }

  private String getUsageId(CaseNote caseNote) {
    return caseNote.getId().toString();
  }

  private String getUsageType(CaseNote caseNote) {
    return caseNote.getClass().getSimpleName();
  }
}
