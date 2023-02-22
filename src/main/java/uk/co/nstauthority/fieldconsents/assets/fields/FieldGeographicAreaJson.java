package uk.co.nstauthority.fieldconsents.assets.fields;

import uk.co.fivium.energyportalapi.generated.types.Field;
import uk.co.fivium.energyportalapi.generated.types.FieldGeographicArea;

public record FieldGeographicAreaJson(
    FieldGeographicArea geographicArea,
    String geographicAreaDisplayName
) {
  static FieldGeographicAreaJson from(Field field) {
    return new FieldGeographicAreaJson(field.getGeographicArea(), field.getGeographicAreaDisplayName());
  }
}