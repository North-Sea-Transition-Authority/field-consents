package uk.co.nstauthority.fieldconsents.assets.fields;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldService.fieldStatusesAllowed;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1Json;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field2;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field2Json;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field3Json;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.fieldList;

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
  void searchFields_singleTestField() {
    when(fieldApi.searchFields(eq("F2"), eq(fieldStatusesAllowed),
        any(FieldsProjectionRoot.class), eq("Search test fields")))
        .thenReturn(List.of(field2));

    List<FieldJson> singleTestField = fieldService.searchFields("F2", "Search test fields");
    assertThat(singleTestField).containsExactly(field2Json);
  }


  @Test
  void getField_fieldExists() {
    when(fieldApi.findFieldById(eq(field1.getFieldId()), any(FieldProjectionRoot.class), eq("Field service test")))
        .thenReturn(Optional.of(field1));

    var fieldJsonOptional = fieldService.getField(field1.getFieldId(), "Field service test");
    assertThat(fieldJsonOptional.get()).isEqualTo(field1Json);
  }

  @Test
  void getField_fieldNotExists() {
    when(fieldApi.findFieldById(eq(0), any(FieldProjectionRoot.class), eq("Field service test")))
        .thenReturn(Optional.empty());

    var fieldJsonOptional = fieldService.getField(0, "Field service test");
    assertThat(fieldJsonOptional).isEqualTo(Optional.empty());
  }

  @Test
  void getFieldOrError_fieldExists() {
    when(fieldApi.findFieldById(eq(field1.getFieldId()), any(FieldProjectionRoot.class), eq("Field service test")))
        .thenReturn(Optional.of(field1));

    var fieldJson = fieldService.getFieldOrError(field1.getFieldId(), "Field service test");
    assertThat(fieldJson).isEqualTo(field1Json);
  }

  @Test
  void getFieldOrError_fieldNotExists() {
    when(fieldApi.findFieldById(eq(0), any(FieldProjectionRoot.class), eq("Field service test")))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> fieldService.getFieldOrError(0, "Field service test"))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Field not found for field id 0");
  }

}
