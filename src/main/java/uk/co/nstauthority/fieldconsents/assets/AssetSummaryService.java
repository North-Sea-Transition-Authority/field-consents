package uk.co.nstauthority.fieldconsents.assets;

import java.util.List;
import java.util.stream.IntStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

@Service
public class AssetSummaryService {
  
  private final ApplicationAssetService applicationAssetService;

  @Autowired
  public AssetSummaryService(ApplicationAssetService applicationAssetService) {
    this.applicationAssetService = applicationAssetService;
  }

  List<AssetView> getSummaryViews(ApplicationVersion applicationVersion) {
    List<ApplicationAsset> assets = applicationAssetService.getAdditionalAssetsForApplicationVersion(applicationVersion);
    return IntStream.range(0, assets.size())
        .mapToObj(index -> AssetView.from(assets.get(index), index + 1))
        .toList();
  }
}
