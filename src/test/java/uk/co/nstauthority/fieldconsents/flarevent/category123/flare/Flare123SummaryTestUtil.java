package uk.co.nstauthority.fieldconsents.flarevent.category123.flare;

import java.math.BigDecimal;
import java.util.List;

public class Flare123SummaryTestUtil {

  public static BigDecimal getCategory1Total(List<? extends Flare123Row> monthRows) {
    return monthRows.stream().map(Flare123Row::getCategory1).reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  public static BigDecimal getCategory2Total(List<? extends Flare123Row> monthRows) {
    return monthRows.stream().map(Flare123Row::getCategory2).reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  public static BigDecimal getCategory3Total(List<? extends Flare123Row> monthRows) {
    return monthRows.stream().map(Flare123Row::getCategory3).reduce(BigDecimal.ZERO, BigDecimal::add);
  }
}
