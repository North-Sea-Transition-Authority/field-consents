package uk.co.nstauthority.fieldconsents.assets.fields;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.fivium.energyportalapi.generated.types.Field;
import uk.co.nstauthority.fieldconsents.assets.AssetJson;
import uk.co.nstauthority.fieldconsents.assets.AssetType;

public class FieldJson implements AssetJson {

  private final Integer fieldId;

  private final String fieldName;

  private static final Logger LOGGER = LoggerFactory.getLogger(FieldJson.class);

  public static FieldJson from(Field field) {
    return new FieldJson(field.getFieldId(), field.getFieldName());
  }

  public static FieldJson fromCachedInformation(Integer fieldId, String fieldName) {
    LOGGER.warn("Had to fallback to field cache info for: id {}, name {}", fieldId, fieldName);
    return new FieldJson(fieldId, fieldName);
  }

  public FieldJson(Integer fieldId, String fieldName) {
    this.fieldId = fieldId;
    this.fieldName = fieldName;
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
  public AssetType getAssetType() {
    return AssetType.FIELD;
  }
}
