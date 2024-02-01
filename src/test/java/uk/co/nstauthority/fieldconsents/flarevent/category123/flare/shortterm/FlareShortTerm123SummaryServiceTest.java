package uk.co.nstauthority.fieldconsents.flarevent.category123.flare.shortterm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthTestUtil.SHORT_TERM_END_DATE;
import static uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthTestUtil.SHORT_TERM_START_DATE;
import static uk.co.nstauthority.fieldconsents.flarevent.category123.flare.Flare123SummaryTestUtil.getCategory1Total;
import static uk.co.nstauthority.fieldconsents.flarevent.category123.flare.Flare123SummaryTestUtil.getCategory2Total;
import static uk.co.nstauthority.fieldconsents.flarevent.category123.flare.Flare123SummaryTestUtil.getCategory3Total;
import static uk.co.nstauthority.fieldconsents.flarevent.category123.flare.summary.Flare123SummaryUtil.CATEGORY_1_HEADING_WITH_UNIT;
import static uk.co.nstauthority.fieldconsents.flarevent.category123.flare.summary.Flare123SummaryUtil.CATEGORY_2_HEADING_WITH_UNIT;
import static uk.co.nstauthority.fieldconsents.flarevent.category123.flare.summary.Flare123SummaryUtil.CATEGORY_3_HEADING_WITH_UNIT;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryUtil.AVERAGE_PROMPT_WITH_UNIT;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryUtil.CATEGORY_TOTAL_HEADING_WITH_UNIT;
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
import uk.co.nstauthority.fieldconsents.flarevent.category123.flare.Flare123Row;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.summary.SummaryCardType;
import uk.co.nstauthority.fieldconsents.summary.SummaryTableRow;
import uk.co.nstauthority.fieldconsents.summary.SummaryTableView;
import uk.co.nstauthority.fieldconsents.util.BigDecimalUtil;

@ExtendWith(MockitoExtension.class)
class FlareShortTerm123SummaryServiceTest {

  @Mock
  private FlareShortTerm123MonthRepository flareShortTerm123MonthRepository;

  @Mock
  private ApplicationUnitService applicationUnitService;

  @InjectMocks
  private FlareShortTerm123SummaryService flareShortTerm123SummaryService;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.FLARE);
  }

  @Test
  void getFlareShortTerm123SummaryCard_noShortTermMonths() {
    when(flareShortTerm123MonthRepository.findAllByApplicationVersion(applicationVersion))
        .thenReturn(Collections.emptyList());

    assertThat(flareShortTerm123SummaryService.getFlareShortTerm123SummaryCard(applicationVersion))
        .isEqualTo(SummaryCard.emptySummaryCard());
  }

  @Test
  void getFlareShortTerm123SummaryCard_shortTermMonthsExist() {
    var consentMonths = FlareShortTerm123TestUtil.getFlareShortTerm123MonthsForPeriod(applicationVersion,
        SHORT_TERM_START_DATE, SHORT_TERM_END_DATE);
    var categoryUnit = FlareVentUnit.TONNES_PER_MONTH;
    var averageUnit = FlareVentUnit.TONNES_PER_DAY;

    when(flareShortTerm123MonthRepository.findAllByApplicationVersion(applicationVersion))
        .thenReturn(consentMonths);
    when(applicationUnitService.getFlareCategoryUnit(applicationVersion))
        .thenReturn(categoryUnit);
    when(applicationUnitService.getFlareAverageUnit(applicationVersion))
        .thenReturn(averageUnit);

    var totalDays = consentMonths.stream()
        .mapToInt(this::getFlareShortTermConsentDays)
        .sum();
    var category1Total = getCategory1Total(consentMonths);
    var category2Total = getCategory2Total(consentMonths);
    var category3Total = getCategory3Total(consentMonths);
    var categoryTotal = BigDecimalUtil.sum(category1Total, category2Total, category3Total);

    assertThat(flareShortTerm123SummaryService.getFlareShortTerm123SummaryCard(applicationVersion))
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
                        getSummaryTableRowOfShortTermTotals(category1Total, category2Total, category3Total, categoryTotal,
                            totalDays),
                        getSummaryTableRowWithShortTermAverageData(averageUnit, categoryTotal, totalDays)
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
        CATEGORY_2_HEADING_WITH_UNIT.apply(categoryUnit.getDisplayName()),
        CATEGORY_3_HEADING_WITH_UNIT.apply(categoryUnit.getDisplayName()),
        CATEGORY_TOTAL_HEADING_WITH_UNIT.apply(categoryUnit.getDisplayName()),
        COMMENTS_HEADING
    ));
  }

  private SummaryTableRow getSummaryTableRowForShortTermMonth(Flare123Row consentMonth, Integer consentDays) {
    return new SummaryTableRow(List.of(
        DateUtils.formatShort(consentMonth.getMonth(), consentMonth.getYear()),
        String.valueOf(consentDays),
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

  private int getFlareShortTermConsentDays(FlareShortTerm123Month consentMonth) {
    return DateUtils.daysBetweenInclusive(consentMonth.getStartDate(), consentMonth.getEndDate());
  }

  private SummaryTableRow getSummaryTableRowOfShortTermTotals(BigDecimal category1Total,
                                                              BigDecimal category2Total,
                                                              BigDecimal category3Total,
                                                              BigDecimal categoryTotal,
                                                              Integer totalDays) {
    return new SummaryTableRow(Stream.of(
        TOTAL_PROMPT,
        String.valueOf(totalDays),
        bigDecimalToFormattedString(category1Total),
        bigDecimalToFormattedString(category2Total),
        bigDecimalToFormattedString(category3Total),
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
}
