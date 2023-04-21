package uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.teams.TeamTestUtil.randomInteger;
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
import uk.co.nstauthority.fieldconsents.teams.TeamMemberTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberViewService;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberViewTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamService;
import uk.co.nstauthority.fieldconsents.teams.TeamTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.TeamView;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.TeamMemberRolesForm;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.TeamRole;
import uk.co.nstauthority.fieldconsents.util.enumutil.DisplayableEnumOptionUtil;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RegulatorEditMemberController.class})
class RegulatorEditMemberControllerTest extends AbstractControllerTest {

  @MockBean
  private RegulatorTeamService regulatorTeamService;

  @MockBean
  private TeamService teamService;

  @MockBean
  private TeamMemberViewService teamMemberViewService;

  @MockBean
  RegulatorTeamMemberEditService regulatorTeamMemberEditService;

  @MockBean
  RegulatorTeamMemberEditRolesValidator regulatorTeamMemberEditRolesValidator;

  private Team regulatorTeam;
  private TeamView teamView;
  private ServiceUserDetail accessManager;
  private ServiceUserDetail nonAccessManager;

  @BeforeEach
  void setUp() {
    regulatorTeam = TeamTestUtil.Builder()
        .withTeamType(TeamType.REGULATOR)
        .withId(randomInteger())
        .build();
    teamView = TeamTestUtil.createTeamView(regulatorTeam);
    accessManager = ServiceUserDetailTestUtil.Builder()
        .withWuaId(new Random().nextLong())
        .build();
    nonAccessManager = ServiceUserDetailTestUtil.Builder()
        .withWuaId(new Random().nextLong())
        .build();
  }

  @SecurityTest
  void renderEditMember_whenNotAuthorised_thenRedirectionToLoginUrl() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(RegulatorEditMemberController.class)
            .renderEditMember(teamView.teamId(), new WebUserAccountId(accessManager.wuaId())))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void renderEditMember_whenNotAccessManager_thenForbidden() throws Exception {

    when(teamMemberService.isMemberOfTeamWithAnyRoleOf(teamView.teamId(), nonAccessManager,
        Set.of(RegulatorTeamRole.ACCESS_MANAGER.name()))
    ).thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(RegulatorEditMemberController.class)
            .renderEditMember(teamView.teamId(), new WebUserAccountId(nonAccessManager.wuaId()))))
            .with(user(nonAccessManager)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void renderEditMember_whenAccessManager_thenOk() throws Exception {
    makeValidRenderEditMemberRequest(Set.of(RegulatorTeamRole.ACCESS_MANAGER));
  }

  @Test
  void renderEditMember_whenAccessManager_andOk_thenAssertModelProperties() throws Exception {

    Set<TeamRole> userRoles = Set.of(
        RegulatorTeamRole.INDUSTRY_ACCESS_MANAGER, RegulatorTeamRole.ACCESS_MANAGER);

    var modelAndView = makeValidRenderEditMemberRequest(userRoles)
        .getModelAndView();

    assertThat(modelAndView).isNotNull();

    var model = modelAndView.getModelMap();

    var expectedRoles = userRoles.stream()
        .map(TeamRole::name)
        .collect(Collectors.toSet());

    assertThat((TeamMemberRolesForm) model.get("form")).extracting("roles")
        .isEqualTo(expectedRoles);

    assertThat(model).containsKeys("pageTitle")
        .extractingByKeys("roles", "backLinkUrl")
        .containsExactly(
            DisplayableEnumOptionUtil.getDisplayableOptionsWithDescription(RegulatorTeamRole.class),
            ReverseRouter.route(on(RegulatorTeamManagementController.class).renderMemberList(teamView.teamId()))
        );
  }

  private MvcResult makeValidRenderEditMemberRequest(Set<TeamRole> teamMemberRoles) throws Exception {
    when(teamService.getTeam(teamView.teamId(), RegulatorAddMemberController.TEAM_TYPE))
        .thenReturn(Optional.of(regulatorTeam));

    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamType(TeamType.REGULATOR)
        .withTeamId(teamView.teamId())
        .withWebUserAccountId(accessManager.wuaId())
        .withRoles(teamMemberRoles)
        .build();

    when(teamMemberService.getUserAsTeamMembers(accessManager)).thenReturn(List.of(teamMember));
    when(teamMemberService.isMemberOfTeam(teamView.teamId(), accessManager)).thenReturn(true);

    when(teamMemberService.getTeamMember(regulatorTeam, teamMember.wuaId()))
        .thenReturn(Optional.of(teamMember));

    var teamMemberView = TeamMemberViewTestUtil.Builder()
        .withRoles(teamMemberRoles)
        .withWebUserAccountId(teamMember.wuaId())
        .build();
    when(teamMemberViewService.getTeamMemberView(teamMember)).thenReturn(Optional.of(teamMemberView));

    return mockMvc.perform(get(ReverseRouter.route(on(RegulatorEditMemberController.class)
            .renderEditMember(teamView.teamId(), teamMember.wuaId())))
            .with(user(accessManager)))
        .andExpect(status().isOk())
        .andReturn();
  }

  @Test
  void renderEditMember_whenAccessManagerAndTeamMemberDoesntExist_thenClientError() throws Exception {
    Set<TeamRole> teamMemberRoles = Set.of(RegulatorTeamRole.ACCESS_MANAGER);

    when(teamService.getTeam(teamView.teamId(), RegulatorAddMemberController.TEAM_TYPE))
        .thenReturn(Optional.of(regulatorTeam));

    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamType(TeamType.REGULATOR)
        .withTeamId(teamView.teamId())
        .withWebUserAccountId(accessManager.wuaId())
        .withRoles(teamMemberRoles)
        .build();

    when(teamMemberService.getUserAsTeamMembers(accessManager)).thenReturn(List.of(teamMember));
    when(teamMemberService.isMemberOfTeam(teamView.teamId(), accessManager)).thenReturn(true);

    when(teamMemberService.getTeamMember(regulatorTeam, teamMember.wuaId()))
        .thenReturn(Optional.empty());

    mockMvc.perform(get(ReverseRouter.route(on(RegulatorEditMemberController.class)
            .renderEditMember(teamView.teamId(), teamMember.wuaId())))
            .with(user(accessManager)))
        .andExpect(status().is4xxClientError());
  }

  @Test
  void renderEditMember_whenAccessManagerAndTeamMemberViewDoesntExist_thenClientError() throws Exception {
    Set<TeamRole> teamMemberRoles = Set.of(RegulatorTeamRole.ACCESS_MANAGER);

    when(teamService.getTeam(teamView.teamId(), RegulatorAddMemberController.TEAM_TYPE))
        .thenReturn(Optional.of(regulatorTeam));

    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamType(TeamType.REGULATOR)
        .withTeamId(teamView.teamId())
        .withWebUserAccountId(accessManager.wuaId())
        .withRoles(teamMemberRoles)
        .build();

    when(teamMemberService.getUserAsTeamMembers(accessManager)).thenReturn(List.of(teamMember));
    when(teamMemberService.isMemberOfTeam(teamView.teamId(), accessManager)).thenReturn(true);

    when(teamMemberService.getTeamMember(regulatorTeam, teamMember.wuaId()))
        .thenReturn(Optional.of(teamMember));

    when(teamMemberViewService.getTeamMemberView(teamMember)).thenReturn(Optional.empty());

    mockMvc.perform(get(ReverseRouter.route(on(RegulatorEditMemberController.class)
            .renderEditMember(teamView.teamId(), teamMember.wuaId())))
            .with(user(accessManager)))
        .andExpect(status().is4xxClientError());
  }

  @SecurityTest
  void editMember_whenNotAuthorized_thenIsForbidden() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(RegulatorEditMemberController.class)
            .renderEditMember(teamView.teamId(), new WebUserAccountId(accessManager.wuaId())))))
        .andExpect(status().isForbidden());
  }

}