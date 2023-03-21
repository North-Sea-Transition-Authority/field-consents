package uk.co.nstauthority.fieldconsents.flarevent.summary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryTestUtil.getCategoryATotal;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryTestUtil.getCategoryBTotal;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryTestUtil.getCategoryCTotal;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryUtil.AVERAGE_PROMPT_WITH_UNIT;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryUtil.CATEGORY_A_HEADING_WITH_UNIT;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryUtil.CATEGORY_B_HEADING_WITH_UNIT;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryUtil.CATEGORY_C_HEADING_WITH_UNIT;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryUtil.CATEGORY_TOTAL_HEADING_WITH_UNIT;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryUtil.COMMENTS_HEADING;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryUtil.MONTH_HEADING;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryUtil.SHUTDOWN_DAYS_HEADING;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryUtil.TOTAL_PROMPT;
import static uk.co.nstauthority.fieldconsents.formatting.DecimalFormatUtils.bigDecimalToFormattedString;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentRow;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentUnit;
import uk.co.nstauthority.fieldconsents.flarevent.flare.annual.FlareAnnualTestUtil;
import uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport.FlareReportMonth;
import uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport.FlareReportTestUtil;
import uk.co.nstauthority.fieldconsents.flarevent.vent.ventreport.VentReportMonth;
import uk.co.nstauthority.fieldconsents.flarevent.vent.ventreport.VentReportTestUtil;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.summary.SummaryCardType;
import uk.co.nstauthority.fieldconsents.summary.SummaryDataView;
import uk.co.nstauthority.fieldconsents.summary.SummaryKeyValue;
import uk.co.nstauthority.fieldconsents.summary.SummaryTableRow;
import uk.co.nstauthority.fieldconsents.summary.SummaryTableView;
import uk.co.nstauthority.fieldconsents.util.BigDecimalUtil;

@ExtendWith(MockitoExtension.class)
class EmissionReportSummaryServiceTest {

  @InjectMocks
  private EmissionReportSummaryService emissionReportSummaryService;

  @ParameterizedTest
  @EnumSource(value = ApplicationType.class, mode = EnumSource.Mode.EXCLUDE, names = {"PRODUCTION"})
  void getReportPeriodSummaryCard(ApplicationType applicationType) {
    var appTypeDisplayName = applicationType.getDisplayName();
    var reportPeriod = ApplicationType.FLARE.equals(applicationType)
        ? FlareReportTestUtil.getFullFlareReportPeriod()
        : VentReportTestUtil.getFullVentReportPeriod();

    assertThat(emissionReportSummaryService.getReportPeriodSummaryCard(reportPeriod, applicationType))
        .isEqualTo(
            new SummaryCard(
                "%s report period".formatted(appTypeDisplayName),
                SummaryCardType.SIMPLE_SUMMARY,
                new SummaryDataView(
                    List.of(
                        new SummaryKeyValue(
                            "Which year do you have %s report data up to?".formatted(appTypeDisplayName.toLowerCase()),
                            String.valueOf(reportPeriod.getReportEndYear())),
                        new SummaryKeyValue(
                            "Which is the latest full month of %s report data you have?".formatted(appTypeDisplayName.toLowerCase()),
                            DateUtils.formatFull(reportPeriod.getReportEndMonth()))
                    )
                )
            )
        );
  }

  @Test
  void getReportTableSummaryCard_flare() {
    var reportMonths = FlareReportTestUtil.getFlareReportMonthsForYear(
        ApplicationTestUtil.getApplicationVersionWithType(ApplicationType.FLARE), 2023);
    var categoryUnit = FlareVentUnit.TONNES_PER_MONTH;
    var averageUnit = FlareVentUnit.TONNES_PER_DAY;

    var shutDownDaysTotal = reportMonths.stream()
            .mapToInt(FlareReportMonth::getShutDownDays)
            .sum();
    var totalDays = reportMonths.stream()
            .mapToInt(reportMonth -> YearMonth.of(reportMonth.getYear(), reportMonth.getMonth()).lengthOfMonth())
            .sum();
    var categoryATotal = getCategoryATotal(reportMonths);
    var categoryBTotal = getCategoryBTotal(reportMonths);
    var categoryCTotal = getCategoryCTotal(reportMonths);
    var categoryTotal = BigDecimalUtil.sum(categoryATotal, categoryBTotal, categoryCTotal);

    assertThat(emissionReportSummaryService.getReportTableSummaryCard(reportMonths, categoryUnit, averageUnit))
        .isEqualTo(
            new SummaryCard(
                null,
                SummaryCardType.TABLE_SUMMARY,
                new SummaryTableView(
                    List.of(
                        getSummaryTableRowHeading(categoryUnit),
                        getSummaryTableRowForReportMonth(reportMonths.get(0), reportMonths.get(0).getShutDownDays()),
                        getSummaryTableRowForReportMonth(reportMonths.get(1), reportMonths.get(1).getShutDownDays()),
                        getSummaryTableRowForReportMonth(reportMonths.get(2), reportMonths.get(2).getShutDownDays()),
                        getSummaryTableRowForReportMonth(reportMonths.get(3), reportMonths.get(3).getShutDownDays()),
                        getSummaryTableRowForReportMonth(reportMonths.get(4), reportMonths.get(4).getShutDownDays()),
                        getSummaryTableRowForReportMonth(reportMonths.get(5), reportMonths.get(5).getShutDownDays()),
                        getSummaryTableRowForReportMonth(reportMonths.get(6), reportMonths.get(6).getShutDownDays()),
                        getSummaryTableRowForReportMonth(reportMonths.get(7), reportMonths.get(7).getShutDownDays()),
                        getSummaryTableRowForReportMonth(reportMonths.get(8), reportMonths.get(8).getShutDownDays()),
                        getSummaryTableRowForReportMonth(reportMonths.get(9), reportMonths.get(9).getShutDownDays()),
                        getSummaryTableRowForReportMonth(reportMonths.get(10), reportMonths.get(10).getShutDownDays()),
                        getSummaryTableRowForReportMonth(reportMonths.get(11), reportMonths.get(11).getShutDownDays()),
                        getSummaryTableRowOfTotals(categoryATotal, categoryBTotal, categoryCTotal, categoryTotal,
                            shutDownDaysTotal),
                        getSummaryTableRowWithAverageData(averageUnit, categoryTotal, totalDays)
                    )
                )
            )
        );
  }

  @Test
  void getReportTableSummaryCard_vent() {
    var reportMonths = VentReportTestUtil.getVentReportMonthsForYear(
        ApplicationTestUtil.getApplicationVersionWithType(ApplicationType.VENT), 2023);
    var categoryUnit = FlareVentUnit.TONNES_PER_MONTH;
    var averageUnit = FlareVentUnit.TONNES_PER_DAY;

    var shutDownDaysTotal = reportMonths.stream()
        .mapToInt(VentReportMonth::getShutDownDays)
        .sum();
    var totalDays = reportMonths.stream()
        .mapToInt(reportMonth -> YearMonth.of(reportMonth.getYear(), reportMonth.getMonth()).lengthOfMonth())
        .sum();
    var categoryATotal = getCategoryATotal(reportMonths);
    var categoryBTotal = getCategoryBTotal(reportMonths);
    var categoryCTotal = getCategoryCTotal(reportMonths);
    var categoryTotal = BigDecimalUtil.sum(categoryATotal, categoryBTotal, categoryCTotal);

    assertThat(emissionReportSummaryService.getReportTableSummaryCard(reportMonths, categoryUnit, averageUnit))
        .isEqualTo(
            new SummaryCard(
                null,
                SummaryCardType.TABLE_SUMMARY,
                new SummaryTableView(
                    List.of(
                        getSummaryTableRowHeading(categoryUnit),
                        getSummaryTableRowForReportMonth(reportMonths.get(0), reportMonths.get(0).getShutDownDays()),
                        getSummaryTableRowForReportMonth(reportMonths.get(1), reportMonths.get(1).getShutDownDays()),
                        getSummaryTableRowForReportMonth(reportMonths.get(2), reportMonths.get(2).getShutDownDays()),
                        getSummaryTableRowForReportMonth(reportMonths.get(3), reportMonths.get(3).getShutDownDays()),
                        getSummaryTableRowForReportMonth(reportMonths.get(4), reportMonths.get(4).getShutDownDays()),
                        getSummaryTableRowForReportMonth(reportMonths.get(5), reportMonths.get(5).getShutDownDays()),
                        getSummaryTableRowForReportMonth(reportMonths.get(6), reportMonths.get(6).getShutDownDays()),
                        getSummaryTableRowForReportMonth(reportMonths.get(7), reportMonths.get(7).getShutDownDays()),
                        getSummaryTableRowForReportMonth(reportMonths.get(8), reportMonths.get(8).getShutDownDays()),
                        getSummaryTableRowForReportMonth(reportMonths.get(9), reportMonths.get(9).getShutDownDays()),
                        getSummaryTableRowForReportMonth(reportMonths.get(10), reportMonths.get(10).getShutDownDays()),
                        getSummaryTableRowForReportMonth(reportMonths.get(11), reportMonths.get(11).getShutDownDays()),
                        getSummaryTableRowOfTotals(categoryATotal, categoryBTotal, categoryCTotal, categoryTotal,
                            shutDownDaysTotal),
                        getSummaryTableRowWithAverageData(averageUnit, categoryTotal, totalDays)
                    )
                )
            )
        );
  }

  @Test
  void getReportTableSummaryCard_unexpectedReportMonthClass() {
    var flareAnnualMonths = FlareAnnualTestUtil.getFlareAnnualMonthsForYear(
        ApplicationTestUtil.getApplicationVersionWithType(ApplicationType.VENT), 2023);
    var categoryUnit = FlareVentUnit.TONNES_PER_MONTH;
    var averageUnit = FlareVentUnit.TONNES_PER_DAY;

    assertThatThrownBy(() ->
        emissionReportSummaryService.getReportTableSummaryCard(flareAnnualMonths, categoryUnit, averageUnit))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Unexpected report month class: " + flareAnnualMonths.get(0).getClass().getName());
  }

  private SummaryTableRow getSummaryTableRowHeading(FlareVentUnit categoryUnit) {
    return new SummaryTableRow(List.of(
        MONTH_HEADING,
        CATEGORY_A_HEADING_WITH_UNIT.apply(categoryUnit.getDisplayName()),
        CATEGORY_B_HEADING_WITH_UNIT.apply(categoryUnit.getDisplayName()),
        CATEGORY_C_HEADING_WITH_UNIT.apply(categoryUnit.getDisplayName()),
        CATEGORY_TOTAL_HEADING_WITH_UNIT.apply(categoryUnit.getDisplayName()),
        SHUTDOWN_DAYS_HEADING,
        COMMENTS_HEADING
    ));
  }

  private SummaryTableRow getSummaryTableRowForReportMonth(FlareVentRow reportMonth, Integer shutDownDays) {
    return new SummaryTableRow(List.of(
        DateUtils.formatShort(reportMonth.getMonth(), reportMonth.getYear()),
        bigDecimalToFormattedString(reportMonth.getCategoryA()),
        bigDecimalToFormattedString(reportMonth.getCategoryB()),
        bigDecimalToFormattedString(reportMonth.getCategoryC()),
        bigDecimalToFormattedString(BigDecimalUtil.sum(
            reportMonth.getCategoryA(),
            reportMonth.getCategoryB(),
            reportMonth.getCategoryC()
        )),
        String.valueOf(shutDownDays),
        reportMonth.getComments()
    ));
  }

  private SummaryTableRow getSummaryTableRowOfTotals(BigDecimal categoryATotal,
                                                     BigDecimal categoryBTotal,
                                                     BigDecimal categoryCTotal,
                                                     BigDecimal categoryTotal,
                                                     Integer shutDownDaysTotal) {
    return new SummaryTableRow(Stream.of(
        TOTAL_PROMPT,
        bigDecimalToFormattedString(categoryATotal),
        bigDecimalToFormattedString(categoryBTotal),
        bigDecimalToFormattedString(categoryCTotal),
        String.valueOf(categoryTotal),
        String.valueOf(shutDownDaysTotal),
        null).toList()
    );
  }

  private SummaryTableRow getSummaryTableRowWithAverageData(FlareVentUnit averageUnit,
                                                            BigDecimal categoryTotal,
                                                            int totalDays) {
    return new SummaryTableRow(Stream.of(
        AVERAGE_PROMPT_WITH_UNIT.apply(averageUnit.getDisplayName()),
        null,
        null,
        null,
        bigDecimalToFormattedString(BigDecimalUtil.divideRound(categoryTotal, totalDays)),
        null,
        null).toList()
    );
  }
}