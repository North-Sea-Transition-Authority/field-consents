package uk.co.nstauthority.fieldconsents.charts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
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
import uk.co.nstauthority.fieldconsents.flarevent.EmissionCategoryType;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentRow;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentUnit;
import uk.co.nstauthority.fieldconsents.flarevent.flare.annual.FlareAnnualService;
import uk.co.nstauthority.fieldconsents.flarevent.flare.annual.FlareAnnualTestUtil;
import uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport.FlareReportService;
import uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport.FlareReportTestUtil;
import uk.co.nstauthority.fieldconsents.flarevent.flare.shortterm.FlareShortTermService;
import uk.co.nstauthority.fieldconsents.flarevent.flare.shortterm.FlareShortTermTestUtil;
import uk.co.nstauthority.fieldconsents.flarevent.vent.annual.VentAnnualService;
import uk.co.nstauthority.fieldconsents.flarevent.vent.annual.VentAnnualTestUtil;
import uk.co.nstauthority.fieldconsents.flarevent.vent.shortterm.VentShortTermService;
import uk.co.nstauthority.fieldconsents.flarevent.vent.shortterm.VentShortTermTestUtil;
import uk.co.nstauthority.fieldconsents.flarevent.vent.ventreport.VentReportService;
import uk.co.nstauthority.fieldconsents.flarevent.vent.ventreport.VentReportTestUtil;

@ExtendWith(MockitoExtension.class)
class EmissionsChartDataServiceTest {

  @Mock
  private ApplicationUnitService applicationUnitService;

  @Mock
  private ConsentLengthService consentLengthService;

  @Mock
  private FlareReportService flareReportService;

  @Mock
  private VentReportService ventReportService;

  @Mock
  private FlareShortTermService flareShortTermService;

  @Mock
  private FlareAnnualService flareAnnualService;

  @Mock
  private VentShortTermService ventShortTermService;

  @Mock
  private VentAnnualService ventAnnualService;

  @InjectMocks
  private EmissionsChartDataService emissionsChartDataService;

  private ApplicationVersion applicationVersion;
  private ConsentLengthDetails consentLengthDetails;

  @BeforeEach
  void setUp() {
    applicationVersion = new ApplicationVersion();
    applicationVersion.setApplication(new Application());

    consentLengthDetails = new ConsentLengthDetails();
  }

  @Test
  void getReportChartData_flare_abc() {
    var applicationType = ApplicationType.FLARE;
    var emissionCategoryUnit = FlareVentUnit.TONNES_PER_MONTH;
    var reportMonths = FlareReportTestUtil.getFlareReportMonthsForYear(applicationVersion, LocalDate.now().getYear());

    applicationVersion.getApplication().setType(applicationType);

    when(applicationUnitService.getEmissionCategoryType(applicationVersion)).thenReturn(EmissionCategoryType.CATEGORY_ABC);
    when(flareReportService.flareReportMonthsComplete(applicationVersion)).thenReturn(true);
    when(flareReportService.getFlareReportMonths(applicationVersion)).thenReturn(reportMonths);
    when(applicationUnitService.getEmissionCategoryUnit(applicationVersion)).thenReturn(emissionCategoryUnit);

    assertThat(emissionsChartDataService.getReportChartData(applicationVersion))
        .contains(new EmissionsChartData(
            EmissionsChartType.REPORT.getHeading(
                applicationType,
                reportMonths.getFirst().getYearMonth(),
                reportMonths.getLast().getYearMonth()
            ),
            reportMonths.stream().map(FlareVentRow::getMonth).map(month -> month.getDisplayName(TextStyle.SHORT, Locale.UK)).toList(),
            "Month",
            "Volume (%s)".formatted(emissionCategoryUnit.getDisplayName()),
            List.of(
                new EmissionsChartData.Series(
                    "Category A",
                    HighchartsColour.LIGHT_BLUE,
                    reportMonths.stream().map(FlareVentRow::getCategoryA).map(BigDecimal::floatValue).toList()
                ),
                new EmissionsChartData.Series(
                    "Category B",
                    HighchartsColour.BLUE,
                    reportMonths.stream().map(FlareVentRow::getCategoryB).map(BigDecimal::floatValue).toList()
                ),
                new EmissionsChartData.Series(
                    "Category C",
                    HighchartsColour.DARK_BLUE,
                    reportMonths.stream().map(FlareVentRow::getCategoryC).map(BigDecimal::floatValue).toList()
                )
            )
        ));
  }

  @Test
  void getReportChartData_vent() {
    var applicationType = ApplicationType.VENT;
    var emissionCategoryUnit = FlareVentUnit.TONNES_PER_MONTH;
    var reportMonths = VentReportTestUtil.getVentReportMonthsForYear(applicationVersion, LocalDate.now().getYear());

    applicationVersion.getApplication().setType(applicationType);

    when(applicationUnitService.getEmissionCategoryType(applicationVersion)).thenReturn(EmissionCategoryType.CATEGORY_ABC);
    when(ventReportService.ventReportMonthsComplete(applicationVersion)).thenReturn(true);
    when(ventReportService.getVentReportMonths(applicationVersion)).thenReturn(reportMonths);
    when(applicationUnitService.getEmissionCategoryUnit(applicationVersion)).thenReturn(emissionCategoryUnit);

    assertThat(emissionsChartDataService.getReportChartData(applicationVersion))
        .contains(new EmissionsChartData(
            EmissionsChartType.REPORT.getHeading(
                applicationType,
                reportMonths.getFirst().getYearMonth(),
                reportMonths.getLast().getYearMonth()
            ),
            reportMonths.stream().map(FlareVentRow::getMonth).map(month -> month.getDisplayName(TextStyle.SHORT, Locale.UK)).toList(),
            "Month",
            "Volume (%s)".formatted(emissionCategoryUnit.getDisplayName()),
            List.of(
                new EmissionsChartData.Series(
                    "Category A",
                    HighchartsColour.LIGHT_BLUE,
                    reportMonths.stream().map(FlareVentRow::getCategoryA).map(BigDecimal::floatValue).toList()
                ),
                new EmissionsChartData.Series(
                    "Category B",
                    HighchartsColour.BLUE,
                    reportMonths.stream().map(FlareVentRow::getCategoryB).map(BigDecimal::floatValue).toList()
                ),
                new EmissionsChartData.Series(
                    "Category C",
                    HighchartsColour.DARK_BLUE,
                    reportMonths.stream().map(FlareVentRow::getCategoryC).map(BigDecimal::floatValue).toList()
                )
            )
        ));
  }

  @ParameterizedTest
  @EnumSource(value = ApplicationType.class, names = {"FLARE", "VENT"}, mode = Mode.EXCLUDE)
  void getReportChartData_nonEmissions(ApplicationType applicationType) {
    applicationVersion.getApplication().setType(applicationType);
    assertThat(emissionsChartDataService.getReportChartData(applicationVersion)).isEmpty();
  }

  @Test
  void getConsentChartData_flare_abc_shortTerm() {
    var applicationType = ApplicationType.FLARE;
    var emissionsCategoryType = EmissionCategoryType.CATEGORY_ABC;
    var consentLengthType = ConsentLengthType.SHORT_TERM;

    var emissionCategoryUnit = FlareVentUnit.TONNES_PER_MONTH;
    var consentMonths = FlareShortTermTestUtil.getFlareShortTermMonthsForPeriod(applicationVersion, LocalDate.now().minusDays(7), LocalDate.now());

    applicationVersion.getApplication().setType(applicationType);
    consentLengthDetails.setConsentLength(consentLengthType);

    when(applicationUnitService.getEmissionCategoryType(applicationVersion)).thenReturn(emissionsCategoryType);
    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);
    when(flareShortTermService.flareShortTermMonthsExist(applicationVersion)).thenReturn(true);
    when(flareShortTermService.getFlareShortTermMonths(applicationVersion)).thenReturn(consentMonths);
    when(applicationUnitService.getEmissionCategoryUnit(applicationVersion)).thenReturn(emissionCategoryUnit);

    assertThat(emissionsChartDataService.getConsentChartData(applicationVersion))
        .contains(new EmissionsChartData(
            EmissionsChartType.CONSENT.getHeading(
                applicationType,
                consentMonths.getFirst().getYearMonth(),
                consentMonths.getLast().getYearMonth()
            ),
            consentMonths.stream().map(FlareVentRow::getMonth).map(month -> month.getDisplayName(TextStyle.SHORT, Locale.UK)).toList(),
            "Month",
            "Volume (%s)".formatted(emissionCategoryUnit.getDisplayName()),
            List.of(
                new EmissionsChartData.Series(
                    "Category A",
                    HighchartsColour.LIGHT_BLUE,
                    consentMonths.stream().map(FlareVentRow::getCategoryA).map(BigDecimal::floatValue).toList()
                ),
                new EmissionsChartData.Series(
                    "Category B",
                    HighchartsColour.BLUE,
                    consentMonths.stream().map(FlareVentRow::getCategoryB).map(BigDecimal::floatValue).toList()
                ),
                new EmissionsChartData.Series(
                    "Category C",
                    HighchartsColour.DARK_BLUE,
                    consentMonths.stream().map(FlareVentRow::getCategoryC).map(BigDecimal::floatValue).toList()
                )
            )
        ));
  }

  @Test
  void getConsentChartData_flare_abc_annual() {
    var applicationType = ApplicationType.FLARE;
    var emissionsCategoryType = EmissionCategoryType.CATEGORY_ABC;
    var consentLengthType = ConsentLengthType.ANNUAL;

    var emissionCategoryUnit = FlareVentUnit.TONNES_PER_MONTH;
    var consentMonths = FlareAnnualTestUtil.getFlareAnnualMonthsForYear(applicationVersion, LocalDate.now().getYear());

    applicationVersion.getApplication().setType(applicationType);
    consentLengthDetails.setConsentLength(consentLengthType);

    when(applicationUnitService.getEmissionCategoryType(applicationVersion)).thenReturn(emissionsCategoryType);
    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);
    when(flareAnnualService.flareAnnualMonthsExist(applicationVersion)).thenReturn(true);
    when(flareAnnualService.getFlareAnnualMonths(applicationVersion)).thenReturn(consentMonths);
    when(applicationUnitService.getEmissionCategoryUnit(applicationVersion)).thenReturn(emissionCategoryUnit);

    assertThat(emissionsChartDataService.getConsentChartData(applicationVersion))
        .contains(new EmissionsChartData(
            EmissionsChartType.CONSENT.getHeading(
                applicationType,
                consentMonths.getFirst().getYearMonth(),
                consentMonths.getLast().getYearMonth()
            ),
            consentMonths.stream().map(FlareVentRow::getMonth).map(month -> month.getDisplayName(TextStyle.SHORT, Locale.UK)).toList(),
            "Month",
            "Volume (%s)".formatted(emissionCategoryUnit.getDisplayName()),
            List.of(
                new EmissionsChartData.Series(
                    "Category A",
                    HighchartsColour.LIGHT_BLUE,
                    consentMonths.stream().map(FlareVentRow::getCategoryA).map(BigDecimal::floatValue).toList()
                ),
                new EmissionsChartData.Series(
                    "Category B",
                    HighchartsColour.BLUE,
                    consentMonths.stream().map(FlareVentRow::getCategoryB).map(BigDecimal::floatValue).toList()
                ),
                new EmissionsChartData.Series(
                    "Category C",
                    HighchartsColour.DARK_BLUE,
                    consentMonths.stream().map(FlareVentRow::getCategoryC).map(BigDecimal::floatValue).toList()
                )
            )
        ));
  }

  @Test
  void getConsentChartData_flare_abc_longTerm() {
    var applicationType = ApplicationType.FLARE;
    var emissionsCategoryType = EmissionCategoryType.CATEGORY_ABC;
    var consentLengthType = ConsentLengthType.LONG_TERM;

    applicationVersion.getApplication().setType(applicationType);
    consentLengthDetails.setConsentLength(consentLengthType);

    when(applicationUnitService.getEmissionCategoryType(applicationVersion)).thenReturn(emissionsCategoryType);
    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);

    assertThat(emissionsChartDataService.getConsentChartData(applicationVersion)).isEmpty();
  }

  @Test
  void getConsentChartData_vent_abc_shortTerm() {
    var applicationType = ApplicationType.VENT;
    var emissionsCategoryType = EmissionCategoryType.CATEGORY_ABC;
    var consentLengthType = ConsentLengthType.SHORT_TERM;

    var emissionCategoryUnit = FlareVentUnit.TONNES_PER_MONTH;
    var consentMonths = VentShortTermTestUtil.getVentShortTermMonthsForPeriod(applicationVersion, LocalDate.now().minusDays(7), LocalDate.now());

    applicationVersion.getApplication().setType(applicationType);
    consentLengthDetails.setConsentLength(consentLengthType);

    when(applicationUnitService.getEmissionCategoryType(applicationVersion)).thenReturn(emissionsCategoryType);
    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);
    when(ventShortTermService.ventShortTermMonthsExist(applicationVersion)).thenReturn(true);
    when(ventShortTermService.getVentShortTermMonths(applicationVersion)).thenReturn(consentMonths);
    when(applicationUnitService.getEmissionCategoryUnit(applicationVersion)).thenReturn(emissionCategoryUnit);

    assertThat(emissionsChartDataService.getConsentChartData(applicationVersion))
        .contains(new EmissionsChartData(
            EmissionsChartType.CONSENT.getHeading(
                applicationType,
                consentMonths.getFirst().getYearMonth(),
                consentMonths.getLast().getYearMonth()
            ),
            consentMonths.stream().map(FlareVentRow::getMonth).map(month -> month.getDisplayName(TextStyle.SHORT, Locale.UK)).toList(),
            "Month",
            "Volume (%s)".formatted(emissionCategoryUnit.getDisplayName()),
            List.of(
                new EmissionsChartData.Series(
                    "Category A",
                    HighchartsColour.LIGHT_BLUE,
                    consentMonths.stream().map(FlareVentRow::getCategoryA).map(BigDecimal::floatValue).toList()
                ),
                new EmissionsChartData.Series(
                    "Category B",
                    HighchartsColour.BLUE,
                    consentMonths.stream().map(FlareVentRow::getCategoryB).map(BigDecimal::floatValue).toList()
                ),
                new EmissionsChartData.Series(
                    "Category C",
                    HighchartsColour.DARK_BLUE,
                    consentMonths.stream().map(FlareVentRow::getCategoryC).map(BigDecimal::floatValue).toList()
                )
            )
        ));
  }

  @Test
  void getConsentChartData_vent_abc_annual() {
    var applicationType = ApplicationType.VENT;
    var emissionsCategoryType = EmissionCategoryType.CATEGORY_ABC;
    var consentLengthType = ConsentLengthType.ANNUAL;

    var emissionCategoryUnit = FlareVentUnit.TONNES_PER_MONTH;
    var consentMonths = VentAnnualTestUtil.getVentAnnualMonthsForYear(applicationVersion, LocalDate.now().getYear());

    applicationVersion.getApplication().setType(applicationType);
    consentLengthDetails.setConsentLength(consentLengthType);

    when(applicationUnitService.getEmissionCategoryType(applicationVersion)).thenReturn(emissionsCategoryType);
    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);
    when(ventAnnualService.ventAnnualMonthsExist(applicationVersion)).thenReturn(true);
    when(ventAnnualService.getVentAnnualMonths(applicationVersion)).thenReturn(consentMonths);
    when(applicationUnitService.getEmissionCategoryUnit(applicationVersion)).thenReturn(emissionCategoryUnit);

    assertThat(emissionsChartDataService.getConsentChartData(applicationVersion))
        .contains(new EmissionsChartData(
            EmissionsChartType.CONSENT.getHeading(
                applicationType,
                consentMonths.getFirst().getYearMonth(),
                consentMonths.getLast().getYearMonth()
            ),
            consentMonths.stream().map(FlareVentRow::getMonth).map(month -> month.getDisplayName(TextStyle.SHORT, Locale.UK)).toList(),
            "Month",
            "Volume (%s)".formatted(emissionCategoryUnit.getDisplayName()),
            List.of(
                new EmissionsChartData.Series(
                    "Category A",
                    HighchartsColour.LIGHT_BLUE,
                    consentMonths.stream().map(FlareVentRow::getCategoryA).map(BigDecimal::floatValue).toList()
                ),
                new EmissionsChartData.Series(
                    "Category B",
                    HighchartsColour.BLUE,
                    consentMonths.stream().map(FlareVentRow::getCategoryB).map(BigDecimal::floatValue).toList()
                ),
                new EmissionsChartData.Series(
                    "Category C",
                    HighchartsColour.DARK_BLUE,
                    consentMonths.stream().map(FlareVentRow::getCategoryC).map(BigDecimal::floatValue).toList()
                )
            )
        ));
  }

  @Test
  void getConsentChartData_vent_abc_longTerm() {
    var applicationType = ApplicationType.VENT;
    var emissionsCategoryType = EmissionCategoryType.CATEGORY_ABC;
    var consentLengthType = ConsentLengthType.LONG_TERM;

    applicationVersion.getApplication().setType(applicationType);
    consentLengthDetails.setConsentLength(consentLengthType);

    when(applicationUnitService.getEmissionCategoryType(applicationVersion)).thenReturn(emissionsCategoryType);
    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);

    assertThat(emissionsChartDataService.getConsentChartData(applicationVersion)).isEmpty();
  }

}