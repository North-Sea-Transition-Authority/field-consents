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
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.OPERATOR_PAY_FOR_APPLICATION;
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
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.co.fivium.digitalpaymentslibrary.payment.CreateCardPaymentResult;
import uk.co.fivium.digitalpaymentslibrary.payment.PaymentDto;
import uk.co.fivium.digitalpaymentslibrary.payment.PaymentStatus;
import uk.co.nstauthority.fieldconsents.AbstractApplicationControllerTest;
import uk.co.nstauthority.fieldconsents.application.ApplicationContextJson;
import uk.co.nstauthority.fieldconsents.application.ApplicationContextService;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.tasklist.shared.ApplicationTaskListController;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil;
import uk.co.nstauthority.fieldconsents.authorisation.ParameterizedSecurityTest;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.branding.CustomerBrandingConfigurationProperties;
import uk.co.nstauthority.fieldconsents.branding.ServiceBrandingConfigurationProperties;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBanner;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBannerType;
import uk.co.nstauthority.fieldconsents.formatting.DecimalFormatUtils;
import uk.co.nstauthority.fieldconsents.mvc.AbsoluteUrlService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.fieldconsents.workarea.WorkAreaController;

@ContextConfiguration(classes = ApplicationPaymentController.class)
class ApplicationPaymentControllerTest extends AbstractApplicationControllerTest {

  private static final UUID PAYMENT_ID = UUID.randomUUID();

  @MockBean
  private ApplicationService applicationService;

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

    when(applicationVersionService.findLatestApplicationVersion(ApplicationTestUtil.APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion));
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);
  }

  @SecurityTest
  void getStartPayment_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ApplicationPaymentController.class).getStartPayment(APPLICATION_ID))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getStartPayment_userDoesNotHaveOperatorPayForApplicationCaseProcessingAction() throws Exception {
    when(caseProcessingActionService.getUserActionItems(applicationVersion, user))
        .thenReturn(List.of());

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationPaymentController.class).getStartPayment(APPLICATION_ID)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void getStartPayment() throws Exception {
    var applicationReference = "testApplicationReference";
    var applicationContextJson =
        new ApplicationContextJson(FieldTestUtil.field1Json, OrganisationUnitTestUtil.orgUnit1Json);
    var paymentDescription = "testPaymentDescription";
    var paymentAmountPence = 93000;
    var absoluteGetStartPaymentUrl = "testAbsoluteGetStartPaymentUrl";

    when(caseProcessingActionService.getUserActionItems(applicationVersion, user))
        .thenReturn(List.of(OPERATOR_PAY_FOR_APPLICATION));
    when(applicationService.generateApplicationReference(applicationVersion)).thenReturn(applicationReference);
    when(applicationContextService.getApplicationContextJson(applicationVersion)).thenReturn(applicationContextJson);
    when(applicationPaymentService.getPaymentDescription(applicationVersion)).thenReturn(paymentDescription);
    when(applicationPaymentService.getPaymentAmountPence(applicationVersion)).thenReturn(paymentAmountPence);
    when(absoluteUrlService.getAbsoluteUrl(ReverseRouter.route(on(ApplicationPaymentController.class)
        .getStartPayment(APPLICATION_ID)))).thenReturn(absoluteGetStartPaymentUrl);

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
            .getStartPayment(APPLICATION_ID)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/startPayment"))
        .andExpect(model().attribute("applicationReference", applicationReference))
        .andExpect(model().attribute("applicationContextJson", applicationContextJson))
        .andExpect(model().attribute("paymentDescription", paymentDescription))
        .andExpect(model().attribute("formattedPaymentAmount",
            DecimalFormatUtils.formatMoney((double) paymentAmountPence / 100)))
        .andExpect(model().attribute("startPaymentUrl", ReverseRouter.route(on(ApplicationPaymentController.class)
            .startPayment(APPLICATION_ID, null))))
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
  void startPayment_userDoesNotHaveOperatorPayForApplicationCaseProcessingAction() throws Exception {
    when(caseProcessingActionService.getUserActionItems(applicationVersion, user))
        .thenReturn(List.of());

    mockMvc.perform(post(ReverseRouter.route(on(ApplicationPaymentController.class).startPayment(APPLICATION_ID, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void startPayment_paymentAlreadyCompleted() throws Exception {
    var absoluteGetPaymentProcessedUrl = "testAbsoluteGetPaymentProcessedUrl";
    var paymentId = UUID.randomUUID();
    var createCardPaymentResult = CreateCardPaymentResult.alreadyCompleted();

    when(caseProcessingActionService.getUserActionItems(applicationVersion, user))
        .thenReturn(List.of(OPERATOR_PAY_FOR_APPLICATION));
    when(absoluteUrlService.getAbsoluteUrl(ReverseRouter.route(on(ApplicationPaymentController.class)
        .getPaymentProcessed(APPLICATION_ID, paymentId, null, null)))).thenReturn(absoluteGetPaymentProcessedUrl);
    when(applicationPaymentService.createPayment(eq(applicationVersion), eq(user), returnUrlArgumentCaptor.capture()))
        .thenReturn(createCardPaymentResult);

    mockMvc.perform(post(ReverseRouter.route(on(ApplicationPaymentController.class).startPayment(APPLICATION_ID, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(ApplicationPaymentController.class)
            .getPaymentCompleted(APPLICATION_ID))));

    assertThat(returnUrlArgumentCaptor.getValue().apply(paymentId)).isEqualTo(absoluteGetPaymentProcessedUrl);

    verify(applicationService).submitApplication(applicationVersion, user);
  }

  @Test
  void startPayment_success() throws Exception {
    var absoluteGetPaymentProcessedUrl = "testAbsoluteGetPaymentProcessedUrl";
    var paymentId = UUID.randomUUID();
    var govPayNextUrl = "testGovPayNextUrl";
    var createCardPaymentResult = CreateCardPaymentResult.success(govPayNextUrl);

    when(caseProcessingActionService.getUserActionItems(applicationVersion, user))
        .thenReturn(List.of(OPERATOR_PAY_FOR_APPLICATION));
    when(absoluteUrlService.getAbsoluteUrl(ReverseRouter.route(on(ApplicationPaymentController.class)
        .getPaymentProcessed(APPLICATION_ID, paymentId, null, null)))).thenReturn(absoluteGetPaymentProcessedUrl);
    when(applicationPaymentService.createPayment(eq(applicationVersion), eq(user), returnUrlArgumentCaptor.capture()))
        .thenReturn(createCardPaymentResult);

    mockMvc.perform(post(ReverseRouter.route(on(ApplicationPaymentController.class).startPayment(APPLICATION_ID, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(govPayNextUrl));

    assertThat(returnUrlArgumentCaptor.getValue().apply(paymentId)).isEqualTo(absoluteGetPaymentProcessedUrl);

    verify(applicationService, never()).submitApplication(any(), any());
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
    when(caseProcessingActionService.getUserActionItems(applicationVersion, user))
        .thenReturn(List.of());

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

    when(caseProcessingActionService.getUserActionItems(applicationVersion, user))
        .thenReturn(List.of(OPERATOR_RETURN_APPLICATION_TO_IN_PROGRESS_FROM_AWAITING_PAYMENT));
    when(applicationPaymentService.getAndRefreshPaymentDtos(applicationVersion)).thenReturn(paymentDtos);
    when(paymentDto1.status()).thenReturn(PaymentStatus.FAILED);
    when(paymentDto1.status()).thenReturn(PaymentStatus.SUCCESS);

    mockMvc.perform(post(ReverseRouter.route(on(ApplicationPaymentController.class)
            .returnToInProgress(APPLICATION_ID, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(ApplicationPaymentController.class)
            .getPaymentCompleted(APPLICATION_ID))));

    verify(applicationService).submitApplication(applicationVersion, user);
    verify(applicationPaymentService, never()).cancelInProgressPayments(any());
    verify(applicationService, never()).returnApplicationToInProgressFromAwaitingPayment(any());
  }

  @Test
  void returnToInProgress_noPaymentExistsThatHasSucceeded() throws Exception {
    var paymentDto1 = mock(PaymentDto.class);
    var paymentDto2 = mock(PaymentDto.class);
    var paymentDtos = List.of(paymentDto1, paymentDto2);

    when(caseProcessingActionService.getUserActionItems(applicationVersion, user))
        .thenReturn(List.of(OPERATOR_RETURN_APPLICATION_TO_IN_PROGRESS_FROM_AWAITING_PAYMENT));
    when(applicationPaymentService.getAndRefreshPaymentDtos(applicationVersion)).thenReturn(paymentDtos);
    when(paymentDto1.status()).thenReturn(PaymentStatus.FAILED);
    when(paymentDto1.status()).thenReturn(PaymentStatus.IN_PROGRESS);

    mockMvc.perform(post(ReverseRouter.route(on(ApplicationPaymentController.class)
            .returnToInProgress(APPLICATION_ID, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(ApplicationTaskListController.class)
            .getTaskList(APPLICATION_ID))));

    verify(applicationService, never()).submitApplication(any(), any());
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
  void getPaymentProcessed_userDoesNotHaveOperatorPayForApplicationCaseProcessingAction() throws Exception {
    when(caseProcessingActionService.getUserActionItems(applicationVersion, user))
        .thenReturn(List.of());

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationPaymentController.class)
            .getPaymentProcessed(APPLICATION_ID, PAYMENT_ID, null, null)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void getPaymentProcessed_paymentIsNotForApplicationVersion() throws Exception {
    when(caseProcessingActionService.getUserActionItems(applicationVersion, user))
        .thenReturn(List.of(OPERATOR_PAY_FOR_APPLICATION));
    when(applicationPaymentService.isPaymentForApplicationVersion(PAYMENT_ID, applicationVersion)).thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationPaymentController.class)
            .getPaymentProcessed(APPLICATION_ID, PAYMENT_ID, null, null)))
            .with(user(user)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void getPaymentProcessed_paymentStatusSuccess() throws Exception {
    when(caseProcessingActionService.getUserActionItems(applicationVersion, user))
        .thenReturn(List.of(OPERATOR_PAY_FOR_APPLICATION));
    when(applicationPaymentService.handlePaymentProcessed(PAYMENT_ID))
        .thenReturn(PaymentStatus.SUCCESS);
    when(applicationPaymentService.isPaymentForApplicationVersion(PAYMENT_ID, applicationVersion)).thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationPaymentController.class)
            .getPaymentProcessed(APPLICATION_ID, PAYMENT_ID, null, null)))
            .with(user(user)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(ApplicationPaymentController.class)
            .getPaymentCompleted(APPLICATION_ID))));
  }

  @Test
  void getPaymentProcessed_paymentStatusNotSuccess() throws Exception {
    when(caseProcessingActionService.getUserActionItems(applicationVersion, user))
        .thenReturn(List.of(OPERATOR_PAY_FOR_APPLICATION));
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
            .getStartPayment(APPLICATION_ID))));

    verify(applicationService, never()).submitApplication(any(), any());
  }

  @SecurityTest
  void getPaymentCompleted_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ApplicationPaymentController.class)
            .getPaymentCompleted(APPLICATION_ID))))
        .andExpect(redirectionToLoginUrl());
  }

  @ParameterizedSecurityTest
  @EnumSource(value = ApplicationVersionStatus.class, names = "SUBMITTED", mode = EnumSource.Mode.EXCLUDE)
  void getPaymentCompleted_statusNotSubmitted(ApplicationVersionStatus applicationVersionStatus) throws Exception {
    applicationVersion.setStatus(applicationVersionStatus);

    when(applicationAccessService.hasApplicationPermission(user, applicationVersion, RolePermission.SUBMIT_FCS_APPLICATIONS))
        .thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationPaymentController.class)
            .getPaymentCompleted(APPLICATION_ID)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void getPaymentCompleted_userDoesNotHaveSubmitPermission() throws Exception {
    applicationVersion.setStatus(ApplicationVersionStatus.SUBMITTED);

    when(applicationAccessService.hasApplicationPermission(user, applicationVersion, RolePermission.SUBMIT_FCS_APPLICATIONS))
        .thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationPaymentController.class)
            .getPaymentCompleted(APPLICATION_ID)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void getPaymentCompleted() throws Exception {
    applicationVersion.setStatus(ApplicationVersionStatus.SUBMITTED);

    var applicationReference = "testApplicationReference";

    when(applicationAccessService.hasApplicationPermission(user, applicationVersion, RolePermission.SUBMIT_FCS_APPLICATIONS))
        .thenReturn(true);
    when(applicationService.generateApplicationReference(applicationVersion)).thenReturn(applicationReference);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationPaymentController.class)
            .getPaymentCompleted(APPLICATION_ID)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/submissionConfirmation"))
        .andExpect(model().attribute("pageTitle", ApplicationPaymentController.APPLICATION_SUBMITTED_TITLE))
        .andExpect(model().attribute("applicationReference", applicationReference))
        .andExpect(model().attribute("workAreaUrl", ReverseRouter.route(on(WorkAreaController.class)
            .getWorkArea(null, null))));
  }
}
