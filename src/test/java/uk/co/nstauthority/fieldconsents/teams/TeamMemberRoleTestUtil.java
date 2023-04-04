package uk.co.nstauthority.fieldconsents.teams;

import static uk.co.nstauthority.fieldconsents.teams.TeamTestUtil.randomInteger;

import uk.co.nstauthority.fieldconsents.exception.IllegalUtilClassInstantiationException;

public class TeamMemberRoleTestUtil {

  private TeamMemberRoleTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static Builder Builder() {
    return new Builder();
  }

  public static class Builder {

    private Builder() {}

    private Integer id = randomInteger();

    private Team team = TeamTestUtil.Builder().build();

    private long webUserAccountId = 100;

    private String role = "TEST_ROLE";

    public Builder withId(Integer id) {
      this.id = id;
      return this;
    }

    public Builder withTeam(Team team) {
      this.team = team;
      return this;
    }

    public Builder withWebUserAccountId(long webUserAccountId) {
      this.webUserAccountId = webUserAccountId;
      return this;
    }

    public Builder withRole(String role) {
      this.role = role;
      return this;
    }

    public TeamMemberRole build() {
      var teamMemberRole = new TeamMemberRole(id);
      teamMemberRole.setTeam(team);
      teamMemberRole.setWuaId(webUserAccountId);
      teamMemberRole.setRole(role);
      return teamMemberRole;
    }

  }
}
