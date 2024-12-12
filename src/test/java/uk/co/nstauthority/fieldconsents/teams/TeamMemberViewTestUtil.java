package uk.co.nstauthority.fieldconsents.teams;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import net.datafaker.Faker;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.fieldconsents.teams.management.view.TeamMemberView;

public class TeamMemberViewTestUtil {

  public static Builder newBuilder() {
    return new Builder();
  }

  public static Builder newBuilder(EnergyPortalUserDto energyPortalUserDto) {
    return new Builder()
        .withWuaId(energyPortalUserDto.webUserAccountId())
        .withForename(energyPortalUserDto.forename())
        .withSurname(energyPortalUserDto.surname());
  }

  public static class Builder {

    private static final Faker faker = new Faker();

    private Long wuaId = ThreadLocalRandom.current().nextLong(100, 200);

    private String forename = faker.name().firstName();
    private String surname = faker.name().lastName();
    private String email = "%s.%s@example.com".formatted(forename, surname);
    private String telNo = faker.phoneNumber().cellPhone();

    private UUID teamId = UUID.randomUUID();
    private List<Role> roles = List.of();

    public Builder withUser(ServiceUserDetail userDetail) {
      return this
          .withWuaId(userDetail.wuaId())
          .withForename(userDetail.forename())
          .withSurname(userDetail.surname())
          .withEmail(userDetail.emailAddress());
    }

    public Builder withWuaId(Long wuaId) {
      this.wuaId = wuaId;
      return this;
    }

    public Builder withForename(String forename) {
      this.forename = forename;
      return this;
    }

    public Builder withSurname(String surname) {
      this.surname = surname;
      return this;
    }

    public Builder withEmail(String email) {
      this.email = email;
      return this;
    }

    public Builder withTelNo(String telNo) {
      this.telNo = telNo;
      return this;
    }

    public Builder withTeamId(UUID teamId) {
      this.teamId = teamId;
      return this;
    }

    public Builder withTeam(Team team) {
      return withTeamId(team.getId());
    }

    public Builder withRoles(Role... roles) {
      this.roles = Arrays.asList(roles);
      return this;
    }

    public TeamMemberView build() {
      return new TeamMemberView(
          wuaId,
          forename,
          surname,
          email,
          telNo,
          teamId,
          roles
      );
    }
  }

}
