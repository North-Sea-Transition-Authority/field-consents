package uk.co.nstauthority.fieldconsents.application.assets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetTestUtil.assetView2;
import static uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetTestUtil.assetView3;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field2Json;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.assetlicences.ApplicationAssetLicenceService;
import uk.co.nstauthority.fieldconsents.application.flags.ApplicationFlagService;
import uk.co.nstauthority.fieldconsents.application.flags.ApplicationFlagType;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitService;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.summary.SummaryDataView;
import uk.co.nstauthority.fieldconsents.summary.SummaryKeyValue;
import uk.co.nstauthority.fieldconsents.util.BooleanUtil;

@ExtendWith(MockitoExtension.class)
class AssetSummaryServiceTest {

  @Mock
  private ApplicationAssetService applicationAssetService;

  @Mock
  private ApplicationAssetLicenceService applicationAssetLicenceService;

  @Mock
  private OrganisationUnitService organisationUnitService;

  @Mock
  private ApplicationFlagService applicationFlagService;
  
  private AssetSummaryService assetSummaryService;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    assetSummaryService = new AssetSummaryService(applicationAssetService, applicationAssetLicenceService,
        organisationUnitService, applicationFlagService);
    applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.VENT);
  }

  @Test
  void getSummaryViews_noAssets() {
    when(applicationAssetService.getSecondaryAssets(applicationVersion)).thenReturn(Collections.emptyList());

    var AssetViews = assetSummaryService.getSummaryViews(applicationVersion);

    assertThat(AssetViews).isEmpty();
  }

  @Test
  void getSummaryViews_manyAssets() {
    var assets = ApplicationAssetTestUtil.secondaryAssets;
    var assetLicencesMap = ApplicationAssetTestUtil.secondaryAssetsLicencesMap;
    when(applicationAssetService.getSecondaryAssets(applicationVersion)).thenReturn(assets);
    when(applicationAssetService.getAssetJsonForApplicationAsset(assets.get(0)))
        .thenReturn(field2Json);
    when(applicationAssetService.getAssetJsonForApplicationAsset(assets.get(1)))
        .thenReturn(FieldTestUtil.field3Json);
    when(applicationAssetLicenceService.getAssetLicencesMap(applicationVersion))
        .thenReturn(assetLicencesMap);
    when(organisationUnitService.getOrganisationUnitByIdOrFallback(
        eq(assets.get(0).getAssetOperatorOuId()), any(), eq(assets.get(0).getCachedAssetOperatorName())))
        .thenReturn(OrganisationUnitTestUtil.orgUnit2Json);
    when(organisationUnitService.getOrganisationUnitByIdOrFallback(
        eq(assets.get(1).getAssetOperatorOuId()), any(), eq(assets.get(1).getCachedAssetOperatorName())))
        .thenReturn(OrganisationUnitTestUtil.orgUnit3Json);

    var assetViews = assetSummaryService.getSummaryViews(applicationVersion);

    assertThat(assetViews).isEqualTo(ApplicationAssetTestUtil.assetViews);
  }

  @Test
  void getSummaryView() {
    var asset = ApplicationAssetTestUtil.fieldAsset2;
    when(applicationAssetLicenceService.getAssetLicences(asset))
        .thenReturn(ApplicationAssetTestUtil.fieldAsset2Licences);
    when(applicationAssetService.getAssetJsonForApplicationAsset(asset))
        .thenReturn(field2Json);
    when(organisationUnitService.getOrganisationUnitByIdOrFallback(
        eq(asset.getAssetOperatorOuId()), any(), eq(asset.getCachedAssetOperatorName())))
        .thenReturn(OrganisationUnitTestUtil.orgUnit2Json);

    var assetView = assetSummaryService.getSummaryView(asset);
    assertThat(assetView).isEqualTo(assetView2);
  }

  @Test
  void getAdditionalAssetsSummaryCards_noAdditionalAssetInfo() {
    when(applicationFlagService.findFlagValue(applicationVersion, ApplicationFlagType.HAS_SECONDARY_ASSETS))
        .thenReturn(Optional.empty());

    assertThat(assetSummaryService.getAdditionalAssetsSummaryCards(applicationVersion))
        .isEqualTo(SummaryCard.emptySummaryCardList());
  }

  @Test
  void getAdditionalAssetsSummaryCards_noAdditionalAssets() {
    when(applicationFlagService.findFlagValue(applicationVersion, ApplicationFlagType.HAS_SECONDARY_ASSETS))
        .thenReturn(Optional.of(Boolean.FALSE));
    when(applicationAssetService.getSecondaryAssets(applicationVersion)).thenReturn(Collections.emptyList());

    assertThat(assetSummaryService.getAdditionalAssetsSummaryCards(applicationVersion))
        .isEqualTo(List.of(
            SummaryCard.simpleSummaryCard(
                new SummaryDataView(List.of(
                    new SummaryKeyValue(ApplicationFlagType.HAS_SECONDARY_ASSETS.getDisplayName(),
                        BooleanUtil.yesNoFromBoolean(Boolean.FALSE))
                ))
            )
        ));
  }

  @Test
  void getAdditionalAssetsSummaryCards_additionalAssetsExist() {
    when(applicationFlagService.findFlagValue(applicationVersion, ApplicationFlagType.HAS_SECONDARY_ASSETS))
        .thenReturn(Optional.of(Boolean.TRUE));

    var assets = ApplicationAssetTestUtil.secondaryAssets;
    var assetLicencesMap = ApplicationAssetTestUtil.secondaryAssetsLicencesMap;
    when(applicationAssetService.getSecondaryAssets(applicationVersion)).thenReturn(assets);
    when(applicationAssetService.getAssetJsonForApplicationAsset(assets.get(0)))
        .thenReturn(field2Json);
    when(applicationAssetService.getAssetJsonForApplicationAsset(assets.get(1)))
        .thenReturn(FieldTestUtil.field3Json);
    when(applicationAssetLicenceService.getAssetLicencesMap(applicationVersion))
        .thenReturn(assetLicencesMap);
    when(organisationUnitService.getOrganisationUnitByIdOrFallback(
        eq(assets.get(0).getAssetOperatorOuId()), any(), eq(assets.get(0).getCachedAssetOperatorName())))
        .thenReturn(OrganisationUnitTestUtil.orgUnit2Json);
    when(organisationUnitService.getOrganisationUnitByIdOrFallback(
        eq(assets.get(1).getAssetOperatorOuId()), any(), eq(assets.get(1).getCachedAssetOperatorName())))
        .thenReturn(OrganisationUnitTestUtil.orgUnit3Json);

    var fieldPrompt = "Field";
    var operatorPrompt = "Field operator";
    var licencesPrompt = "Licences";

    assertThat(assetSummaryService.getAdditionalAssetsSummaryCards(applicationVersion))
        .isEqualTo(
            List.of(
                SummaryCard.simpleSummaryCard(
                    SummaryDataView.newWithKeyValue(ApplicationFlagType.HAS_SECONDARY_ASSETS.getDisplayName(), Boolean.TRUE)
                ),
                SummaryCard.simpleSummaryCardWithHeading(
                    fieldPrompt + " " + assetView2.displayOrder(),
                    SummaryDataView
                        .newWithKeyValue(fieldPrompt, assetView2.assetName())
                        .addKeyValue(operatorPrompt, assetView2.assetOperatorName())
                        .addKeyValue(licencesPrompt, assetView2.assetLicences())
                ),
                SummaryCard.simpleSummaryCardWithHeading(
                    "Field " + assetView3.displayOrder(),
                    new SummaryDataView(List.of(
                        new SummaryKeyValue(fieldPrompt, assetView3.assetName()),
                        new SummaryKeyValue(operatorPrompt, assetView3.assetOperatorName()),
                        new SummaryKeyValue(licencesPrompt, assetView3.assetLicences())
                    ))
                )
            )
        );
  }
}