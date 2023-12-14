package uk.co.nstauthority.fieldconsents.teams;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.teams.TeamTestUtil.randomInteger;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.industry.IndustryTeamRole.CREATOR;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.industry.IndustryTeamRole.EDITOR;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.industry.IndustryTeamRole.FINANCE_ADMINISTRATOR;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.industry.IndustryTeamRole.SUBMITTER;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.opred.OpredTeamRole.ALLOCATOR;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.opred.OpredTeamRole.RESPONDER;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamRole.ACCESS_MANAGER;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamRole.CASE_MANAGER;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamRole.CASE_OFFICER;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamRole.CONSENTS_AND_AUTHORISATIONS_MANAGER;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamRole.DOCUMENT_TEMPLATE_MANAGER;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamRole.TECHNICAL_REVIEWER;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.authorisation.PermissionService;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.TeamRole;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.industry.IndustryTeamRole;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamRole;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

  @Mock
  private TeamRepository teamRepository;

  @Mock
  private TeamMemberService teamMemberService;

  @Mock
  private PermissionService permissionService;

  @InjectMocks
  private TeamService teamService;

  private ServiceUserDetail user;

  private Team team;

  @BeforeEach
  void setUp() {
    user = ServiceUserDetailTestUtil.Builder().build();
    team = TeamTestUtil.Builder().build();
  }

  @Test
  void getTeam_whenMatch_thenReturnTeam() {

    when(teamRepository.findByIdAndTeamType(team.getId(), team.getTeamType())).thenReturn(Optional.of(team));
    var result = teamService.getTeam(TeamId.valueOf(team.getId()), team.getTeamType());

    assertThat(result).contains(team);
    verify(teamRepository).findByIdAndTeamType(team.getId(), team.getTeamType());
  }

  @Test
  void getTeam_whenNoMatch_thenEmptyOptionalReturned() {
    when(teamRepository.findByIdAndTeamType(team.getId(), team.getTeamType())).thenReturn(Optional.empty());
    var result = teamService.getTeam(TeamId.valueOf(team.getId()), team.getTeamType());

    assertThat(result).isEmpty();
    verify(teamRepository).findByIdAndTeamType(team.getId(), team.getTeamType());
  }

  @ParameterizedTest
  @EnumSource(value = TeamType.class)
  void getTeamsOfTypeThatUserBelongsTo_whenUserIsNotMember_thenNoTeamsReturned(TeamType teamType) {
    when(teamRepository.findAllTeamsOfTypeThatUserIsMemberOf(user.wuaId(), teamType)).thenReturn(List.of());

    var result = teamService.getTeamsOfTypeThatUserBelongsTo(user, teamType);

    assertThat(result).isEmpty();
    verify(teamRepository, times(1)).findAllTeamsOfTypeThatUserIsMemberOf(user.wuaId(), teamType);
  }

  @ParameterizedTest
  @EnumSource(value = TeamType.class)
  void getTeamsOfTypeThatUserBelongsTo_whenUserIsMember_thenTeamsReturned(TeamType teamType) {
    when(teamRepository.findAllTeamsOfTypeThatUserIsMemberOf(user.wuaId(), teamType)).thenReturn(List.of(team));

    var result = teamService.getTeamsOfTypeThatUserBelongsTo(user, teamType);

    assertThat(result).containsExactly(team);
    verify(teamRepository, times(1)).findAllTeamsOfTypeThatUserIsMemberOf(user.wuaId(), teamType);
  }

  @ParameterizedTest
  @EnumSource(value = TeamType.class)
  void getTeamsOfTypeThatUserBelongsTo_wuaId_whenUserIsNotMember_thenNoTeamsReturned(TeamType teamType) {
    when(teamRepository.findAllTeamsOfTypeThatUserIsMemberOf(user.wuaId(), teamType)).thenReturn(List.of());

    var result = teamService.getTeamsOfTypeThatUserBelongsTo(new WebUserAccountId(user.wuaId()), teamType);

    assertThat(result).isEmpty();
    verify(teamRepository, times(1)).findAllTeamsOfTypeThatUserIsMemberOf(user.wuaId(), teamType);
  }

  @ParameterizedTest
  @EnumSource(value = TeamType.class)
  void getTeamsOfTypeThatUserBelongsTo__wuaId_whenUserIsMember_thenTeamsReturned(TeamType teamType) {
    when(teamRepository.findAllTeamsOfTypeThatUserIsMemberOf(user.wuaId(), teamType)).thenReturn(List.of(team));

    var result = teamService.getTeamsOfTypeThatUserBelongsTo(new WebUserAccountId(user.wuaId()), teamType);

    assertThat(result).containsExactly(team);
    verify(teamRepository, times(1)).findAllTeamsOfTypeThatUserIsMemberOf(user.wuaId(), teamType);
  }

  @ParameterizedTest
  @EnumSource(value = TeamType.class)
  void getTeamsOfTypeThatUserHasPermissionFor_whenUserIsNotMember_thenNoTeamsReturned(TeamType teamType) {
    when(teamRepository.findAllTeamsOfTypeThatUserIsMemberOf(user.wuaId(), teamType)).thenReturn(List.of());

    var result = teamService.getTeamsOfTypeThatUserHasPermissionFor(user, teamType, RolePermission.VIEW_PERMISSIONS);

    assertThat(result).isEmpty();
    verify(teamRepository, times(1)).findAllTeamsOfTypeThatUserIsMemberOf(user.wuaId(), teamType);
  }

  @ParameterizedTest
  @EnumSource(value = TeamType.class)
  void getTeamsOfTypeThatUserHasPermissionFor_whenUserIsMemberAndHasPermission_thenTeamsReturned(TeamType teamType) {
    when(teamRepository.findAllTeamsOfTypeThatUserIsMemberOf(user.wuaId(), teamType)).thenReturn(List.of(team));
    when(permissionService.hasPermissionForTeam(team, user, RolePermission.VIEW_PERMISSIONS))
        .thenReturn(true);

    var result = teamService.getTeamsOfTypeThatUserHasPermissionFor(user, teamType, RolePermission.VIEW_PERMISSIONS);

    assertThat(result).containsExactly(team);
    verify(teamRepository, times(1)).findAllTeamsOfTypeThatUserIsMemberOf(user.wuaId(), teamType);
  }

  @ParameterizedTest
  @EnumSource(value = TeamType.class)
  void getTeamsOfTypeThatUserHasPermissionFor_whenUserIsMemberAndDoesntHavePermission_thenNoTeamsReturned(TeamType teamType) {
    when(teamRepository.findAllTeamsOfTypeThatUserIsMemberOf(user.wuaId(), teamType)).thenReturn(List.of(team));
    when(permissionService.hasPermissionForTeam(team, user, RolePermission.VIEW_PERMISSIONS))
        .thenReturn(false);

    var result = teamService.getTeamsOfTypeThatUserHasPermissionFor(user, teamType, RolePermission.VIEW_PERMISSIONS);

    assertThat(result).isEmpty();
    verify(teamRepository, times(1)).findAllTeamsOfTypeThatUserIsMemberOf(user.wuaId(), teamType);
  }

  @Test
  void getUserAccessibleTeams_whenCanManageIndustryTeams_thenIndustryTeamsReturned() {
    var industryTeamManager = TeamMemberTestUtil.Builder()
        .withTeamType(TeamType.REGULATOR)
        .withRole(RegulatorTeamRole.INDUSTRY_ACCESS_MANAGER)
        .build();

    var userOwnTeam = TeamTestUtil.Builder().build();
    var industryTeam = TeamTestUtil.Builder()
        .withTeamType(TeamType.INDUSTRY)
        .build();

    when(teamMemberService.getUserAsTeamMembers(user))
        .thenReturn(List.of(industryTeamManager));

    when(teamRepository.findAllByTeamTypeIn(List.of(TeamType.INDUSTRY)))
        .thenReturn(List.of(industryTeam));

    when(teamRepository.findAllTeamsThatUserIsMemberOf(user.wuaId()))
        .thenReturn(List.of(userOwnTeam));

    var result = teamService.getUserAccessibleTeams(user);

    assertThat(result)
        .containsExactly(industryTeam, userOwnTeam);
  }

  @Test
  void getUserAccessibleTeams_whenOnlyHasAccessToViewOwnTeams_thenOnlyPersonalTeams() {
    var industryTeamManager = TeamMemberTestUtil.Builder()
        .withTeamType(TeamType.REGULATOR)
        .withRole(ACCESS_MANAGER)
        .build();

    var userOwnTeam = TeamTestUtil.Builder().build();

    when(teamMemberService.getUserAsTeamMembers(user))
        .thenReturn(List.of(industryTeamManager));

    when(teamRepository.findAllTeamsThatUserIsMemberOf(user.wuaId()))
        .thenReturn(List.of(userOwnTeam));

    var result = teamService.getUserAccessibleTeams(user);

    assertThat(result)
        .containsExactly(userOwnTeam);

    verify(teamRepository, never()).findAllByTeamTypeIn(any());
  }

  @Test
  void getUserAccessibleTeams_whenHasAccessToViewIndustryTeams_andIsInIndustryTeam_thenVerifyDistinct() {
    var industryTeamManager = TeamMemberTestUtil.Builder()
        .withTeamType(TeamType.REGULATOR)
        .withRole(RegulatorTeamRole.INDUSTRY_ACCESS_MANAGER)
        .build();

    var userOwnTeam = TeamTestUtil.Builder().build();
    var industryTeamId = randomInteger();
    var industryTeam = TeamTestUtil.Builder()
        .withId(industryTeamId)
        .withTeamType(TeamType.INDUSTRY)
        .build();
    var duplicateIndustryTeam = TeamTestUtil.Builder()
        .withId(industryTeamId)
        .build();

    when(teamMemberService.getUserAsTeamMembers(user))
        .thenReturn(List.of(industryTeamManager));

    when(teamRepository.findAllByTeamTypeIn(List.of(TeamType.INDUSTRY)))
        .thenReturn(List.of(industryTeam));

    when(teamRepository.findAllTeamsThatUserIsMemberOf(user.wuaId()))
        .thenReturn(List.of(userOwnTeam, duplicateIndustryTeam));

    var result = teamService.getUserAccessibleTeams(user);

    assertThat(result)
        .containsExactly(industryTeam, userOwnTeam);
  }

  @Test
  void isMemberOfTeam_whenMember_thenTrue() {
    var wuaId = new WebUserAccountId(1);
    var teamId = new TeamId(randomInteger());

    when(teamService.isMemberOfTeam(teamId, wuaId)).thenReturn(true);
    assertThat(teamService.isMemberOfTeam(teamId, wuaId)).isTrue();
  }

  @Test
  void isMemberOfTeam_whenNotMember_thenFalse() {
    var wuaId = new WebUserAccountId(1);
    var teamId = new TeamId(randomInteger());

    when(teamService.isMemberOfTeam(teamId, wuaId)).thenReturn(false);
    assertThat(teamService.isMemberOfTeam(teamId, wuaId)).isFalse();
  }

  @Test
  void canUserAccessMultipleTeams_oneTeam() {
    var industryTeamViewer = TeamMemberTestUtil.Builder()
        .withTeamType(TeamType.INDUSTRY)
        .withRole(IndustryTeamRole.VIEWER)
        .build();

    var industryTeam = TeamTestUtil.Builder()
        .withTeamType(TeamType.INDUSTRY)
        .build();

    when(teamMemberService.getUserAsTeamMembers(user)).thenReturn(List.of(industryTeamViewer));

    when(teamRepository.findAllTeamsThatUserIsMemberOf(user.wuaId()))
        .thenReturn(List.of(industryTeam));

    var result = teamService.canUserAccessMultipleTeams(user);

    assertThat(result).isFalse();
  }

  @Test
  void canUserAccessMultipleTeams_manyTeams() {
    var industryTeamManager = TeamMemberTestUtil.Builder()
        .withTeamType(TeamType.REGULATOR)
        .withRole(RegulatorTeamRole.INDUSTRY_ACCESS_MANAGER)
        .build();

    var industryTeam1 = TeamTestUtil.Builder()
        .withTeamType(TeamType.INDUSTRY)
        .build();

    var industryTeam2 = TeamTestUtil.Builder()
        .withTeamType(TeamType.INDUSTRY)
        .build();

    when(teamMemberService.getUserAsTeamMembers(user)).thenReturn(List.of(industryTeamManager));

    when(teamRepository.findAllByTeamTypeIn(any()))
        .thenReturn(List.of(industryTeam1, industryTeam2));

    var result = teamService.canUserAccessMultipleTeams(user);

    assertThat(result).isTrue();
  }

  @Test
  void createTeam_verifySave() {
    var industryTeam = TeamTestUtil.Builder()
        .withTeamType(TeamType.INDUSTRY)
        .build();

    teamService.createTeam(industryTeam);

    verify(teamRepository, times(1)).save(industryTeam);
  }

  @Test
  void getTeamByOrganisationGroupId_teamExists() {

    var organisationGroupId = 10000;
    var industryTeam = TeamTestUtil.Builder()
        .withTeamType(TeamType.INDUSTRY)
        .withOrganisationGroupId(organisationGroupId)
        .build();

    when(teamRepository.findByOrganisationGroupId(organisationGroupId))
        .thenReturn(Optional.of(industryTeam));

    assertThat(teamService.getTeamByOrganisationGroupId(organisationGroupId))
        .contains(industryTeam);
  }

  @Test
  void getTeamByOrganisationGroupId_teamDoesntExist() {

    var organisationGroupId = 10000;

    when(teamRepository.findByOrganisationGroupId(organisationGroupId))
        .thenReturn(Optional.empty());

    assertThat(teamService.getTeamByOrganisationGroupId(organisationGroupId))
        .isEmpty();
  }

  @Test
  void isRegulatorUser_whenUserIsNotRegulator_thenFalse() {
    when(teamRepository.findAllTeamsOfTypeThatUserIsMemberOf(user.wuaId(), TeamType.REGULATOR)).thenReturn(
        Collections.emptyList());

    assertThat(teamService.isRegulatorUser(user)).isFalse();
  }

  @Test
  void isRegulatorUser_whenUserIsRegulator_thenTrue() {
    when(teamRepository.findAllTeamsOfTypeThatUserIsMemberOf(user.wuaId(), TeamType.REGULATOR)).thenReturn(List.of(team));

    assertThat(teamService.isRegulatorUser(user)).isTrue();
  }

  @Test
  void isIndustryUser_whenUserIsNotIndustry_thenFalse() {
    when(teamRepository.findAllTeamsOfTypeThatUserIsMemberOf(user.wuaId(), TeamType.INDUSTRY)).thenReturn(
        Collections.emptyList());

    assertThat(teamService.isIndustryUser(user)).isFalse();
  }

  @Test
  void isIndustryUser_whenUserIsIndustry_thenTrue() {
    when(teamRepository.findAllTeamsOfTypeThatUserIsMemberOf(user.wuaId(), TeamType.INDUSTRY)).thenReturn(List.of(team));

    assertThat(teamService.isIndustryUser(user)).isTrue();
  }

  @Test
  void isConsulteeUser_whenUserIsNotConsultee_thenFalse() {
    when(teamRepository.findAllTeamsOfTypeThatUserIsMemberOf(user.wuaId(), TeamType.OPRED)).thenReturn(
        Collections.emptyList());

    assertThat(teamService.isConsulteeUser(user)).isFalse();
  }

  @Test
  void isConsulteeUser_whenUserIsConsultee_thenTrue() {
    when(teamRepository.findAllTeamsOfTypeThatUserIsMemberOf(user.wuaId(), TeamType.OPRED)).thenReturn(List.of(team));

    assertThat(teamService.isConsulteeUser(user)).isTrue();
  }

  @Test
  void getUserPermissionsForTeam_whenPermissions_thenPermissionsReturned() {
    var expectedPermissions = Set.of(RolePermission.PROCESS_FCS_APPLICATIONS, RolePermission.VIEW_FCS_APPLICATIONS);
    when(permissionService.getUserPermissionsForTeam(team, user))
        .thenReturn(expectedPermissions);
    assertThat(teamService.getUserPermissionsForTeam(team, user))
        .containsAll(expectedPermissions);
  }

  @Test
  void getUserPermissionsForTeam_whenNoPermissions_thenEmpty() {
    when(permissionService.getUserPermissionsForTeam(team, user))
        .thenReturn(Collections.emptySet());
    assertThat(teamService.getUserPermissionsForTeam(team, user))
        .containsAll(Collections.emptySet());
  }

  @ParameterizedTest
  @EnumSource(TeamType.class)
  void getTeamsByType(TeamType teamType) {
    var team1 = new TeamTestUtil.TeamBuilder().build();
    var team2 = new TeamTestUtil.TeamBuilder().build();

    when(teamRepository.findAllByTeamTypeIn(Collections.singleton(teamType))).thenReturn(List.of(team1, team2));

    assertThat(teamService.getTeamsByType(teamType)).containsExactly(team1, team2);
  }

  @Test
  void getWuaIdsOfTeamMembersWithRoles() {
    var teamMember1 = TeamMemberTestUtil.Builder().withRole(CASE_OFFICER).withWebUserAccountId(1L).build();
    var teamMember2 = TeamMemberTestUtil.Builder().withRole(ACCESS_MANAGER).withWebUserAccountId(2L).build();
    when(teamRepository.findAllByTeamTypeIn(Collections.singleton(team.getTeamType()))).thenReturn(List.of(team));
    when(teamMemberService.getTeamMembers(team)).thenReturn(List.of(teamMember1, teamMember2));

    assertThat(teamService.getWuaIdsOfTeamMembersWithRoles(team.getTeamType(), Set.of(CASE_OFFICER)))
        .containsExactly(WebUserAccountId.from(1L));
  }

  @Test
  void getWuaIdsOfTeamMembersWithRoles_whenTeamNotFound() {
    when(teamRepository.findAllByTeamTypeIn(Collections.singleton(team.getTeamType())))
        .thenReturn(Collections.emptyList());

    assertThat(teamService.getWuaIdsOfTeamMembersWithRoles(team.getTeamType(), Set.of(CASE_OFFICER)))
        .isEmpty();
  }

  @Test
  void getWuaIdsOfTeamMembersWithRoles_whenTeamMembersNotFound() {
    when(teamRepository.findAllByTeamTypeIn(Collections.singleton(team.getTeamType()))).thenReturn(List.of(team));
    when(teamMemberService.getTeamMembers(team)).thenReturn(Collections.emptyList());

    assertThat(teamService.getWuaIdsOfTeamMembersWithRoles(team.getTeamType(), Set.of(CASE_OFFICER)))
        .isEmpty();
  }

  @ParameterizedTest
  @MethodSource("teamToTeamRoles")
  void hasAnyTeamRoleOf_whenUserIsMemberOfNoTeam(TeamType teamType, Team team, Set<TeamRole> teamRoles) {
    when(teamRepository.findAllTeamsOfTypeThatUserIsMemberOf(user.wuaId(), teamType)).thenReturn(List.of());

    assertThat(teamService.hasAnyTeamRoleOf(user, teamType, teamRoles)).isFalse();
  }

  @ParameterizedTest
  @MethodSource("teamToTeamRoles")
  void hasAnyTeamRoleOf_whenTeamMemberIsNotInAnyRoles(TeamType teamType, Team team, Set<TeamRole> teamRoles) {
    when(teamRepository.findAllTeamsOfTypeThatUserIsMemberOf(user.wuaId(), teamType)).thenReturn(List.of(team));

    var teamRoleNames = teamRoles.stream().map(TeamRole::name).collect(Collectors.toSet());
    when(teamMemberService.isMemberOfTeamWithAnyRoleOf(team.toTeamId(), user, teamRoleNames)).thenReturn(false);

    assertThat(teamService.hasAnyTeamRoleOf(user, teamType, teamRoles)).isFalse();
  }

  @ParameterizedTest
  @MethodSource("teamToTeamRoles")
  void hasAnyTeamRoleOf_whenTeamMemberIsInRoles(TeamType teamType, Team team, Set<TeamRole> teamRoles) {
    when(teamRepository.findAllTeamsOfTypeThatUserIsMemberOf(user.wuaId(), teamType)).thenReturn(List.of(team));

    var teamRoleNames = teamRoles.stream().map(TeamRole::name).collect(Collectors.toSet());
    when(teamMemberService.isMemberOfTeamWithAnyRoleOf(team.toTeamId(), user, teamRoleNames)).thenReturn(true);

    assertThat(teamService.hasAnyTeamRoleOf(user, teamType, teamRoles)).isTrue();
  }

  private static Stream<Arguments> teamToTeamRoles() {
    return Stream.of(
        arguments(TeamType.REGULATOR, TeamTestUtil.Builder().build(),
            Set.of(
              CASE_OFFICER,
              CASE_MANAGER,
              TECHNICAL_REVIEWER,
              CONSENTS_AND_AUTHORISATIONS_MANAGER,
              DOCUMENT_TEMPLATE_MANAGER)),
        arguments(TeamType.OPRED, TeamTestUtil.Builder().withTeamType(TeamType.OPRED).build(),
            Set.of(ALLOCATOR, RESPONDER)),
        arguments(TeamType.INDUSTRY, TeamTestUtil.Builder().withTeamType(TeamType.INDUSTRY).build(),
            Set.of(CREATOR, SUBMITTER, EDITOR, FINANCE_ADMINISTRATOR))
    );
  }
}
