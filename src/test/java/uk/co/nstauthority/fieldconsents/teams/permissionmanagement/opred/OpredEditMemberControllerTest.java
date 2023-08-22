package uk.co.nstauthority.fieldconsents.teams.permissionmanagement.opred;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MvcResult;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.Team;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberRoleService;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberViewService;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberViewTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.TeamView;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.TeamMemberRolesForm;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.TeamRole;
import uk.co.nstauthority.fieldconsents.util.enumutil.DisplayableEnumOptionUtil;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = OpredEditMemberController.class)
class OpredEditMemberControllerTest extends AbstractControllerTest {

  @MockBean
  private TeamMemberViewService teamMemberViewService;

  @MockBean
  private TeamMemberRoleService teamMemberRoleService;

  @MockBean
  private OpredTeamMemberEditRolesValidator opredTeamMemberEditRolesValidator;

  private Team opredTeam;
  private TeamView teamView;
  private ServiceUserDetail accessManager;

  @BeforeEach
  void setUp() {
    opredTeam = TeamTestUtil.Builder().withTeamType(TeamType.OPRED).build();
    teamView = TeamTestUtil.createTeamView(opredTeam);
    accessManager = ServiceUserDetailTestUtil.Builder().withWuaId(new Random().nextLong()).build();
  }

  @SecurityTest
  void renderEditMember_whenNotAuthorised_thenRedirectedToLogin() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(OpredEditMemberController.class)
            .renderEditMember(teamView.teamId(), new WebUserAccountId(accessManager.wuaId())))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void renderEditMember_whenAccessManager_thenOk() throws Exception {
    makeValidRenderEditMemberRequest(Set.of(OpredTeamRole.ACCESS_MANAGER));
  }

  @Test
  void renderEditMember_whenAccessManager_andOk_thenAssertModelProperties() throws Exception {
    Set<TeamRole> userRoles = Set.of(OpredTeamRole.ACCESS_MANAGER);

    var model = makeValidRenderEditMemberRequest(userRoles)
        .getModelAndView()
        .getModel();

    assertThat(model)
        .isNotNull()
        .containsKey("pageTitle")
        .containsEntry("roles", DisplayableEnumOptionUtil.getDisplayableOptionsWithDescription(OpredTeamRole.class))
        .containsEntry("backLinkUrl", ReverseRouter.route(on(OpredTeamManagementController.class).renderMemberList(teamView.teamId())));

    var expectedRoles = userRoles.stream().map(TeamRole::name).collect(Collectors.toSet());
    assertThat((TeamMemberRolesForm) model.get("form")).extracting("roles").isEqualTo(expectedRoles);
  }

  private MvcResult makeValidRenderEditMemberRequest(Set<TeamRole> teamMemberRoles) throws Exception {
    when(permissionService.hasPermission(accessManager, Set.of(RolePermission.GRANT_ROLES))).thenReturn(true);
    when(teamMemberService.isMemberOfTeamWithAnyRoleOf(teamView.teamId(), accessManager, Set.of(OpredTeamRole.ACCESS_MANAGER.name()))).thenReturn(true);
    when(teamService.getTeam(teamView.teamId(), OpredEditMemberController.TEAM_TYPE)).thenReturn(Optional.of(opredTeam));

    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamType(TeamType.OPRED)
        .withTeamId(teamView.teamId())
        .withWebUserAccountId(accessManager.wuaId())
        .withRoles(teamMemberRoles)
        .build();

    var teamMemberView = TeamMemberViewTestUtil.Builder()
        .withRoles(teamMemberRoles)
        .withWebUserAccountId(teamMember.wuaId())
        .build();

    when(teamMemberService.getUserAsTeamMembers(accessManager)).thenReturn(List.of(teamMember));
    when(teamMemberService.isMemberOfTeam(teamView.teamId(), accessManager)).thenReturn(true);
    when(teamMemberService.getTeamMember(opredTeam, teamMember.wuaId())).thenReturn(Optional.of(teamMember));
    when(teamMemberViewService.getTeamMemberView(teamMember)).thenReturn(Optional.of(teamMemberView));

    return mockMvc.perform(get(ReverseRouter.route(on(OpredEditMemberController.class)
            .renderEditMember(teamView.teamId(), teamMember.wuaId())))
            .with(user(accessManager)))
        .andExpect(status().isOk())
        .andReturn();
  }

  @Test
  void renderEditMember_whenAccessManagerAndTeamMemberDoesntExist_thenClientError() throws Exception {
    Set<TeamRole> userRoles = Set.of(OpredTeamRole.ACCESS_MANAGER);

    when(permissionService.hasPermission(accessManager, Set.of(RolePermission.GRANT_ROLES))).thenReturn(true);
    when(teamMemberService.isMemberOfTeamWithAnyRoleOf(teamView.teamId(), accessManager, Set.of(OpredTeamRole.ACCESS_MANAGER.name()))).thenReturn(true);
    when(teamService.getTeam(teamView.teamId(), OpredEditMemberController.TEAM_TYPE)).thenReturn(Optional.of(opredTeam));

    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamType(TeamType.OPRED)
        .withTeamId(teamView.teamId())
        .withWebUserAccountId(accessManager.wuaId())
        .withRoles(userRoles)
        .build();

    when(teamMemberService.getUserAsTeamMembers(accessManager)).thenReturn(List.of(teamMember));
    when(teamMemberService.isMemberOfTeam(teamView.teamId(), accessManager)).thenReturn(true);
    when(teamMemberService.getTeamMember(opredTeam, teamMember.wuaId())).thenReturn(Optional.empty());

    mockMvc.perform(get(ReverseRouter.route(on(OpredEditMemberController.class)
            .renderEditMember(teamView.teamId(), teamMember.wuaId())))
            .with(user(accessManager)))
        .andExpect(status().is4xxClientError());
  }

  @Test
  void renderEditMember_whenAccessManagerAndTeamMemberViewDoesntExist_thenClientError() throws Exception {
    Set<TeamRole> userRoles = Set.of(OpredTeamRole.ACCESS_MANAGER);

    when(permissionService.hasPermission(accessManager, Set.of(RolePermission.GRANT_ROLES))).thenReturn(true);
    when(teamMemberService.isMemberOfTeamWithAnyRoleOf(teamView.teamId(), accessManager, Set.of(OpredTeamRole.ACCESS_MANAGER.name()))).thenReturn(true);
    when(teamService.getTeam(teamView.teamId(), OpredEditMemberController.TEAM_TYPE)).thenReturn(Optional.of(opredTeam));

    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamType(TeamType.OPRED)
        .withTeamId(teamView.teamId())
        .withWebUserAccountId(accessManager.wuaId())
        .withRoles(userRoles)
        .build();

    when(teamMemberService.getUserAsTeamMembers(accessManager)).thenReturn(List.of(teamMember));
    when(teamMemberService.isMemberOfTeam(teamView.teamId(), accessManager)).thenReturn(true);
    when(teamMemberService.getTeamMember(opredTeam, teamMember.wuaId())).thenReturn(Optional.of(teamMember));
    when(teamMemberViewService.getTeamMemberView(teamMember)).thenReturn(Optional.empty());

    mockMvc.perform(get(ReverseRouter.route(on(OpredEditMemberController.class)
            .renderEditMember(teamView.teamId(), teamMember.wuaId())))
            .with(user(accessManager)))
        .andExpect(status().is4xxClientError());
  }

  @Test
  void editMember_whenNotAuthorized_thenIsRedirectedToLoginUrl() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(OpredEditMemberController.class)
            .editMember(teamView.teamId(), new WebUserAccountId(accessManager.wuaId()), null, null)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }
}
