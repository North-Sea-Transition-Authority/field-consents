package uk.co.nstauthority.fieldconsents.flarevent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;

class FlareVentRowTest {

  @Test
  void updateFlareVentRowFromForm_validForm() {
    var flareVentRowForm = FlareVentRowTestUtil.getValidFlareVentRowForm();
    var flareVentRow = FlareVentRowTestUtil.getValidFlareVentRow();

    flareVentRow.updateFlareVentRowFromForm(flareVentRowForm);

    assertThat(flareVentRow)
        .extracting(
            FlareVentRow::getCategoryA,
            FlareVentRow::getCategoryB,
            FlareVentRow::getCategoryC
        )
        .containsExactly(
            BigDecimal.valueOf(1),
            BigDecimal.valueOf(1.999999),
            BigDecimal.valueOf(99999999)
        );
  }

  @Test
  void updateFlareVentRowFromForm_badForm() {
    var flareVentRowForm = FlareVentRowTestUtil.getValidFlareVentRowForm();
    flareVentRowForm.getCategoryA().setInputValue("a");
    var flareVentRow = FlareVentRowTestUtil.getValidFlareVentRow();

    assertThatThrownBy(() -> flareVentRow.updateFlareVentRowFromForm(flareVentRowForm))
        .isInstanceOf(NoSuchElementException.class);
  }
}
