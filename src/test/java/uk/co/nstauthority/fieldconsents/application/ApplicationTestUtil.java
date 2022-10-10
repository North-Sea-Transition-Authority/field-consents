package uk.co.nstauthority.fieldconsents.application;

import java.time.Instant;

public class ApplicationTestUtil {
  static public final int USER_WUA_ID = 1;
  static public final int APPLICATION_ID = 1;
  static public final int APPLICATION_VERSION_ID = 1;

  public static Application getApplicationWithType(ApplicationType applicationType) {
    return new Application(APPLICATION_ID, applicationType, Instant.now(), USER_WUA_ID);
  }

  public static ApplicationVersion getApplicationVersionWithType(ApplicationType applicationType) {
    Application newApplication = getApplicationWithType(applicationType);
    return new ApplicationVersion(APPLICATION_ID, newApplication, APPLICATION_VERSION_ID);
  }
}
