package uk.co.nstauthority.fieldconsents.assets.fields;

import uk.co.fivium.energyportalapi.generated.types.Field;
import uk.co.fivium.energyportalapi.generated.types.FieldStatus;

record FieldStatusJson(
    FieldStatus status,
    String statusDisplayName
) {
  static FieldStatusJson from(Field field) {
    return new FieldStatusJson(field.getStatus(), field.getStatusDisplayName());
  }
}
