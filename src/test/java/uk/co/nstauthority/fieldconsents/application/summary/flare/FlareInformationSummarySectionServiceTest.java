package uk.co.nstauthority.fieldconsents.application.summary.flare;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
  private FlareAnnualService flareAnnualService;

  @Mock
  private FlareShortTermService flareShortTermService;

  @Mock
  private FlareSummaryService flareSummaryService;

  @Mock
  private FlareReportService flareReportService;

  @Mock
  private FlareReportGasDataService flareReportGasDataService;

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
  void getSummarySection_shortTermAndAnnual(ConsentLengthType consentLengthType) {
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