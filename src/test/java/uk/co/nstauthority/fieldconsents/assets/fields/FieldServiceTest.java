package uk.co.nstauthority.fieldconsents.assets.fields;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldService.fieldsWithOperatorsAndLicensesProjectionRoot;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1Json;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1JsonWithOperator;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1JsonWithOperatorAndLicences;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1WithOperator;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1WithOperatorAndLicences;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field2;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field2Json;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field2JsonWithOperatorAndLicences;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field2WithOperatorAndLicences;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field3;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field3Json;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.client.field.FieldApi;
import uk.co.fivium.energyportalapi.generated.client.FieldProjectionRoot;

@ExtendWith(MockitoExtension.class)
public class FieldServiceTest {

  private static final String REQUEST_PURPOSE = "Field service test";

  @Mock
  private FieldApi fieldApi;

  @InjectMocks
  private FieldService fieldService;

  private final RequestPurpose requestPurpose = new RequestPurpose(REQUEST_PURPOSE);

  @Test
  void findField_fieldExists() {
    when(fieldApi.findFieldById(eq(field1.getFieldId()), any(FieldProjectionRoot.class), any(RequestPurpose.class)))
        .thenReturn(Optional.of(field1));

    var fieldJsonOptional = fieldService.findField(field1.getFieldId(), REQUEST_PURPOSE);
    assertThat(fieldJsonOptional).usingRecursiveComparison()
        .isEqualTo(Optional.of(field1Json));
  }

  @Test
  void findFieldWithOperator_fieldExists() {
    when(fieldApi.findFieldById(eq(field1WithOperator.getFieldId()), any(FieldProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(Optional.of(field1WithOperator));

    var fieldJsonOptional = fieldService.findFieldWithOperator(field1WithOperator.getFieldId(), REQUEST_PURPOSE);
    assertThat(fieldJsonOptional).usingRecursiveComparison()
        .isEqualTo(Optional.of(field1JsonWithOperator));
  }

  @Test
  void findField_fieldNotExists() {
    when(fieldApi.findFieldById(eq(0), any(FieldProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(Optional.empty());

    var fieldJsonOptional = fieldService.findField(0, REQUEST_PURPOSE);
    assertThat(fieldJsonOptional).isEqualTo(Optional.empty());
  }

  @Test
  void findFieldWithOperator_fieldNotExists() {
    when(fieldApi.findFieldById(eq(0), any(FieldProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(Optional.empty());

    var fieldJsonOptional = fieldService.findFieldWithOperator(0, REQUEST_PURPOSE);
    assertThat(fieldJsonOptional).isNotPresent();
  }

  @Test
  void getField_fieldExists() {
    when(fieldApi.findFieldById(eq(field1.getFieldId()), any(FieldProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(Optional.of(field1));

    var fieldJson = fieldService.getField(field1.getFieldId(), REQUEST_PURPOSE);
    assertThat(fieldJson).usingRecursiveComparison()
        .isEqualTo(field1Json);
  }

  @Test
  void getFieldWithOperator_fieldExists() {
    when(fieldApi.findFieldById(eq(field1WithOperator.getFieldId()), any(FieldProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(Optional.of(field1WithOperator));

    var fieldJson = fieldService.getFieldWithOperator(field1WithOperator.getFieldId(), REQUEST_PURPOSE);
    assertThat(fieldJson).usingRecursiveComparison()
        .isEqualTo(field1JsonWithOperator);
  }

  @Test
  void getField_fieldNotExists() {
    when(fieldApi.findFieldById(eq(0), any(FieldProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> fieldService.getField(0, REQUEST_PURPOSE))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining(FieldService.FIELD_NOT_FOUND.formatted(0));
  }

  @Test
  void getFieldWithOperator_fieldNotExists() {
    when(fieldApi.findFieldById(eq(0), any(FieldProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> fieldService.getFieldWithOperator(0, REQUEST_PURPOSE))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining(FieldService.FIELD_NOT_FOUND.formatted(0));
  }

  @Test
  void findFieldsByIds_noIds() {
    assertThat(fieldService.findFieldsByIds(Collections.emptyList(), "")).isEmpty();
    verifyNoInteractions(fieldApi);
  }

  @Test
  void findFieldsByIds() {
    var ids = List.of(1, 2, 3);
    var requestPurpose = new RequestPurpose("request purpose");

    when(fieldApi.getFieldsByIds(ids, FieldService.fieldsProjectionRoot, requestPurpose)).thenReturn(List.of(field1, field2, field3));

    assertThat(fieldService.findFieldsByIds(ids, requestPurpose.purpose()))
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactly(field1Json, field2Json, field3Json);
  }

  @Test
  void findFieldWithOperatorAndLicences_fieldExists() {
    when(fieldApi.findFieldById(eq(field1WithOperatorAndLicences.getFieldId()), any(FieldProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(Optional.of(field1WithOperatorAndLicences));

    var fieldJsonOptional = fieldService.findFieldWithOperatorAndLicences(field1WithOperatorAndLicences.getFieldId(), REQUEST_PURPOSE);
    assertThat(fieldJsonOptional).usingRecursiveComparison()
        .isEqualTo(Optional.of(field1JsonWithOperatorAndLicences));
  }

  @Test
  void findFieldWithOperatorAndLicences_fieldNotExists() {
    when(fieldApi.findFieldById(eq(0), any(FieldProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(Optional.empty());

    var fieldJsonOptional = fieldService.findFieldWithOperatorAndLicences(0, REQUEST_PURPOSE);
    assertThat(fieldJsonOptional).isNotPresent();
  }

  @Test
  void findFieldsWithOperatorAndLicences_noIds() {
    assertThat(fieldService.findFieldsWithOperatorAndLicences(Collections.emptyList(), "")).isEmpty();
    verifyNoInteractions(fieldApi);
  }

  @Test
  void findFieldsWithOperatorAndLicences() {
    var ids = List.of(1, 2);
    var requestPurpose = new RequestPurpose("request purpose");

    when(fieldApi.getFieldsByIds(ids, fieldsWithOperatorsAndLicensesProjectionRoot, requestPurpose))
        .thenReturn(List.of(field1WithOperatorAndLicences, field2WithOperatorAndLicences));

    assertThat(fieldService.findFieldsWithOperatorAndLicences(ids, requestPurpose.purpose()))
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactly(field1JsonWithOperatorAndLicences, field2JsonWithOperatorAndLicences);
  }

  @Test
  void getFieldWithOperatorAndLicences_fieldExists() {
    when(fieldApi.findFieldById(eq(field1WithOperatorAndLicences.getFieldId()), any(FieldProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(Optional.of(field1WithOperatorAndLicences));

    var fieldJson = fieldService.getFieldWithOperatorAndLicences(field1WithOperatorAndLicences.getFieldId(), REQUEST_PURPOSE);
    assertThat(fieldJson).usingRecursiveComparison()
        .isEqualTo(field1JsonWithOperatorAndLicences);
  }

  @Test
  void getFieldWithOperatorAndLicences_fieldNotExists() {
    when(fieldApi.findFieldById(eq(0), any(FieldProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> fieldService.getFieldWithOperatorAndLicences(0, REQUEST_PURPOSE))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining(FieldService.FIELD_NOT_FOUND.formatted(0));
  }
}
