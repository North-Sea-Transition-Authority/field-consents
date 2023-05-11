package uk.co.nstauthority.fieldconsents.application.summary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_ID;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
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
  void getSubmitSummary_whenInProgressAndUserHasSubmitPermission(ApplicationVersion applicationVersion) throws Exception {
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
            .submitApplication(APPLICATION_ID)))
        .containsEntry("backLinkUrl", ReverseRouter.route(on(ApplicationTaskListController.class)
            .getTaskList(APPLICATION_ID)))
        .containsEntry("isSubmittable", false)
        .containsEntry("userHasSubmitPermission", true);
  }

  @SecurityTest
  void getSubmitSummary_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ApplicationSummaryController.class)
            .getReviewAndSubmit(APPLICATION_ID, null))))
        .andExpect(redirectionToLoginUrl());
  }

  @ParameterizedTest
  @MethodSource("getSubmittedApplicationVersions")
  void getSummaryOrRedirect_whenSubmittedApplication_thenGetSummaryView(ApplicationVersion applicationVersion) throws Exception {
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);
    when(applicationAccessService.hasApplicationPermission(
        user, applicationVersion, RolePermission.EDIT_FCS_APPLICATIONS
    )).thenReturn(true);

    when(applicationSummaryService.getSummarySections(applicationVersion))
        .thenReturn(Collections.emptyList());

    when(applicationService.generateApplicationReference(applicationVersion))
        .thenReturn(DUMMY_APP_REF);

    getSummaryOrRedirectAndCheckModel(applicationVersion, DUMMY_APP_REF);
  }

  @ParameterizedTest
  @MethodSource("getInProgressApplicationVersions")
  void getSummaryOrRedirect_whenInProgressAndUserHasNoEditPermission_thenGetSummaryView(ApplicationVersion applicationVersion) throws Exception {
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);
    when(applicationAccessService.hasApplicationPermission(
        user, applicationVersion, RolePermission.EDIT_FCS_APPLICATIONS
    )).thenReturn(false);

    getSummaryOrRedirectAndCheckModel(applicationVersion, "Application summary");
  }

  private void getSummaryOrRedirectAndCheckModel(ApplicationVersion applicationVersion, String expectedPageTitle) throws Exception {
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
            .getWorkArea(null)));

  }

  @ParameterizedTest
  @MethodSource("getInProgressApplicationVersions")
  void getSummaryOrRedirect_whenInProgressApplication_thenRedirect(ApplicationVersion applicationVersion) throws Exception {
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);
    when(applicationAccessService.hasApplicationPermission(
        user, applicationVersion, RolePermission.EDIT_FCS_APPLICATIONS
    )).thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationSummaryController.class)
            .getApplicationSummary(APPLICATION_ID, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().is3xxRedirection());
  }

  @SecurityTest
  void getSummaryOrRedirect_noUser() throws Exception {
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
