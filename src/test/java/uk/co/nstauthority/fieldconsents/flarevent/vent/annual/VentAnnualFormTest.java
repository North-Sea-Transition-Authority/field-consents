package uk.co.nstauthority.fieldconsents.flarevent.vent.annual;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import org.junit.jupiter.api.Test;

class VentAnnualFormTest {

  @Test
  void checkYear() {
    VentAnnualForm ventAnnualForm = VentAnnualTestUtil.getStubVentAnnualFormForYear(2022);
    assertThat(ventAnnualForm.getYear()).isEqualTo("2022");
  }

  @Test
  void checkYear_null() {
    VentAnnualForm ventAnnualForm = new VentAnnualForm(new ArrayList<>());
    assertThat(ventAnnualForm.getYear()).isNull();
  }

}
