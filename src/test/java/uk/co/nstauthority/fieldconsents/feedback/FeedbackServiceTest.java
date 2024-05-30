package uk.co.nstauthority.fieldconsents.feedback;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_ID;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_REFERENCE;
import static uk.co.nstauthority.fieldconsents.integrationtest.ApplicationDataItemViewIntegrationTestUtil.USER_DETAIL;

import java.time.Clock;
import java.time.Instant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import uk.co.fivium.feedbackmanagementservice.client.CannotSendFeedbackException;
import uk.co.fivium.feedbackmanagementservice.client.FeedbackClientService;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.summary.ApplicationSummaryController;
import uk.co.nstauthority.fieldconsents.mvc.AbsoluteUrlService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@ExtendWith(MockitoExtension.class)
class FeedbackServiceTest {

  private static final String CONTEXT_PATH = "/service-name";
  private static final Instant CURRENT_INSTANT = Instant.now();
  private static String expectedApplicationUrl;

  @Mock
  private Clock clock;

  @Mock
  private FeedbackClientService feedbackClientService;

  @Mock
  private ApplicationService applicationService;

  @Mock
  private AbsoluteUrlService absoluteUrlService;

  @InjectMocks
  private FeedbackService feedbackService;

  @Captor
  private ArgumentCaptor<Feedback> feedbackArgumentCaptor;

  private FeedbackForm form;

  private ApplicationVersion applicationVersion;

  @BeforeAll
  static void setUp() {
    var request = new MockHttpServletRequest();
    request.setContextPath(CONTEXT_PATH);
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

    expectedApplicationUrl = ServletUriComponentsBuilder.fromCurrentContextPath().toUriString() +
        ReverseRouter.route(on(ApplicationSummaryController.class)
            .getApplicationSummary(APPLICATION_ID, null));
  }

  @BeforeEach
  void setup() {
    form = new FeedbackForm();
    form.setServiceRating(ServiceFeedbackRating.SATISFIED.name());
    form.getFeedback().setInputValue("feedback test");

    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);

    when(clock.instant()).thenReturn(CURRENT_INSTANT);
  }

  @Test
  void saveFeedback_cannotSendFeedbackException() throws CannotSendFeedbackException {
    when(feedbackClientService.saveFeedback(any(Feedback.class)))
        .thenThrow(new CannotSendFeedbackException("test exception"));

    feedbackService.saveFeedback(form.getServiceRating(), form.getFeedback().getInputValue(), USER_DETAIL);

    verify(feedbackClientService).saveFeedback(feedbackArgumentCaptor.capture());
    
    assertThat(feedbackArgumentCaptor.getValue()).extracting(
        Feedback::getSubmitterName,
        Feedback::getSubmitterEmail,
        Feedback::getServiceRating,
        Feedback::getComment,
        Feedback::getGivenDatetime,
        Feedback::getTransactionId,
        Feedback::getTransactionReference,
        Feedback::getTransactionLink
    ).containsExactly(
        USER_DETAIL.displayName(),
        USER_DETAIL.emailAddress(),
        form.getServiceRating(),
        form.getFeedback().getInputValue(),
        CURRENT_INSTANT,
        null,
        null,
        null
    );
  }

  @Test
  void saveFeedback() throws CannotSendFeedbackException {
    assertDoesNotThrow(() -> feedbackService.saveFeedback(
        form.getServiceRating(),
        form.getFeedback().getInputValue(),
        USER_DETAIL)
    );

    verify(feedbackClientService).saveFeedback(feedbackArgumentCaptor.capture());

    assertThat(feedbackArgumentCaptor.getValue()).extracting(
        Feedback::getSubmitterName,
        Feedback::getSubmitterEmail,
        Feedback::getServiceRating,
        Feedback::getComment,
        Feedback::getGivenDatetime,
        Feedback::getTransactionId,
        Feedback::getTransactionReference,
        Feedback::getTransactionLink
    ).containsExactly(
        USER_DETAIL.displayName(),
        USER_DETAIL.emailAddress(),
        form.getServiceRating(),
        form.getFeedback().getInputValue(),
        CURRENT_INSTANT,
        null,
        null,
        null
    );
  }

  @Test
  void saveFeedback_withApplicationVersion() throws CannotSendFeedbackException {
    when(applicationService.generateApplicationReference(applicationVersion)).thenReturn(APPLICATION_REFERENCE);
    when(absoluteUrlService.getAbsoluteUrl(anyString())).thenReturn(expectedApplicationUrl);

    assertDoesNotThrow(() -> feedbackService.saveFeedback(
        applicationVersion,
        form.getServiceRating(),
        form.getFeedback().getInputValue(),
        USER_DETAIL)
    );

    verify(feedbackClientService).saveFeedback(feedbackArgumentCaptor.capture());

    assertThat(feedbackArgumentCaptor.getValue()).extracting(
        Feedback::getSubmitterName,
        Feedback::getSubmitterEmail,
        Feedback::getServiceRating,
        Feedback::getComment,
        Feedback::getGivenDatetime,
        Feedback::getTransactionId,
        Feedback::getTransactionReference,
        Feedback::getTransactionLink
    ).containsExactly(
        USER_DETAIL.displayName(),
        USER_DETAIL.emailAddress(),
        form.getServiceRating(),
        form.getFeedback().getInputValue(),
        CURRENT_INSTANT,
        applicationVersion.getApplication().getId().toString(),
        APPLICATION_REFERENCE,
        expectedApplicationUrl
    );
  }

  @Test
  void saveFeedback_withApplicationVersion_cannotSendFeedbackException() throws CannotSendFeedbackException {
    when(applicationService.generateApplicationReference(applicationVersion)).thenReturn(APPLICATION_REFERENCE);
    when(absoluteUrlService.getAbsoluteUrl(anyString())).thenReturn(expectedApplicationUrl);

    when(feedbackClientService.saveFeedback(any(Feedback.class)))
        .thenThrow(new CannotSendFeedbackException("test exception"));

    feedbackService.saveFeedback(
        applicationVersion,
        form.getServiceRating(),
        form.getFeedback().getInputValue(),
        USER_DETAIL
    );

    verify(feedbackClientService).saveFeedback(feedbackArgumentCaptor.capture());

    assertThat(feedbackArgumentCaptor.getValue()).extracting(
        Feedback::getSubmitterName,
        Feedback::getSubmitterEmail,
        Feedback::getServiceRating,
        Feedback::getComment,
        Feedback::getGivenDatetime,
        Feedback::getTransactionId,
        Feedback::getTransactionReference,
        Feedback::getTransactionLink
    ).containsExactly(
        USER_DETAIL.displayName(),
        USER_DETAIL.emailAddress(),
        form.getServiceRating(),
        form.getFeedback().getInputValue(),
        CURRENT_INSTANT,
        applicationVersion.getApplication().getId().toString(),
        APPLICATION_REFERENCE,
        expectedApplicationUrl
    );
  }
}
