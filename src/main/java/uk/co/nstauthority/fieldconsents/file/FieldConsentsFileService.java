package uk.co.nstauthority.fieldconsents.file;

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
import uk.co.fivium.fileuploadlibrary.fds.FileUploadComponentAttributes;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;

@Service
public class FieldConsentsFileService {

  private static final Logger LOGGER = LoggerFactory.getLogger(FieldConsentsFileService.class);

  private final FileService fileService;

  FieldConsentsFileService(FileService fileService) {
    this.fileService = fileService;
  }

  public void saveDocuments(FieldConsentsFileUsage fileUsage, Collection<UploadedFileForm> uploadedFileForms) {
    var fileIds = uploadedFileForms.stream().map(UploadedFileForm::getFileId).toList();
    var uploadedFiles = fileService.findAll(fileIds);

    uploadedFiles.forEach(uploadedFile -> throwIfFileDoesNotBelongToUsage(uploadedFile, fileUsage));

    var descriptionsByFileId = FileUploadLibraryUtils.getFileDescriptionsByFileId(uploadedFileForms);

    for (var uploadedFile : uploadedFiles) {
      fileService.updateUsageAndDescription(
          uploadedFile,
          builder -> buildFileUsage(builder, fileUsage),
          descriptionsByFileId.get(uploadedFile.getId())
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

  public ResponseStatusException getFileNotFoundException(UUID fileId, FieldConsentsFileUsage fileUsage) {
    return new ResponseStatusException(NOT_FOUND, "File [%s] does not exist for %s [%s]"
        .formatted(fileId, fileUsage.usageType(), fileUsage.usageId()));
  }

  public List<UploadedFile> getUploadedFiles(FieldConsentsFileUsage fileUsage) {
    return fileService.findAll(fileUsage.usageId(), fileUsage.usageType(), fileUsage.documentType());
  }

  public void throwIfFileDoesNotBelongToUsage(UploadedFile uploadedFile, FieldConsentsFileUsage fileUsage) {
    if (Objects.isNull(uploadedFile.getUsageId())
        && Objects.isNull(uploadedFile.getUsageType())
        && Objects.isNull(uploadedFile.getDocumentType())) {
      return;
    }

    if (!Objects.equals(uploadedFile.getUsageId(), fileUsage.usageId())
        || !Objects.equals(uploadedFile.getUsageType(), fileUsage.usageType())
        || !Objects.equals(uploadedFile.getDocumentType(), fileUsage.documentType())) {
      LOGGER.warn("Access was attempted to a file not linked to the correct {}", fileUsage.usageType());
      throw getFileNotFoundException(uploadedFile.getId(), fileUsage);
    }
  }

  private FileUsage buildFileUsage(FileUsage.Builder fileUsageBuilder, FieldConsentsFileUsage fileUsage) {
    return fileUsageBuilder
        .withUsageId(fileUsage.usageId())
        .withUsageType(fileUsage.usageType())
        .withDocumentType(fileUsage.documentType())
        .build();
  }

  public FileUploadComponentAttributes.Builder fileUploadComponentAttributesBuilder() {
    return fileService.getFileUploadAttributes();
  }

  public void copyUploadedFiles(FieldConsentsFileUsage sourceUsage, FieldConsentsFileUsage targetUsage) {
    for (var uploadedFile : getUploadedFiles(sourceUsage)) {
      fileService.copy(uploadedFile, builder -> buildFileUsage(builder, targetUsage));
    }
  }

}
