package uk.co.nstauthority.fieldconsents.assets;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

public record AssetView(
    Integer displayOrder,
    Integer assetNo,
    String assetName,
    String assetOperatorName,
    String deleteUrl
) {

  static AssetView from(ApplicationAsset applicationAsset, Integer displayOrder) {

    String deleteUrl = ReverseRouter.route(on(ApplicationAssetController.class).deleteAssetConfirm(
        applicationAsset.getApplicationVersion().getApplication().getId(),
        applicationAsset.getAssetNo()
    ));

    return new AssetView(
        displayOrder,
        applicationAsset.getAssetNo(),
        applicationAsset.getCachedFieldName(),
        applicationAsset.getCachedAssetOperatorName(),
        deleteUrl
    );
  }
}
