package uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDtoTestUtil;
import uk.co.nstauthority.fieldconsents.teams.Team;
import uk.co.nstauthority.fieldconsents.teams.TeamId;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberRoleService;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberService;
import uk.co.nstauthority.fieldconsents.teams.TeamService;
import uk.co.nstauthority.fieldconsents.teams.TeamTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@ExtendWith(MockitoExtension.class)
class RegulatorTeamServiceTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  private static final WebUserAccountId WEB_USER_ACCOUNT_ID = WebUserAccountId.from(USER);

  @Mock
  private TeamService teamService;

  @Mock
  private TeamMemberService teamMemberService;

  @Mock
  private TeamMemberRoleService teamMemberRoleService;

  @InjectMocks
  private RegulatorTeamService regulatorTeamService;

  private Team team;

  private TeamId teamId;

  @BeforeEach
  void setUp() {
    team = TeamTestUtil.Builder().build();
    teamId = new TeamId(team.getId());
  }

  @Test
  void getRegulatorTeamForUser_whenUserBelongsToRegulatorTeam_thenReturnRegulatorTeam() {
    when(teamService.getTeamsOfTypeThatUserBelongsTo(USER, TeamType.REGULATOR))
        .thenReturn(List.of(team));

    var result = regulatorTeamService.getRegulatorTeamForUser(USER);

    assertThat(result).contains(team);
  }

  @Test
  void getRegulatorTeamForUser_whenUserDoesNotBelongToRegulatorTeam_thenReturnEmpty() {
    when(teamService.getTeamsOfTypeThatUserBelongsTo(USER, TeamType.REGULATOR))
        .thenReturn(Collections.emptyList());

    var result = regulatorTeamService.getRegulatorTeamForUser(USER);

    assertThat(result).isEmpty();
  }

  @Test
  void getTeam_whenMatch_thenReturnTeam() {
    when(teamService.getTeam(teamId, TeamType.REGULATOR)).thenReturn(Optional.of(team));

    assertThat(regulatorTeamService.getTeam(teamId)).contains(team);
    verify(teamService, times(1)).getTeam(teamId, team.getTeamType());
  }

  @Test
  void getTeam_whenNoMatch_thenEmptyOptional() {
    when(teamService.getTeam(teamId, TeamType.REGULATOR)).thenReturn(Optional.empty());

    assertThat(regulatorTeamService.getTeam(teamId)).isEmpty();
    verify(teamService, times(1)).getTeam(teamId, team.getTeamType());
  }

  @Test
  void isAccessManager_whenAccessManager_thenTrue() {
    when(teamMemberService.isMemberOfTeamWithAnyRoleOf(teamId, USER, Set.of(RegulatorTeamRole.ACCESS_MANAGER.name())))
        .thenReturn(true);

    assertTrue(regulatorTeamService.isAccessManager(teamId, USER));
  }

  @Test
  void isAccessManager_whenAccessManager_thenFalse() {
    when(teamMemberService.isMemberOfTeamWithAnyRoleOf(teamId, USER, Set.of(RegulatorTeamRole.ACCESS_MANAGER.name())))
        .thenReturn(false);

    assertFalse(regulatorTeamService.isAccessManager(teamId, USER));
  }

  @Test
  void isCaseOfficer_whenCaseOfficer_thenTrue() {
    when(teamService.getTeamsOfTypeThatUserBelongsTo(WEB_USER_ACCOUNT_ID, TeamType.REGULATOR))
        .thenReturn(List.of(team));
    when(teamMemberService.isMemberOfTeamWithAnyRoleOf(teamId, WEB_USER_ACCOUNT_ID, Set.of(RegulatorTeamRole.CASE_OFFICER.name())))
        .thenReturn(true);

    assertTrue(regulatorTeamService.isCaseOfficer(WEB_USER_ACCOUNT_ID));
  }

  @Test
  void isCaseOfficer_whenNotCaseOfficer_thenFalse() {
    when(teamService.getTeamsOfTypeThatUserBelongsTo(WEB_USER_ACCOUNT_ID, TeamType.REGULATOR))
        .thenReturn(List.of(team));
    when(teamMemberService.isMemberOfTeamWithAnyRoleOf(teamId, WEB_USER_ACCOUNT_ID, Set.of(RegulatorTeamRole.CASE_OFFICER.name())))
        .thenReturn(false);

    assertFalse(regulatorTeamService.isCaseOfficer(WEB_USER_ACCOUNT_ID));
  }

  @Test
  void isTechnicalReviewer_whenNoTeamsOfRegulatorType_thenFalse() {
    when(teamService.getTeamsOfTypeThatUserBelongsTo(WEB_USER_ACCOUNT_ID, TeamType.REGULATOR))
        .thenReturn(Collections.emptyList());

    assertFalse(regulatorTeamService.isTechnicalReviewer(WEB_USER_ACCOUNT_ID));
  }

  @Test
  void isTechnicalReviewer_whenTechnicalReviewer_thenTrue() {
    when(teamService.getTeamsOfTypeThatUserBelongsTo(WEB_USER_ACCOUNT_ID, TeamType.REGULATOR))
        .thenReturn(List.of(team));
    when(teamMemberService.isMemberOfTeamWithAnyRoleOf(teamId, WEB_USER_ACCOUNT_ID, Set.of(RegulatorTeamRole.TECHNICAL_REVIEWER.name())))
        .thenReturn(true);

    assertTrue(regulatorTeamService.isTechnicalReviewer(WEB_USER_ACCOUNT_ID));
  }

  @Test
  void isTechnicalReviewer_whenNotTechnicalReviewer_thenFalse() {
    when(teamService.getTeamsOfTypeThatUserBelongsTo(WEB_USER_ACCOUNT_ID, TeamType.REGULATOR))
        .thenReturn(List.of(team));
    when(teamMemberService.isMemberOfTeamWithAnyRoleOf(teamId, WEB_USER_ACCOUNT_ID, Set.of(RegulatorTeamRole.TECHNICAL_REVIEWER.name())))
        .thenReturn(false);

    assertFalse(regulatorTeamService.isTechnicalReviewer(WEB_USER_ACCOUNT_ID));
  }

  @Test
  void isCaseOfficer_whenNoTeamsOfRegulatorType_thenFalse() {
    when(teamService.getTeamsOfTypeThatUserBelongsTo(WEB_USER_ACCOUNT_ID, TeamType.REGULATOR))
        .thenReturn(Collections.emptyList());

    assertFalse(regulatorTeamService.isCaseOfficer(WEB_USER_ACCOUNT_ID));
  }

  @Test
  void addUserTeamRoles_verifyRepositoryInteractions() {
    var userToAdd = EnergyPortalUserDtoTestUtil.Builder().build();
    var regulatorRoles = Set.of(
        RegulatorTeamRole.ACCESS_MANAGER,
        RegulatorTeamRole.INDUSTRY_ACCESS_MANAGER
    );

    regulatorTeamService.addUserTeamRoles(team, userToAdd, regulatorRoles);

    var rolesAsStrings = regulatorRoles
        .stream()
        .map(RegulatorTeamRole::name)
        .collect(Collectors.toSet());

    verify(teamMemberRoleService, times(1)).addUserTeamRoles(team, userToAdd, rolesAsStrings);
  }

  @Test
  void isCamUser_whenCamUser_thenTrue() {
    when(teamService.getTeamsOfTypeThatUserBelongsTo(WEB_USER_ACCOUNT_ID, TeamType.REGULATOR))
        .thenReturn(List.of(team));
    when(teamMemberService.isMemberOfTeamWithAnyRoleOf(teamId, WEB_USER_ACCOUNT_ID, Set.of(RegulatorTeamRole.CONSENTS_AND_AUTHORISATIONS_MANAGER.name())))
        .thenReturn(true);

    assertTrue(regulatorTeamService.isCamUser(WEB_USER_ACCOUNT_ID));
  }

  @Test
  void isCamUser_whenNotCamUser_thenFalse() {
    when(teamService.getTeamsOfTypeThatUserBelongsTo(WEB_USER_ACCOUNT_ID, TeamType.REGULATOR))
        .thenReturn(List.of(team));
    when(teamMemberService.isMemberOfTeamWithAnyRoleOf(teamId, WEB_USER_ACCOUNT_ID, Set.of(RegulatorTeamRole.CONSENTS_AND_AUTHORISATIONS_MANAGER.name())))
        .thenReturn(false);

    assertFalse(regulatorTeamService.isCamUser(WEB_USER_ACCOUNT_ID));
  }

  @Test
  void isCamUser_whenNoTeamsOfRegulatorType_thenFalse() {
    when(teamService.getTeamsOfTypeThatUserBelongsTo(WEB_USER_ACCOUNT_ID, TeamType.REGULATOR))
        .thenReturn(Collections.emptyList());

    assertFalse(regulatorTeamService.isCaseOfficer(WEB_USER_ACCOUNT_ID));
  }
}
