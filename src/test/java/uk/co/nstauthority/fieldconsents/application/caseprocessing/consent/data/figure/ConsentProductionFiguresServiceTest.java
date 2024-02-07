package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.production.ProductionUnit.KSCM_PER_DAY;
import static uk.co.nstauthority.fieldconsents.production.ProductionUnit.SCM_PER_MONTH;

import java.time.YearMonth;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.unit.ApplicationUnitService;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.production.ProductionRow;
import uk.co.nstauthority.fieldconsents.production.ProductionTestUtils;
import uk.co.nstauthority.fieldconsents.production.annual.AnnualProductionService;
import uk.co.nstauthority.fieldconsents.production.longterm.LongTermProductionService;
import uk.co.nstauthority.fieldconsents.production.longterm.LongTermProductionYear;
import uk.co.nstauthority.fieldconsents.production.shortterm.ShortTermProductionService;
import uk.co.nstauthority.fieldconsents.util.BigDecimalUtil;

@ExtendWith(MockitoExtension.class)
class ConsentProductionFiguresServiceTest {

  @Mock
  private ApplicationUnitService applicationUnitService;

  @Mock
  private ShortTermProductionService shortTermProductionService;

  @Mock
  private AnnualProductionService annualProductionService;

  @Mock
  private LongTermProductionService longTermProductionService;

  @InjectMocks
  private ConsentProductionFiguresService consentProductionFiguresService;

  @Test
  void getShortTermConsentProductionFiguresDto() {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);

    var oilUnit = SCM_PER_MONTH;
    var averageUnit = KSCM_PER_DAY;
    var averageConversionFactor = 1000;
    var productionMonths = ProductionTestUtils.getShortTermProductionMonthsData(applicationVersion);

    when(shortTermProductionService.getShortTermProductionMonths(applicationVersion)).thenReturn(productionMonths);
    when(applicationUnitService.getProductionOilUnit(applicationVersion)).thenReturn(oilUnit);
    when(applicationUnitService.getProductionAverageUnit(applicationVersion)).thenReturn(averageUnit);
    when(applicationUnitService.getProductionAverageConversionFactor(oilUnit, averageUnit))
        .thenReturn(averageConversionFactor);

    var totalDays = productionMonths.stream()
        .mapToInt(productionMonth ->
            DateUtils.daysBetweenInclusive(productionMonth.getStartDate(), productionMonth.getEndDate())
        )
        .sum();
    var oilMinTotal = BigDecimalUtil.sum(productionMonths, ProductionRow::getOilMinValue);
    var oilMaxTotal = BigDecimalUtil.sum(productionMonths, ProductionRow::getOilMaxValue);
    var gasMinTotal = BigDecimalUtil.sum(productionMonths, ProductionRow::getGasMinValue);
    var gasMaxTotal = BigDecimalUtil.sum(productionMonths, ProductionRow::getGasMaxValue);

    assertThat(consentProductionFiguresService.getShortTermConsentProductionFiguresDto(applicationVersion)).isEqualTo(
        new ConsentProductionFiguresDto(
            BigDecimalUtil.divideRound(oilMinTotal, totalDays * averageConversionFactor),
            BigDecimalUtil.divideRound(oilMaxTotal, totalDays * averageConversionFactor),
            BigDecimalUtil.divideRound(gasMinTotal, totalDays),
            BigDecimalUtil.divideRound(gasMaxTotal, totalDays)
        )
    );
  }

  @Test
  void getAnnualConsentProductionFiguresDto() {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);

    var oilUnit = SCM_PER_MONTH;
    var averageUnit = KSCM_PER_DAY;
    var averageConversionFactor = 1000;
    var productionMonths = ProductionTestUtils.getAnnualProductionMonthsData(applicationVersion);

    when(annualProductionService.getAnnualProductionMonths(applicationVersion)).thenReturn(productionMonths);
    when(applicationUnitService.getProductionOilUnit(applicationVersion)).thenReturn(oilUnit);
    when(applicationUnitService.getProductionAverageUnit(applicationVersion)).thenReturn(averageUnit);
    when(applicationUnitService.getProductionAverageConversionFactor(oilUnit, averageUnit))
        .thenReturn(averageConversionFactor);

    var totalDays = productionMonths.stream()
        .mapToInt(productionMonth ->
            YearMonth.of(productionMonth.getYear(), productionMonth.getMonth()).lengthOfMonth()
        )
        .sum();
    var oilMinTotal = BigDecimalUtil.sum(productionMonths, ProductionRow::getOilMinValue);
    var oilMaxTotal = BigDecimalUtil.sum(productionMonths, ProductionRow::getOilMaxValue);
    var gasMinTotal = BigDecimalUtil.sum(productionMonths, ProductionRow::getGasMinValue);
    var gasMaxTotal = BigDecimalUtil.sum(productionMonths, ProductionRow::getGasMaxValue);

    assertThat(consentProductionFiguresService.getAnnualConsentProductionFiguresDto(applicationVersion)).isEqualTo(
        new ConsentProductionFiguresDto(
            BigDecimalUtil.divideRound(oilMinTotal, totalDays * averageConversionFactor),
            BigDecimalUtil.divideRound(oilMaxTotal, totalDays * averageConversionFactor),
            BigDecimalUtil.divideRound(gasMinTotal, totalDays),
            BigDecimalUtil.divideRound(gasMaxTotal, totalDays)
        )
    );
  }

  @Test
  void getLongTermConsentProductionFiguresDtos() {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);

    var productionYears = ProductionTestUtils.getLongTermProductionYearsData(applicationVersion);

    when(longTermProductionService.getLongTermProductionYears(applicationVersion)).thenReturn(productionYears);

    assertThat(consentProductionFiguresService.getLongTermConsentProductionFiguresDtos(applicationVersion)).containsOnly(
        entry(productionYears.get(0).getYear(), getProductionFiguresForLongTermYear(productionYears.get(0))),
        entry(productionYears.get(1).getYear(), getProductionFiguresForLongTermYear(productionYears.get(1))),
        entry(productionYears.get(2).getYear(), getProductionFiguresForLongTermYear(productionYears.get(2))),
        entry(productionYears.get(3).getYear(), getProductionFiguresForLongTermYear(productionYears.get(3))),
        entry(productionYears.get(4).getYear(), getProductionFiguresForLongTermYear(productionYears.get(4)))
    );
  }

  private ConsentProductionFiguresDto getProductionFiguresForLongTermYear(LongTermProductionYear productionYear) {
    return new ConsentProductionFiguresDto(
        productionYear.getOilMinValue(),
        productionYear.getOilMaxValue(),
        productionYear.getGasMinValue(),
        productionYear.getGasMaxValue()
    );
  }
}
