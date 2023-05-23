package uk.co.nstauthority.fieldconsents.assets.fields;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1;

import org.junit.jupiter.api.Test;
import uk.co.nstauthority.fieldconsents.assets.AssetType;

class FieldJsonTest {

  @Test
  void from() {
    FieldJson fieldJson = FieldJson.from(field1);
    assertThat(fieldJson)
        .usingRecursiveComparison()
        .isEqualTo(
            new FieldJson(
                field1.getFieldId(),
                field1.getFieldName(),
                new FieldStatusJson(field1.getStatus(), field1.getStatusDisplayName()),
                GeographicArea.valueOf(field1.getGeographicArea().name()),
                Shore.valueOf(field1.getShore().name()))
        );

    // the asserts below check the getters in FieldJson
    assertThat(fieldJson)
        .extracting(
            FieldJson::getId,
            FieldJson::getName,
            FieldJson::getStatusDisplayName,
            FieldJson::getAssetType,
            FieldJson::getGeographicArea,
            FieldJson::getShore
        )
        .containsExactly(
            field1.getFieldId(),
            field1.getFieldName(),
            field1.getStatusDisplayName(),
            AssetType.FIELD,
            GeographicArea.valueOf(field1.getGeographicArea().name()),
            Shore.valueOf(field1.getShore().name())
        );
  }

  @Test
  void fromCachedInformation() {
    FieldJson fieldJson = FieldJson.fromCachedInformation(field1.getFieldId(), field1.getFieldName());
    assertThat(fieldJson)
        .usingRecursiveComparison()
        .isEqualTo(
            new FieldJson(
                field1.getFieldId(),
                field1.getFieldName(),
                null,
                null,
                null)
        );

    // the asserts below check the getters in FieldJson
    assertThat(fieldJson)
        .extracting(
            FieldJson::getId,
            FieldJson::getName,
            FieldJson::getStatusDisplayName,
            FieldJson::getAssetType,
            FieldJson::getGeographicArea,
            FieldJson::getShore
        )
        .containsExactly(
            field1.getFieldId(),
            field1.getFieldName(),
            null,
            AssetType.FIELD,
            null,
            null
        );
  }
}
