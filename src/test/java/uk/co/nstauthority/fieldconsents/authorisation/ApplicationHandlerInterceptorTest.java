package uk.co.nstauthority.fieldconsents.authorisation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_ID;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CASE_OFFICER_ASSIGN_OWNERSHIP;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CASE_OFFICER_RELEASE_OWNERSHIP;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CASE_OFFICER_TAKE_OWNERSHIP;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;

import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.AbstractApplicationControllerTest;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionNotFoundException;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.authorisation.role.grouped.UserCanViewConsent;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.Role;

@ContextConfiguration(classes = ApplicationHandlerInterceptorTest.TestController.class)
class ApplicationHandlerInterceptorTest extends AbstractApplicationControllerTest {

  private ApplicationVersion applicationVersionInProgress;

  private ApplicationVersion applicationVersionSubmitted;

  @BeforeEach
  void setUp() {
    applicationVersionInProgress = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
    applicationVersionSubmitted = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.FLARE);
  }

  @SecurityTest
  void noSecurityAnnotation() throws Exception {
    assertThatThrownBy(() ->
        mockMvc.perform(get(ReverseRouter.route(on(TestController.class).noSecurity()))
            .with(user(user))
        ))
        .hasMessageEndingWith("Controllers must be annotated with @Security");
  }

  @SecurityTest
  void noApplicationIdOnEndpoint() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(on(ApplicationHandlerInterceptorTest.TestController.class)
            .noApplicationIdOnEndpoint()))
            .with(user(user))
        )
        .andExpect(status().isNotFound());
  }

  @SecurityTest
  void noApplicationExists() throws Exception {
    // this is called in ApplicationHandlerInterceptor
    doThrow(new ApplicationVersionNotFoundException(":("))
        .when(applicationVersionService)
        .getLatestApplicationVersionByApplicationId(APPLICATION_ID);

    mockMvc.perform(
        get(ReverseRouter.route(on(ApplicationHandlerInterceptorTest.TestController.class)
            .noApplicationExists(APPLICATION_ID)))
            .with(user(user)))
        .andExpect(status().isNotFound());
  }

  @SecurityTest
  void applicationStatusInvalid() throws Exception {
    // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersionInProgress);

    mockMvc.perform(
            get(ReverseRouter.route(on(ApplicationHandlerInterceptorTest.TestController.class)
                .applicationStatusInvalid(APPLICATION_ID)))
                .with(user(user)))
        .andExpect(status().isForbidden())
        .andExpect(status().reason(
            "User %s attempted to access application version %s with status %s. Expected status(s) %s"
                .formatted(
                    user.wuaId(),
                    applicationVersionInProgress.getId(),
                    applicationVersionInProgress.getStatus(),
                    ApplicationVersionStatus.SUBMITTED
                )
        ));
  }

  @SecurityTest
  void applicationStatusValid() throws Exception {
    // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersionInProgress);

    mockMvc.perform(
            get(ReverseRouter.route(on(ApplicationHandlerInterceptorTest.TestController.class)
                .applicationStatusValid(APPLICATION_ID)))
                .with(user(user)))
        .andExpect(status().isOk());
  }

  @SecurityTest
  void userDoesntHavePermission() throws Exception {
    // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersionInProgress);

    mockMvc.perform(
            get(ReverseRouter.route(on(ApplicationHandlerInterceptorTest.TestController.class)
                .userDoesntHaveRole(APPLICATION_ID)))
                .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void userHasRole_regulator() throws Exception {
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersionInProgress));

    when(fieldConsentsAccessService.userHasAnyRegulatorRole(user, Set.of(Role.CASE_OFFICER)))
        .thenReturn(true);

    mockMvc.perform(
            get(ReverseRouter.route(on(ApplicationHandlerInterceptorTest.TestController.class)
                .userHasRole(APPLICATION_ID)))
                .with(user(user)))
        .andExpect(status().isOk());
  }

  @SecurityTest
  void userHasRole_consultee() throws Exception {
    // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersionInProgress);

    when(fieldConsentsAccessService.userHasAnyIndustryRole(user, applicationVersionInProgress, Set.of(Role.CREATOR)))
        .thenReturn(true);

    mockMvc.perform(
            get(ReverseRouter.route(on(ApplicationHandlerInterceptorTest.TestController.class)
                .userHasRole(APPLICATION_ID)))
                .with(user(user)))
        .andExpect(status().isOk());
  }

  @SecurityTest
  void userHasRole_industry() throws Exception {
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersionInProgress);

    when(fieldConsentsAccessService.userHasAnyIndustryRole(user, applicationVersionInProgress, Set.of(Role.CREATOR)))
        .thenReturn(true);

    mockMvc.perform(
            get(ReverseRouter.route(on(ApplicationHandlerInterceptorTest.TestController.class)
                .userHasRole(APPLICATION_ID)))
                .with(user(user)))
        .andExpect(status().isOk());
  }

  @SecurityTest
  void actionEndPointForbidden() throws Exception {
    // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersionSubmitted);

    when(caseProcessingActionService.userHasAnyAction(applicationVersionSubmitted, user, CASE_OFFICER_RELEASE_OWNERSHIP))
        .thenReturn(false);

    mockMvc.perform(
            post(ReverseRouter.route(on(ApplicationHandlerInterceptorTest.TestController.class)
                .actionEndPointForbidden(APPLICATION_ID)))
                .with(user(user))
                .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void actionEndPointAllowed() throws Exception {
    // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersionSubmitted);

    when(caseProcessingActionService.userHasAnyAction(
        applicationVersionSubmitted,
        user,
        CASE_OFFICER_TAKE_OWNERSHIP
    ))
        .thenReturn(true);

    mockMvc.perform(
            post(ReverseRouter.route(on(ApplicationHandlerInterceptorTest.TestController.class)
                .actionEndPointAllowed(APPLICATION_ID)))
                .with(user(user))
                .with(csrf()))
        .andExpect(status().isOk());
  }

  @SecurityTest
  void getAssigmentEndpointAllowed() throws Exception {
    // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersionSubmitted);

    when(caseProcessingActionService.userHasAnyAction(
        applicationVersionSubmitted,
        user,
        CASE_OFFICER_ASSIGN_OWNERSHIP
    ))
        .thenReturn(true);

    mockMvc.perform(
            get(ReverseRouter.route(on(ApplicationHandlerInterceptorTest.TestController.class)
                .getAssignmentEndpoint(APPLICATION_ID)))
                .with(user(user)))
        .andExpect(status().isOk());
  }

  @SecurityTest
  void getAssigmentEndpointForbidden() throws Exception {
    // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersionSubmitted);

    mockMvc.perform(
            get(ReverseRouter.route(on(ApplicationHandlerInterceptorTest.TestController.class)
                .getAssignmentEndpoint(APPLICATION_ID)))
                .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void postAssigmentEndpointAllowed() throws Exception {
    // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersionSubmitted);

    when(caseProcessingActionService.userHasAnyAction(
        applicationVersionSubmitted,
        user,
        CASE_OFFICER_ASSIGN_OWNERSHIP
    ))
        .thenReturn(true);

    mockMvc.perform(
            post(ReverseRouter.route(on(ApplicationHandlerInterceptorTest.TestController.class)
                .postAssignmentEndpoint(APPLICATION_ID)))
                .with(user(user))
                .with(csrf()))
        .andExpect(status().isOk());
  }

  @SecurityTest
  void postAssigmentEndpointForbidden() throws Exception {
    // this is called in ApplicationHandlerInterceptor
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .thenReturn(applicationVersionSubmitted);

    mockMvc.perform(
            post(ReverseRouter.route(on(ApplicationHandlerInterceptorTest.TestController.class)
                .postAssignmentEndpoint(APPLICATION_ID)))
                .with(user(user))
                .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @RequestMapping("/applications/test")
  @Controller
  static class TestController {

    public TestController() {
    }

    private static final String VIEW_NAME = "test-view";

    @GetMapping("/no-security")
    public ModelAndView noSecurity() {
      return new ModelAndView(VIEW_NAME);
    }

    @GetMapping("/no-application-id")
    @HasApplicationStatus(statuses = ApplicationVersionStatus.IN_PROGRESS)
    public ModelAndView noApplicationIdOnEndpoint() {
      return new ModelAndView(VIEW_NAME);
    }

    @GetMapping("/no-application-exists/{applicationId}")
    @HasApplicationStatus(statuses = ApplicationVersionStatus.IN_PROGRESS)
    public ModelAndView noApplicationExists(@PathVariable Integer applicationId) {
      return new ModelAndView(VIEW_NAME);
    }

    @GetMapping("/application-status-invalid/{applicationId}")
    @HasApplicationStatus(statuses = ApplicationVersionStatus.SUBMITTED)
    public ModelAndView applicationStatusInvalid(@PathVariable Integer applicationId) {
      return new ModelAndView(VIEW_NAME);
    }

    @GetMapping("/application-status-valid/{applicationId}")
    @HasApplicationStatus(statuses = ApplicationVersionStatus.IN_PROGRESS)
    public ModelAndView applicationStatusValid(@PathVariable Integer applicationId) {
      return new ModelAndView(VIEW_NAME);
    }

    @GetMapping("/no-org-groups/{applicationId}")
    @HasApplicationStatus(statuses = ApplicationVersionStatus.IN_PROGRESS)
    public ModelAndView noOrgGroups(@PathVariable Integer applicationId) {
      return new ModelAndView(VIEW_NAME);
    }

    @GetMapping("/no-team-for-org-group/{applicationId}")
    @HasApplicationStatus(statuses = ApplicationVersionStatus.IN_PROGRESS)
    public ModelAndView noTeamForOrgGroup(@PathVariable Integer applicationId) {
      return new ModelAndView(VIEW_NAME);
    }

    @GetMapping("/user-doesnt-have-permission/{applicationId}")
    @HasApplicationStatus(statuses = ApplicationVersionStatus.IN_PROGRESS)
    @UserCanViewConsent
    public ModelAndView userDoesntHaveRole(@PathVariable Integer applicationId) {
      return new ModelAndView(VIEW_NAME);
    }

    @GetMapping("/user-has-role/{applicationId}")
    @HasApplicationOrRegulatorRole(
        regulatorRoles = Role.CASE_OFFICER,
        industryRoles = Role.CREATOR
    )
    public ModelAndView userHasRole(@PathVariable Integer applicationId) {
      return new ModelAndView(VIEW_NAME);
    }

    @PostMapping("/action-end-point-forbidden/{applicationId}")
    @ActionEndPoint(CASE_OFFICER_RELEASE_OWNERSHIP)
    public ModelAndView actionEndPointForbidden(@PathVariable Integer applicationId) {
      return new ModelAndView(VIEW_NAME);
    }

    @PostMapping("/action-end-point-allowed/{applicationId}")
    @ActionEndPoint(CASE_OFFICER_TAKE_OWNERSHIP)
    public ModelAndView actionEndPointAllowed(@PathVariable Integer applicationId) {
      return new ModelAndView(VIEW_NAME);
    }

    @GetMapping("/get-assignment-endpoint/{applicationId}")
    @ActionEndPoint(CASE_OFFICER_ASSIGN_OWNERSHIP)
    public ModelAndView getAssignmentEndpoint(@PathVariable Integer applicationId) {
      return new ModelAndView(VIEW_NAME);
    }

    @PostMapping("/post-assignment-endpoint/{applicationId}")
    @ActionEndPoint(CASE_OFFICER_ASSIGN_OWNERSHIP)
    public ModelAndView postAssignmentEndpoint(@PathVariable Integer applicationId) {
      return new ModelAndView(VIEW_NAME);
    }
  }
}
