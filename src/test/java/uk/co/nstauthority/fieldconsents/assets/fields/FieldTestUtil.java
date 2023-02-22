package uk.co.nstauthority.fieldconsents.assets.fields;

import java.util.Collections;
import java.util.List;
import uk.co.fivium.energyportalapi.generated.types.Field;
import uk.co.nstauthority.fieldconsents.licences.LicenceTestUtil;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitJson;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil;

public class FieldTestUtil {

  public static final Integer FIELD_ID_1 = 1;
  public static final Integer FIELD_ID_2 = 2;
  public static final Integer FIELD_ID_3 = 3;

  public static final String FIELD_NAME_1 = "F1";
  public static final String FIELD_NAME_2 = "F2";
  public static final String FIELD_NAME_3 = "F3";

  public static Field field1 = Field.newBuilder().fieldId(FIELD_ID_1).fieldName(FIELD_NAME_1).build();
  public static Field field1WithOperator = Field.newBuilder().fieldId(FIELD_ID_1).fieldName(FIELD_NAME_1)
      .fieldOperator(OrganisationUnitTestUtil.orgUnit1).build();

  public static Field field1WithOperatorAndLicences = Field.newBuilder().fieldId(FIELD_ID_1).fieldName(FIELD_NAME_1)
      .fieldOperator(OrganisationUnitTestUtil.orgUnit1)
      .licences(LicenceTestUtil.licences3)
      .build();

    public static Field field1WithOperatorButEmptyLicences = Field.newBuilder().fieldId(FIELD_ID_1).fieldName(FIELD_NAME_1)
      .fieldOperator(OrganisationUnitTestUtil.orgUnit1)
      .licences(Collections.emptyList())
      .build();

  public static Field field1WithNoOperatorButLicences = Field.newBuilder().fieldId(FIELD_ID_1).fieldName(FIELD_NAME_1)
      .fieldOperator(null)
      .licences(LicenceTestUtil.licences3)
      .build();

  public static FieldJson field1Json = new FieldJson(
      field1.getFieldId(),
      field1.getFieldName()
  );

  public static FieldWithOperatorJson field1JsonWithOperator = new FieldWithOperatorJson(
      field1WithOperator.getFieldId(),
      field1WithOperator.getFieldName(),
      OrganisationUnitJson.from(field1WithOperator.getFieldOperator())
  );

  public static FieldWithOperatorAndLicencesJson field1JsonWithOperatorAndLicences =
      FieldWithOperatorAndLicencesJson.from(field1WithOperatorAndLicences);

  public static FieldWithOperatorAndLicencesJson field1JsonWithNullOperatorAndLicences =
      new FieldWithOperatorAndLicencesJson(
          field1.getFieldId(),
          field1.getFieldName(),
          null,
          null
      );

  public static FieldWithOperatorAndLicencesJson field1JsonWithOperatorButEmptyLicences =
      FieldWithOperatorAndLicencesJson.from(field1WithOperatorButEmptyLicences);

  public static FieldWithOperatorAndLicencesJson field1JsonWithNoOperatorButLicences =
      FieldWithOperatorAndLicencesJson.from(field1WithNoOperatorButLicences);

  public static Field field2 = Field.newBuilder().fieldId(FIELD_ID_2).fieldName(FIELD_NAME_2).build();
  public static Field field2WithOperator = Field.newBuilder().fieldId(FIELD_ID_2).fieldName(FIELD_NAME_2).fieldOperator(OrganisationUnitTestUtil.orgUnit2).build();

  public static Field field2WithOperatorAndLicences = Field.newBuilder().fieldId(FIELD_ID_2).fieldName(FIELD_NAME_2)
      .fieldOperator(OrganisationUnitTestUtil.orgUnit2)
      .licences(LicenceTestUtil.licences2)
      .build();

  public static FieldJson field2Json = new FieldJson(
      field2WithOperator.getFieldId(),
      field2WithOperator.getFieldName()
  );

  public static FieldWithOperatorJson field2JsonWithOperator = new FieldWithOperatorJson(
      field2WithOperator.getFieldId(),
      field2WithOperator.getFieldName(),
      OrganisationUnitJson.from(field2WithOperator.getFieldOperator())
  );

  public static FieldWithOperatorAndLicencesJson field2JsonWithOperatorAndLicences =
      FieldWithOperatorAndLicencesJson.from(field2WithOperatorAndLicences);

  public static Field field3 = Field.newBuilder().fieldId(FIELD_ID_3).fieldName(FIELD_NAME_3).build();
  public static Field field3WithOperator = Field.newBuilder().fieldId(FIELD_ID_3).fieldName(FIELD_NAME_3).fieldOperator(OrganisationUnitTestUtil.orgUnit3).build();
  public static FieldJson field3Json = new FieldJson(
      field3WithOperator.getFieldId(),
      field3WithOperator.getFieldName()
  );

  public static FieldWithOperatorJson field3JsonWithOperator = new FieldWithOperatorJson(
      field3WithOperator.getFieldId(),
      field3WithOperator.getFieldName(),
      OrganisationUnitJson.from(field3WithOperator.getFieldOperator())
  );

  public static List<Field> fieldList = List.of(field1, field2, field3);
  public static List<Field> fieldsWithOperatorList = List.of(field1WithOperator, field2WithOperator, field3WithOperator);
}
