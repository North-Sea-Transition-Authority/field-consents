package uk.co.nstauthority.fieldconsents.flarevent.category123.vent.annual;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.flarevent.category123.vent.Vent123SummaryTestUtil.getCategory1Total;
import static uk.co.nstauthority.fieldconsents.flarevent.category123.vent.summary.Vent123SummaryUtil.CATEGORY_1_HEADING_WITH_UNIT;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryUtil.AVERAGE_PROMPT_WITH_UNIT;
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
import uk.co.nstauthority.fieldconsents.flarevent.category123.vent.Vent123Row;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.summary.SummaryCardType;
import uk.co.nstauthority.fieldconsents.summary.SummaryTableRow;
import uk.co.nstauthority.fieldconsents.summary.SummaryTableView;
import uk.co.nstauthority.fieldconsents.util.BigDecimalUtil;

@ExtendWith(MockitoExtension.class)
class VentAnnual123SummaryServiceTest {

  @Mock
  private VentAnnual123MonthRepository ventAnnual123MonthRepository;

  @Mock
  private ApplicationUnitService applicationUnitService;

  @InjectMocks
  private VentAnnual123SummaryService ventAnnual123SummaryService;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.FLARE);
  }

  @Test
  void getVentAnnual123SummaryCard_noAnnualMonths() {
    when(ventAnnual123MonthRepository.findAllByApplicationVersion(applicationVersion))
        .thenReturn(Collections.emptyList());

    assertThat(ventAnnual123SummaryService.getVentAnnual123SummaryCard(applicationVersion))
        .isEqualTo(SummaryCard.emptySummaryCard());
  }

  @Test
  void getVentAnnual123SummaryCard_annualMonthsExist() {
    var ventAnnual123Months = VentAnnual123TestUtil.getVentAnnual123MonthsForYear(applicationVersion, 2022);
    var categoryUnit = FlareVentUnit.TONNES_PER_MONTH;
    var averageUnit = FlareVentUnit.TONNES_PER_DAY;

    when(ventAnnual123MonthRepository.findAllByApplicationVersion(applicationVersion))
        .thenReturn(ventAnnual123Months);
    when(applicationUnitService.getVentCategoryUnit(applicationVersion))
        .thenReturn(categoryUnit);
    when(applicationUnitService.getVentAverageUnit(applicationVersion))
        .thenReturn(averageUnit);

    var totalDays = ventAnnual123Months.stream()
        .mapToInt(consentMonth -> YearMonth.of(consentMonth.getYear(), consentMonth.getMonth()).lengthOfMonth())
        .sum();
    var category1Total = getCategory1Total(ventAnnual123Months);

    assertThat(ventAnnual123SummaryService.getVentAnnual123SummaryCard(applicationVersion))
        .isEqualTo(
            new SummaryCard(
                null,
                SummaryCardType.TABLE_SUMMARY,
                new SummaryTableView(
                    List.of(
                        getSummaryTableRowAnnualHeading(categoryUnit),
                        getSummaryTableRowForAnnualMonth(ventAnnual123Months.get(0)),
                        getSummaryTableRowForAnnualMonth(ventAnnual123Months.get(1)),
                        getSummaryTableRowForAnnualMonth(ventAnnual123Months.get(2)),
                        getSummaryTableRowForAnnualMonth(ventAnnual123Months.get(3)),
                        getSummaryTableRowForAnnualMonth(ventAnnual123Months.get(4)),
                        getSummaryTableRowForAnnualMonth(ventAnnual123Months.get(5)),
                        getSummaryTableRowForAnnualMonth(ventAnnual123Months.get(6)),
                        getSummaryTableRowForAnnualMonth(ventAnnual123Months.get(7)),
                        getSummaryTableRowForAnnualMonth(ventAnnual123Months.get(8)),
                        getSummaryTableRowForAnnualMonth(ventAnnual123Months.get(9)),
                        getSummaryTableRowForAnnualMonth(ventAnnual123Months.get(10)),
                        getSummaryTableRowForAnnualMonth(ventAnnual123Months.get(11)),
                        getSummaryTableRowOfAnnualTotals(category1Total),
                        getSummaryTableRowWithAnnualAverageData(averageUnit, category1Total, totalDays)
                    )
                )
            )
        );
  }

  private SummaryTableRow getSummaryTableRowAnnualHeading(FlareVentUnit categoryUnit) {
    return new SummaryTableRow(List.of(
        MONTH_HEADING,
        CATEGORY_1_HEADING_WITH_UNIT.apply(categoryUnit.getDisplayName()),
        COMMENTS_HEADING
    ));
  }

  private SummaryTableRow getSummaryTableRowForAnnualMonth(Vent123Row consentMonth) {
    return new SummaryTableRow(List.of(
        DateUtils.formatShort(consentMonth.getMonth(), consentMonth.getYear()),
        bigDecimalToFormattedString(consentMonth.getCategory1()),
        consentMonth.getComments()
    ));
  }

  private SummaryTableRow getSummaryTableRowOfAnnualTotals(BigDecimal category1Total) {
    return new SummaryTableRow(Stream.of(
        TOTAL_PROMPT,
        bigDecimalToFormattedString(category1Total),
        null).toList()
    );
  }

  private SummaryTableRow getSummaryTableRowWithAnnualAverageData(FlareVentUnit averageUnit,
                                                                  BigDecimal categoryTotal,
                                                                  int totalDays) {
    return new SummaryTableRow(Stream.of(
        AVERAGE_PROMPT_WITH_UNIT.apply(averageUnit.getDisplayName()),
        bigDecimalToFormattedString(BigDecimalUtil.divideRound(categoryTotal, totalDays)),
        null).toList()
    );
  }
}
