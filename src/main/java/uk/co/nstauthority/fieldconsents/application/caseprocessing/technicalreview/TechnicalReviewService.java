package uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview;

import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewStatus.OPEN;

import jakarta.persistence.EntityNotFoundException;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.response.TechnicalReviewFileUsage;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.file.FieldConsentsFileService;

@Service
public class TechnicalReviewService {

  static final UnaryOperator<String> NO_OPEN_TECHNICAL_REVIEW_EXISTS =
      "Open technical review not found for application with version id %s"::formatted;

  static final UnaryOperator<String> OPEN_TECHNICAL_REVIEW_EXISTS =
      "A technical review is already open for the application with version id %s"::formatted;

  static final String TECHNICAL_REVIEW_NOT_FOUND = "Technical review with id %s not found for application id %s";

  private final Clock clock;

  private final TechnicalReviewRepository technicalReviewRepository;

  private final TechnicalReviewAssignmentService technicalReviewAssignmentService;

  private final FieldConsentsFileService fieldConsentsFileService;

  public TechnicalReviewService(Clock clock,
                                TechnicalReviewRepository technicalReviewRepository,
                                TechnicalReviewAssignmentService technicalReviewAssignmentService,
                                FieldConsentsFileService fieldConsentsFileService) {
    this.clock = clock;
    this.technicalReviewRepository = technicalReviewRepository;
    this.technicalReviewAssignmentService = technicalReviewAssignmentService;
    this.fieldConsentsFileService = fieldConsentsFileService;
  }

  public boolean openTechnicalReviewExists(ApplicationVersion applicationVersion) {
    return technicalReviewRepository
        .existsByRequestApplicationVersion_ApplicationAndTechnicalReviewStatus(applicationVersion.getApplication(), OPEN);
  }

  public Optional<TechnicalReview> findOpenTechnicalReview(ApplicationVersion applicationVersion) {
    return technicalReviewRepository
        .findByRequestApplicationVersion_ApplicationAndTechnicalReviewStatus(applicationVersion.getApplication(), OPEN);
  }

  public TechnicalReview getOpenTechnicalReview(ApplicationVersion applicationVersion) {
    return findOpenTechnicalReview(applicationVersion)
        .orElseThrow(() -> new EntityNotFoundException(
            NO_OPEN_TECHNICAL_REVIEW_EXISTS.apply(String.valueOf(applicationVersion.getId()))));
  }

  public TechnicalReview getTechnicalReviewByApplicationAndId(Application application, Integer technicalReviewId) {
    return technicalReviewRepository.findByRequestApplicationVersion_ApplicationAndId(application, technicalReviewId)
        .orElseThrow(() ->
            new EntityNotFoundException(TECHNICAL_REVIEW_NOT_FOUND.formatted(technicalReviewId, application.getId()))
        );
  }

  public Optional<WebUserAccountId> findTechnicalReviewerWuaId(ApplicationVersion applicationVersion) {
    return findOpenTechnicalReview(applicationVersion)
        .map(technicalReview -> WebUserAccountId.from(technicalReview.getTechnicalReviewerWuaId()));
  }

  public TechnicalReviewRequestForm getTechnicalReviewRequestForm(ApplicationVersion applicationVersion) {
    if (openTechnicalReviewExists(applicationVersion)) {
      throw new IllegalStateException(OPEN_TECHNICAL_REVIEW_EXISTS.apply(String.valueOf(applicationVersion.getId())));
    }
    return new TechnicalReviewRequestForm();
  }

  @Transactional
  public void saveTechnicalReviewRequest(
      ApplicationVersion requestForApplicationVersion,
      Instant deadlineInstant,
      String requestText,
      ServiceUserDetail technicalReviewerUser,
      ServiceUserDetail user
  ) {
    var technicalReview = new TechnicalReview();
    technicalReview.setRequestApplicationVersion(requestForApplicationVersion);
    technicalReview.setTechnicalReviewStatus(OPEN);
    technicalReview.setRequestedByWuaId(user.wuaId());
    technicalReview.setRequestedDateTime(clock.instant());
    technicalReview.setRequestText(requestText);
    technicalReview.setDeadlineDateTime(deadlineInstant);
    technicalReviewRepository.save(technicalReview);
    technicalReviewAssignmentService.assignTechnicalReviewer(technicalReview, technicalReviewerUser, user);
  }

  public List<TechnicalReview> getTechnicalReviewsByApplication(Application application) {
    return technicalReviewRepository.findByRequestApplicationVersion_Application(application);
  }

  @Transactional
  public void saveTechnicalReviewResponse(
      ApplicationVersion responseForApplicationVersion,
      TechnicalReview technicalReview,
      ServiceUserDetail serviceUserDetail,
      TechnicalReviewResponseType technicalReviewResponseType,
      String consentConditions,
      String rejectionReason,
      List<UploadedFileForm> documents
  ) {
    technicalReview.setResponseApplicationVersion(responseForApplicationVersion);
    technicalReview.setRespondedByWuaId(serviceUserDetail.wuaId());
    technicalReview.setRespondedDateTime(clock.instant());
    technicalReview.setResponseType(technicalReviewResponseType);

    var isRejected = TechnicalReviewResponseType.REJECT.equals(technicalReviewResponseType);
    technicalReview.setResponseText(isRejected ? rejectionReason : consentConditions);
    technicalReview.setTechnicalReviewStatus(TechnicalReviewStatus.CLOSED);

    var fileUsage = TechnicalReviewFileUsage.responseFrom(technicalReview);
    fieldConsentsFileService.saveDocuments(fileUsage, documents);
    technicalReviewRepository.save(technicalReview);
  }

}
