package uk.co.nstauthority.fieldconsents.teams.management;

import uk.co.nstauthority.fieldconsents.teams.TeamType;

public class TeamManagementException extends RuntimeException {

  public TeamManagementException(String message) {
    super(message);
  }

  public static TeamManagementException expectedStatic(TeamType teamType) {
    return new TeamManagementException("TeamType %s is scoped, expected static".formatted(teamType));
  }

  public static TeamManagementException expectedScoped(TeamType teamType) {
    return new TeamManagementException("TeamType %s is static, expected scoped".formatted(teamType));
  }

}
