package uk.co.nstauthority.fieldconsents.teams.permissionmanagement.opred;

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
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.energyportal.EnergyPortalConfiguration;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDtoTestUtil;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.Team;
import uk.co.nstauthority.fieldconsents.teams.TeamId;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.AddTeamMemberValidator;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamRole;

@ContextConfiguration(classes = {OpredAddMemberController.class})
class OpredAddMemberControllerTest extends AbstractControllerTest {

  private static final Team TEAM = TeamTestUtil.Builder().withTeamType(TeamType.OPRED).build();
  private static final TeamId TEAM_ID = TEAM.toTeamId();
  
  @MockBean
  private EnergyPortalUserService energyPortalUserService;

  @SpyBean
  private AddTeamMemberValidator addTeamMemberValidator;

  @Autowired
  private ApplicationContext applicationContext;

  @SecurityTest
  void renderAddTeamMember_whenUserIsNotLoggedIn_thenRedirectedToLogin() throws Exception {
    var webUserAccountIdToAdd = new WebUserAccountId(123);

    when(teamService.getTeam(TEAM_ID, OpredAddMemberController.TEAM_TYPE)).thenReturn(Optional.of(TEAM));
    when(energyPortalUserService.findByWuaId(webUserAccountIdToAdd)).thenReturn(Optional.of(EnergyPortalUserDtoTestUtil.Builder().build()));

    mockMvc.perform(get(ReverseRouter.route(on(OpredAddMemberController.class).renderAddTeamMember(TEAM_ID))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void renderAddTeamMember_whenAccessManager_thenOk() throws Exception {
    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamId(TEAM_ID)
        .withTeamType(TeamType.OPRED)
        .withRole(OpredTeamRole.ACCESS_MANAGER)
        .build();

    when(teamMemberService.getUserAsTeamMembers(user)).thenReturn(List.of(teamMember));
    when(teamMemberService.isMemberOfTeam(TEAM_ID, user)).thenReturn(true);
    when(teamService.getTeam(TEAM_ID, OpredAddMemberController.TEAM_TYPE)).thenReturn(Optional.of(TEAM));

    mockMvc.perform(get(ReverseRouter.route(on(OpredAddMemberController.class)
            .renderAddTeamMember(TEAM_ID)))
            .with(user(user)))
        .andExpect(status().isOk());
  }

  @SecurityTest
  void renderAddTeamMember_whenThirdPartyAccessManager_thenOk() throws Exception {
    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamType(TeamType.OPRED)
        .withRole(RegulatorTeamRole.INDUSTRY_ACCESS_MANAGER)
        .build();

    when(teamMemberService.getUserAsTeamMembers(user)).thenReturn(List.of(teamMember));
    when(teamService.getTeam(TEAM_ID, OpredAddMemberController.TEAM_TYPE)).thenReturn(Optional.of(TEAM));

    mockMvc.perform(get(ReverseRouter.route(on(OpredAddMemberController.class)
            .renderAddTeamMember(TEAM_ID)))
            .with(user(user)))
        .andExpect(status().isOk());
  }

  @SecurityTest
  void renderAddTeamMember_whenNoMatchingPrivs_thenForbidden() throws Exception {
    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamType(TeamType.OPRED)
        .withRole(OpredTeamRole.ALLOCATOR)
        .build();

    when(teamMemberService.getUserAsTeamMembers(user)).thenReturn(List.of(teamMember));
    when(teamMemberService.isMemberOfTeam(TEAM_ID, user)).thenReturn(true);
    when(teamService.getTeam(TEAM_ID, OpredAddMemberController.TEAM_TYPE)).thenReturn(Optional.of(TEAM));

    mockMvc.perform(get(ReverseRouter.route(on(OpredAddMemberController.class)
            .renderAddTeamMember(TEAM_ID)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void renderAddTeamMember_whenNoTeamFound_thenNotFound() throws Exception {
    var teamMember = TeamMemberTestUtil.Builder().withTeamType(TeamType.OPRED).withRole(OpredTeamRole.RESPONDER).build();

    when(teamMemberService.getUserAsTeamMembers(user)).thenReturn(List.of(teamMember));
    when(teamMemberService.isMemberOfTeam(TEAM_ID, user)).thenReturn(false);
    when(permissionService.hasPermission(user, Set.of(RolePermission.RESPOND_TO_CONSULTATION))).thenReturn(true);
    when(teamService.getTeam(TEAM_ID, OpredAddMemberController.TEAM_TYPE)).thenReturn(Optional.empty());

    mockMvc.perform(get(ReverseRouter.route(on(OpredAddMemberController.class)
            .renderAddTeamMember(TEAM_ID)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void renderAddTeamMember_whenTeamIdIsNotOpredTeam_thenIsForbidden() throws Exception {
    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamType(TeamType.OPRED)
        .withRole(OpredTeamRole.ACCESS_MANAGER)
        .build();

    when(teamMemberService.getUserAsTeamMembers(user)).thenReturn(List.of(teamMember));
    when(teamMemberService.isMemberOfTeam(TEAM_ID, user)).thenReturn(true);
    when(teamService.getTeam(TEAM_ID, OpredAddMemberController.TEAM_TYPE)).thenReturn(Optional.empty());

    mockMvc.perform(get(ReverseRouter.route(on(OpredAddMemberController.class)
            .renderAddTeamMember(TEAM_ID)))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void renderAddTeamMember_assertModelProperties() throws Exception {
    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamId(TEAM.toTeamId())
        .withTeamType(TeamType.OPRED)
        .withRole(OpredTeamRole.ACCESS_MANAGER)
        .build();

    when(teamMemberService.getUserAsTeamMembers(user)).thenReturn(List.of(teamMember));
    when(teamMemberService.isMemberOfTeam(TEAM_ID, user)).thenReturn(true);
    when(teamService.getTeam(TEAM_ID, OpredAddMemberController.TEAM_TYPE)).thenReturn(Optional.of(TEAM));

    var registrationUrl = applicationContext.getBean(EnergyPortalConfiguration.class).registrationUrl();

    mockMvc.perform(get(ReverseRouter.route(on(OpredAddMemberController.class)
            .renderAddTeamMember(TEAM_ID)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/permissionmanagement/addTeamMemberPage"))
        .andExpect(model().attribute("htmlTitle", "Add user to %s".formatted(TEAM.getDisplayName())))
        .andExpect(model().attribute("backLinkUrl", ReverseRouter.route(on(OpredTeamManagementController.class).renderMemberList(TEAM_ID))))
        .andExpect(model().attribute("registrationUrl", registrationUrl))
        .andExpect(model().attribute("submitUrl", ReverseRouter.route(on(OpredAddMemberController.class).addMemberToTeamSubmission(TEAM_ID, null, null))));
  }

  @SecurityTest
  void addMemberToTeamSubmission_whenUserIsNotLoggedIn_thenRedirectedToLogin() throws Exception {
    var webUserAccountIdToAdd = new WebUserAccountId(123);

    when(teamService.getTeam(TEAM_ID, OpredAddMemberController.TEAM_TYPE)).thenReturn(Optional.of(TEAM));
    when(energyPortalUserService.findByWuaId(webUserAccountIdToAdd)).thenReturn(Optional.of(EnergyPortalUserDtoTestUtil.Builder().build()));

    mockMvc.perform(post(ReverseRouter.route(on(OpredAddMemberController.class)
            .addMemberToTeamSubmission(TEAM_ID, null, null)))
            .with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void addMemberToTeamSubmission_whenAccessManager_thenOk() throws Exception {
    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamId(TEAM.toTeamId())
        .withTeamType(TeamType.OPRED)
        .withRole(OpredTeamRole.ACCESS_MANAGER)
        .build();

    when(teamMemberService.getUserAsTeamMembers(user)).thenReturn(List.of(teamMember));
    when(teamMemberService.isMemberOfTeam(TEAM_ID, user)).thenReturn(true);
    when(teamService.getTeam(TEAM_ID, OpredAddMemberController.TEAM_TYPE)).thenReturn(Optional.of(TEAM));

    mockMvc.perform(get(ReverseRouter.route(on(OpredAddMemberController.class)
            .renderAddTeamMember(TEAM_ID)))
            .with(user(user)))
        .andExpect(status().isOk());
  }

  @SecurityTest
  void addMemberToTeamSubmission_whenThirdPartyAccessManager_thenOk() throws Exception {
    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamType(TeamType.OPRED)
        .withRole(RegulatorTeamRole.INDUSTRY_ACCESS_MANAGER)
        .build();

    when(teamMemberService.getUserAsTeamMembers(user)).thenReturn(List.of(teamMember));
    when(teamMemberService.isMemberOfTeam(TEAM_ID, user)).thenReturn(false);
    when(teamService.getTeam(TEAM_ID, OpredAddMemberController.TEAM_TYPE)).thenReturn(Optional.of(TEAM));

    mockMvc.perform(post(ReverseRouter.route(on(OpredAddMemberController.class)
            .addMemberToTeamSubmission(TEAM_ID, null, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk());
  }

  @SecurityTest
  void addMemberToTeamSubmission_whenNoMatchingPrivs_thenForbidden() throws Exception {
    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamType(TeamType.OPRED)
        .withRole(OpredTeamRole.ALLOCATOR)
        .build();

    when(teamMemberService.getUserAsTeamMembers(user)).thenReturn(List.of(teamMember));
    when(teamMemberService.isMemberOfTeam(TEAM_ID, user)).thenReturn(true);
    when(teamService.getTeam(TEAM_ID, OpredAddMemberController.TEAM_TYPE)).thenReturn(Optional.of(TEAM));

    mockMvc.perform(post(ReverseRouter.route(on(OpredAddMemberController.class)
            .addMemberToTeamSubmission(TEAM_ID, null, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void addMemberToTeamSubmission_whenNoTeamFound_thenNotFound() throws Exception {
    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamType(TeamType.OPRED)
        .withRole(OpredTeamRole.RESPONDER)
        .build();

    when(teamMemberService.getUserAsTeamMembers(user)).thenReturn(List.of(teamMember));
    when(teamMemberService.isMemberOfTeam(TEAM_ID, user)).thenReturn(false);
    when(teamService.getTeam(TEAM_ID, OpredAddMemberController.TEAM_TYPE)).thenReturn(Optional.empty());

    mockMvc.perform(post(ReverseRouter.route(on(OpredAddMemberController.class)
            .addMemberToTeamSubmission(TEAM_ID, null, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @Test
  void addMemberToTeamSubmission_whenTeamIdNotOpredType_thenForbidden() throws Exception {
    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamType(TeamType.OPRED)
        .withRole(OpredTeamRole.ACCESS_MANAGER)
        .build();

    when(teamMemberService.getUserAsTeamMembers(user)).thenReturn(List.of(teamMember));
    when(teamMemberService.isMemberOfTeam(TEAM_ID, user)).thenReturn(true);
    when(teamService.getTeam(TEAM_ID, OpredAddMemberController.TEAM_TYPE)).thenReturn(Optional.empty());

    mockMvc.perform(post(ReverseRouter.route(on(OpredAddMemberController.class)
            .addMemberToTeamSubmission(TEAM_ID, null, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void addMemberToTeamSubmission_whenInvalidForm_thenUserStaysOnFormPage() throws Exception {
    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamId(TEAM.toTeamId())
        .withTeamType(TeamType.OPRED)
        .withRole(OpredTeamRole.ACCESS_MANAGER)
        .build();

    when(teamMemberService.getUserAsTeamMembers(user)).thenReturn(List.of(teamMember));
    when(teamMemberService.isMemberOfTeam(TEAM_ID, user)).thenReturn(true);
    when(teamService.getTeam(TEAM_ID, OpredAddMemberController.TEAM_TYPE)).thenReturn(Optional.of(TEAM));

    mockMvc.perform(post(ReverseRouter.route(on(OpredAddMemberController.class)
            .addMemberToTeamSubmission(TEAM_ID, null, null)))
            .with(csrf())
            .with(user(user))
            .param("username", ""))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/permissionmanagement/addTeamMemberPage"));
  }

  @Test
  void addMemberToTeamSubmission_whenValidForm_thenUserTakenToRolesSelection() throws Exception {
    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamId(TEAM.toTeamId())
        .withTeamType(TeamType.OPRED)
        .withRole(OpredTeamRole.ACCESS_MANAGER)
        .build();

    when(teamMemberService.getUserAsTeamMembers(user)).thenReturn(List.of(teamMember));
    when(teamMemberService.isMemberOfTeam(TEAM_ID, user)).thenReturn(true);
    when(teamService.getTeam(TEAM_ID, OpredAddMemberController.TEAM_TYPE)).thenReturn(Optional.of(TEAM));

    var username = "username";
    var userToAdd = EnergyPortalUserDtoTestUtil.Builder().build();

    when(energyPortalUserService.findUserByUsername(username)).thenReturn(List.of(userToAdd));

    var wuaId = new WebUserAccountId(userToAdd.webUserAccountId());
    when(teamService.isMemberOfTeam(TEAM_ID, wuaId)).thenReturn(false);

    mockMvc.perform(post(ReverseRouter.route(on(OpredAddMemberController.class)
            .addMemberToTeamSubmission(TEAM_ID, null, null)))
            .with(csrf())
            .with(user(user))
            .param("username", username))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(OpredAddRolesController.class).renderAddTeamMemberRoles(TEAM_ID, wuaId))));
  }

  @Test
  void addMemberToTeamSubmission_whenValidForm_andAlreadyInTeam_thenUserTakenToEditRoles() throws Exception {
    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamId(TEAM.toTeamId())
        .withTeamType(TeamType.OPRED)
        .withRole(OpredTeamRole.ACCESS_MANAGER)
        .build();

    when(teamMemberService.getUserAsTeamMembers(user)).thenReturn(List.of(teamMember));
    when(teamMemberService.isMemberOfTeam(TEAM_ID, user)).thenReturn(true);
    when(teamService.getTeam(TEAM_ID, OpredAddMemberController.TEAM_TYPE)).thenReturn(Optional.of(TEAM));

    var username = "username";
    var userToAdd = EnergyPortalUserDtoTestUtil.Builder().build();

    when(energyPortalUserService.findUserByUsername(username)).thenReturn(List.of(userToAdd));

    var wuaId = new WebUserAccountId(userToAdd.webUserAccountId());
    when(teamService.isMemberOfTeam(TEAM_ID, wuaId)).thenReturn(true);

    mockMvc.perform(post(ReverseRouter.route(on(OpredAddMemberController.class)
            .addMemberToTeamSubmission(TEAM_ID, null, null)))
            .with(csrf())
            .with(user(user))
            .param("username", username))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(OpredEditMemberController.class).renderEditMember(TEAM_ID, wuaId))));
  }
}
