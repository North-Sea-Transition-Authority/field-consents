package uk.co.nstauthority.fieldconsents.flarevent.summary;

import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryUtil.AVERAGE_PROMPT_WITH_UNIT;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryUtil.CATEGORY_A_HEADING_WITH_UNIT;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryUtil.CATEGORY_B_HEADING_WITH_UNIT;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryUtil.CATEGORY_C_HEADING_WITH_UNIT;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryUtil.CATEGORY_TOTAL_HEADING_WITH_UNIT;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryUtil.COMMENTS_HEADING;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryUtil.MONTH_HEADING;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryUtil.SHUTDOWN_DAYS_HEADING;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryUtil.TOTAL_PROMPT;

import java.time.YearMonth;
import java.util.List;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentReportPeriod;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentRow;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentUnit;
import uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport.FlareReportMonth;
import uk.co.nstauthority.fieldconsents.flarevent.vent.ventreport.VentReportMonth;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.summary.SummaryDataView;
import uk.co.nstauthority.fieldconsents.summary.SummaryTableView;
import uk.co.nstauthority.fieldconsents.util.BigDecimalUtil;

@Service
public class EmissionReportSummaryService {

  public SummaryCard getReportPeriodSummaryCard(FlareVentReportPeriod reportPeriod,
                                                ApplicationType applicationType) {

    var appTypeDisplayName = applicationType.getDisplayName();

    var summaryData = SummaryDataView
        .newWithKeyValue("Which year do you have %s report data up to?".formatted(appTypeDisplayName.toLowerCase()),
            reportPeriod.getReportEndYear())
        .addKeyValue("Which is the latest full month of %s report data you have?".formatted(appTypeDisplayName.toLowerCase()),
            DateUtils.formatFull(reportPeriod.getReportEndMonth()));

    return SummaryCard.simpleSummaryCardWithHeading(
        "%s report period".formatted(appTypeDisplayName),
        summaryData
    );
  }

  public SummaryCard getReportTableSummaryCard(List<? extends FlareVentRow> reportMonths,
                                               FlareVentUnit categoryUnit,
                                               FlareVentUnit averageUnit) {
    var summaryTable = SummaryTableView.newWithHeading(
        MONTH_HEADING,
        CATEGORY_A_HEADING_WITH_UNIT.apply(categoryUnit.getDisplayName()),
        CATEGORY_B_HEADING_WITH_UNIT.apply(categoryUnit.getDisplayName()),
        CATEGORY_C_HEADING_WITH_UNIT.apply(categoryUnit.getDisplayName()),
        CATEGORY_TOTAL_HEADING_WITH_UNIT.apply(categoryUnit.getDisplayName()),
        SHUTDOWN_DAYS_HEADING,
        COMMENTS_HEADING
    );

    var totalDays = 0;
    var shutDownDaysTotal = 0;
    for (var reportMonth : reportMonths) {
      var shutDownDays = getReportMonthShutDownDays(reportMonth);
      summaryTable.addRow(
          DateUtils.formatShort(reportMonth.getMonth(), reportMonth.getYear()),
          reportMonth.getCategoryA(),
          reportMonth.getCategoryB(),
          reportMonth.getCategoryC(),
          BigDecimalUtil.sum(
              reportMonth.getCategoryA(),
              reportMonth.getCategoryB(),
              reportMonth.getCategoryC()
          ),
          shutDownDays,
          reportMonth.getComments()
      );
      totalDays = totalDays + YearMonth.of(reportMonth.getYear(), reportMonth.getMonth()).lengthOfMonth();
      shutDownDaysTotal = shutDownDaysTotal + shutDownDays;
    }

    var categoryATotal = BigDecimalUtil.sum(reportMonths, FlareVentRow::getCategoryA);
    var categoryBTotal = BigDecimalUtil.sum(reportMonths, FlareVentRow::getCategoryB);
    var categoryCTotal = BigDecimalUtil.sum(reportMonths, FlareVentRow::getCategoryC);
    var categoryTotal = BigDecimalUtil.sum(categoryATotal, categoryBTotal, categoryCTotal);

    summaryTable
        .addRow(
            TOTAL_PROMPT,
            categoryATotal,
            categoryBTotal,
            categoryCTotal,
            categoryTotal,
            shutDownDaysTotal,
            null
        )
        .addRow(
            AVERAGE_PROMPT_WITH_UNIT.apply(averageUnit.getDisplayName()),
            null,
            null,
            null,
            BigDecimalUtil.divideRound(categoryTotal, totalDays),
            null,
            null
        );

    return SummaryCard.tableSummaryCard(summaryTable);
  }

  private int getReportMonthShutDownDays(FlareVentRow reportMonth) {
    if (reportMonth instanceof FlareReportMonth flareReportMonth) {
      return flareReportMonth.getShutDownDays();
    } else if (reportMonth instanceof VentReportMonth ventReportMonth) {
      return ventReportMonth.getShutDownDays();
    } else {
      throw new RuntimeException("Unexpected report month class: " + reportMonth.getClass().getName());
    }
  }
}
