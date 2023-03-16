package uk.co.nstauthority.fieldconsents.application.summary.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetTestUtil.fieldAsset1;
import static uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetTestUtil.terminalAsset1;
import static uk.co.nstauthority.fieldconsents.application.summary.SummaryTestUtil.CONSENT_DETAILS_DISPLAY_ORDER;
import static uk.co.nstauthority.fieldconsents.application.summary.SummaryTestUtil.assertEmptySummaryGroup;
import static uk.co.nstauthority.fieldconsents.application.summary.SummaryTestUtil.assertSummaryGroup;
import static uk.co.nstauthority.fieldconsents.application.summary.SummaryTestUtil.assertSummaryItem;
import static uk.co.nstauthority.fieldconsents.application.summary.SummaryTestUtil.assertSummarySection;
import static uk.co.nstauthority.fieldconsents.application.summary.SummaryTestUtil.simpleSummaryGroup;
import static uk.co.nstauthority.fieldconsents.summary.SummaryGroupType.SIMPLE_SUMMARY;

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
import uk.co.nstauthority.fieldconsents.summary.SummaryDataView;
import uk.co.nstauthority.fieldconsents.summary.SummaryGroup;
import uk.co.nstauthority.fieldconsents.summary.SummaryGroupType;

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
  @MethodSource("getSummaryGroups")
  void getSummarySection_production(SummaryGroup summaryGroup) {
    var applicationVersion = ApplicationTestUtil.getApplicationVersionWithType(ApplicationType.PRODUCTION);
    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(fieldAsset1);
    when(applicationContextService.getApplicationContextSummaryGroup(applicationVersion))
        .thenReturn(summaryGroup);
    when(consentLengthService.getConsentLengthSummaryGroup(applicationVersion))
        .thenReturn(summaryGroup);
    when(gasInjectionService.getGasInjectionSummaryGroup(applicationVersion))
          .thenReturn(summaryGroup);

    var summarySectionOptional = consentDetailsSummarySectionService.getSummarySection(applicationVersion);

    assertThat(summarySectionOptional).isNotEmpty();
    var summarySection = summarySectionOptional.get();
    assertSummarySection(summarySection, CONSENT_DETAILS_DISPLAY_ORDER);
    var summaryItems = summarySection.summaryItems();

    assertThat(summaryItems).hasSize(3);

    assertSummaryItem(summaryItems.get(0), APPLICATION_DETAILS_ITEM, 1);
    assertSummaryItem(summaryItems.get(1), CONSENT_DURATION_ITEM, 1);
    assertSummaryItem(summaryItems.get(2), GAS_INJECTION_ITEM, 1);

    if (SummaryGroupType.EMPTY_SUMMARY.equals(summaryGroup.summaryGroupType())) {
      assertEmptySummaryGroup(summaryItems.get(0).summaryGroups().get(0));
      assertEmptySummaryGroup(summaryItems.get(1).summaryGroups().get(0));
      assertEmptySummaryGroup(summaryItems.get(2).summaryGroups().get(0));
    } else {
      assertSummaryGroup(summaryItems.get(0).summaryGroups().get(0), null, SIMPLE_SUMMARY, SummaryDataView.class);
      assertSummaryGroup(summaryItems.get(1).summaryGroups().get(0), null, SIMPLE_SUMMARY, SummaryDataView.class);
      assertSummaryGroup(summaryItems.get(2).summaryGroups().get(0), null, SIMPLE_SUMMARY, SummaryDataView.class);
    }
  }

  private static Stream<Arguments> getSummaryGroups() {
    return Stream.of(
        Arguments.of(SummaryGroup.emptySummaryGroup()),
        Arguments.of(simpleSummaryGroup)
    );
  }

  @ParameterizedTest
  @MethodSource("getApplicationTypeAssetAndSummaryGroup")
  void getSummarySection_flareVentFieldTerminal(ApplicationType applicationType,
                                                ApplicationAsset applicationAsset,
                                                SummaryGroup summaryGroup) {
    var applicationVersion = ApplicationTestUtil.getApplicationVersionWithType(applicationType);
    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(applicationAsset);
    when(applicationContextService.getApplicationContextSummaryGroup(applicationVersion))
        .thenReturn(summaryGroup);
    when(consentLengthService.getConsentLengthSummaryGroup(applicationVersion))
        .thenReturn(summaryGroup);
    if (applicationAsset.isField()) {
      when(assetSummaryService.getAdditionalAssetsSummaryGroups(applicationVersion))
          .thenReturn(List.of(summaryGroup));
    }

    var summarySectionOptional = consentDetailsSummarySectionService.getSummarySection(applicationVersion);

    assertThat(summarySectionOptional).isNotEmpty();
    var summarySection = summarySectionOptional.get();
    assertSummarySection(summarySection, CONSENT_DETAILS_DISPLAY_ORDER);
    var summaryItems = summarySection.summaryItems();

    assertSummaryItem(summaryItems.get(0), APPLICATION_DETAILS_ITEM, 1);
    assertSummaryItem(summaryItems.get(1), CONSENT_DURATION_ITEM, 1);

    if (SummaryGroupType.EMPTY_SUMMARY.equals(summaryGroup.summaryGroupType())) {
      assertEmptySummaryGroup(summaryItems.get(0).summaryGroups().get(0));
      assertEmptySummaryGroup(summaryItems.get(1).summaryGroups().get(0));
    } else {
      assertSummaryGroup(summaryItems.get(0).summaryGroups().get(0), null, SIMPLE_SUMMARY, SummaryDataView.class);
      assertSummaryGroup(summaryItems.get(1).summaryGroups().get(0), null, SIMPLE_SUMMARY, SummaryDataView.class);
    }


    if (applicationAsset.isField()) {
      assertThat(summaryItems).hasSize(3);
      assertSummaryItem(summaryItems.get(2), ADDITIONAL_ASSETS_ITEM, 1);
      if (SummaryGroupType.EMPTY_SUMMARY.equals(summaryGroup.summaryGroupType())) {
        assertEmptySummaryGroup(summaryItems.get(2).summaryGroups().get(0));
      } else {
        assertSummaryGroup(summaryItems.get(2).summaryGroups().get(0), null, SIMPLE_SUMMARY, SummaryDataView.class);
      }
    } else {
      assertThat(summaryItems).hasSize(2);
    }
  }

  private static Stream<Arguments> getApplicationTypeAssetAndSummaryGroup() {
    return Stream.of(
        Arguments.of(ApplicationType.FLARE, fieldAsset1, SummaryGroup.emptySummaryGroup()),
        Arguments.of(ApplicationType.VENT, fieldAsset1, SummaryGroup.emptySummaryGroup()),
        Arguments.of(ApplicationType.FLARE, terminalAsset1, SummaryGroup.emptySummaryGroup()),
        Arguments.of(ApplicationType.VENT, terminalAsset1, SummaryGroup.emptySummaryGroup()),
        Arguments.of(ApplicationType.FLARE, fieldAsset1, simpleSummaryGroup),
        Arguments.of(ApplicationType.VENT, fieldAsset1, simpleSummaryGroup),
        Arguments.of(ApplicationType.FLARE, terminalAsset1, simpleSummaryGroup),
        Arguments.of(ApplicationType.VENT, terminalAsset1, simpleSummaryGroup)
    );
  }
}