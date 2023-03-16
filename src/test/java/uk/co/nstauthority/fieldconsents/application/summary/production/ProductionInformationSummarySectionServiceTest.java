package uk.co.nstauthority.fieldconsents.application.summary.production;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.summary.SummaryTestUtil.PRODUCTION_INFORMATION_DISPLAY_ORDER;
import static uk.co.nstauthority.fieldconsents.application.summary.SummaryTestUtil.assertEmptySummaryCard;
import static uk.co.nstauthority.fieldconsents.application.summary.SummaryTestUtil.assertSummaryCard;
import static uk.co.nstauthority.fieldconsents.application.summary.SummaryTestUtil.assertSummaryItem;
import static uk.co.nstauthority.fieldconsents.application.summary.SummaryTestUtil.assertSummarySection;

import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthTestUtil;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.production.ProductionView;
import uk.co.nstauthority.fieldconsents.production.annual.AnnualProductionService;
import uk.co.nstauthority.fieldconsents.production.longterm.LongTermProductionService;
import uk.co.nstauthority.fieldconsents.production.shortterm.ShortTermProductionService;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.summary.SummaryCardType;

@ExtendWith(MockitoExtension.class)
class ProductionInformationSummarySectionServiceTest {

  @Mock
  private ConsentLengthService consentLengthService;

  @Mock
  private ShortTermProductionService shortTermProductionService;

  @Mock
  private AnnualProductionService annualProductionService;

  @Mock
  private LongTermProductionService longTermProductionService;

  @InjectMocks
  private ProductionInformationSummarySectionService productionInformationSummarySectionService;

  ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getApplicationVersionWithType(ApplicationType.PRODUCTION);
  }

  @ParameterizedTest
  @EnumSource(value = ApplicationType.class, names = {"FLARE", "VENT"})
  void getSummarySection_nonProduction(ApplicationType applicationType) {
    var nonProductionAppVersion = ApplicationTestUtil.getApplicationVersionWithType(applicationType);

    assertThat(productionInformationSummarySectionService.getSummarySection(nonProductionAppVersion))
        .isNotPresent();
  }

  @Test
  void getSummarySection_noConsentLengthDetails() {
    when(consentLengthService.findConsentLengthDetails(applicationVersion))
        .thenReturn(Optional.empty());

    assertThat(productionInformationSummarySectionService.getSummarySection(applicationVersion))
        .isNotPresent();
  }

  @ParameterizedTest
  @MethodSource("getSummaryCardsForShortTerm")
  void getSummarySection_shortTerm(SummaryCard summaryCard) {
    when(consentLengthService.findConsentLengthDetails(applicationVersion))
        .thenReturn(Optional.of(ConsentLengthTestUtil.getConsentLengthDetailsForShortTerm(applicationVersion)));
    when(shortTermProductionService.getProductionShortTermSummaryCard(applicationVersion))
        .thenReturn(summaryCard);

    var summarySectionOptional = productionInformationSummarySectionService.getSummarySection(applicationVersion);

    assertThat(summarySectionOptional).isNotEmpty();
    var summarySection = summarySectionOptional.get();
    assertSummarySection(summarySection, PRODUCTION_INFORMATION_DISPLAY_ORDER);

    var summaryItems = summarySection.summaryItems();
    assertThat(summaryItems).hasSize(1);

    assertSummaryItem(summaryItems.get(0), ConsentLengthType.SHORT_TERM.getDisplayName(), 1);
    if (SummaryCardType.EMPTY_SUMMARY.equals(summaryCard.summaryCardType())) {
      assertEmptySummaryCard(summaryItems.get(0).summaryCards().get(0));
    } else {
      assertSummaryCard(summaryItems.get(0).summaryCards().get(0), summaryCard.displayName(),
          summaryCard.summaryCardType(), ProductionView.class);
    }
  }

  private static Stream<Arguments> getSummaryCardsForShortTerm() {
    return Stream.of(
        Arguments.of(SummaryCard.emptySummaryCard()),
        Arguments.of(new SummaryCard("test short term", SummaryCardType.PRODUCTION_SHORT_TERM,
            ProductionView.empty()))
    );
  }

  @ParameterizedTest
  @MethodSource("getSummaryCardsForAnnual")
  void getSummarySection_annual(SummaryCard summaryCard) {
    when(consentLengthService.findConsentLengthDetails(applicationVersion))
        .thenReturn(Optional.of(ConsentLengthTestUtil.getConsentLengthDetailsForAnnual(applicationVersion)));
    when(annualProductionService.getProductionAnnualSummaryCard(applicationVersion))
        .thenReturn(summaryCard);

    var summarySectionOptional = productionInformationSummarySectionService.getSummarySection(applicationVersion);

    assertThat(summarySectionOptional).isNotEmpty();
    var summarySection = summarySectionOptional.get();
    assertSummarySection(summarySection, PRODUCTION_INFORMATION_DISPLAY_ORDER);

    var summaryItems = summarySection.summaryItems();
    assertThat(summaryItems).hasSize(1);
    assertSummaryItem(summaryItems.get(0), ConsentLengthType.ANNUAL.getDisplayName(), 1);
    if (SummaryCardType.EMPTY_SUMMARY.equals(summaryCard.summaryCardType())) {
      assertEmptySummaryCard(summaryItems.get(0).summaryCards().get(0));
    } else {
      assertSummaryCard(summaryItems.get(0).summaryCards().get(0), summaryCard.displayName(),
          summaryCard.summaryCardType(), ProductionView.class);
    }
  }

  private static Stream<Arguments> getSummaryCardsForAnnual() {
    return Stream.of(
        Arguments.of(SummaryCard.emptySummaryCard()),
        Arguments.of(new SummaryCard("test annual", SummaryCardType.PRODUCTION_ANNUAL,
            ProductionView.empty()))
    );
  }

  @ParameterizedTest
  @MethodSource("getSummaryCardsForLongTerm")
  void getSummarySection_longTerm(SummaryCard summaryCard) {
    when(consentLengthService.findConsentLengthDetails(applicationVersion))
        .thenReturn(Optional.of(ConsentLengthTestUtil.getConsentLengthDetailsForLongTerm(applicationVersion)));
    when(longTermProductionService.getProductionLongTermSummaryCard(applicationVersion))
        .thenReturn(summaryCard);

    var summarySectionOptional = productionInformationSummarySectionService.getSummarySection(applicationVersion);

    assertThat(summarySectionOptional).isNotEmpty();
    var summarySection = summarySectionOptional.get();
    assertSummarySection(summarySection, PRODUCTION_INFORMATION_DISPLAY_ORDER);

    var summaryItems = summarySection.summaryItems();
    assertThat(summaryItems).hasSize(1);
    assertSummaryItem(summaryItems.get(0), ConsentLengthType.LONG_TERM.getDisplayName(), 1);
    if (SummaryCardType.EMPTY_SUMMARY.equals(summaryCard.summaryCardType())) {
      assertEmptySummaryCard(summaryItems.get(0).summaryCards().get(0));
    } else {
      assertSummaryCard(summaryItems.get(0).summaryCards().get(0), summaryCard.displayName(),
          summaryCard.summaryCardType(), ProductionView.class);
    }
  }

  private static Stream<Arguments> getSummaryCardsForLongTerm() {
    return Stream.of(
        Arguments.of(SummaryCard.emptySummaryCard()),
        Arguments.of(new SummaryCard("test long term", SummaryCardType.PRODUCTION_LONG_TERM,
            ProductionView.empty()))
    );
  }
}