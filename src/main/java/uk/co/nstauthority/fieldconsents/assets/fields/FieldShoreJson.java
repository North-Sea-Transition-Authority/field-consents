package uk.co.nstauthority.fieldconsents.assets.fields;

import uk.co.fivium.energyportalapi.generated.types.Field;
import uk.co.fivium.energyportalapi.generated.types.FieldShore;

public record FieldShoreJson(
    FieldShore shore,
    String shoreDisplayName
) {
  static FieldShoreJson from(Field field) {
    return new FieldShoreJson(field.getShore(), field.getShoreDisplayName());
  }
}
