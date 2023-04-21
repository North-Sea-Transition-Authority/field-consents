package uk.co.nstauthority.fieldconsents.teams;

import java.util.Random;
import uk.co.nstauthority.fieldconsents.exception.IllegalUtilClassInstantiationException;

public class TeamTestUtil {

  public static Integer randomInteger() {
    var random = new Random();
    return random.nextInt();
  }

  public static TeamView createTeamView(Team team) {
    return new TeamView(new TeamId(team.getId()), team.getTeamType(), team.getDisplayName());
  }

  private TeamTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static TeamBuilder Builder() {
    return new TeamBuilder();
  }

  public static class TeamBuilder {

    private Integer id = randomInteger();
    private TeamType teamType = TeamType.REGULATOR;
    private String displayName = "team name";

    private Integer organisationGroupId = randomInteger();

    public TeamBuilder withId(Integer id) {
      this.id = id;
      return this;
    }

    public TeamBuilder withTeamType(TeamType teamType) {
      this.teamType = teamType;
      return this;
    }

    public TeamBuilder withDisplayName(String displayName) {
      this.displayName = displayName;
      return this;
    }

    public TeamBuilder withOrganisationGroupId(Integer id) {
      this.organisationGroupId = id;
      return this;
    }

    public Team build() {
      var team = new Team(id);
      team.setTeamType(teamType);
      team.setDisplayName(displayName);
      team.setOrganisationGroupId(organisationGroupId);
      return team;
    }
  }
}