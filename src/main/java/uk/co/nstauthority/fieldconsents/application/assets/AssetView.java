package uk.co.nstauthority.fieldconsents.application.assets;

public record AssetView(
    Integer displayOrder,
    Integer assetNo,
    String assetName,
    String assetOperatorName,

    String assetLicences,
    String deleteUrl
) {}