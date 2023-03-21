package uk.co.nstauthority.fieldconsents.summary;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import uk.co.nstauthority.fieldconsents.formatting.DecimalFormatUtils;

public record SummaryTableView(
    List<SummaryTableRow> tableRows
) {
  public static SummaryTableView newWithHeading(Object... rowValues) {
    var summaryTableView = new SummaryTableView(new ArrayList<>());
    return summaryTableView.addRow(rowValues);
  }

  public SummaryTableView addRow(Object... rowValues) {
    valueCountCheck(rowValues.length);
    var formattedRowValueStrings = Arrays.stream(rowValues).map(this::format).toList();
    tableRows.add(new SummaryTableRow(formattedRowValueStrings));
    return this;
  }

  private void valueCountCheck(int expectedValueCount) {
    var allRowsHaveEqualValueCount = tableRows.stream()
        .allMatch(summaryTableRow -> summaryTableRow.valueCount() == expectedValueCount);

    if (!allRowsHaveEqualValueCount) {
      throw new RuntimeException("Error adding to summary table, " +
          "all table rows must have the same number of values, expected value count %s"
              .formatted(expectedValueCount));
    }
  }

  private String format(Object rowValue) {
    if (rowValue == null) {
      return null;
    } else if (rowValue instanceof String stringRowValue) {
      return stringRowValue;
    } else if (rowValue instanceof Integer integerRowValue) {
      return String.valueOf(integerRowValue);
    } else if (rowValue instanceof BigDecimal bigDecimalRowValue) {
      return DecimalFormatUtils.bigDecimalToFormattedString(bigDecimalRowValue);
    } else {
      throw new RuntimeException("Unexpected summary table row value class type: " + rowValue.getClass().getName());
    }
  }
}
