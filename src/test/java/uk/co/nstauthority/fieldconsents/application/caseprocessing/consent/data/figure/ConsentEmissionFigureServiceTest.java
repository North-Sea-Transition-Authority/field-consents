package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthTestUtil.SHORT_TERM_END_DATE;
import static uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthTestUtil.SHORT_TERM_START_DATE;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryTestUtil.getCategoryATotal;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryTestUtil.getCategoryBTotal;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionSummaryTestUtil.getCategoryCTotal;

import java.time.YearMonth;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.flarevent.flare.annual.FlareAnnualService;
import uk.co.nstauthority.fieldconsents.flarevent.flare.annual.FlareAnnualTestUtil;
import uk.co.nstauthority.fieldconsents.flarevent.flare.shortterm.FlareShortTermService;
import uk.co.nstauthority.fieldconsents.flarevent.flare.shortterm.FlareShortTermTestUtil;
import uk.co.nstauthority.fieldconsents.flarevent.vent.annual.VentAnnualService;
import uk.co.nstauthority.fieldconsents.flarevent.vent.annual.VentAnnualTestUtil;
import uk.co.nstauthority.fieldconsents.flarevent.vent.shortterm.VentShortTermService;
import uk.co.nstauthority.fieldconsents.flarevent.vent.shortterm.VentShortTermTestUtil;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.util.BigDecimalUtil;

@ExtendWith(MockitoExtension.class)
class ConsentEmissionFigureServiceTest {

  @Mock
  private FlareShortTermService flareShortTermService;

  @Mock
  private VentShortTermService ventShortTermService;

  @Mock
  private FlareAnnualService flareAnnualService;

  @Mock
  private VentAnnualService ventAnnualService;

  @InjectMocks
  private ConsentEmissionFigureService consentEmissionFigureService;

  @Test
  void getShortTermEmissionDailyAverage_applicationTypeIsFlare() {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.FLARE);

    var consentMonths = FlareShortTermTestUtil.getFlareShortTermMonthsForPeriod(
        applicationVersion,
        SHORT_TERM_START_DATE,
        SHORT_TERM_END_DATE
    );

    when(flareShortTermService.getFlareShortTermMonths(applicationVersion)).thenReturn(consentMonths);

    var totalDays = consentMonths.stream()
        .mapToInt(consentMonth -> DateUtils.daysBetweenInclusive(consentMonth.getStartDate(), consentMonth.getEndDate()))
        .sum();
    var categoryATotal = getCategoryATotal(consentMonths);
    var categoryBTotal = getCategoryBTotal(consentMonths);
    var categoryCTotal = getCategoryCTotal(consentMonths);
    var categoryTotal = BigDecimalUtil.sum(categoryATotal, categoryBTotal, categoryCTotal);

    assertThat(consentEmissionFigureService.getShortTermEmissionDailyAverage(applicationVersion))
        .isEqualTo(BigDecimalUtil.divideRound(categoryTotal, totalDays));
  }

  @Test
  void getShortTermEmissionDailyAverage_applicationTypeIsVent() {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.VENT);

    var consentMonths = VentShortTermTestUtil.getVentShortTermMonthsForPeriod(
        applicationVersion,
        SHORT_TERM_START_DATE,
        SHORT_TERM_END_DATE
    );

    when(ventShortTermService.getVentShortTermMonths(applicationVersion)).thenReturn(consentMonths);

    var totalDays = consentMonths.stream()
        .mapToInt(consentMonth -> DateUtils.daysBetweenInclusive(consentMonth.getStartDate(), consentMonth.getEndDate()))
        .sum();
    var categoryATotal = getCategoryATotal(consentMonths);
    var categoryBTotal = getCategoryBTotal(consentMonths);
    var categoryCTotal = getCategoryCTotal(consentMonths);
    var categoryTotal = BigDecimalUtil.sum(categoryATotal, categoryBTotal, categoryCTotal);

    assertThat(consentEmissionFigureService.getShortTermEmissionDailyAverage(applicationVersion))
        .isEqualTo(BigDecimalUtil.divideRound(categoryTotal, totalDays));
  }

  @Test
  void getShortTermEmissionDailyAverage_applicationTypeIsProduction() {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);

    assertThatThrownBy(() -> consentEmissionFigureService.getShortTermEmissionDailyAverage(applicationVersion))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Unexpected ApplicationType: PRODUCTION");
  }

  @Test
  void getAnnualEmissionDailyAverage_applicationTypeIsFlare() {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.FLARE);

    var consentMonths = FlareAnnualTestUtil.getFlareAnnualMonthsForYear(applicationVersion, 2024);

    when(flareAnnualService.getFlareAnnualMonths(applicationVersion)).thenReturn(consentMonths);

    var totalDays = consentMonths.stream()
        .mapToInt(consentMonth -> YearMonth.of(consentMonth.getYear(), consentMonth.getMonth()).lengthOfMonth())
        .sum();
    var categoryATotal = getCategoryATotal(consentMonths);
    var categoryBTotal = getCategoryBTotal(consentMonths);
    var categoryCTotal = getCategoryCTotal(consentMonths);
    var categoryTotal = BigDecimalUtil.sum(categoryATotal, categoryBTotal, categoryCTotal);

    assertThat(consentEmissionFigureService.getAnnualEmissionDailyAverage(applicationVersion))
        .isEqualTo(BigDecimalUtil.divideRound(categoryTotal, totalDays));
  }

  @Test
  void getAnnualEmissionDailyAverage_applicationTypeIsVent() {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.VENT);

    var consentMonths = VentAnnualTestUtil.getVentAnnualMonthsForYear(applicationVersion, 2024);

    when(ventAnnualService.getVentAnnualMonths(applicationVersion)).thenReturn(consentMonths);

    var totalDays = consentMonths.stream()
        .mapToInt(consentMonth -> YearMonth.of(consentMonth.getYear(), consentMonth.getMonth()).lengthOfMonth())
        .sum();
    var categoryATotal = getCategoryATotal(consentMonths);
    var categoryBTotal = getCategoryBTotal(consentMonths);
    var categoryCTotal = getCategoryCTotal(consentMonths);
    var categoryTotal = BigDecimalUtil.sum(categoryATotal, categoryBTotal, categoryCTotal);

    assertThat(consentEmissionFigureService.getAnnualEmissionDailyAverage(applicationVersion))
        .isEqualTo(BigDecimalUtil.divideRound(categoryTotal, totalDays));
  }

  @Test
  void getAnnualEmissionDailyAverage_applicationTypeIsProduction() {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);

    assertThatThrownBy(() -> consentEmissionFigureService.getAnnualEmissionDailyAverage(applicationVersion))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Unexpected ApplicationType: PRODUCTION");
  }
}
