package uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.SERVICE_USER_DETAIL_USER_5;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewService.NO_OPEN_TECHNICAL_REVIEW_EXISTS;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewService.OPEN_TECHNICAL_REVIEW_EXISTS;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewStatus.OPEN;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewTestUtil.CURRENT_INSTANT;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewTestUtil.DEADLINE_AHEAD_HOURS;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewTestUtil.TECHNICAL_REVIEW_REQUEST_TEXT;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewTestUtil.USER;

import jakarta.persistence.EntityNotFoundException;
import java.time.Clock;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

@ExtendWith(MockitoExtension.class)
class TechnicalReviewServiceTest {

  @Mock
  private Clock clock;

  @Mock
  private TechnicalReviewRepository technicalReviewRepository;

  @Mock
  private TechnicalReviewAssignmentService technicalReviewAssignmentService;

  @InjectMocks
  private TechnicalReviewService technicalReviewService;

  private ApplicationVersion applicationVersion;

  private TechnicalReview technicalReview;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.FLARE);
    when(clock.instant()).thenReturn(CURRENT_INSTANT);
    technicalReview = TechnicalReviewTestUtil
        .getOpenTechnicalReview(applicationVersion, SERVICE_USER_DETAIL_USER_5, clock);
    technicalReview.setTechnicalReviewerWuaId(null);
  }

  @Test
  void openTechnicalReviewExists_whenExists() {
    when(technicalReviewRepository
        .existsByApplicationVersion_ApplicationAndTechnicalReviewStatus(applicationVersion.getApplication(), OPEN))
        .thenReturn(true);

    assertTrue(technicalReviewService.openTechnicalReviewExists(applicationVersion));
  }

  @Test
  void openWithdrawalExists_whenDoesNotExist() {
    when(technicalReviewRepository
        .existsByApplicationVersion_ApplicationAndTechnicalReviewStatus(applicationVersion.getApplication(), OPEN))
        .thenReturn(false);

    assertFalse(technicalReviewService.openTechnicalReviewExists(applicationVersion));
  }

  @Test
  void findOpenTechnicalReview_whenExists() {
    when(technicalReviewRepository
        .findByApplicationVersion_ApplicationAndTechnicalReviewStatus(applicationVersion.getApplication(), OPEN))
        .thenReturn(Optional.of(technicalReview));

    assertThat(technicalReviewService.findOpenTechnicalReview(applicationVersion))
        .contains(technicalReview);
  }

  @Test
  void findOpenTechnicalReview_whenDoesNotExist() {
    when(technicalReviewRepository
        .findByApplicationVersion_ApplicationAndTechnicalReviewStatus(applicationVersion.getApplication(), OPEN))
        .thenReturn(Optional.empty());

    assertThat(technicalReviewService.findOpenTechnicalReview(applicationVersion))
        .isEmpty();
  }

  @Test
  void getOpenTechnicalReview_whenExists() {
    when(technicalReviewRepository
        .findByApplicationVersion_ApplicationAndTechnicalReviewStatus(applicationVersion.getApplication(), OPEN))
        .thenReturn(Optional.of(technicalReview));

    assertThat(technicalReviewService.getOpenTechnicalReview(applicationVersion))
        .isEqualTo(technicalReview);
  }

  @Test
  void getOpenTechnicalReview_whenDoesNotExist() {
    when(technicalReviewRepository
        .findByApplicationVersion_ApplicationAndTechnicalReviewStatus(applicationVersion.getApplication(), OPEN))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> technicalReviewService.getOpenTechnicalReview(applicationVersion))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage(NO_OPEN_TECHNICAL_REVIEW_EXISTS.apply(String.valueOf(applicationVersion.getId())));
  }

  @Test
  void getTechnicalReviewRequestForm_noOpenTechnicalReviewExists() {
    when(technicalReviewRepository.existsByApplicationVersion_ApplicationAndTechnicalReviewStatus(applicationVersion.getApplication(), OPEN))
        .thenReturn(false);

    assertThat(technicalReviewService.getTechnicalReviewRequestForm(applicationVersion))
        .usingRecursiveComparison()
        .isEqualTo(new TechnicalReviewRequestForm());
  }

  @Test
  void getTechnicalReviewRequestForm_technicalReviewAlreadyOpen() {
    when(technicalReviewRepository.existsByApplicationVersion_ApplicationAndTechnicalReviewStatus(applicationVersion.getApplication(), OPEN))
        .thenReturn(true);

    assertThatThrownBy(() -> technicalReviewService.getTechnicalReviewRequestForm(applicationVersion))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage(OPEN_TECHNICAL_REVIEW_EXISTS.apply(String.valueOf(applicationVersion.getId())));
  }

  @Test
  void saveTechnicalReviewRequest() {
    when(clock.instant()).thenReturn(CURRENT_INSTANT);

    technicalReviewService.saveTechnicalReviewRequest(applicationVersion,
        clock.instant().plus(DEADLINE_AHEAD_HOURS, ChronoUnit.HOURS),
        TECHNICAL_REVIEW_REQUEST_TEXT,
        SERVICE_USER_DETAIL_USER_5,
        USER
    );

    ArgumentCaptor<TechnicalReview> technicalReviewArgumentCaptor = ArgumentCaptor.forClass(TechnicalReview.class);
    verify(technicalReviewRepository, times(1)).save(technicalReviewArgumentCaptor.capture());
    var actualTechnicalReview = technicalReviewArgumentCaptor.getValue();

    assertThat(actualTechnicalReview)
        .usingRecursiveComparison()
        .isEqualTo(technicalReview);

    verify(technicalReviewAssignmentService, times(1))
        .assignTechnicalReviewer(actualTechnicalReview, SERVICE_USER_DETAIL_USER_5, USER);
  }
}
