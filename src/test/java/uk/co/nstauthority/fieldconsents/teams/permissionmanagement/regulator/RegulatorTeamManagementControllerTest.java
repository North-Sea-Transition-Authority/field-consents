package uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator;

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
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.branding.CustomerConfigurationProperties;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.TeamId;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberViewService;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberViewTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamService;
import uk.co.nstauthority.fieldconsents.teams.TeamTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@ContextConfiguration(classes = RegulatorTeamManagementController.class)
class RegulatorTeamManagementControllerTest extends AbstractControllerTest {

  @MockBean
  private TeamMemberViewService teamMemberViewService;

  @MockBean
  private RegulatorTeamService regulatorTeamService;

  @MockBean
  private TeamService teamService;

  @Autowired
  private ApplicationContext applicationContext;

  @SecurityTest
  void renderMemberList_whenNotAuthenticated_thenRedirectionToLoginUrl() throws Exception {
    var teamId = new TeamId(randomInteger());
    mockMvc.perform(
            get(ReverseRouter.route(on(RegulatorTeamManagementController.class).renderMemberList(teamId))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void renderMemberList_whenNotMemberOfTeam_thenForbidden() throws Exception {

    var user = ServiceUserDetailTestUtil.Builder().build();

    var teamId = new TeamId(randomInteger());

    when(teamMemberService.isMemberOfTeam(teamId, user)).thenReturn(false);

    mockMvc.perform(
            get(ReverseRouter.route(on(RegulatorTeamManagementController.class).renderMemberList(teamId)))
                .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void renderMemberList_whenMemberOfTeam_thenOk() throws Exception {

    var user = ServiceUserDetailTestUtil.Builder().build();

    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.REGULATOR)
        .build();

    var teamId = new TeamId(team.getId());

    when(teamMemberService.isMemberOfTeam(teamId, user)).thenReturn(true);
    when(teamService.getTeam(teamId, RegulatorTeamManagementController.TEAM_TYPE)).thenReturn(Optional.of(team));

    mockMvc.perform(
            get(ReverseRouter.route(on(RegulatorTeamManagementController.class).renderMemberList(teamId)))
                .with(user(user)))
        .andExpect(status().isOk());
  }

  @Test
  void renderMemberList_whenNoTeamFound_thenNotFound() throws Exception {

    var user = ServiceUserDetailTestUtil.Builder().build();

    var teamId = new TeamId(randomInteger());

    when(teamMemberService.isMemberOfTeam(teamId, user)).thenReturn(true);
    when(regulatorTeamService.getTeam(teamId)).thenReturn(Optional.empty());

    mockMvc.perform(
            get(ReverseRouter.route(on(RegulatorTeamManagementController.class).renderMemberList(teamId)))
                .with(user(user)))
        .andExpect(status().isNotFound());
  }

  @Test
  void renderMemberList_whenNotAccessManager_assertModelProperties() throws Exception {

    var user = ServiceUserDetailTestUtil.Builder().build();

    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.REGULATOR)
        .build();

    var teamId = new TeamId(team.getId());

    when(teamMemberService.isMemberOfTeam(teamId, user)).thenReturn(true);
    when(teamService.getTeam(teamId, RegulatorTeamManagementController.TEAM_TYPE)).thenReturn(Optional.of(team));

    var teamMemberView = TeamMemberViewTestUtil.Builder()
        .withRole(RegulatorTeamRole.ACCESS_MANAGER)
        .build();

    when(teamMemberViewService.getTeamMemberViewsForTeam(team)).thenReturn(List.of(teamMemberView));

    var mnemonic = applicationContext.getBean(CustomerConfigurationProperties.class).mnemonic();

    mockMvc.perform(
            get(ReverseRouter.route(on(RegulatorTeamManagementController.class).renderMemberList(teamId)))
                .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/permissionmanagement/teamMembersPage"))
        .andExpect(model().attribute("pageTitle", "Manage %s".formatted(mnemonic)))
        .andExpect(model().attribute("teamName", mnemonic))
        .andExpect(model().attribute("teamRoles", RegulatorTeamRole.values()))
        .andExpect(model().attributeDoesNotExist("addTeamMemberUrl"));
  }

  @Test
  void renderMemberList_whenAccessManager_assertModelProperties() throws Exception {

    var user = ServiceUserDetailTestUtil.Builder().build();

    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.REGULATOR)
        .build();

    var teamId = new TeamId(team.getId());

    when(regulatorTeamService.isAccessManager(teamId, user)).thenReturn(true);

    when(teamMemberService.isMemberOfTeam(teamId, user)).thenReturn(true);
    when(teamService.getTeam(teamId, RegulatorTeamManagementController.TEAM_TYPE)).thenReturn(Optional.of(team));

    var teamMemberView = TeamMemberViewTestUtil.Builder()
        .withRoles(Set.of(RegulatorTeamRole.ACCESS_MANAGER))
        .build();
    when(teamMemberViewService.getTeamMemberViewsForTeam(team)).thenReturn(List.of(teamMemberView));

    var canRemoveUsers = true;
    when(teamMemberService.isMemberOfTeamWithAnyRoleOf(teamId, user, Set.of(RegulatorTeamRole.ACCESS_MANAGER.name())))
        .thenReturn(canRemoveUsers);

    var mnemonic = applicationContext.getBean(CustomerConfigurationProperties.class).mnemonic();

    mockMvc.perform(
            get(ReverseRouter.route(on(RegulatorTeamManagementController.class).renderMemberList(teamId)))
                .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/permissionmanagement/teamMembersPage"))
        .andExpect(model().attribute("pageTitle", "Manage %s".formatted(mnemonic)))
        .andExpect(model().attribute("teamName", mnemonic))
        .andExpect(model().attribute("teamRoles", RegulatorTeamRole.values()))
        .andExpect(model().attribute(
            "addTeamMemberUrl",
            ReverseRouter.route(on(RegulatorAddMemberController.class).renderAddTeamMember(teamId))
        ))
        .andExpect(model().attribute("canRemoveUsers", canRemoveUsers))
        .andExpect(model().attribute("teamMembers", List.of(teamMemberView)));
  }
}