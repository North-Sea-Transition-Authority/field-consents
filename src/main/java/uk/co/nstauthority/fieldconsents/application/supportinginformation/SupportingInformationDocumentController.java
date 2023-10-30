package uk.co.nstauthority.fieldconsents.application.supportinginformation;

import java.util.UUID;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionFileUsage;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationPermission;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationStatus;
import uk.co.nstauthority.fieldconsents.file.FieldConsentsFileService;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@RestController
@RequestMapping("applications/{applicationId}/supporting-information/documents")
class SupportingInformationDocumentController {

  private final FileService fileService;
  private final ApplicationVersionService applicationVersionService;
  private final FieldConsentsFileService fieldConsentsFileService;

  SupportingInformationDocumentController(
      FileService fileService,
      ApplicationVersionService applicationVersionService,
      FieldConsentsFileService fieldConsentsFileService
  ) {
    this.fileService = fileService;
    this.applicationVersionService = applicationVersionService;
    this.fieldConsentsFileService = fieldConsentsFileService;
  }

  @HasApplicationPermission(permissions = RolePermission.VIEW_FCS_APPLICATIONS)
  @HasApplicationStatus(statuses = {ApplicationVersionStatus.IN_PROGRESS, ApplicationVersionStatus.SUBMITTED})
  @GetMapping("{fileId}")
  ResponseEntity<InputStreamResource> download(@PathVariable Integer applicationId, @PathVariable UUID fileId) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    var usage = ApplicationVersionFileUsage.supportingDocumentFrom(applicationVersion);
    var uploadedFile = fileService.find(fileId)
        .orElseThrow(() -> fieldConsentsFileService.getFileNotFoundException(fileId, usage));

    fieldConsentsFileService.throwIfFileDoesNotBelongToUsage(uploadedFile, usage);

    return fileService.download(uploadedFile);
  }

}
