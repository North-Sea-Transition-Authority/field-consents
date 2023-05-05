package uk.co.nstauthority.fieldconsents.teams.permissionmanagement.industry;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.energyportal.organisationgroup.OrganisationGroupRestController;
import uk.co.nstauthority.fieldconsents.energyportal.organisationgroup.OrganisationGroupTestUtil;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.TeamId;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberViewService;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberViewTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.TeamListController;

@ContextConfiguration(classes = IndustryTeamManagementController.class)
class IndustryTeamManagementControllerTest extends AbstractControllerTest {

  @MockBean
  private TeamMemberViewService teamMemberViewService;

  @MockBean
  private IndustryTeamService industryTeamService;

  @MockBean
  private IndustryNewTeamFormValidator industryNewTeamFormValidator;

  @SecurityTest
  void renderNewIndustryTeamForm_whenNotAuthenticated_thenRedirectToLogin() throws Exception {
    mockMvc.perform(get(
        ReverseRouter.route(on(IndustryTeamManagementController.class).renderNewIndustryTeamForm(new IndustryNewTeamForm()))
        ))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void renderNewIndustryTeamForm_whenUserDoesntHaveManageIndustryTeamsPermission() throws Exception {
    var user = ServiceUserDetailTestUtil.Builder().build();

    when(permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_INDUSTRY_TEAMS)))
        .thenReturn(false);

    mockMvc.perform(
        get(ReverseRouter.route(on(IndustryTeamManagementController.class)
            .renderNewIndustryTeamForm(new IndustryNewTeamForm())))
            .with(user(user))
        )
        .andExpect(status().isForbidden());
  }

  @Test
  void renderNewIndustryTeamForm_whenUserHasManageIndustryTeamsPermission() throws Exception {
    var user = ServiceUserDetailTestUtil.Builder().build();

    when(permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_INDUSTRY_TEAMS)))
        .thenReturn(true);

    mockMvc.perform(
            get(ReverseRouter.route(on(IndustryTeamManagementController.class)
                .renderNewIndustryTeamForm(new IndustryNewTeamForm())))
                .with(user(user))
        )
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/permissionmanagement/addTeam"))
        .andExpect(model().attribute("pageTitle", "Add new industry team"))
        .andExpect(model().attribute(
            "organisationGroupSearchRestUrl",
            ReverseRouter.route(on(OrganisationGroupRestController.class).getOrganisationGroupSearchResults(null))
        ))
        .andExpect(model().attribute(
            "submitFormUrl",
            ReverseRouter.route(on(IndustryTeamManagementController.class)
                .addNewIndustryTeam(null, null, null))
        ))
        .andExpect(model().attribute(
            "backLinkUrl",
            ReverseRouter.route(on(TeamListController.class).resolveTeamListEntryRoute())
        ));
  }

  @SecurityTest
  void addNewIndustryTeam_whenNotAuthenticated_thenRedirectToLogin() throws Exception {
    mockMvc.perform(post(
            ReverseRouter.route(on(IndustryTeamManagementController.class)
                .addNewIndustryTeam(new IndustryNewTeamForm(), null, null))
        ).with(csrf()))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void addNewIndustryTeam_whenUserDoesntHaveManageIndustryTeamsPermission() throws Exception {
    var user = ServiceUserDetailTestUtil.Builder().build();

    when(permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_INDUSTRY_TEAMS)))
        .thenReturn(false);

    mockMvc.perform(
            post(ReverseRouter.route(on(IndustryTeamManagementController.class)
                .addNewIndustryTeam(new IndustryNewTeamForm(), null, null)))
                .with(user(user))
                .with(csrf())
        )
        .andExpect(status().isForbidden());
  }

  @Test
  void addNewIndustryTeam_whenUserHasManageIndustryTeamsPermission_formInvalid() throws Exception {
    var user = ServiceUserDetailTestUtil.Builder().build();

    when(permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_INDUSTRY_TEAMS)))
        .thenReturn(true);

    var form = new IndustryNewTeamForm();

    doCallRealMethod().when(industryNewTeamFormValidator).validate(any(), any());

    mockMvc.perform(
            post(ReverseRouter.route(on(IndustryTeamManagementController.class)
                .addNewIndustryTeam(form, null, null)))
                .with(user(user))
                .with(csrf())
                .flashAttr("form", form)
        )
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/permissionmanagement/addTeam"))
        .andExpect(model().attribute("pageTitle", "Add new industry team"))
        .andExpect(model().attribute(
            "organisationGroupSearchRestUrl",
            ReverseRouter.route(on(OrganisationGroupRestController.class).getOrganisationGroupSearchResults(null))
        ))
        .andExpect(model().attribute(
            "submitFormUrl",
            ReverseRouter.route(on(IndustryTeamManagementController.class)
                .addNewIndustryTeam(null, null, null))
        ))
        .andExpect(model().attribute(
            "backLinkUrl",
            ReverseRouter.route(on(TeamListController.class).resolveTeamListEntryRoute())
        ));
  }

  @Test
  void addNewIndustryTeam_whenUserHasManageIndustryTeamsPermission_formValid() throws Exception {
    var user = ServiceUserDetailTestUtil.Builder().build();

    when(permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_INDUSTRY_TEAMS)))
        .thenReturn(true);

    var organisationGroupId = 10000;
    var orgGroup = OrganisationGroupTestUtil.createOrganisationGroupDto(organisationGroupId, "Royal Dutch Shell");

    var form = new IndustryNewTeamForm();
    form.setOrganisationGroupId(String.valueOf(organisationGroupId));

    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.INDUSTRY)
        .build();

    when(industryTeamService.getTeamByOrganisationGroupId((organisationGroupId)))
        .thenReturn(Optional.of(team));

    when(organisationGroupQueryService.getOrganisationGroupById(any())).thenReturn(Optional.of(orgGroup));

    mockMvc.perform(
            post(ReverseRouter.route(on(IndustryTeamManagementController.class)
                .addNewIndustryTeam(form, null, null)))
                .with(user(user))
                .with(csrf())
                .flashAttr("form", form)
        )
        .andExpect(status().is3xxRedirection());
  }

  @Test
  void addNewIndustryTeam_whenUserHasManageIndustryTeamsPermission_formValid_noOrgGroupFound() throws Exception {
    var user = ServiceUserDetailTestUtil.Builder().build();

    when(permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_INDUSTRY_TEAMS)))
        .thenReturn(true);

    var organisationGroupId = 10000;
    var form = new IndustryNewTeamForm();
    form.setOrganisationGroupId(String.valueOf(organisationGroupId));

    when(organisationGroupQueryService.getOrganisationGroupById(organisationGroupId))
        .thenReturn(Optional.empty());

    mockMvc.perform(
        post(ReverseRouter.route(on(IndustryTeamManagementController.class)
            .addNewIndustryTeam(form, null, null)))
            .with(user(user))
            .with(csrf())
            .flashAttr("form", form))
        .andExpect(status().is4xxClientError());
  }

  @Test
  void addNewIndustryTeam_whenUserHasManageIndustryTeamsPermission_formValid_noExistingTeamExists() throws Exception {
    var user = ServiceUserDetailTestUtil.Builder().build();

    when(permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_INDUSTRY_TEAMS)))
        .thenReturn(true);

    var organisationGroupId = 10000;
    var orgGroupName = "Royal Dutch Shell";
    var orgGroup = OrganisationGroupTestUtil.createOrganisationGroupDto(organisationGroupId, orgGroupName);

    var form = new IndustryNewTeamForm();
    form.setOrganisationGroupId(String.valueOf(organisationGroupId));

    when(organisationGroupQueryService.getOrganisationGroupById(any())).thenReturn(Optional.of(orgGroup));

    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.INDUSTRY)
        .build();

    when(industryTeamService.getTeamByOrganisationGroupId((organisationGroupId)))
        .thenReturn(Optional.empty());

    when(industryTeamService.createTeam(orgGroupName, organisationGroupId))
        .thenReturn(team);

    mockMvc.perform(
            post(ReverseRouter.route(on(IndustryTeamManagementController.class)
                .addNewIndustryTeam(form, null, null)))
                .with(user(user))
                .with(csrf())
                .flashAttr("form", form)
        )
        .andExpect(status().is3xxRedirection());
  }


  @SecurityTest
  void renderMemberList_whenNotAuthenticated_thenRedirectToLogin() throws Exception {
    var teamId = new TeamId(randomInteger());
    mockMvc.perform(
        get(ReverseRouter.route(on(IndustryTeamManagementController.class).renderMemberList(teamId))))
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  void renderMemberList_whenMemberOfTeam_thenOk() throws Exception {

    var user = ServiceUserDetailTestUtil.Builder().build();

    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.INDUSTRY)
        .build();

    var teamId = team.toTeamId();

    when(teamMemberService.isMemberOfTeam(teamId, user)).thenReturn(true);
    when(teamService.getTeam(teamId, IndustryTeamManagementController.TEAM_TYPE)).thenReturn(Optional.of(team));

    mockMvc.perform(
            get(ReverseRouter.route(on(IndustryTeamManagementController.class).renderMemberList(teamId)))
                .with(user(user)))
        .andExpect(status().isOk());
  }

  @Test
  void renderMemberList_whenNoTeamFound_thenNotFound() throws Exception {

    var user = ServiceUserDetailTestUtil.Builder().build();

    var teamId = new TeamId(randomInteger());

    when(teamMemberService.isMemberOfTeam(teamId, user)).thenReturn(true);
    when(teamService.getTeam(teamId, IndustryTeamManagementController.TEAM_TYPE)).thenReturn(Optional.empty());

    mockMvc.perform(
            get(ReverseRouter.route(on(IndustryTeamManagementController.class).renderMemberList(teamId)))
                .with(user(user)))
        .andExpect(status().isNotFound());
  }

  @Test
  void renderMemberList_whenNotAccessManager_assertModelProperties() throws Exception {

    var user = ServiceUserDetailTestUtil.Builder().build();

    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.INDUSTRY)
        .build();

    var teamId = team.toTeamId();

    when(teamMemberService.isMemberOfTeam(teamId, user)).thenReturn(true);
    when(teamService.getTeam(teamId, IndustryTeamManagementController.TEAM_TYPE)).thenReturn(Optional.of(team));

    var teamMemberView = TeamMemberViewTestUtil.Builder()
        .withRole(IndustryTeamRole.CREATOR)
        .build();

    when(teamMemberViewService.getTeamMemberViewsForTeam(team)).thenReturn(List.of(teamMemberView));

    mockMvc.perform(
            get(ReverseRouter.route(on(IndustryTeamManagementController.class).renderMemberList(teamId)))
                .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/permissionmanagement/teamMembersPage"))
        .andExpect(model().attribute("pageTitle", "Manage %s".formatted(team.getDisplayName())))
        .andExpect(model().attribute("teamName", team.getDisplayName()))
        .andExpect(model().attribute("teamRoles", IndustryTeamRole.values()))
        .andExpect(model().attributeDoesNotExist("addTeamMemberUrl"));
  }

  @Test
  void renderMemberList_whenAccessManagerAndInMultipleTeams_assertModelProperties() throws Exception {

    var user = ServiceUserDetailTestUtil.Builder().build();

    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.INDUSTRY)
        .build();

    var teamId = team.toTeamId();

    when(teamMemberService.isMemberOfTeam(teamId, user)).thenReturn(true);
    when(industryTeamService.isAccessManager(teamId, user)).thenReturn(true);
    when(teamService.getTeam(teamId, IndustryTeamManagementController.TEAM_TYPE)).thenReturn(Optional.of(team));

    var teamMemberView = TeamMemberViewTestUtil.Builder().build();
    when(teamMemberViewService.getTeamMemberViewsForTeam(team)).thenReturn(List.of(teamMemberView));

    when(teamService.canUserAccessMultipleTeams(user))
        .thenReturn(true);

    var canRemoveUsers = true;
    when(permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_INDUSTRY_TEAMS)))
        .thenReturn(canRemoveUsers);

    mockMvc.perform(
            get(ReverseRouter.route(on(IndustryTeamManagementController.class).renderMemberList(teamId)))
                .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/permissionmanagement/teamMembersPage"))
        .andExpect(model().attribute("pageTitle", "Manage %s".formatted(team.getDisplayName())))
        .andExpect(model().attribute("teamName", team.getDisplayName()))
        .andExpect(model().attribute("teamRoles", IndustryTeamRole.values()))
        .andExpect(model().attribute(
            "backLinkUrl",
            ReverseRouter.route(on(TeamListController.class).resolveTeamListEntryRoute())
        ))
        .andExpect(model().attribute(
            "addTeamMemberUrl",
            ReverseRouter.route(on(IndustryAddMemberController.class).renderAddTeamMember(teamId))
        ))
        .andExpect(model().attribute("canRemoveUsers", canRemoveUsers))
        .andExpect(model().attribute("teamMembers", List.of(teamMemberView)));
  }

}
