package uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.response.document;

import java.util.UUID;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewService;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationPermission;
import uk.co.nstauthority.fieldconsents.file.FieldConsentsFileService;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@RestController
@RequestMapping("applications/{applicationId}/technical-review-response-documents")
public class TechnicalReviewResponseDocumentController {

  private final FileService fileService;
  private final ApplicationVersionService applicationVersionService;
  private final TechnicalReviewService technicalReviewService;
  private final FieldConsentsFileService fieldConsentsFileService;

  TechnicalReviewResponseDocumentController(
      FileService fileService,
      ApplicationVersionService applicationVersionService,
      TechnicalReviewService technicalReviewService,
      FieldConsentsFileService fieldConsentsFileService
  ) {
    this.fileService = fileService;
    this.applicationVersionService = applicationVersionService;
    this.technicalReviewService = technicalReviewService;
    this.fieldConsentsFileService = fieldConsentsFileService;
  }

  @GetMapping("{technicalReviewId}/{fileId}")
  @HasApplicationPermission(permissions = RolePermission.VIEW_FCS_CASE_PROCESSING_DOCUMENTS)
  public ResponseEntity<InputStreamResource> download(
      @PathVariable Integer applicationId,
      @PathVariable Integer technicalReviewId,
      @PathVariable UUID fileId
  ) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    var application = applicationVersion.getApplication();
    var technicalReview = technicalReviewService.getTechnicalReviewByApplicationAndId(application, technicalReviewId);
    var usage = TechnicalReviewFileUsage.responseFrom(technicalReview);
    var uploadedFile = fileService.find(fileId)
        .orElseThrow(() -> fieldConsentsFileService.getFileNotFoundException(fileId, usage));

    fieldConsentsFileService.throwIfFileDoesNotBelongToUsage(uploadedFile, usage);

    return fileService.download(uploadedFile);
  }

}
