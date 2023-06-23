package uk.co.nstauthority.fieldconsents.application.supportinginformation;

import java.util.UUID;
import java.util.function.Function;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.fivium.fileuploadlibrary.core.UploadedFile;
import uk.co.fivium.fileuploadlibrary.fds.FileDeleteResponse;
import uk.co.fivium.fileuploadlibrary.fds.FileUploadResponse;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionFileService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationPermission;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationStatus;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@RestController
@RequestMapping("applications/{applicationId}/supporting-information/documents")
class SupportingInformationDocumentController {

  private final FileService fileService;
  private final ApplicationVersionService applicationVersionService;
  private final SupportingInformationDocumentService supportingInformationDocumentService;
  private final ApplicationVersionFileService applicationVersionFileService;

  SupportingInformationDocumentController(
      FileService fileService,
      ApplicationVersionService applicationVersionService,
      SupportingInformationDocumentService supportingInformationDocumentService,
      ApplicationVersionFileService applicationVersionFileService) {
    this.fileService = fileService;
    this.applicationVersionService = applicationVersionService;
    this.supportingInformationDocumentService = supportingInformationDocumentService;
    this.applicationVersionFileService = applicationVersionFileService;
  }

  @HasApplicationPermission(permissions = RolePermission.EDIT_FCS_APPLICATIONS)
  @HasApplicationStatus(statuses = ApplicationVersionStatus.IN_PROGRESS)
  @PostMapping
  FileUploadResponse upload(@PathVariable Integer applicationId,
                            MultipartFile file,
                            ServiceUserDetail userDetail) {
    return fileService.upload(builder -> builder
        .withMultipartFile(file)
        .withUploadedBy(userDetail.wuaId().toString())
        .build());
  }

  @HasApplicationPermission(permissions = RolePermission.VIEW_FCS_APPLICATIONS)
  @HasApplicationStatus(statuses = {ApplicationVersionStatus.IN_PROGRESS, ApplicationVersionStatus.SUBMITTED})
  @GetMapping("{fileId}")
  ResponseEntity<InputStreamResource> download(@PathVariable Integer applicationId, @PathVariable UUID fileId) {
    return findFileAndThen(applicationId, fileId, fileService::download);
  }

  @HasApplicationPermission(permissions = RolePermission.EDIT_FCS_APPLICATIONS)
  @HasApplicationStatus(statuses = ApplicationVersionStatus.IN_PROGRESS)
  @PostMapping("{fileId}")
  FileDeleteResponse delete(@PathVariable Integer applicationId, @PathVariable UUID fileId) {
    return findFileAndThen(applicationId, fileId, fileService::delete);
  }

  private <T> T findFileAndThen(Integer applicationId, UUID fileId, Function<UploadedFile, T> andThen) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    var uploadedFile = fileService.find(fileId)
        .orElseThrow(() -> applicationVersionFileService.getFileNotFoundException(fileId, applicationVersion));

    supportingInformationDocumentService.throwIfFileDoesNotBelongToApplicationVersion(uploadedFile, applicationVersion);

    return andThen.apply(uploadedFile);
  }

}
