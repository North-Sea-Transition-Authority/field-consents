package uk.co.nstauthority.fieldconsents.charts;

import static uk.co.nstauthority.fieldconsents.application.ApplicationType.PRODUCTION;
import static uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType.LONG_TERM;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.application.unit.ApplicationUnitService;
import uk.co.nstauthority.fieldconsents.charts.ProductionChartData.DataPoint;
import uk.co.nstauthority.fieldconsents.charts.ProductionChartData.Series;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.production.ProductionRow;
import uk.co.nstauthority.fieldconsents.production.ProductionUnit;
import uk.co.nstauthority.fieldconsents.production.annual.AnnualProductionMonth;
import uk.co.nstauthority.fieldconsents.production.annual.AnnualProductionService;
import uk.co.nstauthority.fieldconsents.production.longterm.LongTermProductionService;
import uk.co.nstauthority.fieldconsents.production.longterm.LongTermProductionYear;
import uk.co.nstauthority.fieldconsents.production.shortterm.ShortTermProductionMonth;
import uk.co.nstauthority.fieldconsents.production.shortterm.ShortTermProductionService;

@Service
public class ProductionChartDataService {

  private final ConsentLengthService consentLengthService;
  private final ShortTermProductionService shortTermProductionService;
  private final AnnualProductionService annualProductionService;
  private final ApplicationUnitService applicationUnitService;
  private final LongTermProductionService longTermProductionService;

  ProductionChartDataService(
      ConsentLengthService consentLengthService,
      ShortTermProductionService shortTermProductionService,
      AnnualProductionService annualProductionService,
      ApplicationUnitService applicationUnitService,
      LongTermProductionService longTermProductionService
  ) {
    this.consentLengthService = consentLengthService;
    this.shortTermProductionService = shortTermProductionService;
    this.annualProductionService = annualProductionService;
    this.applicationUnitService = applicationUnitService;
    this.longTermProductionService = longTermProductionService;
  }

  public Optional<ProductionChartData> getProductionChartData(
      ApplicationVersion applicationVersion,
      ProductionType productionType
  ) {
    var applicationType = applicationVersion.getApplication().getType();
    if (applicationType != PRODUCTION) {
      throw new UnsupportedOperationException("Cannot create production chart for non-Production application %s"
          .formatted(applicationType));
    }

    var consentLength = consentLengthService.getConsentLengthDetails(applicationVersion).getConsentLength();
    if (!consentDataExists(applicationVersion, consentLength)) {
      return Optional.empty();
    }

    return switch (consentLength) {
      case SHORT_TERM, ANNUAL -> Optional.of(
          getProductionChartDataForShortTermOrAnnual(
              applicationVersion,
              productionType,
              consentLength
          )
      );
      case LONG_TERM -> Optional.of(
          getProductionChartDataForLongTerm(
              applicationVersion,
              productionType,
              consentLength
          )
      );
    };
  }

  private boolean consentDataExists(ApplicationVersion applicationVersion, ConsentLengthType consentLength) {
    return switch (consentLength) {
      case SHORT_TERM -> shortTermProductionService.shortTermProductionMonthsExist(applicationVersion);
      case ANNUAL -> annualProductionService.annualProductionMonthsExist(applicationVersion);
      case LONG_TERM -> longTermProductionService.longTermProductionYearsExist(applicationVersion);
    };
  }

  private ProductionChartData getProductionChartDataForShortTermOrAnnual(
      ApplicationVersion applicationVersion,
      ProductionType productionType,
      ConsentLengthType consentLength
  ) {
    var productionMonths = getMonthsFromProductionRows(applicationVersion, consentLength);
    var productionUnit = getProductionUnit(applicationVersion, productionType);

    return getShortTermOrAnnualProductionChartData(
        productionMonths,
        productionType,
        productionUnit
    );
  }

  private List<? extends ProductionRow> getMonthsFromProductionRows(ApplicationVersion applicationVersion,
                                                                    ConsentLengthType consentLength) {
    return switch (consentLength) {
      case SHORT_TERM -> shortTermProductionService.getShortTermProductionMonths(applicationVersion);
      case ANNUAL -> annualProductionService.getAnnualProductionMonths(applicationVersion);
      default -> throw new UnsupportedOperationException("Unsupported consent length type %s".formatted(consentLength));
    };
  }

  private ProductionChartData getShortTermOrAnnualProductionChartData(
      List<? extends ProductionRow> productionRows,
      ProductionType productionType,
      ProductionUnit productionUnit
  ) {
    var yearMonths = getYearMonthFromShortTermOrAnnualProductionRow(productionRows);
    var months = yearMonths.stream()
        .map(month -> month.getMonth().getDisplayName(TextStyle.SHORT, Locale.UK))
        .toList();
    var listOfSeries = List.of(createDataSeries(productionRows, productionType));
    var chartHeading = getChartHeadingInMonths(yearMonths, productionType);

    //CHECKSTYLE:OFF
    var yAxisTitle = "Volume (%s)".formatted(productionUnit.getDisplayName());
    //CHECKSTYLE:ON

    return new ProductionChartData(chartHeading, months, "Month", yAxisTitle, listOfSeries);
  }

  private static List<YearMonth> getYearMonthFromShortTermOrAnnualProductionRow(
      List<? extends ProductionRow> productionRows
  ) {
    return productionRows.stream()
        .map(ProductionChartDataService::getYearMonthFromShortTermOrAnnualProductionRow)
        .flatMap(Optional::stream)
        .toList();
  }

  private static Optional<YearMonth> getYearMonthFromShortTermOrAnnualProductionRow(ProductionRow productionRow) {
    return switch (productionRow) {
      case ShortTermProductionMonth shortTermProductionMonth -> Optional.of(
          YearMonth.of(
              shortTermProductionMonth.getYear(),
              shortTermProductionMonth.getMonth()
          )
      );
      case AnnualProductionMonth annualProductionMonth -> Optional.of(
          YearMonth.of(
              annualProductionMonth.getYear(),
              annualProductionMonth.getMonth()
          )
      );
      default -> Optional.empty();
    };
  }

  private ProductionChartData getProductionChartDataForLongTerm(
      ApplicationVersion applicationVersion,
      ProductionType productionType,
      ConsentLengthType consentLength
  ) {
    var productionMonths = getYearsAsStringsFromProductionRows(applicationVersion, consentLength);
    var productionUnit = getProductionUnit(applicationVersion, productionType);

    return getLongTermProductionChartData(
        productionMonths,
        productionType,
        productionUnit
    );
  }

  private List<? extends ProductionRow> getYearsAsStringsFromProductionRows(ApplicationVersion applicationVersion,
                                                                            ConsentLengthType consentLength) {
    if (LONG_TERM.equals(consentLength)) {
      return longTermProductionService.getLongTermProductionYears(applicationVersion);
    } else {
      throw new UnsupportedOperationException("Unsupported consent length type %s".formatted(consentLength));
    }
  }

  private static String getChartHeadingInMonths(
      List<YearMonth> yearMonths,
      ProductionType productionType
  ) {
    var firstYear = yearMonths.getFirst().getYear();
    var lastYear = yearMonths.getLast().getYear();

    var startingMonth = (firstYear == lastYear)
        ? DateUtils.formatShort(yearMonths.getFirst().getMonth())
        : "%s %d".formatted(
        DateUtils.formatShort(yearMonths.getFirst().getMonth()),
        firstYear);
    var finishingMonth = "%s %d".formatted(
        DateUtils.formatShort(yearMonths.getLast().getMonth()),
        lastYear);

    return "%s (%s - %s)".formatted(
        productionType.getSeriesName(),
        startingMonth,
        finishingMonth
    );
  }

  private ProductionChartData getLongTermProductionChartData(
      List<? extends ProductionRow> productionRows,
      ProductionType productionType,
      ProductionUnit productionUnit
  ) {
    var years = getYearsAsStringsFromProductionRows(productionRows);
    var listOfSeries = List.of(createDataSeries(productionRows, productionType));
    var chartHeading = getChartHeadingInYears(years, productionType);

    //CHECKSTYLE:OFF
    var yAxisTitle = "Volume (%s)".formatted(productionUnit.getDisplayName());
    //CHECKSTYLE:ON

    return new ProductionChartData(chartHeading, years, "Year", yAxisTitle, listOfSeries);
  }

  private static List<String> getYearsAsStringsFromProductionRows(
      List<? extends ProductionRow> productionRows
  ) {
    return productionRows.stream()
        .map(ProductionChartDataService::getYearFromProductionRow)
        .flatMap(Optional::stream)
        .map(Object::toString)
        .toList();
  }

  private static Optional<Integer> getYearFromProductionRow(ProductionRow productionRow) {
    return switch (productionRow) {
      case ShortTermProductionMonth shortTermProductionMonth -> Optional.of(shortTermProductionMonth.getYear());
      case AnnualProductionMonth annualProductionMonth -> Optional.of(annualProductionMonth.getYear());
      case LongTermProductionYear longTermProductionYear -> Optional.of(longTermProductionYear.getYear());
      default -> Optional.empty();
    };
  }

  private static String getChartHeadingInYears(
      List<String> yearsAsStrings,
      ProductionType productionType
  ) {
    return "%s (%s - %s)".formatted(
        productionType.getSeriesName(),
        yearsAsStrings.getFirst(),
        yearsAsStrings.getLast()
    );
  }

  private ProductionUnit getProductionUnit(ApplicationVersion applicationVersion, ProductionType productionType) {
    return switch (productionType) {
      case OIL -> applicationUnitService.getProductionOilUnit(applicationVersion);
      case GAS -> applicationUnitService.getProductionGasUnit(applicationVersion);
    };
  }

  private static Series createDataSeries(List<? extends ProductionRow> productionRows, ProductionType productionType) {
    var minimumProductionData = productionRows.stream()
        .map(productionRow -> productionType.getMinimumFunction().apply(productionRow))
        .map(BigDecimal::floatValue)
        .toList();
    var maximumProductionData = productionRows.stream()
        .map(productionRow -> productionType.getMaximumFunction().apply(productionRow))
        .map(BigDecimal::floatValue)
        .toList();

    List<DataPoint> dataPoints = new ArrayList<>();
    for (int i = 0; i < productionRows.size(); i++) {
      var dataPoint = new DataPoint(
          i,
          minimumProductionData.get(i),
          maximumProductionData.get(i),
          productionType.getHighchartsColour());
      dataPoints.add(dataPoint);
    }

    return new Series(
        productionType.getSeriesName(),
        productionType.getHighchartsColour(),
        "columnrange",
        dataPoints
    );
  }
}
