package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure;

import java.time.YearMonth;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.unit.ApplicationUnitService;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.production.ProductionRow;
import uk.co.nstauthority.fieldconsents.production.annual.AnnualProductionService;
import uk.co.nstauthority.fieldconsents.production.longterm.LongTermProductionService;
import uk.co.nstauthority.fieldconsents.production.longterm.LongTermProductionYear;
import uk.co.nstauthority.fieldconsents.production.shortterm.ShortTermProductionService;
import uk.co.nstauthority.fieldconsents.util.BigDecimalUtil;

@Service
public class ConsentProductionFiguresService {

  private final ApplicationUnitService applicationUnitService;
  private final ShortTermProductionService shortTermProductionService;
  private final AnnualProductionService annualProductionService;
  private final LongTermProductionService longTermProductionService;

  ConsentProductionFiguresService(
      ApplicationUnitService applicationUnitService,
      ShortTermProductionService shortTermProductionService,
      AnnualProductionService annualProductionService,
      LongTermProductionService longTermProductionService
  ) {
    this.applicationUnitService = applicationUnitService;
    this.shortTermProductionService = shortTermProductionService;
    this.annualProductionService = annualProductionService;
    this.longTermProductionService = longTermProductionService;
  }

  public ConsentProductionFiguresDto getShortTermConsentProductionFiguresDto(ApplicationVersion applicationVersion) {
    var productionMonths = shortTermProductionService.getShortTermProductionMonths(applicationVersion);

    var oilUnit = applicationUnitService.getProductionOilUnit(applicationVersion);
    var averageUnit = applicationUnitService.getProductionAverageUnit(applicationVersion);
    var oilAverageConversionFactor = applicationUnitService.getProductionAverageConversionFactor(oilUnit, averageUnit);

    var oilMinTotal = BigDecimalUtil.sum(productionMonths, ProductionRow::getOilMinValue);
    var oilMaxTotal = BigDecimalUtil.sum(productionMonths, ProductionRow::getOilMaxValue);
    var gasMinTotal = BigDecimalUtil.sum(productionMonths, ProductionRow::getGasMinValue);
    var gasMaxTotal = BigDecimalUtil.sum(productionMonths, ProductionRow::getGasMaxValue);

    var totalDays = productionMonths
        .stream()
        .mapToInt(productionMonth -> DateUtils.daysBetweenInclusive(productionMonth.getStartDate(), productionMonth.getEndDate()))
        .sum();

    return new ConsentProductionFiguresDto(
        BigDecimalUtil.divideRound(oilMinTotal, totalDays * oilAverageConversionFactor),
        BigDecimalUtil.divideRound(oilMaxTotal, totalDays * oilAverageConversionFactor),
        BigDecimalUtil.divideRound(gasMinTotal, totalDays),
        BigDecimalUtil.divideRound(gasMaxTotal, totalDays)
    );
  }

  public ConsentProductionFiguresDto getAnnualConsentProductionFiguresDto(ApplicationVersion applicationVersion) {
    var productionMonths = annualProductionService.getAnnualProductionMonths(applicationVersion);

    var oilUnit = applicationUnitService.getProductionOilUnit(applicationVersion);
    var averageUnit = applicationUnitService.getProductionAverageUnit(applicationVersion);
    var oilAverageConversionFactor = applicationUnitService.getProductionAverageConversionFactor(oilUnit, averageUnit);

    var oilMinTotal = BigDecimalUtil.sum(productionMonths, ProductionRow::getOilMinValue);
    var oilMaxTotal = BigDecimalUtil.sum(productionMonths, ProductionRow::getOilMaxValue);
    var gasMinTotal = BigDecimalUtil.sum(productionMonths, ProductionRow::getGasMinValue);
    var gasMaxTotal = BigDecimalUtil.sum(productionMonths, ProductionRow::getGasMaxValue);

    var totalDays = productionMonths
        .stream()
        .mapToInt(productionMonth -> YearMonth.of(productionMonth.getYear(), productionMonth.getMonth()).lengthOfMonth())
        .sum();

    return new ConsentProductionFiguresDto(
        BigDecimalUtil.divideRound(oilMinTotal, totalDays * oilAverageConversionFactor),
        BigDecimalUtil.divideRound(oilMaxTotal, totalDays * oilAverageConversionFactor),
        BigDecimalUtil.divideRound(gasMinTotal, totalDays),
        BigDecimalUtil.divideRound(gasMaxTotal, totalDays)
    );
  }

  public Map<Integer, ConsentProductionFiguresDto> getLongTermConsentProductionFiguresDtos(
      ApplicationVersion applicationVersion
  ) {
    var productionYears = longTermProductionService.getLongTermProductionYears(applicationVersion);

    return productionYears
        .stream()
        .collect(
            Collectors.toMap(
                LongTermProductionYear::getYear,
                productionYear ->
                    new ConsentProductionFiguresDto(
                        productionYear.getOilMinValue(),
                        productionYear.getOilMaxValue(),
                        productionYear.getGasMinValue(),
                        productionYear.getGasMaxValue()
                    )
            )
        );
  }
}
