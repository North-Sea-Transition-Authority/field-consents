package uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.SERVICE_USER_DETAIL_USER_5;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewService.NO_OPEN_TECHNICAL_REVIEW_EXISTS;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewService.OPEN_TECHNICAL_REVIEW_EXISTS;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewService.TECHNICAL_REVIEW_NOT_FOUND;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewStatus.OPEN;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewTestUtil.CASE_OFFICER_USER;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewTestUtil.CURRENT_INSTANT;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewTestUtil.DEADLINE_AHEAD_HOURS;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewTestUtil.TECHNICAL_REVIEW_REQUEST_TEXT;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewTestUtil.TECHNICAL_REVIEW_ID_1;

import jakarta.persistence.EntityNotFoundException;
import java.time.Clock;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.response.TechnicalReviewFileUsage;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.file.FieldConsentsFileService;

@ExtendWith(MockitoExtension.class)
class TechnicalReviewServiceTest {

  @Mock
  private Clock clock;

  @Mock
  private TechnicalReviewRepository technicalReviewRepository;

  @Mock
  private TechnicalReviewAssignmentService technicalReviewAssignmentService;

  @Mock
  private FieldConsentsFileService fieldConsentsFileService;

  @Mock
  private TechnicalReviewEmailService technicalReviewEmailService;

  @InjectMocks
  private TechnicalReviewService technicalReviewService;

  private ApplicationVersion applicationVersion;

  private TechnicalReview technicalReview;

  @Captor
  private ArgumentCaptor<TechnicalReview> technicalReviewCaptor;

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
        .existsByRequestApplicationVersion_ApplicationAndTechnicalReviewStatus(applicationVersion.getApplication(), OPEN))
        .thenReturn(true);

    assertTrue(technicalReviewService.openTechnicalReviewExists(applicationVersion));
  }

  @Test
  void openWithdrawalExists_whenDoesNotExist() {
    when(technicalReviewRepository
        .existsByRequestApplicationVersion_ApplicationAndTechnicalReviewStatus(applicationVersion.getApplication(), OPEN))
        .thenReturn(false);

    assertFalse(technicalReviewService.openTechnicalReviewExists(applicationVersion));
  }

  @Test
  void getTechnicalReviewByApplicationAndId_whenFound() {
    var application = applicationVersion.getApplication();
    when(technicalReviewRepository.findByRequestApplicationVersion_ApplicationAndId(
        application, TECHNICAL_REVIEW_ID_1))
        .thenReturn(Optional.of(technicalReview));

    assertThat(technicalReviewService.getTechnicalReviewByApplicationAndId(application, TECHNICAL_REVIEW_ID_1))
        .isEqualTo(technicalReview);
  }

  @Test
  void getTechnicalReviewByApplicationAndId_whenNotFound() {
    var application = applicationVersion.getApplication();
    when(technicalReviewRepository.findByRequestApplicationVersion_ApplicationAndId(
        application, TECHNICAL_REVIEW_ID_1))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> technicalReviewService.getTechnicalReviewByApplicationAndId(application,
        TECHNICAL_REVIEW_ID_1))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage(TECHNICAL_REVIEW_NOT_FOUND.formatted(TECHNICAL_REVIEW_ID_1, application.getId()));
  }

  @Test
  void findOpenTechnicalReview_whenExists() {
    when(technicalReviewRepository
        .findByRequestApplicationVersion_ApplicationAndTechnicalReviewStatus(applicationVersion.getApplication(), OPEN))
        .thenReturn(Optional.of(technicalReview));

    assertThat(technicalReviewService.findOpenTechnicalReview(applicationVersion))
        .contains(technicalReview);
  }

  @Test
  void findOpenTechnicalReview_whenDoesNotExist() {
    when(technicalReviewRepository
        .findByRequestApplicationVersion_ApplicationAndTechnicalReviewStatus(applicationVersion.getApplication(), OPEN))
        .thenReturn(Optional.empty());

    assertThat(technicalReviewService.findOpenTechnicalReview(applicationVersion))
        .isEmpty();
  }

  @Test
  void getOpenTechnicalReview_whenExists() {
    when(technicalReviewRepository
        .findByRequestApplicationVersion_ApplicationAndTechnicalReviewStatus(applicationVersion.getApplication(), OPEN))
        .thenReturn(Optional.of(technicalReview));

    assertThat(technicalReviewService.getOpenTechnicalReview(applicationVersion))
        .isEqualTo(technicalReview);
  }

  @Test
  void getOpenTechnicalReview_whenDoesNotExist() {
    when(technicalReviewRepository
        .findByRequestApplicationVersion_ApplicationAndTechnicalReviewStatus(applicationVersion.getApplication(), OPEN))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> technicalReviewService.getOpenTechnicalReview(applicationVersion))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage(NO_OPEN_TECHNICAL_REVIEW_EXISTS.apply(String.valueOf(applicationVersion.getId())));
  }

  @Test
  void findTechnicalReviewerWuaId_whenExists() {
    technicalReview.setTechnicalReviewerWuaId(CASE_OFFICER_USER.wuaId());
    when(technicalReviewRepository
        .findByRequestApplicationVersion_ApplicationAndTechnicalReviewStatus(applicationVersion.getApplication(), OPEN))
        .thenReturn(Optional.of(technicalReview));

    assertThat(technicalReviewService.findTechnicalReviewerWuaId(applicationVersion))
        .contains(WebUserAccountId.from(technicalReview.getTechnicalReviewerWuaId()));
  }

  @Test
  void findTechnicalReviewerWuaId_whenDoesNotExist() {
    when(technicalReviewRepository
        .findByRequestApplicationVersion_ApplicationAndTechnicalReviewStatus(applicationVersion.getApplication(), OPEN))
        .thenReturn(Optional.empty());

    assertThat(technicalReviewService.findTechnicalReviewerWuaId(applicationVersion))
        .isEmpty();
  }

  @Test
  void getTechnicalReviewsByApplication_whenNoReviews() {
    when(technicalReviewRepository.findByRequestApplicationVersion_Application(applicationVersion.getApplication()))
        .thenReturn(Collections.emptyList());

    assertThat(technicalReviewService.getTechnicalReviewsByApplication(applicationVersion.getApplication()))
        .isEmpty();
  }

  @Test
  void getTechnicalReviewsByApplication_whenMultipleReviews() {
    var closedTechnicalReview = TechnicalReviewTestUtil
        .getClosedTechnicalReview(applicationVersion, SERVICE_USER_DETAIL_USER_5, clock);

    when(technicalReviewRepository.findByRequestApplicationVersion_Application(applicationVersion.getApplication()))
        .thenReturn(List.of(
            closedTechnicalReview,
            technicalReview
          )
        );

    assertThat(technicalReviewService.getTechnicalReviewsByApplication(applicationVersion.getApplication()))
        .containsExactly(
            closedTechnicalReview,
            technicalReview
        );
  }

  @Test
  void getTechnicalReviewRequestForm_noOpenTechnicalReviewExists() {
    when(technicalReviewRepository.existsByRequestApplicationVersion_ApplicationAndTechnicalReviewStatus(applicationVersion.getApplication(), OPEN))
        .thenReturn(false);

    assertThat(technicalReviewService.getTechnicalReviewRequestForm(applicationVersion))
        .usingRecursiveComparison()
        .isEqualTo(new TechnicalReviewRequestForm());
  }

  @Test
  void getTechnicalReviewRequestForm_technicalReviewAlreadyOpen() {
    when(technicalReviewRepository.existsByRequestApplicationVersion_ApplicationAndTechnicalReviewStatus(applicationVersion.getApplication(), OPEN))
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
        CASE_OFFICER_USER
    );

    verify(technicalReviewRepository).save(technicalReviewCaptor.capture());
    var actualTechnicalReview = technicalReviewCaptor.getValue();

    assertThat(actualTechnicalReview)
        .usingRecursiveComparison()
        .isEqualTo(technicalReview);

    verify(technicalReviewAssignmentService, times(1))
        .assignTechnicalReviewer(actualTechnicalReview, SERVICE_USER_DETAIL_USER_5, CASE_OFFICER_USER);
  }

  @ParameterizedTest
  @EnumSource(TechnicalReviewResponseType.class)
  void saveTechnicalReviewResponse(TechnicalReviewResponseType responseType) {
    var consentCondition = "consent condition";
    var rejectionReason = "rejection reason";
    var uploadedFileForms = Collections.singletonList(new UploadedFileForm());

    technicalReview.setId(TECHNICAL_REVIEW_ID_1);
    technicalReviewService.saveTechnicalReviewResponse(
        applicationVersion,
        technicalReview,
        SERVICE_USER_DETAIL_USER_5,
        responseType,
        consentCondition,
        rejectionReason,
        uploadedFileForms
    );

    verify(fieldConsentsFileService).saveDocuments(TechnicalReviewFileUsage.responseFrom(technicalReview), uploadedFileForms);

    verify(technicalReviewRepository).save(technicalReviewCaptor.capture());
    assertThat(technicalReviewCaptor.getValue())
        .extracting(
            TechnicalReview::getRespondedByWuaId,
            TechnicalReview::getRespondedDateTime,
            TechnicalReview::getResponseText,
            TechnicalReview::getTechnicalReviewStatus
        ).containsExactly(
            SERVICE_USER_DETAIL_USER_5.wuaId(),
            CURRENT_INSTANT,
            TechnicalReviewResponseType.REJECT.equals(responseType) ? rejectionReason : consentCondition,
            TechnicalReviewStatus.CLOSED
        );

    verify(technicalReviewEmailService).sendTechnicalReviewResponseEmail(technicalReview, SERVICE_USER_DETAIL_USER_5);
  }

  @ParameterizedTest
  @EnumSource(TechnicalReviewResponseType.class)
  void saveTechnicalReviewResponse_whenSendTechnicalReviewResponseEmailFails_thenTechnicalReviewResponseIsStillSubmitted(TechnicalReviewResponseType responseType) {
    var consentCondition = "consent condition";
    var rejectionReason = "rejection reason";
    var uploadedFileForms = Collections.singletonList(new UploadedFileForm());

    technicalReview.setId(TECHNICAL_REVIEW_ID_1);

    // WHEN the email service call throws an exception
    doThrow(new RuntimeException("Failed to send email"))
        .when(technicalReviewEmailService)
        .sendTechnicalReviewResponseEmail(technicalReview, SERVICE_USER_DETAIL_USER_5);

    // THEN it will be caught by the caller and not re-thrown
    assertDoesNotThrow(
        () -> technicalReviewService.saveTechnicalReviewResponse(
            applicationVersion,
            technicalReview,
            SERVICE_USER_DETAIL_USER_5,
            responseType,
            consentCondition,
            rejectionReason,
            uploadedFileForms
        )
    );

    verify(fieldConsentsFileService).saveDocuments(TechnicalReviewFileUsage.responseFrom(technicalReview), uploadedFileForms);

    verify(technicalReviewRepository).save(technicalReviewCaptor.capture());
    assertThat(technicalReviewCaptor.getValue())
        .extracting(
            TechnicalReview::getRespondedByWuaId,
            TechnicalReview::getRespondedDateTime,
            TechnicalReview::getResponseText,
            TechnicalReview::getTechnicalReviewStatus
        ).containsExactly(
            SERVICE_USER_DETAIL_USER_5.wuaId(),
            CURRENT_INSTANT,
            TechnicalReviewResponseType.REJECT.equals(responseType) ? rejectionReason : consentCondition,
            TechnicalReviewStatus.CLOSED
        );

    verify(technicalReviewEmailService).sendTechnicalReviewResponseEmail(technicalReview, SERVICE_USER_DETAIL_USER_5);
  }
}
