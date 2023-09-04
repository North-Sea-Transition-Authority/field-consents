package uk.co.nstauthority.fieldconsents.application.summary.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationContextService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAsset;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.assets.AssetSummaryService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.application.rationale.flare.ApplicationRationaleFlareService;
import uk.co.nstauthority.fieldconsents.application.rationale.vent.ApplicationRationaleVentService;
import uk.co.nstauthority.fieldconsents.production.gasinjection.GasInjectionService;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.summary.SummaryItem;
import uk.co.nstauthority.fieldconsents.summary.SummarySection;

@ExtendWith(MockitoExtension.class)
class ConsentDetailsSummarySectionServiceTest {

  private static final SummaryCard SUMMARY_CARD = SummaryCard.emptySummaryCard();
  private static final SummaryItem SUMMARY_ITEM = SummaryItem.withCard("example item", SUMMARY_CARD);

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

  @Mock
  private ApplicationRationaleFlareService applicationRationaleFlareService;

  @Mock
  private ApplicationRationaleVentService applicationRationaleVentService;

  @Spy
  @InjectMocks
  private ConsentDetailsSummarySectionService consentDetailsSummarySectionService;

  @ParameterizedTest
  @EnumSource(ApplicationType.class)
  void getSummarySection_allEmpty(ApplicationType applicationType) {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(applicationType);

    doReturn(Optional.empty()).when(consentDetailsSummarySectionService).getApplicationContextSummaryItem(applicationVersion);
    doReturn(Optional.empty()).when(consentDetailsSummarySectionService).getApplicationRationaleSummaryItem(applicationVersion);
    doReturn(Optional.empty()).when(consentDetailsSummarySectionService).getConsentDurationSummaryItem(applicationVersion);
    doReturn(Optional.empty()).when(consentDetailsSummarySectionService).getAdditionalAssetsSummaryItem(applicationVersion);
    doReturn(Optional.empty()).when(consentDetailsSummarySectionService).getGasInjectionSummaryItem(applicationVersion);

    assertThat(consentDetailsSummarySectionService.getSummarySection(applicationVersion)).isEmpty();
  }

  @ParameterizedTest
  @EnumSource(ApplicationType.class)
  void getSummarySection(ApplicationType applicationType) {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(applicationType);

    doReturn(Optional.of(SUMMARY_ITEM)).when(consentDetailsSummarySectionService).getApplicationContextSummaryItem(applicationVersion);
    doReturn(Optional.of(SUMMARY_ITEM)).when(consentDetailsSummarySectionService).getApplicationRationaleSummaryItem(applicationVersion);
    doReturn(Optional.of(SUMMARY_ITEM)).when(consentDetailsSummarySectionService).getConsentDurationSummaryItem(applicationVersion);
    doReturn(Optional.of(SUMMARY_ITEM)).when(consentDetailsSummarySectionService).getAdditionalAssetsSummaryItem(applicationVersion);
    doReturn(Optional.of(SUMMARY_ITEM)).when(consentDetailsSummarySectionService).getGasInjectionSummaryItem(applicationVersion);

    assertThat(consentDetailsSummarySectionService.getSummarySection(applicationVersion))
        .isPresent()
        .get()
        .isEqualTo(new SummarySection(10, List.of(
            SUMMARY_ITEM,
            SUMMARY_ITEM,
            SUMMARY_ITEM,
            SUMMARY_ITEM,
            SUMMARY_ITEM
        )));
  }

  @ParameterizedTest
  @EnumSource(ApplicationType.class)
  void getApplicationContextSummaryItem(ApplicationType applicationType) {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(applicationType);

    when(applicationContextService.getApplicationContextSummaryCard(applicationVersion)).thenReturn(SUMMARY_CARD);

    assertThat(consentDetailsSummarySectionService.getApplicationContextSummaryItem(applicationVersion))
        .isPresent()
        .get()
        .isEqualTo(SummaryItem.withCard("Application details", SUMMARY_CARD));
  }

  @Test
  void getApplicationRationaleSummaryItem_flare() {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.FLARE);

    when(applicationRationaleFlareService.getSummaryCard(applicationVersion)).thenReturn(SUMMARY_CARD);

    assertThat(consentDetailsSummarySectionService.getApplicationRationaleSummaryItem(applicationVersion))
        .isPresent()
        .get()
        .isEqualTo(SummaryItem.withCard("Application rationale", SUMMARY_CARD));
  }

  @Test
  void getApplicationRationaleSummaryItem_vent() {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.VENT);

    when(applicationRationaleVentService.getSummaryCard(applicationVersion)).thenReturn(SUMMARY_CARD);

    assertThat(consentDetailsSummarySectionService.getApplicationRationaleSummaryItem(applicationVersion))
        .isPresent()
        .get()
        .isEqualTo(SummaryItem.withCard("Application rationale", SUMMARY_CARD));
  }

  @ParameterizedTest
  @EnumSource(ApplicationType.class)
  void getConsentDurationSummaryItem(ApplicationType applicationType) {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(applicationType);

    when(consentLengthService.getConsentLengthSummaryCard(applicationVersion)).thenReturn(SUMMARY_CARD);

    assertThat(consentDetailsSummarySectionService.getConsentDurationSummaryItem(applicationVersion))
        .isPresent()
        .get()
        .isEqualTo(SummaryItem.withCard("Consent duration", SUMMARY_CARD));
  }

  @Test
  void getAdditionalAssetsSummaryItem_production() {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
    assertThat(consentDetailsSummarySectionService.getAdditionalAssetsSummaryItem(applicationVersion)).isEmpty();
  }

  @ParameterizedTest
  @EnumSource(value = ApplicationType.class, names = "PRODUCTION", mode = EnumSource.Mode.EXCLUDE)
  void getAdditionalAssetsSummaryItem_nonProduction_nonField(ApplicationType applicationType) {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(applicationType);

    var applicationAsset = new ApplicationAsset();
    applicationAsset.setTerminalId(1); // this makes it a 'terminal' application asset

    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(applicationAsset);

    assertThat(consentDetailsSummarySectionService.getAdditionalAssetsSummaryItem(applicationVersion)).isEmpty();
  }

  @ParameterizedTest
  @EnumSource(value = ApplicationType.class, names = "PRODUCTION", mode = EnumSource.Mode.EXCLUDE)
  void getAdditionalAssetsSummaryItem_nonProduction_field(ApplicationType applicationType) {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(applicationType);

    var applicationAsset = new ApplicationAsset();
    applicationAsset.setFieldId(1); // this makes it a 'field' application asset

    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(applicationAsset);
    when(assetSummaryService.getAdditionalAssetsSummaryCards(applicationVersion)).thenReturn(
        Collections.singletonList(SUMMARY_CARD));

    assertThat(consentDetailsSummarySectionService.getAdditionalAssetsSummaryItem(applicationVersion))
        .isPresent()
        .get()
        .isEqualTo(SummaryItem.withCard("Additional fields and licences", SUMMARY_CARD));
  }

  @ParameterizedTest
  @EnumSource(value = ApplicationType.class, names = "PRODUCTION", mode = EnumSource.Mode.EXCLUDE)
  void getGasInjectionSummaryItem_nonProduction(ApplicationType applicationType) {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(applicationType);
    assertThat(consentDetailsSummarySectionService.getGasInjectionSummaryItem(applicationVersion)).isEmpty();
  }

  @Test
  void getGasInjectionSummaryItem_production() {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);

    when(gasInjectionService.getGasInjectionSummaryCard(applicationVersion)).thenReturn(SUMMARY_CARD);

    assertThat(consentDetailsSummarySectionService.getGasInjectionSummaryItem(applicationVersion))
        .isPresent()
        .get()
        .isEqualTo(SummaryItem.withCard("Gas injection", SUMMARY_CARD));
  }
}
