package uk.co.nstauthority.fieldconsents.assets.fields;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class FieldService {

  public List<FieldJson> getAllFields() {
    return FieldData.fields;
  }

  public Optional<FieldJson> getField(Integer fieldId) {
    return FieldData.fields.stream().filter(fieldJson -> fieldJson.fieldId().equals(fieldId)).findFirst();
  }

  public FieldJson getFieldOrError(Integer fieldId) {
    return getField(fieldId)
        .orElseThrow(() -> new RuntimeException("Field not found for field id %s".formatted(fieldId)));
  }

}
