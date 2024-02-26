package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.response;

import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CONSULTATION_RESPONSE;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.VIEW_FCS_CASE_PROCESSING_DOCUMENTS;

import java.util.UUID;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.co.fivium.fileuploadlibrary.fds.FileDeleteResponse;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationFileUsage;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.ActionEndPoint;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationPermission;
import uk.co.nstauthority.fieldconsents.file.FieldConsentsFileUsage;
import uk.co.nstauthority.fieldconsents.file.FileControllerHelperService;

@RestController
@RequestMapping("/applications/{applicationId}/consultations/{consultationId}/response/files")
public class ConsultationResponseFileController {

  private final ApplicationService applicationService;
  private final ConsultationService consultationService;
  private final FileControllerHelperService fileControllerHelperService;

  ConsultationResponseFileController(
      ApplicationService applicationService,
      ConsultationService consultationService,
      FileControllerHelperService fileControllerHelperService
  ) {
    this.applicationService = applicationService;
    this.consultationService = consultationService;
    this.fileControllerHelperService = fileControllerHelperService;
  }

  @GetMapping("/{fileId}")
  @HasApplicationPermission(permissions = VIEW_FCS_CASE_PROCESSING_DOCUMENTS)
  public ResponseEntity<InputStreamResource> download(
      @PathVariable Integer applicationId,
      @PathVariable Integer consultationId,
      @PathVariable UUID fileId,
      ServiceUserDetail userDetail
  ) {
    return fileControllerHelperService.download(fileId, () -> getFileUsage(applicationId, consultationId), userDetail);
  }

  @PostMapping("/delete/{fileId}")
  @ActionEndPoint(CONSULTATION_RESPONSE)
  public ResponseEntity<FileDeleteResponse> delete(
      @PathVariable Integer applicationId,
      @PathVariable Integer consultationId,
      @PathVariable UUID fileId,
      ServiceUserDetail userDetail
  ) {
    return fileControllerHelperService.delete(fileId, () -> getFileUsage(applicationId, consultationId), userDetail);
  }

  private FieldConsentsFileUsage getFileUsage(Integer applicationId, Integer consultationId) {
    var application = applicationService.getApplicationById(applicationId);
    var consultation = consultationService.getConsultationByIdAndApplication(consultationId, application);
    return ConsultationFileUsage.responseUsageFrom(consultation);
  }

}
