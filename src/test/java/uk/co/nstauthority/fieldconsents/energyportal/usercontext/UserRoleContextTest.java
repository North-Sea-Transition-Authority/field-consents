package uk.co.nstauthority.fieldconsents.energyportal.usercontext;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.RoleGroup;
import uk.co.nstauthority.fieldconsents.teams.Role;
import uk.co.nstauthority.fieldconsents.teams.Team;
import uk.co.nstauthority.fieldconsents.teams.TeamRole;
import uk.co.nstauthority.fieldconsents.teams.TeamRoleTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

class UserRoleContextTest {

  private static final Team INDUSTRY_TEAM = TeamTestUtil.newBuilder().withTeamType(TeamType.INDUSTRY).build();
  private static final Team REGULATOR_TEAM = TeamTestUtil.newBuilder().withTeamType(TeamType.REGULATOR).build();
  private static final TeamRole INDUSTRY_ROLE = TeamRoleTestUtil.newBuilder()
      .withTeam(INDUSTRY_TEAM)
      .withRole(Role.SUBMITTER)
      .build();
  private static final TeamRole REGULATOR_ROLE_1 = TeamRoleTestUtil.newBuilder()
      .withTeam(REGULATOR_TEAM)
      .withRole(Role.CASE_OFFICER)
      .build();
  private static final TeamRole REGULATOR_ROLE_2 = TeamRoleTestUtil.newBuilder()
      .withTeam(REGULATOR_TEAM)
      .withRole(Role.TECHNICAL_REVIEWER)
      .build();

  @Test
  void from_noRoles() {
    var context = UserRoleContext.from(List.of());

    assertThat(context.rolesByTeamType()).isEmpty();
  }

  @Test
  void from() {
    var context = UserRoleContext.from(List.of(INDUSTRY_ROLE, REGULATOR_ROLE_1, REGULATOR_ROLE_2));

    assertThat(context.rolesByTeamType()).hasSize(2);
    assertThat(context.rolesByTeamType().get(TeamType.INDUSTRY))
        .containsExactly(Role.SUBMITTER);
    assertThat(context.rolesByTeamType().get(TeamType.REGULATOR))
        .containsExactlyInAnyOrder(Role.CASE_OFFICER, Role.TECHNICAL_REVIEWER);
  }

  @Test
  void hasRole() {
    var context = UserRoleContext.from(List.of(
        REGULATOR_ROLE_1
    ));

    assertThat(context.hasRole(TeamType.REGULATOR, Role.CASE_OFFICER)).isTrue();
  }

  @Test
  void hasRole_whenRoleDoesNotExist() {
    var context = UserRoleContext.from(List.of(
        REGULATOR_ROLE_1
    ));

    assertThat(context.hasRole(TeamType.REGULATOR, Role.TECHNICAL_REVIEWER)).isFalse();
  }

@Test
void hasRole_whenTeamTypeDoesNotExist() {
  var context = UserRoleContext.from(List.of(
      REGULATOR_ROLE_1
  ));

  assertThat(context.hasRole(TeamType.INDUSTRY, Role.SUBMITTER)).isFalse();
}

  @Test
  void hasAnyRole() {
    var context = UserRoleContext.from(List.of(
        REGULATOR_ROLE_1, REGULATOR_ROLE_2
    ));

    var rolesToCheck = RoleGroup.CASE_OFFICER_AND_TECHNICAL_REVIEWER;

    assertThat(context.hasAnyRole(TeamType.REGULATOR, rolesToCheck)).isTrue();
  }

  @Test
  void hasAnyRole_RoleDoesNotExist() {
    var context = UserRoleContext.from(List.of(
        INDUSTRY_ROLE
    ));

    var rolesToCheck = RoleGroup.CASE_OFFICER_AND_TECHNICAL_REVIEWER;

    assertThat(context.hasAnyRole(TeamType.INDUSTRY, rolesToCheck)).isFalse();
  }
}