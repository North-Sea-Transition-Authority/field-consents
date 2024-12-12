package uk.co.nstauthority.fieldconsents.teams;

import java.util.UUID;

public class TeamTestUtil {

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {

    private UUID id = UUID.randomUUID();
    private String name = "Test team";
    private TeamType teamType = TeamType.REGULATOR;
    private String scopeType = null;
    private String scopeId = null;

    public Builder withId(UUID id) {
      this.id = id;
      return this;
    }

    public Builder withName(String name) {
      this.name = name;
      return this;
    }

    public Builder withTeamType(TeamType teamType) {
      this.teamType = teamType;
      return this;
    }

    public Builder withScopeType(String scopeType) {
      this.scopeType = scopeType;
      return this;
    }

    public Builder withScopeId(String scopeId) {
      this.scopeId = scopeId;
      return this;
    }

    public Team build() {
      var team = new Team(id);
      team.setName(name);
      team.setTeamType(teamType);
      team.setScopeType(scopeType);
      team.setScopeId(scopeId);

      return team;
    }

    private Builder() {

    }

  }

}
