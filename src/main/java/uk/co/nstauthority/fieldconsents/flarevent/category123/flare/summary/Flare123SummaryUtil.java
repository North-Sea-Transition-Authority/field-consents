package uk.co.nstauthority.fieldconsents.flarevent.category123.flare.summary;

import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryUtil.CATEGORY_HEADING;

import java.util.function.UnaryOperator;

public class Flare123SummaryUtil {

  private Flare123SummaryUtil() {
    throw new IllegalStateException("Utility class");
  }

  // report and consent table headings and row prompts
  public static final String CATEGORY_1_HEADING = "Base Load Flare " + CATEGORY_HEADING + " 1";
  public static final String CATEGORY_2_HEADING = "Operational/Mode Change " + CATEGORY_HEADING + " 2";
  public static final String CATEGORY_3_HEADING = "ESD/Trips " + CATEGORY_HEADING + " 3";
  public static final UnaryOperator<String> CATEGORY_1_HEADING_WITH_UNIT = (CATEGORY_1_HEADING + " (%s)")::formatted;
  public static final UnaryOperator<String> CATEGORY_2_HEADING_WITH_UNIT = (CATEGORY_2_HEADING + " (%s)")::formatted;
  public static final UnaryOperator<String> CATEGORY_3_HEADING_WITH_UNIT = (CATEGORY_3_HEADING + " (%s)")::formatted;
}
