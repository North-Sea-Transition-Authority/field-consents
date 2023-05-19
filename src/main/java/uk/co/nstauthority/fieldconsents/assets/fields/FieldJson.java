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

  private final GeographicArea geographicArea;

  private final FieldShoreJson shoreJson;

  private static final Logger LOGGER = LoggerFactory.getLogger(FieldJson.class);

  public static FieldJson from(Field field) {
    return new FieldJson(field.getFieldId(), field.getFieldName(), FieldStatusJson.from(field),
        GeographicArea.valueOf(field.getGeographicArea().name()), FieldShoreJson.from(field));
  }

  public static FieldJson fromCachedInformation(Integer fieldId, String fieldName) {
    LOGGER.warn("Had to fallback to field cache info for: id {}, name {}", fieldId, fieldName);
    return new FieldJson(fieldId, fieldName, null, null, null);
  }

  public FieldJson(Integer fieldId, String fieldName, FieldStatusJson statusJson,
                   GeographicArea geographicArea, FieldShoreJson shoreJson) {
    this.fieldId = fieldId;
    this.fieldName = fieldName;
    this.statusJson = statusJson;
    this.geographicArea = geographicArea;
    this.shoreJson = shoreJson;
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

  public GeographicArea getGeographicArea() {
    return geographicArea;
  }

  public String getShoreDisplayName() {
    return shoreJson != null ? shoreJson.shoreDisplayName() : null;
  }

  public FieldShoreJson getShoreJson() {
    return shoreJson;
  }
}
