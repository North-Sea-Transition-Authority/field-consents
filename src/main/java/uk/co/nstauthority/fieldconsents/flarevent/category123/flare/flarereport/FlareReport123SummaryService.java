package uk.co.nstauthority.fieldconsents.flarevent.category123.flare.flarereport;

import static uk.co.nstauthority.fieldconsents.flarevent.category123.flare.summary.Flare123SummaryUtil.CATEGORY_1_HEADING_WITH_UNIT;
import static uk.co.nstauthority.fieldconsents.flarevent.category123.flare.summary.Flare123SummaryUtil.CATEGORY_2_HEADING_WITH_UNIT;
import static uk.co.nstauthority.fieldconsents.flarevent.category123.flare.summary.Flare123SummaryUtil.CATEGORY_3_HEADING_WITH_UNIT;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryUtil.AVERAGE_PROMPT_WITH_UNIT;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryUtil.CATEGORY_TOTAL_HEADING_WITH_UNIT;
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
public class FlareReport123SummaryService {

  private final FlareReport123Service flareReport123Service;
  private final ApplicationUnitService applicationUnitService;

  @Autowired
  public FlareReport123SummaryService(FlareReport123Service flareReport123Service,
                                      ApplicationUnitService applicationUnitService) {
    this.flareReport123Service = flareReport123Service;
    this.applicationUnitService = applicationUnitService;
  }

  public SummaryCard getFlareReport123SummaryCard(ApplicationVersion applicationVersion) {
    var reportMonths = flareReport123Service.getFlareReport123Months(applicationVersion);

    if (reportMonths.isEmpty()) {
      return SummaryCard.emptySummaryCard();
    }

    var categoryUnit = applicationUnitService.getFlareCategoryUnit(applicationVersion);
    var averageUnit = applicationUnitService.getFlareAverageUnit(applicationVersion);

    var summaryTable = SummaryTableView.newWithHeading(
        MONTH_HEADING,
        CATEGORY_1_HEADING_WITH_UNIT.apply(categoryUnit.getDisplayName()),
        CATEGORY_2_HEADING_WITH_UNIT.apply(categoryUnit.getDisplayName()),
        CATEGORY_3_HEADING_WITH_UNIT.apply(categoryUnit.getDisplayName()),
        CATEGORY_TOTAL_HEADING_WITH_UNIT.apply(categoryUnit.getDisplayName()),
        SHUTDOWN_DAYS_HEADING,
        COMMENTS_HEADING
    );

    var totalDays = 0;
    var shutDownDaysTotal = 0;
    for (var reportMonth : reportMonths) {
      summaryTable.addRow(
          DateUtils.formatShort(reportMonth.getMonth(), reportMonth.getYear()),
          reportMonth.getCategory1(),
          reportMonth.getCategory2(),
          reportMonth.getCategory3(),
          BigDecimalUtil.sum(
              reportMonth.getCategory1(),
              reportMonth.getCategory2(),
              reportMonth.getCategory3()
          ),
          reportMonth.getShutDownDays(),
          reportMonth.getComments()
      );
      totalDays = totalDays + YearMonth.of(reportMonth.getYear(), reportMonth.getMonth()).lengthOfMonth();
      shutDownDaysTotal = shutDownDaysTotal + reportMonth.getShutDownDays();
    }

    var category1Total = BigDecimalUtil.sum(reportMonths, FlareReport123Month::getCategory1);
    var category2Total = BigDecimalUtil.sum(reportMonths, FlareReport123Month::getCategory2);
    var category3Total = BigDecimalUtil.sum(reportMonths, FlareReport123Month::getCategory3);
    var categoryTotal = BigDecimalUtil.sum(category1Total, category2Total, category3Total);

    summaryTable
        .addRow(
            TOTAL_PROMPT,
            category1Total,
            category2Total,
            category3Total,
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
}
