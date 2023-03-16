package uk.co.nstauthority.fieldconsents.application.summary.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetTestUtil.fieldAsset1;
import static uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetTestUtil.terminalAsset1;
import static uk.co.nstauthority.fieldconsents.application.summary.SummaryTestUtil.CONSENT_DETAILS_DISPLAY_ORDER;
import static uk.co.nstauthority.fieldconsents.application.summary.SummaryTestUtil.assertEmptySummaryCard;
import static uk.co.nstauthority.fieldconsents.application.summary.SummaryTestUtil.assertSummaryCard;
import static uk.co.nstauthority.fieldconsents.application.summary.SummaryTestUtil.assertSummaryItem;
import static uk.co.nstauthority.fieldconsents.application.summary.SummaryTestUtil.assertSummarySection;
import static uk.co.nstauthority.fieldconsents.application.summary.SummaryTestUtil.simpleSummaryCard;
import static uk.co.nstauthority.fieldconsents.summary.SummaryCardType.SIMPLE_SUMMARY;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationContextService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAsset;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.assets.AssetSummaryService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.production.gasinjection.GasInjectionService;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.summary.SummaryCardType;
import uk.co.nstauthority.fieldconsents.summary.SummaryDataView;

@ExtendWith(MockitoExtension.class)
class ConsentDetailsSummarySectionServiceTest {

  private static final String APPLICATION_DETAILS_ITEM = "Application details";

  private static final String CONSENT_DURATION_ITEM = "Consent duration";

  private static final String ADDITIONAL_ASSETS_ITEM = "Additional fields and licences";

  private static final String GAS_INJECTION_ITEM = "Gas injection";

  @Mock
  private ApplicationContextService applicationContextService;

  @Mock
  private ConsentLengthService consentLengthService;

  @Mock
  private GasInjectionService gasInjectionService;

  @Mock
  private ApplicationAssetService applicationAssetService;

  @Mock
  private AssetSummaryService assetSummaryService;

  @InjectMocks
  private ConsentDetailsSummarySectionService consentDetailsSummarySectionService;

  @ParameterizedTest
  @MethodSource("getSummaryCards")
  void getSummarySection_production(SummaryCard summaryCard) {
    var applicationVersion = ApplicationTestUtil.getApplicationVersionWithType(ApplicationType.PRODUCTION);
    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(fieldAsset1);
    when(applicationContextService.getApplicationContextSummaryCard(applicationVersion))
        .thenReturn(summaryCard);
    when(consentLengthService.getConsentLengthSummaryCard(applicationVersion))
        .thenReturn(summaryCard);
    when(gasInjectionService.getGasInjectionSummaryCard(applicationVersion))
          .thenReturn(summaryCard);

    var summarySectionOptional = consentDetailsSummarySectionService.getSummarySection(applicationVersion);

    assertThat(summarySectionOptional).isNotEmpty();
    var summarySection = summarySectionOptional.get();
    assertSummarySection(summarySection, CONSENT_DETAILS_DISPLAY_ORDER);
    var summaryItems = summarySection.summaryItems();

    assertThat(summaryItems).hasSize(3);

    assertSummaryItem(summaryItems.get(0), APPLICATION_DETAILS_ITEM, 1);
    assertSummaryItem(summaryItems.get(1), CONSENT_DURATION_ITEM, 1);
    assertSummaryItem(summaryItems.get(2), GAS_INJECTION_ITEM, 1);

    if (SummaryCardType.EMPTY_SUMMARY.equals(summaryCard.summaryCardType())) {
      assertEmptySummaryCard(summaryItems.get(0).summaryCards().get(0));
      assertEmptySummaryCard(summaryItems.get(1).summaryCards().get(0));
      assertEmptySummaryCard(summaryItems.get(2).summaryCards().get(0));
    } else {
      assertSummaryCard(summaryItems.get(0).summaryCards().get(0), null, SIMPLE_SUMMARY, SummaryDataView.class);
      assertSummaryCard(summaryItems.get(1).summaryCards().get(0), null, SIMPLE_SUMMARY, SummaryDataView.class);
      assertSummaryCard(summaryItems.get(2).summaryCards().get(0), null, SIMPLE_SUMMARY, SummaryDataView.class);
    }
  }

  private static Stream<Arguments> getSummaryCards() {
    return Stream.of(
        Arguments.of(SummaryCard.emptySummaryCard()),
        Arguments.of(simpleSummaryCard)
    );
  }

  @ParameterizedTest
  @MethodSource("getApplicationTypeAssetAndSummaryCard")
  void getSummarySection_flareVentFieldTerminal(ApplicationType applicationType,
                                                ApplicationAsset applicationAsset,
                                                SummaryCard summaryCard) {
    var applicationVersion = ApplicationTestUtil.getApplicationVersionWithType(applicationType);
    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(applicationAsset);
    when(applicationContextService.getApplicationContextSummaryCard(applicationVersion))
        .thenReturn(summaryCard);
    when(consentLengthService.getConsentLengthSummaryCard(applicationVersion))
        .thenReturn(summaryCard);
    if (applicationAsset.isField()) {
      when(assetSummaryService.getAdditionalAssetsSummaryCards(applicationVersion))
          .thenReturn(List.of(summaryCard));
    }

    var summarySectionOptional = consentDetailsSummarySectionService.getSummarySection(applicationVersion);

    assertThat(summarySectionOptional).isNotEmpty();
    var summarySection = summarySectionOptional.get();
    assertSummarySection(summarySection, CONSENT_DETAILS_DISPLAY_ORDER);
    var summaryItems = summarySection.summaryItems();

    assertSummaryItem(summaryItems.get(0), APPLICATION_DETAILS_ITEM, 1);
    assertSummaryItem(summaryItems.get(1), CONSENT_DURATION_ITEM, 1);

    if (SummaryCardType.EMPTY_SUMMARY.equals(summaryCard.summaryCardType())) {
      assertEmptySummaryCard(summaryItems.get(0).summaryCards().get(0));
      assertEmptySummaryCard(summaryItems.get(1).summaryCards().get(0));
    } else {
      assertSummaryCard(summaryItems.get(0).summaryCards().get(0), null, SIMPLE_SUMMARY, SummaryDataView.class);
      assertSummaryCard(summaryItems.get(1).summaryCards().get(0), null, SIMPLE_SUMMARY, SummaryDataView.class);
    }


    if (applicationAsset.isField()) {
      assertThat(summaryItems).hasSize(3);
      assertSummaryItem(summaryItems.get(2), ADDITIONAL_ASSETS_ITEM, 1);
      if (SummaryCardType.EMPTY_SUMMARY.equals(summaryCard.summaryCardType())) {
        assertEmptySummaryCard(summaryItems.get(2).summaryCards().get(0));
      } else {
        assertSummaryCard(summaryItems.get(2).summaryCards().get(0), null, SIMPLE_SUMMARY, SummaryDataView.class);
      }
    } else {
      assertThat(summaryItems).hasSize(2);
    }
  }

  private static Stream<Arguments> getApplicationTypeAssetAndSummaryCard() {
    return Stream.of(
        Arguments.of(ApplicationType.FLARE, fieldAsset1, SummaryCard.emptySummaryCard()),
        Arguments.of(ApplicationType.VENT, fieldAsset1, SummaryCard.emptySummaryCard()),
        Arguments.of(ApplicationType.FLARE, terminalAsset1, SummaryCard.emptySummaryCard()),
        Arguments.of(ApplicationType.VENT, terminalAsset1, SummaryCard.emptySummaryCard()),
        Arguments.of(ApplicationType.FLARE, fieldAsset1, simpleSummaryCard),
        Arguments.of(ApplicationType.VENT, fieldAsset1, simpleSummaryCard),
        Arguments.of(ApplicationType.FLARE, terminalAsset1, simpleSummaryCard),
        Arguments.of(ApplicationType.VENT, terminalAsset1, simpleSummaryCard)
    );
  }
}