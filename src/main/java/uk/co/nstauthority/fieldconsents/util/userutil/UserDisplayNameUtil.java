package uk.co.nstauthority.fieldconsents.util.userutil;

import uk.co.nstauthority.fieldconsents.exception.IllegalUtilClassInstantiationException;

public class UserDisplayNameUtil {

  private UserDisplayNameUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static String getUserDisplayName(String forename, String surname) {
    return "%s %s".formatted(forename, surname);
  }

  public static String getUserDisplayNameAndEmail(String forename, String surname, String emailAddress) {
    return "%s (%s)".formatted(getUserDisplayName(forename, surname), emailAddress);
  }
}
