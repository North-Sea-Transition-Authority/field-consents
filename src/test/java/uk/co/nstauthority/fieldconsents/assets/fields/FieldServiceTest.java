package uk.co.nstauthority.fieldconsents.assets.fields;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldService.fieldStatusesAllowed;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1Json;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1JsonWithOperator;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1WithOperator;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field2;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field2Json;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field2JsonWithOperator;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field2WithOperator;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field3Json;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field3JsonWithOperator;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.fieldList;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.fieldsWithOperatorList;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.energyportalapi.client.field.FieldApi;
import uk.co.fivium.energyportalapi.generated.client.FieldProjectionRoot;
import uk.co.fivium.energyportalapi.generated.client.FieldsProjectionRoot;

@ExtendWith(MockitoExtension.class)
public class FieldServiceTest {

  FieldService fieldService;

  @Mock
  FieldApi fieldApi;

  @BeforeEach
  void setup() {
    fieldService = new FieldService(fieldApi);
  }

  @Test
  void searchFields_allTestFields() {
    when(fieldApi.searchFields(eq("F"), eq(fieldStatusesAllowed),
        any(FieldsProjectionRoot.class), eq("Search test fields")))
        .thenReturn(fieldList);

    List<FieldJson> allTestFields = fieldService.searchFields("F", "Search test fields");
    assertThat(allTestFields).containsExactly(field1Json, field2Json, field3Json);
  }

  @Test
  void searchFieldsWithOperator_allTestFields() {
    when(fieldApi.searchFields(eq("F"), eq(fieldStatusesAllowed),
        any(FieldsProjectionRoot.class), eq("Search test fields")))
        .thenReturn(fieldsWithOperatorList);

    List<FieldJson> allTestFields = fieldService.searchFieldsWithOperator("F", "Search test fields");
    assertThat(allTestFields).containsExactly(field1JsonWithOperator, field2JsonWithOperator, field3JsonWithOperator);
  }

  @Test
  void searchFields_singleTestField() {
    when(fieldApi.searchFields(eq("F2"), eq(fieldStatusesAllowed),
        any(FieldsProjectionRoot.class), eq("Search test fields")))
        .thenReturn(List.of(field2));

    List<FieldJson> singleTestField = fieldService.searchFields("F2", "Search test fields");
    assertThat(singleTestField).containsExactly(field2Json);
  }

  @Test
  void searchFieldsWithOperator_singleTestField() {
    when(fieldApi.searchFields(eq("F2"), eq(fieldStatusesAllowed),
        any(FieldsProjectionRoot.class), eq("Search test fields")))
        .thenReturn(List.of(field2WithOperator));

    List<FieldJson> singleTestField = fieldService.searchFieldsWithOperator("F2", "Search test fields");
    assertThat(singleTestField).containsExactly(field2JsonWithOperator);
  }

  @Test
  void findField_fieldExists() {
    when(fieldApi.findFieldById(eq(field1.getFieldId()), any(FieldProjectionRoot.class), eq("Field service test")))
        .thenReturn(Optional.of(field1));

    var fieldJsonOptional = fieldService.findField(field1.getFieldId(), "Field service test");
    assertThat(fieldJsonOptional).contains(field1Json);
  }

  @Test
  void findFieldWithOperator_fieldExists() {
    when(fieldApi.findFieldById(eq(field1WithOperator.getFieldId()), any(FieldProjectionRoot.class), eq("Field service test")))
        .thenReturn(Optional.of(field1WithOperator));

    var fieldJsonOptional = fieldService.findFieldWithOperator(field1WithOperator.getFieldId(), "Field service test");
    assertThat(fieldJsonOptional).contains(field1JsonWithOperator);
  }

  @Test
  void findField_fieldNotExists() {
    when(fieldApi.findFieldById(eq(0), any(FieldProjectionRoot.class), eq("Field service test")))
        .thenReturn(Optional.empty());

    var fieldJsonOptional = fieldService.findField(0, "Field service test");
    assertThat(fieldJsonOptional).isEqualTo(Optional.empty());
  }

  @Test
  void findFieldWithOperator_fieldNotExists() {
    when(fieldApi.findFieldById(eq(0), any(FieldProjectionRoot.class), eq("Field service test")))
        .thenReturn(Optional.empty());

    var fieldJsonOptional = fieldService.findFieldWithOperator(0, "Field service test");
    assertThat(fieldJsonOptional).isNotPresent();
  }

  @Test
  void getField_fieldExists() {
    when(fieldApi.findFieldById(eq(field1.getFieldId()), any(FieldProjectionRoot.class), eq("Field service test")))
        .thenReturn(Optional.of(field1));

    var fieldJson = fieldService.getField(field1.getFieldId(), "Field service test");
    assertThat(fieldJson).isEqualTo(field1Json);
  }

  @Test
  void getFieldWithOperator_fieldExists() {
    when(fieldApi.findFieldById(eq(field1WithOperator.getFieldId()), any(FieldProjectionRoot.class), eq("Field service test")))
        .thenReturn(Optional.of(field1WithOperator));

    var fieldJson = fieldService.getFieldWithOperator(field1WithOperator.getFieldId(), "Field service test");
    assertThat(fieldJson).isEqualTo(field1JsonWithOperator);
  }

  @Test
  void getField_fieldNotExists() {
    when(fieldApi.findFieldById(eq(0), any(FieldProjectionRoot.class), eq("Field service test")))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> fieldService.getField(0, "Field service test"))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Field not found for field id 0");
  }

  @Test
  void getFieldWithOperator_fieldNotExists() {
    when(fieldApi.findFieldById(eq(0), any(FieldProjectionRoot.class), eq("Field service test")))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> fieldService.getFieldWithOperator(0, "Field service test"))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Field not found for field id 0");
  }
}
