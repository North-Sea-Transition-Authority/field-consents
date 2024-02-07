package uk.co.nstauthority.fieldconsents.flarevent.summary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthTestUtil.SHORT_TERM_END_DATE;
import static uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthTestUtil.SHORT_TERM_START_DATE;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryTestUtil.getCategoryATotal;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryTestUtil.getCategoryBTotal;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryTestUtil.getCategoryCTotal;
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
import static uk.co.nstauthority.fieldconsents.formatting.DecimalFormatUtils.bigDecimalToFormattedString;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.flarevent.EmissionLongTermYear;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentRow;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentUnit;
import uk.co.nstauthority.fieldconsents.flarevent.flare.annual.FlareAnnualTestUtil;
import uk.co.nstauthority.fieldconsents.flarevent.flare.longterm.FlareLongTermTestUtil;
import uk.co.nstauthority.fieldconsents.flarevent.flare.shortterm.FlareShortTermMonth;
import uk.co.nstauthority.fieldconsents.flarevent.flare.shortterm.FlareShortTermTestUtil;
import uk.co.nstauthority.fieldconsents.flarevent.vent.annual.VentAnnualTestUtil;
import uk.co.nstauthority.fieldconsents.flarevent.vent.longterm.VentLongTermTestUtil;
import uk.co.nstauthority.fieldconsents.flarevent.vent.shortterm.VentShortTermMonth;
import uk.co.nstauthority.fieldconsents.flarevent.vent.shortterm.VentShortTermTestUtil;
import uk.co.nstauthority.fieldconsents.flarevent.vent.ventreport.VentReportTestUtil;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.summary.SummaryCardType;
import uk.co.nstauthority.fieldconsents.summary.SummaryTableRow;
import uk.co.nstauthority.fieldconsents.summary.SummaryTableView;
import uk.co.nstauthority.fieldconsents.util.BigDecimalUtil;

@ExtendWith(MockitoExtension.class)
class EmissionConsentSummaryServiceTest {

  @InjectMocks
  private EmissionConsentSummaryService emissionConsentSummaryService;

  @Test
  void getShortTermConsentSummaryCard_flare() {
    var consentMonths = FlareShortTermTestUtil.getFlareShortTermMonthsForPeriod(
        ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.FLARE),
        SHORT_TERM_START_DATE,
        SHORT_TERM_END_DATE
    );
    var categoryUnit = FlareVentUnit.TONNES_PER_MONTH;
    var averageUnit = FlareVentUnit.TONNES_PER_DAY;

    var totalDays = consentMonths.stream()
        .mapToInt(this::getFlareShortTermConsentDays)
        .sum();
    var categoryATotal = getCategoryATotal(consentMonths);
    var categoryBTotal = getCategoryBTotal(consentMonths);
    var categoryCTotal = getCategoryCTotal(consentMonths);
    var categoryTotal = BigDecimalUtil.sum(categoryATotal, categoryBTotal, categoryCTotal);

    assertThat(emissionConsentSummaryService.getShortTermConsentSummaryCard(consentMonths, categoryUnit, averageUnit))
        .isEqualTo(
            new SummaryCard(
                null,
                SummaryCardType.TABLE_SUMMARY,
                new SummaryTableView(
                    List.of(
                        getSummaryTableRowShortTermHeading(categoryUnit),
                        getSummaryTableRowForShortTermMonth(consentMonths.get(0), getFlareShortTermConsentDays(consentMonths.get(0))),
                        getSummaryTableRowForShortTermMonth(consentMonths.get(1), getFlareShortTermConsentDays(consentMonths.get(1))),
                        getSummaryTableRowForShortTermMonth(consentMonths.get(2), getFlareShortTermConsentDays(consentMonths.get(2))),
                        getSummaryTableRowForShortTermMonth(consentMonths.get(3), getFlareShortTermConsentDays(consentMonths.get(3))),
                        getSummaryTableRowForShortTermMonth(consentMonths.get(4), getFlareShortTermConsentDays(consentMonths.get(4))),
                        getSummaryTableRowForShortTermMonth(consentMonths.get(5), getFlareShortTermConsentDays(consentMonths.get(5))),
                        getSummaryTableRowForShortTermMonth(consentMonths.get(6), getFlareShortTermConsentDays(consentMonths.get(6))),
                        getSummaryTableRowOfShortTermTotals(categoryATotal, categoryBTotal, categoryCTotal, categoryTotal,
                            totalDays),
                        getSummaryTableRowWithShortTermAverageData(averageUnit, categoryTotal, totalDays)
                    )
                )
            )
        );
  }

  @Test
  void getShortTermConsentSummaryCard_vent() {
    var consentMonths = VentShortTermTestUtil.getVentShortTermMonthsForPeriod(
        ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.VENT),
        SHORT_TERM_START_DATE,
        SHORT_TERM_END_DATE
    );
    var categoryUnit = FlareVentUnit.TONNES_PER_MONTH;
    var averageUnit = FlareVentUnit.TONNES_PER_DAY;

    var totalDays = consentMonths.stream()
        .mapToInt(this::getVentShortTermConsentDays)
        .sum();
    var categoryATotal = getCategoryATotal(consentMonths);
    var categoryBTotal = getCategoryBTotal(consentMonths);
    var categoryCTotal = getCategoryCTotal(consentMonths);
    var categoryTotal = BigDecimalUtil.sum(categoryATotal, categoryBTotal, categoryCTotal);

    assertThat(emissionConsentSummaryService.getShortTermConsentSummaryCard(consentMonths, categoryUnit, averageUnit))
        .isEqualTo(
            new SummaryCard(
                null,
                SummaryCardType.TABLE_SUMMARY,
                new SummaryTableView(
                    List.of(
                        getSummaryTableRowShortTermHeading(categoryUnit),
                        getSummaryTableRowForShortTermMonth(consentMonths.get(0), getVentShortTermConsentDays(consentMonths.get(0))),
                        getSummaryTableRowForShortTermMonth(consentMonths.get(1), getVentShortTermConsentDays(consentMonths.get(1))),
                        getSummaryTableRowForShortTermMonth(consentMonths.get(2), getVentShortTermConsentDays(consentMonths.get(2))),
                        getSummaryTableRowForShortTermMonth(consentMonths.get(3), getVentShortTermConsentDays(consentMonths.get(3))),
                        getSummaryTableRowForShortTermMonth(consentMonths.get(4), getVentShortTermConsentDays(consentMonths.get(4))),
                        getSummaryTableRowForShortTermMonth(consentMonths.get(5), getVentShortTermConsentDays(consentMonths.get(5))),
                        getSummaryTableRowForShortTermMonth(consentMonths.get(6), getVentShortTermConsentDays(consentMonths.get(6))),
                        getSummaryTableRowOfShortTermTotals(categoryATotal, categoryBTotal, categoryCTotal, categoryTotal,
                            totalDays),
                        getSummaryTableRowWithShortTermAverageData(averageUnit, categoryTotal, totalDays)
                    )
                )
            )
        );
  }

  @Test
  void getShortTermConsentSummaryCard_unexpectedConsentMonthClass() {
    var reportMonths = VentReportTestUtil.getVentReportMonthsForYear(
        ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.VENT), 2023);
    var categoryUnit = FlareVentUnit.TONNES_PER_MONTH;
    var averageUnit = FlareVentUnit.TONNES_PER_DAY;

    assertThatThrownBy(() ->
        emissionConsentSummaryService.getShortTermConsentSummaryCard(reportMonths, categoryUnit, averageUnit))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Unexpected consent month class: " + reportMonths.get(0).getClass().getName());
  }

  private SummaryTableRow getSummaryTableRowShortTermHeading(FlareVentUnit categoryUnit) {
    return new SummaryTableRow(List.of(
        MONTH_HEADING,
        CONSENT_DAYS_HEADING,
        CATEGORY_A_HEADING_WITH_UNIT.apply(categoryUnit.getDisplayName()),
        CATEGORY_B_HEADING_WITH_UNIT.apply(categoryUnit.getDisplayName()),
        CATEGORY_C_HEADING_WITH_UNIT.apply(categoryUnit.getDisplayName()),
        CATEGORY_TOTAL_HEADING_WITH_UNIT.apply(categoryUnit.getDisplayName()),
        COMMENTS_HEADING
    ));
  }

  private SummaryTableRow getSummaryTableRowForShortTermMonth(FlareVentRow consentMonth, Integer consentDays) {
    return new SummaryTableRow(List.of(
        DateUtils.formatShort(consentMonth.getMonth(), consentMonth.getYear()),
        String.valueOf(consentDays),
        bigDecimalToFormattedString(consentMonth.getCategoryA()),
        bigDecimalToFormattedString(consentMonth.getCategoryB()),
        bigDecimalToFormattedString(consentMonth.getCategoryC()),
        bigDecimalToFormattedString(BigDecimalUtil.sum(
            consentMonth.getCategoryA(),
            consentMonth.getCategoryB(),
            consentMonth.getCategoryC()
        )),
        consentMonth.getComments()
    ));
  }

  private int getFlareShortTermConsentDays(FlareShortTermMonth consentMonth) {
    return DateUtils.daysBetweenInclusive(consentMonth.getStartDate(), consentMonth.getEndDate());
  }

  private int getVentShortTermConsentDays(VentShortTermMonth consentMonth) {
    return DateUtils.daysBetweenInclusive(consentMonth.getStartDate(), consentMonth.getEndDate());
  }

  private SummaryTableRow getSummaryTableRowOfShortTermTotals(BigDecimal categoryATotal,
                                                              BigDecimal categoryBTotal,
                                                              BigDecimal categoryCTotal,
                                                              BigDecimal categoryTotal,
                                                              Integer totalDays) {
    return new SummaryTableRow(Stream.of(
        TOTAL_PROMPT,
        String.valueOf(totalDays),
        bigDecimalToFormattedString(categoryATotal),
        bigDecimalToFormattedString(categoryBTotal),
        bigDecimalToFormattedString(categoryCTotal),
        String.valueOf(categoryTotal),
        null).toList()
    );
  }

  private SummaryTableRow getSummaryTableRowWithShortTermAverageData(FlareVentUnit averageUnit,
                                                                     BigDecimal categoryTotal,
                                                                     int totalDays) {
    return new SummaryTableRow(Stream.of(
        AVERAGE_PROMPT_WITH_UNIT.apply(averageUnit.getDisplayName()),
        null,
        null,
        null,
        null,
        bigDecimalToFormattedString(BigDecimalUtil.divideRound(categoryTotal, totalDays)),
        null).toList()
    );
  }

  @ParameterizedTest
  @MethodSource("getAnnualConsentMonths")
  void getAnnualConsentSummaryCard(List<? extends FlareVentRow> consentMonths) {
    var categoryUnit = FlareVentUnit.TONNES_PER_MONTH;
    var averageUnit = FlareVentUnit.TONNES_PER_DAY;

    var totalDays = consentMonths.stream()
        .mapToInt(consentMonth -> YearMonth.of(consentMonth.getYear(), consentMonth.getMonth()).lengthOfMonth())
        .sum();
    var categoryATotal = getCategoryATotal(consentMonths);
    var categoryBTotal = getCategoryBTotal(consentMonths);
    var categoryCTotal = getCategoryCTotal(consentMonths);
    var categoryTotal = BigDecimalUtil.sum(categoryATotal, categoryBTotal, categoryCTotal);

    assertThat(emissionConsentSummaryService.getAnnualConsentSummaryCard(consentMonths, categoryUnit, averageUnit))
        .isEqualTo(
            new SummaryCard(
                null,
                SummaryCardType.TABLE_SUMMARY,
                new SummaryTableView(
                    List.of(
                        getSummaryTableRowAnnualHeading(categoryUnit),
                        getSummaryTableRowForAnnualMonth(consentMonths.get(0)),
                        getSummaryTableRowForAnnualMonth(consentMonths.get(1)),
                        getSummaryTableRowForAnnualMonth(consentMonths.get(2)),
                        getSummaryTableRowForAnnualMonth(consentMonths.get(3)),
                        getSummaryTableRowForAnnualMonth(consentMonths.get(4)),
                        getSummaryTableRowForAnnualMonth(consentMonths.get(5)),
                        getSummaryTableRowForAnnualMonth(consentMonths.get(6)),
                        getSummaryTableRowForAnnualMonth(consentMonths.get(7)),
                        getSummaryTableRowForAnnualMonth(consentMonths.get(8)),
                        getSummaryTableRowForAnnualMonth(consentMonths.get(9)),
                        getSummaryTableRowForAnnualMonth(consentMonths.get(10)),
                        getSummaryTableRowForAnnualMonth(consentMonths.get(11)),
                        getSummaryTableRowOfAnnualTotals(categoryATotal, categoryBTotal, categoryCTotal, categoryTotal),
                        getSummaryTableRowWithAnnualAverageData(averageUnit, categoryTotal, totalDays)
                    )
                )
            )
        );
  }

  private static Stream<Arguments> getAnnualConsentMonths() {
    return Stream.of(
        Arguments.of(FlareAnnualTestUtil.getFlareAnnualMonthsForYear(
            ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.FLARE), 2023)),
        Arguments.of(VentAnnualTestUtil.getVentAnnualMonthsForYear(
            ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.VENT), 2023))
    );
  }

  private SummaryTableRow getSummaryTableRowAnnualHeading(FlareVentUnit categoryUnit) {
    return new SummaryTableRow(List.of(
        MONTH_HEADING,
        CATEGORY_A_HEADING_WITH_UNIT.apply(categoryUnit.getDisplayName()),
        CATEGORY_B_HEADING_WITH_UNIT.apply(categoryUnit.getDisplayName()),
        CATEGORY_C_HEADING_WITH_UNIT.apply(categoryUnit.getDisplayName()),
        CATEGORY_TOTAL_HEADING_WITH_UNIT.apply(categoryUnit.getDisplayName()),
        COMMENTS_HEADING
    ));
  }

  private SummaryTableRow getSummaryTableRowForAnnualMonth(FlareVentRow consentMonth) {
    return new SummaryTableRow(List.of(
        DateUtils.formatShort(consentMonth.getMonth(), consentMonth.getYear()),
        bigDecimalToFormattedString(consentMonth.getCategoryA()),
        bigDecimalToFormattedString(consentMonth.getCategoryB()),
        bigDecimalToFormattedString(consentMonth.getCategoryC()),
        bigDecimalToFormattedString(BigDecimalUtil.sum(
            consentMonth.getCategoryA(),
            consentMonth.getCategoryB(),
            consentMonth.getCategoryC()
        )),
        consentMonth.getComments()
    ));
  }

  private SummaryTableRow getSummaryTableRowOfAnnualTotals(BigDecimal categoryATotal,
                                                           BigDecimal categoryBTotal,
                                                           BigDecimal categoryCTotal,
                                                           BigDecimal categoryTotal) {
    return new SummaryTableRow(Stream.of(
        TOTAL_PROMPT,
        bigDecimalToFormattedString(categoryATotal),
        bigDecimalToFormattedString(categoryBTotal),
        bigDecimalToFormattedString(categoryCTotal),
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

  @ParameterizedTest
  @MethodSource("getLongTermConsentMonths")
  void getLongTermConsentSummaryCard(List<? extends EmissionLongTermYear> consentYears) {
    var gasUnit = FlareVentUnit.TONNES_PER_DAY;

    assertThat(emissionConsentSummaryService.getLongTermConsentSummaryCard(consentYears, gasUnit))
        .isEqualTo(
            new SummaryCard(
                null,
                SummaryCardType.TABLE_SUMMARY,
                new SummaryTableView(
                    List.of(
                        getSummaryTableRowLongTermHeading(gasUnit),
                        getSummaryTableRowForLongTermYear(consentYears.get(0)),
                        getSummaryTableRowForLongTermYear(consentYears.get(1)),
                        getSummaryTableRowForLongTermYear(consentYears.get(2)),
                        getSummaryTableRowForLongTermYear(consentYears.get(3)),
                        getSummaryTableRowForLongTermYear(consentYears.get(4))
                    )
                )
            )
        );
  }

  private static Stream<Arguments> getLongTermConsentMonths() {
    return Stream.of(
        Arguments.of(FlareLongTermTestUtil.getFlareLongTermYears(
            ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.FLARE), 2021, 2025)),
        Arguments.of(VentLongTermTestUtil.getVentLongTermYears(
            ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.VENT), 2021, 2025))
    );
  }

  private SummaryTableRow getSummaryTableRowLongTermHeading(FlareVentUnit gasUnit) {
    return new SummaryTableRow(List.of(
        YEAR_HEADING,
        GAS_HEADING_WITH_UNIT.apply(gasUnit.getDisplayName())
    ));
  }

  private SummaryTableRow getSummaryTableRowForLongTermYear(EmissionLongTermYear consentYear) {
    return new SummaryTableRow(List.of(
        String.valueOf(consentYear.getYear()),
        bigDecimalToFormattedString(consentYear.getGas())
    ));
  }
}
