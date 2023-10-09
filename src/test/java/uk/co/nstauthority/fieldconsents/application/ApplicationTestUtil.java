package uk.co.nstauthority.fieldconsents.application;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class ApplicationTestUtil {
  public static final long USER_WUA_ID = 1;
  public static final long CASE_OFFICER_WUA_ID = 10;

  public static final int APPLICATION_ID = 1;
  public static final int APPLICATION_VERSION_ID = 1;

  public static final int APPLICATION_VERSION_NUMBER = 1;

  public static final int PRIMARY_OPERATOR_OU_ID_1 = 1;
  public static final String CACHED_PRIMARY_OPERATOR_NAME_1 = "TEST ORG UNIT 1";

  public static final int PRIMARY_OPERATOR_OU_ID_2 = 2;
  public static final String CACHED_PRIMARY_OPERATOR_NAME_2 = "TEST ORG UNIT 2";

  public static final int PRIMARY_OPERATOR_OU_ID_3 = 3;
  public static final String CACHED_PRIMARY_OPERATOR_NAME_3 = "TEST ORG UNIT 3";

  public static final int APPLICATION_NO = 500;

  public static final String APPLICATION_REFERENCE = "PCON/500/0";

  private static Application getNewApplicationWithType(ApplicationType applicationType) {
    return new Application(APPLICATION_ID, applicationType, Instant.now(), USER_WUA_ID, 0, null);
  }

  public static ApplicationVersion getNewApplicationVersionWithType(ApplicationType applicationType) {
    return getNewApplicationVersionWithTypeIdAndVersionNumber(applicationType, APPLICATION_VERSION_ID, APPLICATION_VERSION_NUMBER);
  }

  public static ApplicationVersion getNewApplicationVersionWithTypeIdAndVersionNumber(ApplicationType applicationType,
                                                                                      Integer applicationVersionId,
                                                                                      Integer applicationVersionNumber) {
    var newApplication = getNewApplicationWithType(applicationType);
    return new ApplicationVersion(
        applicationVersionId,
        newApplication,
        applicationVersionNumber,
        PRIMARY_OPERATOR_OU_ID_1,
        CACHED_PRIMARY_OPERATOR_NAME_1,
        Instant.now(),
        USER_WUA_ID,
        null,
        null,
        ApplicationVersionStatus.IN_PROGRESS,
        null);
  }

  private static Application getAwaitingPaymentApplicationWithType(ApplicationType applicationType) {
    return new Application(APPLICATION_ID, applicationType, Instant.now(), USER_WUA_ID, 0, null);
  }

  public static ApplicationVersion getAwaitingPaymentApplicationVersionWithType(ApplicationType applicationType) {
    return getAwaitingPaymentApplicationVersionWithTypeIdAndVersionNumber(
        applicationType,
        APPLICATION_VERSION_ID,
        APPLICATION_VERSION_NUMBER
    );
  }

  public static ApplicationVersion getAwaitingPaymentApplicationVersionWithTypeIdAndVersionNumber(
      ApplicationType applicationType,
      Integer applicationVersionId,
      Integer applicationVersionNumber
  ) {
    var newApplication = getNewApplicationWithType(applicationType);
    return new ApplicationVersion(
        applicationVersionId,
        newApplication,
        applicationVersionNumber,
        PRIMARY_OPERATOR_OU_ID_1,
        CACHED_PRIMARY_OPERATOR_NAME_1,
        Instant.now(),
        USER_WUA_ID,
        null,
        null,
        ApplicationVersionStatus.AWAITING_PAYMENT,
        null
    );
  }

  private static Application getSubmittedApplicationWithType(ApplicationType applicationType) {
    return new Application(APPLICATION_ID, applicationType, Instant.now(), USER_WUA_ID, 0, APPLICATION_NO);
  }

  public static ApplicationVersion getSubmittedApplicationVersionWithType(ApplicationType applicationType) {
    return getSubmittedApplicationVersionWithTypeIdAndVersionNumber(applicationType, APPLICATION_VERSION_ID, APPLICATION_VERSION_NUMBER);
  }

  public static ApplicationVersion getSubmittedApplicationVersionWithTypeIdAndVersionNumber(ApplicationType applicationType,
                                                                                            Integer applicationVersionId,
                                                                                            Integer applicationVersionNumber) {
    var submittedApplication = getSubmittedApplicationWithType(applicationType);
    var submittedApplicationVersion = new ApplicationVersion(
        applicationVersionId,
        submittedApplication,
        applicationVersionNumber,
        PRIMARY_OPERATOR_OU_ID_1,
        CACHED_PRIMARY_OPERATOR_NAME_1,
        Instant.now().minusSeconds(60),
        USER_WUA_ID,
        Instant.now(),
        USER_WUA_ID,
        ApplicationVersionStatus.SUBMITTED,
        null);

    submittedApplicationVersion.setSubmittedDateTime(Instant.now().plus(3, ChronoUnit.DAYS));
    submittedApplicationVersion.setSubmittedByWuaId(USER_WUA_ID);
    return submittedApplicationVersion;
  }
}
