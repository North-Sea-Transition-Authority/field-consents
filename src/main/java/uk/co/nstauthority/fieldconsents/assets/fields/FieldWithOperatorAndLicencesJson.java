package uk.co.nstauthority.fieldconsents.assets.fields;

import java.util.Collections;
import java.util.List;
import uk.co.fivium.energyportalapi.generated.types.Field;
import uk.co.nstauthority.fieldconsents.assets.AssetWithLicencesJson;
import uk.co.nstauthority.fieldconsents.assets.AssetWithOperatorJson;
import uk.co.nstauthority.fieldconsents.licences.LicenceJson;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitJson;

public class FieldWithOperatorAndLicencesJson extends FieldJson implements AssetWithOperatorJson, AssetWithLicencesJson {

  private final OrganisationUnitJson operatorJson;

  private final List<LicenceJson> licenses;

  public static FieldWithOperatorAndLicencesJson from(Field field) {
    return new FieldWithOperatorAndLicencesJson(
        field.getFieldId(),
        field.getFieldName(),
        FieldStatusJson.from(field),
        FieldGeographicAreaJson.from(field),
        FieldShoreJson.from(field),
        field.getFieldOperator() != null
            ? OrganisationUnitJson.from(field.getFieldOperator())
            : null,
        field.getLicences() == null || field.getLicences().isEmpty()
            ? Collections.emptyList()
            : field.getLicences().stream().map(LicenceJson::from).toList()
    );
  }

  public FieldWithOperatorAndLicencesJson(Integer fieldId,
                                          String fieldName,
                                          FieldStatusJson statusJson,
                                          FieldGeographicAreaJson geographicAreaJson,
                                          FieldShoreJson shoreJson,
                                          OrganisationUnitJson operatorJson,
                                          List<LicenceJson> licenses) {
    super(fieldId, fieldName, statusJson, geographicAreaJson, shoreJson);
    this.operatorJson = operatorJson;
    this.licenses = licenses;
  }

  @Override
  public OrganisationUnitJson getOperatorJson() {
    return operatorJson;
  }

  @Override
  public List<LicenceJson> getLicences() {
    return licenses;
  }
}
