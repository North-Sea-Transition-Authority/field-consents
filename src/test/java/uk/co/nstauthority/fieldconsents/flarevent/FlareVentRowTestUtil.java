package uk.co.nstauthority.fieldconsents.flarevent;

import java.math.BigDecimal;

class FlareVentRowTestUtil {

  static FlareVentRowForm getValidFlareVentRowForm() {
    FlareVentRowForm form = new FlareVentRowForm();
    form.getCategoryA().setInputValue("1");
    form.getCategoryB().setInputValue("1.999999");
    form.getCategoryC().setInputValue("99999999");
    return form;
  }

  static FlareVentRow getValidFlareVentRow() {
    FlareVentRow flareVentRow = new FlareVentRow();
    flareVentRow.setCategoryA(BigDecimal.ZERO);
    flareVentRow.setCategoryB(BigDecimal.ONE);
    flareVentRow.setCategoryC(BigDecimal.valueOf(999.999));
    return flareVentRow;
  }
}
