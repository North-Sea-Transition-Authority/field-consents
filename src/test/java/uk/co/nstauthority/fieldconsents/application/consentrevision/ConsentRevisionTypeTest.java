package uk.co.nstauthority.fieldconsents.application.consentrevision;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uk.co.nstauthority.fieldconsents.application.Application;

class ConsentRevisionTypeTest {

  @Test
  void from_variationNoZero() {
    var application = new Application();
    application.setVariationNo(0);

    assertThat(ConsentRevisionType.from(application)).isEqualTo(ConsentRevisionType.NEW_CONSENT);
  }

  @Test
  void from_variationNoGreaterThanZero() {
    var application = new Application();
    application.setVariationNo(1);

    assertThat(ConsentRevisionType.from(application)).isEqualTo(ConsentRevisionType.REVISION);
  }
}
