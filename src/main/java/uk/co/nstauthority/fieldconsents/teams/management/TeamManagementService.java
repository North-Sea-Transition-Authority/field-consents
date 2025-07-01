package uk.co.nstauthority.fieldconsents.teams.management;

import jakarta.transaction.Transactional;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import uk.co.fivium.digital.energyportalteamaccesslibrary.team.EnergyPortalAccessService;
import uk.co.fivium.digital.energyportalteamaccesslibrary.team.InstigatingWebUserAccountId;
import uk.co.fivium.digital.energyportalteamaccesslibrary.team.ResourceType;
import uk.co.fivium.digital.energyportalteamaccesslibrary.team.TargetWebUserAccountId;
import uk.co.fivium.energyportal.accounts.starter.EnergyPortalServiceAccessService;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.client.user.UserApi;
import uk.co.fivium.energyportalapi.generated.client.UserProjectionRoot;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.UserDetailService;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.teams.Role;
import uk.co.nstauthority.fieldconsents.teams.Team;
import uk.co.nstauthority.fieldconsents.teams.TeamQueryService;
import uk.co.nstauthority.fieldconsents.teams.TeamRepository;
import uk.co.nstauthority.fieldconsents.teams.TeamRole;
import uk.co.nstauthority.fieldconsents.teams.TeamRoleRepository;
import uk.co.nstauthority.fieldconsents.teams.TeamScopeReference;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.management.view.TeamMemberView;

@Service
public class TeamManagementService {

  private static final String RESOURCE_TYPE_NAME = "FCS_ACCESS_TEAM";

  private final TeamRepository teamRepository;
  private final TeamRoleRepository teamRoleRepository;
  private final TeamQueryService teamQueryService;
  private final UserApi userApi;
  private final UserDetailService userDetailService;
  private final EnergyPortalAccessService energyPortalAccessService;
  private final EnergyPortalServiceAccessService energyPortalServiceAccessService;
  private final EnergyPortalUserService energyPortalUserService;
  private final Environment environment;

  TeamManagementService(
      TeamRepository teamRepository,
      TeamRoleRepository teamRoleRepository,
      UserApi userApi,
      TeamQueryService teamQueryService,
      UserDetailService userDetailService,
      EnergyPortalAccessService energyPortalAccessService,
      EnergyPortalServiceAccessService energyPortalServiceAccessService,
      EnergyPortalUserService energyPortalUserService,
      Environment environment
  ) {
    this.teamRepository = teamRepository;
    this.teamRoleRepository = teamRoleRepository;
    this.userApi = userApi;
    this.teamQueryService = teamQueryService;
    this.userDetailService = userDetailService;
    this.energyPortalAccessService = energyPortalAccessService;
    this.energyPortalServiceAccessService = energyPortalServiceAccessService;
    this.energyPortalUserService = energyPortalUserService;
    this.environment = environment;
  }

  public Team createScopedTeam(String name, TeamType teamType, TeamScopeReference scopeRef) {
    if (!teamType.isScoped()) {
      throw TeamManagementException.expectedScoped(teamType);
    }

    if (doesScopedTeamWithReferenceExist(teamType, scopeRef)) {
      throw new TeamManagementException("Team of type %s scope type %s and scope id %s already exists"
          .formatted(teamType, scopeRef.getId(), scopeRef.getType()));
    }

    var team = new Team();
    team.setName(name);
    team.setTeamType(teamType);
    team.setScopeType(scopeRef.getType());
    team.setScopeId(scopeRef.getId());

    return teamRepository.save(team);
  }

  Set<TeamType> getTeamTypesUserIsMemberOf(ServiceUserDetail userDetail) {
    return teamRoleRepository.findAllByWuaId(userDetail.wuaId())
        .stream()
        .map(teamRole -> teamRole.getTeam().getTeamType())
        .collect(Collectors.toSet());
  }

  public Optional<Team> getStaticTeamOfTypeUserCanManage(TeamType teamType, ServiceUserDetail userDetail) {
    if (teamType.isScoped()) {
      throw TeamManagementException.expectedStatic(teamType);
    }

    return getTeamsOfTypeUserCanManage(teamType, userDetail).stream().findFirst();
  }

  Optional<Team> getStaticTeamOfTypeUserIsMemberOf(TeamType teamType, ServiceUserDetail userDetail) {
    if (teamType.isScoped()) {
      throw TeamManagementException.expectedStatic(teamType);
    }

    return getTeamsOfTypeUserIsMemberOf(teamType, userDetail)
        .stream()
        .findFirst();
  }

  public Set<Team> getScopedTeamsOfTypeUserCanManage(TeamType teamType, ServiceUserDetail userDetail) {
    if (!teamType.isScoped()) {
      throw TeamManagementException.expectedStatic(teamType);
    }

    var teams = new HashSet<>(getTeamsOfTypeUserCanManage(teamType, userDetail));

    if (teamType.equals(TeamType.INDUSTRY) && userCanManageAnyOrganisationTeam(userDetail)) {
      // If we want org teams, and the user is a regulator who can manage any org team, include all the org teams.
      teams.addAll(getAllScopedTeamsOfType(TeamType.INDUSTRY));
    }

    return teams;
  }

  Set<Team> getScopedTeamsOfTypeUserIsMemberOf(TeamType teamType, ServiceUserDetail userDetail) {
    if (!teamType.isScoped()) {
      throw TeamManagementException.expectedScoped(teamType);
    }

    var teams = new HashSet<>(getTeamsOfTypeUserIsMemberOf(teamType, userDetail));

    if (teamType.equals(TeamType.INDUSTRY) && userCanManageAnyOrganisationTeam(userDetail)) {
      // If we want org teams, and the user is a regulator who can manage any org team, include all the org teams.
      teams.addAll(getAllScopedTeamsOfType(TeamType.INDUSTRY));
    }

    return new HashSet<>(teams);
  }

  public Optional<Team> getTeam(UUID teamId) {
    return teamRepository.findById(teamId);
  }

  public TeamMemberView getTeamMemberView(Team team, Long wuaId) {
    var teamRoles = teamRoleRepository.findByWuaIdAndTeam(wuaId, team);
    var teamMemberViews = teamQueryService.getTeamMemberViews(teamRoles);

    if (teamMemberViews.size() == 1) {
      return teamMemberViews.getFirst();
    }

    if (teamMemberViews.isEmpty()) {
      return teamQueryService.getTeamMemberView(team, wuaId);
    }

    throw new TeamManagementException("Expected exactly 1 team member view, got %d".formatted(teamRoles.size()));
  }

  public List<TeamMemberView> getTeamMemberViewsForTeam(Team team) {
    var allowedRoles = team.getTeamType().getAllowedRoles();
    var teamRoles = teamRoleRepository.findByTeam(team)
        .stream()
        .filter(teamRole -> allowedRoles.contains(teamRole.getRole()))
        .toList();

    return teamQueryService.getTeamMemberViews(teamRoles);
  }

  @Transactional
  public void setUserTeamRoles(Long wuaId, Team team, Collection<Role> roles) {
    if (!new HashSet<>(team.getTeamType().getAllowedRoles()).containsAll(roles)) {
      throw new TeamManagementException("Roles %s are not valid for team type %s".formatted(roles, team.getTeamType()));
    }

    var requestPurpose = new RequestPurpose("Validate user account");
    var projection = new UserProjectionRoot().isAccountShared().canLogin();

    var user = userApi.findUserById(Math.toIntExact(wuaId), projection, requestPurpose)
        .orElseThrow(() -> new TeamManagementException("User account with wuaId %s does not exist".formatted(wuaId)));

    if (Boolean.TRUE.equals(user.getIsAccountShared())) {
      throw new TeamManagementException("User account with wuaId %s is a shared account so can't be added to teams"
          .formatted(wuaId));
    }

    if (!Boolean.TRUE.equals(user.getCanLogin())) {
      throw new TeamManagementException("User account with wuaId %s is not active so can't be added to teams"
          .formatted(wuaId));
    }

    var isNewUser = teamRoleRepository.findAllByWuaId(wuaId).isEmpty();

    teamRoleRepository.deleteByWuaIdAndTeam(wuaId, team);

    var newTeamRoles = roles.stream()
        .map(role -> {
          var teamRole = new TeamRole();
          teamRole.setTeam(team);
          teamRole.setRole(role);
          teamRole.setWuaId(wuaId);
          return teamRole;
        }).toList();

    teamRoleRepository.saveAll(newTeamRoles);

    if (!doesTeamHaveTeamManager(team)) {
      throw new TeamManagementException("At least 1 team manager must exist in team %s".formatted(team.getId()));
    }

    if (isNewUser) {

      if (environment.matchesProfiles("use-epas")) {
        energyPortalServiceAccessService.addUser(wuaId);
      } else {
        energyPortalAccessService.addUserToAccessTeam(
            new ResourceType(RESOURCE_TYPE_NAME),
            new TargetWebUserAccountId(wuaId),
            new InstigatingWebUserAccountId(userDetailService.getUserDetail().wuaId())
        );
      }
    }
  }

  @Transactional
  public void removeUserFromTeam(Long wuaId, Team team) {
    if (!willManageTeamRoleBePresentAfterMemberRemoval(team, wuaId)) {
      throw new TeamManagementException("Can't remove last team manager user %s from team %s".formatted(wuaId, team.getId()));
    }

    teamRoleRepository.deleteByWuaIdAndTeam(wuaId, team);

    if (teamRoleRepository.findAllByWuaId(wuaId).isEmpty()) {

      if (environment.matchesProfiles("use-epas")) {
        energyPortalServiceAccessService.removeUser(wuaId);
      } else {
        energyPortalAccessService.removeUserFromAccessTeam(
            new ResourceType(RESOURCE_TYPE_NAME),
            new TargetWebUserAccountId(wuaId),
            new InstigatingWebUserAccountId(userDetailService.getUserDetail().wuaId())
        );
      }
    }
  }

  public boolean willManageTeamRoleBePresentAfterMemberRoleUpdate(Team team, Long wuaId, Collection<Role> membersNewRoles) {
    if (membersNewRoles.contains(Role.ACCESS_MANAGER)) {
      return true;
    }

    return willManageTeamRoleBePresentAfterMemberRemoval(team, wuaId);
  }

  public boolean willManageTeamRoleBePresentAfterMemberRemoval(Team team, Long wuaId) {
    return teamRoleRepository.findByTeam(team).stream()
        .filter(teamRole -> !teamRole.getWuaId().equals(wuaId))
        .anyMatch(teamRole -> teamRole.getRole().equals(Role.ACCESS_MANAGER));
  }

  public boolean doesScopedTeamWithReferenceExist(TeamType teamType, TeamScopeReference scopeRef) {
    return teamRepository.findByTeamTypeAndScopeTypeAndScopeId(teamType, scopeRef.getType(), scopeRef.getId())
        .isPresent();
  }

  public boolean canManageTeam(Team team, ServiceUserDetail userDetail) {
    if (team.getTeamType().isScoped()) {
      return getScopedTeamsOfTypeUserCanManage(team.getTeamType(), userDetail)
          .stream()
          .anyMatch(scopedTeam -> scopedTeam.getId().equals(team.getId()));
    }

    return getStaticTeamOfTypeUserCanManage(team.getTeamType(), userDetail).isPresent();
  }

  public boolean isMemberOfTeam(Team team, ServiceUserDetail userDetail) {
    return teamRoleRepository.existsByTeamAndWuaId(team, userDetail.wuaId());
  }

  public boolean userCanManageAnyOrganisationTeam(ServiceUserDetail userDetail) {
    return teamQueryService.userHasStaticRole(userDetail, TeamType.REGULATOR, Role.INDUSTRY_ACCESS_MANAGER);
  }

  private List<Team> getAllScopedTeamsOfType(TeamType teamType) {
    if (!teamType.isScoped()) {
      throw new TeamManagementException("TeamType %s is static, expected scoped".formatted(teamType));
    }

    return teamRepository.findByTeamType(teamType);
  }

  private boolean doesTeamHaveTeamManager(Team team) {
    return teamRoleRepository.findByTeam(team).stream()
        .anyMatch(teamRole -> teamRole.getRole().equals(Role.ACCESS_MANAGER));
  }

  private Set<Team> getTeamsUserCanManage(ServiceUserDetail userDetail) {
    var userTeamRoles = teamRoleRepository.findByWuaIdAndRole(userDetail.wuaId(), Role.ACCESS_MANAGER);
    return userTeamRoles.stream()
        .map(TeamRole::getTeam)
        .collect(Collectors.toSet());
  }

  private Set<Team> getTeamsUserIsMemberOf(ServiceUserDetail userDetail) {
    var userTeamRoles = teamRoleRepository.findAllByWuaId(userDetail.wuaId());
    return userTeamRoles.stream()
        .map(TeamRole::getTeam)
        .collect(Collectors.toSet());
  }

  private Set<Team> getTeamsOfTypeUserCanManage(TeamType teamType, ServiceUserDetail userDetail) {
    return getTeamsUserCanManage(userDetail).stream()
        .filter(team -> team.getTeamType().equals(teamType))
        .collect(Collectors.toSet());
  }

  private Set<Team> getTeamsOfTypeUserIsMemberOf(TeamType teamType, ServiceUserDetail userDetail) {
    return getTeamsUserIsMemberOf(userDetail).stream()
        .filter(team -> team.getTeamType().equals(teamType))
        .collect(Collectors.toSet());
  }

}