package uk.co.nstauthority.fieldconsents.assets.fields;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.fivium.energyportalapi.generated.types.Field;
import uk.co.nstauthority.fieldconsents.assets.AssetJson;
import uk.co.nstauthority.fieldconsents.assets.AssetType;

public class FieldJson implements AssetJson {

  private final Integer fieldId;

  private final String fieldName;

  private final FieldStatusJson statusJson;

  private final FieldGeographicAreaJson geographicAreaJson;

  private static final Logger LOGGER = LoggerFactory.getLogger(FieldJson.class);

  public static FieldJson from(Field field) {
    return new FieldJson(field.getFieldId(), field.getFieldName(), FieldStatusJson.from(field),
        FieldGeographicAreaJson.from(field));
  }

  public static FieldJson fromCachedInformation(Integer fieldId, String fieldName) {
    LOGGER.warn("Had to fallback to field cache info for: id {}, name {}", fieldId, fieldName);
    return new FieldJson(fieldId, fieldName, null, null);
  }

  public FieldJson(Integer fieldId, String fieldName, FieldStatusJson statusJson,
                   FieldGeographicAreaJson geographicAreaJson) {
    this.fieldId = fieldId;
    this.fieldName = fieldName;
    this.statusJson = statusJson;
    this.geographicAreaJson = geographicAreaJson;
  }

  @Override
  public Integer getId() {
    return fieldId;
  }

  @Override
  public String getName() {
    return fieldName;
  }

  @Override
  public String getStatusDisplayName() {
    return statusJson != null ? statusJson.statusDisplayName() : null;
  }

  @Override
  public AssetType getAssetType() {
    return AssetType.FIELD;
  }

  public String getGeographicAreaDisplayName() {
    return geographicAreaJson != null ? geographicAreaJson.geographicAreaDisplayName() : null;
  }
}
