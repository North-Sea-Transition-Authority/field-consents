package uk.co.nstauthority.fieldconsents.application.assets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.assetlicences.ApplicationAssetLicenceService;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitService;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil;

@ExtendWith(MockitoExtension.class)
class AssetSummaryServiceTest {

  @Mock
  private ApplicationAssetService applicationAssetService;

  @Mock
  private ApplicationAssetLicenceService applicationAssetLicenceService;

  @Mock
  private OrganisationUnitService organisationUnitService;
  
  private AssetSummaryService assetSummaryService;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    assetSummaryService = new AssetSummaryService(applicationAssetService, applicationAssetLicenceService,
        organisationUnitService);
    applicationVersion = ApplicationTestUtil.getApplicationVersionWithType(ApplicationType.VENT);
  }

  @Test
  void getSummaryViews_noAssets() {
    when(applicationAssetService.getSecondaryAssets(applicationVersion)).thenReturn(new ArrayList<>());

    var AssetViews = assetSummaryService.getSummaryViews(applicationVersion);

    assertThat(AssetViews).isEmpty();
  }

  @Test
  void getSummaryViews_manyAssets() {
    var assets = ApplicationAssetTestUtil.secondaryAssets;
    var assetLicencesMap = ApplicationAssetTestUtil.secondaryAssetsLicencesMap;
    when(applicationAssetService.getSecondaryAssets(applicationVersion)).thenReturn(assets);
    when(applicationAssetService.getAssetJsonForApplicationAsset(assets.get(0)))
        .thenReturn(FieldTestUtil.field2Json);
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
        .thenReturn(FieldTestUtil.field2Json);
    when(organisationUnitService.getOrganisationUnitByIdOrFallback(
        eq(asset.getAssetOperatorOuId()), any(), eq(asset.getCachedAssetOperatorName())))
        .thenReturn(OrganisationUnitTestUtil.orgUnit2Json);

    var assetView = assetSummaryService.getSummaryView(asset);
    assertThat(assetView).isEqualTo(ApplicationAssetTestUtil.assetView2);
  }

}