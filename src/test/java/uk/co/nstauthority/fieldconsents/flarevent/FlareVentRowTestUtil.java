package uk.co.nstauthority.fieldconsents.flarevent;

import java.math.BigDecimal;
import java.time.Month;

class FlareVentRowTestUtil {

  static FlareVentRowForm getValidFlareVentRowForm() {
    FlareVentRowForm form = new FlareVentRowForm();
    form.setYear("2022");
    form.setMonth(Month.JANUARY);
    form.getCategoryA().setInputValue("1");
    form.getCategoryB().setInputValue("1.999999");
    form.getCategoryC().setInputValue("99999999");
    form.setComments("form comments");
    return form;
  }

  static FlareVentRow getValidFlareVentRow() {
    FlareVentRow flareVentRow = new FlareVentRow();
    flareVentRow.setYear(2023);
    flareVentRow.setMonth(Month.FEBRUARY);
    flareVentRow.setCategoryA(BigDecimal.ZERO);
    flareVentRow.setCategoryB(BigDecimal.ONE);
    flareVentRow.setCategoryC(BigDecimal.valueOf(999.999));
    flareVentRow.setComments("entity comments");
    return flareVentRow;
  }
}
