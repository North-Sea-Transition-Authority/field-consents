package uk.co.nstauthority.fieldconsents.teams;

import static uk.co.nstauthority.fieldconsents.teams.TeamTestUtil.randomInteger;

import java.util.HashSet;
import java.util.Set;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.exception.IllegalUtilClassInstantiationException;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.TeamRole;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamRole;

public class TeamMemberTestUtil {

  private TeamMemberTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static TeamMemberBuilder Builder() {
    return new TeamMemberBuilder();
  }

  public static class TeamMemberBuilder {

    private WebUserAccountId webUserAccountId = new WebUserAccountId(123);
    private Set<TeamRole> teamRoles = new HashSet<>();
    private TeamId teamId = new TeamId(randomInteger());
    private TeamType teamType = TeamType.REGULATOR;

    public TeamMemberBuilder withWebUserAccountId(long webUserAccountId) {
      this.webUserAccountId = new WebUserAccountId(webUserAccountId);
      return this;
    }

    public TeamMemberBuilder withTeamId(TeamId teamId) {
      this.teamId = teamId;
      return this;
    }

    public TeamMemberBuilder withTeamType(TeamType teamType) {
      this.teamType = teamType;
      return this;
    }

    public TeamMemberBuilder withRole(TeamRole teamRole) {
      teamRoles.add(teamRole);
      return this;
    }

    public TeamMemberBuilder withRoles(Set<TeamRole> teamRoles) {
      this.teamRoles = teamRoles;
      return this;
    }

    public TeamMember build() {
      if (teamRoles.isEmpty()) {
        teamRoles.add(RegulatorTeamRole.ACCESS_MANAGER);
      }

      var team = TeamTestUtil.Builder()
          .withId(teamId.id())
          .withTeamType(teamType)
          .build();

      return new TeamMember(webUserAccountId, TeamTestUtil.createTeamView(team), teamRoles);
    }

  }
}
