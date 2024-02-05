package uk.co.nstauthority.fieldconsents.flarevent.category123.vent;

import java.math.BigDecimal;
import java.util.List;

public class Vent123SummaryTestUtil {

  public static BigDecimal getCategory1Total(List<? extends Vent123Row> monthRows) {
    return monthRows.stream().map(Vent123Row::getCategory1).reduce(BigDecimal.ZERO, BigDecimal::add);
  }
}
