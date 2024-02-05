package uk.co.nstauthority.fieldconsents.flarevent.category123.vent.shortterm;

import static uk.co.nstauthority.fieldconsents.flarevent.category123.vent.summary.Vent123SummaryUtil.CATEGORY_1_HEADING_WITH_UNIT;
import static uk.co.nstauthority.fieldconsents.flarevent.category123.vent.summary.Vent123SummaryUtil.YEAR_MONTH_COMPARATOR;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryUtil.AVERAGE_PROMPT_WITH_UNIT;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryUtil.COMMENTS_HEADING;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryUtil.CONSENT_DAYS_HEADING;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryUtil.MONTH_HEADING;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryUtil.TOTAL_PROMPT;

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
public class VentShortTerm123SummaryService {

  private final VentShortTerm123MonthRepository ventShortTerm123MonthRepository;
  private final ApplicationUnitService applicationUnitService;

  @Autowired
  public VentShortTerm123SummaryService(VentShortTerm123MonthRepository ventShortTerm123MonthRepository,
                                        ApplicationUnitService applicationUnitService) {
    this.ventShortTerm123MonthRepository = ventShortTerm123MonthRepository;
    this.applicationUnitService = applicationUnitService;
  }

  private List<VentShortTerm123Month> getVentShortTerm123Months(ApplicationVersion applicationVersion) {
    return ventShortTerm123MonthRepository.findAllByApplicationVersion(applicationVersion)
        .stream()
        .sorted(YEAR_MONTH_COMPARATOR)
        .toList();
  }

  public SummaryCard getVentShortTerm123SummaryCard(ApplicationVersion applicationVersion) {

    var shortTermConsentMonths = getVentShortTerm123Months(applicationVersion);

    if (shortTermConsentMonths.isEmpty()) {
      return SummaryCard.emptySummaryCard();
    }

    var categoryUnit = applicationUnitService.getVentCategoryUnit(applicationVersion);
    var averageUnit = applicationUnitService.getVentAverageUnit(applicationVersion);

    var summaryTable = SummaryTableView.newWithHeading(
        MONTH_HEADING,
        CONSENT_DAYS_HEADING,
        CATEGORY_1_HEADING_WITH_UNIT.apply(categoryUnit.getDisplayName()),
        COMMENTS_HEADING
    );

    var totalDays = 0;
    for (var shortTermConsentMonth : shortTermConsentMonths) {
      var consentDays = DateUtils.daysBetweenInclusive(
          shortTermConsentMonth.getStartDate(), shortTermConsentMonth.getEndDate());
      summaryTable.addRow(
          DateUtils.formatShort(shortTermConsentMonth.getMonth(), shortTermConsentMonth.getYear()),
          consentDays,
          shortTermConsentMonth.getCategory1(),
          shortTermConsentMonth.getComments()
      );
      totalDays = totalDays + consentDays;
    }

    var category1Total = BigDecimalUtil.sum(shortTermConsentMonths, VentShortTerm123Month::getCategory1);

    summaryTable
        .addRow(
            TOTAL_PROMPT,
            totalDays,
            category1Total,
            null
        )
        .addRow(
            AVERAGE_PROMPT_WITH_UNIT.apply(averageUnit.getDisplayName()),
            null,
            BigDecimalUtil.divideRound(category1Total, totalDays),
            null
        );

    return SummaryCard.tableSummaryCard(summaryTable);
  }
}
