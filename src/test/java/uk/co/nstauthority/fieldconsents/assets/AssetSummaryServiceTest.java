package uk.co.nstauthority.fieldconsents.assets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
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

@ExtendWith(MockitoExtension.class)
class AssetSummaryServiceTest {

  @Mock
  private ApplicationAssetService applicationAssetService;
  
  private AssetSummaryService assetSummaryService;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    assetSummaryService = new AssetSummaryService(applicationAssetService);
    applicationVersion = ApplicationTestUtil.getApplicationVersionWithType(ApplicationType.VENT);
  }

  @Test
  void getSummaryViews_noAssets() {
    when(applicationAssetService.getAdditionalAssetsForApplicationVersion(applicationVersion)).thenReturn(new ArrayList<>());

    var AssetViews = assetSummaryService.getSummaryViews(applicationVersion);

    assertThat(AssetViews).isEmpty();
  }

  @Test
  void getSummaryViews_manyAssets() {
    var assets = ApplicationAssetTestUtil.assets;
    when(applicationAssetService.getAdditionalAssetsForApplicationVersion(applicationVersion)).thenReturn(assets);

    var assetViews = assetSummaryService.getSummaryViews(applicationVersion);
    String expectedUrlBase = "/applications/" + applicationVersion.getApplication().getId() + "/additional-assets/";
    String expectedUrlDeleteUrl = "/delete";

    assertThat(assetViews)
        .extracting(
            AssetView::displayOrder,
            AssetView::assetNo,
            AssetView::assetName,
            AssetView::deleteUrl,
            AssetView::assetOperatorName
        )
        .containsExactly(
            tuple(1,
                assets.get(0).getAssetNo(),
                assets.get(0).getCachedFieldName(),
                expectedUrlBase + assets.get(0).getAssetNo() + expectedUrlDeleteUrl,
                assets.get(0).getCachedAssetOperatorName()),
            tuple(2,
                assets.get(1).getAssetNo(),
                assets.get(1).getCachedFieldName(),
                expectedUrlBase + assets.get(1).getAssetNo() + expectedUrlDeleteUrl,
                assets.get(1).getCachedAssetOperatorName()),
            tuple(3,
                assets.get(2).getAssetNo(),
                assets.get(2).getCachedFieldName(),
                expectedUrlBase + assets.get(2).getAssetNo() + expectedUrlDeleteUrl,
                assets.get(2).getCachedAssetOperatorName())
        );
  }
}