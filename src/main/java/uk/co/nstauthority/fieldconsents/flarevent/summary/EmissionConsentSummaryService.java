package uk.co.nstauthority.fieldconsents.flarevent.summary;

import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryUtil.AVERAGE_PROMPT_WITH_UNIT;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryUtil.CATEGORY_A_HEADING_WITH_UNIT;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryUtil.CATEGORY_B_HEADING_WITH_UNIT;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryUtil.CATEGORY_C_HEADING_WITH_UNIT;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryUtil.CATEGORY_TOTAL_HEADING_WITH_UNIT;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryUtil.COMMENTS_HEADING;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryUtil.CONSENT_DAYS_HEADING;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryUtil.GAS_HEADING_WITH_UNIT;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryUtil.MONTH_HEADING;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryUtil.TOTAL_PROMPT;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryUtil.YEAR_HEADING;

import java.time.YearMonth;
import java.util.List;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.flarevent.EmissionLongTermYear;
import uk.co.nstauthority.fieldconsents.flarevent.EmissionShortTermUtil;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentRow;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentUnit;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.summary.SummaryTableView;
import uk.co.nstauthority.fieldconsents.util.BigDecimalUtil;

@Service
public class EmissionConsentSummaryService {

  public SummaryCard getShortTermConsentSummaryCard(List<? extends FlareVentRow> shortTermConsentMonths,
                                                    FlareVentUnit categoryUnit,
                                                    FlareVentUnit averageUnit) {

    var summaryTable = SummaryTableView.newWithHeading(
        MONTH_HEADING,
        CONSENT_DAYS_HEADING,
        CATEGORY_A_HEADING_WITH_UNIT.apply(categoryUnit.getDisplayName()),
        CATEGORY_B_HEADING_WITH_UNIT.apply(categoryUnit.getDisplayName()),
        CATEGORY_C_HEADING_WITH_UNIT.apply(categoryUnit.getDisplayName()),
        CATEGORY_TOTAL_HEADING_WITH_UNIT.apply(categoryUnit.getDisplayName()),
        COMMENTS_HEADING
    );

    var totalDays = 0;
    for (var shortTermConsentMonth : shortTermConsentMonths) {
      var consentDays = EmissionShortTermUtil.getShortTermMonthConsentDays(shortTermConsentMonth);
      summaryTable.addRow(
          DateUtils.formatShort(shortTermConsentMonth.getMonth(), shortTermConsentMonth.getYear()),
          consentDays,
          shortTermConsentMonth.getCategoryA(),
          shortTermConsentMonth.getCategoryB(),
          shortTermConsentMonth.getCategoryC(),
          BigDecimalUtil.sum(
              shortTermConsentMonth.getCategoryA(),
              shortTermConsentMonth.getCategoryB(),
              shortTermConsentMonth.getCategoryC()
          ),
          shortTermConsentMonth.getComments()
      );
      totalDays = totalDays + consentDays;
    }

    var categoryATotal = BigDecimalUtil.sum(shortTermConsentMonths, FlareVentRow::getCategoryA);
    var categoryBTotal = BigDecimalUtil.sum(shortTermConsentMonths, FlareVentRow::getCategoryB);
    var categoryCTotal = BigDecimalUtil.sum(shortTermConsentMonths, FlareVentRow::getCategoryC);
    var categoryTotal = BigDecimalUtil.sum(categoryATotal, categoryBTotal, categoryCTotal);

    summaryTable
        .addRow(
            TOTAL_PROMPT,
            totalDays,
            categoryATotal,
            categoryBTotal,
            categoryCTotal,
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

  public SummaryCard getAnnualConsentSummaryCard(List<? extends FlareVentRow> annualConsentMonths,
                                                 FlareVentUnit categoryUnit,
                                                 FlareVentUnit averageUnit) {

    var summaryTable = SummaryTableView.newWithHeading(
        MONTH_HEADING,
        CATEGORY_A_HEADING_WITH_UNIT.apply(categoryUnit.getDisplayName()),
        CATEGORY_B_HEADING_WITH_UNIT.apply(categoryUnit.getDisplayName()),
        CATEGORY_C_HEADING_WITH_UNIT.apply(categoryUnit.getDisplayName()),
        CATEGORY_TOTAL_HEADING_WITH_UNIT.apply(categoryUnit.getDisplayName()),
        COMMENTS_HEADING
    );

    var totalDays = 0;
    for (var annualConsentMonth : annualConsentMonths) {
      summaryTable.addRow(
          DateUtils.formatShort(annualConsentMonth.getMonth(), annualConsentMonth.getYear()),
          annualConsentMonth.getCategoryA(),
          annualConsentMonth.getCategoryB(),
          annualConsentMonth.getCategoryC(),
          BigDecimalUtil.sum(
              annualConsentMonth.getCategoryA(),
              annualConsentMonth.getCategoryB(),
              annualConsentMonth.getCategoryC()
          ),
          annualConsentMonth.getComments()
      );
      totalDays = totalDays + YearMonth.of(annualConsentMonth.getYear(), annualConsentMonth.getMonth()).lengthOfMonth();
    }

    var categoryATotal = BigDecimalUtil.sum(annualConsentMonths, FlareVentRow::getCategoryA);
    var categoryBTotal = BigDecimalUtil.sum(annualConsentMonths, FlareVentRow::getCategoryB);
    var categoryCTotal = BigDecimalUtil.sum(annualConsentMonths, FlareVentRow::getCategoryC);
    var categoryTotal = BigDecimalUtil.sum(categoryATotal, categoryBTotal, categoryCTotal);

    summaryTable
        .addRow(
            TOTAL_PROMPT,
            categoryATotal,
            categoryBTotal,
            categoryCTotal,
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

  // migrated cases only
  public SummaryCard getLongTermConsentSummaryCard(List<? extends EmissionLongTermYear> emissionLongTermYears,
                                                   FlareVentUnit gasUnit) {

    var summaryTable = SummaryTableView.newWithHeading(
        YEAR_HEADING,
        GAS_HEADING_WITH_UNIT.apply(gasUnit.getDisplayName())
    );

    for (var emissionLongTermYear : emissionLongTermYears) {
      summaryTable.addRow(
          emissionLongTermYear.getYear(),
          emissionLongTermYear.getGas()
      );
    }

    return SummaryCard.tableSummaryCard(summaryTable);
  }
}
