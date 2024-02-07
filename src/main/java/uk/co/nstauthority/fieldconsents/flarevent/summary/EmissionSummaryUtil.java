package uk.co.nstauthority.fieldconsents.flarevent.summary;

import java.util.function.UnaryOperator;

public class EmissionSummaryUtil {

  private EmissionSummaryUtil() {
    throw new IllegalStateException("Utility class");
  }

  // report and consent table headings and row prompts
  public static final String MONTH_HEADING = "Month";
  public static final String CONSENT_DAYS_HEADING = "Consent days";
  public static final String CATEGORY_HEADING = "Category";
  public static final String CATEGORY_A_HEADING = CATEGORY_HEADING + " A";
  public static final String CATEGORY_B_HEADING = CATEGORY_HEADING + " B";
  public static final String CATEGORY_C_HEADING = CATEGORY_HEADING + " C";
  public static final UnaryOperator<String> CATEGORY_A_HEADING_WITH_UNIT = (CATEGORY_A_HEADING + " (%s)")::formatted;
  public static final UnaryOperator<String> CATEGORY_B_HEADING_WITH_UNIT = (CATEGORY_B_HEADING + " (%s)")::formatted;
  public static final UnaryOperator<String> CATEGORY_C_HEADING_WITH_UNIT = (CATEGORY_C_HEADING + " (%s)")::formatted;
  public static final String TOTAL_PROMPT = "Totals";
  public static final UnaryOperator<String> CATEGORY_TOTAL_HEADING_WITH_UNIT = (TOTAL_PROMPT + " (%s)")::formatted;
  public static final String SHUTDOWN_DAYS_HEADING = "Days of total shutdown";
  public static final UnaryOperator<String> AVERAGE_PROMPT_WITH_UNIT = "Daily average (%s)"::formatted;
  public static final String COMMENTS_HEADING = "Comments";
  public static final String YEAR_HEADING = "Year";
  public static final UnaryOperator<String> GAS_HEADING_WITH_UNIT = "Gas (%s)"::formatted;
}
