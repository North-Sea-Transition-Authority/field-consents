package uk.co.nstauthority.fieldconsents.teams.management;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.co.fivium.energyportalapi.generated.types.User;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.energyportal.EnergyPortalConfiguration;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.Role;
import uk.co.nstauthority.fieldconsents.teams.Team;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.management.form.AddMemberFormValidator;
import uk.co.nstauthority.fieldconsents.teams.management.form.MemberRolesFormValidator;
import uk.co.nstauthority.fieldconsents.teams.management.view.TeamMemberView;
import uk.co.nstauthority.fieldconsents.teams.management.view.TeamTypeView;
import uk.co.nstauthority.fieldconsents.teams.management.view.TeamView;

@SuppressWarnings({"unchecked", "DataFlowIssue"})
@ContextConfiguration(classes = TeamManagementController.class)
class TeamManagementControllerTest extends AbstractControllerTest {

  @MockitoBean
  private MemberRolesFormValidator memberRolesFormValidator;

  @MockitoBean
  private AddMemberFormValidator addMemberFormValidator;

  @MockitoBean
  private EnergyPortalConfiguration energyPortalConfiguration;

  @MockitoBean
  private EnergyPortalUserService energyPortalUserService;

  private static Team regTeam;
  private static Team organisationTeam;
  private static TeamMemberView regTeamMemberView;
  private static ServiceUserDetail invokingUser;

  @BeforeAll
  public static void setUp() {
    regTeam = new Team(UUID.randomUUID());
    regTeam.setTeamType(TeamType.REGULATOR);
    regTeam.setName("reg team one");

    organisationTeam = new Team(UUID.randomUUID());
    organisationTeam.setTeamType(TeamType.INDUSTRY);
    organisationTeam.setName("org team");

    regTeamMemberView = new TeamMemberView(
        1L,
        "Test",
        "User",
        "test@example.com",
        "020123456",
        regTeam.getId(),
        List.of(Role.ACCESS_MANAGER)
    );

    invokingUser = ServiceUserDetailTestUtil.Builder()
        .withWuaId(1L)
        .build();
  }

  @Test
  void renderTeamTypeList() throws Exception {
    when(teamManagementService.getTeamTypesUserIsMemberOf(invokingUser))
        .thenReturn(Set.of(TeamType.INDUSTRY, TeamType.REGULATOR));

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderTeamTypeList(null)))
        .with(user(invokingUser)))
        .andExpect(status().isOk())
        .andReturn().getModelAndView();

    var teamTypeViews = (List<TeamTypeView>) modelAndView.getModel().get("teamTypeViews");

    assertThat(teamTypeViews)
        .extracting(TeamTypeView::teamTypeName)
        .containsExactly(TeamType.INDUSTRY.getDisplayName(), TeamType.REGULATOR.getDisplayName());
  }

  @Test
  void renderTeamTypeList_singeTypeRedirects() throws Exception {
    when(teamManagementService.getTeamTypesUserIsMemberOf(invokingUser))
        .thenReturn(Set.of(TeamType.INDUSTRY));

    mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderTeamTypeList(null)))
        .with(user(invokingUser)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(TeamManagementController.class).renderTeamsOfType(TeamType.INDUSTRY.getUrlSlug(), null))));
  }

  @Test
  void renderTeamTypeList_regWithOrgManageCanSeeOrgTeams() throws Exception {
    when(teamManagementService.getTeamTypesUserIsMemberOf(invokingUser))
        .thenReturn(Set.of(TeamType.REGULATOR));

    when(teamQueryService.userHasStaticRole(invokingUser, TeamType.REGULATOR, Role.INDUSTRY_ACCESS_MANAGER))
        .thenReturn(true);

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderTeamTypeList(null)))
        .with(user(invokingUser)))
        .andExpect(status().isOk())
        .andReturn().getModelAndView();

    var teamTypeViews = (List<TeamTypeView>) modelAndView.getModel().get("teamTypeViews");

    assertThat(teamTypeViews)
        .extracting(TeamTypeView::teamTypeName)
        .containsExactly(TeamType.INDUSTRY.getDisplayName(), TeamType.REGULATOR.getDisplayName());
  }

  @Test
  void renderTeamTypeList_noManageableTeams() throws Exception {
    when(teamManagementService.getTeamTypesUserIsMemberOf(invokingUser))
        .thenReturn(Set.of());

    mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderTeamTypeList(null)))
        .with(user(invokingUser)))
        .andExpect(status().isForbidden());
  }

  @Test
  void renderTeamsOfType_staticTeamRedirectsToSingleInstance() throws Exception {
    when(teamManagementService.getStaticTeamOfTypeUserIsMemberOf(TeamType.REGULATOR, invokingUser))
        .thenReturn(Optional.of(regTeam));

    mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderTeamsOfType(TeamType.REGULATOR.getUrlSlug(), null)))
        .with(user(invokingUser)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(TeamManagementController.class).renderTeamMemberList(regTeam.getId(), null))));
  }

  @Test
  void renderTeamsOfType_singleScopedTeamRedirectsToInstance() throws Exception {
    when(teamManagementService.getScopedTeamsOfTypeUserIsMemberOf(TeamType.INDUSTRY, invokingUser))
        .thenReturn(Set.of(organisationTeam));

    mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderTeamsOfType(TeamType.INDUSTRY.getUrlSlug(), null)))
        .with(user(invokingUser)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(TeamManagementController.class).renderTeamMemberList(organisationTeam.getId(), null))));
  }

  @Test
  void renderTeamsOfType_scopedTeamReturnList() throws Exception {

    var firstOrganisationTeamByName = new Team(UUID.randomUUID());
    firstOrganisationTeamByName.setTeamType(TeamType.INDUSTRY);
    firstOrganisationTeamByName.setName("a team name");

    var secondOrganisationTeamByName = new Team(UUID.randomUUID());
    secondOrganisationTeamByName.setTeamType(TeamType.INDUSTRY);
    secondOrganisationTeamByName.setName("b team name");

    var thirdOrganisationTeamByName = new Team(UUID.randomUUID());
    thirdOrganisationTeamByName.setTeamType(TeamType.INDUSTRY);
    thirdOrganisationTeamByName.setName("C team name");

    when(teamManagementService.getScopedTeamsOfTypeUserIsMemberOf(TeamType.INDUSTRY, invokingUser))
        .thenReturn(Set.of(secondOrganisationTeamByName, thirdOrganisationTeamByName, firstOrganisationTeamByName));

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class)
        .renderTeamsOfType(TeamType.INDUSTRY.getUrlSlug(), null)))
        .with(user(invokingUser)))
        .andExpect(status().isOk())
        .andReturn().getModelAndView();

    var teamTypeViews = (List<TeamView>) modelAndView.getModel().get("teamViews");

    assertThat(teamTypeViews)
        .extracting(TeamView::teamName)
        .containsExactlyInAnyOrder(
            firstOrganisationTeamByName.getName(),
            secondOrganisationTeamByName.getName(),
            thirdOrganisationTeamByName.getName()
        );
  }

  @Test
  void renderTeamsOfType_noManageableTeams() throws Exception {
    when(teamManagementService.getScopedTeamsOfTypeUserIsMemberOf(TeamType.INDUSTRY, invokingUser))
        .thenReturn(Set.of());

    mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderTeamsOfType(TeamType.INDUSTRY.getUrlSlug(), null)))
        .with(user(invokingUser)))
        .andExpect(status().isForbidden());
  }

  @Test
  void renderTeamsOfType_noManageableTeams_orgAdminNotForbidden() throws Exception {
    when(teamManagementService.getScopedTeamsOfTypeUserIsMemberOf(TeamType.INDUSTRY, invokingUser))
        .thenReturn(Set.of());

    when(teamQueryService.userHasStaticRole(invokingUser, TeamType.REGULATOR, Role.INDUSTRY_ACCESS_MANAGER))
        .thenReturn(true);

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class)
        .renderTeamsOfType(TeamType.INDUSTRY.getUrlSlug(), null)))
        .with(user(invokingUser)))
        .andExpect(status().isOk())
        .andReturn().getModelAndView();

    var createNewInstanceUrl = (String) modelAndView.getModel().get("createNewInstanceUrl");

    assertThat(createNewInstanceUrl)
        .isEqualTo(TeamType.INDUSTRY.getCreateNewInstanceRoute().get());
  }


  @Test
  void renderTeamMemberList_whenNotMemberOfTeam_thenForbidden() throws Exception {

    var team = regTeam;

    when(teamManagementService.getTeam(team.getId()))
        .thenReturn(Optional.of(team));

    when(teamManagementService.isMemberOfTeam(team, invokingUser))
        .thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderTeamMemberList(team.getId(), null)))
        .with(user(invokingUser)))
        .andExpect(status().isForbidden());
  }

  @Test
  void renderTeamMemberList_whenMemberOfTeam_thenOk() throws Exception {

    var team = regTeam;

    when(teamManagementService.getTeam(team.getId()))
        .thenReturn(Optional.of(team));

    when(teamManagementService.isMemberOfTeam(team, invokingUser))
        .thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderTeamMemberList(team.getId(), null)))
        .with(user(invokingUser)))
        .andExpect(status().isOk());
  }

  @Test
  void renderTeamMemberList_whenOrganisationTeam_andNotMemberOfTeam_andUserHasManageAnyOrganisationRole_thenOk() throws Exception {

    // GIVEN an organisation team
    var team = organisationTeam;

    when(teamManagementService.getTeam(team.getId()))
        .thenReturn(Optional.of(team));

    // AND the invoking user is not a direct member
    when(teamManagementService.isMemberOfTeam(team, invokingUser))
        .thenReturn(false);

    // WHEN the invoking user has the CREATE_MANAGE_ANY_ORGANISATION_TEAM in the regulator team
    when(teamManagementService.userCanManageAnyOrganisationTeam(invokingUser))
        .thenReturn(true);

    // THEN the invoking user will be able to view the team
    mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderTeamMemberList(team.getId(), null)))
        .with(user(invokingUser)))
        .andExpect(status().isOk());
  }

  @Test
  void renderTeamMemberList_whenOrganisationTeam_andNotMemberOfTeam_andUserWithoutManageAnyOrganisationRole_thenForbidden() throws Exception {

    // GIVEN an organisation team
    var team = organisationTeam;

    when(teamManagementService.getTeam(team.getId()))
        .thenReturn(Optional.of(team));

    // AND the invoking user is not a direct member
    when(teamManagementService.isMemberOfTeam(team, invokingUser))
        .thenReturn(false);

    // WHEN the invoking user does not have the CREATE_MANAGE_ANY_ORGANISATION_TEAM in the regulator team
    when(teamManagementService.userCanManageAnyOrganisationTeam(invokingUser))
        .thenReturn(false);

    // THEN the invoking user will not be able to view the team
    mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderTeamMemberList(team.getId(), null)))
        .with(user(invokingUser)))
        .andExpect(status().isForbidden());
  }

  @ParameterizedTest
  @EnumSource(value = TeamType.class, mode = EnumSource.Mode.EXCLUDE, names = "INDUSTRY")
  void renderTeamMemberList_whenNotOrganisationTeam_andNotMemberOfTeam_andCanManageAnyOrganisationRole_thenForbidden(
      TeamType nonOrganisationTeamType) throws Exception {

    // GIVEN an non-organisation team
    var team = new Team(UUID.randomUUID());
    team.setTeamType(nonOrganisationTeamType);

    when(teamManagementService.getTeam(team.getId()))
        .thenReturn(Optional.of(team));

    // AND the invoking user is not a direct member
    when(teamManagementService.isMemberOfTeam(team, invokingUser))
        .thenReturn(false);

    // WHEN the invoking user has the CREATE_MANAGE_ANY_ORGANISATION_TEAM in the regulator team
    when(teamManagementService.userCanManageAnyOrganisationTeam(invokingUser))
        .thenReturn(true);

    // THEN the invoking user will not be able to view the team
    mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderTeamMemberList(team.getId(), null)))
        .with(user(invokingUser)))
        .andExpect(status().isForbidden());
  }

  @Test
  void renderTeamMemberList_whenIsMemberOfTeamAndTeamManager_thenAssetModelProperties() throws Exception {
    when(teamManagementService.canManageTeam(regTeam, invokingUser))
        .thenReturn(true);

    when(teamManagementService.getTeam(regTeam.getId()))
        .thenReturn(Optional.of(regTeam));

    when(teamManagementService.isMemberOfTeam(regTeam, invokingUser))
        .thenReturn(true);

    when(teamManagementService.getTeamMemberViewsForTeam(regTeam))
        .thenReturn(List.of(regTeamMemberView));

    mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderTeamMemberList(regTeam.getId(), null)))
        .with(user(invokingUser)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/teamManagement/teamMembers"))
        .andExpect(model().attribute("teamName", regTeam.getName()))
        .andExpect(model().attribute("teamMemberViews", List.of(regTeamMemberView)))
        .andExpect(model().attribute("canManageTeam", true))
        .andExpect(model().attribute(
            "addMemberUrl",
            ReverseRouter.route(on(TeamManagementController.class).renderAddMemberToTeam(regTeam.getId(), null))
        ))
        .andExpect(model().attribute("rolesInTeam", regTeam.getTeamType().getAllowedRoles()));
  }

  @Test
  void renderTeamMemberList_whenIsMemberOfTeamAndNotTeamManager_thenAssetModelProperties() throws Exception {
    when(teamManagementService.canManageTeam(regTeam, invokingUser))
        .thenReturn(false);

    when(teamManagementService.isMemberOfTeam(regTeam, invokingUser))
        .thenReturn(true);

    when(teamManagementService.getTeam(regTeam.getId()))
        .thenReturn(Optional.of(regTeam));

    when(teamManagementService.getTeamMemberViewsForTeam(regTeam))
        .thenReturn(List.of(regTeamMemberView));

    mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderTeamMemberList(regTeam.getId(), null)))
        .with(user(invokingUser)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/teamManagement/teamMembers"))
        .andExpect(model().attribute("teamName", regTeam.getName()))
        .andExpect(model().attribute("teamMemberViews", List.of(regTeamMemberView)))
        .andExpect(model().attribute("canManageTeam", false))
        .andExpect(model().attribute(
            "addMemberUrl",
            ReverseRouter.route(on(TeamManagementController.class).renderAddMemberToTeam(regTeam.getId(), null))
        ));
  }

  @Test
  void renderTeamMemberList_noTeamFound() throws Exception {
    var nonExistentTeamId = UUID.randomUUID();
    when(teamManagementService.getTeam(nonExistentTeamId))
        .thenReturn(Optional.empty());

    mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderTeamMemberList(nonExistentTeamId, null)))
        .with(user(invokingUser)))
        .andExpect(status().isNotFound());
  }

  @Test
  void renderTeamMemberList_noAccess() throws Exception {
    when(teamManagementService.getTeam(regTeam.getId()))
        .thenReturn(Optional.of(regTeam));

    when(teamManagementService.getStaticTeamOfTypeUserCanManage(regTeam.getTeamType(), invokingUser))
        .thenReturn(Optional.empty());

    mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderTeamMemberList(regTeam.getId(), null)))
        .with(user(invokingUser)))
        .andExpect(status().isForbidden());
  }

  @Test
  void renderAddMemberToTeam() throws Exception {
    when(teamManagementService.getTeam(organisationTeam.getId()))
        .thenReturn(Optional.of(organisationTeam));

    when(teamManagementService.getScopedTeamsOfTypeUserCanManage(TeamType.INDUSTRY, invokingUser))
        .thenReturn(Set.of(organisationTeam));

    when(energyPortalConfiguration.registrationUrl())
        .thenReturn("https://example.com");

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderAddMemberToTeam(organisationTeam.getId(), null)))
        .with(user(invokingUser)))
        .andExpect(status().isOk())
        .andReturn().getModelAndView();

    var registerUrl = (String) modelAndView.getModel().get("registerUrl");

    assertThat(registerUrl)
        .isEqualTo("https://example.com");
  }

  @Test
  void renderAddMemberToTeam_noTeamFound() throws Exception {
    var nonExistentTeamId = UUID.randomUUID();
    when(teamManagementService.getTeam(nonExistentTeamId))
        .thenReturn(Optional.empty());

    mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderAddMemberToTeam(nonExistentTeamId, null)))
        .with(user(invokingUser)))
        .andExpect(status().isNotFound());
  }

  @Test
  void renderAddMemberToTeam_noAccess() throws Exception {
    when(teamManagementService.getTeam(organisationTeam.getId()))
        .thenReturn(Optional.of(organisationTeam));

    when(teamManagementService.getScopedTeamsOfTypeUserCanManage(TeamType.INDUSTRY, invokingUser))
        .thenReturn(Set.of());

    when(energyPortalConfiguration.registrationUrl())
        .thenReturn("https://example.com");

    mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderAddMemberToTeam(organisationTeam.getId(), null)))
        .with(user(invokingUser)))
        .andExpect(status().isForbidden());
  }

  @Test
  void handleAddMemberToTeam() throws Exception {
    var epaUser = new User.Builder()
        .webUserAccountId(999)
        .isAccountShared(false)
        .canLogin(true)
        .build();

    when(teamManagementService.getTeam(regTeam.getId()))
        .thenReturn(Optional.of(regTeam));

    when(teamManagementService.getStaticTeamOfTypeUserCanManage(regTeam.getTeamType(), invokingUser))
        .thenReturn(Optional.of(regTeam));

    when(addMemberFormValidator.isValid(any(), any()))
        .thenReturn(true);

    when(energyPortalUserService.getEnergyPortalUsersThatCanLogin("foo"))
        .thenReturn(List.of(epaUser));

    mockMvc.perform(post(ReverseRouter.route(on(TeamManagementController.class).handleAddMemberToTeam(regTeam.getId(), null, null)))
        .with(csrf())
        .with(user(invokingUser))
        .param("username", "foo"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(TeamManagementController.class).renderUserTeamRoles(regTeam.getId(), 999L, null))));
  }

  @Test
  void handleAddMemberToTeam_invalidForm() throws Exception {
    when(teamManagementService.getTeam(regTeam.getId()))
        .thenReturn(Optional.of(regTeam));

    when(teamManagementService.getStaticTeamOfTypeUserCanManage(regTeam.getTeamType(), invokingUser))
        .thenReturn(Optional.of(regTeam));

    when(addMemberFormValidator.isValid(any(), any()))
        .thenReturn(false);

    when(energyPortalConfiguration.registrationUrl())
        .thenReturn("https://example.com");

    mockMvc.perform(post(ReverseRouter.route(on(TeamManagementController.class).handleAddMemberToTeam(regTeam.getId(), null, null)))
        .with(csrf())
        .with(user(invokingUser)))
        .andExpect(status().isOk()); // No redirect to next page
  }

  @Test
  void handleAddMemberToTeam_invalidUser() throws Exception {
    when(teamManagementService.getTeam(regTeam.getId()))
        .thenReturn(Optional.of(regTeam));

    when(teamManagementService.getStaticTeamOfTypeUserCanManage(regTeam.getTeamType(), invokingUser))
        .thenReturn(Optional.of(regTeam));

    when(addMemberFormValidator.isValid(any(), any()))
        .thenReturn(true);

    when(energyPortalUserService.getEnergyPortalUsersThatCanLogin("foo"))
        .thenReturn(List.of());

    mockMvc.perform(post(ReverseRouter.route(on(TeamManagementController.class).handleAddMemberToTeam(regTeam.getId(), null, null)))
        .with(csrf())
        .with(user(invokingUser))
        .param("username", "foo"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void handleAddMemberToTeam_noAccess() throws Exception {
    when(teamManagementService.getTeam(regTeam.getId()))
        .thenReturn(Optional.of(regTeam));

    when(teamManagementService.getStaticTeamOfTypeUserCanManage(regTeam.getTeamType(), invokingUser))
        .thenReturn(Optional.empty());

    mockMvc.perform(post(ReverseRouter.route(on(TeamManagementController.class).handleAddMemberToTeam(regTeam.getId(), null, null)))
        .with(csrf())
        .with(user(invokingUser))
        .param("username", "foo"))
        .andExpect(status().isForbidden());
  }

  @Test
  void renderUserTeamRoles() throws Exception {
    when(teamManagementService.getTeam(regTeam.getId()))
        .thenReturn(Optional.of(regTeam));

    when(teamManagementService.getStaticTeamOfTypeUserCanManage(regTeam.getTeamType(), invokingUser))
        .thenReturn(Optional.of(regTeam));

    when(teamManagementService.getTeamMemberView(regTeam, 999L))
        .thenReturn(regTeamMemberView);

    var expectedRoleDisplayNameByEnumName = Map.of(
        Role.ACCESS_MANAGER.name(), Role.ACCESS_MANAGER.getDisplayName(),
        Role.INDUSTRY_ACCESS_MANAGER.name(), Role.INDUSTRY_ACCESS_MANAGER.getDisplayName(),
        Role.DOCUMENT_TEMPLATE_MANAGER.name(), Role.DOCUMENT_TEMPLATE_MANAGER.getDisplayName(),
        Role.CASE_OFFICER.name(), Role.CASE_OFFICER.getDisplayName(),
        Role.CASE_MANAGER.name(), Role.CASE_MANAGER.getDisplayName(),
        Role.CONSENTS_AND_AUTHORISATIONS_MANAGER.name(), Role.CONSENTS_AND_AUTHORISATIONS_MANAGER.getDisplayName(),
        Role.TECHNICAL_REVIEWER.name(), Role.TECHNICAL_REVIEWER.getDisplayName(),
        Role.VIEWER.name(), Role.VIEWER.getDisplayName()
    );

    mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderUserTeamRoles(regTeam.getId(), 999L, null)))
        .with(user(invokingUser)))
        .andExpect(status().isOk())
        .andExpect(model().attribute("rolesNamesMap", expectedRoleDisplayNameByEnumName))
        .andExpect(model().attribute("teamMemberView", regTeamMemberView))
        .andExpect(model().attribute("rolesInTeam", regTeam.getTeamType().getAllowedRoles()));
  }

  @Test
  void renderUserTeamRoles_noAccess() throws Exception {
    when(teamManagementService.getTeam(regTeam.getId()))
        .thenReturn(Optional.of(regTeam));

    when(teamManagementService.getStaticTeamOfTypeUserCanManage(regTeam.getTeamType(), invokingUser))
        .thenReturn(Optional.empty());


    mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderUserTeamRoles(regTeam.getId(), 999L, null)))
        .with(user(invokingUser)))
        .andExpect(status().isForbidden());
  }

  @Test
  void updateUserTeamRoles() throws Exception {
    when(teamManagementService.getTeam(regTeam.getId()))
        .thenReturn(Optional.of(regTeam));

    when(teamManagementService.getStaticTeamOfTypeUserCanManage(regTeam.getTeamType(), invokingUser))
        .thenReturn(Optional.of(regTeam));

    when(memberRolesFormValidator.isValid(any(), eq(999L), eq(regTeam), any()))
        .thenReturn(true);

    mockMvc.perform(post(ReverseRouter.route(on(TeamManagementController.class).updateUserTeamRoles(regTeam.getId(), 999L, null, null)))
        .with(csrf())
        .with(user(invokingUser))
        .param("roles", "ACCESS_MANAGER"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(TeamManagementController.class).renderTeamMemberList(regTeam.getId(), null))));

    verify(teamManagementService).setUserTeamRoles(999L, regTeam, List.of(Role.ACCESS_MANAGER));
  }

  @Test
  void updateUserTeamRoles_invalidForm() throws Exception {
    when(teamManagementService.getTeam(regTeam.getId()))
        .thenReturn(Optional.of(regTeam));

    when(teamManagementService.getStaticTeamOfTypeUserCanManage(regTeam.getTeamType(), invokingUser))
        .thenReturn(Optional.of(regTeam));

    when(memberRolesFormValidator.isValid(any(), eq(999L), eq(regTeam), any()))
        .thenReturn(false);

    when(teamManagementService.getTeamMemberView(regTeam, 999L))
        .thenReturn(regTeamMemberView);

    mockMvc.perform(post(ReverseRouter.route(on(TeamManagementController.class).updateUserTeamRoles(regTeam.getId(), 999L, null, null)))
        .with(csrf())
        .with(user(invokingUser))
        .param("roles", "MANAGE_TEAM"))
        .andExpect(status().isOk()); // No redirect to next page

    verify(teamManagementService, never()).setUserTeamRoles(any(), any(), any());
  }

  @Test
  void updateUserTeamRoles_noAccess() throws Exception {
    when(teamManagementService.getTeam(regTeam.getId()))
        .thenReturn(Optional.of(regTeam));

    when(teamManagementService.getStaticTeamOfTypeUserCanManage(regTeam.getTeamType(), invokingUser))
        .thenReturn(Optional.empty());

    mockMvc.perform(post(ReverseRouter.route(on(TeamManagementController.class).updateUserTeamRoles(regTeam.getId(), 999L, null, null)))
        .with(csrf())
        .with(user(invokingUser))
        .param("roles", "MANAGE_TEAM"))
        .andExpect(status().isForbidden());

    verify(teamManagementService, never()).setUserTeamRoles(any(), any(), any());
  }

  @Test
  void renderRemoveTeamMember() throws Exception {
    when(teamManagementService.getTeam(regTeam.getId()))
        .thenReturn(Optional.of(regTeam));

    when(teamManagementService.getStaticTeamOfTypeUserCanManage(regTeam.getTeamType(), invokingUser))
        .thenReturn(Optional.of(regTeam));

    when(teamManagementService.getTeamMemberView(regTeam, 999L))
        .thenReturn(regTeamMemberView);

    when(teamManagementService.willManageTeamRoleBePresentAfterMemberRemoval(regTeam, 999L))
        .thenReturn(true);

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderRemoveTeamMember(regTeam.getId(), 999L)))
        .with(user(invokingUser)))
        .andExpect(status().isOk())
        .andReturn().getModelAndView();

    var teamMemberViewModel = (TeamMemberView) modelAndView.getModel().get("teamMemberView");
    var teamName = (String) modelAndView.getModel().get("teamName");
    var canRemoveTeamMember = (boolean) modelAndView.getModel().get("canRemoveTeamMember");

    assertThat(teamMemberViewModel).isEqualTo(regTeamMemberView);
    assertThat(teamName).isEqualTo(regTeam.getName());
    assertThat(canRemoveTeamMember).isTrue();
  }

  @Test
  void renderRemoveTeamMember_noAccess() throws Exception {
    when(teamManagementService.getTeam(regTeam.getId()))
        .thenReturn(Optional.of(regTeam));

    when(teamManagementService.getStaticTeamOfTypeUserCanManage(regTeam.getTeamType(), invokingUser))
        .thenReturn(Optional.empty());

    mockMvc.perform(get(ReverseRouter.route(on(TeamManagementController.class).renderRemoveTeamMember(regTeam.getId(), 999L)))
        .with(user(invokingUser)))
        .andExpect(status().isForbidden());
  }

  @Test
  void handleRemoveTeamMember() throws Exception {
    when(teamManagementService.getTeam(regTeam.getId()))
        .thenReturn(Optional.of(regTeam));

    when(teamManagementService.getStaticTeamOfTypeUserCanManage(regTeam.getTeamType(), invokingUser))
        .thenReturn(Optional.of(regTeam));

    mockMvc.perform(post(ReverseRouter.route(on(TeamManagementController.class).handleRemoveTeamMember(regTeam.getId(), 999L)))
        .with(csrf())
        .with(user(invokingUser)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(TeamManagementController.class).renderTeamMemberList(regTeam.getId(), null))));

    verify(teamManagementService).removeUserFromTeam(999L, regTeam);
  }

  @Test
  void handleRemoveTeamMember_noAccess() throws Exception {
    when(teamManagementService.getTeam(regTeam.getId()))
        .thenReturn(Optional.of(regTeam));

    when(teamManagementService.getStaticTeamOfTypeUserCanManage(regTeam.getTeamType(), invokingUser))
        .thenReturn(Optional.empty());

    mockMvc.perform(post(ReverseRouter.route(on(TeamManagementController.class).handleRemoveTeamMember(regTeam.getId(), 999L)))
        .with(csrf())
        .with(user(invokingUser)))
        .andExpect(status().isForbidden());

    verify(teamManagementService, never()).removeUserFromTeam(999L, regTeam);
  }

}