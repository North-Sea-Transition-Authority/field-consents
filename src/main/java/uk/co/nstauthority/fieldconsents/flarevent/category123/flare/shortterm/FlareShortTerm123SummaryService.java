package uk.co.nstauthority.fieldconsents.flarevent.category123.flare.shortterm;

import static uk.co.nstauthority.fieldconsents.flarevent.category123.flare.summary.Flare123SummaryUtil.CATEGORY_1_HEADING_WITH_UNIT;
import static uk.co.nstauthority.fieldconsents.flarevent.category123.flare.summary.Flare123SummaryUtil.CATEGORY_2_HEADING_WITH_UNIT;
import static uk.co.nstauthority.fieldconsents.flarevent.category123.flare.summary.Flare123SummaryUtil.CATEGORY_3_HEADING_WITH_UNIT;
import static uk.co.nstauthority.fieldconsents.flarevent.category123.flare.summary.Flare123SummaryUtil.YEAR_MONTH_COMPARATOR;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryUtil.AVERAGE_PROMPT_WITH_UNIT;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryUtil.CATEGORY_TOTAL_HEADING_WITH_UNIT;
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
public class FlareShortTerm123SummaryService {

  private final FlareShortTerm123MonthRepository flareShortTerm123MonthRepository;
  private final ApplicationUnitService applicationUnitService;

  @Autowired
  public FlareShortTerm123SummaryService(FlareShortTerm123MonthRepository flareShortTerm123MonthRepository,
                                         ApplicationUnitService applicationUnitService) {
    this.flareShortTerm123MonthRepository = flareShortTerm123MonthRepository;
    this.applicationUnitService = applicationUnitService;
  }

  private List<FlareShortTerm123Month> getFlareShortTerm123Months(ApplicationVersion applicationVersion) {
    return flareShortTerm123MonthRepository.findAllByApplicationVersion(applicationVersion)
        .stream()
        .sorted(YEAR_MONTH_COMPARATOR)
        .toList();
  }

  public SummaryCard getFlareShortTerm123SummaryCard(ApplicationVersion applicationVersion) {

    var shortTermConsentMonths = getFlareShortTerm123Months(applicationVersion);

    if (shortTermConsentMonths.isEmpty()) {
      return SummaryCard.emptySummaryCard();
    }

    var categoryUnit = applicationUnitService.getFlareCategoryUnit(applicationVersion);
    var averageUnit = applicationUnitService.getFlareAverageUnit(applicationVersion);

    var summaryTable = SummaryTableView.newWithHeading(
        MONTH_HEADING,
        CONSENT_DAYS_HEADING,
        CATEGORY_1_HEADING_WITH_UNIT.apply(categoryUnit.getDisplayName()),
        CATEGORY_2_HEADING_WITH_UNIT.apply(categoryUnit.getDisplayName()),
        CATEGORY_3_HEADING_WITH_UNIT.apply(categoryUnit.getDisplayName()),
        CATEGORY_TOTAL_HEADING_WITH_UNIT.apply(categoryUnit.getDisplayName()),
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
          shortTermConsentMonth.getCategory2(),
          shortTermConsentMonth.getCategory3(),
          BigDecimalUtil.sum(
              shortTermConsentMonth.getCategory1(),
              shortTermConsentMonth.getCategory2(),
              shortTermConsentMonth.getCategory3()
          ),
          shortTermConsentMonth.getComments()
      );
      totalDays = totalDays + consentDays;
    }

    var category1Total = BigDecimalUtil.sum(shortTermConsentMonths, FlareShortTerm123Month::getCategory1);
    var category2Total = BigDecimalUtil.sum(shortTermConsentMonths, FlareShortTerm123Month::getCategory2);
    var category3Total = BigDecimalUtil.sum(shortTermConsentMonths, FlareShortTerm123Month::getCategory3);
    var categoryTotal = BigDecimalUtil.sum(category1Total, category2Total, category3Total);

    summaryTable
        .addRow(
            TOTAL_PROMPT,
            totalDays,
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
            null,
            BigDecimalUtil.divideRound(categoryTotal, totalDays),
            null
        );

    return SummaryCard.tableSummaryCard(summaryTable);
  }
}
