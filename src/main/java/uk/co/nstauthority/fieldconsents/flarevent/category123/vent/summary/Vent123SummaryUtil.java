package uk.co.nstauthority.fieldconsents.flarevent.category123.vent.summary;

import java.util.Comparator;
import java.util.function.UnaryOperator;
import uk.co.nstauthority.fieldconsents.flarevent.category123.vent.Vent123Row;

public class Vent123SummaryUtil {

  private Vent123SummaryUtil() {
    throw new IllegalStateException("Utility class");
  }

  public static final Comparator<Vent123Row> YEAR_MONTH_COMPARATOR =
      Comparator.comparing(Vent123Row::getYear)
          .thenComparing(Vent123Row::getMonth);

  // report and consent table headings and row prompts
  public static final String CATEGORY_1_HEADING = "Unignited vent(s)";
  public static final UnaryOperator<String> CATEGORY_1_HEADING_WITH_UNIT = (CATEGORY_1_HEADING + " (%s)")::formatted;
}
