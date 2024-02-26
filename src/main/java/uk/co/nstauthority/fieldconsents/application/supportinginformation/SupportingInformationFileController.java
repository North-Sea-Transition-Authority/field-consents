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
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationPermission;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationStatus;
import uk.co.nstauthority.fieldconsents.file.FieldConsentsFileUsage;
import uk.co.nstauthority.fieldconsents.file.FileControllerHelperService;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@RestController
@RequestMapping("/applications/{applicationId}/supporting-information/files")
class SupportingInformationFileController {

  private final ApplicationVersionService applicationVersionService;
  private final FileControllerHelperService fileControllerHelperService;

  SupportingInformationFileController(
      ApplicationVersionService applicationVersionService,
      FileControllerHelperService fileControllerHelperService
  ) {
    this.applicationVersionService = applicationVersionService;
    this.fileControllerHelperService = fileControllerHelperService;
  }

  @GetMapping("/{fileId}")
  @HasApplicationPermission(permissions = RolePermission.VIEW_FCS_APPLICATIONS)
  public ResponseEntity<InputStreamResource> download(
      @PathVariable Integer applicationId,
      @PathVariable UUID fileId,
      ServiceUserDetail userDetail
  ) {
    return fileControllerHelperService.download(fileId, () -> getFileUsage(applicationId), userDetail);
  }

  @PostMapping("/delete/{fileId}")
  @HasApplicationPermission(permissions = RolePermission.EDIT_FCS_APPLICATIONS)
  @HasApplicationStatus(statuses = ApplicationVersionStatus.IN_PROGRESS)
  public ResponseEntity<FileDeleteResponse> delete(
      @PathVariable Integer applicationId,
      @PathVariable UUID fileId,
      ServiceUserDetail userDetail
  ) {
    return fileControllerHelperService.delete(fileId, () -> getFileUsage(applicationId), userDetail);
  }

  private FieldConsentsFileUsage getFileUsage(Integer applicationId) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    return ApplicationVersionFileUsage.supportingDocumentFrom(applicationVersion);
  }

}
