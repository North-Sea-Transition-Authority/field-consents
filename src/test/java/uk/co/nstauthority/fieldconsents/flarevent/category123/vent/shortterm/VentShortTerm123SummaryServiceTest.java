package uk.co.nstauthority.fieldconsents.flarevent.category123.vent.shortterm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthTestUtil.SHORT_TERM_END_DATE;
import static uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthTestUtil.SHORT_TERM_START_DATE;
import static uk.co.nstauthority.fieldconsents.flarevent.category123.vent.Vent123SummaryTestUtil.getCategory1Total;
import static uk.co.nstauthority.fieldconsents.flarevent.category123.vent.summary.Vent123SummaryUtil.CATEGORY_1_HEADING_WITH_UNIT;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryUtil.AVERAGE_PROMPT_WITH_UNIT;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryUtil.COMMENTS_HEADING;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryUtil.CONSENT_DAYS_HEADING;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryUtil.MONTH_HEADING;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryUtil.TOTAL_PROMPT;
import static uk.co.nstauthority.fieldconsents.formatting.DecimalFormatUtils.bigDecimalToFormattedString;

import java.math.BigDecimal;
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
class VentShortTerm123SummaryServiceTest {

  @Mock
  private VentShortTerm123MonthRepository ventShortTerm123MonthRepository;

  @Mock
  private ApplicationUnitService applicationUnitService;

  @InjectMocks
  private VentShortTerm123SummaryService ventShortTerm123SummaryService;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.FLARE);
  }

  @Test
  void getVentShortTerm123SummaryCard_noShortTermMonths() {
    when(ventShortTerm123MonthRepository.findAllByApplicationVersion(applicationVersion))
        .thenReturn(Collections.emptyList());

    assertThat(ventShortTerm123SummaryService.getVentShortTerm123SummaryCard(applicationVersion))
        .isEqualTo(SummaryCard.emptySummaryCard());
  }

  @Test
  void getVentShortTerm123SummaryCard_shortTermMonthsExist() {
    var consentMonths = VentShortTerm123TestUtil.getVentShortTerm123MonthsForPeriod(applicationVersion,
        SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);
    var categoryUnit = FlareVentUnit.TONNES_PER_MONTH;
    var averageUnit = FlareVentUnit.TONNES_PER_DAY;

    when(ventShortTerm123MonthRepository.findAllByApplicationVersion(applicationVersion))
        .thenReturn(consentMonths);
    when(applicationUnitService.getVentCategoryUnit(applicationVersion))
        .thenReturn(categoryUnit);
    when(applicationUnitService.getVentAverageUnit(applicationVersion))
        .thenReturn(averageUnit);

    var totalDays = consentMonths.stream()
        .mapToInt(this::getVentShortTermConsentDays)
        .sum();
    var category1Total = getCategory1Total(consentMonths);

    assertThat(ventShortTerm123SummaryService.getVentShortTerm123SummaryCard(applicationVersion))
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
                        getSummaryTableRowOfShortTermTotals(category1Total, totalDays),
                        getSummaryTableRowWithShortTermAverageData(averageUnit, category1Total, totalDays)
                    )
                )
            )
        );
  }

  private SummaryTableRow getSummaryTableRowShortTermHeading(FlareVentUnit categoryUnit) {
    return new SummaryTableRow(List.of(
        MONTH_HEADING,
        CONSENT_DAYS_HEADING,
        CATEGORY_1_HEADING_WITH_UNIT.apply(categoryUnit.getDisplayName()),
        COMMENTS_HEADING
    ));
  }

  private SummaryTableRow getSummaryTableRowForShortTermMonth(Vent123Row consentMonth, Integer consentDays) {
    return new SummaryTableRow(List.of(
        DateUtils.formatShort(consentMonth.getMonth(), consentMonth.getYear()),
        String.valueOf(consentDays),
        bigDecimalToFormattedString(consentMonth.getCategory1()),
        consentMonth.getComments()
    ));
  }

  private int getVentShortTermConsentDays(VentShortTerm123Month consentMonth) {
    return DateUtils.daysBetweenInclusive(consentMonth.getStartDate(), consentMonth.getEndDate());
  }

  private SummaryTableRow getSummaryTableRowOfShortTermTotals(BigDecimal category1Total,
                                                              Integer totalDays) {
    return new SummaryTableRow(Stream.of(
        TOTAL_PROMPT,
        String.valueOf(totalDays),
        bigDecimalToFormattedString(category1Total),
        null).toList()
    );
  }

  private SummaryTableRow getSummaryTableRowWithShortTermAverageData(FlareVentUnit averageUnit,
                                                                     BigDecimal categoryTotal,
                                                                     int totalDays) {
    return new SummaryTableRow(Stream.of(
        AVERAGE_PROMPT_WITH_UNIT.apply(averageUnit.getDisplayName()),
        null,
        bigDecimalToFormattedString(BigDecimalUtil.divideRound(categoryTotal, totalDays)),
        null).toList()
    );
  }
}
