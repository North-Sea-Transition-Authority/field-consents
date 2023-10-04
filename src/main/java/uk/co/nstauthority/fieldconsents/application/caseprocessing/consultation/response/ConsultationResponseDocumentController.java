package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.response;

import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CONSULTATION_RESPONSE;

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
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationFileUsage;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.ActionEndPoint;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationPermission;
import uk.co.nstauthority.fieldconsents.file.FieldConsentsFileService;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@RestController
@RequestMapping("applications/{applicationId}/consultations/{consultationId}/response-documents")
public class ConsultationResponseDocumentController {

  private final FileService fileService;
  private final ApplicationService applicationService;
  private final ConsultationService consultationService;
  private final FieldConsentsFileService fieldConsentsFileService;

  ConsultationResponseDocumentController(
      FileService fileService,
      ApplicationService applicationService,
      ConsultationService consultationService,
      FieldConsentsFileService fieldConsentsFileService
  ) {
    this.fileService = fileService;
    this.applicationService = applicationService;
    this.consultationService = consultationService;
    this.fieldConsentsFileService = fieldConsentsFileService;
  }

  @PostMapping
  @ActionEndPoint(CONSULTATION_RESPONSE)
  public FileUploadResponse upload(
      @PathVariable Integer applicationId,
      @PathVariable Integer consultationId,
      MultipartFile file,
      ServiceUserDetail userDetail
  ) {
    return fileService.upload(builder -> builder
        .withMultipartFile(file)
        .withUploadedBy(userDetail.wuaId().toString())
        .build());
  }

  @GetMapping("{fileId}")
  @HasApplicationPermission(permissions = RolePermission.VIEW_FCS_CASE_PROCESSING_DOCUMENTS)
  public ResponseEntity<InputStreamResource> download(
      @PathVariable Integer applicationId,
      @PathVariable Integer consultationId,
      @PathVariable UUID fileId
  ) {
    return findFileAndThen(applicationId, consultationId, fileId, fileService::download);
  }

  @PostMapping("delete/{fileId}")
  @ActionEndPoint(CONSULTATION_RESPONSE)
  public FileDeleteResponse delete(
      @PathVariable Integer applicationId,
      @PathVariable Integer consultationId,
      @PathVariable UUID fileId
  ) {
    return findFileAndThen(applicationId, consultationId, fileId, fileService::delete);
  }

  private <T> T findFileAndThen(Integer applicationId, Integer consultationId, UUID fileId, Function<UploadedFile, T> andThen) {
    var application = applicationService.getApplicationById(applicationId);
    var consultation = consultationService.getConsultationByIdAndApplication(consultationId, application);
    var fileUsage = ConsultationFileUsage.responseUsageFrom(consultation);
    var uploadedFile = fileService.find(fileId).orElseThrow(() ->
        fieldConsentsFileService.getFileNotFoundException(fileId, fileUsage)
    );

    fieldConsentsFileService.throwIfFileDoesNotBelongToUsage(uploadedFile, fileUsage);

    return andThen.apply(uploadedFile);
  }

}
