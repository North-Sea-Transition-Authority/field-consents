package uk.co.nstauthority.fieldconsents.authorisation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_ID;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.ORG_GROUP_ID_1;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.ORG_GROUP_ID_2;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit1With2GroupsJson;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit1WithGroupsJson;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit2WithGroupsJsonNoGroups;

import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.AbstractApplicationControllerTest;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.TeamTestUtil;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@ContextConfiguration(classes = ApplicationHandlerInterceptorTest.TestController.class)
class ApplicationHandlerInterceptorTest extends AbstractApplicationControllerTest {

  private static final String ORG_UNIT_LOOKUP_PURPOSE =
      "Lookup organisation unit with groups for application security check";

  private ApplicationVersion applicationVersionInProgress;

  @BeforeEach
  void setUp() {
    applicationVersionInProgress = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
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
        .andExpect(status().isBadRequest())
        .andExpect(status().reason("Received request with no applicationId present"));
  }

  @SecurityTest
  void noApplicationExists() throws Exception {
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.empty());

    mockMvc.perform(
        get(ReverseRouter.route(on(ApplicationHandlerInterceptorTest.TestController.class)
            .noApplicationExists(APPLICATION_ID)))
            .with(user(user)))
        .andExpect(status().isNotFound())
        .andExpect(status().reason("Received request with non-existent application id %s".formatted(APPLICATION_ID)));
  }

  @SecurityTest
  void applicationStatusInvalid() throws Exception {
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersionInProgress));

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
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersionInProgress));

    mockMvc.perform(
            get(ReverseRouter.route(on(ApplicationHandlerInterceptorTest.TestController.class)
                .applicationStatusValid(APPLICATION_ID)))
                .with(user(user)))
        .andExpect(status().isOk());
  }

  @SecurityTest
  void noOrgGroups() throws Exception {
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersionInProgress));

    when(organisationUnitService.getOrganisationUnitWithGroupsById(
        applicationVersionInProgress.getPrimaryOperatorOuId(), ORG_UNIT_LOOKUP_PURPOSE))
        .thenReturn(orgUnit2WithGroupsJsonNoGroups);

    mockMvc.perform(
            get(ReverseRouter.route(on(ApplicationHandlerInterceptorTest.TestController.class)
                .noOrgGroups(APPLICATION_ID)))
                .with(user(user)))
        .andExpect(status().isForbidden())
        .andExpect(status().reason(
            "No organisation groups found for organisation unit id %s. Application id %s"
                .formatted(applicationVersionInProgress.getPrimaryOperatorOuId(), APPLICATION_ID)
        ));
  }

  @SecurityTest
  void noTeamForOrgGroup() throws Exception {
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersionInProgress));

    when(organisationUnitService.getOrganisationUnitWithGroupsById(
        applicationVersionInProgress.getPrimaryOperatorOuId(), ORG_UNIT_LOOKUP_PURPOSE))
        .thenReturn(orgUnit1WithGroupsJson);

    when(teamService.getTeamByOrganisationGroupId(ORG_GROUP_ID_1))
        .thenReturn(Optional.empty());

    mockMvc.perform(
            get(ReverseRouter.route(on(ApplicationHandlerInterceptorTest.TestController.class)
                .noTeamForOrgGroup(APPLICATION_ID)))
                .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void userDoesntHavePermission() throws Exception {
    var team = TeamTestUtil.Builder().build();
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersionInProgress));

    when(organisationUnitService.getOrganisationUnitWithGroupsById(
        applicationVersionInProgress.getPrimaryOperatorOuId(), ORG_UNIT_LOOKUP_PURPOSE))
        .thenReturn(orgUnit1WithGroupsJson);

    when(teamService.getTeamByOrganisationGroupId(ORG_GROUP_ID_1))
        .thenReturn(Optional.of(team));

    when(permissionService.hasPermissionForTeam(team.toTeamId(), user, Set.of(RolePermission.VIEW_FCS_APPLICATIONS)))
        .thenReturn(false);

    mockMvc.perform(
            get(ReverseRouter.route(on(ApplicationHandlerInterceptorTest.TestController.class)
                .userDoesntHavePermission(APPLICATION_ID)))
                .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void userHasPermission() throws Exception {
    var team = TeamTestUtil.Builder().build();
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersionInProgress));

    when(organisationUnitService.getOrganisationUnitWithGroupsById(
        applicationVersionInProgress.getPrimaryOperatorOuId(), ORG_UNIT_LOOKUP_PURPOSE))
        .thenReturn(orgUnit1WithGroupsJson);

    when(teamService.getTeamByOrganisationGroupId(ORG_GROUP_ID_1))
        .thenReturn(Optional.of(team));

    when(permissionService.hasPermissionForTeam(team.toTeamId(), user, Set.of(RolePermission.VIEW_FCS_APPLICATIONS)))
        .thenReturn(true);

    mockMvc.perform(
            get(ReverseRouter.route(on(ApplicationHandlerInterceptorTest.TestController.class)
                .userHasPermission(APPLICATION_ID)))
                .with(user(user)))
        .andExpect(status().isOk());
  }

  @SecurityTest
  void userHasPermissionViaSecondOrgGroup() throws Exception {
    var team = TeamTestUtil.Builder().build();
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersionInProgress));

    when(organisationUnitService.getOrganisationUnitWithGroupsById(
        applicationVersionInProgress.getPrimaryOperatorOuId(), ORG_UNIT_LOOKUP_PURPOSE))
        .thenReturn(orgUnit1With2GroupsJson);

    when(teamService.getTeamByOrganisationGroupId(ORG_GROUP_ID_1))
        .thenReturn(Optional.empty());

    when(teamService.getTeamByOrganisationGroupId(ORG_GROUP_ID_2))
        .thenReturn(Optional.of(team));

    when(permissionService.hasPermissionForTeam(team.toTeamId(), user, Set.of(RolePermission.VIEW_FCS_APPLICATIONS)))
        .thenReturn(true);

    mockMvc.perform(
            get(ReverseRouter.route(on(ApplicationHandlerInterceptorTest.TestController.class)
                .userHasPermission(APPLICATION_ID)))
                .with(user(user)))
        .andExpect(status().isOk());
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
    @HasApplicationPermission(permissions = RolePermission.VIEW_FCS_APPLICATIONS)
    public ModelAndView noOrgGroups(@PathVariable Integer applicationId) {
      return new ModelAndView(VIEW_NAME);
    }

    @GetMapping("/no-team-for-org-group/{applicationId}")
    @HasApplicationStatus(statuses = ApplicationVersionStatus.IN_PROGRESS)
    @HasApplicationPermission(permissions = RolePermission.VIEW_FCS_APPLICATIONS)
    public ModelAndView noTeamForOrgGroup(@PathVariable Integer applicationId) {
      return new ModelAndView(VIEW_NAME);
    }

    @GetMapping("/user-doesnt-have-permission/{applicationId}")
    @HasApplicationStatus(statuses = ApplicationVersionStatus.IN_PROGRESS)
    @HasApplicationPermission(permissions = RolePermission.VIEW_FCS_APPLICATIONS)
    public ModelAndView userDoesntHavePermission(@PathVariable Integer applicationId) {
      return new ModelAndView(VIEW_NAME);
    }

    @GetMapping("/user-has-permission/{applicationId}")
    @HasApplicationStatus(statuses = ApplicationVersionStatus.IN_PROGRESS)
    @HasApplicationPermission(permissions = RolePermission.VIEW_FCS_APPLICATIONS)
    public ModelAndView userHasPermission(@PathVariable Integer applicationId) {
      return new ModelAndView(VIEW_NAME);
    }
  }
}