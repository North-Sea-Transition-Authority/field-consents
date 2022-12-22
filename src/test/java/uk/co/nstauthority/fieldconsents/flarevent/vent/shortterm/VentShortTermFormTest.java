package uk.co.nstauthority.fieldconsents.flarevent.vent.shortterm;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;

class VentShortTermFormTest {

  @Test
  void checkStartEndDates() {
    VentShortTermForm ventShortTermForm = VentShortTermTestUtil.getStubVentShortTermFormForPeriod(
            LocalDate.of(2022, Month.APRIL, 5),
            LocalDate.of(2023, Month.JANUARY, 10)
    );
    assertThat(ventShortTermForm.getStartDate()).isEqualTo("5 Apr 2022");
    assertThat(ventShortTermForm.getEndDate()).isEqualTo("10 Jan 2023");
  }

  @Test
  void checkStartEndDates_null() {
    VentShortTermForm ventShortTermForm = new VentShortTermForm(new ArrayList<>());
    assertThat(ventShortTermForm.getStartDate()).isNull();
    assertThat(ventShortTermForm.getEndDate()).isNull();
  }

}
