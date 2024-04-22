package uk.co.nstauthority.fieldconsents.flarevent.category123.vent.summary;

import java.util.function.UnaryOperator;

public class Vent123SummaryUtil {

  private Vent123SummaryUtil() {
    throw new IllegalStateException("Utility class");
  }

  // report and consent table headings and row prompts
  public static final String CATEGORY_1_HEADING = "Unignited vent(s)";
  public static final UnaryOperator<String> CATEGORY_1_HEADING_WITH_UNIT = (CATEGORY_1_HEADING + " (%s)")::formatted;
}
