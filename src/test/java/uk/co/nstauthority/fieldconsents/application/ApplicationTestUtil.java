package uk.co.nstauthority.fieldconsents.application;

import java.time.Instant;

public class ApplicationTestUtil {
  static public final int USER_WUA_ID = 1;
  static public final int APPLICATION_ID = 1;
  static public final int APPLICATION_VERSION_ID = 1;
  static public final int PRIMARY_OPERATOR_OU_ID = 1;
  static public final String CACHED_PRIMARY_OPERATOR_NAME = "TEST ORG UNIT";

  static public final int PRIMARY_OPERATOR_OU_ID_2 = 1;
  static public final String CACHED_PRIMARY_OPERATOR_NAME_2 = "TEST ORG UNIT 2";

  static public final int PRIMARY_OPERATOR_OU_ID_3 = 3;
  static public final String CACHED_PRIMARY_OPERATOR_NAME_3 = "TEST ORG UNIT 3";

  public static Application getApplicationWithType(ApplicationType applicationType) {
    return new Application(APPLICATION_ID, applicationType, Instant.now(), USER_WUA_ID);
  }

  public static ApplicationVersion getApplicationVersionWithType(ApplicationType applicationType) {
    Application newApplication = getApplicationWithType(applicationType);
    return new ApplicationVersion(APPLICATION_ID, newApplication, APPLICATION_VERSION_ID, PRIMARY_OPERATOR_OU_ID,
        CACHED_PRIMARY_OPERATOR_NAME);
  }
}
