package uk.co.nstauthority.fieldconsents.flarevent.summary;

import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryUtil.CATEGORY_A_HEADING;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryUtil.CATEGORY_B_HEADING;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryUtil.CATEGORY_C_HEADING;
import static uk.co.nstauthority.fieldconsents.formatting.DateUtils.LONG_MONTH_YEAR;

import java.util.function.UnaryOperator;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentReportGasData;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentReportPeriod;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentUnit;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.summary.SummaryDataView;
import uk.co.nstauthority.fieldconsents.summary.SummaryTableView;

@Service
public class EmissionReportGasDataSummaryService {

  private static final UnaryOperator<String> DENSITY_PROMPT_WITH_UNIT = "Standard density (%s)"::formatted;
  public static final UnaryOperator<String> INERT_GAS_CONTENT_PROMPT_WITH_UNIT = "Inert gas content (%s)"::formatted;
  public static final UnaryOperator<String> HYDROCARBON_CONTENT_PROMPT_WITH_UNIT = "Hydrocarbon content (%s)"::formatted;

  public SummaryCard getReportGasDataTableSummaryCard(FlareVentReportGasData reportGasData,
                                                      FlareVentReportPeriod reportPeriod,
                                                      FlareVentUnit densityUnit,
                                                      FlareVentUnit gasContentUnit) {
    var summaryTable = SummaryTableView
        .newWithHeading(
            null,
            CATEGORY_A_HEADING,
            CATEGORY_B_HEADING,
            CATEGORY_C_HEADING
        )
        .addRow(
            DENSITY_PROMPT_WITH_UNIT.apply(densityUnit.getDisplayName()),
            reportGasData.getCategoryADensity(),
            reportGasData.getCategoryBDensity(),
            reportGasData.getCategoryCDensity()
        )
        .addRow(
            INERT_GAS_CONTENT_PROMPT_WITH_UNIT.apply(gasContentUnit.getDisplayName()),
            reportGasData.getCategoryAInertGasPercentage(),
            reportGasData.getCategoryBInertGasPercentage(),
            reportGasData.getCategoryCInertGasPercentage()
        )
        .addRow(
            HYDROCARBON_CONTENT_PROMPT_WITH_UNIT.apply(gasContentUnit.getDisplayName()),
            reportGasData.getCategoryAHydrocarbonPercentage(),
            reportGasData.getCategoryBHydrocarbonPercentage(),
            reportGasData.getCategoryCHydrocarbonPercentage()
        );

    return SummaryCard.tableSummaryCardWithHeading(
        DateUtils.format(reportPeriod.getReportStartYearMonth(), LONG_MONTH_YEAR) + " to " +
            DateUtils.format(reportPeriod.getReportEndYearMonth(), LONG_MONTH_YEAR),
        summaryTable);
  }

  public SummaryCard getReportGasDataJustificationSummaryCard(FlareVentReportGasData reportGasData) {

    var summaryData = SummaryDataView.newWithKeyValue("Have you evaluated the properties for each category individually?",
        reportGasData.getEvaluatedPerCategory());

    if (Boolean.FALSE.equals(reportGasData.getEvaluatedPerCategory())) {
      summaryData.addKeyValue("Please provide an explanation why you haven’t evaluated the properties for each category",
          reportGasData.getEvaluatedPerCategoryExplanation());
    }

    return SummaryCard.simpleSummaryCard(summaryData);
  }
}
