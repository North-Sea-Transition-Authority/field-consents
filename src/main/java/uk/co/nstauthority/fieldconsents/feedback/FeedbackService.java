package uk.co.nstauthority.fieldconsents.feedback;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.co.fivium.feedbackmanagementservice.client.CannotSendFeedbackException;
import uk.co.fivium.feedbackmanagementservice.client.FeedbackClientService;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.summary.ApplicationSummaryController;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.mvc.AbsoluteUrlService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@Service
class FeedbackService {
  private static final Logger LOGGER = LoggerFactory.getLogger(FeedbackService.class);

  private final Clock clock;
  private final ApplicationService applicationService;
  private final AbsoluteUrlService absoluteUrlService;
  private final FeedbackClientService feedbackClientService;

  FeedbackService(Clock clock,
                  ApplicationService applicationService,
                  AbsoluteUrlService absoluteUrlService,
                  FeedbackClientService feedbackClientService) {
    this.clock = clock;
    this.applicationService = applicationService;
    this.absoluteUrlService = absoluteUrlService;
    this.feedbackClientService = feedbackClientService;
  }

  void saveFeedback(String serviceRating,
                    String feedbackText,
                    ServiceUserDetail userDetail) {
    var feedback = new Feedback();
    sendFeedback(feedback, serviceRating, feedbackText, userDetail);
  }

  void saveFeedback(ApplicationVersion applicationVersion,
                    String serviceRating,
                    String feedbackText,
                    ServiceUserDetail userDetail) {
    var feedback = new Feedback();
    var application = applicationVersion.getApplication();
    feedback.setTransactionId(application.getId().toString());
    feedback.setTransactionReference(applicationService.generateApplicationReference(applicationVersion));
    feedback.setTransactionLink(absoluteUrlService.getAbsoluteUrl(ReverseRouter.route(on(ApplicationSummaryController.class)
        .getApplicationSummary(application.getId(), null))));
    sendFeedback(feedback, serviceRating, feedbackText, userDetail);
  }

  void sendFeedback(Feedback feedback,
                    String serviceRating,
                    String feedbackText,
                    ServiceUserDetail userDetail) {
    feedback.setServiceRating(serviceRating);
    feedback.setComment(feedbackText);
    feedback.setGivenDatetime(clock.instant());
    feedback.setSubmitterEmail(userDetail.emailAddress());
    feedback.setSubmitterName(userDetail.displayNameIncludingAnyProxyUser());
    try {
      feedbackClientService.saveFeedback(feedback);
    } catch (CannotSendFeedbackException e) {
      LOGGER.warn("Feedback failed to send: {} ", e.getMessage());
    }
  }
}
