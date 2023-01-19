package uk.co.nstauthority.fieldconsents.assets.fields;

import java.util.List;
import uk.co.fivium.energyportalapi.generated.types.Field;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil;

public class FieldTestUtil {

  public static final Integer FIELD_ID_1 = 1;
  public static final Integer FIELD_ID_2 = 2;
  public static final Integer FIELD_ID_3 = 3;

  public static final String FIELD_NAME_1 = "F1";
  public static final String FIELD_NAME_2 = "F2";
  public static final String FIELD_NAME_3 = "F3";

  public static Field field1 = Field.newBuilder().fieldId(FIELD_ID_1).fieldName(FIELD_NAME_1).build();
  public static Field field1WithOperator = Field.newBuilder().fieldId(FIELD_ID_1).fieldName(FIELD_NAME_1).fieldOperator(OrganisationUnitTestUtil.orgUnit1).build();

  public static FieldJson field1Json = new FieldJson(
      field1WithOperator.getFieldId(),
      field1WithOperator.getFieldName(),
      null,
      null
  );

  public static FieldJson field1JsonWithOperator = new FieldJson(
      field1WithOperator.getFieldId(),
      field1WithOperator.getFieldName(),
      field1WithOperator.getFieldOperator().getOrganisationUnitId(),
      field1WithOperator.getFieldOperator().getName()
  );

  public static Field field2 = Field.newBuilder().fieldId(FIELD_ID_2).fieldName(FIELD_NAME_2).build();
  public static Field field2WithOperator = Field.newBuilder().fieldId(FIELD_ID_2).fieldName(FIELD_NAME_2).fieldOperator(OrganisationUnitTestUtil.orgUnit2).build();

  public static FieldJson field2Json = new FieldJson(
      field2WithOperator.getFieldId(),
      field2WithOperator.getFieldName(),
      null,
      null
  );

  public static FieldJson field2JsonWithOperator = new FieldJson(
      field2WithOperator.getFieldId(),
      field2WithOperator.getFieldName(),
      field2WithOperator.getFieldOperator().getOrganisationUnitId(),
      field2WithOperator.getFieldOperator().getName()
  );

  public static Field field3 = Field.newBuilder().fieldId(FIELD_ID_3).fieldName(FIELD_NAME_3).build();
  public static Field field3WithOperator = Field.newBuilder().fieldId(FIELD_ID_3).fieldName(FIELD_NAME_3).fieldOperator(OrganisationUnitTestUtil.orgUnit3).build();
  public static FieldJson field3Json = new FieldJson(
      field3WithOperator.getFieldId(),
      field3WithOperator.getFieldName(),
      null,
      null
  );

  public static FieldJson field3JsonWithOperator = new FieldJson(
      field3WithOperator.getFieldId(),
      field3WithOperator.getFieldName(),
      field3WithOperator.getFieldOperator().getOrganisationUnitId(),
      field3WithOperator.getFieldOperator().getName()
  );

  public static List<Field> fieldList = List.of(field1, field2, field3);
  public static List<Field> fieldsWithOperatorList = List.of(field1WithOperator, field2WithOperator, field3WithOperator);
}
