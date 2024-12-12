package uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.response;

import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.TECHNICAL_REVIEWER_SUBMIT_REVIEW;

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
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.ActionEndPoint;
import uk.co.nstauthority.fieldconsents.authorisation.role.grouped.UserIsRegulatorCaseProcessor;
import uk.co.nstauthority.fieldconsents.file.FieldConsentsFileUsage;
import uk.co.nstauthority.fieldconsents.file.FileControllerHelperService;

@RestController
@RequestMapping("applications/{applicationId}/technical-reviews/{technicalReviewId}/response/files")
public class TechnicalReviewResponseFileController {

  private final ApplicationService applicationService;
  private final TechnicalReviewService technicalReviewService;
  private final FileControllerHelperService fileControllerHelperService;

  TechnicalReviewResponseFileController(
      ApplicationService applicationService,
      TechnicalReviewService technicalReviewService,
      FileControllerHelperService fileControllerHelperService
  ) {
    this.applicationService = applicationService;
    this.technicalReviewService = technicalReviewService;
    this.fileControllerHelperService = fileControllerHelperService;
  }

  @GetMapping("/{fileId}")
  @UserIsRegulatorCaseProcessor
  public ResponseEntity<InputStreamResource> download(
      @PathVariable Integer applicationId,
      @PathVariable Integer technicalReviewId,
      @PathVariable UUID fileId,
      ServiceUserDetail userDetail
  ) {
    return fileControllerHelperService.download(fileId, () -> getFileUsage(applicationId, technicalReviewId), userDetail);
  }

  @PostMapping("/delete/{fileId}")
  @ActionEndPoint(TECHNICAL_REVIEWER_SUBMIT_REVIEW)
  public ResponseEntity<FileDeleteResponse> delete(
      @PathVariable Integer applicationId,
      @PathVariable Integer technicalReviewId,
      @PathVariable UUID fileId,
      ServiceUserDetail userDetail
  ) {
    return fileControllerHelperService.delete(fileId, () -> getFileUsage(applicationId, technicalReviewId), userDetail);
  }

  private FieldConsentsFileUsage getFileUsage(Integer applicationId, Integer technicalReviewId) {
    var application = applicationService.getApplicationById(applicationId);
    var technicalReview = technicalReviewService.getTechnicalReviewByApplicationAndId(application, technicalReviewId);
    return TechnicalReviewFileUsage.responseFrom(technicalReview);
  }

}
