package uk.co.nstauthority.fieldconsents.file;

import java.util.UUID;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.fivium.fileuploadlibrary.core.UploadedFile;
import uk.co.fivium.fileuploadlibrary.fds.FileDeleteResponse;
import uk.co.fivium.fileuploadlibrary.fds.FileUploadResponse;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.AccessibleByServiceUsers;

@RestController
@RequestMapping("unlinked-files")
@AccessibleByServiceUsers
public class UnlinkedFileController {

  private static final Logger LOGGER = LoggerFactory.getLogger(UnlinkedFileController.class);

  private final FileService fileService;
  private final FieldConsentsFileService fieldConsentsFileService;

  UnlinkedFileController(FileService fileService, FieldConsentsFileService fieldConsentsFileService) {
    this.fileService = fileService;
    this.fieldConsentsFileService = fieldConsentsFileService;
  }

  @PostMapping
  public FileUploadResponse upload(MultipartFile file, ServiceUserDetail userDetail) {
    return fileService.upload(builder -> builder
        .withMultipartFile(file)
        .withUploadedBy(userDetail.wuaId().toString())
        .build());
  }

  @GetMapping("{fileId}")
  public ResponseEntity<InputStreamResource> download(@PathVariable UUID fileId, ServiceUserDetail serviceUserDetail) {
    return findFileAndThen(fileId, serviceUserDetail, fileService::download);
  }

  @PostMapping("delete/{fileId}")
  public FileDeleteResponse delete(@PathVariable UUID fileId, ServiceUserDetail serviceUserDetail) {
    return findFileAndThen(fileId, serviceUserDetail, fileService::delete);
  }

  private <T> T findFileAndThen(UUID fileId, ServiceUserDetail serviceUserDetail, Function<UploadedFile, T> andThen) {
    return fileService.find(fileId)
        .filter(uploadedFile -> !fieldConsentsFileService.fileHasUsage(uploadedFile))
        .filter(uploadedFile -> fieldConsentsFileService.fileBelongsToUser(uploadedFile, serviceUserDetail))
        .map(andThen)
        .orElseThrow(() -> {
          LOGGER.info("User [{}] attempted to access an unlinked file which they didn't upload [{}]",
              serviceUserDetail.wuaId(), fileId
          );
          return new ResponseStatusException(HttpStatus.NOT_FOUND);
        });
  }

}
