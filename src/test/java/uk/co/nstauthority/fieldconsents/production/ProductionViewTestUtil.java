package uk.co.nstauthority.fieldconsents.production;

import java.util.Collections;

public class ProductionViewTestUtil {

  public static final String MONTH_HEADING = "Month";
  public static final String YEAR_HEADING = "Year";
  public static final String CONSENT_DAYS_HEADING = "Consent days";
  public static final String MIN_OIL_HEADING = "Minimum oil (%s)";
  public static final String MAX_OIL_HEADING = "Maximum oil (%s)";
  public static final String MIN_GAS_HEADING = "Minimum gas (%s)";
  public static final String MAX_GAS_HEADING = "Maximum gas (%s)";
  public static final String TOTAL_PROMPT = "Totals";
  public static final String AVERAGE_PROMPT = "Daily average (%s)";

  private static ProductionView emptyFrom(String periodHeading,
                                          ProductionUnit oilUnit,
                                          ProductionUnit gasUnit) {

    return new ProductionView(
        periodHeading,
        CONSENT_DAYS_HEADING,
        MIN_OIL_HEADING.formatted(oilUnit.getDisplayName()),
        MAX_OIL_HEADING.formatted(oilUnit.getDisplayName()),
        MIN_GAS_HEADING.formatted(gasUnit.getDisplayName()),
        MAX_GAS_HEADING.formatted(gasUnit.getDisplayName()),
        Collections.emptyList()
    );
  }

  public static ProductionView emptyShortTerm() {
    return emptyFrom(MONTH_HEADING, ProductionUnit.KSCM_PER_MONTH, ProductionUnit.KSCM_PER_MONTH);
  }

  public static ProductionView emptyAnnual() {
    return emptyFrom(MONTH_HEADING, ProductionUnit.KSCM_PER_MONTH, ProductionUnit.KSCM_PER_MONTH);
  }

  public static ProductionView emptyLongTerm() {
    return emptyFrom(YEAR_HEADING, ProductionUnit.KSCM_PER_DAY, ProductionUnit.KSCM_PER_DAY);
  }
}