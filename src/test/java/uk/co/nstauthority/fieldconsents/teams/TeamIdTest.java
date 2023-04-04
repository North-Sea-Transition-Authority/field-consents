package uk.co.nstauthority.fieldconsents.teams;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.co.nstauthority.fieldconsents.teams.TeamTestUtil.randomInteger;

import org.junit.jupiter.api.Test;

class TeamIdTest {

  @Test
  void valueOf_String() {
    Integer id = randomInteger();
    var teamId = TeamId.valueOf(id.toString());
    assertThat(teamId.id()).isEqualTo(id);
  }

  @Test
  void valueOf_Uuid() {
    Integer id = randomInteger();
    var teamId = TeamId.valueOf(id);
    assertThat(teamId.id()).isEqualTo(id);
  }
}