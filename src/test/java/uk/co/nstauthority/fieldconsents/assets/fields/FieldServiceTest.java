package uk.co.nstauthority.fieldconsents.assets.fields;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class FieldServiceTest {

//  FieldJson field1 = new FieldJson(1, "F1");
//  FieldJson field2 = new FieldJson(2, "F2");
//  FieldJson field3 = new FieldJson(3, "F3");

  FieldJson affleck = new FieldJson(1164, "AFFLECK");
  FieldJson brent = new FieldJson(895, "BRENT");
  FieldJson cawdor = new FieldJson(6472, "CAWDOR");

  FieldService fieldService;

  @BeforeEach
  void setup() {
    fieldService = new FieldService();
    // TODO need to mock whatever calls FieldService is using, but can't at the moment as it's dummy data
    //when(FieldData.fields).thenReturn(List.of(field1, field2, field3));
  }

  @Test
  void getAllFields_allFields() {
    List<FieldJson> allFields = fieldService.getAllFields();
    assertThat(allFields).contains(affleck, brent, cawdor);
    // TODO change to containsExactly when I can mock correctly assertThat(allFields).containsExactly(field1, field2, field3);
  }

  @Test
  void getField_fieldExists() {
    var fieldJsonOptional = fieldService.getField(brent.fieldId());
    assertThat(fieldJsonOptional.get()).isEqualTo(brent);

  }

  @Test
  void getField_fieldNotExists() {
    var fieldJsonOptional = fieldService.getField(0);
    assertThat(fieldJsonOptional).isEqualTo(Optional.empty());
  }

  @Test
  void getFieldOrError_fieldExists() {
    var fieldJson = fieldService.getFieldOrError(brent.fieldId());
    assertThat(fieldJson).isEqualTo(brent);
  }

  @Test
  void getFieldOrError_fieldNotExists() {
    assertThatThrownBy(() -> fieldService.getFieldOrError(0))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Field not found for field id 0");
  }

}
