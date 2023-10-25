package uk.co.nstauthority.fieldconsents.application.summary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_ID;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.ALLOCATE_CONSULTATION;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.ASSIGN_FCS_APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.EDIT_FCS_APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.PROCESS_FCS_APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.RESPOND_TO_CONSULTATION;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.SUBMIT_FCS_APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.TECHNICAL_REVIEW_FCS_APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.Collections;
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
import uk.co.nstauthority.fieldconsents.application.caseprocessing.ConsulteeCaseProcessingController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.IndustryCaseProcessingController;
import uk.co.nstauthority.fieldconsents.application.tasklist.shared.ApplicationTaskListController;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@ContextConfiguration(classes = ApplicationSummaryController.class)
class ApplicationSummaryControllerTest extends AbstractApplicationControllerTest {

  private static final String DUMMY_APP_REF = "DUMMY_APP_REF";

  private static final RolePermission[] REGULATOR_CASE_PROCESSING_ROLES = new RolePermission[] {
      PROCESS_FCS_APPLICATIONS, ASSIGN_FCS_APPLICATIONS, TECHNICAL_REVIEW_FCS_APPLICATIONS
  };

  private static final RolePermission[] CONSULTEE_CASE_PROCESSING_ROLES = new RolePermission[] {
      ALLOCATE_CONSULTATION, RESPOND_TO_CONSULTATION
  };

  private static final RolePermission[] INDUSTRY_CASE_PROCESSING_RULES = new RolePermission[] {
      EDIT_FCS_APPLICATIONS
  };

  @MockBean
  private ApplicationSummaryService applicationSummaryService;

  @MockBean
  private ApplicationService applicationService;

  @ParameterizedTest
  @MethodSource("getSubmittedApplicationVersions")
  void getApplicationSummary_whenSubmittedApplicationAndUserHasNoProcessPermission_thenGetSummaryView(ApplicationVersion applicationVersion) throws Exception {
    // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID)).thenReturn(Optional.of(applicationVersion));
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
    when(applicationAccessService.hasApplicationPermission(user, applicationVersion, INDUSTRY_CASE_PROCESSING_RULES)).thenReturn(false);
    when(applicationAccessService.hasApplicationPermission(user, applicationVersion, REGULATOR_CASE_PROCESSING_ROLES)).thenReturn(false);
    when(applicationAccessService.hasApplicationPermission(user, applicationVersion, CONSULTEE_CASE_PROCESSING_ROLES)).thenReturn(false);
    when(applicationSummaryService.getSummarySections(applicationVersion)).thenReturn(Collections.emptyList());
    when(applicationService.generateApplicationReference(applicationVersion)).thenReturn(DUMMY_APP_REF);

    getApplicationSummaryAndCheckModel(applicationVersion, DUMMY_APP_REF);
  }

  @ParameterizedTest
  @MethodSource("getSubmittedApplicationVersions")
  void getApplicationSummary_whenSubmittedApplicationAndUserIsConsultee_thenGetConsulteeCaseProcessing(ApplicationVersion applicationVersion) throws Exception {
    // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID)).thenReturn(Optional.of(applicationVersion));
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
    when(applicationAccessService.hasApplicationPermission(user, applicationVersion, REGULATOR_CASE_PROCESSING_ROLES)).thenReturn(false);
    when(applicationAccessService.hasApplicationPermission(user, applicationVersion, CONSULTEE_CASE_PROCESSING_ROLES)).thenReturn(true);
    when(applicationSummaryService.getSummarySections(applicationVersion)).thenReturn(Collections.emptyList());
    when(applicationService.generateApplicationReference(applicationVersion)).thenReturn(DUMMY_APP_REF);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationSummaryController.class)
            .getApplicationSummary(APPLICATION_ID, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(ConsulteeCaseProcessingController.class)
            .caseProcessing(APPLICATION_ID, null, null))));
  }

  @ParameterizedTest
  @MethodSource("getInProgressApplicationVersions")
  void getApplicationSummary_whenInProgressAndUserHasNoEditPermission_thenGetSummaryView(ApplicationVersion applicationVersion) throws Exception {
    // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID)).thenReturn(Optional.of(applicationVersion));
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
    when(applicationAccessService.hasApplicationPermission(user, applicationVersion, INDUSTRY_CASE_PROCESSING_RULES)).thenReturn(false);
    when(applicationAccessService.hasApplicationPermission(user, applicationVersion, REGULATOR_CASE_PROCESSING_ROLES)).thenReturn(true);

    getApplicationSummaryAndCheckModel(applicationVersion, "Application summary");
  }

  private void getApplicationSummaryAndCheckModel(ApplicationVersion applicationVersion, String expectedPageTitle) throws Exception {
    doCallRealMethod().when(applicationSummaryService).getApplicationSummaryModelAndView(any(), any(), any());
    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(ApplicationSummaryController.class)
            .getApplicationSummary(APPLICATION_ID, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/applicationSummary"))
        .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertThat(model)
        .containsEntry("pageTitle", expectedPageTitle)
        .containsKey("summarySections")
        .containsEntry("accordionId", applicationVersion.getId())
        .containsKey("wideSummaryDisplay");

  }

  @ParameterizedTest
  @MethodSource("getInProgressApplicationVersions")
  void getApplicationSummary_whenInProgressApplication_thenRedirectToTaskList(ApplicationVersion applicationVersion) throws Exception {
    // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID)).thenReturn(Optional.of(applicationVersion));
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
    when(applicationAccessService.hasApplicationPermission(user, applicationVersion, INDUSTRY_CASE_PROCESSING_RULES)).thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationSummaryController.class)
            .getApplicationSummary(APPLICATION_ID, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(ApplicationTaskListController.class)
            .getTaskList(APPLICATION_ID))));
  }

  @ParameterizedTest
  @MethodSource("getInProgressV2ApplicationVersions")
  void getApplicationSummary_whenInProgressV2Application_thenRedirect(ApplicationVersion applicationVersion) throws Exception {
    // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID)).thenReturn(Optional.of(applicationVersion));
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
    when(applicationAccessService.hasApplicationPermission(user, applicationVersion, INDUSTRY_CASE_PROCESSING_RULES)).thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationSummaryController.class)
            .getApplicationSummary(APPLICATION_ID, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(IndustryCaseProcessingController.class)
            .getIndustryCaseProcessing(APPLICATION_ID, null))));
  }

  @ParameterizedTest
  @MethodSource("getAwaitingPaymentApplicationVersions")
  void getApplicationSummary_whenAwaitingPaymentApplicationAndUserDoesNotHaveSubmitPermission_thenGetSummaryView(ApplicationVersion applicationVersion) throws Exception {
    // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID)).thenReturn(Optional.of(applicationVersion));
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
    when(applicationAccessService.hasApplicationPermission(user, applicationVersion, SUBMIT_FCS_APPLICATIONS)).thenReturn(false);
    when(applicationService.generateApplicationReference(applicationVersion)).thenReturn(DUMMY_APP_REF);

    getApplicationSummaryAndCheckModel(applicationVersion, DUMMY_APP_REF);
  }

  @ParameterizedTest
  @MethodSource("getAwaitingPaymentApplicationVersions")
  void getApplicationSummary_whenAwaitingPaymentApplicationAndUserHasSubmitPermission_thenRedirectToIndustryCaseProcessing(ApplicationVersion applicationVersion) throws Exception {
    // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID)).thenReturn(Optional.of(applicationVersion));
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
    when(applicationAccessService.hasApplicationPermission(user, applicationVersion, SUBMIT_FCS_APPLICATIONS)).thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationSummaryController.class)
            .getApplicationSummary(APPLICATION_ID, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(IndustryCaseProcessingController.class)
            .getIndustryCaseProcessing(APPLICATION_ID, null))));
  }

  @ParameterizedTest
  @MethodSource("getSubmittedApplicationVersions")
  void getApplicationSummary_whenSubmittedAndUserHasEditPermission_thenRedirectToIndustryCaseProcessing(ApplicationVersion applicationVersion) throws Exception {
    // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID)).thenReturn(Optional.of(applicationVersion));
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
    when(applicationAccessService.hasApplicationPermission(user, applicationVersion, CONSULTEE_CASE_PROCESSING_ROLES)).thenReturn(false);
    when(applicationAccessService.hasApplicationPermission(user, applicationVersion, INDUSTRY_CASE_PROCESSING_RULES)).thenReturn(true);
    when(applicationAccessService.hasApplicationPermission(user, applicationVersion, REGULATOR_CASE_PROCESSING_ROLES)).thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationSummaryController.class)
            .getApplicationSummary(APPLICATION_ID, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(IndustryCaseProcessingController.class)
            .getIndustryCaseProcessing(APPLICATION_ID, null))));
  }

  @ParameterizedTest
  @MethodSource("getSubmittedApplicationVersions")
  void getApplicationSummary_whenSubmittedAndUserHasProcessPermission_thenRedirectToApplicationCaseProcessing(ApplicationVersion applicationVersion) throws Exception {
    // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID)).thenReturn(Optional.of(applicationVersion));
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID)).thenReturn(applicationVersion);
    when(applicationAccessService.hasApplicationPermission(user, applicationVersion, INDUSTRY_CASE_PROCESSING_RULES)).thenReturn(true);
    when(applicationAccessService.hasApplicationPermission(user, applicationVersion, REGULATOR_CASE_PROCESSING_ROLES)).thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationSummaryController.class)
            .getApplicationSummary(APPLICATION_ID, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(ApplicationCaseProcessingController.class)
            .caseProcessing(APPLICATION_ID, null, null))));
  }

  @SecurityTest
  void getApplicationSummary_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ApplicationSummaryController.class)
            .getApplicationSummary(APPLICATION_ID, null))))
        .andExpect(redirectionToLoginUrl());
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

  private static Stream<Arguments> getAwaitingPaymentApplicationVersions() {
    return Stream.of(
        Arguments.of(ApplicationTestUtil.getAwaitingPaymentApplicationVersionWithType(ApplicationType.PRODUCTION)),
        Arguments.of(ApplicationTestUtil.getAwaitingPaymentApplicationVersionWithType(ApplicationType.FLARE)),
        Arguments.of(ApplicationTestUtil.getAwaitingPaymentApplicationVersionWithType(ApplicationType.VENT))
    );
  }

  private static Stream<Arguments> getSubmittedApplicationVersions() {
    return Stream.of(
        Arguments.of(ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION)),
        Arguments.of(ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.FLARE)),
        Arguments.of(ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.VENT))
    );
  }
}
