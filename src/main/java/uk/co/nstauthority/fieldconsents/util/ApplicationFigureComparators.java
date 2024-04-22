package uk.co.nstauthority.fieldconsents.util;

import java.util.Comparator;
import uk.co.nstauthority.fieldconsents.flarevent.EmissionLongTermYear;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentRow;
import uk.co.nstauthority.fieldconsents.flarevent.category123.flare.Flare123Row;
import uk.co.nstauthority.fieldconsents.flarevent.category123.vent.Vent123Row;
import uk.co.nstauthority.fieldconsents.production.annual.AnnualProductionMonth;
import uk.co.nstauthority.fieldconsents.production.longterm.LongTermProductionYear;
import uk.co.nstauthority.fieldconsents.production.shortterm.ShortTermProductionMonth;

public class ApplicationFigureComparators {

  private ApplicationFigureComparators() {
    throw new IllegalStateException("ApplicationFigureComparators is a utility class and should not be instantiated");
  }

  public static Comparator<Vent123Row> vent123Row() {
    return Comparator.comparing(Vent123Row::getYear)
        .thenComparing(Vent123Row::getMonth);
  }

  public static Comparator<Flare123Row> flare123Row() {
    return Comparator.comparing(Flare123Row::getYear)
        .thenComparing(Flare123Row::getMonth);
  }

  public static Comparator<EmissionLongTermYear> emissionLongTermYear() {
    return Comparator.comparing(EmissionLongTermYear::getYear);
  }

  public static Comparator<FlareVentRow> flareVentRow() {
    return Comparator.comparing(FlareVentRow::getYear)
        .thenComparing(FlareVentRow::getMonth);
  }

  public static Comparator<ShortTermProductionMonth> shortTermProductionMonth() {
    return Comparator.comparing(ShortTermProductionMonth::getYear)
        .thenComparing(ShortTermProductionMonth::getMonth);
  }

  public static Comparator<LongTermProductionYear> longTermProductionYear() {
    return Comparator.comparing(LongTermProductionYear::getYear);
  }

  public static Comparator<AnnualProductionMonth> annualProductionMonth() {
    return Comparator.comparing(AnnualProductionMonth::getYear)
        .thenComparing(AnnualProductionMonth::getMonth);
  }

}
