package uk.co.nstauthority.fieldconsents.authorisation;

import java.util.Collection;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.teams.TeamId;
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

    var teamMembers = teamMemberService.getUserAsTeamMembers(user);

    if (teamMembers == null) {
      return false;
    }

    return teamMembers
        .stream()
        .map(TeamMember::roles)
        .flatMap(Collection::stream)
        .map(TeamRole::getRolePermissions)
        .flatMap(Collection::stream)
        .anyMatch(requiredPermissions::contains);
  }

  public boolean hasPermissionForTeam(TeamId teamId, ServiceUserDetail user, Collection<RolePermission> requiredPermissions) {
    var teamMembers = teamMemberService.getUserAsTeamMembers(user);

    if (teamMembers == null) {
      return false;
    }

    return teamMembers
        .stream()
        .filter(teamMember -> teamMember.teamView().teamId().equals(teamId))
        .map(TeamMember::roles)
        .flatMap(Collection::stream)
        .map(TeamRole::getRolePermissions)
        .flatMap(Collection::stream)
        .anyMatch(requiredPermissions::contains);
  }
}
