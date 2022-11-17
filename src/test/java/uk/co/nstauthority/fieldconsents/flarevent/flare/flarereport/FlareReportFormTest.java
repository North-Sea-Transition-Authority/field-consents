package uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import org.junit.jupiter.api.Test;

class FlareReportFormTest {

  @Test
  void checkStartAndEndYear() {
    FlareReportForm flareReportForm = FlareReportTestUtil.getStubFlareReportForm();
    assertThat(flareReportForm.getStartYear()).isEqualTo("2022");
    assertThat(flareReportForm.getEndYear()).isEqualTo("2023");
  }

  @Test
  void checkStartAndEndYear_null() {
    FlareReportForm flareReportForm = new FlareReportForm(new ArrayList<>());
    assertThat(flareReportForm.getStartYear()).isNull();
    assertThat(flareReportForm.getEndYear()).isNull();
  }

}
