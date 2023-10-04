package uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.response.document;

import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.TECHNICAL_REVIEWER_SUBMIT_REVIEW;

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
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.ActionEndPoint;
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
  public ResponseEntity<InputStreamResource> download(@PathVariable Integer applicationId,
                                                      @PathVariable Integer technicalReviewId,
                                                      @PathVariable UUID fileId) {
    return findFileAndThen(applicationId, technicalReviewId, fileId, fileService::download);
  }

  @PostMapping
  @ActionEndPoint(TECHNICAL_REVIEWER_SUBMIT_REVIEW)
  public FileUploadResponse upload(@PathVariable Integer applicationId, MultipartFile file, ServiceUserDetail serviceUserDetail) {
    return fileService.upload(builder -> builder
        .withMultipartFile(file)
        .withUploadedBy(String.valueOf(serviceUserDetail.wuaId()))
        .build());
  }

  @PostMapping("delete/{technicalReviewId}/{fileId}")
  @ActionEndPoint(TECHNICAL_REVIEWER_SUBMIT_REVIEW)
  public FileDeleteResponse delete(@PathVariable Integer applicationId,
                                   @PathVariable Integer technicalReviewId,
                                   @PathVariable UUID fileId) {
    return findFileAndThen(applicationId, technicalReviewId, fileId, fileService::delete);
  }

  private <T> T findFileAndThen(Integer applicationId,
                                Integer technicalReviewId,
                                UUID fileId,
                                Function<UploadedFile, T> andThen) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    var technicalReview = technicalReviewService
        .getTechnicalReviewByApplicationAndId(applicationVersion.getApplication(), technicalReviewId);
    var usage = TechnicalReviewFileUsage.responseFrom(technicalReview);
    var uploadedFile = fileService.find(fileId)
        .orElseThrow(() -> fieldConsentsFileService.getFileNotFoundException(fileId, usage));

    fieldConsentsFileService.throwIfFileDoesNotBelongToUsage(uploadedFile, usage);

    return andThen.apply(uploadedFile);
  }

}
