package uk.co.nstauthority.fieldconsents.teams.permissionmanagement.opred;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.util.NotificationBannerTestUtil.notificationBanner;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBanner;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBannerType;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.Team;
import uk.co.nstauthority.fieldconsents.teams.TeamId;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberRemovalService;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberViewService;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberViewTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamRole;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {OpredRemoveMemberController.class})
class OpredRemoveMemberControllerTest extends AbstractControllerTest {

  private static final Team TEAM = TeamTestUtil.Builder().withTeamType(TeamType.OPRED).build();
  private static final TeamId TEAM_ID = TEAM.toTeamId();
  
  @MockBean
  private TeamMemberViewService teamMemberViewService;

  @MockBean
  private TeamMemberRemovalService teamMemberRemovalService;

  @SecurityTest
  void renderRemoveMember_whenUserIsNotLoggedIn_thenRedirectedToLogin() throws Exception {
    var webUserAccountIdToAdd = new WebUserAccountId(123);

    when(teamService.getTeam(TEAM_ID, OpredRemoveMemberController.TEAM_TYPE)).thenReturn(Optional.of(TEAM));

    mockMvc.perform(get(ReverseRouter.route(on(OpredRemoveMemberController.class).renderRemoveMember(TEAM_ID, webUserAccountIdToAdd))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void renderRemoveMember_whenAccessManager_thenOk() throws Exception {
    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamId(TEAM.toTeamId())
        .withTeamType(TeamType.OPRED)
        .withRole(OpredTeamRole.ACCESS_MANAGER)
        .build();
    var teamMemberView = TeamMemberViewTestUtil.Builder().build();

    when(teamMemberService.getTeamMember(TEAM, teamMember.wuaId())).thenReturn(Optional.of(teamMember));
    when(teamMemberViewService.getTeamMemberView(teamMember)).thenReturn(Optional.of(teamMemberView));
    when(teamMemberService.getUserAsTeamMembers(user)).thenReturn(List.of(teamMember));
    when(teamMemberService.isMemberOfTeam(TEAM_ID, user)).thenReturn(true);
    when(teamService.getTeam(TEAM_ID, OpredRemoveMemberController.TEAM_TYPE)).thenReturn(Optional.of(TEAM));

    mockMvc.perform(get(ReverseRouter.route(on(OpredRemoveMemberController.class)
            .renderRemoveMember(TEAM_ID, teamMember.wuaId())))
            .with(user(user)))
        .andExpect(status().isOk());
  }

  @SecurityTest
  void renderRemoveMember_whenThirdPartyAccessManager_thenOk() throws Exception {    
    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamType(TeamType.REGULATOR)
        .withRole(RegulatorTeamRole.INDUSTRY_ACCESS_MANAGER)
        .build();
    var teamMemberView = TeamMemberViewTestUtil.Builder().build();

    when(teamMemberService.getTeamMember(TEAM, teamMember.wuaId())).thenReturn(Optional.of(teamMember));
    when(teamMemberViewService.getTeamMemberView(teamMember)).thenReturn(Optional.of(teamMemberView));
    when(teamMemberService.getUserAsTeamMembers(user)).thenReturn(List.of(teamMember));
    when(teamMemberService.isMemberOfTeam(TEAM_ID, user)).thenReturn(false);

    when(teamService.getTeam(TEAM_ID, OpredRemoveMemberController.TEAM_TYPE)).thenReturn(Optional.of(TEAM));

    mockMvc.perform(get(ReverseRouter.route(on(OpredRemoveMemberController.class)
            .renderRemoveMember(TEAM_ID, teamMember.wuaId())))
            .with(user(user)))
        .andExpect(status().isOk());
  }

  @SecurityTest
  void renderRemoveMember_whenNoMatchingPrivs_thenForbidden() throws Exception {
    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamType(TeamType.OPRED)
        .withRole(OpredTeamRole.ALLOCATOR)
        .build();

    when(teamMemberService.getUserAsTeamMembers(user)).thenReturn(List.of(teamMember));
    when(teamMemberService.isMemberOfTeam(TEAM_ID, user)).thenReturn(true);
    when(teamService.getTeam(TEAM_ID, OpredRemoveMemberController.TEAM_TYPE)).thenReturn(Optional.of(TEAM));

    mockMvc.perform(get(ReverseRouter.route(on(OpredRemoveMemberController.class)
            .renderRemoveMember(TEAM_ID, teamMember.wuaId())))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void renderRemoveMember_whenAccessManagerAndUserCanBeRemoved_thenOk() throws Exception {
    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamId(TEAM.toTeamId())
        .withTeamType(TeamType.OPRED)
        .withRole(OpredTeamRole.ACCESS_MANAGER)
        .build();
    var teamMemberView = TeamMemberViewTestUtil.Builder().build();

    when(teamMemberService.getTeamMember(TEAM, teamMember.wuaId())).thenReturn(Optional.of(teamMember));
    when(teamMemberViewService.getTeamMemberView(teamMember)).thenReturn(Optional.of(teamMemberView));
    when(teamMemberService.getUserAsTeamMembers(user)).thenReturn(List.of(teamMember));
    when(teamMemberService.isMemberOfTeam(TEAM_ID, user)).thenReturn(true);
    when(teamService.getTeam(TEAM_ID, OpredRemoveMemberController.TEAM_TYPE)).thenReturn(Optional.of(TEAM));

    var canRemoveTeamMember = true;
    when(teamMemberRemovalService.canRemoveTeamMember(TEAM, teamMember.wuaId(), OpredTeamRole.ACCESS_MANAGER)).thenReturn(canRemoveTeamMember);

    mockMvc.perform(get(ReverseRouter.route(on(
            OpredRemoveMemberController.class)
            .renderRemoveMember(TEAM_ID, teamMember.wuaId())))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(model().attribute("teamName", TEAM.getDisplayName()))
        .andExpect(model().attribute("teamMember", teamMemberView))
        .andExpect(model().attribute("backLinkUrl", ReverseRouter.route(on(OpredTeamManagementController.class).renderMemberList(TEAM_ID))))
        .andExpect(model().attribute("removeUrl", ReverseRouter.route(on(OpredRemoveMemberController.class).removeMember(TEAM_ID, teamMember.wuaId(), null))))
        .andExpect(model().attribute("canRemoveTeamMember", canRemoveTeamMember))
        .andExpect(model().attribute("pageTitle", "Are you sure you want to remove %s from %s?".formatted(teamMemberView.getDisplayName(), TEAM.getDisplayName())))
        .andReturn()
        .getModelAndView();
  }

  @Test
  void renderRemoveMember_whenLastAccessManager_thenOk() throws Exception {
    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamId(TEAM.toTeamId())
        .withTeamType(TeamType.OPRED)
        .withRole(OpredTeamRole.ACCESS_MANAGER)
        .build();
    var teamMemberView = TeamMemberViewTestUtil.Builder().build();

    when(teamMemberService.getTeamMember(TEAM, teamMember.wuaId())).thenReturn(Optional.of(teamMember));
    when(teamMemberViewService.getTeamMemberView(teamMember)).thenReturn(Optional.of(teamMemberView));
    when(teamMemberService.getUserAsTeamMembers(user)).thenReturn(List.of(teamMember));
    when(teamMemberService.isMemberOfTeam(TEAM_ID, user)).thenReturn(true);
    when(teamService.getTeam(TEAM_ID, OpredRemoveMemberController.TEAM_TYPE)).thenReturn(Optional.of(TEAM));

    var canRemoveTeamMember = false;
    when(teamMemberRemovalService.canRemoveTeamMember(TEAM, teamMember.wuaId(), OpredTeamRole.ACCESS_MANAGER)).thenReturn(canRemoveTeamMember);

    mockMvc.perform(get(ReverseRouter.route(on(OpredRemoveMemberController.class)
            .renderRemoveMember(TEAM_ID, teamMember.wuaId())))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(model().attribute("teamName", TEAM.getDisplayName()))
        .andExpect(model().attribute("teamMember", teamMemberView))
        .andExpect(model().attribute("backLinkUrl", ReverseRouter.route(on(OpredTeamManagementController.class).renderMemberList(TEAM_ID))))
        .andExpect(model().attribute("removeUrl", ReverseRouter.route(on(OpredRemoveMemberController.class).removeMember(TEAM_ID, teamMember.wuaId(), null))))
        .andExpect(model().attribute("canRemoveTeamMember", canRemoveTeamMember))
        .andExpect(model().attribute("pageTitle", "You are unable to remove %s from %s".formatted(teamMemberView.getDisplayName(), TEAM.getDisplayName())))
        .andReturn()
        .getModelAndView();
  }

  @SecurityTest
  void removeMember_whenUserIsNotLoggedIn_thenRedirectedToLogin() throws Exception {
    var webUserAccountIdToAdd = new WebUserAccountId(123);

    when(teamService.getTeam(TEAM_ID, OpredRemoveMemberController.TEAM_TYPE)).thenReturn(Optional.of(TEAM));

    mockMvc.perform(post(ReverseRouter.route(on(OpredRemoveMemberController.class)
            .removeMember(TEAM_ID, webUserAccountIdToAdd, null)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void removeMember_whenAccessManager_thenOk() throws Exception {
    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamId(TEAM.toTeamId())
        .withTeamType(TeamType.OPRED)
        .withRole(OpredTeamRole.ACCESS_MANAGER)
        .build();
    var teamMemberView = TeamMemberViewTestUtil.Builder().build();

    when(teamMemberService.getTeamMember(TEAM, teamMember.wuaId())).thenReturn(Optional.of(teamMember));
    when(teamMemberViewService.getTeamMemberView(teamMember)).thenReturn(Optional.of(teamMemberView));
    when(teamMemberService.getUserAsTeamMembers(user)).thenReturn(List.of(teamMember));
    when(teamMemberService.isMemberOfTeam(TEAM_ID, user)).thenReturn(true);
    when(teamService.getTeam(TEAM_ID, OpredRemoveMemberController.TEAM_TYPE)).thenReturn(Optional.of(TEAM));

    var canRemoveTeamMember = false;
    when(teamMemberRemovalService.canRemoveTeamMember(TEAM, teamMember.wuaId(), OpredTeamRole.ACCESS_MANAGER)).thenReturn(canRemoveTeamMember);

    mockMvc.perform(get(ReverseRouter.route(on(OpredRemoveMemberController.class)
            .removeMember(TEAM_ID, teamMember.wuaId(), null)))
            .with(user(user)))
        .andExpect(status().isOk());
  }

  @SecurityTest
  void removeMember_whenThirdPartyAccessManager_thenOk() throws Exception {
    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamType(TeamType.REGULATOR)
        .withRole(RegulatorTeamRole.INDUSTRY_ACCESS_MANAGER)
        .build();
    var teamMemberView = TeamMemberViewTestUtil.Builder().build();

    when(teamMemberService.getTeamMember(TEAM, teamMember.wuaId())).thenReturn(Optional.of(teamMember));
    when(teamMemberViewService.getTeamMemberView(teamMember)).thenReturn(Optional.of(teamMemberView));
    when(teamMemberService.getUserAsTeamMembers(user)).thenReturn(List.of(teamMember));
    when(teamMemberService.isMemberOfTeam(TEAM_ID, user)).thenReturn(true);
    when(teamService.getTeam(TEAM_ID, OpredRemoveMemberController.TEAM_TYPE)).thenReturn(Optional.of(TEAM));

    var canRemoveTeamMember = false;
    when(teamMemberRemovalService.canRemoveTeamMember(TEAM, teamMember.wuaId(), OpredTeamRole.ACCESS_MANAGER)).thenReturn(canRemoveTeamMember);

    mockMvc.perform(post(ReverseRouter.route(on(OpredRemoveMemberController.class)
            .removeMember(TEAM_ID, teamMember.wuaId(), null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk());
  }

  @SecurityTest
  void removeMember_whenNoMatchingPrivs_thenForbidden() throws Exception {
    var user = ServiceUserDetailTestUtil.Builder().build();
    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamType(TeamType.OPRED)
        .withRole(OpredTeamRole.ALLOCATOR)
        .build();

    when(teamMemberService.getUserAsTeamMembers(user)).thenReturn(List.of(teamMember));
    when(teamMemberService.isMemberOfTeam(TEAM_ID, user)).thenReturn(true);
    when(teamService.getTeam(TEAM_ID, OpredRemoveMemberController.TEAM_TYPE)).thenReturn(Optional.of(TEAM));

    mockMvc.perform(post(ReverseRouter.route(on(OpredRemoveMemberController.class)
            .removeMember(TEAM_ID, teamMember.wuaId(), null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void removeMember_whenNoTeamFound_thenNotFound() throws Exception {
    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamType(TeamType.OPRED)
        .withRole(OpredTeamRole.ALLOCATOR)
        .build();

    when(teamMemberService.getUserAsTeamMembers(user)).thenReturn(List.of(teamMember));
    when(teamMemberService.isMemberOfTeam(TEAM_ID, user)).thenReturn(false);
    when(permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_INDUSTRY_TEAMS))).thenReturn(true);
    when(teamService.getTeam(TEAM_ID, OpredRemoveMemberController.TEAM_TYPE)).thenReturn(Optional.empty());

    mockMvc.perform(post(ReverseRouter.route(on(OpredRemoveMemberController.class)
            .removeMember(TEAM_ID, teamMember.wuaId(), null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @Test
  void removeMember_whenLoggedIn_verifyCalls() throws Exception {
    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamId(TEAM.toTeamId())
        .withTeamType(TeamType.OPRED)
        .withRole(OpredTeamRole.ACCESS_MANAGER)
        .build();
    var teamMemberView = TeamMemberViewTestUtil.Builder().build();

    when(teamMemberService.getTeamMember(TEAM, teamMember.wuaId())).thenReturn(Optional.of(teamMember));
    when(teamMemberViewService.getTeamMemberView(teamMember)).thenReturn(Optional.of(teamMemberView));
    when(teamMemberService.getUserAsTeamMembers(user)).thenReturn(List.of(teamMember));
    when(teamMemberService.isMemberOfTeam(TEAM_ID, user)).thenReturn(true);
    when(teamService.getTeam(TEAM_ID, OpredRemoveMemberController.TEAM_TYPE)).thenReturn(Optional.of(TEAM));

    var canRemoveTeamMember = true;
    when(teamMemberRemovalService.canRemoveTeamMember(TEAM, teamMember.wuaId(), OpredTeamRole.ACCESS_MANAGER)).thenReturn(canRemoveTeamMember);

    var expectedNotificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeadingContent("%s has been removed from the team".formatted(teamMemberView.getDisplayName()))
        .build();

    mockMvc.perform(post(ReverseRouter.route(on(OpredRemoveMemberController.class)
            .removeMember(TEAM_ID, teamMember.wuaId(), null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(OpredTeamManagementController.class).renderMemberList(TEAM_ID))))
        .andExpect(notificationBanner(expectedNotificationBanner));

    verify(teamMemberRemovalService).removeTeamMember(TEAM, teamMember, OpredTeamRole.ACCESS_MANAGER);
  }
}
