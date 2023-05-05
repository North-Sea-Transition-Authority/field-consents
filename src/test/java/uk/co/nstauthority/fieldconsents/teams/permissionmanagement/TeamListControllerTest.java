package uk.co.nstauthority.fieldconsents.teams.permissionmanagement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.TeamListController.INDUSTRY_NEW_TEAM_FORM_URL;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.branding.CustomerConfigurationProperties;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.TeamTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.TeamView;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.industry.IndustryTeamManagementController;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamManagementController;

@ContextConfiguration(classes = TeamListController.class)
class TeamListControllerTest extends AbstractControllerTest {

  @MockBean
  private TeamManagementService teamManagementService;

  @Autowired
  private CustomerConfigurationProperties customerConfigurationProperties;

  private ServiceUserDetail user;

  @BeforeEach
  void setUp() {
    user = ServiceUserDetailTestUtil.Builder().build();
  }

  @SecurityTest
  void resolveTeamListEntryRoute_whenNotAuthenticated_thenRedirectedToLogin() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(TeamListController.class).resolveTeamListEntryRoute())))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void resolveTeamListEntryRoute_whenNotInAnyTeam_thenForbidden() throws Exception {
    when(teamService.getUserAccessibleTeams(user))
        .thenReturn(List.of());
    mockMvc.perform(get(ReverseRouter.route(on(TeamListController.class).resolveTeamListEntryRoute()))
            .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void resolveTeamListEntryRoute_whenInSingleRegulatorTeam_thenAssertRedirect() throws Exception {
    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.REGULATOR)
        .build();
    when(teamService.getUserAccessibleTeams(user))
        .thenReturn(List.of(team));
    mockMvc.perform(get(ReverseRouter.route(on(TeamListController.class).resolveTeamListEntryRoute()))
            .with(user(user)))
        .andExpect(redirectedUrl(
            ReverseRouter.route(on(RegulatorTeamManagementController.class).renderMemberList(team.toTeamId()))
        ));
  }

  @Test
  void resolveTeamListEntryRoute_whenInSingleRegulatorTeamAndManageIndustryTeam_thenAssertRedirect() throws Exception {
    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.REGULATOR)
        .build();
    when(teamService.getUserAccessibleTeams(user))
        .thenReturn(List.of(team));
    when(permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_INDUSTRY_TEAMS)))
        .thenReturn(true);
    mockMvc.perform(get(ReverseRouter.route(on(TeamListController.class).resolveTeamListEntryRoute()))
            .with(user(user)))
        .andExpect(redirectedUrl(
            ReverseRouter.route(on(TeamListController.class).renderTeamList())
        ));
  }

  @Test
  void resolveTeamListEntryRoute_whenInSingleIndustryTeam_thenAssertRedirect() throws Exception {
    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.INDUSTRY)
        .build();
    when(teamService.getUserAccessibleTeams(user))
        .thenReturn(List.of(team));
    mockMvc.perform(get(ReverseRouter.route(on(TeamListController.class).resolveTeamListEntryRoute()))
            .with(user(user)))
        .andExpect(redirectedUrl(
            ReverseRouter.route(on(IndustryTeamManagementController.class).renderMemberList(team.toTeamId()))
        ));
  }

  @Test
  void resolveTeamListEntryRoute_whenInMultipleTeams_thenAssertRedirect() throws Exception {
    var industryTeam = TeamTestUtil.Builder()
        .withTeamType(TeamType.INDUSTRY)
        .build();
    var regulatorTeam = TeamTestUtil.Builder()
        .withTeamType(TeamType.REGULATOR)
        .build();
    when(teamService.getUserAccessibleTeams(user))
        .thenReturn(List.of(industryTeam, regulatorTeam));
    mockMvc.perform(get(ReverseRouter.route(on(TeamListController.class).resolveTeamListEntryRoute()))
            .with(user(user)))
        .andExpect(redirectedUrl(
            ReverseRouter.route(on(TeamListController.class).renderTeamList())
        ));
  }

  @Test
  void renderTeamList_whenAuthenticated_thenOk() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(TeamListController.class).renderTeamList()))
            .with(user(user)))
        .andExpect(status().isOk());
  }

  @Test
  @SecurityTest
  void renderTeamList_whenNotAuthenticated_thenRedirectedToLogin() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(TeamListController.class).renderTeamList())))
        .andExpect(redirectionToLoginUrl());
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void renderTeamList_hasManageIndustryTeamsPermission_assertModelProperties(boolean hasManageIndustryTeamsPermission) throws Exception {

    var regulatorTeam = TeamTestUtil.Builder()
        .withTeamType(TeamType.REGULATOR)
        .build();
    var regulatorTeamView = TeamView.fromTeam(regulatorTeam, customerConfigurationProperties);

    var industryTeam = TeamTestUtil.Builder()
        .withTeamType(TeamType.INDUSTRY)
        .build();
    var industryTeamView = TeamView.fromTeam(industryTeam, customerConfigurationProperties);

    when(teamService.getUserAccessibleTeams(user))
        .thenReturn(List.of(industryTeam, regulatorTeam));

    when(teamManagementService.teamsToTeamViews(List.of(industryTeam, regulatorTeam)))
        .thenReturn(List.of(industryTeamView, regulatorTeamView));

    when(permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_INDUSTRY_TEAMS)))
        .thenReturn(hasManageIndustryTeamsPermission);

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(TeamListController.class).renderTeamList()))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("fcs/permissionmanagement/teamSelectionPage"))
        .andReturn().getModelAndView();

    assertThat(modelAndView).isNotNull();

    var model = modelAndView.getModel();
    assertThat(model)
        .containsEntry("pageTitle", "Teams")
        .containsEntry("allTeams", List.of(industryTeamView, regulatorTeamView));

    if (hasManageIndustryTeamsPermission) {
      assertThat(model)
          .containsEntry("industryNewTeamFormUrl", INDUSTRY_NEW_TEAM_FORM_URL);
    }
  }
}
