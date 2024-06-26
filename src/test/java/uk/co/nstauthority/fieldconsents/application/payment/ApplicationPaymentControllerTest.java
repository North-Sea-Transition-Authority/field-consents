package uk.co.nstauthority.fieldconsents.application.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_ID;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.OPERATOR_PAY_AND_SUBMIT_APPLICATION;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.OPERATOR_RETURN_APPLICATION_TO_IN_PROGRESS_FROM_AWAITING_PAYMENT;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.util.NotificationBannerTestUtil.notificationBanner;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.co.fivium.digitalpaymentslibrary.payment.CreateCardPaymentResult;
import uk.co.fivium.digitalpaymentslibrary.payment.PaymentDto;
import uk.co.fivium.digitalpaymentslibrary.payment.PaymentStatus;
import uk.co.nstauthority.fieldconsents.AbstractApplicationControllerTest;
import uk.co.nstauthority.fieldconsents.application.ApplicationContext;
import uk.co.nstauthority.fieldconsents.application.ApplicationContextService;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.submission.ApplicationSubmissionController;
import uk.co.nstauthority.fieldconsents.application.submission.ApplicationSubmissionService;
import uk.co.nstauthority.fieldconsents.application.tasklist.shared.ApplicationTaskListController;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.branding.CustomerBrandingConfigurationProperties;
import uk.co.nstauthority.fieldconsents.branding.ServiceBrandingConfigurationProperties;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBanner;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBannerType;
import uk.co.nstauthority.fieldconsents.formatting.DecimalFormatUtils;
import uk.co.nstauthority.fieldconsents.mvc.AbsoluteUrlService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@ContextConfiguration(classes = ApplicationPaymentController.class)
class ApplicationPaymentControllerTest extends AbstractApplicationControllerTest {

  private static final UUID PAYMENT_ID = UUID.randomUUID();

  @MockBean
  private ApplicationService applicationService;

  @MockBean
  private ApplicationSubmissionService applicationSubmissionService;

  @MockBean
  private ApplicationContextService applicationContextService;

  @MockBean
  private ApplicationPaymentService applicationPaymentService;

  @MockBean
  private AbsoluteUrlService absoluteUrlService;

  @Autowired
  private ServiceBrandingConfigurationProperties serviceBrandingConfigurationProperties;

  @Autowired
  private CustomerBrandingConfigurationProperties customerBrandingConfigurationProperties;

  @Captor
  private ArgumentCaptor<Function<UUID, String>> returnUrlArgumentCaptor;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void beforeEach() {
    applicationVersion = ApplicationTestUtil.getAwaitingPaymentApplicationVersionWithType(ApplicationType.PRODUCTION);

    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion));
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);
  }

  @SecurityTest
  void getStartPayment_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ApplicationPaymentController.class)
            .getStartPayment(APPLICATION_ID, null))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getStartPayment_userDoesNotHaveOperatorPayAndSubmitApplicationCaseProcessingAction() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ApplicationPaymentController.class)
            .getStartPayment(APPLICATION_ID, null)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void getStartPayment_userDoesNotHaveOperatorReturnApplicationToInProgressFromAwaitingPaymentCaseProcessingAction()
      throws Exception {
    var applicationReference = "testApplicationReference";
    var applicationContext = ApplicationContext.newBuilder()
        .withPrimaryAsset(FieldTestUtil.field1Json)
        .withPrimaryOperator("Primary operator")
        .withApplicationVersionStatus(applicationVersion.getStatus())
        .build();
    var paymentDescription = "testPaymentDescription";
    var paymentAmountPence = 93000;
    var absoluteGetStartPaymentUrl = "testAbsoluteGetStartPaymentUrl";

    when(caseProcessingActionService.userHasAnyAction(applicationVersion, user, OPERATOR_PAY_AND_SUBMIT_APPLICATION)).thenReturn(true);
    when(applicationService.generateApplicationReference(applicationVersion)).thenReturn(applicationReference);
    when(applicationContextService.getApplicationContext(applicationVersion)).thenReturn(applicationContext);
    when(applicationPaymentService.getPaymentDescription(applicationVersion)).thenReturn(paymentDescription);
    when(applicationPaymentService.getPaymentAmountPence(applicationVersion)).thenReturn(paymentAmountPence);
    when(absoluteUrlService.getAbsoluteUrl(ReverseRouter.route(on(ApplicationPaymentController.class)
        .getStartPayment(APPLICATION_ID, null)))).thenReturn(absoluteGetStartPaymentUrl);

    var expectedSharePaymentMailToLink = ("mailto:?subject=Pay %s for %s application %s&body=Please use this link to " +
        "pay the %s for our %s application: %s")
        .formatted(
            customerBrandingConfigurationProperties.mnemonic(),
            serviceBrandingConfigurationProperties.name(),
            applicationReference,
            customerBrandingConfigurationProperties.name(),
            serviceBrandingConfigurationProperties.name(),
            absoluteGetStartPaymentUrl
        );

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationPaymentController.class)
            .getStartPayment(APPLICATION_ID, null)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/startPayment"))
        .andExpect(model().attribute("applicationReference", applicationReference))
        .andExpect(model().attribute("applicationContext", applicationContext))
        .andExpect(model().attribute("paymentDescription", paymentDescription))
        .andExpect(model().attribute("formattedPaymentAmount",
            DecimalFormatUtils.formatMoney((double) paymentAmountPence / 100)))
        .andExpect(model().attribute("startPaymentUrl", ReverseRouter.route(on(ApplicationPaymentController.class)
            .startPayment(APPLICATION_ID, null))))
        .andExpect(model().attribute("canReturnToInProgress", false))
        .andExpect(model().attribute("returnToInProgressUrl", ReverseRouter.route(on(ApplicationPaymentController.class)
            .returnToInProgress(APPLICATION_ID, null))))
        .andExpect(model().attribute("absoluteGetStartPaymentUrl", absoluteGetStartPaymentUrl))
        .andExpect(model().attribute("sharePaymentMailToLink", expectedSharePaymentMailToLink));
  }

  @SecurityTest
  void getStartPayment_userDoesHaveOperatorReturnApplicationToInProgressFromAwaitingPaymentCaseProcessingAction()
      throws Exception {
    var applicationReference = "testApplicationReference";
    var applicationContext = ApplicationContext.newBuilder()
        .withPrimaryAsset(FieldTestUtil.field1Json)
        .withPrimaryOperator("Primary operator")
        .withApplicationVersionStatus(applicationVersion.getStatus())
        .build();
    var paymentDescription = "testPaymentDescription";
    var paymentAmountPence = 93000;
    var absoluteGetStartPaymentUrl = "testAbsoluteGetStartPaymentUrl";

    when(caseProcessingActionService.userHasAnyAction(
        applicationVersion,
        user,
        OPERATOR_PAY_AND_SUBMIT_APPLICATION
    )).thenReturn(true);

    when(caseProcessingActionService.userHasAnyAction(
        applicationVersion,
        user,
        OPERATOR_RETURN_APPLICATION_TO_IN_PROGRESS_FROM_AWAITING_PAYMENT
    )).thenReturn(true);

    when(applicationService.generateApplicationReference(applicationVersion)).thenReturn(applicationReference);
    when(applicationContextService.getApplicationContext(applicationVersion)).thenReturn(applicationContext);
    when(applicationPaymentService.getPaymentDescription(applicationVersion)).thenReturn(paymentDescription);
    when(applicationPaymentService.getPaymentAmountPence(applicationVersion)).thenReturn(paymentAmountPence);
    when(absoluteUrlService.getAbsoluteUrl(ReverseRouter.route(on(ApplicationPaymentController.class)
        .getStartPayment(APPLICATION_ID, null)))).thenReturn(absoluteGetStartPaymentUrl);

    var expectedSharePaymentMailToLink = ("mailto:?subject=Pay %s for %s application %s&body=Please use this link to " +
        "pay the %s for our %s application: %s")
        .formatted(
            customerBrandingConfigurationProperties.mnemonic(),
            serviceBrandingConfigurationProperties.name(),
            applicationReference,
            customerBrandingConfigurationProperties.name(),
            serviceBrandingConfigurationProperties.name(),
            absoluteGetStartPaymentUrl
        );

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationPaymentController.class)
            .getStartPayment(APPLICATION_ID, null)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/startPayment"))
        .andExpect(model().attribute("applicationReference", applicationReference))
        .andExpect(model().attribute("applicationContext", applicationContext))
        .andExpect(model().attribute("paymentDescription", paymentDescription))
        .andExpect(model().attribute("formattedPaymentAmount",
            DecimalFormatUtils.formatMoney((double) paymentAmountPence / 100)))
        .andExpect(model().attribute("startPaymentUrl", ReverseRouter.route(on(ApplicationPaymentController.class)
            .startPayment(APPLICATION_ID, null))))
        .andExpect(model().attribute("canReturnToInProgress", true))
        .andExpect(model().attribute("returnToInProgressUrl", ReverseRouter.route(on(ApplicationPaymentController.class)
            .returnToInProgress(APPLICATION_ID, null))))
        .andExpect(model().attribute("absoluteGetStartPaymentUrl", absoluteGetStartPaymentUrl))
        .andExpect(model().attribute("sharePaymentMailToLink", expectedSharePaymentMailToLink));
  }

  @SecurityTest
  void startPayment_noUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(ApplicationPaymentController.class).startPayment(APPLICATION_ID, null)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void startPayment_userDoesNotHaveOperatorPayAndSubmitApplicationCaseProcessingAction() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(ApplicationPaymentController.class).startPayment(APPLICATION_ID, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void startPayment_paymentAmountPenceZero() throws Exception {
    when(applicationPaymentService.getPaymentAmountPence(applicationVersion)).thenReturn(0);

    mockMvc.perform(post(ReverseRouter.route(on(ApplicationPaymentController.class).startPayment(APPLICATION_ID, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(ApplicationSubmissionController.class)
            .getApplicationSubmitted(APPLICATION_ID))));

    verify(applicationSubmissionService).submitApplication(applicationVersion, user);
    verify(applicationPaymentService, never()).createPayment(any(), any(), any());
  }

  @Test
  void startPayment_paymentAlreadyCompleted() throws Exception {
    var absoluteGetPaymentProcessedUrl = "testAbsoluteGetPaymentProcessedUrl";
    var paymentId = UUID.randomUUID();
    var createCardPaymentResult = mock(CreateCardPaymentResult.class);

    when(applicationPaymentService.getPaymentAmountPence(applicationVersion)).thenReturn(100);
    when(absoluteUrlService.getAbsoluteUrl(ReverseRouter.route(on(ApplicationPaymentController.class)
        .getPaymentProcessed(APPLICATION_ID, paymentId, null, null)))).thenReturn(absoluteGetPaymentProcessedUrl);
    when(applicationPaymentService.createPayment(eq(applicationVersion), eq(user), returnUrlArgumentCaptor.capture()))
        .thenReturn(createCardPaymentResult);
    when(createCardPaymentResult.getStatus()).thenReturn(CreateCardPaymentResult.Status.PAYMENT_ALREADY_COMPLETED);

    mockMvc.perform(post(ReverseRouter.route(on(ApplicationPaymentController.class).startPayment(APPLICATION_ID, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(ApplicationSubmissionController.class)
            .getApplicationPaidAndSubmitted(APPLICATION_ID))));

    assertThat(returnUrlArgumentCaptor.getValue().apply(paymentId)).isEqualTo(absoluteGetPaymentProcessedUrl);

    verify(applicationSubmissionService).submitApplication(applicationVersion, user);
  }

  @Test
  void startPayment_success() throws Exception {
    var absoluteGetPaymentProcessedUrl = "testAbsoluteGetPaymentProcessedUrl";
    var paymentId = UUID.randomUUID();
    var govPayNextUrl = "testGovPayNextUrl";
    var createCardPaymentResult = mock(CreateCardPaymentResult.class);

    when(applicationPaymentService.getPaymentAmountPence(applicationVersion)).thenReturn(100);
    when(absoluteUrlService.getAbsoluteUrl(ReverseRouter.route(on(ApplicationPaymentController.class)
        .getPaymentProcessed(APPLICATION_ID, paymentId, null, null)))).thenReturn(absoluteGetPaymentProcessedUrl);
    when(applicationPaymentService.createPayment(eq(applicationVersion), eq(user), returnUrlArgumentCaptor.capture()))
        .thenReturn(createCardPaymentResult);
    when(createCardPaymentResult.getStatus()).thenReturn(CreateCardPaymentResult.Status.SUCCESS);
    when(createCardPaymentResult.getGovUkPayNextUrl()).thenReturn(govPayNextUrl);

    mockMvc.perform(post(ReverseRouter.route(on(ApplicationPaymentController.class).startPayment(APPLICATION_ID, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(govPayNextUrl));

    assertThat(returnUrlArgumentCaptor.getValue().apply(paymentId)).isEqualTo(absoluteGetPaymentProcessedUrl);

    verify(applicationSubmissionService, never()).submitApplication(any(), any());
  }

  @SecurityTest
  void returnToInProgress_noUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(ApplicationPaymentController.class)
            .returnToInProgress(APPLICATION_ID, null)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void returnToInProgress_userDoesNotHaveOperatorReturnApplicationToInProgressFromAwaitingPaymentCaseProcessingAction()
      throws Exception {

    mockMvc.perform(post(ReverseRouter.route(on(ApplicationPaymentController.class)
            .returnToInProgress(APPLICATION_ID, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void returnToInProgress_paymentExistsThatHasSucceeded() throws Exception {
    var paymentDto1 = mock(PaymentDto.class);
    var paymentDto2 = mock(PaymentDto.class);
    var paymentDtos = List.of(paymentDto1, paymentDto2);

    when(applicationPaymentService.getAndRefreshPaymentDtos(applicationVersion)).thenReturn(paymentDtos);
    when(paymentDto1.status()).thenReturn(PaymentStatus.FAILED);
    when(paymentDto1.status()).thenReturn(PaymentStatus.SUCCESS);

    mockMvc.perform(post(ReverseRouter.route(on(ApplicationPaymentController.class)
            .returnToInProgress(APPLICATION_ID, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(ApplicationSubmissionController.class)
            .getApplicationPaidAndSubmitted(APPLICATION_ID))));

    verify(applicationSubmissionService).submitApplication(applicationVersion, user);
    verify(applicationPaymentService, never()).cancelInProgressPayments(any());
    verify(applicationService, never()).returnApplicationToInProgressFromAwaitingPayment(any());
  }

  @Test
  void returnToInProgress_noPaymentExistsThatHasSucceeded() throws Exception {
    var paymentDto1 = mock(PaymentDto.class);
    var paymentDto2 = mock(PaymentDto.class);
    var paymentDtos = List.of(paymentDto1, paymentDto2);

    when(applicationPaymentService.getAndRefreshPaymentDtos(applicationVersion)).thenReturn(paymentDtos);
    when(paymentDto1.status()).thenReturn(PaymentStatus.FAILED);
    when(paymentDto1.status()).thenReturn(PaymentStatus.IN_PROGRESS);

    mockMvc.perform(post(ReverseRouter.route(on(ApplicationPaymentController.class)
            .returnToInProgress(APPLICATION_ID, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(ApplicationTaskListController.class)
            .getTaskList(APPLICATION_ID, null))));

    verify(applicationSubmissionService, never()).submitApplication(any(), any());
    verify(applicationPaymentService).cancelInProgressPayments(paymentDtos);
    verify(applicationService).returnApplicationToInProgressFromAwaitingPayment(applicationVersion);
  }

  @SecurityTest
  void getPaymentProcessed_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ApplicationPaymentController.class)
            .getPaymentProcessed(APPLICATION_ID, PAYMENT_ID, null, null))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getPaymentProcessed_userDoesNotHaveOperatorPayAndSubmitApplicationCaseProcessingAction() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ApplicationPaymentController.class)
            .getPaymentProcessed(APPLICATION_ID, PAYMENT_ID, null, null)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void getPaymentProcessed_paymentIsNotForApplicationVersion() throws Exception {
    when(applicationPaymentService.isPaymentForApplicationVersion(PAYMENT_ID, applicationVersion)).thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationPaymentController.class)
            .getPaymentProcessed(APPLICATION_ID, PAYMENT_ID, null, null)))
            .with(user(user)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void getPaymentProcessed_paymentStatusSuccess() throws Exception {
    when(applicationPaymentService.handlePaymentProcessed(PAYMENT_ID))
        .thenReturn(PaymentStatus.SUCCESS);
    when(applicationPaymentService.isPaymentForApplicationVersion(PAYMENT_ID, applicationVersion)).thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationPaymentController.class)
            .getPaymentProcessed(APPLICATION_ID, PAYMENT_ID, null, null)))
            .with(user(user)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(ApplicationSubmissionController.class)
            .getApplicationPaidAndSubmitted(APPLICATION_ID))));
  }

  @Test
  void getPaymentProcessed_paymentStatusNotSuccess() throws Exception {
    when(applicationPaymentService.handlePaymentProcessed(PAYMENT_ID))
        .thenReturn(PaymentStatus.IN_PROGRESS);
    when(applicationPaymentService.isPaymentForApplicationVersion(PAYMENT_ID, applicationVersion)).thenReturn(true);

    var expectedNotificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.INFO)
        .withTitle("Payment not completed")
        .withHeadingContent("You must pay for your application before it is submitted")
        .build();

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationPaymentController.class)
            .getPaymentProcessed(APPLICATION_ID, PAYMENT_ID, null, null)))
            .with(user(user)))
        .andExpect(status().is3xxRedirection())
        .andExpect(notificationBanner(expectedNotificationBanner))
        .andExpect(redirectedUrl(ReverseRouter.route(on(ApplicationPaymentController.class)
            .getStartPayment(APPLICATION_ID, null))));

    verify(applicationSubmissionService, never()).submitApplication(any(), any());
  }
}
