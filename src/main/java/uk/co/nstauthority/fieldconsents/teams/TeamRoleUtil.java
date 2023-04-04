package uk.co.nstauthority.fieldconsents.teams;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import uk.co.nstauthority.fieldconsents.exception.IllegalUtilClassInstantiationException;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.TeamRole;

public class TeamRoleUtil {

  private TeamRoleUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Set<String> getRoleNames(Collection<TeamRole> roles) {
    return roles.stream()
        .map(TeamRole::name)
        .collect(Collectors.toSet());
  }

}
