package uk.co.nstauthority.fieldconsents.application.summary.vent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.summary.SummaryTestUtil.VENT_INFORMATION_DISPLAY_ORDER;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthDetails;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthTestUtil;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.application.unit.ApplicationUnitService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.charts.EmissionsChartData;
import uk.co.nstauthority.fieldconsents.charts.EmissionsChartDataService;
import uk.co.nstauthority.fieldconsents.flarevent.EmissionCategoryType;
import uk.co.nstauthority.fieldconsents.flarevent.category123.vent.annual.VentAnnual123SummaryService;
import uk.co.nstauthority.fieldconsents.flarevent.category123.vent.shortterm.VentShortTerm123SummaryService;
import uk.co.nstauthority.fieldconsents.flarevent.category123.vent.ventreport.VentReport123SummaryService;
import uk.co.nstauthority.fieldconsents.flarevent.category123.vent.ventreportgas.VentReport123GasDataSummaryService;
import uk.co.nstauthority.fieldconsents.flarevent.vent.annual.VentAnnualService;
import uk.co.nstauthority.fieldconsents.flarevent.vent.longterm.VentLongTermSummaryService;
import uk.co.nstauthority.fieldconsents.flarevent.vent.shortterm.VentShortTermService;
import uk.co.nstauthority.fieldconsents.flarevent.vent.ventreport.VentReportSummaryService;
import uk.co.nstauthority.fieldconsents.flarevent.vent.ventreportgas.VentReportGasDataService;
import uk.co.nstauthority.fieldconsents.flarevent.vent.vents.VentSummaryService;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.summary.SummaryItem;
import uk.co.nstauthority.fieldconsents.summary.SummarySection;

@ExtendWith(MockitoExtension.class)
class VentInformationSummarySectionServiceTest {

  private static final String VENTS_ITEM = "Vents";

  private static final String VENT_REPORT_ITEM = "Vent report";

  private static final String VENT_REPORT_GAS_PROPERTIES_ITEM = "Vent report gas properties";

  private static final String UNSUPPORTED_OPERATION_EXCEPTION_MESSAGE = "Unsupported operation for %s";

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  @Mock
  private ConsentLengthService consentLengthService;

  @Mock
  private ApplicationUnitService applicationUnitService;

  @Mock
  private VentAnnualService ventAnnualService;

  @Mock
  private VentShortTermService ventShortTermService;

  @Mock
  private VentSummaryService ventSummaryService;

  @Mock
  private VentReportGasDataService ventReportGasDataService;

  @Mock
  private VentAnnual123SummaryService ventAnnual123SummaryService;

  @Mock
  private VentShortTerm123SummaryService ventShortTerm123SummaryService;

  @Mock
  private VentReport123SummaryService ventReport123SummaryService;

  @Mock
  private VentReport123GasDataSummaryService ventReport123GasDataSummaryService;

  @Mock
  private VentLongTermSummaryService ventLongTermSummaryService;

  @Mock
  private EmissionsChartDataService emissionsChartDataService;

  @Mock
  private VentReportSummaryService ventReportSummaryService;

  @InjectMocks
  private VentInformationSummarySectionService ventInformationSummarySectionService;

  private ApplicationVersion applicationVersion;

  private final EmissionsChartData emissionsChartData = new EmissionsChartData(
      "Example chart title",
      List.of("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sept", "Oct", "Nov", "Dec"),
      "Month",
      "Days in month",
      List.of(new EmissionsChartData.Series(
          "Days",
          "highcharts-color-1",
          List.of(
              new EmissionsChartData.DataPoint("highcharts-color-1", 31),
              new EmissionsChartData.DataPoint("highcharts-color-1", 29),
              new EmissionsChartData.DataPoint("highcharts-color-1", 31),
              new EmissionsChartData.DataPoint("highcharts-color-1", 30),
              new EmissionsChartData.DataPoint("highcharts-color-1", 31),
              new EmissionsChartData.DataPoint("highcharts-color-1", 30),
              new EmissionsChartData.DataPoint("highcharts-color-1", 31),
              new EmissionsChartData.DataPoint("highcharts-color-1", 31),
              new EmissionsChartData.DataPoint("highcharts-color-1", 30),
              new EmissionsChartData.DataPoint("highcharts-color-1", 31),
              new EmissionsChartData.DataPoint("highcharts-color-1", 30),
              new EmissionsChartData.DataPoint("highcharts-color-1", 31)
          )
      ))
  );

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.VENT);
  }

  @ParameterizedTest
  @EnumSource(value = ApplicationType.class, names = {"PRODUCTION", "FLARE"})
  void getSummarySection_nonVent(ApplicationType applicationType) {
    var nonVentAppVersion = ApplicationTestUtil.getNewApplicationVersionWithType(applicationType);

    assertThat(ventInformationSummarySectionService.getSummarySection(nonVentAppVersion, USER))
        .isNotPresent();

    verifyNoInteractions(consentLengthService);
  }

  @Test
  void getSummarySection_noConsentLengthDetails() {
    when(consentLengthService.findConsentLengthDetails(applicationVersion))
        .thenReturn(Optional.empty());

    assertThat(ventInformationSummarySectionService.getSummarySection(applicationVersion, USER))
        .isNotPresent();
  }

  @ParameterizedTest
  @EnumSource(value = ConsentLengthType.class, mode = EnumSource.Mode.EXCLUDE, names = {"LONG_TERM"})
  void getSummarySection_shortTermAndAnnual(ConsentLengthType consentLengthType) {
    var expectedSummaryCard = SummaryCard.emptySummaryCard();
    ConsentLengthDetails consentLengthDetails;

    if (ConsentLengthType.SHORT_TERM.equals(consentLengthType)) {
      consentLengthDetails = ConsentLengthTestUtil.getConsentLengthDetailsForShortTerm(applicationVersion);
      when(ventShortTermService.getVentShortTermSummaryCard(applicationVersion)).thenReturn(expectedSummaryCard);
    } else {
      consentLengthDetails = ConsentLengthTestUtil.getConsentLengthDetailsForAnnual(applicationVersion);
      when(ventAnnualService.getVentAnnualSummaryCard(applicationVersion)).thenReturn(expectedSummaryCard);
    }
    when(consentLengthService.findConsentLengthDetails(applicationVersion)).thenReturn(Optional.of(consentLengthDetails));
    when(applicationUnitService.getEmissionCategoryType(applicationVersion)).thenReturn(EmissionCategoryType.CATEGORY_ABC);
    when(ventSummaryService.getSummariesForVents(applicationVersion)).thenReturn(List.of(expectedSummaryCard));
    when(ventReportSummaryService.getVentReportSummaryCards(applicationVersion)).thenReturn(List.of(expectedSummaryCard));
    when(ventReportGasDataService.getVentReportGasDataSummaryCards(applicationVersion)).thenReturn(List.of(expectedSummaryCard));
    when(emissionsChartDataService.getConsentChartData(applicationVersion)).thenReturn(Optional.of(emissionsChartData));

    assertThat(ventInformationSummarySectionService.getSummarySection(applicationVersion, USER))
        .contains(new SummarySection(VENT_INFORMATION_DISPLAY_ORDER, List.of(
            SummaryItem.withCard(VENTS_ITEM, expectedSummaryCard),
            SummaryItem.withCard(VENT_REPORT_ITEM, expectedSummaryCard),
            SummaryItem.withCard(VENT_REPORT_GAS_PROPERTIES_ITEM, expectedSummaryCard),
            SummaryItem.withCards(consentLengthType.getDisplayName(), List.of(
                SummaryCard.stackedBarChartSummaryCard(emissionsChartData),
                expectedSummaryCard
            ))
        )));
  }

  @Test
  void getSummarySection_shortTerm_cat123() {
    var expectedSummaryCard = SummaryCard.emptySummaryCard();
    var consentLengthDetails = ConsentLengthTestUtil.getConsentLengthDetailsForShortTerm(applicationVersion);

    when(consentLengthService.findConsentLengthDetails(applicationVersion)).thenReturn(Optional.of(consentLengthDetails));
    when(applicationUnitService.getEmissionCategoryType(applicationVersion)).thenReturn(EmissionCategoryType.CATEGORY_123);
    when(ventShortTerm123SummaryService.getVentShortTerm123SummaryCard(applicationVersion)).thenReturn(expectedSummaryCard);
    when(emissionsChartDataService.getConsentChartData(applicationVersion)).thenReturn(Optional.empty());

    assertThat(ventInformationSummarySectionService.getSummarySection(applicationVersion, USER))
        .contains(new SummarySection(VENT_INFORMATION_DISPLAY_ORDER, List.of(
            SummaryItem.withCard(ConsentLengthType.SHORT_TERM.getDisplayName(), expectedSummaryCard)
        )));

    verifyNoInteractions(ventSummaryService);
    verifyNoInteractions(ventAnnual123SummaryService);
    verifyNoInteractions(ventReport123SummaryService);
    verifyNoInteractions(ventReport123GasDataSummaryService);
    verifyNoABCInteractions();
  }

  @Test
  void getSummarySection_annual_cat123() {
    var expectedSummaryCard = SummaryCard.emptySummaryCard();
    var consentLengthDetails = ConsentLengthTestUtil.getConsentLengthDetailsForAnnual(applicationVersion);

    when(consentLengthService.findConsentLengthDetails(applicationVersion)).thenReturn(Optional.of(consentLengthDetails));
    when(applicationUnitService.getEmissionCategoryType(applicationVersion)).thenReturn(EmissionCategoryType.CATEGORY_123);
    when(ventSummaryService.getSummariesForVents(applicationVersion)).thenReturn(List.of(expectedSummaryCard));
    when(ventReport123SummaryService.getVentReport123SummaryCard(applicationVersion)).thenReturn(expectedSummaryCard);
    when(ventReport123GasDataSummaryService.getVentReport123GasDataSummaryCard(applicationVersion)).thenReturn(expectedSummaryCard);
    when(ventAnnual123SummaryService.getVentAnnual123SummaryCard(applicationVersion)).thenReturn(expectedSummaryCard);
    when(emissionsChartDataService.getConsentChartData(applicationVersion)).thenReturn(Optional.empty());

    assertThat(ventInformationSummarySectionService.getSummarySection(applicationVersion, USER))
        .contains(new SummarySection(VENT_INFORMATION_DISPLAY_ORDER, List.of(
            SummaryItem.withCard(VENTS_ITEM, expectedSummaryCard),
            SummaryItem.withCard(VENT_REPORT_ITEM, expectedSummaryCard),
            SummaryItem.withCard(VENT_REPORT_GAS_PROPERTIES_ITEM, expectedSummaryCard),
            SummaryItem.withCard(ConsentLengthType.ANNUAL.getDisplayName(), expectedSummaryCard)
        )));

    verifyNoInteractions(ventShortTerm123SummaryService);
    verifyNoABCInteractions();
  }

  private void verifyNo123Interactions() {
    verifyNoInteractions(ventShortTerm123SummaryService);
    verifyNoInteractions(ventAnnual123SummaryService);
    verifyNoInteractions(ventReport123SummaryService);
    verifyNoInteractions(ventReport123GasDataSummaryService);
  }

  private void verifyNoABCInteractions() {
    verifyNoInteractions(ventShortTermService);
    verifyNoInteractions(ventAnnualService);
    verifyNoInteractions(ventReportGasDataService);
  }

  @Test
  void getSummarySection_longTerm() {
    var emissionCategoryType = EmissionCategoryType.LEGACY_LONG_TERM;
    var expectedSummaryCard = SummaryCard.emptySummaryCard();
    var consentLengthDetails = ConsentLengthTestUtil.getConsentLengthDetailsForLongTerm(applicationVersion);

    when(consentLengthService.findConsentLengthDetails(applicationVersion)).thenReturn(Optional.of(consentLengthDetails));
    when(applicationUnitService.getEmissionCategoryType(applicationVersion)).thenReturn(emissionCategoryType);
    when(ventLongTermSummaryService.getVentLongTermSummaryCard(applicationVersion)).thenReturn(expectedSummaryCard);
    when(emissionsChartDataService.getConsentChartData(applicationVersion)).thenReturn(Optional.empty());

    assertThat(ventInformationSummarySectionService.getSummarySection(applicationVersion, USER))
        .contains(new SummarySection(VENT_INFORMATION_DISPLAY_ORDER, List.of(
            SummaryItem.withCard(ConsentLengthType.LONG_TERM.getDisplayName(), expectedSummaryCard)
        )));

    verifyNoInteractions(ventSummaryService);
    verifyNo123Interactions();
    verifyNoABCInteractions();
  }

  @ParameterizedTest
  @EnumSource(value = EmissionCategoryType.class, mode = EnumSource.Mode.EXCLUDE, names = "LEGACY_LONG_TERM")
  void getSummarySection_longTerm_invalid(EmissionCategoryType emissionCategoryType) {
    var consentLengthDetails = ConsentLengthTestUtil.getConsentLengthDetailsForLongTerm(applicationVersion);

    when(consentLengthService.findConsentLengthDetails(applicationVersion))
        .thenReturn(Optional.of(consentLengthDetails));
    when(applicationUnitService.getEmissionCategoryType(applicationVersion))
        .thenReturn(emissionCategoryType);

    assertThatThrownBy(() -> ventInformationSummarySectionService.getSummarySection(applicationVersion, USER))
        .isInstanceOf(RuntimeException.class)
        .hasMessage(UNSUPPORTED_OPERATION_EXCEPTION_MESSAGE.formatted(emissionCategoryType.name()));
  }

  @Test
  void getSummarySection_shortTerm_invalid() {
    var emissionCategoryType = EmissionCategoryType.LEGACY_LONG_TERM;
    var consentLengthDetails = ConsentLengthTestUtil.getConsentLengthDetailsForShortTerm(applicationVersion);

    when(consentLengthService.findConsentLengthDetails(applicationVersion))
        .thenReturn(Optional.of(consentLengthDetails));
    when(applicationUnitService.getEmissionCategoryType(applicationVersion))
        .thenReturn(emissionCategoryType);

    assertThatThrownBy(() -> ventInformationSummarySectionService.getSummarySection(applicationVersion, USER))
        .isInstanceOf(RuntimeException.class)
        .hasMessage(UNSUPPORTED_OPERATION_EXCEPTION_MESSAGE.formatted(emissionCategoryType.name()));
  }

  @Test
  void getSummarySection_annual_invalid() {
    var emissionCategoryType = EmissionCategoryType.LEGACY_LONG_TERM;
    var consentLengthDetails = ConsentLengthTestUtil.getConsentLengthDetailsForAnnual(applicationVersion);

    when(consentLengthService.findConsentLengthDetails(applicationVersion))
        .thenReturn(Optional.of(consentLengthDetails));
    when(applicationUnitService.getEmissionCategoryType(applicationVersion))
        .thenReturn(emissionCategoryType);

    assertThatThrownBy(() -> ventInformationSummarySectionService.getSummarySection(applicationVersion, USER))
        .isInstanceOf(RuntimeException.class)
        .hasMessage(UNSUPPORTED_OPERATION_EXCEPTION_MESSAGE.formatted(emissionCategoryType.name()));
  }
}
