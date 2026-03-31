package uk.co.nstauthority.fieldconsents.energyportal.usercontext;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import uk.co.nstauthority.fieldconsents.teams.Role;
import uk.co.nstauthority.fieldconsents.teams.TeamRole;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

record UserRoleContext(
    Map<TeamType, Set<Role>> rolesByTeamType
) {
  static UserRoleContext from(List<TeamRole> teamRoles) {
    return new UserRoleContext(teamRoles.stream()
        .collect(Collectors.groupingBy(
            teamRole -> teamRole.getTeam().getTeamType(),
            Collectors.mapping(TeamRole::getRole, Collectors.toSet())
        )));
  }

  boolean hasRole(TeamType type, Role role) {
    return rolesByTeamType.getOrDefault(type, Collections.emptySet()).contains(role);
  }

  boolean hasAnyRole(TeamType type, Set<Role> roles) {
    return !Collections.disjoint(rolesByTeamType.getOrDefault(type, Collections.emptySet()), roles);
  }
}
