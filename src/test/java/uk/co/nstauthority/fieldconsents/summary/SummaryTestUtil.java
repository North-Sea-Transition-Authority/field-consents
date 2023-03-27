package uk.co.nstauthority.fieldconsents.summary;

import java.math.BigDecimal;
import java.util.List;

public class SummaryTestUtil {

  private static final String HEADING1 = "Heading 1";
  private static final String HEADING2 = "Heading 2";
  private static final String HEADING3 = "Heading 3";
  private static final String HEADING4 = "Heading 4";
  private static final String STRING_ROW_VALUE = "1";
  private static final Integer INTEGER_ROW_VALUE = 1;
  private static final BigDecimal BIG_DECIMAL_ROW_VALUE = BigDecimal.ONE;

  public static List<SummaryKeyValue> summaryKeyValues = List.of(
      new SummaryKeyValue("key1", "value1"),
      new SummaryKeyValue("key2", "value2")
  );

  public static SummaryDataView summaryDataView = new SummaryDataView(summaryKeyValues);

  public static SummaryTableView getSummaryTableView() {
    return SummaryTableView.newWithHeading(HEADING1, HEADING2, HEADING3, HEADING4)
        .addRow(null, STRING_ROW_VALUE, INTEGER_ROW_VALUE, BIG_DECIMAL_ROW_VALUE);
  }

  public static SummaryCard getTableSummaryCard() {
    return SummaryCard.tableSummaryCard(getSummaryTableView());
  }

  public static SummaryCard getSimpleSummaryCard() {
    return SummaryCard.simpleSummaryCard(new SummaryDataView(summaryKeyValues));
  }
}
