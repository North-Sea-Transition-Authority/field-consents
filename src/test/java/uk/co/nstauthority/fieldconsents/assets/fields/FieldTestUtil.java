package uk.co.nstauthority.fieldconsents.assets.fields;

import java.util.List;
import uk.co.fivium.energyportalapi.generated.types.Field;

public class FieldTestUtil {
  public static Field field1 = Field.newBuilder().fieldId(1).fieldName("F1").build();
  public static FieldJson field1Json = new FieldJson(field1.getFieldId(), field1.getFieldName());
  public static Field field2 = Field.newBuilder().fieldId(2).fieldName("F2").build();
  public static FieldJson field2Json = new FieldJson(field2.getFieldId(), field2.getFieldName());
  public static Field field3 = Field.newBuilder().fieldId(3).fieldName("F3").build();
  public static FieldJson field3Json = new FieldJson(field3.getFieldId(), field3.getFieldName());
  public static List<Field> fieldList = List.of(field1, field2, field3);
}
