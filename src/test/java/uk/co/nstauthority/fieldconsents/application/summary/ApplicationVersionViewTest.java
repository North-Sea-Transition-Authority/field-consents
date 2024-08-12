package uk.co.nstauthority.fieldconsents.application.summary;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;

class ApplicationVersionViewTest {

  private static final long SUBMITTED_AT_EPOCH_MILLI = 1723204778231L; // 09 Aug 2024

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = new ApplicationVersion();
    applicationVersion.setId(1);
    applicationVersion.setVersion(2);
  }

  @Test
  void from_submittedApplicationVersion() {
    var submittedDateTime = Instant.ofEpochMilli(SUBMITTED_AT_EPOCH_MILLI);

    applicationVersion.setStatus(ApplicationVersionStatus.SUBMITTED);
    applicationVersion.setSubmittedDateTime(submittedDateTime);

    assertThat(ApplicationVersionView.from(applicationVersion))
        .isEqualTo(new ApplicationVersionView(
            applicationVersion.getId(),
            "Version 2: 9 Aug 2024"
        ));
  }

  @Test
  void from_withdrawnApplicationVersion() {
    var submittedDateTime = Instant.ofEpochMilli(SUBMITTED_AT_EPOCH_MILLI);

    applicationVersion.setStatus(ApplicationVersionStatus.WITHDRAWN);
    applicationVersion.setSubmittedDateTime(submittedDateTime);

    assertThat(ApplicationVersionView.from(applicationVersion))
        .isEqualTo(new ApplicationVersionView(
            applicationVersion.getId(),
            "Version 2: 9 Aug 2024 (withdrawn)"
        ));
  }

  @Test
  void from_withdrawnApplicationVersion_noSubmittedDateTime() {
    applicationVersion.setStatus(ApplicationVersionStatus.WITHDRAWN);

    assertThat(ApplicationVersionView.from(applicationVersion))
        .isEqualTo(new ApplicationVersionView(
            applicationVersion.getId(),
            "Version 2 (withdrawn)"
        ));
  }

  @Test
  void from_consentedApplicationVersion() {
    var submittedDateTime = Instant.ofEpochMilli(SUBMITTED_AT_EPOCH_MILLI);

    applicationVersion.setStatus(ApplicationVersionStatus.CONSENTED);
    applicationVersion.setSubmittedDateTime(submittedDateTime);

    assertThat(ApplicationVersionView.from(applicationVersion))
        .isEqualTo(new ApplicationVersionView(
            applicationVersion.getId(),
            "Version 2: 9 Aug 2024"
        ));
  }

}