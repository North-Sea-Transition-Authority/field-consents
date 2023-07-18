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
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;

@Service
public class TechnicalReviewService {

  static final UnaryOperator<String> NO_OPEN_TECHNICAL_REVIEW_EXISTS =
      "Open technical review not found for application with version id %s"::formatted;

  static final UnaryOperator<String> OPEN_TECHNICAL_REVIEW_EXISTS =
      "A technical review is already open for the application with version id %s"::formatted;

  private final Clock clock;

  private final TechnicalReviewRepository technicalReviewRepository;

  private final TechnicalReviewAssignmentService technicalReviewAssignmentService;

  public TechnicalReviewService(Clock clock,
                                TechnicalReviewRepository technicalReviewRepository,
                                TechnicalReviewAssignmentService technicalReviewAssignmentService) {
    this.clock = clock;
    this.technicalReviewRepository = technicalReviewRepository;
    this.technicalReviewAssignmentService = technicalReviewAssignmentService;
  }

  public boolean openTechnicalReviewExists(ApplicationVersion applicationVersion) {
    return technicalReviewRepository
        .existsByApplicationVersion_ApplicationAndTechnicalReviewStatus(applicationVersion.getApplication(), OPEN);
  }

  public Optional<TechnicalReview> findOpenTechnicalReview(ApplicationVersion applicationVersion) {
    return technicalReviewRepository
        .findByApplicationVersion_ApplicationAndTechnicalReviewStatus(applicationVersion.getApplication(), OPEN);
  }

  public TechnicalReview getOpenTechnicalReview(ApplicationVersion applicationVersion) {
    return findOpenTechnicalReview(applicationVersion)
        .orElseThrow(() -> new EntityNotFoundException(
            NO_OPEN_TECHNICAL_REVIEW_EXISTS.apply(String.valueOf(applicationVersion.getId()))));
  }

  public TechnicalReviewRequestForm getTechnicalReviewRequestForm(ApplicationVersion applicationVersion) {
    if (openTechnicalReviewExists(applicationVersion)) {
      throw new IllegalStateException(OPEN_TECHNICAL_REVIEW_EXISTS.apply(String.valueOf(applicationVersion.getId())));
    }
    return new TechnicalReviewRequestForm();
  }

  @Transactional
  public void saveTechnicalReviewRequest(ApplicationVersion applicationVersion,
                                         Instant deadlineInstant,
                                         String requestText,
                                         ServiceUserDetail technicalReviewerUser,
                                         ServiceUserDetail user) {
    var technicalReview = new TechnicalReview();
    technicalReview.setApplicationVersion(applicationVersion);
    technicalReview.setTechnicalReviewStatus(OPEN);
    technicalReview.setRequestedByWuaId(user.wuaId());
    technicalReview.setRequestedDateTime(clock.instant());
    technicalReview.setRequestText(requestText);
    technicalReview.setDeadlineDateTime(deadlineInstant);
    technicalReviewRepository.save(technicalReview);
    technicalReviewAssignmentService.assignTechnicalReviewer(technicalReview, technicalReviewerUser, user);
  }

  public List<TechnicalReview> getTechnicalReviewsByApplication(Application application) {
    return technicalReviewRepository.findByApplicationVersion_Application(application);
  }
}
