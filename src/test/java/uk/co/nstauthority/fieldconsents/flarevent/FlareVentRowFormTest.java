package uk.co.nstauthority.fieldconsents.flarevent;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class FlareVentRowFormTest {

  @Test
  void updateFromFlareVentRow() {
    var flareVentRow = FlareVentRowTestUtil.getValidFlareVentRow();
    var flareVentRowForm = FlareVentRowTestUtil.getValidFlareVentRowForm();

    flareVentRowForm.updateFromFlareVentRow(flareVentRow);

    assertThat(flareVentRowForm)
        .extracting(
            FlareVentRowForm::getYear,
            FlareVentRowForm::getMonth,
            form -> form.getCategoryA().getDisplayName(),
            form -> form.getCategoryA().getFieldName(),
            form -> form.getCategoryA().getInputValue(),
            form -> form.getCategoryB().getDisplayName(),
            form -> form.getCategoryB().getFieldName(),
            form -> form.getCategoryB().getInputValue(),
            form -> form.getCategoryC().getDisplayName(),
            form -> form.getCategoryC().getFieldName(),
            form -> form.getCategoryC().getInputValue(),
            form -> form.getComments().getDisplayName(),
            form -> form.getComments().getFieldName(),
            form -> form.getComments().getInputValue()
        )
        .containsExactly(
            "2023", "February",
            "Category A", "categoryA", "0",
            "Category B", "categoryB", "1",
            "Category C", "categoryC", "999.999",
            "comments", "comments", "entity comments"
        );
  }
}
