package uk.co.nstauthority.fieldconsents.application.submission;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateTestUtil.applicationUpdateRequestView;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.fieldconsents.AbstractApplicationControllerTest;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.update.request.ApplicationUpdateRequestViewService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.update.response.ApplicationUpdateResponseFormValidator;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.update.response.ApplicationUpdateResponseType;
import uk.co.nstauthority.fieldconsents.application.payment.ApplicationPaymentController;
import uk.co.nstauthority.fieldconsents.application.payment.ApplicationPaymentService;
import uk.co.nstauthority.fieldconsents.application.summary.ApplicationSummaryService;
import uk.co.nstauthority.fieldconsents.application.tasklist.shared.ApplicationTaskListController;
import uk.co.nstauthority.fieldconsents.authorisation.ParameterizedSecurityTest;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.fieldconsents.workarea.WorkAreaController;

@ContextConfiguration(classes = ApplicationSubmissionController.class)
class ApplicationSubmissionControllerTest extends AbstractApplicationControllerTest {

  private static final String DUMMY_APP_REF = "DUMMY_APP_REF";

  private static final String NO_APP_REF = "";

  @MockBean
  private ApplicationSubmissionService applicationSubmissionService;

  @MockBean
  private ApplicationService applicationService;

  @MockBean
  private ApplicationSummaryService applicationSummaryService;

  @MockBean
  private ApplicationUpdateService applicationUpdateService;

  @MockBean
  private ApplicationUpdateRequestViewService applicationUpdateRequestViewService;

  @MockBean
  private ApplicationPaymentService applicationPaymentService;

  @MockBean
  private ApplicationUpdateResponseFormValidator applicationUpdateResponseFormValidator;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.FLARE);
    when(applicationVersionService.findLatestApplicationVersion(ApplicationTestUtil.APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion));
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
  }

  @SecurityTest
  void getReviewAndSubmit_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ApplicationSubmissionController.class)
            .getReviewAndSubmit(APPLICATION_ID, null))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getReviewAndSubmit_checkEndPointSecurityOnly_whenSubmittedApplication_thenForbidden() throws Exception {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.FLARE);
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationSubmissionController.class)
            .getReviewAndSubmit(APPLICATION_ID, null)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void getReviewAndSubmit_checkEndPointSecurityOnly_whenUserDoesNotHaveEditPermission_thenForbidden() throws Exception {
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationAccessService.hasApplicationPermission(
        user, applicationVersion, RolePermission.EDIT_FCS_APPLICATIONS
    )).thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationSubmissionController.class)
            .getReviewAndSubmit(APPLICATION_ID, null)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @ParameterizedTest
  @MethodSource("getInProgressApplicationVersions")
  void getReviewAndSubmit_whenInProgressAndNotSubmittableAndUserDoesNotHavePayAndSubmitPermission(
      ApplicationVersion applicationVersion
  ) throws Exception {
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);
    doCallRealMethod().when(applicationSummaryService).addSummarySectionsToModelAndView(any(), any());
    when(applicationSummaryService.getSummarySections(applicationVersion))
        .thenReturn(Collections.emptyList());
    when(applicationSubmissionService.isSubmittable(applicationVersion)).thenReturn(false);
    when(applicationAccessService.hasApplicationPermission(
        user, applicationVersion, RolePermission.PAY_AND_SUBMIT_FCS_APPLICATIONS
    )).thenReturn(false);
    when(applicationService.getApplicationReference(applicationVersion))
        .thenReturn(NO_APP_REF);
    when(applicationUpdateService.openApplicationUpdateExists(applicationVersion))
        .thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationSubmissionController.class)
            .getReviewAndSubmit(APPLICATION_ID, null)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/reviewAndSubmit"))
        .andExpect(model().attribute("pageTitle", "Check your answers before submitting"))
        .andExpect(model().attribute("accordionId", applicationVersion.getId()))
        .andExpect(model().attribute("submitUrl", ReverseRouter.route(on(ApplicationSubmissionController.class)
            .submitApplication(APPLICATION_ID, null, null, null))))
        .andExpect(model().attribute("backLinkUrl", ReverseRouter.route(on(ApplicationTaskListController.class)
            .getTaskList(APPLICATION_ID))))
        .andExpect(model().attribute("isSubmittable", false))
        .andExpect(model().attribute("userHasPayAndSubmitPermission", false))
        .andExpect(model().attribute("applicationReference", NO_APP_REF))
        .andExpect(model().attributeDoesNotExist("paymentRequired"))
        .andExpect(model().attributeExists("summarySections", "wideSummaryDisplay"));

    verify(applicationPaymentService, never()).getPaymentAmountPence(any());
  }

  @ParameterizedTest
  @MethodSource("getInProgressApplicationVersions")
  void getReviewAndSubmit_whenInProgressAndSubmittableAndUserDoesNotHavePayAndSubmitPermission(
      ApplicationVersion applicationVersion
  ) throws Exception {
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);
    doCallRealMethod().when(applicationSummaryService).addSummarySectionsToModelAndView(any(), any());
    when(applicationSummaryService.getSummarySections(applicationVersion))
        .thenReturn(Collections.emptyList());
    when(applicationSubmissionService.isSubmittable(applicationVersion)).thenReturn(true);
    when(applicationAccessService.hasApplicationPermission(
        user, applicationVersion, RolePermission.PAY_AND_SUBMIT_FCS_APPLICATIONS
    )).thenReturn(false);
    when(applicationService.getApplicationReference(applicationVersion))
        .thenReturn(NO_APP_REF);
    when(applicationUpdateService.openApplicationUpdateExists(applicationVersion))
        .thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationSubmissionController.class)
            .getReviewAndSubmit(APPLICATION_ID, null)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/reviewAndSubmit"))
        .andExpect(model().attribute("pageTitle", "Check your answers before submitting"))
        .andExpect(model().attribute("accordionId", applicationVersion.getId()))
        .andExpect(model().attribute("submitUrl", ReverseRouter.route(on(ApplicationSubmissionController.class)
            .submitApplication(APPLICATION_ID, null, null, null))))
        .andExpect(model().attribute("backLinkUrl", ReverseRouter.route(on(ApplicationTaskListController.class)
            .getTaskList(APPLICATION_ID))))
        .andExpect(model().attribute("isSubmittable", true))
        .andExpect(model().attribute("userHasPayAndSubmitPermission", false))
        .andExpect(model().attribute("applicationReference", NO_APP_REF))
        .andExpect(model().attributeDoesNotExist("paymentRequired"))
        .andExpect(model().attributeExists("summarySections", "wideSummaryDisplay"));

    verify(applicationPaymentService, never()).getPaymentAmountPence(any());
  }

  @ParameterizedTest
  @MethodSource("getInProgressApplicationVersions")
  void getReviewAndSubmit_whenInProgressAndSubmittableAndUserHasPayAndSubmitPermissionAndPaymentAmountPenceGreaterThanZero(
      ApplicationVersion applicationVersion
  ) throws Exception {
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);
    doCallRealMethod().when(applicationSummaryService).addSummarySectionsToModelAndView(any(), any());
    when(applicationSummaryService.getSummarySections(applicationVersion))
        .thenReturn(Collections.emptyList());
    when(applicationSubmissionService.isSubmittable(applicationVersion)).thenReturn(true);
    when(applicationAccessService.hasApplicationPermission(
        user, applicationVersion, RolePermission.PAY_AND_SUBMIT_FCS_APPLICATIONS
    )).thenReturn(true);
    when(applicationService.getApplicationReference(applicationVersion))
        .thenReturn(NO_APP_REF);
    when(applicationUpdateService.openApplicationUpdateExists(applicationVersion))
        .thenReturn(false);
    when(applicationPaymentService.getPaymentAmountPence(applicationVersion)).thenReturn(100);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationSubmissionController.class)
            .getReviewAndSubmit(APPLICATION_ID, null)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/reviewAndSubmit"))
        .andExpect(model().attribute("pageTitle", "Check your answers before submitting"))
        .andExpect(model().attribute("accordionId", applicationVersion.getId()))
        .andExpect(model().attribute("submitUrl", ReverseRouter.route(on(ApplicationSubmissionController.class)
            .submitApplication(APPLICATION_ID, null, null, null))))
        .andExpect(model().attribute("backLinkUrl", ReverseRouter.route(on(ApplicationTaskListController.class)
            .getTaskList(APPLICATION_ID))))
        .andExpect(model().attribute("isSubmittable", true))
        .andExpect(model().attribute("userHasPayAndSubmitPermission", true))
        .andExpect(model().attribute("applicationReference", NO_APP_REF))
        .andExpect(model().attribute("paymentRequired", true))
        .andExpect(model().attributeExists("summarySections", "wideSummaryDisplay"));
  }

  @ParameterizedTest
  @MethodSource("getInProgressApplicationVersions")
  void getReviewAndSubmit_whenInProgressAndSubmittableAndUserHasPayAndSubmitPermissionAndPaymentAmountPenceZero(
      ApplicationVersion applicationVersion
  ) throws Exception {
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);
    doCallRealMethod().when(applicationSummaryService).addSummarySectionsToModelAndView(any(), any());
    when(applicationSummaryService.getSummarySections(applicationVersion))
        .thenReturn(Collections.emptyList());
    when(applicationSubmissionService.isSubmittable(applicationVersion)).thenReturn(true);
    when(applicationAccessService.hasApplicationPermission(
        user, applicationVersion, RolePermission.PAY_AND_SUBMIT_FCS_APPLICATIONS
    )).thenReturn(true);
    when(applicationService.getApplicationReference(applicationVersion))
        .thenReturn(NO_APP_REF);
    when(applicationUpdateService.openApplicationUpdateExists(applicationVersion))
        .thenReturn(false);
    when(applicationPaymentService.getPaymentAmountPence(applicationVersion)).thenReturn(0);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationSubmissionController.class)
            .getReviewAndSubmit(APPLICATION_ID, null)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/reviewAndSubmit"))
        .andExpect(model().attribute("pageTitle", "Check your answers before submitting"))
        .andExpect(model().attribute("accordionId", applicationVersion.getId()))
        .andExpect(model().attribute("submitUrl", ReverseRouter.route(on(ApplicationSubmissionController.class)
            .submitApplication(APPLICATION_ID, null, null, null))))
        .andExpect(model().attribute("backLinkUrl", ReverseRouter.route(on(ApplicationTaskListController.class)
            .getTaskList(APPLICATION_ID))))
        .andExpect(model().attribute("isSubmittable", true))
        .andExpect(model().attribute("userHasPayAndSubmitPermission", true))
        .andExpect(model().attribute("applicationReference", NO_APP_REF))
        .andExpect(model().attribute("paymentRequired", false))
        .andExpect(model().attributeExists("summarySections", "wideSummaryDisplay"));
  }

  @ParameterizedTest
  @MethodSource("getInProgressV2ApplicationVersions")
  void getReviewAndSubmit_whenInProgressUpdatesAndSubmittableAndUserHasPayAndSubmitPermission(ApplicationVersion applicationVersion) throws Exception {
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);
    doCallRealMethod().when(applicationSummaryService).addSummarySectionsToModelAndView(any(), any());
    when(applicationSummaryService.getSummarySections(applicationVersion))
        .thenReturn(Collections.emptyList());
    when(applicationSubmissionService.isSubmittable(applicationVersion)).thenReturn(true);
    when(applicationAccessService.hasApplicationPermission(
        user, applicationVersion, RolePermission.PAY_AND_SUBMIT_FCS_APPLICATIONS
    )).thenReturn(true);
    when(applicationService.getApplicationReference(applicationVersion))
        .thenReturn(DUMMY_APP_REF);
    when(applicationUpdateService.openApplicationUpdateExists(applicationVersion))
        .thenReturn(true);
    when(applicationUpdateRequestViewService.getOpenApplicationUpdateRequestView(applicationVersion))
        .thenReturn(applicationUpdateRequestView);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationSubmissionController.class)
            .getReviewAndSubmit(APPLICATION_ID, null)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/reviewAndSubmit"))
        .andExpect(model().attribute("pageTitle", "Check your answers before submitting"))
        .andExpect(model().attribute("accordionId", applicationVersion.getId()))
        .andExpect(model().attribute("submitUrl", ReverseRouter.route(on(ApplicationSubmissionController.class)
            .submitApplication(APPLICATION_ID, null, null, null))))
        .andExpect(model().attribute("backLinkUrl", ReverseRouter.route(on(ApplicationTaskListController.class)
            .getTaskList(APPLICATION_ID))))
        .andExpect(model().attribute("isSubmittable", true))
        .andExpect(model().attribute("userHasPayAndSubmitPermission", true))
        .andExpect(model().attribute("applicationReference", DUMMY_APP_REF))
        .andExpect(model().attribute("applicationUpdateRequestView", applicationUpdateRequestView))
        .andExpect(model().attribute("requestedChangesOnlyRadio", ApplicationUpdateResponseType.REQUESTED_CHANGES_ONLY))
        .andExpect(model().attribute("otherChangesRadio", ApplicationUpdateResponseType.OTHER_CHANGES))
        .andExpect(model().attribute("paymentRequired", false))
        .andExpect(model().attributeExists("summarySections", "wideSummaryDisplay", "form"));

    verify(applicationPaymentService, never()).getPaymentAmountPence(any());
  }

  @Test
  void submitApplication_paymentAmountPenceGreaterThanZero() throws Exception {
    when(applicationSubmissionService.isSubmittable(applicationVersion)).thenReturn(true);
    when(applicationUpdateService.openApplicationUpdateExists(applicationVersion)).thenReturn(false);
    when(applicationPaymentService.getPaymentAmountPence(applicationVersion)).thenReturn(100);

    mockMvc.perform(post(ReverseRouter.route(on(ApplicationSubmissionController.class)
            .submitApplication(APPLICATION_ID, null, null, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(ApplicationPaymentController.class)
            .getStartPayment(APPLICATION_ID, null))));

    verify(applicationService).prepareApplicationForPayment(applicationVersion);
    verify(applicationSubmissionService, never()).submitApplication(any(), any());
  }

  @Test
  void submitApplication_paymentAmountPenceZero() throws Exception {
    when(applicationSubmissionService.isSubmittable(applicationVersion)).thenReturn(true);
    when(applicationUpdateService.openApplicationUpdateExists(applicationVersion)).thenReturn(false);
    when(applicationPaymentService.getPaymentAmountPence(applicationVersion)).thenReturn(0);

    mockMvc.perform(post(ReverseRouter.route(on(ApplicationSubmissionController.class)
            .submitApplication(APPLICATION_ID, null, null, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(ApplicationSubmissionController.class)
            .getApplicationSubmitted(APPLICATION_ID))));

    verify(applicationSubmissionService).submitApplication(applicationVersion, user);
    verify(applicationService, never()).prepareApplicationForPayment(any());
  }

  @ParameterizedTest
  @MethodSource("getInProgressV2ApplicationVersions")
  void submitApplication_whenAppUpdate_formValid(ApplicationVersion applicationVersion) throws Exception {
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);
    when(applicationSubmissionService.isSubmittable(applicationVersion)).thenReturn(true);
    when(applicationService.generateApplicationReference(applicationVersion)).thenReturn(DUMMY_APP_REF);
    when(applicationUpdateService.openApplicationUpdateExists(applicationVersion)).thenReturn(true);
    doCallRealMethod().when(applicationUpdateResponseFormValidator).validate(any(), any());

    mockMvc.perform(post(ReverseRouter.route(on(ApplicationSubmissionController.class)
            .submitApplication(APPLICATION_ID, null, null, null)))
            .with(user(user))
            .with(csrf())
            .param("responseType", ApplicationUpdateResponseType.REQUESTED_CHANGES_ONLY.name())
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(ApplicationSubmissionController.class)
            .getApplicationUpdateSubmitted(APPLICATION_ID))));

    verify(applicationUpdateService, times(1))
        .saveApplicationUpdateResponseAndSubmitApplicationUpdate(applicationVersion,
            ApplicationUpdateResponseType.REQUESTED_CHANGES_ONLY, null, user);
  }

  @ParameterizedTest
  @MethodSource("getInProgressV2ApplicationVersions")
  void submitApplication_whenAppUpdate_formInvalid(ApplicationVersion applicationVersion) throws Exception {
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);
    when(applicationSubmissionService.isSubmittable(applicationVersion)).thenReturn(true);
    when(applicationService.generateApplicationReference(applicationVersion)).thenReturn(DUMMY_APP_REF);
    when(applicationUpdateService.openApplicationUpdateExists(applicationVersion)).thenReturn(true);
    doCallRealMethod().when(applicationUpdateResponseFormValidator).validate(any(), any());

    when(applicationAccessService.hasApplicationPermission(
        user, applicationVersion, RolePermission.PAY_AND_SUBMIT_FCS_APPLICATIONS
    )).thenReturn(true);
    when(applicationService.getApplicationReference(applicationVersion))
        .thenReturn(DUMMY_APP_REF);
    when(applicationSummaryService.getSummarySections(applicationVersion))
        .thenReturn(Collections.emptyList());
    when(applicationUpdateService.openApplicationUpdateExists(applicationVersion))
        .thenReturn(true);
    when(applicationUpdateRequestViewService.getOpenApplicationUpdateRequestView(applicationVersion))
        .thenReturn(applicationUpdateRequestView);
    doCallRealMethod().when(applicationSummaryService).addSummarySectionsToModelAndView(any(), any());

    mockMvc.perform(post(ReverseRouter.route(on(ApplicationSubmissionController.class)
            .submitApplication(APPLICATION_ID, null, null, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/reviewAndSubmit"))
        .andExpect(model().attribute("pageTitle", "Check your answers before submitting"))
        .andExpect(model().attribute("accordionId", applicationVersion.getId()))
        .andExpect(model().attribute("submitUrl", ReverseRouter.route(on(ApplicationSubmissionController.class)
            .submitApplication(APPLICATION_ID, null, null, null))))
        .andExpect(model().attribute("backLinkUrl", ReverseRouter.route(on(ApplicationTaskListController.class)
            .getTaskList(APPLICATION_ID))))
        .andExpect(model().attribute("isSubmittable", true))
            .andExpect(model().attribute("userHasPayAndSubmitPermission", true))
        .andExpect(model().attribute("applicationReference", DUMMY_APP_REF))
        .andExpect(model().attribute("applicationUpdateRequestView", applicationUpdateRequestView))
        .andExpect(model().attribute("requestedChangesOnlyRadio", ApplicationUpdateResponseType.REQUESTED_CHANGES_ONLY))
        .andExpect(model().attribute("otherChangesRadio", ApplicationUpdateResponseType.OTHER_CHANGES))
        .andExpect(model().attributeExists(
            "summarySections", "wideSummaryDisplay", "form"));
  }

  @Test
  void submitApplication_whenNotSubmittable() {
    when(applicationSubmissionService.isSubmittable(applicationVersion)).thenReturn(false);

    assertThatThrownBy(
        () -> mockMvc.perform(post(ReverseRouter.route(on(ApplicationSubmissionController.class)
            .submitApplication(APPLICATION_ID, null, null, null)))
            .with(user(user))
            .with(csrf()))
    ).hasMessageContaining("The application with id 1 cannot be submitted!");
  }

  @SecurityTest
  void submitApplication_withUnauthorizedUser() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(ApplicationSubmissionController.class)
            .submitApplication(APPLICATION_ID, null, null, null)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getApplicationSubmitted_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ApplicationSubmissionController.class)
            .getApplicationSubmitted(APPLICATION_ID))))
        .andExpect(redirectionToLoginUrl());
  }

  @ParameterizedSecurityTest
  @EnumSource(value = ApplicationVersionStatus.class, names = "SUBMITTED", mode = EnumSource.Mode.EXCLUDE)
  void getApplicationSubmitted_statusNotSubmitted(
      ApplicationVersionStatus applicationVersionStatus
  ) throws Exception {
    applicationVersion.setStatus(applicationVersionStatus);

    when(applicationAccessService.hasApplicationPermission(
        user,
        applicationVersion,
        RolePermission.PAY_AND_SUBMIT_FCS_APPLICATIONS
    )).thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationSubmissionController.class)
            .getApplicationSubmitted(APPLICATION_ID)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void getApplicationSubmitted_userDoesNotHavePayAndSubmitPermission() throws Exception {
    applicationVersion.setStatus(ApplicationVersionStatus.SUBMITTED);

    when(applicationAccessService.hasApplicationPermission(
        user,
        applicationVersion,
        RolePermission.PAY_AND_SUBMIT_FCS_APPLICATIONS
    )).thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationSubmissionController.class)
            .getApplicationSubmitted(APPLICATION_ID)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void getApplicationSubmitted() throws Exception {
    applicationVersion.setStatus(ApplicationVersionStatus.SUBMITTED);

    var applicationReference = "testApplicationReference";

    when(applicationAccessService.hasApplicationPermission(
        user,
        applicationVersion,
        RolePermission.PAY_AND_SUBMIT_FCS_APPLICATIONS
    )).thenReturn(true);
    when(applicationService.generateApplicationReference(applicationVersion)).thenReturn(applicationReference);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationSubmissionController.class)
            .getApplicationSubmitted(APPLICATION_ID)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/submissionConfirmation"))
        .andExpect(model().attribute("pageTitle", ApplicationSubmissionController.SUBMITTED_PAGE_TITLE))
        .andExpect(model().attribute("applicationReference", applicationReference))
        .andExpect(model().attribute("workAreaUrl", ReverseRouter.route(on(WorkAreaController.class)
            .getWorkArea(null, null))));
  }

  @SecurityTest
  void getApplicationPaidAndSubmitted_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ApplicationSubmissionController.class)
            .getApplicationPaidAndSubmitted(APPLICATION_ID))))
        .andExpect(redirectionToLoginUrl());
  }

  @ParameterizedSecurityTest
  @EnumSource(value = ApplicationVersionStatus.class, names = "SUBMITTED", mode = EnumSource.Mode.EXCLUDE)
  void getApplicationPaidAndSubmitted_statusNotSubmitted(
      ApplicationVersionStatus applicationVersionStatus
  ) throws Exception {
    applicationVersion.setStatus(applicationVersionStatus);

    when(applicationAccessService.hasApplicationPermission(
        user,
        applicationVersion,
        RolePermission.PAY_AND_SUBMIT_FCS_APPLICATIONS
    )).thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationSubmissionController.class)
            .getApplicationPaidAndSubmitted(APPLICATION_ID)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void getApplicationPaidAndSubmitted_userDoesNotHavePayAndSubmitPermission() throws Exception {
    applicationVersion.setStatus(ApplicationVersionStatus.SUBMITTED);

    when(applicationAccessService.hasApplicationPermission(
        user,
        applicationVersion,
        RolePermission.PAY_AND_SUBMIT_FCS_APPLICATIONS
    )).thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationSubmissionController.class)
            .getApplicationPaidAndSubmitted(APPLICATION_ID)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void getApplicationPaidAndSubmitted() throws Exception {
    applicationVersion.setStatus(ApplicationVersionStatus.SUBMITTED);

    var applicationReference = "testApplicationReference";

    when(applicationAccessService.hasApplicationPermission(
        user,
        applicationVersion,
        RolePermission.PAY_AND_SUBMIT_FCS_APPLICATIONS
    )).thenReturn(true);
    when(applicationService.generateApplicationReference(applicationVersion)).thenReturn(applicationReference);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationSubmissionController.class)
            .getApplicationPaidAndSubmitted(APPLICATION_ID)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/submissionConfirmation"))
        .andExpect(model().attribute("pageTitle", ApplicationSubmissionController.PAID_AND_SUBMITTED_PAGE_TITLE))
        .andExpect(model().attribute("applicationReference", applicationReference))
        .andExpect(model().attribute("workAreaUrl", ReverseRouter.route(on(WorkAreaController.class)
            .getWorkArea(null, null))));
  }

  @SecurityTest
  void getApplicationUpdateSubmitted_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ApplicationSubmissionController.class)
            .getApplicationUpdateSubmitted(APPLICATION_ID))))
        .andExpect(redirectionToLoginUrl());
  }

  @ParameterizedSecurityTest
  @EnumSource(value = ApplicationVersionStatus.class, names = "SUBMITTED", mode = EnumSource.Mode.EXCLUDE)
  void getApplicationUpdateSubmitted_statusNotSubmitted(
      ApplicationVersionStatus applicationVersionStatus
  ) throws Exception {
    applicationVersion.setStatus(applicationVersionStatus);

    when(applicationAccessService.hasApplicationPermission(
        user,
        applicationVersion,
        RolePermission.PAY_AND_SUBMIT_FCS_APPLICATIONS
    )).thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationSubmissionController.class)
            .getApplicationUpdateSubmitted(APPLICATION_ID)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void getApplicationUpdateSubmitted_userDoesNotHavePayAndSubmitPermission() throws Exception {
    applicationVersion.setStatus(ApplicationVersionStatus.SUBMITTED);

    when(applicationAccessService.hasApplicationPermission(
        user,
        applicationVersion,
        RolePermission.PAY_AND_SUBMIT_FCS_APPLICATIONS
    )).thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationSubmissionController.class)
            .getApplicationUpdateSubmitted(APPLICATION_ID)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void getApplicationUpdateSubmitted() throws Exception {
    applicationVersion.setStatus(ApplicationVersionStatus.SUBMITTED);

    var applicationReference = "testApplicationReference";

    when(applicationAccessService.hasApplicationPermission(
        user,
        applicationVersion,
        RolePermission.PAY_AND_SUBMIT_FCS_APPLICATIONS
    )).thenReturn(true);
    when(applicationService.generateApplicationReference(applicationVersion)).thenReturn(applicationReference);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationSubmissionController.class)
            .getApplicationUpdateSubmitted(APPLICATION_ID)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/submissionConfirmation"))
        .andExpect(model().attribute("pageTitle", ApplicationSubmissionController.UPDATE_SUBMITTED_PAGE_TITLE))
        .andExpect(model().attribute("applicationReference", applicationReference))
        .andExpect(model().attribute("workAreaUrl", ReverseRouter.route(on(WorkAreaController.class)
            .getWorkArea(null, null))));
  }

  private static Stream<Arguments> getInProgressApplicationVersions() {
    return Stream.of(
        Arguments.of(ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION)),
        Arguments.of(ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.FLARE)),
        Arguments.of(ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.VENT))
    );
  }

  private static Stream<Arguments> getInProgressV2ApplicationVersions() {
    return Stream.of(
        Arguments.of(ApplicationTestUtil.getNewApplicationVersionWithTypeIdAndVersionNumber(ApplicationType.PRODUCTION, 2, 2)),
        Arguments.of(ApplicationTestUtil.getNewApplicationVersionWithTypeIdAndVersionNumber(ApplicationType.FLARE, 2, 2)),
        Arguments.of(ApplicationTestUtil.getNewApplicationVersionWithTypeIdAndVersionNumber(ApplicationType.VENT, 2, 2))
    );
  }
}
