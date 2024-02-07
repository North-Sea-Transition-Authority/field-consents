package uk.co.nstauthority.fieldconsents.flarevent.summary;

import java.math.BigDecimal;
import java.util.List;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentRow;

public class EmissionSummaryTestUtil {

  public static BigDecimal getCategoryATotal(List<? extends FlareVentRow> monthRows) {
    return monthRows.stream().map(FlareVentRow::getCategoryA).reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  public static BigDecimal getCategoryBTotal(List<? extends FlareVentRow> monthRows) {
    return monthRows.stream().map(FlareVentRow::getCategoryB).reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  public static BigDecimal getCategoryCTotal(List<? extends FlareVentRow> monthRows) {
    return monthRows.stream().map(FlareVentRow::getCategoryC).reduce(BigDecimal.ZERO, BigDecimal::add);
  }
}
