package uk.co.nstauthority.fieldconsents.application.caseprocessing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_ID;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.Collections;
import java.util.List;
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
import uk.co.nstauthority.fieldconsents.application.CaseAssignmentService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionView;
import uk.co.nstauthority.fieldconsents.application.summary.ApplicationSummaryService;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.workarea.WorkAreaController;

@ContextConfiguration(classes = ApplicationCaseProcessingController.class)
class ApplicationCaseProcessingControllerTest extends AbstractApplicationControllerTest {

  private static final String DUMMY_APP_REF = "DUMMY_APP_REF";

  @MockBean
  private ApplicationService applicationService;

  @MockBean
  private ApplicationSummaryService applicationSummaryService;

  @MockBean
  private CaseAssignmentService caseAssignmentService;

  @SecurityTest
  void getApplicationCaseProcessing_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ApplicationCaseProcessingController.class)
            .getApplicationCaseProcessing(APPLICATION_ID, null))))
        .andExpect(redirectionToLoginUrl());
  }

  @ParameterizedTest
  @MethodSource("getSubmittedApplicationVersions")
  void getApplicationCaseProcessing(ApplicationVersion applicationVersion) throws Exception {
    var actionViews =
        List.of(CaseProcessingActionView.from(CaseProcessingActionItem.CASE_OFFICER_TAKE_OWNERSHIP, applicationVersion));
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);
    when(caseProcessingActionService.getUserActionViews(applicationVersion, user))
        .thenReturn(actionViews);
    when(applicationSummaryService.getSummarySections(applicationVersion))
        .thenReturn(Collections.emptyList());
    when(applicationService.generateApplicationReference(applicationVersion))
        .thenReturn(DUMMY_APP_REF);

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(ApplicationCaseProcessingController.class)
            .getApplicationCaseProcessing(APPLICATION_ID, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/application/applicationCaseProcessing"))
        .andReturn().getModelAndView();

    assert modelAndView != null;
    var model = modelAndView.getModel();

    assertThat(model)
        .containsEntry("pageTitle", DUMMY_APP_REF)
        .containsKey("summarySections")
        .containsEntry("accordionId", applicationVersion.getId())
        .containsKey("wideSummaryDisplay")
        .containsEntry("caseProcessingActions", actionViews)
        .containsEntry("backLinkUrl", ReverseRouter.route(on(WorkAreaController.class)
            .getWorkArea(null, null)));
  }

  @ParameterizedTest
  @MethodSource("getSubmittedApplicationVersions")
  void takeOwnershipCaseOfficer(ApplicationVersion applicationVersion) throws Exception {
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);

    mockMvc.perform(
            post(ReverseRouter.route(on(ApplicationCaseProcessingController.class)
                .takeOwnershipCaseOfficer(APPLICATION_ID, null)))
                .with(csrf())
                .with(user(user))
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(ApplicationCaseProcessingController.class)
            .getApplicationCaseProcessing(APPLICATION_ID, null))));

    verify(caseAssignmentService, times(1))
        .assignCaseOfficer(applicationVersion, user);
  }

  @ParameterizedTest
  @MethodSource("getSubmittedApplicationVersions")
  void releaseOwnershipCaseOfficer(ApplicationVersion applicationVersion) throws Exception {
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersion)); // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersion);

    mockMvc.perform(
            post(ReverseRouter.route(on(ApplicationCaseProcessingController.class)
                .releaseOwnershipCaseOfficer(APPLICATION_ID)))
                .with(csrf())
                .with(user(user))
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(ApplicationCaseProcessingController.class)
            .getApplicationCaseProcessing(APPLICATION_ID, null))));

    verify(caseAssignmentService, times(1))
        .unassignCaseOfficer(applicationVersion);
  }

  private static Stream<Arguments> getSubmittedApplicationVersions() {
    return Stream.of(
        Arguments.of(ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION)),
        Arguments.of(ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.FLARE)),
        Arguments.of(ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.VENT))
    );
  }
}
