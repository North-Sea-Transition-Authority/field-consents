package uk.co.nstauthority.fieldconsents.file;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import uk.co.fivium.fileuploadlibrary.fds.FileUploadResponse;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.AccessibleByServiceUsers;

@RestController
@RequestMapping("unlinked-files")
@AccessibleByServiceUsers
public class UnlinkedFileUploadController {

  private final FileControllerHelperService fileControllerHelperService;

  UnlinkedFileUploadController(
      FileControllerHelperService fileControllerHelperService
  ) {
    this.fileControllerHelperService = fileControllerHelperService;
  }

  @PostMapping
  public ResponseEntity<FileUploadResponse> upload(MultipartFile file, ServiceUserDetail userDetail) {
    return fileControllerHelperService.upload(file, userDetail);
  }

}
