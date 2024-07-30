package uk.co.nstauthority.fieldconsents.application.summary.flare;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType.SHORT_TERM;
import static uk.co.nstauthority.fieldconsents.application.summary.SummaryTestUtil.FLARE_INFORMATION_DISPLAY_ORDER;
import static uk.co.nstauthority.fieldconsents.application.summary.SummaryTestUtil.assertSummarySection;

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
import uk.co.nstauthority.fieldconsents.flarevent.category123.flare.annual.FlareAnnual123SummaryService;
import uk.co.nstauthority.fieldconsents.flarevent.category123.flare.flarereport.FlareReport123SummaryService;
import uk.co.nstauthority.fieldconsents.flarevent.category123.flare.flarereportgas.FlareReport123GasDataSummaryService;
import uk.co.nstauthority.fieldconsents.flarevent.category123.flare.shortterm.FlareShortTerm123SummaryService;
import uk.co.nstauthority.fieldconsents.flarevent.flare.annual.FlareAnnualService;
import uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport.FlareReportSummaryService;
import uk.co.nstauthority.fieldconsents.flarevent.flare.flarereportgas.FlareReportGasDataService;
import uk.co.nstauthority.fieldconsents.flarevent.flare.flares.FlareSummaryService;
import uk.co.nstauthority.fieldconsents.flarevent.flare.longterm.FlareLongTermSummaryService;
import uk.co.nstauthority.fieldconsents.flarevent.flare.shortterm.FlareShortTermService;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.summary.SummaryItem;
import uk.co.nstauthority.fieldconsents.summary.SummarySection;

@ExtendWith(MockitoExtension.class)
class FlareInformationSummarySectionServiceTest {

  private static final String FLARES_ITEM = "Flares";

  private static final String FLARE_REPORT_ITEM = "Flare report";

  private static final String FLARE_REPORT_GAS_PROPERTIES_ITEM = "Flare report gas properties";

  private static final String UNSUPPORTED_OPERATION_EXCEPTION_MESSAGE = "Unsupported operation for %s";

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  @Mock
  private ConsentLengthService consentLengthService;

  @Mock
  private ApplicationUnitService applicationUnitService;

  @Mock
  private FlareAnnualService flareAnnualService;

  @Mock
  private FlareShortTermService flareShortTermService;

  @Mock
  private FlareSummaryService flareSummaryService;

  @Mock
  private FlareReportGasDataService flareReportGasDataService;

  @Mock
  private FlareAnnual123SummaryService flareAnnual123SummaryService;

  @Mock
  private FlareShortTerm123SummaryService flareShortTerm123SummaryService;

  @Mock
  private FlareReport123SummaryService flareReport123SummaryService;

  @Mock
  private FlareReport123GasDataSummaryService flareReport123GasDataSummaryService;

  @Mock
  private FlareLongTermSummaryService flareLongTermSummaryService;

  @Mock
  private EmissionsChartDataService emissionsChartDataService;

  @Mock
  private FlareReportSummaryService flareReportSummaryService;

  @InjectMocks
  private FlareInformationSummarySectionService flareInformationSummarySectionService;

  private ApplicationVersion applicationVersion;

  private final EmissionsChartData emissionsChartData = new EmissionsChartData(
      "Example chart title",
      List.of("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sept", "Oct", "Nov", "Dec"),
      "Month",
      "Days in month",
      List.of(new EmissionsChartData.Series(
          "Days",
          "highcharts-colour-blue",
          List.of(31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
      ))
  );

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.FLARE);
  }

  @ParameterizedTest
  @EnumSource(value = ApplicationType.class, names = {"PRODUCTION", "VENT"})
  void getSummarySection_nonFlare(ApplicationType applicationType) {
    var nonFlareAppVersion = ApplicationTestUtil.getNewApplicationVersionWithType(applicationType);

    assertThat(flareInformationSummarySectionService.getSummarySection(nonFlareAppVersion, USER))
        .isNotPresent();

    verifyNoInteractions(consentLengthService);
  }

  @Test
  void getSummarySection_noConsentLengthDetails() {
    when(consentLengthService.findConsentLengthDetails(applicationVersion))
        .thenReturn(Optional.empty());

    assertThat(flareInformationSummarySectionService.getSummarySection(applicationVersion, USER))
        .isNotPresent();
  }

  @ParameterizedTest
  @EnumSource(value = ConsentLengthType.class, mode = EnumSource.Mode.EXCLUDE, names = {"LONG_TERM"})
  void getSummarySection_shortTermAndAnnual_catABC(ConsentLengthType consentLengthType) {
    var expectedSummaryCard = SummaryCard.emptySummaryCard();
    ConsentLengthDetails consentLengthDetails;

    if (ConsentLengthType.SHORT_TERM.equals(consentLengthType)) {
      consentLengthDetails = ConsentLengthTestUtil.getConsentLengthDetailsForShortTerm(applicationVersion);
      when(flareShortTermService.getFlareShortTermSummaryCard(applicationVersion)).thenReturn(expectedSummaryCard);
    } else {
      consentLengthDetails = ConsentLengthTestUtil.getConsentLengthDetailsForAnnual(applicationVersion);
      when(flareAnnualService.getFlareAnnualSummaryCard(applicationVersion)).thenReturn(expectedSummaryCard);
    }

    when(consentLengthService.findConsentLengthDetails(applicationVersion)).thenReturn(Optional.of(consentLengthDetails));
    when(applicationUnitService.getEmissionCategoryType(applicationVersion)).thenReturn(EmissionCategoryType.CATEGORY_ABC);
    when(flareSummaryService.getSummariesForFlares(applicationVersion)).thenReturn(List.of(expectedSummaryCard));
    when(flareReportSummaryService.getFlareReportSummaryCards(applicationVersion)).thenReturn(List.of(expectedSummaryCard));
    when(flareReportGasDataService.getFlareReportGasDataSummaryCards(applicationVersion)).thenReturn(List.of(expectedSummaryCard));
    when(emissionsChartDataService.getConsentChartData(applicationVersion)).thenReturn(Optional.of(emissionsChartData));

    assertThat(flareInformationSummarySectionService.getSummarySection(applicationVersion, USER))
        .contains(new SummarySection(FLARE_INFORMATION_DISPLAY_ORDER, List.of(
            SummaryItem.withCard(FLARES_ITEM, expectedSummaryCard),
            SummaryItem.withCard(FLARE_REPORT_ITEM, expectedSummaryCard),
            SummaryItem.withCard(FLARE_REPORT_GAS_PROPERTIES_ITEM, expectedSummaryCard),
            SummaryItem.withCards(consentLengthType.getDisplayName(), List.of(
                SummaryCard.stackedBarChartSummaryCard(emissionsChartData),
                expectedSummaryCard
            ))
        )));

    if (SHORT_TERM.equals(consentLengthType)) {
      verifyNoInteractions(flareAnnualService);
    } else {
      verifyNoInteractions(flareShortTermService);
    }

    verifyNoInteractions(flareReport123SummaryService);
    verifyNoInteractions(flareReport123GasDataSummaryService);
    verifyNoInteractions(flareAnnual123SummaryService);
    verifyNoInteractions(flareShortTerm123SummaryService);
  }

  @Test
  void getSummarySection_shortTerm_cat123() {
    var tableSummaryCard = mock(SummaryCard.class);
    var consentLengthDetails = ConsentLengthTestUtil.getConsentLengthDetailsForShortTerm(applicationVersion);

    when(consentLengthService.findConsentLengthDetails(applicationVersion)).thenReturn(Optional.of(consentLengthDetails));
    when(applicationUnitService.getEmissionCategoryType(applicationVersion)).thenReturn(EmissionCategoryType.CATEGORY_123);
    when(flareShortTerm123SummaryService.getFlareShortTerm123SummaryCard(applicationVersion)).thenReturn(tableSummaryCard);
    when(emissionsChartDataService.getConsentChartData(applicationVersion)).thenReturn(Optional.empty());

    var summarySectionOptional = flareInformationSummarySectionService.getSummarySection(applicationVersion, USER);

    assertThat(summarySectionOptional).isNotEmpty();
    var summarySection = summarySectionOptional.get();
    assertSummarySection(summarySection, FLARE_INFORMATION_DISPLAY_ORDER);

    var summaryItems = summarySection.summaryItems();
    assertThat(summaryItems).contains(SummaryItem.withCard(SHORT_TERM.getDisplayName(), tableSummaryCard));

    verifyNoInteractions(flareSummaryService);
    verifyNoInteractions(flareAnnual123SummaryService);
    verifyNoInteractions(flareReport123SummaryService);
    verifyNoInteractions(flareReport123GasDataSummaryService);
    verifyNoABCInteractions();
  }

  @Test
  void getSummarySection_annual_cat123() {
    var expectedSummaryCard = SummaryCard.emptySummaryCard();
    var consentLengthDetails = ConsentLengthTestUtil.getConsentLengthDetailsForAnnual(applicationVersion);

    when(consentLengthService.findConsentLengthDetails(applicationVersion)).thenReturn(Optional.of(consentLengthDetails));
    when(applicationUnitService.getEmissionCategoryType(applicationVersion)).thenReturn(EmissionCategoryType.CATEGORY_123);
    when(flareSummaryService.getSummariesForFlares(applicationVersion)).thenReturn(List.of(expectedSummaryCard));
    when(flareReport123SummaryService.getFlareReport123SummaryCard(applicationVersion)).thenReturn(expectedSummaryCard);
    when(flareReport123GasDataSummaryService.getFlareReport123GasDataSummaryCard(applicationVersion)).thenReturn(expectedSummaryCard);
    when(flareAnnual123SummaryService.getFlareAnnual123SummaryCard(applicationVersion)).thenReturn(expectedSummaryCard);
    when(emissionsChartDataService.getConsentChartData(applicationVersion)).thenReturn(Optional.empty());

    assertThat(flareInformationSummarySectionService.getSummarySection(applicationVersion, USER))
        .contains(new SummarySection(FLARE_INFORMATION_DISPLAY_ORDER, List.of(
            SummaryItem.withCard(FLARES_ITEM, expectedSummaryCard),
            SummaryItem.withCard(FLARE_REPORT_ITEM, expectedSummaryCard),
            SummaryItem.withCard(FLARE_REPORT_GAS_PROPERTIES_ITEM, expectedSummaryCard),
            SummaryItem.withCard(ConsentLengthType.ANNUAL.getDisplayName(), expectedSummaryCard)
        )));

    verifyNoInteractions(flareShortTerm123SummaryService);
    verifyNoABCInteractions();
  }

  private void verifyNo123Interactions() {
    verifyNoInteractions(flareReport123SummaryService);
    verifyNoInteractions(flareReport123GasDataSummaryService);
    verifyNoInteractions(flareAnnual123SummaryService);
    verifyNoInteractions(flareShortTerm123SummaryService);
  }

  private void verifyNoABCInteractions() {
    verifyNoInteractions(flareShortTermService);
    verifyNoInteractions(flareAnnualService);
    verifyNoInteractions(flareReportGasDataService);
  }

  @Test
  void getSummarySection_longTerm() {
    var emissionCategoryType = EmissionCategoryType.LEGACY_LONG_TERM;
    var expectedSummaryCard = SummaryCard.emptySummaryCard();
    var consentLengthDetails = ConsentLengthTestUtil.getConsentLengthDetailsForLongTerm(applicationVersion);

    when(consentLengthService.findConsentLengthDetails(applicationVersion)).thenReturn(Optional.of(consentLengthDetails));
    when(applicationUnitService.getEmissionCategoryType(applicationVersion)).thenReturn(emissionCategoryType);
    when(flareLongTermSummaryService.getFlareLongTermSummaryCard(applicationVersion)).thenReturn(expectedSummaryCard);
    when(emissionsChartDataService.getConsentChartData(applicationVersion)).thenReturn(Optional.of(emissionsChartData));

    assertThat(flareInformationSummarySectionService.getSummarySection(applicationVersion, USER))
        .contains(new SummarySection(FLARE_INFORMATION_DISPLAY_ORDER, List.of(
            SummaryItem.withCards(ConsentLengthType.LONG_TERM.getDisplayName(), List.of(
                SummaryCard.stackedBarChartSummaryCard(emissionsChartData),
                expectedSummaryCard
            ))
        )));

    verifyNoInteractions(flareSummaryService);
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

    assertThatThrownBy(() -> flareInformationSummarySectionService.getSummarySection(applicationVersion, USER))
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

    assertThatThrownBy(() -> flareInformationSummarySectionService.getSummarySection(applicationVersion, USER))
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

    assertThatThrownBy(() -> flareInformationSummarySectionService.getSummarySection(applicationVersion, USER))
        .isInstanceOf(RuntimeException.class)
        .hasMessage(UNSUPPORTED_OPERATION_EXCEPTION_MESSAGE.formatted(emissionCategoryType.name()));
  }
}
