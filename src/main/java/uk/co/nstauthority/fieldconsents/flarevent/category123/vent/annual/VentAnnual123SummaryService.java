package uk.co.nstauthority.fieldconsents.flarevent.category123.vent.annual;

import static uk.co.nstauthority.fieldconsents.flarevent.category123.vent.summary.Vent123SummaryUtil.CATEGORY_1_HEADING_WITH_UNIT;
import static uk.co.nstauthority.fieldconsents.flarevent.category123.vent.summary.Vent123SummaryUtil.YEAR_MONTH_COMPARATOR;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryUtil.AVERAGE_PROMPT_WITH_UNIT;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryUtil.COMMENTS_HEADING;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryUtil.MONTH_HEADING;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryUtil.TOTAL_PROMPT;

import java.time.YearMonth;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.unit.ApplicationUnitService;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.summary.SummaryTableView;
import uk.co.nstauthority.fieldconsents.util.BigDecimalUtil;

@Service
public class VentAnnual123SummaryService {

  private final VentAnnual123MonthRepository ventAnnual123MonthRepository;

  private final ApplicationUnitService applicationUnitService;

  @Autowired
  public VentAnnual123SummaryService(VentAnnual123MonthRepository ventAnnual123MonthRepository,
                                     ApplicationUnitService applicationUnitService) {
    this.ventAnnual123MonthRepository = ventAnnual123MonthRepository;
    this.applicationUnitService = applicationUnitService;
  }

  private List<VentAnnual123Month> getVentAnnual123Months(ApplicationVersion applicationVersion) {
    return ventAnnual123MonthRepository.findAllByApplicationVersion(applicationVersion)
        .stream()
        .sorted(YEAR_MONTH_COMPARATOR)
        .toList();
  }

  public SummaryCard getVentAnnual123SummaryCard(ApplicationVersion applicationVersion) {

    var annualConsentMonths = getVentAnnual123Months(applicationVersion);

    if (annualConsentMonths.isEmpty()) {
      return SummaryCard.emptySummaryCard();
    }

    var categoryUnit = applicationUnitService.getVentCategoryUnit(applicationVersion);
    var averageUnit = applicationUnitService.getVentAverageUnit(applicationVersion);

    var summaryTable = SummaryTableView.newWithHeading(
        MONTH_HEADING,
        CATEGORY_1_HEADING_WITH_UNIT.apply(categoryUnit.getDisplayName()),
        COMMENTS_HEADING
    );

    var totalDays = 0;
    for (var annualConsentMonth : annualConsentMonths) {
      summaryTable.addRow(
          DateUtils.formatShort(annualConsentMonth.getMonth(), annualConsentMonth.getYear()),
          annualConsentMonth.getCategory1(),
          annualConsentMonth.getComments()
      );
      totalDays = totalDays + YearMonth.of(annualConsentMonth.getYear(), annualConsentMonth.getMonth()).lengthOfMonth();
    }

    var category1Total = BigDecimalUtil.sum(annualConsentMonths, VentAnnual123Month::getCategory1);

    summaryTable
        .addRow(
            TOTAL_PROMPT,
            category1Total,
            null
        )
        .addRow(
            AVERAGE_PROMPT_WITH_UNIT.apply(averageUnit.getDisplayName()),
            BigDecimalUtil.divideRound(category1Total, totalDays),
            null
        );

    return SummaryCard.tableSummaryCard(summaryTable);
  }
}
