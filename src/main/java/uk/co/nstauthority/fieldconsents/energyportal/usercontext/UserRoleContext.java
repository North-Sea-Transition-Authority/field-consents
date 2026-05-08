package uk.co.nstauthority.fieldconsents.energyportal.usercontext;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import uk.co.nstauthority.fieldconsents.teams.Role;
import uk.co.nstauthority.fieldconsents.teams.TeamRole;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

record UserRoleContext(
    Map<TeamType, Set<Role>> rolesByTeamType,
    Map<Role, Set<String>> scopeIdsByRole
) {
  static UserRoleContext from(List<TeamRole> teamRoles) {
    var rolesByTeamType = teamRoles.stream()
        .collect(Collectors.groupingBy(
            teamRole -> teamRole.getTeam().getTeamType(),
            Collectors.mapping(TeamRole::getRole, Collectors.toSet())
        ));

    var scopeIdsByRole = teamRoles.stream()
        .filter(teamRole -> teamRole.getTeam().getScopeId() != null)
        .collect(Collectors.groupingBy(
                TeamRole::getRole,
                Collectors.mapping(teamRole -> teamRole.getTeam().getScopeId(), Collectors.toSet())
        ));

    return new UserRoleContext(rolesByTeamType, scopeIdsByRole);
  }

  boolean hasRole(TeamType type, Role role) {
    return rolesByTeamType.getOrDefault(type, Collections.emptySet()).contains(role);
  }

  boolean hasAnyRole(TeamType type, Set<Role> roles) {
    return !Collections.disjoint(rolesByTeamType.getOrDefault(type, Collections.emptySet()), roles);
  }

  Set<String> scopeIdsForAnyIndustryRole(Collection<Role> roles) {
    var result = new HashSet<String>();
    for (var role : roles) {
      result.addAll(scopeIdsByRole.getOrDefault(role, Collections.emptySet()));
    }
    return result;
  }
}
