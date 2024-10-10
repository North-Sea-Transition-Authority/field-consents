package uk.co.nstauthority.fieldconsents.charts;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthDetails;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.application.unit.ApplicationUnitService;
import uk.co.nstauthority.fieldconsents.production.ProductionTestUtils;
import uk.co.nstauthority.fieldconsents.production.ProductionUnit;
import uk.co.nstauthority.fieldconsents.production.annual.AnnualProductionMonth;
import uk.co.nstauthority.fieldconsents.production.annual.AnnualProductionService;
import uk.co.nstauthority.fieldconsents.production.longterm.LongTermProductionService;
import uk.co.nstauthority.fieldconsents.production.longterm.LongTermProductionYear;
import uk.co.nstauthority.fieldconsents.production.shortterm.ShortTermProductionMonth;
import uk.co.nstauthority.fieldconsents.production.shortterm.ShortTermProductionService;

@ExtendWith(MockitoExtension.class)
class ProductionChartDataServiceTest {

  @Mock
  private ConsentLengthService consentLengthService;

  @Mock
  private ShortTermProductionService shortTermProductionService;

  @Mock
  private AnnualProductionService annualProductionService;

  @Mock
  private LongTermProductionService longTermProductionService;

  @Mock
  private ApplicationUnitService applicationUnitService;

  @InjectMocks
  private ProductionChartDataService productionChartDataService;

  private ApplicationVersion applicationVersion;
  private ConsentLengthDetails consentLengthDetails;

  @BeforeEach
  void setUp() {
    applicationVersion = new ApplicationVersion();
    applicationVersion.setApplication(new Application());

    consentLengthDetails = new ConsentLengthDetails();
  }

  @Test
  void getProductionChartData_oil_shortTerm() {
    var applicationType = ApplicationType.PRODUCTION;
    var productionType = ProductionType.OIL;
    var consentLengthType = ConsentLengthType.SHORT_TERM;

    var productionCategoryUnit = ProductionUnit.KSCM_PER_DAY;
    var consentMonths = ProductionTestUtils.getShortTermProductionMonthsForPeriod(
        applicationVersion,
        LocalDate.of(2024, 9, 1),
        LocalDate.of(2024, 10, 1)
    );

    applicationVersion.getApplication().setType(applicationType);
    consentLengthDetails.setConsentLength(consentLengthType);

    when(applicationUnitService.getProductionOilUnit(applicationVersion)).thenReturn(productionCategoryUnit);
    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);
    when(shortTermProductionService.shortTermProductionMonthsExist(applicationVersion)).thenReturn(true);
    when(shortTermProductionService.getShortTermProductionMonths(applicationVersion)).thenReturn(consentMonths);

    assertThat(productionChartDataService.getProductionChartData(applicationVersion, productionType))
        .contains(
            new ProductionChartData(
                "Production min/max for oil (Sept - Oct 2024)",
                consentMonths.stream()
                    .map(ShortTermProductionMonth::getMonth)
                    .map(month -> month.getDisplayName(TextStyle.SHORT, Locale.UK))
                    .toList(),
                "Month",
                "Volume (%s)".formatted(productionCategoryUnit.getDisplayName()),
                List.of(
                    new ProductionChartData.Series(
                        productionType.getSeriesName(),
                        productionType.getHighchartsColour(),
                        "columnrange",
                        List.of(
                            new ProductionChartData.DataPoint(
                                0,
                                1.0f,
                                10.0f,
                                productionType.getHighchartsColour()),
                            new ProductionChartData.DataPoint(
                                1,
                                2.0f,
                                20.0f,
                                productionType.getHighchartsColour())
                        )
                    )
                )
            )
        );
  }

  @Test
  void getProductionChartData_oil_interYearShortTerm() {
    var applicationType = ApplicationType.PRODUCTION;
    var productionType = ProductionType.OIL;
    var consentLengthType = ConsentLengthType.SHORT_TERM;

    var productionCategoryUnit = ProductionUnit.KSCM_PER_DAY;
    var consentMonths = ProductionTestUtils.getShortTermProductionMonthsForPeriod(
        applicationVersion,
        LocalDate.of(2024, 12, 1),
        LocalDate.of(2025, 1, 1)
    );

    applicationVersion.getApplication().setType(applicationType);
    consentLengthDetails.setConsentLength(consentLengthType);

    when(applicationUnitService.getProductionOilUnit(applicationVersion)).thenReturn(productionCategoryUnit);
    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);
    when(shortTermProductionService.shortTermProductionMonthsExist(applicationVersion)).thenReturn(true);
    when(shortTermProductionService.getShortTermProductionMonths(applicationVersion)).thenReturn(consentMonths);

    assertThat(productionChartDataService.getProductionChartData(applicationVersion, productionType))
        .contains(
            new ProductionChartData(
                "Production min/max for oil (Dec 2024 - Jan 2025)",
                consentMonths.stream()
                    .map(ShortTermProductionMonth::getMonth)
                    .map(month -> month.getDisplayName(TextStyle.SHORT, Locale.UK))
                    .toList(),
                "Month",
                "Volume (%s)".formatted(productionCategoryUnit.getDisplayName()),
                List.of(
                    new ProductionChartData.Series(
                        productionType.getSeriesName(),
                        productionType.getHighchartsColour(),
                        "columnrange",
                        List.of(
                            new ProductionChartData.DataPoint(
                                0,
                                1.0f,
                                10.0f,
                                productionType.getHighchartsColour()),
                            new ProductionChartData.DataPoint(
                                1,
                                2.0f,
                                20.0f,
                                productionType.getHighchartsColour())
                        )
                    )
                )
            )
        );
  }

  @Test
  void getProductionChartData_oil_annual() {
    var applicationType = ApplicationType.PRODUCTION;
    var productionType = ProductionType.OIL;
    var consentLengthType = ConsentLengthType.ANNUAL;

    var productionCategoryUnit = ProductionUnit.KSCM_PER_DAY;
    var consentMonths = ProductionTestUtils.getAnnualProductionMonthsForYear(
        applicationVersion,
        2024);

    applicationVersion.getApplication().setType(applicationType);
    consentLengthDetails.setConsentLength(consentLengthType);

    when(applicationUnitService.getProductionOilUnit(applicationVersion)).thenReturn(productionCategoryUnit);
    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);
    when(annualProductionService.annualProductionMonthsExist(applicationVersion)).thenReturn(true);
    when(annualProductionService.getAnnualProductionMonths(applicationVersion)).thenReturn(consentMonths);

    assertThat(productionChartDataService.getProductionChartData(applicationVersion, productionType))
        .contains(
            new ProductionChartData(
                "Production min/max for oil (Jan - Dec 2024)",
                consentMonths.stream()
                    .map(AnnualProductionMonth::getMonth)
                    .map(month -> month.getDisplayName(TextStyle.SHORT, Locale.UK))
                    .toList(),
                "Month",
                "Volume (%s)".formatted(productionCategoryUnit.getDisplayName()),
                List.of(
                    new ProductionChartData.Series(
                        productionType.getSeriesName(),
                        productionType.getHighchartsColour(),
                        "columnrange",
                        List.of(
                            new ProductionChartData.DataPoint(
                                0,
                                1.0f,
                                10.0f,
                                productionType.getHighchartsColour()),
                            new ProductionChartData.DataPoint(
                                1,
                                2.0f,
                                20.0f,
                                productionType.getHighchartsColour()),
                            new ProductionChartData.DataPoint(
                                2,
                                3.0f,
                                30.0f,
                                productionType.getHighchartsColour()),
                            new ProductionChartData.DataPoint(
                                3,
                                4.0f,
                                40.0f,
                                productionType.getHighchartsColour()),
                            new ProductionChartData.DataPoint(
                                4,
                                5.0f,
                                50.0f,
                                productionType.getHighchartsColour()),
                            new ProductionChartData.DataPoint(
                                5,
                                6.0f,
                                60.0f,
                                productionType.getHighchartsColour()),
                            new ProductionChartData.DataPoint(
                                6,
                                7.0f,
                                70.0f,
                                productionType.getHighchartsColour()),
                            new ProductionChartData.DataPoint(
                                7,
                                8.0f,
                                80.0f,
                                productionType.getHighchartsColour()),
                            new ProductionChartData.DataPoint(
                                8,
                                9.0f,
                                90.0f,
                                productionType.getHighchartsColour()),
                            new ProductionChartData.DataPoint(
                                9,
                                10.0f,
                                100.0f,
                                productionType.getHighchartsColour()),
                            new ProductionChartData.DataPoint(
                                10,
                                11.0f,
                                110.0f,
                                productionType.getHighchartsColour()),
                            new ProductionChartData.DataPoint(
                                11,
                                12.0f,
                                120.0f,
                                productionType.getHighchartsColour())
                        )
                    )
                )
            )
        );
  }


  @Test
  void getProductionChartData_oil_longterm() {
    var applicationType = ApplicationType.PRODUCTION;
    var productionType = ProductionType.OIL;
    var consentLengthType = ConsentLengthType.LONG_TERM;

    var productionCategoryUnit = ProductionUnit.KSCM_PER_DAY;
    var consentYears = ProductionTestUtils.getLongTermProductionYearsBetweenYears(
        applicationVersion,
        2025,
        2030);

    applicationVersion.getApplication().setType(applicationType);
    consentLengthDetails.setConsentLength(consentLengthType);

    when(applicationUnitService.getProductionOilUnit(applicationVersion)).thenReturn(productionCategoryUnit);
    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);
    when(longTermProductionService.longTermProductionYearsExist(applicationVersion)).thenReturn(true);
    when(longTermProductionService.getLongTermProductionYears(applicationVersion)).thenReturn(consentYears);

    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);

    assertThat(productionChartDataService.getProductionChartData(applicationVersion, productionType))
        .contains(
            new ProductionChartData(
                "%s (%s - %s)".formatted(
                    productionType.getSeriesName(),
                    consentYears.getFirst().getYear(),
                    consentYears.getLast().getYear()),
                consentYears.stream()
                    .map(LongTermProductionYear::getYear)
                    .map(Object::toString)
                    .toList(),
                "Year",
                "Volume (%s)".formatted(productionCategoryUnit.getDisplayName()),
                List.of(
                    new ProductionChartData.Series(
                        productionType.getSeriesName(),
                        productionType.getHighchartsColour(),
                        "columnrange",
                        List.of(
                            new ProductionChartData.DataPoint(
                                0,
                                1.0f,
                                100.0f,
                                productionType.getHighchartsColour()),
                            new ProductionChartData.DataPoint(
                                1,
                                2.0f,
                                200.0f,
                                productionType.getHighchartsColour()),
                            new ProductionChartData.DataPoint(
                                2,
                                3.0f,
                                300.0f,
                                productionType.getHighchartsColour()),
                            new ProductionChartData.DataPoint(
                                3,
                                4.0f,
                                400.0f,
                                productionType.getHighchartsColour()),
                            new ProductionChartData.DataPoint(
                                4,
                                5.0f,
                                500.0f,
                                productionType.getHighchartsColour()),
                            new ProductionChartData.DataPoint(
                                5,
                                6.0f,
                                600.0f,
                                productionType.getHighchartsColour())
                        )
                    )
                )
            )
        );
  }

  @Test
  void getProductionChartData_gas_shortTerm() {
    var applicationType = ApplicationType.PRODUCTION;
    var productionType = ProductionType.GAS;
    var consentLengthType = ConsentLengthType.SHORT_TERM;

    var productionCategoryUnit = ProductionUnit.KSCM_PER_DAY;
    var consentMonths = ProductionTestUtils.getShortTermProductionMonthsForPeriod(
        applicationVersion,
        LocalDate.of(2024, 9, 1),
        LocalDate.of(2024, 10, 1));

    applicationVersion.getApplication().setType(applicationType);
    consentLengthDetails.setConsentLength(consentLengthType);

    when(applicationUnitService.getProductionGasUnit(applicationVersion)).thenReturn(productionCategoryUnit);
    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);
    when(shortTermProductionService.shortTermProductionMonthsExist(applicationVersion)).thenReturn(true);
    when(shortTermProductionService.getShortTermProductionMonths(applicationVersion)).thenReturn(consentMonths);

    assertThat(productionChartDataService.getProductionChartData(applicationVersion, productionType))
        .contains(
            new ProductionChartData(
                "Production min/max for gas (Sept - Oct 2024)",
                consentMonths.stream()
                    .map(ShortTermProductionMonth::getMonth)
                    .map(month -> month.getDisplayName(TextStyle.SHORT, Locale.UK))
                    .toList(),
                "Month",
                "Volume (%s)".formatted(productionCategoryUnit.getDisplayName()),
                List.of(
                    new ProductionChartData.Series(
                        productionType.getSeriesName(),
                        productionType.getHighchartsColour(),
                        "columnrange",
                        List.of(
                            new ProductionChartData.DataPoint(
                                0,
                                100.0f,
                                1000.0f,
                                productionType.getHighchartsColour()),
                            new ProductionChartData.DataPoint(
                                1,
                                200.0f,
                                2000.0f,
                                productionType.getHighchartsColour())
                        )
                    )
                )
            )
        );
  }

  @Test
  void getProductionChartData_gas_interYearShortTerm() {
    var applicationType = ApplicationType.PRODUCTION;
    var productionType = ProductionType.GAS;
    var consentLengthType = ConsentLengthType.SHORT_TERM;

    var productionCategoryUnit = ProductionUnit.KSCM_PER_DAY;
    var consentMonths = ProductionTestUtils.getShortTermProductionMonthsForPeriod(
        applicationVersion,
        LocalDate.of(2024, 12, 1),
        LocalDate.of(2025, 1, 1));

    applicationVersion.getApplication().setType(applicationType);
    consentLengthDetails.setConsentLength(consentLengthType);

    when(applicationUnitService.getProductionGasUnit(applicationVersion)).thenReturn(productionCategoryUnit);
    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);
    when(shortTermProductionService.shortTermProductionMonthsExist(applicationVersion)).thenReturn(true);
    when(shortTermProductionService.getShortTermProductionMonths(applicationVersion)).thenReturn(consentMonths);

    assertThat(productionChartDataService.getProductionChartData(applicationVersion, productionType))
        .contains(
            new ProductionChartData(
                "Production min/max for gas (Dec 2024 - Jan 2025)",
                consentMonths.stream()
                    .map(ShortTermProductionMonth::getMonth)
                    .map(month -> month.getDisplayName(TextStyle.SHORT, Locale.UK))
                    .toList(),
                "Month",
                "Volume (%s)".formatted(productionCategoryUnit.getDisplayName()),
                List.of(
                    new ProductionChartData.Series(
                        productionType.getSeriesName(),
                        productionType.getHighchartsColour(),
                        "columnrange",
                        List.of(
                            new ProductionChartData.DataPoint(
                                0,
                                100.0f,
                                1000.0f,
                                productionType.getHighchartsColour()),
                            new ProductionChartData.DataPoint(
                                1,
                                200.0f,
                                2000.0f,
                                productionType.getHighchartsColour())
                        )
                    )
                )
            )
        );
  }

  @Test
  void getProductionChartData_gas_annual() {
    var applicationType = ApplicationType.PRODUCTION;
    var productionType = ProductionType.GAS;
    var consentLengthType = ConsentLengthType.ANNUAL;

    var productionCategoryUnit = ProductionUnit.KSCM_PER_DAY;
    var consentMonths = ProductionTestUtils.getAnnualProductionMonthsForYear(
        applicationVersion,
        2024);

    applicationVersion.getApplication().setType(applicationType);
    consentLengthDetails.setConsentLength(consentLengthType);

    when(applicationUnitService.getProductionGasUnit(applicationVersion)).thenReturn(productionCategoryUnit);
    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);
    when(annualProductionService.annualProductionMonthsExist(applicationVersion)).thenReturn(true);
    when(annualProductionService.getAnnualProductionMonths(applicationVersion)).thenReturn(consentMonths);

    assertThat(productionChartDataService.getProductionChartData(applicationVersion, productionType))
        .contains(
            new ProductionChartData(
                "Production min/max for gas (Jan - Dec 2024)",
                consentMonths.stream()
                    .map(AnnualProductionMonth::getMonth)
                    .map(month -> month.getDisplayName(TextStyle.SHORT, Locale.UK))
                    .toList(),
                "Month",
                "Volume (%s)".formatted(productionCategoryUnit.getDisplayName()),
                List.of(
                    new ProductionChartData.Series(
                        productionType.getSeriesName(),
                        productionType.getHighchartsColour(),
                        "columnrange",
                        List.of(
                            new ProductionChartData.DataPoint(
                                0,
                                100.0f,
                                1000.0f,
                                productionType.getHighchartsColour()),
                            new ProductionChartData.DataPoint(
                                1,
                                200.0f,
                                2000.0f,
                                productionType.getHighchartsColour()),
                            new ProductionChartData.DataPoint(
                                2,
                                300.0f,
                                3000.0f,
                                productionType.getHighchartsColour()),
                            new ProductionChartData.DataPoint(
                                3,
                                400.0f,
                                4000.0f,
                                productionType.getHighchartsColour()),
                            new ProductionChartData.DataPoint(
                                4,
                                500.0f,
                                5000.0f,
                                productionType.getHighchartsColour()),
                            new ProductionChartData.DataPoint(
                                5,
                                600.0f,
                                6000.0f,
                                productionType.getHighchartsColour()),
                            new ProductionChartData.DataPoint(
                                6,
                                700.0f,
                                7000.0f,
                                productionType.getHighchartsColour()),
                            new ProductionChartData.DataPoint(
                                7,
                                800.0f,
                                8000.0f,
                                productionType.getHighchartsColour()),
                            new ProductionChartData.DataPoint(
                                8,
                                900.0f,
                                9000.0f,
                                productionType.getHighchartsColour()),
                            new ProductionChartData.DataPoint(
                                9,
                                1000.0f,
                                10000.0f,
                                productionType.getHighchartsColour()),
                            new ProductionChartData.DataPoint(
                                10,
                                1100.0f,
                                11000.0f,
                                productionType.getHighchartsColour()),
                            new ProductionChartData.DataPoint(
                                11,
                                1200.0f,
                                12000.0f,
                                productionType.getHighchartsColour())
                        )
                    )
                )
            )
        );
  }


  @Test
  void getProductionChartData_gas_longterm() {
    var applicationType = ApplicationType.PRODUCTION;
    var productionType = ProductionType.GAS;
    var consentLengthType = ConsentLengthType.LONG_TERM;

    var productionCategoryUnit = ProductionUnit.KSCM_PER_DAY;
    var consentYears = ProductionTestUtils.getLongTermProductionYearsBetweenYears(
        applicationVersion,
        2025,
        2030);

    applicationVersion.getApplication().setType(applicationType);
    consentLengthDetails.setConsentLength(consentLengthType);

    when(applicationUnitService.getProductionGasUnit(applicationVersion)).thenReturn(productionCategoryUnit);
    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);
    when(longTermProductionService.longTermProductionYearsExist(applicationVersion)).thenReturn(true);
    when(longTermProductionService.getLongTermProductionYears(applicationVersion)).thenReturn(consentYears);

    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);

    assertThat(productionChartDataService.getProductionChartData(applicationVersion, productionType))
        .contains(
            new ProductionChartData(
                "%s (%s - %s)".formatted(
                    productionType.getSeriesName(),
                    consentYears.getFirst().getYear(),
                    consentYears.getLast().getYear()),
                consentYears.stream()
                    .map(LongTermProductionYear::getYear)
                    .map(Object::toString)
                    .toList(),
                "Year",
                "Volume (%s)".formatted(productionCategoryUnit.getDisplayName()),
                List.of(
                    new ProductionChartData.Series(
                        productionType.getSeriesName(),
                        productionType.getHighchartsColour(),
                        "columnrange",
                        List.of(
                            new ProductionChartData.DataPoint(
                                0,
                                10000.0f,
                                1000000.0f,
                                productionType.getHighchartsColour()),
                            new ProductionChartData.DataPoint(
                                1,
                                20000.0f,
                                2000000.0f,
                                productionType.getHighchartsColour()),
                            new ProductionChartData.DataPoint(
                                2,
                                30000.0f,
                                3000000.0f,
                                productionType.getHighchartsColour()),
                            new ProductionChartData.DataPoint(
                                3,
                                40000.0f,
                                4000000.0f,
                                productionType.getHighchartsColour()),
                            new ProductionChartData.DataPoint(
                                4,
                                50000.0f,
                                5000000.0f,
                                productionType.getHighchartsColour()),
                            new ProductionChartData.DataPoint(
                                5,
                                60000.0f,
                                6000000.0f,
                                productionType.getHighchartsColour())
                        )
                    )
                )
            )
        );
  }
}
