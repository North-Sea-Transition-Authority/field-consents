package uk.co.nstauthority.fieldconsents.application;

import java.time.Instant;

public class ApplicationTestUtil {
  public static  final int USER_WUA_ID = 1;
  public static final int APPLICATION_ID = 1;
  public static final int APPLICATION_VERSION_ID = 1;
  public static final int PRIMARY_OPERATOR_OU_ID_1 = 1;
  public static final String CACHED_PRIMARY_OPERATOR_NAME_1 = "TEST ORG UNIT 1";

  public static final int PRIMARY_OPERATOR_OU_ID_2 = 2;
  public static final String CACHED_PRIMARY_OPERATOR_NAME_2 = "TEST ORG UNIT 2";

  public static final int PRIMARY_OPERATOR_OU_ID_3 = 3;
  public static final String CACHED_PRIMARY_OPERATOR_NAME_3 = "TEST ORG UNIT 3";

  public static Application getApplicationWithType(ApplicationType applicationType) {
    return new Application(APPLICATION_ID, applicationType, Instant.now(), USER_WUA_ID);
  }

  public static ApplicationVersion getApplicationVersionWithType(ApplicationType applicationType) {
    Application newApplication = getApplicationWithType(applicationType);
    return new ApplicationVersion(APPLICATION_ID, newApplication, APPLICATION_VERSION_ID, PRIMARY_OPERATOR_OU_ID_1,
        CACHED_PRIMARY_OPERATOR_NAME_1);
  }
}
