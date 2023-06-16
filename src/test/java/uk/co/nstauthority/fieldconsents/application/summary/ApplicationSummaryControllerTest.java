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
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.ASSIGN_FCS_APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.EDIT_FCS_APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.PROCESS_FCS_APPLICATIONS;
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
import uk.co.nstauthority.fieldconsents.application.caseprocessing.IndustryCaseProcessingController;
import uk.co.nstauthority.fieldconsents.application.submission.ApplicationSubmissionController;
import uk.co.nstauthority.fieldconsents.application.submission.ApplicationSubmissionService;
import uk.co.nstauthority.fieldconsents.application.tasklist.shared.ApplicationTaskListController;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.fieldconsents.workarea.WorkAreaController;

@ContextConfiguration(classes = ApplicationSummaryController.class)
class ApplicationSummaryControllerTest extends AbstractApplicationControllerTest {

  private static final String DUMMY_APP_REF = "DUMMY_APP_REF";

  @MockBean
  private ApplicationSummaryService applicationSummaryService;

  @MockBean
  private ApplicationSubmissionService applicationSubmissionService;

  @MockBean
  private ApplicationService applicationService;

  @ParameterizedTest
  @MethodSource("getInProgressApplicationVersions")
  void getReviewAndSubmit_whenInProgressAndUserHasSubmitPermission(ApplicationVersion applicationVersion) throws Exception {
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);
    when(applicationSummaryService.getSummarySections(applicationVersion))
        .thenReturn(Collections.emptyList());
    when(applicationSubmissionService.isSubmittable(applicationVersion)).thenReturn(false);
    when(applicationAccessService.hasApplicationPermission(
        user, applicationVersion, RolePermission.SUBMIT_FCS_APPLICATIONS
    )).thenReturn(true);

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(ApplicationSummaryController.class)
            .getReviewAndSubmit(APPLICATION_ID, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/reviewAndSubmit"))
        .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertThat(model)
        .containsEntry("pageTitle", "Check your answers before submitting")
        .containsEntry("accordionId", applicationVersion.getId())
        .containsKey("summarySections")
        .containsKey("wideSummaryDisplay")
        .containsEntry("submitUrl", ReverseRouter.route(on(ApplicationSubmissionController.class)
            .submitApplication(APPLICATION_ID, null)))
        .containsEntry("backLinkUrl", ReverseRouter.route(on(ApplicationTaskListController.class)
            .getTaskList(APPLICATION_ID)))
        .containsEntry("isSubmittable", false)
        .containsEntry("userHasSubmitPermission", true);
  }

  @SecurityTest
  void getReviewAndSubmit_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ApplicationSummaryController.class)
            .getReviewAndSubmit(APPLICATION_ID, null))))
        .andExpect(redirectionToLoginUrl());
  }

  @ParameterizedTest
  @MethodSource("getSubmittedApplicationVersions")
  void getApplicationSummary_whenSubmittedApplicationAndUserHasNoProcessPermission_thenGetSummaryView(ApplicationVersion applicationVersion) throws Exception {
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);
    when(applicationAccessService.hasApplicationPermission(
        user, applicationVersion, EDIT_FCS_APPLICATIONS
    )).thenReturn(false);
    when(applicationAccessService.hasApplicationPermission(
        user, applicationVersion, PROCESS_FCS_APPLICATIONS, ASSIGN_FCS_APPLICATIONS
    )).thenReturn(false);

    when(applicationSummaryService.getSummarySections(applicationVersion))
        .thenReturn(Collections.emptyList());

    when(applicationService.generateApplicationReference(applicationVersion))
        .thenReturn(DUMMY_APP_REF);

    getApplicationSummaryAndCheckModel(applicationVersion, DUMMY_APP_REF);
  }

  @ParameterizedTest
  @MethodSource("getInProgressApplicationVersions")
  void getApplicationSummary_whenInProgressAndUserHasNoEditPermission_thenGetSummaryView(ApplicationVersion applicationVersion) throws Exception {
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);
    when(applicationAccessService.hasApplicationPermission(
        user, applicationVersion, EDIT_FCS_APPLICATIONS
    )).thenReturn(false);
    when(applicationAccessService.hasApplicationPermission(
        user, applicationVersion, PROCESS_FCS_APPLICATIONS, ASSIGN_FCS_APPLICATIONS
    )).thenReturn(true);

    getApplicationSummaryAndCheckModel(applicationVersion, "Application summary");
  }

  private void getApplicationSummaryAndCheckModel(ApplicationVersion applicationVersion, String expectedPageTitle) throws Exception {
    doCallRealMethod().when(applicationSummaryService).getApplicationSummaryModelAndView(any(), any(), any(), any());
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
        .containsKey("wideSummaryDisplay")
        .containsEntry("backLinkUrl", ReverseRouter.route(on(WorkAreaController.class)
            .getWorkArea(null, null)));

  }

  @ParameterizedTest
  @MethodSource("getInProgressApplicationVersions")
  void getApplicationSummary_whenInProgressApplication_thenRedirect(ApplicationVersion applicationVersion) throws Exception {
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);
    when(applicationAccessService.hasApplicationPermission(
        user, applicationVersion, EDIT_FCS_APPLICATIONS
    )).thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationSummaryController.class)
            .getApplicationSummary(APPLICATION_ID, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().is3xxRedirection());
  }

  @ParameterizedTest
  @MethodSource("getSubmittedApplicationVersions")
  void getApplicationSummary_whenSubmittedAndUserHaEditPermission_thenRedirectToIndustryCaseProcessing(ApplicationVersion applicationVersion) throws Exception {
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);
    when(applicationAccessService.hasApplicationPermission(
        user, applicationVersion, EDIT_FCS_APPLICATIONS
    )).thenReturn(true);
    when(applicationAccessService.hasApplicationPermission(
        user, applicationVersion, PROCESS_FCS_APPLICATIONS, ASSIGN_FCS_APPLICATIONS
    )).thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationSummaryController.class)
            .getApplicationSummary(APPLICATION_ID, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(IndustryCaseProcessingController.class)
            .getIndustryCaseProcessing(APPLICATION_ID, null)))
        );
  }

  @ParameterizedTest
  @MethodSource("getSubmittedApplicationVersions")
  void getApplicationSummary_whenSubmittedAndUserHasProcessPermission_thenRedirectToApplicationCaseProcessing(ApplicationVersion applicationVersion) throws Exception {
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);
    when(applicationAccessService.hasApplicationPermission(
        user, applicationVersion, EDIT_FCS_APPLICATIONS
    )).thenReturn(true);
    when(applicationAccessService.hasApplicationPermission(
        user, applicationVersion, PROCESS_FCS_APPLICATIONS, ASSIGN_FCS_APPLICATIONS
    )).thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationSummaryController.class)
            .getApplicationSummary(APPLICATION_ID, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(ApplicationCaseProcessingController.class)
            .getApplicationCaseProcessing(APPLICATION_ID, null)))
        );
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

  private static Stream<Arguments> getSubmittedApplicationVersions() {
    return Stream.of(
        Arguments.of(ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION)),
        Arguments.of(ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.FLARE)),
        Arguments.of(ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.VENT))
    );
  }
}
