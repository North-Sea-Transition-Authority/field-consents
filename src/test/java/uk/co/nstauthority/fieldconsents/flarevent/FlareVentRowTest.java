package uk.co.nstauthority.fieldconsents.flarevent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Month;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

class FlareVentRowTest {

  ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getApplicationVersionWithType(ApplicationType.FLARE);
  }

  @Test
  void updateFlareVentRowFromForm_validForm() {
    var flareVentRowForm = FlareVentRowTestUtil.getValidFlareVentRowForm();
    var flareVentRow = FlareVentRowTestUtil.getValidFlareVentRow();

    flareVentRow.updateFlareVentRowFromForm(applicationVersion, flareVentRowForm);

    assertThat(flareVentRow)
        .extracting(
            FlareVentRow::getApplicationVersion,
            FlareVentRow::getYear,
            FlareVentRow::getMonth,
            FlareVentRow::getCategoryA,
            FlareVentRow::getCategoryB,
            FlareVentRow::getCategoryC,
            FlareVentRow::getComments
        )
        .containsExactly(
            applicationVersion,
            2022,
            Month.JANUARY,
            BigDecimal.valueOf(1.123456),
            BigDecimal.valueOf(1.999999),
            BigDecimal.valueOf(99999999.123456),
            "form comments"
        );
  }

  @Test
  void updateFlareVentRowFromForm_badForm() {
    var flareVentRowForm = FlareVentRowTestUtil.getValidFlareVentRowForm();
    flareVentRowForm.getCategoryA().setInputValue("a");
    var flareVentRow = FlareVentRowTestUtil.getValidFlareVentRow();

    assertThatThrownBy(() -> flareVentRow.updateFlareVentRowFromForm(applicationVersion, flareVentRowForm))
        .isInstanceOf(NoSuchElementException.class);
  }
}
