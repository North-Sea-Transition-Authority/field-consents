package uk.co.nstauthority.fieldconsents.teams.permissionmanagement.opred;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.teams.TeamTestUtil.randomInteger;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.TeamId;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberViewService;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberViewTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.TeamListController;

@ContextConfiguration(classes = OpredTeamManagementController.class)
class OpredTeamManagementControllerTest extends AbstractControllerTest {

  @MockBean
  private TeamMemberViewService teamMemberViewService;

  @MockBean
  private OpredTeamService opredTeamService;

  @SecurityTest
  void renderMemberList_whenNotAuthenticated_thenRedirectToLogin() throws Exception {
    var teamId = new TeamId(randomInteger());
    mockMvc.perform(
        get(ReverseRouter.route(on(
            OpredTeamManagementController.class).renderMemberList(teamId))))
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  void renderMemberList_whenMemberOfTeam_thenOk() throws Exception {
    var user = ServiceUserDetailTestUtil.Builder().build();
    var team = TeamTestUtil.Builder().withTeamType(TeamType.OPRED).build();
    var teamId = team.toTeamId();

    when(teamMemberService.isMemberOfTeam(teamId, user)).thenReturn(true);
    when(teamService.getTeam(teamId, OpredTeamManagementController.TEAM_TYPE)).thenReturn(Optional.of(team));

    mockMvc.perform(get(ReverseRouter.route(on(OpredTeamManagementController.class)
            .renderMemberList(teamId)))
            .with(user(user)))
        .andExpect(status().isOk());
  }

  @Test
  void renderMemberList_whenNoTeamFound_thenNotFound() throws Exception {
    var user = ServiceUserDetailTestUtil.Builder().build();
    var teamId = new TeamId(randomInteger());

    when(teamMemberService.isMemberOfTeam(teamId, user)).thenReturn(true);
    when(teamService.getTeam(teamId, OpredTeamManagementController.TEAM_TYPE)).thenReturn(Optional.empty());

    mockMvc.perform(get(ReverseRouter.route(on(OpredTeamManagementController.class)
            .renderMemberList(teamId)))
            .with(user(user)))
        .andExpect(status().isNotFound());
  }

  @Test
  void renderMemberList_whenNotAccessManager_assertModelProperties() throws Exception {
    var user = ServiceUserDetailTestUtil.Builder().build();
    var team = TeamTestUtil.Builder().withTeamType(TeamType.OPRED).build();
    var teamId = team.toTeamId();

    when(teamMemberService.isMemberOfTeam(teamId, user)).thenReturn(true);
    when(teamService.getTeam(teamId, OpredTeamManagementController.TEAM_TYPE)).thenReturn(Optional.of(team));

    var teamMemberView = TeamMemberViewTestUtil.Builder().withRole(OpredTeamRole.RESPONDER).build();

    when(teamMemberViewService.getTeamMemberViewsForTeam(team)).thenReturn(List.of(teamMemberView));

    mockMvc.perform(get(ReverseRouter.route(on(OpredTeamManagementController.class)
            .renderMemberList(teamId)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/permissionmanagement/teamMembersPage"))
        .andExpect(model().attribute("pageTitle", "Manage %s".formatted(team.getDisplayName())))
        .andExpect(model().attribute("teamName", team.getDisplayName()))
        .andExpect(model().attribute("teamRoles", OpredTeamRole.values()))
        .andExpect(model().attributeDoesNotExist("addTeamMemberUrl"));
  }

  @Test
  void renderMemberList_whenAccessManagerAndInMultipleTeams_assertModelProperties() throws Exception {
    var user = ServiceUserDetailTestUtil.Builder().build();
    var team = TeamTestUtil.Builder().withTeamType(TeamType.OPRED).build();
    var teamId = team.toTeamId();

    when(teamMemberService.isMemberOfTeam(teamId, user)).thenReturn(true);
    when(opredTeamService.isAccessManager(teamId, user)).thenReturn(true);
    when(teamService.getTeam(teamId, OpredTeamManagementController.TEAM_TYPE)).thenReturn(Optional.of(team));

    var teamMemberView = TeamMemberViewTestUtil.Builder().build();
    when(teamMemberViewService.getTeamMemberViewsForTeam(team)).thenReturn(List.of(teamMemberView));

    when(teamService.canUserAccessMultipleTeams(user)).thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(OpredTeamManagementController.class)
            .renderMemberList(teamId)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/permissionmanagement/teamMembersPage"))
        .andExpect(model().attribute("pageTitle", "Manage %s".formatted(team.getDisplayName())))
        .andExpect(model().attribute("teamName", team.getDisplayName()))
        .andExpect(model().attribute("teamRoles", OpredTeamRole.values()))
        .andExpect(model().attribute("backLinkUrl", ReverseRouter.route(on(TeamListController.class).resolveTeamListEntryRoute())))
        .andExpect(model().attribute("teamMembers", List.of(teamMemberView)));
  }

}
