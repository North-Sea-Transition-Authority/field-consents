package uk.co.nstauthority.fieldconsents.flarevent.flare.annual;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import org.junit.jupiter.api.Test;

class FlareAnnualFormTest {

  @Test
  void checkYear() {
    FlareAnnualForm flareAnnualForm = FlareAnnualTestUtil.getStubFlareAnnualFormForYear(2022);
    assertThat(flareAnnualForm.getYear()).isEqualTo("2022");
  }

  @Test
  void checkYear_null() {
    FlareAnnualForm flareAnnualForm = new FlareAnnualForm(new ArrayList<>());
    assertThat(flareAnnualForm.getYear()).isNull();
  }

}
