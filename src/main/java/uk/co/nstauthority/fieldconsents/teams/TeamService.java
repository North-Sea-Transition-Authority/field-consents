package uk.co.nstauthority.fieldconsents.teams;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.PermissionService;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.TeamRole;

@Service
public class TeamService {

  private final TeamRepository teamRepository;
  private final TeamMemberService teamMemberService;
  private final PermissionService permissionService;

  @Autowired
  TeamService(TeamRepository teamRepository,
              TeamMemberService teamMemberService,
              PermissionService permissionService) {
    this.teamRepository = teamRepository;
    this.teamMemberService = teamMemberService;
    this.permissionService = permissionService;
  }

  public Optional<Team> getTeam(TeamId teamId, TeamType teamType) {
    return teamRepository.findByIdAndTeamType(teamId.id(), teamType);
  }

  public List<Team> getTeamsByOrganisationGroupIds(Collection<Integer> organisationGroupIds) {
    return teamRepository.findAllByOrganisationGroupIdIn(organisationGroupIds);
  }

  public List<Team> getTeamsByType(TeamType teamType) {
    return teamRepository.findAllByTeamTypeIn(Collections.singleton(teamType));
  }

  public List<Team> getTeamsOfTypeThatUserBelongsTo(ServiceUserDetail user, TeamType teamType) {
    return teamRepository.findAllTeamsOfTypeThatUserIsMemberOf(user.wuaId(), teamType);
  }

  public List<Team> getTeamsOfTypeThatUserBelongsTo(WebUserAccountId wuaId, TeamType teamType) {
    return teamRepository.findAllTeamsOfTypeThatUserIsMemberOf(wuaId.id(), teamType);
  }

  public boolean isRegulatorUser(ServiceUserDetail user) {
    return !getTeamsOfTypeThatUserBelongsTo(user, TeamType.REGULATOR).isEmpty();
  }

  public boolean isIndustryUser(ServiceUserDetail user) {
    return !getTeamsOfTypeThatUserBelongsTo(user, TeamType.INDUSTRY).isEmpty();
  }

  public boolean isConsulteeUser(ServiceUserDetail user) {
    return !getTeamsOfTypeThatUserBelongsTo(user, TeamType.OPRED).isEmpty();
  }

  public List<Team> getTeamsOfTypeThatUserHasPermissionFor(ServiceUserDetail user,
                                                           TeamType teamType,
                                                           Set<RolePermission> requiredPermissions) {
    return getTeamsOfTypeThatUserBelongsTo(user, teamType)
        .stream()
        .filter(team -> permissionService.hasPermissionForTeam(team, user, requiredPermissions))
        .toList();
  }

  public List<Team> getUserAccessibleTeams(ServiceUserDetail user) {

    var userAsTeamMembers = teamMemberService.getUserAsTeamMembers(user);

    var viewableTeamTypes = new ArrayList<TeamType>();
    if (userHasPermissionInTeamType(RolePermission.MANAGE_INDUSTRY_TEAMS, TeamType.REGULATOR, userAsTeamMembers)) {
      viewableTeamTypes.add(TeamType.INDUSTRY);
    }

    var accessibleTeams = new ArrayList<Team>();
    if (!viewableTeamTypes.isEmpty()) {
      addAccessibleTeams(teamRepository.findAllByTeamTypeIn(viewableTeamTypes), accessibleTeams);
    }
    addAccessibleTeams(teamRepository.findAllTeamsThatUserIsMemberOf(user.wuaId()), accessibleTeams);

    return accessibleTeams;
  }

  public boolean isMemberOfTeam(TeamId teamId, WebUserAccountId webUserAccountId) {
    return teamMemberService.isMemberOfTeam(teamId, webUserAccountId);
  }

  private void addAccessibleTeams(Collection<Team> teamsToAdd, Collection<Team> accessibleTeams) {
    teamsToAdd.forEach(teamToAdd -> {
      if (accessibleTeams.stream().noneMatch(team -> team.toTeamId().equals(teamToAdd.toTeamId()))) {
        accessibleTeams.add(teamToAdd);
      }
    });
  }

  private boolean userHasPermissionInTeamType(RolePermission permission, TeamType teamType,
                                              Collection<TeamMember> userAsTeamMembers) {

    return userAsTeamMembers.stream()
        .filter(teamMember -> teamMember.teamView().teamType().equals(teamType))
        .flatMap(teamMember -> teamMember.roles().stream())
        .flatMap(teamRole -> teamRole.getRolePermissions().stream())
        .anyMatch(rolePermission -> rolePermission.equals(permission));
  }

  public boolean canUserAccessMultipleTeams(ServiceUserDetail user) {
    return getUserAccessibleTeams(user).size() > 1;
  }

  public void createTeam(Team team) {
    teamRepository.save(team);
  }

  public Optional<Team> getTeamByOrganisationGroupId(int organisationGroupId) {
    return teamRepository.findByOrganisationGroupId(organisationGroupId);
  }

  public Set<RolePermission> getUserPermissionsForTeam(Team team, ServiceUserDetail user) {
    return permissionService.getUserPermissionsForTeam(team, user);
  }

  public List<WebUserAccountId> getWuaIdsOfTeamMembersWithRoles(TeamType teamType, Set<TeamRole> teamRoles) {
    return getTeamsByType(teamType)
        .stream()
        .map(teamMemberService::getTeamMembers)
        .flatMap(Collection::stream)
        .filter(teamMember -> teamMember.roles().containsAll(teamRoles))
        .map(TeamMember::wuaId)
        .toList();
  }

  public boolean hasAnyTeamRoleOf(ServiceUserDetail user, TeamType teamType, Set<TeamRole> roles) {
    var teamRoleNames = roles.stream().map(TeamRole::name).collect(Collectors.toSet());
    return getTeamsOfTypeThatUserBelongsTo(user, teamType)
        .stream()
        .anyMatch(team -> teamMemberService.isMemberOfTeamWithAnyRoleOf(team.toTeamId(), user, teamRoleNames));
  }
}
