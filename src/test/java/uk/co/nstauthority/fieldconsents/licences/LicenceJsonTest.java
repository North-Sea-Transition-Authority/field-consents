package uk.co.nstauthority.fieldconsents.licences;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.co.nstauthority.fieldconsents.licences.LicenceTestUtil.licence1;

import org.junit.jupiter.api.Test;

class LicenceJsonTest {

  @Test
  void from() {
    assertThat(LicenceJson.from(licence1))
        .usingRecursiveComparison()
        .isEqualTo(new LicenceJson(licence1.getId(), licence1.getLicenceRef()));
  }

  @Test
  void fromCachedInformation() {
    assertThat(LicenceJson.fromCachedInformation(licence1.getId(), licence1.getLicenceRef()))
        .usingRecursiveComparison()
        .isEqualTo(new LicenceJson(licence1.getId(), licence1.getLicenceRef()));
  }
}
