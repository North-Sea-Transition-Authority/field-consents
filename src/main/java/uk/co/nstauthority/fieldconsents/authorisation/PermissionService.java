package uk.co.nstauthority.fieldconsents.authorisation;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.teams.Team;
import uk.co.nstauthority.fieldconsents.teams.TeamMember;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberService;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.TeamRole;

@Service
public class PermissionService {

  private final TeamMemberService teamMemberService;

  @Autowired
  public PermissionService(TeamMemberService teamMemberService) {
    this.teamMemberService = teamMemberService;
  }

  public boolean hasPermission(ServiceUserDetail user, Set<RolePermission> requiredPermissions) {
    return getUserPermissionsForPredicate(user, teamMember -> true) // implies any team
        .stream()
        .anyMatch(requiredPermissions::contains);
  }

  public boolean hasPermissionForTeam(Team team, ServiceUserDetail user, Collection<RolePermission> requiredPermissions) {
    return getUserPermissionsForTeam(team, user)
        .stream()
        .anyMatch(requiredPermissions::contains);
  }

  public Set<RolePermission> getUserPermissionsForTeam(Team team, ServiceUserDetail user) {
    return getUserPermissionsForPredicate(user, teamMember -> teamMember.teamView().teamId().equals(team.toTeamId()));
  }

  private Set<RolePermission> getUserPermissionsForPredicate(ServiceUserDetail user,
                                                             Predicate<TeamMember> teamMemberPredicate) {
    var teamMembers = teamMemberService.getUserAsTeamMembers(user);

    if (teamMembers == null) {
      return Collections.emptySet();
    }

    return teamMembers
        .stream()
        .filter(teamMemberPredicate)
        .map(TeamMember::roles)
        .flatMap(Collection::stream)
        .map(TeamRole::getRolePermissions)
        .flatMap(Collection::stream)
        .collect(Collectors.toSet());
  }
}
