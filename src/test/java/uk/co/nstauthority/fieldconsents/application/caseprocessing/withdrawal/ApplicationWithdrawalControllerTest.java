package uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.verifyNoInteractions;
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
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CASE_OFFICER_WITHDRAWAL_RESPONSE;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.OPERATOR_WITHDRAWAL_REQUEST;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal.ApplicationWithdrawalTestUtil.DUMMY_APP_REF;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal.ApplicationWithdrawalTestUtil.getWithdrawalRequestView;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.util.NotificationBannerTestUtil.notificationBanner;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.fieldconsents.AbstractApplicationControllerTest;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.ApplicationCaseProcessingController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.IndustryCaseProcessingController;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBanner;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBannerType;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.workarea.WorkAreaController;

@ContextConfiguration(classes = ApplicationWithdrawalController.class)
class ApplicationWithdrawalControllerTest extends AbstractApplicationControllerTest {

  @MockBean
  private ApplicationService applicationService;

  @MockBean
  private ApplicationWithdrawalService applicationWithdrawalService;

  @MockBean
  private WithdrawalRequestFormValidator withdrawalRequestFormValidator;

  @MockBean
  private WithdrawalResponseFormValidator withdrawalResponseFormValidator;

  @MockBean
  private WithdrawalRequestViewService withdrawalRequestViewService;

  @SecurityTest
  void getApplicationWithdrawalRequest_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ApplicationWithdrawalController.class)
            .getApplicationWithdrawalRequest(APPLICATION_ID))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getApplicationWithdrawalRequest_checkEndPointSecurityOnly_forbidden() throws Exception {
    var applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationWithdrawalController.class)
            .getApplicationWithdrawalRequest(APPLICATION_ID)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void getApplicationWithdrawalRequest_checkEndPointSecurityOnly_allowed() throws Exception {
    var applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion));
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);
    when(applicationService.generateApplicationReference(applicationVersion))
        .thenReturn(DUMMY_APP_REF);
    when(applicationWithdrawalService.getWithdrawalRequestForm(applicationVersion)).thenReturn(new WithdrawalRequestForm());

    when(caseProcessingActionService.userHasAnyAction(
        applicationVersion,
        user,
        OPERATOR_WITHDRAWAL_REQUEST
    )).thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationWithdrawalController.class)
            .getApplicationWithdrawalRequest(APPLICATION_ID)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/withdrawalRequestForm"));
  }

  @ParameterizedTest
  @MethodSource("getSubmittedApplicationVersions")
  void getApplicationWithdrawalRequest(ApplicationVersion applicationVersion) throws Exception {
    var form = new WithdrawalRequestForm();
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion));
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);
    when(applicationService.generateApplicationReference(applicationVersion))
        .thenReturn(DUMMY_APP_REF);
    when(applicationWithdrawalService.getWithdrawalRequestForm(applicationVersion)).thenReturn(form);

    when(caseProcessingActionService.userHasAnyAction(
        applicationVersion,
        user,
        OPERATOR_WITHDRAWAL_REQUEST
    )).thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationWithdrawalController.class)
            .getApplicationWithdrawalRequest(APPLICATION_ID)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/withdrawalRequestForm"))
        .andExpect(model().attribute("applicationReference", DUMMY_APP_REF))
        .andExpect(model().attribute("form", form))
        .andExpect(model().attribute("submitUrl",
            ReverseRouter.route(on(ApplicationWithdrawalController.class)
                .submitApplicationWithdrawalRequest(APPLICATION_ID, null, null, null, null))))
        .andExpect(model().attribute("backLinkUrl",
            ReverseRouter.route(on(IndustryCaseProcessingController.class)
                .getIndustryCaseProcessing(APPLICATION_ID, null, null, null))));
  }

  @SecurityTest
  void submitApplicationWithdrawalRequest_noUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(ApplicationWithdrawalController.class)
            .submitApplicationWithdrawalRequest(APPLICATION_ID, null, null, user, null)))
        .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @ParameterizedTest
  @MethodSource("getSubmittedApplicationVersions")
  void submitApplicationWithdrawalRequest_withEmptyForm(ApplicationVersion applicationVersion) throws Exception {
    var form = new WithdrawalRequestForm();
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion));
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);

    when(caseProcessingActionService.userHasAnyAction(
        applicationVersion,
        user,
        OPERATOR_WITHDRAWAL_REQUEST
    )).thenReturn(true);

    doCallRealMethod().when(withdrawalRequestFormValidator).validate(any(), any());

    mockMvc.perform(
            post(ReverseRouter.route(on(ApplicationWithdrawalController.class)
                .submitApplicationWithdrawalRequest(APPLICATION_ID, form, null, user, null)))
                .with(csrf())
                .with(user(user))
        )
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/withdrawalRequestForm"));

    verifyNoInteractions(applicationWithdrawalService);
  }

  @ParameterizedTest
  @MethodSource("getSubmittedApplicationVersions")
  void submitApplicationWithdrawalRequest_withNonEmptyForm(ApplicationVersion applicationVersion) throws Exception {
    var form = new WithdrawalRequestForm();
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion));
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);

    when(caseProcessingActionService.userHasAnyAction(
        applicationVersion,
        user,
        OPERATOR_WITHDRAWAL_REQUEST
    )).thenReturn(true);

    doCallRealMethod().when(withdrawalRequestFormValidator).validate(any(), any());

    var expectedNotificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeadingContent("Withdrawal request sent")
        .build();

    mockMvc.perform(
            post(ReverseRouter.route(on(ApplicationWithdrawalController.class)
                .submitApplicationWithdrawalRequest(APPLICATION_ID, form, null, user, null)))
                .with(csrf())
                .with(user(user))
                .param("requestText", "test")
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(IndustryCaseProcessingController.class)
            .getIndustryCaseProcessing(APPLICATION_ID, null, null, null))))
        .andExpect(notificationBanner(expectedNotificationBanner));
  }

  @SecurityTest
  void getApplicationWithdrawalResponse_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ApplicationWithdrawalController.class)
            .getApplicationWithdrawalResponse(APPLICATION_ID))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getApplicationWithdrawalResponse_checkEndPointSecurityOnly_forbidden() throws Exception {
    var applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationWithdrawalController.class)
            .getApplicationWithdrawalResponse(APPLICATION_ID)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void getApplicationWithdrawalResponse_checkEndPointSecurityOnly_allowed() throws Exception {
    var applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion));
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);
    when(applicationService.generateApplicationReference(applicationVersion))
        .thenReturn(DUMMY_APP_REF);
    when(applicationWithdrawalService.getWithdrawalResponseForm(applicationVersion))
        .thenReturn(new WithdrawalResponseForm());
    when(withdrawalRequestViewService.getWithdrawalRequestView(applicationVersion))
        .thenReturn(getWithdrawalRequestView());
    when(caseProcessingActionService.userHasAnyAction(
        applicationVersion,
        user,
        CASE_OFFICER_WITHDRAWAL_RESPONSE
    )).thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationWithdrawalController.class)
            .getApplicationWithdrawalResponse(APPLICATION_ID)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/withdrawalResponseForm"));
  }

  @ParameterizedTest
  @MethodSource("getSubmittedApplicationVersions")
  void getApplicationWithdrawalResponse(ApplicationVersion applicationVersion) throws Exception {
    var form = new WithdrawalResponseForm();
    var withdrawalRequestView = getWithdrawalRequestView();
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion));
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);
    when(applicationService.generateApplicationReference(applicationVersion))
        .thenReturn(DUMMY_APP_REF);
    when(applicationWithdrawalService.getWithdrawalResponseForm(applicationVersion))
        .thenReturn(form);
    when(withdrawalRequestViewService.getWithdrawalRequestView(applicationVersion))
        .thenReturn(withdrawalRequestView);
    when(caseProcessingActionService.userHasAnyAction(
        applicationVersion,
        user,
        CASE_OFFICER_WITHDRAWAL_RESPONSE
    )).thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationWithdrawalController.class)
            .getApplicationWithdrawalResponse(APPLICATION_ID)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/withdrawalResponseForm"))
        .andExpect(model().attribute("form", form))
        .andExpect(model().attribute("responseStatuses", WithdrawalStatus.getWithdrawalResponseOptions()))
        .andExpect(model().attribute("withdrawalRequestView", withdrawalRequestView))
        .andExpect(model().attribute("submitUrl",
            ReverseRouter.route(on(ApplicationWithdrawalController.class)
                .submitApplicationWithdrawalResponse(APPLICATION_ID, null, null, null, null))))
        .andExpect(model().attribute("backLinkUrl",
            ReverseRouter.route(on(ApplicationCaseProcessingController.class)
                .caseProcessing(APPLICATION_ID, null, null, null))));
  }

  @SecurityTest
  void submitApplicationWithdrawalResponse_noUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(ApplicationWithdrawalController.class)
            .submitApplicationWithdrawalResponse(APPLICATION_ID, null, null, user, null)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @ParameterizedTest
  @MethodSource("getSubmittedApplicationVersions")
  void submitApplicationWithdrawalResponse_withEmptyForm(ApplicationVersion applicationVersion) throws Exception {
    var form = new WithdrawalResponseForm();
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion));
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);

    when(caseProcessingActionService.userHasAnyAction(
        applicationVersion,
        user,
        CASE_OFFICER_WITHDRAWAL_RESPONSE
    )).thenReturn(true);
    when(withdrawalRequestViewService.getWithdrawalRequestView(applicationVersion))
        .thenReturn(getWithdrawalRequestView());

    doCallRealMethod().when(withdrawalResponseFormValidator).validate(any(), any());

    mockMvc.perform(
            post(ReverseRouter.route(on(ApplicationWithdrawalController.class)
                .submitApplicationWithdrawalResponse(APPLICATION_ID, form, null, user, null)))
                .with(csrf())
                .with(user(user))
        )
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/withdrawalResponseForm"));

    verifyNoInteractions(applicationWithdrawalService);
  }

  @ParameterizedTest
  @MethodSource("getSubmittedApplicationVersions")
  void submitApplicationWithdrawalResponse_withNonEmptyForm_andRequestAccepted(ApplicationVersion applicationVersion) throws Exception {
    var form = new WithdrawalResponseForm();
    var withdrawalRequestView = getWithdrawalRequestView();
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion));
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);

    when(caseProcessingActionService.userHasAnyAction(
        applicationVersion,
        user,
        CASE_OFFICER_WITHDRAWAL_RESPONSE
    )).thenReturn(true);
    when(withdrawalRequestViewService.getWithdrawalRequestView(applicationVersion))
        .thenReturn(withdrawalRequestView);

    doCallRealMethod().when(withdrawalResponseFormValidator).validate(any(), any());

    var expectedNotificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeadingContent("Withdrawal accepted")
        .build();

    mockMvc.perform(
            post(ReverseRouter.route(on(ApplicationWithdrawalController.class)
                .submitApplicationWithdrawalResponse(APPLICATION_ID, form, null, user, null)))
                .with(csrf())
                .with(user(user))
                .param("responseStatus", WithdrawalStatus.ACCEPTED.getEnumName())
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(WorkAreaController.class)
            .getWorkArea(null, null))))
        .andExpect(notificationBanner(expectedNotificationBanner));
  }

  @ParameterizedTest
  @MethodSource("getSubmittedApplicationVersions")
  void submitApplicationWithdrawalResponse_withNonEmptyForm_andRequestRejected(ApplicationVersion applicationVersion) throws Exception {
    var form = new WithdrawalResponseForm();
    var withdrawalRequestView = getWithdrawalRequestView();
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion));
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);

    when(caseProcessingActionService.userHasAnyAction(
        applicationVersion,
        user,
        CASE_OFFICER_WITHDRAWAL_RESPONSE
    )).thenReturn(true);
    when(withdrawalRequestViewService.getWithdrawalRequestView(applicationVersion))
        .thenReturn(withdrawalRequestView);

    doCallRealMethod().when(withdrawalResponseFormValidator).validate(any(), any());

    var expectedNotificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeadingContent("Withdrawal rejected")
        .build();

    mockMvc.perform(
            post(ReverseRouter.route(on(ApplicationWithdrawalController.class)
                .submitApplicationWithdrawalResponse(APPLICATION_ID, form, null, user, null)))
                .with(csrf())
                .with(user(user))
                .param("responseStatus", WithdrawalStatus.REJECTED.getEnumName())
                .param("responseText.inputValue", "response text")
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(WorkAreaController.class)
            .getWorkArea(null, null))))
        .andExpect(notificationBanner(expectedNotificationBanner));
  }

  private static Stream<Arguments> getSubmittedApplicationVersions() {
    return Stream.of(
        Arguments.of(ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION)),
        Arguments.of(ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.FLARE)),
        Arguments.of(ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.VENT))
    );
  }
}
