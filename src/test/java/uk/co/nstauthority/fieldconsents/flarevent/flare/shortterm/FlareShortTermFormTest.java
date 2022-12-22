package uk.co.nstauthority.fieldconsents.flarevent.flare.shortterm;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;

class FlareShortTermFormTest {

  @Test
  void checkStartEndDates() {
    FlareShortTermForm flareShortTermForm =
        FlareShortTermTestUtil.getStubFlareShortTermFormForPeriod(
            LocalDate.of(2022, Month.APRIL, 5),
            LocalDate.of(2023, Month.JANUARY, 10));
    assertThat(flareShortTermForm.getStartDate()).isEqualTo("5 Apr 2022");
    assertThat(flareShortTermForm.getEndDate()).isEqualTo("10 Jan 2023");
  }

  @Test
  void checkStartEndDates_null() {
    FlareShortTermForm flareShortTermForm = new FlareShortTermForm(new ArrayList<>());
    assertThat(flareShortTermForm.getStartDate()).isNull();
    assertThat(flareShortTermForm.getEndDate()).isNull();
  }
}
