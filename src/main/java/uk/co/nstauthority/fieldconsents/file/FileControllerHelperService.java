package uk.co.nstauthority.fieldconsents.file;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.fivium.fileuploadlibrary.core.FileSource;
import uk.co.fivium.fileuploadlibrary.core.UploadedFile;
import uk.co.fivium.fileuploadlibrary.fds.FileDeleteResponse;
import uk.co.fivium.fileuploadlibrary.fds.FileUploadComponentAttributes;
import uk.co.fivium.fileuploadlibrary.fds.FileUploadResponse;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@Service
public class FileControllerHelperService {

  private final FileService fileService;
  private final FieldConsentsFileService fieldConsentsFileService;

  FileControllerHelperService(
      FileService fileService,
      FieldConsentsFileService fieldConsentsFileService
  ) {
    this.fileService = fileService;
    this.fieldConsentsFileService = fieldConsentsFileService;
  }

  public <T> FileUploadComponentAttributes fileUploadComponentAttributes(
      List<UploadedFileForm> uploadedFileForms,
      Class<T> fileControllerClass,
      Function<T, ResponseEntity<InputStreamResource>> downloadFunction,
      Function<T, ResponseEntity<FileDeleteResponse>> deleteFunction
  ) {
    return fileService.getFileUploadAttributes()
        .withPath("form.documents")
        .withUploadUrl(ReverseRouter.route(on(UnlinkedFileUploadController.class).upload(null, null)))
        .withDownloadUrl(ReverseRouter.route(downloadFunction.apply(on(fileControllerClass))))
        .withDeleteUrl(ReverseRouter.route(deleteFunction.apply(on(fileControllerClass))))
        .withExistingFiles(uploadedFileForms)
        .build();
  }

  public ResponseEntity<FileUploadResponse> upload(MultipartFile multipartFile, ServiceUserDetail userDetail) {
    var result = fileService.upload(builder -> builder
        .withFileSource(FileSource.fromMultipartFile(multipartFile))
        .withUploadedBy(userDetail.wuaId().toString())
        .build());

    return ResponseEntity.ok(result);
  }

  public ResponseEntity<InputStreamResource> download(
      UUID fileId,
      Supplier<FieldConsentsFileUsage> usageSupplier,
      ServiceUserDetail userDetail
  ) {
    return findFileAndThen(
      fileId,
      usageSupplier,
      userDetail,
      fileService::download
    );
  }

  public ResponseEntity<FileDeleteResponse> delete(
      UUID fileId,
      Supplier<FieldConsentsFileUsage> usageSupplier,
      ServiceUserDetail userDetail
  ) {
    return findFileAndThen(
        fileId,
        usageSupplier,
        userDetail,
        uploadedFile -> ResponseEntity.ok(fileService.delete(uploadedFile))
    );
  }

  <T> ResponseEntity<T> findFileAndThen(
      UUID fileId,
      Supplier<FieldConsentsFileUsage> usageSupplier,
      ServiceUserDetail userDetail,
      Function<UploadedFile, ResponseEntity<T>> andThen
  ) {
    var uploadedFileOptional = fileService.find(fileId);

    if (uploadedFileOptional.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    var uploadedFile = uploadedFileOptional.get();

    if (fieldConsentsFileService.fileHasUsage(uploadedFile)) {
      fieldConsentsFileService.throwIfFileDoesNotBelongToUsage(uploadedFile, usageSupplier.get());
      return andThen.apply(uploadedFile);
    }

    if (fieldConsentsFileService.fileBelongsToUser(uploadedFile, userDetail)) {
      return andThen.apply(uploadedFile);
    }

    return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
  }

}
