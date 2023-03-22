package uk.co.nstauthority.fieldconsents.application.summary.vent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
  private VentAnnualService ventAnnualService;

  @Mock
  private VentShortTermService ventShortTermService;

  @Mock
  private VentSummaryService ventSummaryService;

  @Mock
  private VentReportService ventReportService;

  @Mock
  private VentReportGasDataService ventReportGasDataService;

  @InjectMocks
  private VentInformationSummarySectionService ventInformationSummarySectionService;

  ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getApplicationVersionWithType(ApplicationType.VENT);
  }

  @ParameterizedTest
  @EnumSource(value = ApplicationType.class, names = {"PRODUCTION", "FLARE"})
  void getSummarySection_nonVent(ApplicationType applicationType) {
    var nonVentAppVersion = ApplicationTestUtil.getApplicationVersionWithType(applicationType);

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