package uk.co.nstauthority.fieldconsents.util.userutil;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class UserDisplayNameUtilTest {

  @Test
  void getUserDisplayName() {
    var forename = "Forename";
    var surname = "Surname";
    assertThat(UserDisplayNameUtil.getUserDisplayName(forename, surname)).isEqualTo(forename + " " + surname);
  }

  @Test
  void getUserDisplayNameAndEmail() {
    var forename = "Forename";
    var surname = "Surname";
    var emailAddress = "test@test.com";

    assertThat(UserDisplayNameUtil.getUserDisplayNameAndEmail(forename, surname, emailAddress))
        .isEqualTo(forename + " " + surname + " (" + emailAddress + ")");
  }
}
