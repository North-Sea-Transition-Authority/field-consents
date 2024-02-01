package uk.co.nstauthority.fieldconsents.application.summary.flare;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.summary.SummaryTestUtil.FLARE_INFORMATION_DISPLAY_ORDER;
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
import uk.co.nstauthority.fieldconsents.flarevent.category123.flare.annual.FlareAnnual123SummaryService;
import uk.co.nstauthority.fieldconsents.flarevent.category123.flare.flarereport.FlareReport123SummaryService;
import uk.co.nstauthority.fieldconsents.flarevent.category123.flare.flarereportgas.FlareReport123GasDataSummaryService;
import uk.co.nstauthority.fieldconsents.flarevent.category123.flare.shortterm.FlareShortTerm123SummaryService;
import uk.co.nstauthority.fieldconsents.flarevent.flare.annual.FlareAnnualService;
import uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport.FlareReportService;
import uk.co.nstauthority.fieldconsents.flarevent.flare.flarereportgas.FlareReportGasDataService;
import uk.co.nstauthority.fieldconsents.flarevent.flare.flares.FlareSummaryService;
import uk.co.nstauthority.fieldconsents.flarevent.flare.shortterm.FlareShortTermService;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.summary.SummaryItem;

@ExtendWith(MockitoExtension.class)
class FlareInformationSummarySectionServiceTest {

  private static final String FLARES_ITEM = "Flares";

  private static final String FLARE_REPORT_ITEM = "Flare report";

  private static final String FLARE_REPORT_GAS_PROPERTIES_ITEM = "Flare report gas properties";

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
  private FlareReportService flareReportService;

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

  @InjectMocks
  private FlareInformationSummarySectionService flareInformationSummarySectionService;

  ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.FLARE);
  }

  @ParameterizedTest
  @EnumSource(value = ApplicationType.class, names = {"PRODUCTION", "VENT"})
  void getSummarySection_nonFlare(ApplicationType applicationType) {
    var nonFlareAppVersion = ApplicationTestUtil.getNewApplicationVersionWithType(applicationType);

    assertThat(flareInformationSummarySectionService.getSummarySection(nonFlareAppVersion))
        .isNotPresent();

    verifyNoInteractions(consentLengthService);
  }

  @Test
  void getSummarySection_noConsentLengthDetails() {
    when(consentLengthService.findConsentLengthDetails(applicationVersion))
        .thenReturn(Optional.empty());

    assertThat(flareInformationSummarySectionService.getSummarySection(applicationVersion))
        .isNotPresent();
  }

  @ParameterizedTest
  @EnumSource(value = ConsentLengthType.class, mode = EnumSource.Mode.EXCLUDE, names = {"LONG_TERM"})
  void getSummarySection_shortTermAndAnnual_catABC(ConsentLengthType consentLengthType) {
    var expectedSummaryCard = SummaryCard.emptySummaryCard();
    ConsentLengthDetails consentLengthDetails;

    if (ConsentLengthType.SHORT_TERM.equals(consentLengthType)) {
      consentLengthDetails = ConsentLengthTestUtil.getConsentLengthDetailsForShortTerm(applicationVersion);
      when(flareShortTermService.getFlareShortTermSummaryCard(applicationVersion))
          .thenReturn(expectedSummaryCard);
    } else {
      consentLengthDetails = ConsentLengthTestUtil.getConsentLengthDetailsForAnnual(applicationVersion);
      when(flareAnnualService.getFlareAnnualSummaryCard(applicationVersion))
          .thenReturn(expectedSummaryCard);
    }
    when(consentLengthService.findConsentLengthDetails(applicationVersion))
        .thenReturn(Optional.of(consentLengthDetails));
    when(applicationUnitService.getEmissionCategoryType(applicationVersion))
        .thenReturn(EmissionCategoryType.CATEGORY_ABC);
    when(flareSummaryService.getSummariesForFlares(applicationVersion))
        .thenReturn(List.of(expectedSummaryCard));
    when(flareReportService.getFlareReportSummaryCards(applicationVersion))
        .thenReturn(List.of(expectedSummaryCard));
    when(flareReportGasDataService.getFlareReportGasDataSummaryCards(applicationVersion))
        .thenReturn(List.of(expectedSummaryCard));

    var summarySectionOptional = flareInformationSummarySectionService.getSummarySection(applicationVersion);

    assertThat(summarySectionOptional).isNotEmpty();
    var summarySection = summarySectionOptional.get();
    assertSummarySection(summarySection, FLARE_INFORMATION_DISPLAY_ORDER);

    var summaryItems = summarySection.summaryItems();
    assertThat(summaryItems).hasSize(4);

    assertSummaryItem(summaryItems.get(0), FLARES_ITEM, 1);
    assertSummaryItem(summaryItems.get(1), FLARE_REPORT_ITEM, 1);
    assertSummaryItem(summaryItems.get(2), FLARE_REPORT_GAS_PROPERTIES_ITEM, 1);
    assertSummaryItem(summaryItems.get(3), consentLengthType.getDisplayName(), 1);

    for (SummaryItem summaryItem : summaryItems) {
      assertEmptySummaryCard(summaryItem.summaryCards().get(0));
    }

    if (ConsentLengthType.SHORT_TERM.equals(consentLengthType)) {
      verify(flareShortTermService, times(1)).getFlareShortTermSummaryCard(applicationVersion);
      verifyNoInteractions(flareAnnualService);
    } else {
      verify(flareAnnualService, times(1)).getFlareAnnualSummaryCard(applicationVersion);
      verifyNoInteractions(flareShortTermService);
    }

    verify(flareReportService, times(1)).getFlareReportSummaryCards(applicationVersion);
    verify(flareReportGasDataService, times(1)).getFlareReportGasDataSummaryCards(applicationVersion);

    verifyNoInteractions(flareReport123SummaryService);
    verifyNoInteractions(flareReport123GasDataSummaryService);
    verifyNoInteractions(flareAnnual123SummaryService);
    verifyNoInteractions(flareShortTerm123SummaryService);
  }

  @Test
  void getSummarySection_shortTerm_cat123() {
    var expectedSummaryCard = SummaryCard.emptySummaryCard();
    var consentLengthDetails = ConsentLengthTestUtil.getConsentLengthDetailsForShortTerm(applicationVersion);

    when(consentLengthService.findConsentLengthDetails(applicationVersion))
        .thenReturn(Optional.of(consentLengthDetails));
    when(applicationUnitService.getEmissionCategoryType(applicationVersion))
        .thenReturn(EmissionCategoryType.CATEGORY_123);
    when(flareSummaryService.getSummariesForFlares(applicationVersion))
        .thenReturn(List.of(expectedSummaryCard));
    when(flareShortTerm123SummaryService.getFlareShortTerm123SummaryCard(applicationVersion))
        .thenReturn(expectedSummaryCard);

    var summarySectionOptional = flareInformationSummarySectionService.getSummarySection(applicationVersion);

    assertThat(summarySectionOptional).isNotEmpty();
    var summarySection = summarySectionOptional.get();
    assertSummarySection(summarySection, FLARE_INFORMATION_DISPLAY_ORDER);

    var summaryItems = summarySection.summaryItems();
    assertThat(summaryItems).hasSize(2);

    assertSummaryItem(summaryItems.get(0), FLARES_ITEM, 1);
    assertSummaryItem(summaryItems.get(1), ConsentLengthType.SHORT_TERM.getDisplayName(), 1);

    for (SummaryItem summaryItem : summaryItems) {
      assertEmptySummaryCard(summaryItem.summaryCards().get(0));
    }

    verify(flareShortTerm123SummaryService, times(1))
        .getFlareShortTerm123SummaryCard(applicationVersion);

    verifyNoInteractions(flareAnnual123SummaryService);
    verifyNoInteractions(flareReport123SummaryService);
    verifyNoInteractions(flareReport123GasDataSummaryService);
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
    when(flareSummaryService.getSummariesForFlares(applicationVersion))
        .thenReturn(List.of(expectedSummaryCard));
    when(flareReport123SummaryService.getFlareReport123SummaryCard(applicationVersion))
        .thenReturn(expectedSummaryCard);
    when(flareReport123GasDataSummaryService.getFlareReport123GasDataSummaryCard(applicationVersion))
        .thenReturn(expectedSummaryCard);
    when(flareAnnual123SummaryService.getFlareAnnual123SummaryCard(applicationVersion))
        .thenReturn(expectedSummaryCard);

    var summarySectionOptional = flareInformationSummarySectionService.getSummarySection(applicationVersion);

    assertThat(summarySectionOptional).isNotEmpty();
    var summarySection = summarySectionOptional.get();
    assertSummarySection(summarySection, FLARE_INFORMATION_DISPLAY_ORDER);

    var summaryItems = summarySection.summaryItems();
    assertThat(summaryItems).hasSize(4);

    assertSummaryItem(summaryItems.get(0), FLARES_ITEM, 1);
    assertSummaryItem(summaryItems.get(1), FLARE_REPORT_ITEM, 1);
    assertSummaryItem(summaryItems.get(2), FLARE_REPORT_GAS_PROPERTIES_ITEM, 1);
    assertSummaryItem(summaryItems.get(3), ConsentLengthType.ANNUAL.getDisplayName(), 1);

    for (SummaryItem summaryItem : summaryItems) {
      assertEmptySummaryCard(summaryItem.summaryCards().get(0));
    }

    verify(flareReport123SummaryService, times(1))
        .getFlareReport123SummaryCard(applicationVersion);
    verify(flareReport123GasDataSummaryService, times(1))
        .getFlareReport123GasDataSummaryCard(applicationVersion);
    verify(flareAnnual123SummaryService, times(1))
        .getFlareAnnual123SummaryCard(applicationVersion);

    verifyNoInteractions(flareShortTerm123SummaryService);
    verifyNoABCInteractions();
  }

  private void verifyNoABCInteractions() {
    verifyNoInteractions(flareShortTermService);
    verifyNoInteractions(flareAnnualService);
    verifyNoInteractions(flareReportService);
    verifyNoInteractions(flareReportGasDataService);
  }

  @Test
  void getSummarySection_longTerm() {
    when(consentLengthService.findConsentLengthDetails(applicationVersion))
        .thenReturn(Optional.of(ConsentLengthTestUtil.getConsentLengthDetailsForLongTerm(applicationVersion)));

    assertThatThrownBy(() -> flareInformationSummarySectionService.getSummarySection(applicationVersion))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Incorrect consent length type: " + ConsentLengthType.LONG_TERM);
  }
}
