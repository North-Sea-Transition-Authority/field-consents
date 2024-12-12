package uk.co.nstauthority.fieldconsents.teams;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class TeamRoleTestUtil {

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {

    private UUID id = UUID.randomUUID();
    private Team team = TeamTestUtil.newBuilder().build();
    private Role role = Role.CASE_OFFICER;
    private Long wuaId = ThreadLocalRandom.current().nextLong(100, 200);

    public Builder withId(UUID id) {
      this.id = id;
      return this;
    }

    public Builder withTeam(Team team) {
      this.team = team;
      return this;
    }

    public Builder withRole(Role role) {
      this.role = role;
      return this;
    }

    public Builder withWuaId(Long wuaId) {
      this.wuaId = wuaId;
      return this;
    }

    public TeamRole build() {
      var teamRole = new TeamRole(id);
      teamRole.setTeam(team);
      teamRole.setRole(role);
      teamRole.setWuaId(wuaId);

      return teamRole;
    }

    private Builder() {

    }

  }

}
