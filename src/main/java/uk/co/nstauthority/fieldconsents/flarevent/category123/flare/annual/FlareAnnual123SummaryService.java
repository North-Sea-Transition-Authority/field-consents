package uk.co.nstauthority.fieldconsents.flarevent.category123.flare.annual;

import static uk.co.nstauthority.fieldconsents.flarevent.category123.flare.summary.Flare123SummaryUtil.CATEGORY_1_HEADING_WITH_UNIT;
import static uk.co.nstauthority.fieldconsents.flarevent.category123.flare.summary.Flare123SummaryUtil.CATEGORY_2_HEADING_WITH_UNIT;
import static uk.co.nstauthority.fieldconsents.flarevent.category123.flare.summary.Flare123SummaryUtil.CATEGORY_3_HEADING_WITH_UNIT;
import static uk.co.nstauthority.fieldconsents.flarevent.category123.flare.summary.Flare123SummaryUtil.YEAR_MONTH_COMPARATOR;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryUtil.AVERAGE_PROMPT_WITH_UNIT;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryUtil.CATEGORY_TOTAL_HEADING_WITH_UNIT;
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
public class FlareAnnual123SummaryService {

  private final FlareAnnual123MonthRepository flareAnnual123MonthRepository;

  private final ApplicationUnitService applicationUnitService;

  @Autowired
  public FlareAnnual123SummaryService(FlareAnnual123MonthRepository flareAnnual123MonthRepository,
                                      ApplicationUnitService applicationUnitService) {
    this.flareAnnual123MonthRepository = flareAnnual123MonthRepository;
    this.applicationUnitService = applicationUnitService;
  }

  private List<FlareAnnual123Month> getFlareAnnual123Months(ApplicationVersion applicationVersion) {
    return flareAnnual123MonthRepository.findAllByApplicationVersion(applicationVersion)
        .stream()
        .sorted(YEAR_MONTH_COMPARATOR)
        .toList();
  }

  public SummaryCard getFlareAnnual123SummaryCard(ApplicationVersion applicationVersion) {

    var annualConsentMonths = getFlareAnnual123Months(applicationVersion);

    if (annualConsentMonths.isEmpty()) {
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
        COMMENTS_HEADING
    );

    var totalDays = 0;
    for (var annualConsentMonth : annualConsentMonths) {
      summaryTable.addRow(
          DateUtils.formatShort(annualConsentMonth.getMonth(), annualConsentMonth.getYear()),
          annualConsentMonth.getCategory1(),
          annualConsentMonth.getCategory2(),
          annualConsentMonth.getCategory3(),
          BigDecimalUtil.sum(
              annualConsentMonth.getCategory1(),
              annualConsentMonth.getCategory2(),
              annualConsentMonth.getCategory3()
          ),
          annualConsentMonth.getComments()
      );
      totalDays = totalDays + YearMonth.of(annualConsentMonth.getYear(), annualConsentMonth.getMonth()).lengthOfMonth();
    }

    var category1Total = BigDecimalUtil.sum(annualConsentMonths, FlareAnnual123Month::getCategory1);
    var category2Total = BigDecimalUtil.sum(annualConsentMonths, FlareAnnual123Month::getCategory2);
    var category3Total = BigDecimalUtil.sum(annualConsentMonths, FlareAnnual123Month::getCategory3);
    var categoryTotal = BigDecimalUtil.sum(category1Total, category2Total, category3Total);

    summaryTable
        .addRow(
            TOTAL_PROMPT,
            category1Total,
            category2Total,
            category3Total,
            categoryTotal,
            null
        )
        .addRow(
            AVERAGE_PROMPT_WITH_UNIT.apply(averageUnit.getDisplayName()),
            null,
            null,
            null,
            BigDecimalUtil.divideRound(categoryTotal, totalDays),
            null
        );

    return SummaryCard.tableSummaryCard(summaryTable);
  }
}
