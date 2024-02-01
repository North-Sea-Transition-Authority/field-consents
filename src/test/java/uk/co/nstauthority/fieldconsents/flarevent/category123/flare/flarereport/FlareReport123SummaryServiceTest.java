package uk.co.nstauthority.fieldconsents.flarevent.category123.flare.flarereport;

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
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryUtil.SHUTDOWN_DAYS_HEADING;
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
class FlareReport123SummaryServiceTest {

  @Mock
  private FlareReport123Service flareReport123Service;

  @Mock
  private ApplicationUnitService applicationUnitService;

  @InjectMocks
  private FlareReport123SummaryService flareReport123SummaryService;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.FLARE);
  }

  @Test
  void getFlareReport123SummaryCard_noReportMonths() {
    when(flareReport123Service.getFlareReport123Months(applicationVersion))
        .thenReturn(Collections.emptyList());

    assertThat(flareReport123SummaryService.getFlareReport123SummaryCard(applicationVersion))
        .isEqualTo(SummaryCard.emptySummaryCard());
  }

  @Test
  void getFlareReport123SummaryCard_reportMonthsExist() {
    var reportMonths = FlareReport123TestUtil.getFlareReport123MonthsForYear(applicationVersion, 2023);
    var categoryUnit = FlareVentUnit.TONNES_PER_MONTH;
    var averageUnit = FlareVentUnit.TONNES_PER_DAY;

    when(flareReport123Service.getFlareReport123Months(applicationVersion))
        .thenReturn(reportMonths);
    when(applicationUnitService.getFlareCategoryUnit(applicationVersion))
        .thenReturn(categoryUnit);
    when(applicationUnitService.getFlareAverageUnit(applicationVersion))
        .thenReturn(averageUnit);

    var shutDownDaysTotal = reportMonths.stream()
        .mapToInt(FlareReport123Month::getShutDownDays)
        .sum();
    var totalDays = reportMonths.stream()
        .mapToInt(reportMonth -> YearMonth.of(reportMonth.getYear(), reportMonth.getMonth()).lengthOfMonth())
        .sum();

    var category1Total = getCategory1Total(reportMonths);
    var category2Total = getCategory2Total(reportMonths);
    var category3Total = getCategory3Total(reportMonths);
    var categoryTotal = BigDecimalUtil.sum(category1Total, category2Total, category3Total);

    assertThat(flareReport123SummaryService.getFlareReport123SummaryCard(applicationVersion))
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
                        getSummaryTableRowOfTotals(category1Total, category2Total, category3Total, categoryTotal,
                            shutDownDaysTotal),
                        getSummaryTableRowWithAverageData(averageUnit, categoryTotal, totalDays)
                    )
                )
            )
        );
  }

  private SummaryTableRow getSummaryTableRowHeading(FlareVentUnit categoryUnit) {
    return new SummaryTableRow(List.of(
        MONTH_HEADING,
        CATEGORY_1_HEADING_WITH_UNIT.apply(categoryUnit.getDisplayName()),
        CATEGORY_2_HEADING_WITH_UNIT.apply(categoryUnit.getDisplayName()),
        CATEGORY_3_HEADING_WITH_UNIT.apply(categoryUnit.getDisplayName()),
        CATEGORY_TOTAL_HEADING_WITH_UNIT.apply(categoryUnit.getDisplayName()),
        SHUTDOWN_DAYS_HEADING,
        COMMENTS_HEADING
    ));
  }

  private SummaryTableRow getSummaryTableRowForReportMonth(Flare123Row reportMonth, Integer shutDownDays) {
    return new SummaryTableRow(List.of(
        DateUtils.formatShort(reportMonth.getMonth(), reportMonth.getYear()),
        bigDecimalToFormattedString(reportMonth.getCategory1()),
        bigDecimalToFormattedString(reportMonth.getCategory2()),
        bigDecimalToFormattedString(reportMonth.getCategory3()),
        bigDecimalToFormattedString(BigDecimalUtil.sum(
            reportMonth.getCategory1(),
            reportMonth.getCategory2(),
            reportMonth.getCategory3()
        )),
        String.valueOf(shutDownDays),
        reportMonth.getComments()
    ));
  }

  private SummaryTableRow getSummaryTableRowOfTotals(BigDecimal category1Total,
                                                     BigDecimal category2Total,
                                                     BigDecimal category3Total,
                                                     BigDecimal categoryTotal,
                                                     Integer shutDownDaysTotal) {
    return new SummaryTableRow(Stream.of(
        TOTAL_PROMPT,
        bigDecimalToFormattedString(category1Total),
        bigDecimalToFormattedString(category2Total),
        bigDecimalToFormattedString(category3Total),
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
