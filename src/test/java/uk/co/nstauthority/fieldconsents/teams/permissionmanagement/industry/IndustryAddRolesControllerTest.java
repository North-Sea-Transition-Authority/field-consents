package uk.co.nstauthority.fieldconsents.teams.permissionmanagement.industry;

import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.teams.TeamTestUtil.randomInteger;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDtoTestUtil;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.TeamId;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamRole;
import uk.co.nstauthority.fieldconsents.util.StreamUtils;

@ContextConfiguration(classes = {
    IndustryAddRolesController.class,
    IndustryTeamMemberRolesValidator.class
})
class IndustryAddRolesControllerTest extends AbstractControllerTest {

  @MockBean
  private IndustryTeamService industryTeamService;

  @MockBean
  private EnergyPortalUserService energyPortalUserService;

  @Autowired
  IndustryTeamMemberRolesValidator industryTeamMemberRolesValidator;

  @BeforeEach
  void setUp() {
    doCallRealMethod().when(userDetailService).getUserDetail();
  }

  @SecurityTest
  void renderAddTeamMemberRoles_whenUserIsNotLoggedIn_thenRedirectedToLogin() throws Exception {

    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.INDUSTRY)
        .build();

    var teamId = team.toTeamId();

    var webUserAccountIdToAdd = new WebUserAccountId(123);

    when(teamService.getTeam(teamId, IndustryAddRolesController.TEAM_TYPE)).thenReturn(Optional.of(team));

    when(energyPortalUserService.findByWuaId(webUserAccountIdToAdd))
        .thenReturn(Optional.of(EnergyPortalUserDtoTestUtil.Builder().build()));

    mockMvc.perform(
            get(ReverseRouter.route(on(IndustryAddRolesController.class)
                .renderAddTeamMemberRoles(teamId, webUserAccountIdToAdd))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void renderAddTeamMemberRoles_whenAccessManager_thenOk() throws Exception {

    var user = ServiceUserDetailTestUtil.Builder().build();

    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.INDUSTRY)
        .build();
    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamId(team.toTeamId())
        .withTeamType(TeamType.INDUSTRY)
        .withRole(IndustryTeamRole.ACCESS_MANAGER)
        .build();

    var teamId = team.toTeamId();

    when(teamMemberService.getUserAsTeamMembers(user)).thenReturn(List.of(teamMember));
    when(teamMemberService.isMemberOfTeam(teamId, user)).thenReturn(true);
    when(teamService.getTeam(teamId, IndustryAddRolesController.TEAM_TYPE)).thenReturn(Optional.of(team));

    var energyPortalUser = EnergyPortalUserDtoTestUtil.Builder().build();
    when(energyPortalUserService.findByWuaId(teamMember.wuaId()))
        .thenReturn(Optional.of(energyPortalUser));

    mockMvc.perform(
            get(ReverseRouter.route(on(IndustryAddRolesController.class)
                .renderAddTeamMemberRoles(teamId, teamMember.wuaId())))
                .with(user(user)))
        .andExpect(status().isOk());
  }

  @SecurityTest
  void renderAddTeamMemberRoles_whenThirdPartyAccessManager_thenOk() throws Exception {
    var user = ServiceUserDetailTestUtil.Builder().build();

    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.INDUSTRY)
        .build();
    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamType(TeamType.REGULATOR)
        .withRole(RegulatorTeamRole.INDUSTRY_ACCESS_MANAGER)
        .build();

    var teamId = team.toTeamId();

    when(teamMemberService.getUserAsTeamMembers(user)).thenReturn(List.of(teamMember));
    when(teamMemberService.isMemberOfTeam(teamId, user)).thenReturn(false);

    when(teamService.getTeam(teamId, IndustryAddRolesController.TEAM_TYPE)).thenReturn(Optional.of(team));

    var energyPortalUser = EnergyPortalUserDtoTestUtil.Builder().build();
    when(energyPortalUserService.findByWuaId(teamMember.wuaId()))
        .thenReturn(Optional.of(energyPortalUser));

    mockMvc.perform(
            get(ReverseRouter.route(on(IndustryAddRolesController.class)
                .renderAddTeamMemberRoles(teamId, teamMember.wuaId())))
                .with(user(user)))
        .andExpect(status().isOk());
  }

  @SecurityTest
  void renderAddTeamMemberRoles_whenNoMatchingPrivs_thenForbidden() throws Exception {
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
    when(teamService.getTeam(teamId, IndustryAddRolesController.TEAM_TYPE)).thenReturn(Optional.of(team));

    mockMvc.perform(
            get(ReverseRouter.route(on(IndustryAddRolesController.class)
                .renderAddTeamMemberRoles(teamId, teamMember.wuaId())))
                .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void renderAddTeamMemberRoles_whenNoTeamFound_thenNotFound() throws Exception {

    var user = ServiceUserDetailTestUtil.Builder().build();
    var teamId = new TeamId(randomInteger());

    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamType(TeamType.INDUSTRY)
        .withRole(IndustryTeamRole.CREATOR)
        .build();

    when(teamMemberService.getUserAsTeamMembers(user)).thenReturn(List.of(teamMember));
    when(teamMemberService.isMemberOfTeam(teamId, user)).thenReturn(false);
    when(permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_INDUSTRY_TEAMS))).thenReturn(true);

    when(teamService.getTeam(teamId, IndustryAddRolesController.TEAM_TYPE)).thenReturn(Optional.empty());

    mockMvc.perform(
            get(ReverseRouter.route(on(IndustryAddRolesController.class)
                .renderAddTeamMemberRoles(teamId, teamMember.wuaId())))
                .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void renderAddTeamMemberRoles_whenNoEnergyPortalUser_thenNotFound() throws Exception {

    var user = ServiceUserDetailTestUtil.Builder().build();

    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.INDUSTRY)
        .build();
    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamId(team.toTeamId())
        .withTeamType(TeamType.INDUSTRY)
        .withRole(IndustryTeamRole.ACCESS_MANAGER)
        .build();

    var teamId = team.toTeamId();

    when(teamMemberService.getUserAsTeamMembers(user)).thenReturn(List.of(teamMember));
    when(teamMemberService.isMemberOfTeam(teamId, user)).thenReturn(true);
    when(teamService.getTeam(teamId, IndustryAddRolesController.TEAM_TYPE)).thenReturn(Optional.of(team));

    when(energyPortalUserService.findByWuaId(teamMember.wuaId()))
        .thenReturn(Optional.empty());

    mockMvc.perform(
            get(ReverseRouter.route(on(IndustryAddRolesController.class)
                .renderAddTeamMemberRoles(teamId, teamMember.wuaId())))
                .with(user(user)))
        .andExpect(status().isNotFound());
  }

  @ParameterizedTest
  @MethodSource("getEnergyPortalUserThatShouldResultInBadRequest")
  void renderAddTeamMemberRoles_whenEnergyPortalUserNotValid_thenBadRequest(EnergyPortalUserDto energyPortalUser) throws Exception {

    var user = ServiceUserDetailTestUtil.Builder().build();

    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.INDUSTRY)
        .build();
    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamId(team.toTeamId())
        .withTeamType(TeamType.INDUSTRY)
        .withRole(IndustryTeamRole.ACCESS_MANAGER)
        .build();

    var teamId = team.toTeamId();

    when(teamMemberService.getUserAsTeamMembers(user)).thenReturn(List.of(teamMember));
    when(teamMemberService.isMemberOfTeam(teamId, user)).thenReturn(true);
    when(teamService.getTeam(teamId, IndustryAddRolesController.TEAM_TYPE)).thenReturn(Optional.of(team));

    when(energyPortalUserService.findByWuaId(teamMember.wuaId()))
        .thenReturn(Optional.of(energyPortalUser));

    mockMvc.perform(
            get(ReverseRouter.route(on(IndustryAddRolesController.class)
                .renderAddTeamMemberRoles(teamId, teamMember.wuaId())))
                .with(user(user)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void renderAddTeamMemberRoles_assertModelProperties() throws Exception {

    var user = ServiceUserDetailTestUtil.Builder().build();

    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.INDUSTRY)
        .build();
    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamId(team.toTeamId())
        .withTeamType(TeamType.INDUSTRY)
        .withRole(IndustryTeamRole.ACCESS_MANAGER)
        .build();

    var teamId = team.toTeamId();

    when(teamMemberService.getUserAsTeamMembers(user)).thenReturn(List.of(teamMember));
    when(teamMemberService.isMemberOfTeam(teamId, user)).thenReturn(true);
    when(teamService.getTeam(teamId, IndustryAddRolesController.TEAM_TYPE)).thenReturn(Optional.of(team));

    var energyPortalUser = EnergyPortalUserDtoTestUtil.Builder().build();
    when(energyPortalUserService.findByWuaId(teamMember.wuaId()))
        .thenReturn(Optional.of(energyPortalUser));

    mockMvc.perform(
            get(ReverseRouter.route(on(IndustryAddRolesController.class)
                .renderAddTeamMemberRoles(teamId, teamMember.wuaId())))
                .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/permissionmanagement/addTeamMemberRolesPage"))
        .andExpect(model().attribute(
            "pageTitle",
            "What actions does %s perform?".formatted(energyPortalUser.displayName())
        ))
        .andExpect(model().attribute(
            "backLinkUrl",
            ReverseRouter.route(on(IndustryAddMemberController.class)
                .renderAddTeamMember(teamId))
        ))
        .andExpect(model().attributeExists("form"))
        .andExpect(model().attribute("roles", getDisplayableIndustryRoles()));
  }

  @SecurityTest
  void saveAddTeamMemberRoles_whenUserIsNotLoggedIn_thenRedirectedToLogin() throws Exception {

    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.INDUSTRY)
        .build();

    var teamId = team.toTeamId();

    var webUserAccountIdToAdd = new WebUserAccountId(123);

    when(teamService.getTeam(teamId, IndustryAddRolesController.TEAM_TYPE)).thenReturn(Optional.of(team));

    when(energyPortalUserService.findByWuaId(webUserAccountIdToAdd))
        .thenReturn(Optional.of(EnergyPortalUserDtoTestUtil.Builder().build()));

    mockMvc.perform(
            post(ReverseRouter.route(on(IndustryAddRolesController.class)
                .saveAddTeamMemberRoles(teamId, webUserAccountIdToAdd, null, null)))
                .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void saveAddTeamMemberRoles_whenAccessManager_thenOk() throws Exception {

    var user = ServiceUserDetailTestUtil.Builder().build();

    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.INDUSTRY)
        .build();
    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamId(team.toTeamId())
        .withTeamType(TeamType.INDUSTRY)
        .withRole(IndustryTeamRole.ACCESS_MANAGER)
        .build();

    var teamId = team.toTeamId();

    when(teamMemberService.getUserAsTeamMembers(user)).thenReturn(List.of(teamMember));
    when(teamMemberService.isMemberOfTeam(teamId, user)).thenReturn(true);
    when(teamService.getTeam(teamId, IndustryAddRolesController.TEAM_TYPE)).thenReturn(Optional.of(team));

    var energyPortalUser = EnergyPortalUserDtoTestUtil.Builder().build();
    when(energyPortalUserService.findByWuaId(teamMember.wuaId()))
        .thenReturn(Optional.of(energyPortalUser));

    mockMvc.perform(
            get(ReverseRouter.route(on(IndustryAddRolesController.class)
                .saveAddTeamMemberRoles(teamId, teamMember.wuaId(), null, null)))
                .with(user(user)))
        .andExpect(status().isOk());
  }

  @SecurityTest
  void saveAddTeamMemberRoles_whenThirdPartyAccessManager_thenOk() throws Exception {
    var user = ServiceUserDetailTestUtil.Builder().build();

    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.INDUSTRY)
        .build();
    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamType(TeamType.REGULATOR)
        .withRole(RegulatorTeamRole.INDUSTRY_ACCESS_MANAGER)
        .build();

    var teamId = team.toTeamId();

    when(teamMemberService.getUserAsTeamMembers(user)).thenReturn(List.of(teamMember));
    when(teamMemberService.isMemberOfTeam(teamId, user)).thenReturn(false);
    when(teamService.getTeam(teamId, IndustryAddRolesController.TEAM_TYPE)).thenReturn(Optional.of(team));

    var energyPortalUser = EnergyPortalUserDtoTestUtil.Builder().build();
    when(energyPortalUserService.findByWuaId(teamMember.wuaId()))
        .thenReturn(Optional.of(energyPortalUser));

    mockMvc.perform(
            post(ReverseRouter.route(on(IndustryAddRolesController.class)
                .saveAddTeamMemberRoles(teamId, teamMember.wuaId(), null, null)))
                .with(user(user))
                .with(csrf()))
        .andExpect(status().isOk());
  }

  @SecurityTest
  void saveAddTeamMemberRoles_whenNoMatchingPrivs_thenForbidden() throws Exception {
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

    when(teamService.getTeam(teamId, IndustryAddRolesController.TEAM_TYPE)).thenReturn(Optional.of(team));

    mockMvc.perform(
            post(ReverseRouter.route(on(IndustryAddRolesController.class)
                .saveAddTeamMemberRoles(teamId, teamMember.wuaId(), null, null)))
                .with(user(user))
                .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void saveAddTeamMemberRoles_whenNoTeamFound_thenNotFound() throws Exception {

    var user = ServiceUserDetailTestUtil.Builder().build();
    var teamId = new TeamId(randomInteger());

    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamType(TeamType.INDUSTRY)
        .withRole(IndustryTeamRole.CREATOR)
        .build();

    when(teamMemberService.getUserAsTeamMembers(user)).thenReturn(List.of(teamMember));
    when(teamMemberService.isMemberOfTeam(teamId, user)).thenReturn(false);
    when(permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_INDUSTRY_TEAMS))).thenReturn(true);

    when(teamService.getTeam(teamId, IndustryAddRolesController.TEAM_TYPE)).thenReturn(Optional.empty());

    mockMvc.perform(
            post(ReverseRouter.route(on(IndustryAddRolesController.class)
                .saveAddTeamMemberRoles(teamId, teamMember.wuaId(), null, null)))
                .with(user(user))
                .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @Test
  void saveAddTeamMemberRoles_whenEnergyPortalUserNotFound_thenNotFound() throws Exception {

    var user = ServiceUserDetailTestUtil.Builder().build();

    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.INDUSTRY)
        .build();
    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamId(team.toTeamId())
        .withTeamType(TeamType.INDUSTRY)
        .withRole(IndustryTeamRole.ACCESS_MANAGER)
        .build();

    var teamId = team.toTeamId();

    when(teamMemberService.getUserAsTeamMembers(user)).thenReturn(List.of(teamMember));
    when(teamMemberService.isMemberOfTeam(teamId, user)).thenReturn(true);
    when(teamService.getTeam(teamId, IndustryAddRolesController.TEAM_TYPE)).thenReturn(Optional.of(team));

    when(energyPortalUserService.findByWuaId(teamMember.wuaId()))
        .thenReturn(Optional.empty());

    mockMvc.perform(
            post(ReverseRouter.route(on(IndustryAddRolesController.class)
                .saveAddTeamMemberRoles(teamId, teamMember.wuaId(), null, null)))
                .with(user(user))
                .with(csrf())
        )
        .andExpect(status().isNotFound());
  }

  @ParameterizedTest
  @MethodSource("getEnergyPortalUserThatShouldResultInBadRequest")
  void saveAddTeamMemberRoles_whenEnergyPortalUserNotValid_thenBadRequest(EnergyPortalUserDto energyPortalUser) throws Exception {

    var user = ServiceUserDetailTestUtil.Builder().build();

    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.INDUSTRY)
        .build();
    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamId(team.toTeamId())
        .withTeamType(TeamType.INDUSTRY)
        .withRole(IndustryTeamRole.ACCESS_MANAGER)
        .build();

    var teamId = team.toTeamId();

    when(teamMemberService.getUserAsTeamMembers(user)).thenReturn(List.of(teamMember));
    when(teamMemberService.isMemberOfTeam(teamId, user)).thenReturn(true);
    when(teamService.getTeam(teamId, IndustryAddRolesController.TEAM_TYPE)).thenReturn(Optional.of(team));

    when(energyPortalUserService.findByWuaId(teamMember.wuaId()))
        .thenReturn(Optional.of(energyPortalUser));

    mockMvc.perform(
            post(ReverseRouter.route(on(IndustryAddRolesController.class)
                .saveAddTeamMemberRoles(teamId, teamMember.wuaId(), null, null)))
                .with(user(user))
                .with(csrf())
        )
        .andExpect(status().isBadRequest());
  }

  @Test
  void saveAddTeamMemberRoles_whenInvalidTeamMemberRolesForm_thenStayOnFormPage() throws Exception {

    var user = ServiceUserDetailTestUtil.Builder().build();

    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.INDUSTRY)
        .build();
    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamId(team.toTeamId())
        .withTeamType(TeamType.INDUSTRY)
        .withRole(IndustryTeamRole.ACCESS_MANAGER)
        .build();

    var teamId = team.toTeamId();

    when(teamMemberService.getUserAsTeamMembers(user)).thenReturn(List.of(teamMember));
    when(teamMemberService.isMemberOfTeam(teamId, user)).thenReturn(true);
    when(teamService.getTeam(teamId, IndustryAddRolesController.TEAM_TYPE)).thenReturn(Optional.of(team));

    var energyPortalUser = EnergyPortalUserDtoTestUtil.Builder().build();
    when(energyPortalUserService.findByWuaId(teamMember.wuaId()))
        .thenReturn(Optional.of(energyPortalUser));

    mockMvc.perform(
            post(ReverseRouter.route(on(IndustryAddRolesController.class)
                .saveAddTeamMemberRoles(teamId, teamMember.wuaId(), null, null)))
                .with(user(user))
                .with(csrf())
                .param("roles", "")
        )
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/permissionmanagement/addTeamMemberRolesPage"));
  }

  @Test
  void saveAddTeamMemberRoles_whenValidTeamMemberRolesForm_thenRedirectionToMembersPage() throws Exception {

    var user = ServiceUserDetailTestUtil.Builder().build();

    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.INDUSTRY)
        .build();
    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamId(team.toTeamId())
        .withTeamType(TeamType.INDUSTRY)
        .withRole(IndustryTeamRole.ACCESS_MANAGER)
        .build();

    var teamId = team.toTeamId();

    when(teamMemberService.getUserAsTeamMembers(user)).thenReturn(List.of(teamMember));
    when(teamMemberService.isMemberOfTeam(teamId, user)).thenReturn(true);
    when(teamService.getTeam(teamId, IndustryAddRolesController.TEAM_TYPE)).thenReturn(Optional.of(team));

    var energyPortalUser = EnergyPortalUserDtoTestUtil.Builder().build();
    when(energyPortalUserService.findByWuaId(teamMember.wuaId()))
        .thenReturn(Optional.of(energyPortalUser));

    mockMvc.perform(
            post(ReverseRouter.route(on(IndustryAddRolesController.class)
                .saveAddTeamMemberRoles(teamId, teamMember.wuaId(), null, null)))
                .with(user(user))
                .with(csrf())
                .param("roles", IndustryTeamRole.ACCESS_MANAGER.name())
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(IndustryTeamManagementController.class)
            .renderMemberList(teamId))));
  }

  private Map<String, String> getDisplayableIndustryRoles() {
    return Arrays.stream(IndustryTeamRole.values())
        .sorted(Comparator.comparing(IndustryTeamRole::getDisplayOrder))
        .collect(StreamUtils.toLinkedHashMap(
            IndustryTeamRole::name,
            role -> "%s (%s)".formatted(role.getDescription(), role.getDisplayName())
        ));
  }

  private static Stream<Arguments> getEnergyPortalUserThatShouldResultInBadRequest() {

    var noLoginEnergyPortalUser = EnergyPortalUserDtoTestUtil.Builder()
        .canLogin(false)
        .build();

    var sharedAccountEnergyPortalUser = EnergyPortalUserDtoTestUtil.Builder()
        .hasSharedAccount(true)
        .build();

    return Stream.of(
        Arguments.of(noLoginEnergyPortalUser),
        Arguments.of(sharedAccountEnergyPortalUser)
    );
  }
}
