package uk.co.nstauthority.fieldconsents.application.supportinginformation;

import java.util.UUID;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.co.fivium.fileuploadlibrary.fds.FileDeleteResponse;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionFileUsage;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationStatus;
import uk.co.nstauthority.fieldconsents.authorisation.role.grouped.UserCanEditApplication;
import uk.co.nstauthority.fieldconsents.authorisation.role.grouped.UserCanViewApplication;
import uk.co.nstauthority.fieldconsents.file.FileControllerHelperService;

@RestController
@RequestMapping("/application-versions/{applicationVersionId}/supporting-information/files")
class SupportingInformationFileController {

  private final FileControllerHelperService fileControllerHelperService;

  SupportingInformationFileController(FileControllerHelperService fileControllerHelperService) {
    this.fileControllerHelperService = fileControllerHelperService;
  }

  @GetMapping("/{fileId}")
  @UserCanViewApplication
  public ResponseEntity<InputStreamResource> download(
      @PathVariable Integer applicationVersionId,
      @PathVariable UUID fileId,
      ServiceUserDetail userDetail
  ) {
    return fileControllerHelperService.download(
        fileId,
        () -> ApplicationVersionFileUsage.supportingDocumentFrom(applicationVersionId),
        userDetail
    );
  }

  @PostMapping("/delete/{fileId}")
  @UserCanEditApplication
  @HasApplicationStatus(statuses = ApplicationVersionStatus.IN_PROGRESS)
  public ResponseEntity<FileDeleteResponse> delete(
      @PathVariable Integer applicationVersionId,
      @PathVariable UUID fileId,
      ServiceUserDetail userDetail
  ) {
    return fileControllerHelperService.delete(
        fileId,
        () -> ApplicationVersionFileUsage.supportingDocumentFrom(applicationVersionId),
        userDetail
    );
  }

}
