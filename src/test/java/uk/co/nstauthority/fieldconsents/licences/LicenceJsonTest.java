package uk.co.nstauthority.fieldconsents.licences;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.co.nstauthority.fieldconsents.licences.LicenceJsonTestUtil.licence1;

import org.junit.jupiter.api.Test;

class LicenceJsonTest {

  @Test
  void from() {
    assertThat(LicenceJson.from(licence1))
        .usingRecursiveComparison()
        .isEqualTo(new LicenceJson(
                licence1.getId(),
                licence1.getLicenceType(),
                licence1.getLicenceNo(),
                licence1.getLicenceRef(),
                licence1.getScheduleExpiryDate()
            )
        );
  }

  @Test
  void fromCachedInformation() {
    assertThat(LicenceJson.fromCachedInformation(
            licence1.getId(),
            licence1.getLicenceType(),
            licence1.getLicenceNo(),
            licence1.getLicenceRef(),
            licence1.getScheduleExpiryDate()
        )
    )
        .usingRecursiveComparison()
        .isEqualTo(new LicenceJson(
                licence1.getId(),
                licence1.getLicenceType(),
                licence1.getLicenceNo(),
                licence1.getLicenceRef(),
                licence1.getScheduleExpiryDate()
            )
        );
  }
}
