package uk.co.nstauthority.fieldconsents.application.summary.vent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.summary.SummaryTestUtil.VENT_INFORMATION_DISPLAY_ORDER;
import static uk.co.nstauthority.fieldconsents.application.summary.SummaryTestUtil.assertEmptySummaryCard;
import static uk.co.nstauthority.fieldconsents.application.summary.SummaryTestUtil.assertSummaryItem;
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
import uk.co.nstauthority.fieldconsents.flarevent.EmissionCategoryType;
import uk.co.nstauthority.fieldconsents.flarevent.category123.vent.annual.VentAnnual123SummaryService;
import uk.co.nstauthority.fieldconsents.flarevent.category123.vent.shortterm.VentShortTerm123SummaryService;
import uk.co.nstauthority.fieldconsents.flarevent.category123.vent.ventreport.VentReport123SummaryService;
import uk.co.nstauthority.fieldconsents.flarevent.category123.vent.ventreportgas.VentReport123GasDataSummaryService;
import uk.co.nstauthority.fieldconsents.flarevent.vent.annual.VentAnnualService;
import uk.co.nstauthority.fieldconsents.flarevent.vent.shortterm.VentShortTermService;
import uk.co.nstauthority.fieldconsents.flarevent.vent.ventreport.VentReportService;
import uk.co.nstauthority.fieldconsents.flarevent.vent.ventreportgas.VentReportGasDataService;
import uk.co.nstauthority.fieldconsents.flarevent.vent.vents.VentSummaryService;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.summary.SummaryItem;

@ExtendWith(MockitoExtension.class)
class VentInformationSummarySectionServiceTest {

  private static final String VENTS_ITEM = "Vents";

  private static final String VENT_REPORT_ITEM = "Vent report";

  private static final String VENT_REPORT_GAS_PROPERTIES_ITEM = "Vent report gas properties";

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
  private VentReportService ventReportService;

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

  @InjectMocks
  private VentInformationSummarySectionService ventInformationSummarySectionService;

  ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.VENT);
  }

  @ParameterizedTest
  @EnumSource(value = ApplicationType.class, names = {"PRODUCTION", "FLARE"})
  void getSummarySection_nonVent(ApplicationType applicationType) {
    var nonVentAppVersion = ApplicationTestUtil.getNewApplicationVersionWithType(applicationType);

    assertThat(ventInformationSummarySectionService.getSummarySection(nonVentAppVersion))
        .isNotPresent();

    verifyNoInteractions(consentLengthService);
  }

  @Test
  void getSummarySection_noConsentLengthDetails() {
    when(consentLengthService.findConsentLengthDetails(applicationVersion))
        .thenReturn(Optional.empty());

    assertThat(ventInformationSummarySectionService.getSummarySection(applicationVersion))
        .isNotPresent();
  }

  @ParameterizedTest
  @EnumSource(value = ConsentLengthType.class, mode = EnumSource.Mode.EXCLUDE, names = {"LONG_TERM"})
  void getSummarySection_shortTermAndAnnual(ConsentLengthType consentLengthType) {
    var expectedSummaryCard = SummaryCard.emptySummaryCard();
    ConsentLengthDetails consentLengthDetails;

    if (ConsentLengthType.SHORT_TERM.equals(consentLengthType)) {
      consentLengthDetails = ConsentLengthTestUtil.getConsentLengthDetailsForShortTerm(applicationVersion);
      when(ventShortTermService.getVentShortTermSummaryCard(applicationVersion))
          .thenReturn(expectedSummaryCard);
    } else {
      consentLengthDetails = ConsentLengthTestUtil.getConsentLengthDetailsForAnnual(applicationVersion);
      when(ventAnnualService.getVentAnnualSummaryCard(applicationVersion))
          .thenReturn(expectedSummaryCard);
    }
    when(consentLengthService.findConsentLengthDetails(applicationVersion))
        .thenReturn(Optional.of(consentLengthDetails));
    when(applicationUnitService.getEmissionCategoryType(applicationVersion))
        .thenReturn(EmissionCategoryType.CATEGORY_ABC);
    when(ventSummaryService.getSummariesForVents(applicationVersion))
        .thenReturn(List.of(expectedSummaryCard));
    when(ventReportService.getVentReportSummaryCards(applicationVersion))
        .thenReturn(List.of(expectedSummaryCard));
    when(ventReportGasDataService.getVentReportGasDataSummaryCards(applicationVersion))
        .thenReturn(List.of(expectedSummaryCard));

    var summarySectionOptional = ventInformationSummarySectionService.getSummarySection(applicationVersion);

    assertThat(summarySectionOptional).isNotEmpty();
    var summarySection = summarySectionOptional.get();
    assertSummarySection(summarySection, VENT_INFORMATION_DISPLAY_ORDER);

    var summaryItems = summarySection.summaryItems();
    assertThat(summaryItems).hasSize(4);

    assertSummaryItem(summaryItems.get(0), VENTS_ITEM, 1);
    assertSummaryItem(summaryItems.get(1), VENT_REPORT_ITEM, 1);
    assertSummaryItem(summaryItems.get(2), VENT_REPORT_GAS_PROPERTIES_ITEM, 1);
    assertSummaryItem(summaryItems.get(3), consentLengthType.getDisplayName(), 1);

    for (SummaryItem summaryItem : summaryItems) {
      assertEmptySummaryCard(summaryItem.summaryCards().get(0));
    }

    if (ConsentLengthType.SHORT_TERM.equals(consentLengthType)) {
      verify(ventShortTermService, times(1)).getVentShortTermSummaryCard(applicationVersion);
      verifyNoInteractions(ventAnnualService);
    } else {
      verify(ventAnnualService, times(1)).getVentAnnualSummaryCard(applicationVersion);
      verifyNoInteractions(ventShortTermService);
    }

    verify(ventReportService, times(1)).getVentReportSummaryCards(applicationVersion);
    verify(ventReportGasDataService, times(1)).getVentReportGasDataSummaryCards(applicationVersion);

    verifyNoInteractions(ventReport123SummaryService);
    verifyNoInteractions(ventReport123GasDataSummaryService);
    verifyNoInteractions(ventAnnual123SummaryService);
    verifyNoInteractions(ventShortTerm123SummaryService);
  }

  @Test
  void getSummarySection_shortTerm_cat123() {
    var expectedSummaryCard = SummaryCard.emptySummaryCard();
    var consentLengthDetails = ConsentLengthTestUtil.getConsentLengthDetailsForShortTerm(applicationVersion);

    when(consentLengthService.findConsentLengthDetails(applicationVersion))
        .thenReturn(Optional.of(consentLengthDetails));
    when(applicationUnitService.getEmissionCategoryType(applicationVersion))
        .thenReturn(EmissionCategoryType.CATEGORY_123);
    when(ventSummaryService.getSummariesForVents(applicationVersion))
        .thenReturn(List.of(expectedSummaryCard));
    when(ventShortTerm123SummaryService.getVentShortTerm123SummaryCard(applicationVersion))
        .thenReturn(expectedSummaryCard);

    var summarySectionOptional = ventInformationSummarySectionService.getSummarySection(applicationVersion);

    assertThat(summarySectionOptional).isNotEmpty();
    var summarySection = summarySectionOptional.get();
    assertSummarySection(summarySection, VENT_INFORMATION_DISPLAY_ORDER);

    var summaryItems = summarySection.summaryItems();
    assertThat(summaryItems).hasSize(2);

    assertSummaryItem(summaryItems.get(0), VENTS_ITEM, 1);
    assertSummaryItem(summaryItems.get(1), ConsentLengthType.SHORT_TERM.getDisplayName(), 1);

    for (SummaryItem summaryItem : summaryItems) {
      assertEmptySummaryCard(summaryItem.summaryCards().get(0));
    }

    verify(ventShortTerm123SummaryService, times(1))
        .getVentShortTerm123SummaryCard(applicationVersion);

    verifyNoInteractions(ventAnnual123SummaryService);
    verifyNoInteractions(ventReport123SummaryService);
    verifyNoInteractions(ventReport123GasDataSummaryService);
    verifyNoABCInteractions();
  }

  @Test
  void getSummarySection_annual_cat123() {
    var expectedSummaryCard = SummaryCard.emptySummaryCard();
    var consentLengthDetails = ConsentLengthTestUtil.getConsentLengthDetailsForAnnual(applicationVersion);

    when(consentLengthService.findConsentLengthDetails(applicationVersion))
        .thenReturn(Optional.of(consentLengthDetails));
    when(applicationUnitService.getEmissionCategoryType(applicationVersion))
        .thenReturn(EmissionCategoryType.CATEGORY_123);
    when(ventSummaryService.getSummariesForVents(applicationVersion))
        .thenReturn(List.of(expectedSummaryCard));
    when(ventReport123SummaryService.getVentReport123SummaryCard(applicationVersion))
        .thenReturn(expectedSummaryCard);
    when(ventReport123GasDataSummaryService.getVentReport123GasDataSummaryCard(applicationVersion))
        .thenReturn(expectedSummaryCard);
    when(ventAnnual123SummaryService.getVentAnnual123SummaryCard(applicationVersion))
        .thenReturn(expectedSummaryCard);

    var summarySectionOptional = ventInformationSummarySectionService.getSummarySection(applicationVersion);

    assertThat(summarySectionOptional).isNotEmpty();
    var summarySection = summarySectionOptional.get();
    assertSummarySection(summarySection, VENT_INFORMATION_DISPLAY_ORDER);

    var summaryItems = summarySection.summaryItems();
    assertThat(summaryItems).hasSize(4);

    assertSummaryItem(summaryItems.get(0), VENTS_ITEM, 1);
    assertSummaryItem(summaryItems.get(1), VENT_REPORT_ITEM, 1);
    assertSummaryItem(summaryItems.get(2), VENT_REPORT_GAS_PROPERTIES_ITEM, 1);
    assertSummaryItem(summaryItems.get(3), ConsentLengthType.ANNUAL.getDisplayName(), 1);

    for (SummaryItem summaryItem : summaryItems) {
      assertEmptySummaryCard(summaryItem.summaryCards().get(0));
    }

    verify(ventReport123SummaryService, times(1))
        .getVentReport123SummaryCard(applicationVersion);
    verify(ventReport123GasDataSummaryService, times(1))
        .getVentReport123GasDataSummaryCard(applicationVersion);
    verify(ventAnnual123SummaryService, times(1))
        .getVentAnnual123SummaryCard(applicationVersion);

    verifyNoInteractions(ventShortTerm123SummaryService);
    verifyNoABCInteractions();
  }

  private void verifyNoABCInteractions() {
    verifyNoInteractions(ventShortTermService);
    verifyNoInteractions(ventAnnualService);
    verifyNoInteractions(ventReportService);
    verifyNoInteractions(ventReportGasDataService);
  }

  @Test
  void getSummarySection_longTerm() {
    when(consentLengthService.findConsentLengthDetails(applicationVersion))
        .thenReturn(Optional.of(ConsentLengthTestUtil.getConsentLengthDetailsForLongTerm(applicationVersion)));

    assertThatThrownBy(() -> ventInformationSummarySectionService.getSummarySection(applicationVersion))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Incorrect consent length type: " + ConsentLengthType.LONG_TERM);
  }
}
