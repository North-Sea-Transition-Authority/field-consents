package uk.co.nstauthority.fieldconsents.assets.fields;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.FIELD_1_GEOGRAPHIC_AREA;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.FIELD_1_STATUS;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1WithOperator;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1WithOperatorButEmptyLicences;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1WithOperatorAndLicences;

import java.util.Collections;
import org.junit.jupiter.api.Test;
import uk.co.nstauthority.fieldconsents.licences.LicenceJson;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitJson;

class FieldWithOperatorAndLicencesJsonTest {

  @Test
  void from_noOperatorOrLicences() {
    assertThat(FieldWithOperatorAndLicencesJson.from(field1))
        .usingRecursiveComparison()
        .isEqualTo(
            new FieldWithOperatorAndLicencesJson(
                field1.getFieldId(),
                field1.getFieldName(),
                FIELD_1_STATUS,
                FIELD_1_GEOGRAPHIC_AREA,
                null,
                Collections.emptyList()
            )
        );
  }

  @Test
  void from_withOperatorButNoLicences() {
    assertThat(FieldWithOperatorAndLicencesJson.from(field1WithOperator))
        .usingRecursiveComparison()
        .isEqualTo(
            new FieldWithOperatorAndLicencesJson(
                field1WithOperator.getFieldId(),
                field1WithOperator.getFieldName(),
                FIELD_1_STATUS,
                FIELD_1_GEOGRAPHIC_AREA,
                new OrganisationUnitJson(field1WithOperator.getFieldOperator().getOrganisationUnitId(),
                    field1WithOperator.getFieldOperator().getName()),
                Collections.emptyList()
            )
        );
  }

  @Test
  void from_withOperatorAndEmptyLicences() {
    assertThat(FieldWithOperatorAndLicencesJson.from(field1WithOperatorButEmptyLicences))
        .usingRecursiveComparison()
        .isEqualTo(
            new FieldWithOperatorAndLicencesJson(
                field1WithOperatorButEmptyLicences.getFieldId(),
                field1WithOperatorButEmptyLicences.getFieldName(),
                FIELD_1_STATUS,
                FIELD_1_GEOGRAPHIC_AREA,
                new OrganisationUnitJson(field1WithOperatorButEmptyLicences.getFieldOperator().getOrganisationUnitId(),
                    field1WithOperatorButEmptyLicences.getFieldOperator().getName()),
                Collections.emptyList()
            )
        );
  }

  @Test
  void from_withOperatorAndLicences() {
    assertThat(FieldWithOperatorAndLicencesJson.from(field1WithOperatorAndLicences))
        .usingRecursiveComparison()
        .isEqualTo(
            new FieldWithOperatorAndLicencesJson(
                field1WithOperatorAndLicences.getFieldId(),
                field1WithOperatorAndLicences.getFieldName(),
                FIELD_1_STATUS,
                FIELD_1_GEOGRAPHIC_AREA,
                new OrganisationUnitJson(field1WithOperatorAndLicences.getFieldOperator().getOrganisationUnitId(),
                    field1WithOperatorAndLicences.getFieldOperator().getName()),
                field1WithOperatorAndLicences.getLicences().stream().map(
                    licence -> new LicenceJson(licence.getId(), licence.getLicenceRef())
                ).toList()
            )
        );
  }
}
