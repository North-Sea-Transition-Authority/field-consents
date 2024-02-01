package uk.co.nstauthority.fieldconsents.flarevent.category123.flare.annual;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.flarevent.category123.flare.Flare123SummaryTestUtil.getCategory1Total;
import static uk.co.nstauthority.fieldconsents.flarevent.category123.flare.Flare123SummaryTestUtil.getCategory2Total;
import static uk.co.nstauthority.fieldconsents.flarevent.category123.flare.Flare123SummaryTestUtil.getCategory3Total;
import static uk.co.nstauthority.fieldconsents.flarevent.category123.flare.summary.Flare123SummaryUtil.CATEGORY_1_HEADING_WITH_UNIT;
import static uk.co.nstauthority.fieldconsents.flarevent.category123.flare.summary.Flare123SummaryUtil.CATEGORY_2_HEADING_WITH_UNIT;
import static uk.co.nstauthority.fieldconsents.flarevent.category123.flare.summary.Flare123SummaryUtil.CATEGORY_3_HEADING_WITH_UNIT;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryUtil.AVERAGE_PROMPT_WITH_UNIT;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryUtil.CATEGORY_TOTAL_HEADING_WITH_UNIT;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryUtil.COMMENTS_HEADING;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryUtil.MONTH_HEADING;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryUtil.TOTAL_PROMPT;
import static uk.co.nstauthority.fieldconsents.formatting.DecimalFormatUtils.bigDecimalToFormattedString;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.unit.ApplicationUnitService;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentUnit;
import uk.co.nstauthority.fieldconsents.flarevent.category123.flare.Flare123Row;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.summary.SummaryCardType;
import uk.co.nstauthority.fieldconsents.summary.SummaryTableRow;
import uk.co.nstauthority.fieldconsents.summary.SummaryTableView;
import uk.co.nstauthority.fieldconsents.util.BigDecimalUtil;

@ExtendWith(MockitoExtension.class)
class FlareAnnual123SummaryServiceTest {

  @Mock
  private FlareAnnual123MonthRepository flareAnnual123MonthRepository;

  @Mock
  private ApplicationUnitService applicationUnitService;

  @InjectMocks
  private FlareAnnual123SummaryService flareAnnual123SummaryService;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.FLARE);
  }

  @Test
  void getFlareAnnual123SummaryCard_noAnnualMonths() {
    when(flareAnnual123MonthRepository.findAllByApplicationVersion(applicationVersion))
        .thenReturn(Collections.emptyList());

    assertThat(flareAnnual123SummaryService.getFlareAnnual123SummaryCard(applicationVersion))
        .isEqualTo(SummaryCard.emptySummaryCard());
  }

  @Test
  void getFlareAnnual123SummaryCard_annualMonthsExist() {
    var flareAnnual123Months = FlareAnnual123TestUtil.getFlareAnnual123MonthsForYear(applicationVersion, 2022);
    var categoryUnit = FlareVentUnit.TONNES_PER_MONTH;
    var averageUnit = FlareVentUnit.TONNES_PER_DAY;

    when(flareAnnual123MonthRepository.findAllByApplicationVersion(applicationVersion))
        .thenReturn(flareAnnual123Months);
    when(applicationUnitService.getFlareCategoryUnit(applicationVersion))
        .thenReturn(categoryUnit);
    when(applicationUnitService.getFlareAverageUnit(applicationVersion))
        .thenReturn(averageUnit);

    var totalDays = flareAnnual123Months.stream()
        .mapToInt(consentMonth -> YearMonth.of(consentMonth.getYear(), consentMonth.getMonth()).lengthOfMonth())
        .sum();
    var category1Total = getCategory1Total(flareAnnual123Months);
    var category2Total = getCategory2Total(flareAnnual123Months);
    var category3Total = getCategory3Total(flareAnnual123Months);
    var categoryTotal = BigDecimalUtil.sum(category1Total, category2Total, category3Total);

    assertThat(flareAnnual123SummaryService.getFlareAnnual123SummaryCard(applicationVersion))
        .isEqualTo(
            new SummaryCard(
                null,
                SummaryCardType.TABLE_SUMMARY,
                new SummaryTableView(
                    List.of(
                        getSummaryTableRowAnnualHeading(categoryUnit),
                        getSummaryTableRowForAnnualMonth(flareAnnual123Months.get(0)),
                        getSummaryTableRowForAnnualMonth(flareAnnual123Months.get(1)),
                        getSummaryTableRowForAnnualMonth(flareAnnual123Months.get(2)),
                        getSummaryTableRowForAnnualMonth(flareAnnual123Months.get(3)),
                        getSummaryTableRowForAnnualMonth(flareAnnual123Months.get(4)),
                        getSummaryTableRowForAnnualMonth(flareAnnual123Months.get(5)),
                        getSummaryTableRowForAnnualMonth(flareAnnual123Months.get(6)),
                        getSummaryTableRowForAnnualMonth(flareAnnual123Months.get(7)),
                        getSummaryTableRowForAnnualMonth(flareAnnual123Months.get(8)),
                        getSummaryTableRowForAnnualMonth(flareAnnual123Months.get(9)),
                        getSummaryTableRowForAnnualMonth(flareAnnual123Months.get(10)),
                        getSummaryTableRowForAnnualMonth(flareAnnual123Months.get(11)),
                        getSummaryTableRowOfAnnualTotals(category1Total, category2Total, category3Total, categoryTotal),
                        getSummaryTableRowWithAnnualAverageData(averageUnit, categoryTotal, totalDays)
                    )
                )
            )
        );
  }

  private SummaryTableRow getSummaryTableRowAnnualHeading(FlareVentUnit categoryUnit) {
    return new SummaryTableRow(List.of(
        MONTH_HEADING,
        CATEGORY_1_HEADING_WITH_UNIT.apply(categoryUnit.getDisplayName()),
        CATEGORY_2_HEADING_WITH_UNIT.apply(categoryUnit.getDisplayName()),
        CATEGORY_3_HEADING_WITH_UNIT.apply(categoryUnit.getDisplayName()),
        CATEGORY_TOTAL_HEADING_WITH_UNIT.apply(categoryUnit.getDisplayName()),
        COMMENTS_HEADING
    ));
  }

  private SummaryTableRow getSummaryTableRowForAnnualMonth(Flare123Row consentMonth) {
    return new SummaryTableRow(List.of(
        DateUtils.formatShort(consentMonth.getMonth(), consentMonth.getYear()),
        bigDecimalToFormattedString(consentMonth.getCategory1()),
        bigDecimalToFormattedString(consentMonth.getCategory2()),
        bigDecimalToFormattedString(consentMonth.getCategory3()),
        bigDecimalToFormattedString(BigDecimalUtil.sum(
            consentMonth.getCategory1(),
            consentMonth.getCategory2(),
            consentMonth.getCategory3()
        )),
        consentMonth.getComments()
    ));
  }

  private SummaryTableRow getSummaryTableRowOfAnnualTotals(BigDecimal category1Total,
                                                           BigDecimal category2Total,
                                                           BigDecimal category3Total,
                                                           BigDecimal categoryTotal) {
    return new SummaryTableRow(Stream.of(
        TOTAL_PROMPT,
        bigDecimalToFormattedString(category1Total),
        bigDecimalToFormattedString(category2Total),
        bigDecimalToFormattedString(category3Total),
        String.valueOf(categoryTotal),
        null).toList()
    );
  }

  private SummaryTableRow getSummaryTableRowWithAnnualAverageData(FlareVentUnit averageUnit,
                                                                  BigDecimal categoryTotal,
                                                                  int totalDays) {
    return new SummaryTableRow(Stream.of(
        AVERAGE_PROMPT_WITH_UNIT.apply(averageUnit.getDisplayName()),
        null,
        null,
        null,
        bigDecimalToFormattedString(BigDecimalUtil.divideRound(categoryTotal, totalDays)),
        null).toList()
    );
  }
}
