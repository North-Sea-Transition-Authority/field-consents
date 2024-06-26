package uk.co.nstauthority.fieldconsents.application.payment;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.OPERATOR_PAY_AND_SUBMIT_APPLICATION;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.OPERATOR_RETURN_APPLICATION_TO_IN_PROGRESS_FROM_AWAITING_PAYMENT;

import java.util.UUID;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.fivium.digitalpaymentslibrary.payment.PaymentStatus;
import uk.co.nstauthority.fieldconsents.application.ApplicationContextService;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionService;
import uk.co.nstauthority.fieldconsents.application.submission.ApplicationSubmissionController;
import uk.co.nstauthority.fieldconsents.application.submission.ApplicationSubmissionService;
import uk.co.nstauthority.fieldconsents.application.tasklist.shared.ApplicationTaskListController;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.ActionEndPoint;
import uk.co.nstauthority.fieldconsents.branding.CustomerBrandingConfigurationProperties;
import uk.co.nstauthority.fieldconsents.branding.ServiceBrandingConfigurationProperties;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBanner;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBannerType;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBannerUtil;
import uk.co.nstauthority.fieldconsents.formatting.DecimalFormatUtils;
import uk.co.nstauthority.fieldconsents.mvc.AbsoluteUrlService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@Controller
@RequestMapping("/applications/{applicationId}/pay")
public class ApplicationPaymentController {

  private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationPaymentController.class);

  private final ApplicationService applicationService;
  private final ApplicationVersionService applicationVersionService;
  private final ApplicationContextService applicationContextService;
  private final ApplicationPaymentService applicationPaymentService;
  private final AbsoluteUrlService absoluteUrlService;
  private final CaseProcessingActionService caseProcessingActionService;
  private final ServiceBrandingConfigurationProperties serviceBrandingConfigurationProperties;
  private final CustomerBrandingConfigurationProperties customerBrandingConfigurationProperties;
  private final ApplicationSubmissionService applicationSubmissionService;

  @Autowired
  ApplicationPaymentController(
      ApplicationService applicationService,
      ApplicationVersionService applicationVersionService,
      ApplicationContextService applicationContextService,
      ApplicationPaymentService applicationPaymentService,
      AbsoluteUrlService absoluteUrlService,
      CaseProcessingActionService caseProcessingActionService,
      ServiceBrandingConfigurationProperties serviceBrandingConfigurationProperties,
      CustomerBrandingConfigurationProperties customerBrandingConfigurationProperties,
      ApplicationSubmissionService applicationSubmissionService
  ) {
    this.applicationService = applicationService;
    this.applicationVersionService = applicationVersionService;
    this.applicationContextService = applicationContextService;
    this.applicationPaymentService = applicationPaymentService;
    this.absoluteUrlService = absoluteUrlService;
    this.caseProcessingActionService = caseProcessingActionService;
    this.serviceBrandingConfigurationProperties = serviceBrandingConfigurationProperties;
    this.customerBrandingConfigurationProperties = customerBrandingConfigurationProperties;
    this.applicationSubmissionService = applicationSubmissionService;
  }

  @GetMapping
  @ActionEndPoint(OPERATOR_PAY_AND_SUBMIT_APPLICATION)
  public ModelAndView getStartPayment(@PathVariable Integer applicationId, ServiceUserDetail user) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    var applicationReference = applicationService.generateApplicationReference(applicationVersion);
    var applicationContext = applicationContextService.getApplicationContext(applicationVersion);
    var paymentAmountPence = applicationPaymentService.getPaymentAmountPence(applicationVersion);
    var absoluteGetStartPaymentUrl = absoluteUrlService.getAbsoluteUrl(
        ReverseRouter.route(on(ApplicationPaymentController.class).getStartPayment(applicationId, null)));
    var sharePaymentMailToLink = ("mailto:?subject=Pay %s for %s application %s&body=Please use this link to pay the" +
        " %s for our %s application: %s")
        .formatted(
            customerBrandingConfigurationProperties.mnemonic(),
            serviceBrandingConfigurationProperties.name(),
            applicationReference,
            customerBrandingConfigurationProperties.name(),
            serviceBrandingConfigurationProperties.name(),
            absoluteGetStartPaymentUrl
        );

    var canReturnToInProgress = caseProcessingActionService.userHasAnyAction(
        applicationVersion,
        user,
        OPERATOR_RETURN_APPLICATION_TO_IN_PROGRESS_FROM_AWAITING_PAYMENT
    );

    return new ModelAndView("fcs/application/startPayment")
        .addObject("applicationReference", applicationReference)
        .addObject("applicationContext", applicationContext)
        .addObject("paymentDescription", applicationPaymentService.getPaymentDescription(applicationVersion))
        .addObject("formattedPaymentAmount", DecimalFormatUtils.formatMoney((double) paymentAmountPence / 100))
        .addObject(
            "startPaymentUrl",
            ReverseRouter.route(on(ApplicationPaymentController.class).startPayment(applicationId, null))
        )
        .addObject("canReturnToInProgress", canReturnToInProgress)
        .addObject(
            "returnToInProgressUrl",
            ReverseRouter.route(on(ApplicationPaymentController.class).returnToInProgress(applicationId, null))
        )
        .addObject("absoluteGetStartPaymentUrl", absoluteGetStartPaymentUrl)
        .addObject("sharePaymentMailToLink", sharePaymentMailToLink);
  }

  @PostMapping
  @ActionEndPoint(OPERATOR_PAY_AND_SUBMIT_APPLICATION)
  public ModelAndView startPayment(@PathVariable Integer applicationId, ServiceUserDetail user) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    // If the payment amount was previously > 0 and the application was set to AWAITING_PAYMENT status, however the fee
    // line has since changed and the payment amount is now 0, submit the application.
    if (applicationPaymentService.getPaymentAmountPence(applicationVersion) <= 0) {
      LOGGER.info(
          "Found payment amount <= 0 before creating payment for application {}, submitting application",
          applicationId
      );

      applicationSubmissionService.submitApplication(applicationVersion, user);

      return ReverseRouter.redirect(on(ApplicationSubmissionController.class)
          .getApplicationSubmitted(applicationId));
    }

    Function<UUID, String> returnUrlFunction = paymentId ->
        absoluteUrlService.getAbsoluteUrl(ReverseRouter.route(on(ApplicationPaymentController.class)
            .getPaymentProcessed(applicationId, paymentId, null, null)));

    var createCardPaymentResult = applicationPaymentService.createPayment(applicationVersion, user, returnUrlFunction);
    var status = createCardPaymentResult.getStatus();

    switch (status) {
      case PAYMENT_ALREADY_COMPLETED -> {
        LOGGER.info(
            "Received status already completed when creating payment for application {}, submitting application",
            applicationId
        );

        applicationSubmissionService.submitApplication(applicationVersion, user);

        return ReverseRouter.redirect(on(ApplicationSubmissionController.class)
            .getApplicationPaidAndSubmitted(applicationId));
      }
      case SUCCESS -> {
        return new ModelAndView("redirect:%s".formatted(createCardPaymentResult.getGovUkPayNextUrl()));
      }
      default -> throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Unexpected CreateCardPaymentResult status %s".formatted(status)
      );
    }
  }

  @PostMapping("/return-to-in-progress")
  @ActionEndPoint(OPERATOR_RETURN_APPLICATION_TO_IN_PROGRESS_FROM_AWAITING_PAYMENT)
  public ModelAndView returnToInProgress(@PathVariable Integer applicationId, ServiceUserDetail user) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    var paymentDtos = applicationPaymentService.getAndRefreshPaymentDtos(applicationVersion);

    if (paymentDtos.stream().anyMatch(paymentDto -> paymentDto.status() == PaymentStatus.SUCCESS)) {
      LOGGER.info(
          "Found completed payment before returning application {} to in progress, submitting application",
          applicationId
      );

      applicationSubmissionService.submitApplication(applicationVersion, user);

      return ReverseRouter.redirect(on(ApplicationSubmissionController.class)
          .getApplicationPaidAndSubmitted(applicationId));
    }

    applicationPaymentService.cancelInProgressPayments(paymentDtos);

    applicationService.returnApplicationToInProgressFromAwaitingPayment(applicationVersion);

    return ReverseRouter.redirect(on(ApplicationTaskListController.class).getTaskList(applicationId, null));
  }

  @GetMapping("/payment-processed/{paymentId}")
  @ActionEndPoint(OPERATOR_PAY_AND_SUBMIT_APPLICATION)
  public ModelAndView getPaymentProcessed(
      @PathVariable Integer applicationId,
      @PathVariable UUID paymentId,
      ServiceUserDetail user,
      RedirectAttributes redirectAttributes
  ) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    if (!applicationPaymentService.isPaymentForApplicationVersion(paymentId, applicationVersion)) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Payment %s is not for application version %s".formatted(paymentId, applicationVersion.getId())
      );
    }

    var paymentStatus = applicationPaymentService.handlePaymentProcessed(paymentId);

    if (paymentStatus == PaymentStatus.SUCCESS) {
      applicationSubmissionService.submitApplication(applicationVersion, user);

      return ReverseRouter.redirect(on(ApplicationSubmissionController.class)
          .getApplicationPaidAndSubmitted(applicationId));
    }

    NotificationBannerUtil.applyNotificationBanner(
        redirectAttributes,
        NotificationBanner.builder()
            .withBannerType(NotificationBannerType.INFO)
            .withTitle("Payment not completed")
            .withHeadingContent("You must pay for your application before it is submitted")
            .build()
    );

    return ReverseRouter.redirect(on(ApplicationPaymentController.class).getStartPayment(applicationId, null));
  }
}
