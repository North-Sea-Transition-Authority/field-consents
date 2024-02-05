package uk.co.nstauthority.fieldconsents.flarevent.category123.vent.ventreport;

import static uk.co.nstauthority.fieldconsents.flarevent.category123.vent.summary.Vent123SummaryUtil.CATEGORY_1_HEADING_WITH_UNIT;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryUtil.AVERAGE_PROMPT_WITH_UNIT;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryUtil.COMMENTS_HEADING;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryUtil.MONTH_HEADING;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryUtil.SHUTDOWN_DAYS_HEADING;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryUtil.TOTAL_PROMPT;

import java.time.YearMonth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.unit.ApplicationUnitService;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.summary.SummaryTableView;
import uk.co.nstauthority.fieldconsents.util.BigDecimalUtil;

@Service
public class VentReport123SummaryService {

  private final VentReport123Service ventReport123Service;
  private final ApplicationUnitService applicationUnitService;

  @Autowired
  public VentReport123SummaryService(VentReport123Service ventReport123Service,
                                     ApplicationUnitService applicationUnitService) {
    this.ventReport123Service = ventReport123Service;
    this.applicationUnitService = applicationUnitService;
  }

  public SummaryCard getVentReport123SummaryCard(ApplicationVersion applicationVersion) {
    var reportMonths = ventReport123Service.getVentReport123Months(applicationVersion);

    if (reportMonths.isEmpty()) {
      return SummaryCard.emptySummaryCard();
    }

    var categoryUnit = applicationUnitService.getVentCategoryUnit(applicationVersion);
    var averageUnit = applicationUnitService.getVentAverageUnit(applicationVersion);

    var summaryTable = SummaryTableView.newWithHeading(
        MONTH_HEADING,
        CATEGORY_1_HEADING_WITH_UNIT.apply(categoryUnit.getDisplayName()),
        SHUTDOWN_DAYS_HEADING,
        COMMENTS_HEADING
    );

    var totalDays = 0;
    var shutDownDaysTotal = 0;
    for (var reportMonth : reportMonths) {
      summaryTable.addRow(
          DateUtils.formatShort(reportMonth.getMonth(), reportMonth.getYear()),
          reportMonth.getCategory1(),
          reportMonth.getShutDownDays(),
          reportMonth.getComments()
      );
      totalDays = totalDays + YearMonth.of(reportMonth.getYear(), reportMonth.getMonth()).lengthOfMonth();
      shutDownDaysTotal = shutDownDaysTotal + reportMonth.getShutDownDays();
    }

    var category1Total = BigDecimalUtil.sum(reportMonths, VentReport123Month::getCategory1);

    summaryTable
        .addRow(
            TOTAL_PROMPT,
            category1Total,
            shutDownDaysTotal,
            null
        )
        .addRow(
            AVERAGE_PROMPT_WITH_UNIT.apply(averageUnit.getDisplayName()),
            BigDecimalUtil.divideRound(category1Total, totalDays),
            null,
            null
        );

    return SummaryCard.tableSummaryCard(summaryTable);
  }
}
