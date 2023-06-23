package uk.co.nstauthority.fieldconsents.application;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uk.co.fivium.fileuploadlibrary.FileUploadLibraryUtils;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.fivium.fileuploadlibrary.core.FileUsage;
import uk.co.fivium.fileuploadlibrary.core.UploadedFile;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;

@Service
public class ApplicationVersionFileService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationVersionFileService.class);

  private final FileService fileService;

  ApplicationVersionFileService(FileService fileService) {
    this.fileService = fileService;
  }

  public void saveDocuments(
      ApplicationVersion applicationVersion,
      Collection<UploadedFileForm> uploadedFileForms,
      String documentType
  ) {
    var fileIds = uploadedFileForms.stream().map(UploadedFileForm::getFileId).toList();
    var uploadedFiles = fileService.findAll(fileIds);

    uploadedFiles.forEach(uploadedFile ->
        throwIfFileDoesNotBelongToApplicationVersion(uploadedFile, applicationVersion, documentType));

    var descriptions = FileUploadLibraryUtils.getFileDescriptionsByFileId(uploadedFileForms);

    for (var uploadedFile : uploadedFiles) {
      fileService.updateUsageAndDescription(
          uploadedFile,
          usageBuilder -> buildFileUsage(usageBuilder, applicationVersion, documentType),
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

  public List<UploadedFile> getUploadedFiles(ApplicationVersion applicationVersion, String documentType) {
    return fileService.findAll(getUsageId(applicationVersion), getUsageType(applicationVersion), documentType);
  }

  public ResponseStatusException getFileNotFoundException(UUID fileId, ApplicationVersion applicationVersion) {
    return new ResponseStatusException(NOT_FOUND, "File %s does not exist for application version %s"
        .formatted(fileId, applicationVersion.getId()));
  }

  public void throwIfFileDoesNotBelongToApplicationVersion(
      UploadedFile uploadedFile,
      ApplicationVersion applicationVersion,
      String documentType
  ) {
    if (Objects.isNull(uploadedFile.getUsageId())
        && Objects.isNull(uploadedFile.getUsageType())
        && Objects.isNull(uploadedFile.getDocumentType())) {
      return;
    }
    var logMessage = "An attempt was made to download a file not linked to the correct application version";

    if (!uploadedFile.getUsageId().equals(getUsageId(applicationVersion))) {
      LOGGER.warn(logMessage);
      throw getFileNotFoundException(uploadedFile.getId(), applicationVersion);
    }

    if (!uploadedFile.getUsageType().equals(getUsageType(applicationVersion))) {
      LOGGER.warn(logMessage);
      throw getFileNotFoundException(uploadedFile.getId(), applicationVersion);
    }

    if (!uploadedFile.getDocumentType().equals(documentType)) {
      LOGGER.warn(logMessage);
      throw getFileNotFoundException(uploadedFile.getId(), applicationVersion);
    }
  }

  private FileUsage buildFileUsage(
      FileUsage.Builder fileUsageBuilder,
      ApplicationVersion applicationVersion,
      String documentType
  ) {
    return fileUsageBuilder
        .withUsageId(getUsageId(applicationVersion))
        .withUsageType(getUsageType(applicationVersion))
        .withDocumentType(documentType)
        .build();
  }

  private String getUsageId(ApplicationVersion applicationVersion) {
    return applicationVersion.getId().toString();
  }

  private String getUsageType(ApplicationVersion applicationVersion) {
    return applicationVersion.getClass().getSimpleName();
  }

}
