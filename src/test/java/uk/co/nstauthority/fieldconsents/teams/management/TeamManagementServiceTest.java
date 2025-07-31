
package uk.co.nstauthority.fieldconsents.teams.management;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.energyportal.accounts.starter.EnergyPortalServiceAccessService;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.client.user.UserApi;
import uk.co.fivium.energyportalapi.generated.client.UserProjectionRoot;
import uk.co.fivium.energyportalapi.generated.types.User;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.teams.Role;
import uk.co.nstauthority.fieldconsents.teams.Team;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberViewTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamQueryService;
import uk.co.nstauthority.fieldconsents.teams.TeamRepository;
import uk.co.nstauthority.fieldconsents.teams.TeamRole;
import uk.co.nstauthority.fieldconsents.teams.TeamRoleRepository;
import uk.co.nstauthority.fieldconsents.teams.TeamScopeReference;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@ExtendWith(MockitoExtension.class)
class TeamManagementServiceTest {

  private static final long USER_1_WUA_ID = 1;
  private static final long USER_2_WUA_ID = 2;

  private static final User USER_1 = User.newBuilder()
      .webUserAccountId(Math.toIntExact(USER_1_WUA_ID))
      .title("Ms")
      .forename("User")
      .surname("One")
      .primaryEmailAddress("one@example.com")
      .telephoneNumber("1")
      .canLogin(true)
      .isAccountShared(false)
      .build();

  private static final ServiceUserDetail USER_DETAIL_1 = ServiceUserDetailTestUtil.Builder()
      .withWuaId(USER_1_WUA_ID)
      .build();

  private static Team regTeam;
  private static Team orgTeam1;
  private static Team orgTeam2;

  private static TeamRole regTeamUser1RoleManage;
  private static TeamRole regTeamUser1RoleOrgAdmin;
  private static TeamRole regTeamUser2RoleOrgAdmin;

  private static TeamRole orgTeam1User1RoleManage;
  private static TeamRole orgTeam2User1RoleManage;

  @Mock
  private TeamRepository teamRepository;

  @Mock
  private TeamRoleRepository teamRoleRepository;

  @Mock
  private TeamQueryService teamQueryService;

  @Mock
  private UserApi userApi;

  @Mock
  private EnergyPortalServiceAccessService energyPortalServiceAccessService;

  @InjectMocks
  private TeamManagementService teamManagementService;

  @Captor
  private ArgumentCaptor<Team> teamArgumentCaptor;

  @Captor
  private ArgumentCaptor<List<TeamRole>> teamRoleListCaptor;

  @BeforeAll
  public static void setUp() {

    regTeam = new Team(UUID.randomUUID());
    regTeam.setTeamType(TeamType.REGULATOR);
    regTeamUser1RoleManage = new TeamRole();
    regTeamUser1RoleManage.setTeam(regTeam);
    regTeamUser1RoleManage.setWuaId(USER_1_WUA_ID);
    regTeamUser1RoleManage.setRole(Role.ACCESS_MANAGER);

    regTeamUser1RoleOrgAdmin = new TeamRole();
    regTeamUser1RoleOrgAdmin.setTeam(regTeam);
    regTeamUser1RoleOrgAdmin.setWuaId(USER_1_WUA_ID);
    regTeamUser1RoleOrgAdmin.setRole(Role.INDUSTRY_ACCESS_MANAGER);

    regTeamUser2RoleOrgAdmin = new TeamRole();
    regTeamUser2RoleOrgAdmin.setTeam(regTeam);
    regTeamUser2RoleOrgAdmin.setWuaId(USER_2_WUA_ID);
    regTeamUser2RoleOrgAdmin.setRole(Role.INDUSTRY_ACCESS_MANAGER);

    orgTeam1 = new Team(UUID.randomUUID());
    orgTeam1.setTeamType(TeamType.INDUSTRY);
    orgTeam1User1RoleManage = new TeamRole();
    orgTeam1User1RoleManage.setTeam(orgTeam1);
    orgTeam1User1RoleManage.setWuaId(USER_1_WUA_ID);
    orgTeam1User1RoleManage.setRole(Role.ACCESS_MANAGER);

    orgTeam2 = new Team(UUID.randomUUID());
    orgTeam2.setTeamType(TeamType.INDUSTRY);
    orgTeam2User1RoleManage = new TeamRole();
    orgTeam2User1RoleManage.setTeam(orgTeam2);
    orgTeam2User1RoleManage.setWuaId(USER_1_WUA_ID);
    orgTeam2User1RoleManage.setRole(Role.ACCESS_MANAGER);
  }

  @Test
  void createScopedTeam() {
    var scopeRef = TeamScopeReference.from("1", TeamScopeReference.ORGANISATION_GROUP_ID);

    teamManagementService.createScopedTeam("foo", TeamType.INDUSTRY, scopeRef);

    verify(teamRepository).save(teamArgumentCaptor.capture());
    var newTeam = teamArgumentCaptor.getValue();

    assertThat(newTeam.getName()).isEqualTo("foo");
    assertThat(newTeam.getTeamType()).isEqualTo(TeamType.INDUSTRY);
    assertThat(newTeam.getScopeType()).isEqualTo(scopeRef.getType());
    assertThat(newTeam.getScopeId()).isEqualTo(scopeRef.getId());
  }

  @Test
  void createScopedTeam_wrongType() {
    var scopeRef = TeamScopeReference.from("1", TeamScopeReference.ORGANISATION_GROUP_ID);

    assertThatExceptionOfType(TeamManagementException.class)
        .isThrownBy(() -> teamManagementService.createScopedTeam("foo", TeamType.REGULATOR, scopeRef));
    verify(teamRepository, never()).save(any());
  }

  @Test
  void createScopedTeam_alreadyExists() {
    var scopeRef = TeamScopeReference.from("1", TeamScopeReference.ORGANISATION_GROUP_ID);

    when(teamRepository.findByTeamTypeAndScopeTypeAndScopeId(TeamType.INDUSTRY, TeamScopeReference.ORGANISATION_GROUP_ID, "1"))
        .thenReturn(Optional.of(orgTeam1));

    assertThatExceptionOfType(TeamManagementException.class)
        .isThrownBy(() -> teamManagementService.createScopedTeam("foo", TeamType.INDUSTRY, scopeRef));

    verify(teamRepository, never()).save(any());
  }

  @Test
  void getTeamTypesUserIsMemberOf() {

    when(teamRoleRepository.findAllByWuaId(USER_1_WUA_ID))
        .thenReturn(List.of(regTeamUser1RoleManage, orgTeam1User1RoleManage, orgTeam2User1RoleManage));

    assertThat(teamManagementService.getTeamTypesUserIsMemberOf(USER_DETAIL_1))
        .containsExactlyInAnyOrder(TeamType.REGULATOR, TeamType.INDUSTRY);
  }

  @Test
  void getStaticTeamOfTypeUserCanManage() {
    when(teamRoleRepository.findByWuaIdAndRole(USER_1_WUA_ID, Role.ACCESS_MANAGER))
        .thenReturn(List.of(regTeamUser1RoleManage, orgTeam1User1RoleManage, orgTeam2User1RoleManage));

    assertThat(teamManagementService.getStaticTeamOfTypeUserCanManage(TeamType.REGULATOR, USER_DETAIL_1))
        .hasValue(regTeam);
  }

  @Test
  void getStaticTeamOfTypeUserCanManage_notStatic() {
    assertThatExceptionOfType(TeamManagementException.class)
        .isThrownBy(() -> teamManagementService.getStaticTeamOfTypeUserCanManage(TeamType.INDUSTRY, USER_DETAIL_1));
  }

  @Test
  void getScopedTeamOfTypeUserCanManage() {
    when(teamRoleRepository.findByWuaIdAndRole(USER_1_WUA_ID, Role.ACCESS_MANAGER))
        .thenReturn(List.of(regTeamUser1RoleManage, orgTeam1User1RoleManage));

    when(teamQueryService.userHasStaticRole(USER_DETAIL_1, TeamType.REGULATOR, Role.INDUSTRY_ACCESS_MANAGER))
        .thenReturn(false);

    assertThat(teamManagementService.getScopedTeamsOfTypeUserCanManage(TeamType.INDUSTRY, USER_DETAIL_1))
        .containsExactlyInAnyOrder(orgTeam1);
  }

  @Test
  void getScopedTeamOfTypeUserCanManage_regulatorWithRoleCanManageAllOrgs() {
    // User has direct manage team role in reg team and org team 1
    when(teamRoleRepository.findByWuaIdAndRole(USER_1_WUA_ID, Role.ACCESS_MANAGER))
        .thenReturn(List.of(regTeamUser1RoleManage, orgTeam1User1RoleManage));

    // User has the special create/manage any org team priv
    when(teamQueryService.userHasStaticRole(USER_DETAIL_1, TeamType.REGULATOR, Role.INDUSTRY_ACCESS_MANAGER))
        .thenReturn(true);

    // There are 2 org teams
    when(teamRepository.findByTeamType(TeamType.INDUSTRY))
        .thenReturn(List.of(orgTeam1, orgTeam2));

    // Verify they can manage both org team 1 and 2
    assertThat(teamManagementService.getScopedTeamsOfTypeUserCanManage(TeamType.INDUSTRY, USER_DETAIL_1))
        .containsExactlyInAnyOrder(orgTeam1, orgTeam2);
  }

  @Test
  void getScopedTeamOfTypeUserCanManage_notScoped() {
    assertThatExceptionOfType(TeamManagementException.class)
        .isThrownBy(() -> teamManagementService.getScopedTeamsOfTypeUserCanManage(TeamType.REGULATOR, USER_DETAIL_1));
  }

  @Test
  void getTeam() {
    var uuid = UUID.randomUUID();
    when(teamRepository.findById(uuid))
        .thenReturn(Optional.of(regTeam));

    assertThat(teamManagementService.getTeam(uuid))
        .isEqualTo(Optional.of(regTeam));
  }

  @Test
  void getTeamMemberView() {
    var teamRoles = List.of(regTeamUser1RoleManage, regTeamUser1RoleOrgAdmin);

    when(teamRoleRepository.findByWuaIdAndTeam(USER_1_WUA_ID, regTeam))
        .thenReturn(teamRoles);

    var teamMemberView = TeamMemberViewTestUtil.newBuilder()
        .build();

    when(teamQueryService.getTeamMemberViews(teamRoles))
        .thenReturn(List.of(teamMemberView));

    assertThat(teamManagementService.getTeamMemberView(regTeam, USER_1_WUA_ID)).isEqualTo(teamMemberView);
  }

  @Test
  void getTeamMemberViewsForTeam() {
    var teamRoles = List.of(
        regTeamUser1RoleOrgAdmin,
        regTeamUser1RoleManage,
        regTeamUser2RoleOrgAdmin
    );

    // the list returns roles not in the order declared in the TeamType enum
    when(teamRoleRepository.findByTeam(regTeam))
        .thenReturn(teamRoles);

    var teamMemberViews = List.of(
        TeamMemberViewTestUtil.newBuilder()
            .withTeam(regTeamUser1RoleOrgAdmin.getTeam())
            .withRoles(regTeamUser1RoleOrgAdmin.getRole(), regTeamUser1RoleManage.getRole())
            .withWuaId(regTeamUser1RoleOrgAdmin.getWuaId())
            .build(),
        TeamMemberViewTestUtil.newBuilder()
            .withTeam(regTeamUser2RoleOrgAdmin.getTeam())
            .withRoles(regTeamUser2RoleOrgAdmin.getRole())
            .withWuaId(regTeamUser2RoleOrgAdmin.getWuaId())
            .build()
    );

    when(teamQueryService.getTeamMemberViews(teamRoles)).thenReturn(teamMemberViews);

    assertThat(teamManagementService.getTeamMemberViewsForTeam(regTeam)).isEqualTo(teamMemberViews);
  }

  @Test
  void setUserTeamRoles() {
    var expectedProjection = new UserProjectionRoot()
        .isAccountShared()
        .canLogin();

    when(userApi.findUserById(eq(1), refEq(expectedProjection), any(RequestPurpose.class)))
        .thenReturn(Optional.of(USER_1));
    when(teamRoleRepository.findByTeam(regTeam))
        .thenReturn(List.of(regTeamUser1RoleManage)); // Make doesTeamHaveTeamManager() check return true

    teamManagementService.setUserTeamRoles(USER_1_WUA_ID, regTeam, List.of(Role.ACCESS_MANAGER, Role.INDUSTRY_ACCESS_MANAGER));

    verify(teamRoleRepository).deleteByWuaIdAndTeam(USER_1_WUA_ID, regTeam);
    verify(teamRoleRepository).saveAll(teamRoleListCaptor.capture());

    assertThat(teamRoleListCaptor.getValue()).extracting(TeamRole::getTeam)
        .contains(regTeam, regTeam);
    assertThat(teamRoleListCaptor.getValue()).extracting(TeamRole::getWuaId)
        .contains(USER_1_WUA_ID, USER_1_WUA_ID);
    assertThat(teamRoleListCaptor.getValue()).extracting(TeamRole::getRole)
        .contains(Role.ACCESS_MANAGER, Role.INDUSTRY_ACCESS_MANAGER);
  }

  @Test
  void setUserTeamRoles_isNewUser_andNonFoxIdp() {
    when(userApi.findUserById(anyInt(), any(), any())).thenReturn(Optional.of(USER_1));
    when(teamRoleRepository.findByTeam(regTeam)).thenReturn(List.of(regTeamUser1RoleManage));
    when(teamRoleRepository.findAllByWuaId(USER_1_WUA_ID)).thenReturn(List.of());

    teamManagementService.setUserTeamRoles(USER_1_WUA_ID, regTeam, List.of(Role.ACCESS_MANAGER, Role.INDUSTRY_ACCESS_MANAGER));

    verify(energyPortalServiceAccessService).addUser(USER_1_WUA_ID);
  }

  @Test
  void setUserTeamRoles_isNotNewUser() {
    when(userApi.findUserById(anyInt(), any(), any())).thenReturn(Optional.of(USER_1));
    when(teamRoleRepository.findByTeam(regTeam)).thenReturn(List.of(regTeamUser1RoleManage));
    when(teamRoleRepository.findAllByWuaId(USER_1_WUA_ID)).thenReturn(List.of(regTeamUser1RoleManage));

    teamManagementService.setUserTeamRoles(USER_1_WUA_ID, regTeam, List.of(Role.ACCESS_MANAGER, Role.INDUSTRY_ACCESS_MANAGER));

    verifyNoInteractions(energyPortalServiceAccessService);
  }

  @Test
  void setUserTeamRoles_noTeamManagerLeft() {
    when(userApi.findUserById(eq(1), any(), any(RequestPurpose.class)))
        .thenReturn(Optional.of(USER_1));

    when(teamRoleRepository.findByTeam(regTeam))
        .thenReturn(List.of()); // Make doesTeamHaveTeamManager() check return false

    var roles = List.of(Role.INDUSTRY_ACCESS_MANAGER);
    assertThatExceptionOfType(TeamManagementException.class)
        .isThrownBy(() -> teamManagementService.setUserTeamRoles(USER_1_WUA_ID, regTeam, roles));
  }

  @Test
  void setUserTeamRoles_invalidRoles() {
    var roles = List.of(Role.EDITOR);

    assertThatExceptionOfType(TeamManagementException.class)
        .isThrownBy(() -> teamManagementService.setUserTeamRoles(USER_1_WUA_ID, regTeam, roles));

    verify(teamRoleRepository, never()).deleteByWuaIdAndTeam(any(), any());
    verify(teamRoleRepository, never()).saveAll(any());
  }

  @Test
  void setUserTeamRoles_noEpaUser() {
    when(userApi.findUserById(eq(1), any(), any(RequestPurpose.class)))
        .thenReturn(Optional.empty());

    var roles =  List.of(Role.ACCESS_MANAGER, Role.INDUSTRY_ACCESS_MANAGER);

    assertThatExceptionOfType(TeamManagementException.class)
        .isThrownBy(() -> teamManagementService.setUserTeamRoles(USER_1_WUA_ID, regTeam, roles));

    verify(teamRoleRepository, never()).deleteByWuaIdAndTeam(any(), any());
    verify(teamRoleRepository, never()).saveAll(any());
  }

  @Test
  void setUserTeamRoles_sharedAccount() {
    var epaUser = new User();
    epaUser.setIsAccountShared(true);

    when(userApi.findUserById(eq(1), any(), any(RequestPurpose.class)))
        .thenReturn(Optional.empty());

    var roles = List.of(Role.ACCESS_MANAGER, Role.INDUSTRY_ACCESS_MANAGER);

    assertThatExceptionOfType(TeamManagementException.class)
        .isThrownBy(() -> teamManagementService.setUserTeamRoles(USER_1_WUA_ID, regTeam, roles));

    verify(teamRoleRepository, never()).deleteByWuaIdAndTeam(any(), any());
    verify(teamRoleRepository, never()).saveAll(any());
  }

  @Test
  void setUserTeamRoles_canNotLogin() {
    var epaUser = new User();
    epaUser.setCanLogin(false);

    when(userApi.findUserById(eq(1), any(), any(RequestPurpose.class)))
        .thenReturn(Optional.empty());

    var roles = List.of(Role.ACCESS_MANAGER, Role.INDUSTRY_ACCESS_MANAGER);

    assertThatExceptionOfType(TeamManagementException.class)
        .isThrownBy(() -> teamManagementService.setUserTeamRoles(USER_1_WUA_ID, regTeam, roles));

    verify(teamRoleRepository, never()).deleteByWuaIdAndTeam(any(), any());
    verify(teamRoleRepository, never()).saveAll(any());
  }

  @Test
  void removeUserFromTeam() {
    when(teamRoleRepository.findByTeam(regTeam))
        .thenReturn(List.of(regTeamUser1RoleManage));

    teamManagementService.removeUserFromTeam(USER_2_WUA_ID, regTeam);

    verify(teamRoleRepository).deleteByWuaIdAndTeam(USER_2_WUA_ID, regTeam);
  }

  @Test
  void removeUserFromTeam_removedFromLastTeam_andNonFoxIdp() {
    when(teamRoleRepository.findByTeam(regTeam)).thenReturn(List.of(regTeamUser1RoleManage));
    when(teamRoleRepository.findAllByWuaId(USER_2_WUA_ID)).thenReturn(List.of());

    teamManagementService.removeUserFromTeam(USER_2_WUA_ID, regTeam);

    verify(teamRoleRepository).deleteByWuaIdAndTeam(USER_2_WUA_ID, regTeam);
    verify(energyPortalServiceAccessService).removeUser(USER_2_WUA_ID);
  }

  @Test
  void removeUserFromTeam_removedFromTeamNotLast() {
    when(teamRoleRepository.findByTeam(regTeam)).thenReturn(List.of(regTeamUser1RoleManage));
    when(teamRoleRepository.findAllByWuaId(USER_2_WUA_ID)).thenReturn(List.of(regTeamUser1RoleManage));

    teamManagementService.removeUserFromTeam(USER_2_WUA_ID, regTeam);

    verify(teamRoleRepository).deleteByWuaIdAndTeam(USER_2_WUA_ID, regTeam);
    verify(energyPortalServiceAccessService, never()).removeUser(anyLong());
  }

  @Test
  void removeUserFromTeam_lastTeamManager() {
    when(teamRoleRepository.findByTeam(regTeam))
        .thenReturn(List.of(regTeamUser1RoleManage));

    assertThatExceptionOfType(TeamManagementException.class)
        .isThrownBy(() -> teamManagementService.removeUserFromTeam(USER_1_WUA_ID, regTeam));

    verify(teamRoleRepository, never()).deleteByWuaIdAndTeam(USER_1_WUA_ID, regTeam);
  }

  @Test
  void willManageTeamRoleBePresentAfterMemberRoleUpdate() {
    when(teamRoleRepository.findByTeam(regTeam))
        .thenReturn(List.of(regTeamUser1RoleManage));

    assertThat(teamManagementService.willManageTeamRoleBePresentAfterMemberRoleUpdate(regTeam, USER_2_WUA_ID, List.of(Role.INDUSTRY_ACCESS_MANAGER)))
        .isTrue();
  }

  @Test
  void willManageTeamRoleBePresentAfterMemberRoleUpdate_newRolesIncludeManage() {
    assertThat(teamManagementService.willManageTeamRoleBePresentAfterMemberRoleUpdate(regTeam,
        USER_1_WUA_ID, List.of(Role.ACCESS_MANAGER, Role.INDUSTRY_ACCESS_MANAGER)))
        .isTrue();
  }

  @Test
  void willManageTeamRoleBePresentAfterMemberRoleUpdate_noManageRoleLeft() {
    when(teamRoleRepository.findByTeam(regTeam))
        .thenReturn(List.of(regTeamUser1RoleManage));

    assertThat(teamManagementService.willManageTeamRoleBePresentAfterMemberRoleUpdate(regTeam, USER_1_WUA_ID, List.of(Role.INDUSTRY_ACCESS_MANAGER)))
        .isFalse();
  }

  @Test
  void willManageTeamRoleBePresentAfterMemberRemoval() {
    when(teamRoleRepository.findByTeam(regTeam))
        .thenReturn(List.of(regTeamUser1RoleManage));

    assertThat(teamManagementService.willManageTeamRoleBePresentAfterMemberRemoval(regTeam, USER_2_WUA_ID))
        .isTrue();
  }

  @Test
  void willManageTeamRoleBePresentAfterMemberRemoval_noManageRoleLeft() {
    when(teamRoleRepository.findByTeam(regTeam))
        .thenReturn(List.of(regTeamUser1RoleManage));

    assertThat(teamManagementService.willManageTeamRoleBePresentAfterMemberRemoval(regTeam, USER_1_WUA_ID))
        .isFalse();
  }

  @Test
  void doesScopedTeamWithReferenceExist_existingTeam() {
    var scopeRef = TeamScopeReference.from("1", TeamScopeReference.ORGANISATION_GROUP_ID);

    when(teamRepository.findByTeamTypeAndScopeTypeAndScopeId(TeamType.INDUSTRY, TeamScopeReference.ORGANISATION_GROUP_ID, "1"))
        .thenReturn(Optional.of(orgTeam1));

    assertThat(teamManagementService.doesScopedTeamWithReferenceExist(TeamType.INDUSTRY, scopeRef))
        .isTrue();
  }

  @Test
  void doesScopedTeamWithReferenceExist_noExistingTeam() {
    var scopeRef = TeamScopeReference.from("1", TeamScopeReference.ORGANISATION_GROUP_ID);

    when(teamRepository.findByTeamTypeAndScopeTypeAndScopeId(TeamType.INDUSTRY, TeamScopeReference.ORGANISATION_GROUP_ID, "1"))
        .thenReturn(Optional.empty());

    assertThat(teamManagementService.doesScopedTeamWithReferenceExist(TeamType.INDUSTRY, scopeRef))
        .isFalse();
  }

  @Test
  void userCanManageAnyOrganisationTeam_whenHasRole_thenTrue() {

    when(teamQueryService.userHasStaticRole(USER_DETAIL_1, TeamType.REGULATOR, Role.INDUSTRY_ACCESS_MANAGER))
        .thenReturn(true);

    assertThat(teamManagementService.userCanManageAnyOrganisationTeam(USER_DETAIL_1)).isTrue();
  }

  @Test
  void userCanManageAnyOrganisationTeam_whenNoRole_thenFalse() {

    when(teamQueryService.userHasStaticRole(USER_DETAIL_1, TeamType.REGULATOR, Role.INDUSTRY_ACCESS_MANAGER))
        .thenReturn(false);

    assertThat(teamManagementService.userCanManageAnyOrganisationTeam(USER_DETAIL_1)).isFalse();
  }

  @Test
  void isMemberOfTeam_whenMemberOfTeam_thenTrue() {

    when(teamRoleRepository.existsByTeamAndWuaId(regTeam, USER_1_WUA_ID))
        .thenReturn(true);

    assertThat(teamManagementService.isMemberOfTeam(regTeam, USER_DETAIL_1)).isTrue();
  }

  @Test
  void isMemberOfTeam_whenNotMemberOfTeam_thenFalse() {

    when(teamRoleRepository.existsByTeamAndWuaId(regTeam, USER_1_WUA_ID))
        .thenReturn(false);

    assertThat(teamManagementService.isMemberOfTeam(regTeam, USER_DETAIL_1)).isFalse();
  }

  @Test
  void canManageTeam_whenScopedTeam_andCanManageTeam_thenTrue() {

    var scopedTeam = new Team(UUID.randomUUID());
    scopedTeam.setTeamType(TeamType.INDUSTRY);

    var teamRole = new TeamRole();
    teamRole.setTeam(scopedTeam);
    teamRole.setRole(Role.ACCESS_MANAGER);

    when(teamRoleRepository.findByWuaIdAndRole(USER_1_WUA_ID, Role.ACCESS_MANAGER))
        .thenReturn(List.of(teamRole));

    assertThat(teamManagementService.canManageTeam(scopedTeam, USER_DETAIL_1)).isTrue();
  }

  @Test
  void canManageTeam_whenScopedTeam_andCannotManageTeam_thenFalse() {

    var scopedTeam = new Team(UUID.randomUUID());
    scopedTeam.setTeamType(TeamType.INDUSTRY);

    when(teamRoleRepository.findByWuaIdAndRole(USER_1_WUA_ID, Role.ACCESS_MANAGER))
        .thenReturn(List.of());

    assertThat(teamManagementService.canManageTeam(scopedTeam, USER_DETAIL_1)).isFalse();
  }

  @Test
  void canManageTeam_whenOrganisationScopedTeam_andCannotManageTeam_andHasManageAnyOrganisationTeamRole_thenTrue() {

    // GIVEN a scoped organisation team
    var scopedTeam = new Team(UUID.randomUUID());
    scopedTeam.setTeamType(TeamType.INDUSTRY);

    // AND the user doesn't have the manage team permission in that team
    when(teamRoleRepository.findByWuaIdAndRole(USER_1_WUA_ID, Role.ACCESS_MANAGER))
        .thenReturn(List.of());

    // WHEN the user has the CREATE_MANAGE_ANY_ORGANISATION_TEAM role in the regulator team
    when(teamQueryService.userHasStaticRole(USER_DETAIL_1, TeamType.REGULATOR, Role.INDUSTRY_ACCESS_MANAGER))
        .thenReturn(true);

    when(teamRepository.findByTeamType(TeamType.INDUSTRY))
        .thenReturn(List.of(scopedTeam));

    // THEN the user can manage the team
    assertThat(teamManagementService.canManageTeam(scopedTeam, USER_DETAIL_1)).isTrue();
  }

  @Test
  void canManageTeam_whenStaticTeam_andCanManageTeam_thenTrue() {

    var staticTeam = new Team((UUID.randomUUID()));
    staticTeam.setTeamType(TeamType.REGULATOR);

    var teamRole = new TeamRole();
    teamRole.setTeam(staticTeam);
    teamRole.setRole(Role.ACCESS_MANAGER);

    when(teamRoleRepository.findByWuaIdAndRole(USER_1_WUA_ID, Role.ACCESS_MANAGER))
        .thenReturn(List.of(teamRole));

    assertThat(teamManagementService.canManageTeam(staticTeam, USER_DETAIL_1)).isTrue();
  }

  @Test
  void canManageTeam_whenStaticTeam_andCannotManageTeam_thenFalse() {

    var staticTeam = new Team((UUID.randomUUID()));
    staticTeam.setTeamType(TeamType.REGULATOR);

    when(teamRoleRepository.findByWuaIdAndRole(USER_1_WUA_ID, Role.ACCESS_MANAGER))
        .thenReturn(List.of());

    assertThat(teamManagementService.canManageTeam(staticTeam, USER_DETAIL_1)).isFalse();
  }

  @Test
  void getStaticTeamOfTypeUserIsMemberOf_whenScopedTeamType_thenException() {

    var scopedTeamType = TeamType.INDUSTRY;

    assertThatThrownBy(() -> teamManagementService.getStaticTeamOfTypeUserIsMemberOf(scopedTeamType, USER_DETAIL_1))
        .isInstanceOf(TeamManagementException.class);
  }

  @Test
  void getStaticTeamOfTypeUserIsMemberOf_whenNotMemberOfTeamOfType_thenEmptyOptional() {

    var staticTeamType = TeamType.REGULATOR;

    when(teamRoleRepository.findAllByWuaId(USER_1_WUA_ID))
        .thenReturn(List.of());

    var resultingTeam = teamManagementService.getStaticTeamOfTypeUserIsMemberOf(staticTeamType, USER_DETAIL_1);

    assertThat(resultingTeam).isEmpty();
  }

  @Test
  void getStaticTeamOfTypeUserIsMemberOf_whenMemberOfTeamOfType_thenTeamReturned() {

    var staticTeamType = TeamType.REGULATOR;

    var expectedTeam = new Team(UUID.randomUUID());
    expectedTeam.setTeamType(staticTeamType);

    var teamRole = new TeamRole();
    teamRole.setTeam(expectedTeam);

    when(teamRoleRepository.findAllByWuaId(USER_1_WUA_ID))
        .thenReturn(List.of(teamRole));

    var resultingTeam = teamManagementService.getStaticTeamOfTypeUserIsMemberOf(staticTeamType, USER_DETAIL_1);

    assertThat(resultingTeam).contains(expectedTeam);
  }

  @Test
  void getScopedTeamsOfTypeUserIsMemberOf_whenStaticTeamType_thenException() {

    var staticTeamType = TeamType.REGULATOR;

    assertThatThrownBy(() -> teamManagementService.getScopedTeamsOfTypeUserIsMemberOf(staticTeamType, USER_DETAIL_1))
        .isInstanceOf(TeamManagementException.class);
  }

  @Test
  void getScopedTeamsOfTypeUserIsMemberOf_whenUserNotMemberOfAnyTeamOfType_thenEmptySetReturned() {

    var scopedTeamType = TeamType.INDUSTRY;

    when(teamRoleRepository.findAllByWuaId(USER_1_WUA_ID))
        .thenReturn(List.of());

    var resultingScopedTeams = teamManagementService.getScopedTeamsOfTypeUserIsMemberOf(scopedTeamType, USER_DETAIL_1);

    assertThat(resultingScopedTeams).isEmpty();
  }

  @Test
  void getScopedTeamsOfTypeUserIsMemberOf_whenUserMemberOfTeamOfType_thenScopedTeamsReturned() {

    var scopedTeamType = TeamType.INDUSTRY;

    var firstTeamOfType = new Team(UUID.randomUUID());
    firstTeamOfType.setTeamType(scopedTeamType);

    var firstRoleForFirstTeam = new TeamRole();
    firstRoleForFirstTeam.setTeam(firstTeamOfType);
    firstRoleForFirstTeam.setRole(Role.ACCESS_MANAGER);

    var secondRoleForFirstTeam = new TeamRole();
    secondRoleForFirstTeam.setTeam(firstTeamOfType);
    secondRoleForFirstTeam.setRole(Role.INDUSTRY_ACCESS_MANAGER);

    var secondTeamOfType = new Team(UUID.randomUUID());
    secondTeamOfType.setTeamType(scopedTeamType);

    var firstRoleForSecondTeam = new TeamRole();
    firstRoleForSecondTeam.setTeam(secondTeamOfType);
    firstRoleForSecondTeam.setRole(Role.ACCESS_MANAGER);

    when(teamRoleRepository.findAllByWuaId(USER_1_WUA_ID))
        .thenReturn(List.of(firstRoleForSecondTeam, firstRoleForFirstTeam, secondRoleForFirstTeam));

    var resultingScopedTeams = teamManagementService.getScopedTeamsOfTypeUserIsMemberOf(scopedTeamType, USER_DETAIL_1);

    assertThat(resultingScopedTeams)
        .containsExactlyInAnyOrder(firstTeamOfType, secondTeamOfType);
  }

  @Test
  void getScopedTeamsOfTypeUserIsMemberOf_whenUserHasManageAnyOrganisationTeamRole_thenAllOrganisationTeamsReturned() {

    var scopedTeamType = TeamType.INDUSTRY;

    var teamUserIsMemberOf = new Team(UUID.randomUUID());
    teamUserIsMemberOf.setTeamType(scopedTeamType);

    var roleForTeamUserIsMemberOf = new TeamRole();
    roleForTeamUserIsMemberOf.setTeam(teamUserIsMemberOf);
    roleForTeamUserIsMemberOf.setRole(Role.ACCESS_MANAGER);

    var teamUserIsNotMemberOf = new Team(UUID.randomUUID());
    teamUserIsNotMemberOf.setTeamType(scopedTeamType);

    when(teamRoleRepository.findAllByWuaId(USER_1_WUA_ID))
        .thenReturn(List.of(roleForTeamUserIsMemberOf));

    when(teamQueryService.userHasStaticRole(USER_DETAIL_1, TeamType.REGULATOR, Role.INDUSTRY_ACCESS_MANAGER))
        .thenReturn(true);

    when(teamRepository.findByTeamType(scopedTeamType))
        .thenReturn(List.of(teamUserIsNotMemberOf, teamUserIsMemberOf));

    var resultingScopedTeams = teamManagementService.getScopedTeamsOfTypeUserIsMemberOf(scopedTeamType, USER_DETAIL_1);

    assertThat(resultingScopedTeams)
        .containsExactlyInAnyOrder(teamUserIsNotMemberOf, teamUserIsMemberOf);
  }
}