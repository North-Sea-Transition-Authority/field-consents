package uk.co.nstauthority.fieldconsents.application.consentrevision;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.revision.ApplicationRevisionType;

class ApplicationRevisionTypeTest {

  @Test
  void from_variationNoZero() {
    var application = new Application();
    application.setVariationNo(0);

    assertThat(ApplicationRevisionType.from(application)).isEqualTo(ApplicationRevisionType.NEW_CONSENT);
  }

  @Test
  void from_variationNoGreaterThanZero() {
    var application = new Application();
    application.setVariationNo(1);

    assertThat(ApplicationRevisionType.from(application)).isEqualTo(ApplicationRevisionType.REVISION);
  }
}
