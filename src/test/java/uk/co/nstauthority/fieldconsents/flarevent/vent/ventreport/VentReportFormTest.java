package uk.co.nstauthority.fieldconsents.flarevent.vent.ventreport;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import org.junit.jupiter.api.Test;

class VentReportFormTest {

  @Test
  void checkStartAndEndYear() {
    VentReportForm ventReportForm = VentReportTestUtil.getStubVentReportForm();
    assertThat(ventReportForm.getStartYear()).isEqualTo("2022");
    assertThat(ventReportForm.getEndYear()).isEqualTo("2023");
  }

  @Test
  void checkStartAndEndYear_null() {
    VentReportForm ventReportForm = new VentReportForm(new ArrayList<>());
    assertThat(ventReportForm.getStartYear()).isNull();
    assertThat(ventReportForm.getEndYear()).isNull();
  }

}
