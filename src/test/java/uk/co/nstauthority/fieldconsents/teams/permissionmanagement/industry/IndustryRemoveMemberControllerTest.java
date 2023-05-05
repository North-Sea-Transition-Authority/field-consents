package uk.co.nstauthority.fieldconsents.teams.permissionmanagement.industry;

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
import static uk.co.nstauthority.fieldconsents.teams.TeamTestUtil.randomInteger;
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
@ContextConfiguration(classes = {IndustryRemoveMemberController.class})
class IndustryRemoveMemberControllerTest extends AbstractControllerTest {

  @MockBean
  private TeamMemberViewService teamMemberViewService;

  @MockBean
  private TeamMemberRemovalService teamMemberRemovalService;

  @SecurityTest
  void renderRemoveMember_whenUserIsNotLoggedIn_thenRedirectedToLogin() throws Exception {

    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.INDUSTRY)
        .build();

    var teamId = team.toTeamId();

    var webUserAccountIdToAdd = new WebUserAccountId(123);

    when(teamService.getTeam(teamId, IndustryRemoveMemberController.TEAM_TYPE)).thenReturn(Optional.of(team));

    mockMvc.perform(
            get(ReverseRouter.route(on(IndustryRemoveMemberController.class)
                .renderRemoveMember(teamId, webUserAccountIdToAdd))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void renderRemoveMember_whenAccessManager_thenOk() throws Exception {

    var user = ServiceUserDetailTestUtil.Builder().build();

    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.INDUSTRY)
        .build();
    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamId(team.toTeamId())
        .withTeamType(TeamType.INDUSTRY)
        .withRole(IndustryTeamRole.ACCESS_MANAGER)
        .build();
    var teamMemberView = TeamMemberViewTestUtil.Builder().build();

    var teamId = team.toTeamId();

    when(teamMemberService.getTeamMember(team, teamMember.wuaId())).thenReturn(Optional.of(teamMember));
    when(teamMemberViewService.getTeamMemberView(teamMember)).thenReturn(Optional.of(teamMemberView));
    when(teamMemberService.getUserAsTeamMembers(user)).thenReturn(List.of(teamMember));
    when(teamMemberService.isMemberOfTeam(teamId, user)).thenReturn(true);
    when(teamService.getTeam(teamId, IndustryRemoveMemberController.TEAM_TYPE)).thenReturn(Optional.of(team));

    mockMvc.perform(
            get(ReverseRouter.route(on(IndustryRemoveMemberController.class)
                .renderRemoveMember(teamId, teamMember.wuaId())))
                .with(user(user)))
        .andExpect(status().isOk());
  }

  @SecurityTest
  void renderRemoveMember_whenThirdPartyAccessManager_thenOk() throws Exception {
    var user = ServiceUserDetailTestUtil.Builder().build();

    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.INDUSTRY)
        .build();
    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamType(TeamType.REGULATOR)
        .withRole(RegulatorTeamRole.INDUSTRY_ACCESS_MANAGER)
        .build();
    var teamMemberView = TeamMemberViewTestUtil.Builder().build();

    var teamId = team.toTeamId();

    when(teamMemberService.getTeamMember(team, teamMember.wuaId())).thenReturn(Optional.of(teamMember));
    when(teamMemberViewService.getTeamMemberView(teamMember)).thenReturn(Optional.of(teamMemberView));
    when(teamMemberService.getUserAsTeamMembers(user)).thenReturn(List.of(teamMember));
    when(teamMemberService.isMemberOfTeam(teamId, user)).thenReturn(false);

    when(teamService.getTeam(teamId, IndustryRemoveMemberController.TEAM_TYPE)).thenReturn(Optional.of(team));

    mockMvc.perform(
            get(ReverseRouter.route(on(IndustryRemoveMemberController.class)
                .renderRemoveMember(teamId, teamMember.wuaId())))
                .with(user(user)))
        .andExpect(status().isOk());
  }

  @SecurityTest
  void renderRemoveMember_whenNoMatchingPrivs_thenForbidden() throws Exception {
    var user = ServiceUserDetailTestUtil.Builder().build();

    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.INDUSTRY)
        .build();
    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamType(TeamType.INDUSTRY)
        .withRole(IndustryTeamRole.CREATOR)
        .build();

    var teamId = team.toTeamId();

    when(teamMemberService.getUserAsTeamMembers(user)).thenReturn(List.of(teamMember));
    when(teamMemberService.isMemberOfTeam(teamId, user)).thenReturn(true);
    when(teamService.getTeam(teamId, IndustryRemoveMemberController.TEAM_TYPE)).thenReturn(Optional.of(team));

    mockMvc.perform(
            get(ReverseRouter.route(on(IndustryRemoveMemberController.class)
                .renderRemoveMember(teamId, teamMember.wuaId())))
                .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void renderRemoveMember_whenAccessManagerAndUserCanBeRemoved_thenOk() throws Exception {

    var user = ServiceUserDetailTestUtil.Builder().build();

    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.INDUSTRY)
        .build();
    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamId(team.toTeamId())
        .withTeamType(TeamType.INDUSTRY)
        .withRole(IndustryTeamRole.ACCESS_MANAGER)
        .build();
    var teamMemberView = TeamMemberViewTestUtil.Builder().build();

    var teamId = team.toTeamId();

    when(teamMemberService.getTeamMember(team, teamMember.wuaId())).thenReturn(Optional.of(teamMember));
    when(teamMemberViewService.getTeamMemberView(teamMember)).thenReturn(Optional.of(teamMemberView));
    when(teamMemberService.getUserAsTeamMembers(user)).thenReturn(List.of(teamMember));
    when(teamMemberService.isMemberOfTeam(teamId, user)).thenReturn(true);
    when(teamService.getTeam(teamId, IndustryRemoveMemberController.TEAM_TYPE)).thenReturn(Optional.of(team));

    var canRemoveTeamMember = true;
    when(teamMemberRemovalService.canRemoveTeamMember(team, teamMember.wuaId(), IndustryTeamRole.ACCESS_MANAGER))
        .thenReturn(canRemoveTeamMember);

    mockMvc.perform(get(ReverseRouter.route(on(IndustryRemoveMemberController.class)
            .renderRemoveMember(teamId, teamMember.wuaId())))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(model().attribute("teamName", team.getDisplayName()))
        .andExpect(model().attribute("teamMember", teamMemberView))
        .andExpect(model().attribute(
            "backLinkUrl",
            ReverseRouter.route(on(IndustryTeamManagementController.class)
                .renderMemberList(teamId))
        ))
        .andExpect(model().attribute(
            "removeUrl",
            ReverseRouter.route(on(IndustryRemoveMemberController.class)
                .removeMember(teamId, teamMember.wuaId(), null))
        ))
        .andExpect(model().attribute("canRemoveTeamMember", canRemoveTeamMember))
        .andExpect(model().attribute(
            "pageTitle",
            "Are you sure you want to remove %s from %s?".formatted(teamMemberView.getDisplayName(),
                team.getDisplayName())
        ))
        .andReturn()
        .getModelAndView();
  }

  @Test
  void renderRemoveMember_whenLastAccessManager_thenOk() throws Exception {

    var user = ServiceUserDetailTestUtil.Builder().build();

    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.INDUSTRY)
        .build();
    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamId(team.toTeamId())
        .withTeamType(TeamType.INDUSTRY)
        .withRole(IndustryTeamRole.ACCESS_MANAGER)
        .build();
    var teamMemberView = TeamMemberViewTestUtil.Builder().build();

    var teamId = team.toTeamId();

    when(teamMemberService.getTeamMember(team, teamMember.wuaId())).thenReturn(Optional.of(teamMember));
    when(teamMemberViewService.getTeamMemberView(teamMember)).thenReturn(Optional.of(teamMemberView));
    when(teamMemberService.getUserAsTeamMembers(user)).thenReturn(List.of(teamMember));
    when(teamMemberService.isMemberOfTeam(teamId, user)).thenReturn(true);
    when(teamService.getTeam(teamId, IndustryRemoveMemberController.TEAM_TYPE)).thenReturn(Optional.of(team));

    var canRemoveTeamMember = false;
    when(teamMemberRemovalService.canRemoveTeamMember(team, teamMember.wuaId(), IndustryTeamRole.ACCESS_MANAGER))
        .thenReturn(canRemoveTeamMember);

    mockMvc.perform(get(ReverseRouter.route(on(IndustryRemoveMemberController.class)
            .renderRemoveMember(teamId, teamMember.wuaId())))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(model().attribute("teamName", team.getDisplayName()))
        .andExpect(model().attribute("teamMember", teamMemberView))
        .andExpect(model().attribute(
            "backLinkUrl",
            ReverseRouter.route(on(IndustryTeamManagementController.class)
                .renderMemberList(teamId))
        ))
        .andExpect(model().attribute(
            "removeUrl",
            ReverseRouter.route(on(IndustryRemoveMemberController.class)
                .removeMember(teamId, teamMember.wuaId(), null))
        ))
        .andExpect(model().attribute("canRemoveTeamMember", canRemoveTeamMember))
        .andExpect(model().attribute(
            "pageTitle",
            "You are unable to remove %s from %s".formatted(teamMemberView.getDisplayName(),
                team.getDisplayName())
        ))
        .andReturn()
        .getModelAndView();
  }

  @SecurityTest
  void removeMember_whenUserIsNotLoggedIn_thenRedirectedToLogin() throws Exception {

    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.INDUSTRY)
        .build();

    var teamId = team.toTeamId();

    var webUserAccountIdToAdd = new WebUserAccountId(123);

    when(teamService.getTeam(teamId, IndustryRemoveMemberController.TEAM_TYPE)).thenReturn(Optional.of(team));

    mockMvc.perform(
            post(ReverseRouter.route(on(IndustryRemoveMemberController.class)
                .removeMember(teamId, webUserAccountIdToAdd, null)))
                .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void removeMember_whenAccessManager_thenOk() throws Exception {

    var user = ServiceUserDetailTestUtil.Builder().build();

    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.INDUSTRY)
        .build();
    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamId(team.toTeamId())
        .withTeamType(TeamType.INDUSTRY)
        .withRole(IndustryTeamRole.ACCESS_MANAGER)
        .build();
    var teamMemberView = TeamMemberViewTestUtil.Builder().build();

    var teamId = team.toTeamId();

    when(teamMemberService.getTeamMember(team, teamMember.wuaId())).thenReturn(Optional.of(teamMember));
    when(teamMemberViewService.getTeamMemberView(teamMember)).thenReturn(Optional.of(teamMemberView));
    when(teamMemberService.getUserAsTeamMembers(user)).thenReturn(List.of(teamMember));
    when(teamMemberService.isMemberOfTeam(teamId, user)).thenReturn(true);
    when(teamService.getTeam(teamId, IndustryRemoveMemberController.TEAM_TYPE)).thenReturn(Optional.of(team));

    var canRemoveTeamMember = false;
    when(teamMemberRemovalService.canRemoveTeamMember(team, teamMember.wuaId(), IndustryTeamRole.ACCESS_MANAGER))
        .thenReturn(canRemoveTeamMember);

    mockMvc.perform(
            get(ReverseRouter.route(on(IndustryRemoveMemberController.class)
                .removeMember(teamId, teamMember.wuaId(), null)))
                .with(user(user)))
        .andExpect(status().isOk());
  }

  @SecurityTest
  void removeMember_whenThirdPartyAccessManager_thenOk() throws Exception {
    var user = ServiceUserDetailTestUtil.Builder().build();

    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.INDUSTRY)
        .build();
    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamType(TeamType.REGULATOR)
        .withRole(RegulatorTeamRole.INDUSTRY_ACCESS_MANAGER)
        .build();
    var teamMemberView = TeamMemberViewTestUtil.Builder().build();

    var teamId = team.toTeamId();

    when(teamMemberService.getTeamMember(team, teamMember.wuaId())).thenReturn(Optional.of(teamMember));
    when(teamMemberViewService.getTeamMemberView(teamMember)).thenReturn(Optional.of(teamMemberView));
    when(teamMemberService.getUserAsTeamMembers(user)).thenReturn(List.of(teamMember));
    when(teamMemberService.isMemberOfTeam(teamId, user)).thenReturn(true);
    when(teamService.getTeam(teamId, IndustryRemoveMemberController.TEAM_TYPE)).thenReturn(Optional.of(team));

    var canRemoveTeamMember = false;
    when(teamMemberRemovalService.canRemoveTeamMember(team, teamMember.wuaId(), IndustryTeamRole.ACCESS_MANAGER))
        .thenReturn(canRemoveTeamMember);

    mockMvc.perform(
            post(ReverseRouter.route(on(IndustryRemoveMemberController.class)
                .removeMember(teamId, teamMember.wuaId(), null)))
                .with(user(user))
                .with(csrf()))
        .andExpect(status().isOk());
  }

  @SecurityTest
  void removeMember_whenNoMatchingPrivs_thenForbidden() throws Exception {
    var user = ServiceUserDetailTestUtil.Builder().build();

    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.INDUSTRY)
        .build();
    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamType(TeamType.INDUSTRY)
        .withRole(IndustryTeamRole.CREATOR)
        .build();

    var teamId = team.toTeamId();

    when(teamMemberService.getUserAsTeamMembers(user)).thenReturn(List.of(teamMember));
    when(teamMemberService.isMemberOfTeam(teamId, user)).thenReturn(true);
    when(teamService.getTeam(teamId, IndustryRemoveMemberController.TEAM_TYPE)).thenReturn(Optional.of(team));

    mockMvc.perform(
            post(ReverseRouter.route(on(IndustryRemoveMemberController.class)
                .removeMember(teamId, teamMember.wuaId(), null)))
                .with(user(user))
                .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void removeMember_whenNoTeamFound_thenNotFound() throws Exception {

    var user = ServiceUserDetailTestUtil.Builder().build();
    var teamId = new TeamId(randomInteger());

    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamType(TeamType.INDUSTRY)
        .withRole(IndustryTeamRole.CREATOR)
        .build();

    when(teamMemberService.getUserAsTeamMembers(user)).thenReturn(List.of(teamMember));
    when(teamMemberService.isMemberOfTeam(teamId, user)).thenReturn(false);
    when(permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_INDUSTRY_TEAMS))).thenReturn(true);

    when(teamService.getTeam(teamId, IndustryRemoveMemberController.TEAM_TYPE)).thenReturn(Optional.empty());

    mockMvc.perform(
            post(ReverseRouter.route(on(IndustryRemoveMemberController.class)
                .removeMember(teamId, teamMember.wuaId(), null)))
                .with(user(user))
                .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @Test
  void removeMember_whenLoggedIn_verifyCalls() throws Exception {

    var user = ServiceUserDetailTestUtil.Builder().build();

    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.INDUSTRY)
        .build();
    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamId(team.toTeamId())
        .withTeamType(TeamType.INDUSTRY)
        .withRole(IndustryTeamRole.ACCESS_MANAGER)
        .build();
    var teamMemberView = TeamMemberViewTestUtil.Builder().build();

    var teamId = team.toTeamId();

    when(teamMemberService.getTeamMember(team, teamMember.wuaId())).thenReturn(Optional.of(teamMember));
    when(teamMemberViewService.getTeamMemberView(teamMember)).thenReturn(Optional.of(teamMemberView));
    when(teamMemberService.getUserAsTeamMembers(user)).thenReturn(List.of(teamMember));
    when(teamMemberService.isMemberOfTeam(teamId, user)).thenReturn(true);
    when(teamService.getTeam(teamId, IndustryRemoveMemberController.TEAM_TYPE)).thenReturn(Optional.of(team));

    var canRemoveTeamMember = true;
    when(teamMemberRemovalService.canRemoveTeamMember(team, teamMember.wuaId(), IndustryTeamRole.ACCESS_MANAGER))
        .thenReturn(canRemoveTeamMember);

    var expectedNotificationBanner = NotificationBanner.builder()
        .withTitle("Removed member from team")
        .withBannerType(NotificationBannerType.SUCCESS)
        .withContent("%s has been removed from the team".formatted(teamMemberView.getDisplayName()))
        .build();

    mockMvc.perform(post(ReverseRouter.route(on(IndustryRemoveMemberController.class)
            .removeMember(teamId, teamMember.wuaId(), null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(IndustryTeamManagementController.class)
            .renderMemberList(teamId))))
        .andExpect(notificationBanner(expectedNotificationBanner));

    verify(teamMemberRemovalService).removeTeamMember(team, teamMember, IndustryTeamRole.ACCESS_MANAGER);

  }
}
