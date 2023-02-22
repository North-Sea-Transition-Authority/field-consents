package uk.co.nstauthority.fieldconsents.assets.fields;

import uk.co.fivium.energyportalapi.generated.types.Field;
import uk.co.nstauthority.fieldconsents.assets.AssetWithOperatorJson;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitJson;

public class FieldWithOperatorJson extends FieldJson implements AssetWithOperatorJson {

  private final OrganisationUnitJson operatorJson;

  public static FieldWithOperatorJson from(Field field) {
    return new FieldWithOperatorJson(
        field.getFieldId(),
        field.getFieldName(),
        FieldStatusJson.from(field),
        FieldGeographicAreaJson.from(field),
        field.getFieldOperator() != null ? OrganisationUnitJson.from(field.getFieldOperator()) : null
    );
  }

  public FieldWithOperatorJson(Integer fieldId,
                               String fieldName,
                               FieldStatusJson statusJson,
                               FieldGeographicAreaJson geographicAreaJson,
                               OrganisationUnitJson operatorJson) {
    super(fieldId, fieldName, statusJson, geographicAreaJson);
    this.operatorJson = operatorJson;
  }

  @Override
  public OrganisationUnitJson getOperatorJson() {
    return operatorJson;
  }
}
