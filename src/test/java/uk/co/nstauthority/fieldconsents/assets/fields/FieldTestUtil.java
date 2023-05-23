package uk.co.nstauthority.fieldconsents.assets.fields;

import java.util.Collections;
import java.util.List;
import uk.co.fivium.energyportalapi.generated.types.Field;
import uk.co.fivium.energyportalapi.generated.types.FieldGeographicArea;
import uk.co.fivium.energyportalapi.generated.types.FieldShore;
import uk.co.fivium.energyportalapi.generated.types.FieldStatus;
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

  public static final FieldStatusJson FIELD_1_STATUS =
      new FieldStatusJson(FieldStatus.STATUS500, "500 - Appraisal - FDP submitted review");
  public static final FieldStatusJson FIELD_2_STATUS =
      new FieldStatusJson(FieldStatus.STATUS600, "600 - Construction - FDP approved");
  public static final FieldStatusJson FIELD_3_STATUS =
      new FieldStatusJson(FieldStatus.STATUS700, "700 - Producing");

  public static final GeographicArea FIELD_1_GEOGRAPHIC_AREA = GeographicArea.valueOf(FieldGeographicArea.CNS.name());
  public static final GeographicArea FIELD_2_GEOGRAPHIC_AREA = GeographicArea.valueOf(FieldGeographicArea.SNS.name());
  public static final GeographicArea FIELD_3_GEOGRAPHIC_AREA = GeographicArea.valueOf(FieldGeographicArea.NNS.name());

  public static final Shore FIELD_1_SHORE = Shore.valueOf(FieldShore.OFFSHORE.name());

  public static final Shore FIELD_2_SHORE = Shore.valueOf(FieldShore.ONSHORE.name());

  public static final Shore FIELD_3_SHORE = Shore.valueOf(FieldShore.UNKNOWN.name());

  public static Field field1 = Field.newBuilder().fieldId(FIELD_ID_1).fieldName(FIELD_NAME_1)
      .status(FIELD_1_STATUS.status())
      .statusDisplayName(FIELD_1_STATUS.statusDisplayName())
      .geographicArea(FieldGeographicArea.valueOf(FIELD_1_GEOGRAPHIC_AREA.name()))
      .geographicAreaDisplayName(FIELD_1_GEOGRAPHIC_AREA.getDisplayName())
      .shore(FieldShore.valueOf(FIELD_1_SHORE.name()))
      .shoreDisplayName(FIELD_1_SHORE.getDisplayName())
      .build();
  public static Field field1WithOperator = Field.newBuilder().fieldId(FIELD_ID_1).fieldName(FIELD_NAME_1)
      .status(FIELD_1_STATUS.status())
      .statusDisplayName(FIELD_1_STATUS.statusDisplayName())
      .geographicArea(FieldGeographicArea.valueOf(FIELD_1_GEOGRAPHIC_AREA.name()))
      .geographicAreaDisplayName(FIELD_1_GEOGRAPHIC_AREA.getDisplayName())
      .shore(FieldShore.valueOf(FIELD_1_SHORE.name()))
      .shoreDisplayName(FIELD_1_SHORE.getDisplayName())
      .fieldOperator(OrganisationUnitTestUtil.orgUnit1).build();

  public static Field field1WithOperatorAndLicences = Field.newBuilder().fieldId(FIELD_ID_1).fieldName(FIELD_NAME_1)
      .status(FIELD_1_STATUS.status())
      .statusDisplayName(FIELD_1_STATUS.statusDisplayName())
      .geographicArea(FieldGeographicArea.valueOf(FIELD_1_GEOGRAPHIC_AREA.name()))
      .geographicAreaDisplayName(FIELD_1_GEOGRAPHIC_AREA.getDisplayName())
      .shore(FieldShore.valueOf(FIELD_1_SHORE.name()))
      .shoreDisplayName(FIELD_1_SHORE.getDisplayName())
      .fieldOperator(OrganisationUnitTestUtil.orgUnit1)
      .licences(LicenceTestUtil.licences3)
      .build();

  public static Field field1WithOperatorButEmptyLicences = Field.newBuilder().fieldId(FIELD_ID_1).fieldName(FIELD_NAME_1)
      .status(FIELD_1_STATUS.status())
      .statusDisplayName(FIELD_1_STATUS.statusDisplayName())
      .geographicArea(FieldGeographicArea.valueOf(FIELD_1_GEOGRAPHIC_AREA.name()))
      .geographicAreaDisplayName(FIELD_1_GEOGRAPHIC_AREA.getDisplayName())
      .shore(FieldShore.valueOf(FIELD_1_SHORE.name()))
      .shoreDisplayName(FIELD_1_SHORE.getDisplayName())
      .shore(FieldShore.valueOf(FIELD_1_SHORE.name()))
      .shoreDisplayName(FIELD_1_SHORE.getDisplayName())
      .fieldOperator(OrganisationUnitTestUtil.orgUnit1)
      .licences(Collections.emptyList())
      .build();

  public static Field field1WithNoOperatorButLicences = Field.newBuilder().fieldId(FIELD_ID_1).fieldName(FIELD_NAME_1)
      .status(FIELD_1_STATUS.status())
      .statusDisplayName(FIELD_1_STATUS.statusDisplayName())
      .geographicArea(FieldGeographicArea.valueOf(FIELD_1_GEOGRAPHIC_AREA.name()))
      .geographicAreaDisplayName(FIELD_1_GEOGRAPHIC_AREA.getDisplayName())
      .shore(FieldShore.valueOf(FIELD_1_SHORE.name()))
      .shoreDisplayName(FIELD_1_SHORE.getDisplayName())
      .fieldOperator(null)
      .licences(LicenceTestUtil.licences3)
      .build();

  public static FieldJson field1Json = new FieldJson(
      field1.getFieldId(),
      field1.getFieldName(),
      FIELD_1_STATUS,
      FIELD_1_GEOGRAPHIC_AREA,
      FIELD_1_SHORE);

  public static FieldWithOperatorJson field1JsonWithOperator = new FieldWithOperatorJson(
      field1WithOperator.getFieldId(),
      field1WithOperator.getFieldName(),
      FIELD_1_STATUS,
      FIELD_1_GEOGRAPHIC_AREA,
      FIELD_1_SHORE,
      OrganisationUnitJson.from(field1WithOperator.getFieldOperator())
  );

  public static FieldWithOperatorAndLicencesJson field1JsonWithOperatorAndLicences =
      FieldWithOperatorAndLicencesJson.from(field1WithOperatorAndLicences);

  public static FieldWithOperatorAndLicencesJson field1JsonWithNullOperatorAndLicences =
      new FieldWithOperatorAndLicencesJson(
          field1.getFieldId(),
          field1.getFieldName(),
          FIELD_1_STATUS,
          FIELD_1_GEOGRAPHIC_AREA,
          FIELD_1_SHORE,
          null,
          null
      );

  public static FieldWithOperatorAndLicencesJson field1JsonWithOperatorButEmptyLicences =
      FieldWithOperatorAndLicencesJson.from(field1WithOperatorButEmptyLicences);

  public static FieldWithOperatorAndLicencesJson field1JsonWithNoOperatorButLicences =
      FieldWithOperatorAndLicencesJson.from(field1WithNoOperatorButLicences);

  public static Field field2 = Field.newBuilder().fieldId(FIELD_ID_2).fieldName(FIELD_NAME_2)
      .status(FIELD_2_STATUS.status())
      .statusDisplayName(FIELD_2_STATUS.statusDisplayName())
      .geographicArea(FieldGeographicArea.valueOf(FIELD_2_GEOGRAPHIC_AREA.name()))
      .geographicAreaDisplayName(FIELD_2_GEOGRAPHIC_AREA.getDisplayName())
      .shore(FieldShore.valueOf(FIELD_2_SHORE.name()))
      .shoreDisplayName(FIELD_2_SHORE.getDisplayName())
      .build();

  public static Field field2WithOperator = Field.newBuilder().fieldId(FIELD_ID_2).fieldName(FIELD_NAME_2)
      .status(FIELD_2_STATUS.status())
      .statusDisplayName(FIELD_2_STATUS.statusDisplayName())
      .geographicArea(FieldGeographicArea.valueOf(FIELD_2_GEOGRAPHIC_AREA.name()))
      .geographicAreaDisplayName(FIELD_2_GEOGRAPHIC_AREA.getDisplayName())
      .shore(FieldShore.valueOf(FIELD_2_SHORE.name()))
      .shoreDisplayName(FIELD_2_SHORE.getDisplayName())
      .fieldOperator(OrganisationUnitTestUtil.orgUnit2)
      .build();

  public static Field field2WithOperatorAndLicences = Field.newBuilder().fieldId(FIELD_ID_2).fieldName(FIELD_NAME_2)
      .status(FIELD_2_STATUS.status())
      .statusDisplayName(FIELD_2_STATUS.statusDisplayName())
      .geographicArea(FieldGeographicArea.valueOf(FIELD_2_GEOGRAPHIC_AREA.name()))
      .geographicAreaDisplayName(FIELD_2_GEOGRAPHIC_AREA.getDisplayName())
      .shore(FieldShore.valueOf(FIELD_2_SHORE.name()))
      .shoreDisplayName(FIELD_2_SHORE.getDisplayName())
      .fieldOperator(OrganisationUnitTestUtil.orgUnit2)
      .licences(LicenceTestUtil.licences2)
      .build();

  public static FieldJson field2Json = new FieldJson(
      field2WithOperator.getFieldId(),
      field2WithOperator.getFieldName(),
      FIELD_2_STATUS,
      FIELD_2_GEOGRAPHIC_AREA,
      FIELD_2_SHORE);

  public static FieldWithOperatorJson field2JsonWithOperator = new FieldWithOperatorJson(
      field2WithOperator.getFieldId(),
      field2WithOperator.getFieldName(),
      FIELD_2_STATUS,
      FIELD_2_GEOGRAPHIC_AREA,
      FIELD_2_SHORE,
      OrganisationUnitJson.from(field2WithOperator.getFieldOperator())
  );

  public static FieldWithOperatorAndLicencesJson field2JsonWithOperatorAndLicences =
      FieldWithOperatorAndLicencesJson.from(field2WithOperatorAndLicences);

  public static Field field3 = Field.newBuilder().fieldId(FIELD_ID_3).fieldName(FIELD_NAME_3)
      .status(FIELD_3_STATUS.status())
      .statusDisplayName(FIELD_3_STATUS.statusDisplayName())
      .geographicArea(FieldGeographicArea.valueOf(FIELD_3_GEOGRAPHIC_AREA.name()))
      .geographicAreaDisplayName(FIELD_3_GEOGRAPHIC_AREA.getDisplayName())
      .shore(FieldShore.valueOf(FIELD_3_SHORE.name()))
      .shoreDisplayName(FIELD_3_SHORE.getDisplayName())
      .build();

  public static Field field3WithOperator = Field.newBuilder().fieldId(FIELD_ID_3).fieldName(FIELD_NAME_3)
      .status(FIELD_3_STATUS.status())
      .statusDisplayName(FIELD_3_STATUS.statusDisplayName())
      .geographicArea(FieldGeographicArea.valueOf(FIELD_3_GEOGRAPHIC_AREA.name()))
      .geographicAreaDisplayName(FIELD_3_GEOGRAPHIC_AREA.getDisplayName())
      .shore(FieldShore.valueOf(FIELD_3_SHORE.name()))
      .shoreDisplayName(FIELD_3_SHORE.getDisplayName())
      .fieldOperator(OrganisationUnitTestUtil.orgUnit3)
      .build();

  public static FieldJson field3Json = new FieldJson(
      field3WithOperator.getFieldId(),
      field3WithOperator.getFieldName(),
      FIELD_3_STATUS,
      FIELD_3_GEOGRAPHIC_AREA,
      FIELD_3_SHORE);

  public static FieldWithOperatorJson field3JsonWithOperator = new FieldWithOperatorJson(
      field3WithOperator.getFieldId(),
      field3WithOperator.getFieldName(),
      FIELD_3_STATUS,
      FIELD_3_GEOGRAPHIC_AREA,
      FIELD_3_SHORE,
      OrganisationUnitJson.from(field3WithOperator.getFieldOperator())
  );

  public static List<Field> fieldList = List.of(field1, field2, field3);
  public static List<Field> fieldsWithOperatorList = List.of(field1WithOperator, field2WithOperator, field3WithOperator);
}
