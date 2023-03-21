package uk.co.nstauthority.fieldconsents.flarevent.summary;

import java.math.BigDecimal;
import java.util.List;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentRow;

class EmissionSummaryTestUtil {

  static BigDecimal getCategoryATotal(List<? extends FlareVentRow> monthRows) {
    return monthRows.stream().map(FlareVentRow::getCategoryA).reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  static BigDecimal getCategoryBTotal(List<? extends FlareVentRow> monthRows) {
    return monthRows.stream().map(FlareVentRow::getCategoryB).reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  static BigDecimal getCategoryCTotal(List<? extends FlareVentRow> monthRows) {
    return monthRows.stream().map(FlareVentRow::getCategoryC).reduce(BigDecimal.ZERO, BigDecimal::add);
  }
}
