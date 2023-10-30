package uk.co.nstauthority.fieldconsents.application.payment;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.OPERATOR_PAY_FOR_APPLICATION;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.OPERATOR_RETURN_APPLICATION_TO_IN_PROGRESS_FROM_AWAITING_PAYMENT;

import java.util.UUID;
import java.util.function.Function;
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
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.tasklist.shared.ApplicationTaskListController;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.ActionEndPoint;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationPermission;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationStatus;
import uk.co.nstauthority.fieldconsents.branding.CustomerBrandingConfigurationProperties;
import uk.co.nstauthority.fieldconsents.branding.ServiceBrandingConfigurationProperties;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBanner;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBannerType;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBannerUtil;
import uk.co.nstauthority.fieldconsents.formatting.DecimalFormatUtils;
import uk.co.nstauthority.fieldconsents.mvc.AbsoluteUrlService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.fieldconsents.workarea.WorkAreaController;

@Controller
@RequestMapping("/applications/{applicationId}/pay")
public class ApplicationPaymentController {

  static final String APPLICATION_SUBMITTED_TITLE = "Application paid and submitted";

  private final ApplicationService applicationService;
  private final ApplicationVersionService applicationVersionService;
  private final ApplicationContextService applicationContextService;
  private final ApplicationPaymentService applicationPaymentService;
  private final AbsoluteUrlService absoluteUrlService;
  private final ServiceBrandingConfigurationProperties serviceBrandingConfigurationProperties;
  private final CustomerBrandingConfigurationProperties customerBrandingConfigurationProperties;

  @Autowired
  ApplicationPaymentController(
      ApplicationService applicationService,
      ApplicationVersionService applicationVersionService,
      ApplicationContextService applicationContextService,
      ApplicationPaymentService applicationPaymentService,
      AbsoluteUrlService absoluteUrlService,
      ServiceBrandingConfigurationProperties serviceBrandingConfigurationProperties,
      CustomerBrandingConfigurationProperties customerBrandingConfigurationProperties
  ) {
    this.applicationService = applicationService;
    this.applicationVersionService = applicationVersionService;
    this.applicationContextService = applicationContextService;
    this.applicationPaymentService = applicationPaymentService;
    this.absoluteUrlService = absoluteUrlService;
    this.serviceBrandingConfigurationProperties = serviceBrandingConfigurationProperties;
    this.customerBrandingConfigurationProperties = customerBrandingConfigurationProperties;
  }

  @GetMapping
  @ActionEndPoint(OPERATOR_PAY_FOR_APPLICATION)
  public ModelAndView getStartPayment(@PathVariable Integer applicationId) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    var applicationReference = applicationService.generateApplicationReference(applicationVersion);
    var applicationContextJson = applicationContextService.getApplicationContextJson(applicationVersion);
    var absoluteGetStartPaymentUrl = absoluteUrlService.getAbsoluteUrl(
        ReverseRouter.route(on(ApplicationPaymentController.class).getStartPayment(applicationId)));
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

    return new ModelAndView("fcs/application/startPayment")
        .addObject("applicationReference", applicationReference)
        .addObject("applicationContextJson", applicationContextJson)
        .addObject("paymentDescription", applicationPaymentService.getPaymentDescription(applicationVersion))
        .addObject("formattedPaymentAmount", DecimalFormatUtils.formatMoney(1D))
        .addObject(
            "startPaymentUrl",
            ReverseRouter.route(on(ApplicationPaymentController.class).startPayment(applicationId, null))
        )
        .addObject(
            "returnToInProgressUrl",
            ReverseRouter.route(on(ApplicationPaymentController.class).returnToInProgress(applicationId))
        )
        .addObject("absoluteGetStartPaymentUrl", absoluteGetStartPaymentUrl)
        .addObject("sharePaymentMailToLink", sharePaymentMailToLink);
  }

  @PostMapping
  @ActionEndPoint(OPERATOR_PAY_FOR_APPLICATION)
  public ModelAndView startPayment(@PathVariable Integer applicationId, ServiceUserDetail user) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    Function<UUID, String> returnUrlFunction = paymentId ->
        absoluteUrlService.getAbsoluteUrl(ReverseRouter.route(on(ApplicationPaymentController.class)
            .getPaymentProcessed(applicationId, paymentId, null, null)));

    var createCardPaymentResult = applicationPaymentService.createPayment(applicationVersion, user, returnUrlFunction);
    var status = createCardPaymentResult.getStatus();

    switch (status) {
      case PAYMENT_ALREADY_COMPLETED -> {
        applicationService.submitApplication(applicationVersion, user);

        return ReverseRouter.redirect(on(ApplicationPaymentController.class).getPaymentCompleted(applicationId));
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
  public ModelAndView returnToInProgress(@PathVariable Integer applicationId) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    applicationService.returnApplicationToInProgressFromAwaitingPayment(applicationVersion);

    return ReverseRouter.redirect(on(ApplicationTaskListController.class).getTaskList(applicationId));
  }

  @GetMapping("/payment-processed/{paymentId}")
  @ActionEndPoint(OPERATOR_PAY_FOR_APPLICATION)
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
      applicationService.submitApplication(applicationVersion, user);

      return ReverseRouter.redirect(on(ApplicationPaymentController.class).getPaymentCompleted(applicationId));
    }

    NotificationBannerUtil.applyNotificationBanner(
        redirectAttributes,
        NotificationBanner.builder()
            .withBannerType(NotificationBannerType.INFO)
            .withTitle("Payment not completed")
            .withHeadingContent("You must pay for your application before it is submitted")
            .build()
    );

    return ReverseRouter.redirect(on(ApplicationPaymentController.class).getStartPayment(applicationId));
  }

  @GetMapping("/payment-completed")
  @HasApplicationStatus(statuses = ApplicationVersionStatus.SUBMITTED)
  @HasApplicationPermission(permissions = RolePermission.SUBMIT_FCS_APPLICATIONS)
  public ModelAndView getPaymentCompleted(@PathVariable Integer applicationId) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    return new ModelAndView("fcs/application/submissionConfirmation")
        .addObject("pageTitle", APPLICATION_SUBMITTED_TITLE)
        .addObject("applicationReference", applicationService.generateApplicationReference(applicationVersion))
        .addObject("workAreaUrl", ReverseRouter.route(on(WorkAreaController.class).getWorkArea(null, null)));
  }
}
