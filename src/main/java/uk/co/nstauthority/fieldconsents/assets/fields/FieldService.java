package uk.co.nstauthority.fieldconsents.assets.fields;

import java.util.List;
import java.util.Optional;
import javax.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalapi.client.field.FieldApi;
import uk.co.fivium.energyportalapi.generated.client.FieldProjectionRoot;
import uk.co.fivium.energyportalapi.generated.client.FieldsProjectionRoot;
import uk.co.fivium.energyportalapi.generated.types.Field;
import uk.co.fivium.energyportalapi.generated.types.FieldStatus;

@Service
public class FieldService {

  private final FieldApi fieldApi;

  // this status list has been taken from the DEVUK fields search
  // screen, we need to understand what these mean
  static final List<FieldStatus> fieldStatusesAllowed =
      List.of(FieldStatus.STATUS500, FieldStatus.STATUS600, FieldStatus.STATUS700,
          FieldStatus.STATUS799, FieldStatus.STATUS800, FieldStatus.STATUS899);

  static final FieldsProjectionRoot fieldsProjectionRoot = new FieldsProjectionRoot().fieldName().fieldId();

  static final FieldProjectionRoot fieldProjectionRoot = new FieldProjectionRoot().fieldName().fieldId();

  static final FieldsProjectionRoot fieldsWithOperatorsProjectionRoot =
      new FieldsProjectionRoot().fieldName().fieldId().fieldOperator().organisationUnitId().name().root();

  static final FieldProjectionRoot fieldWithOperatorProjectionRoot =
      new FieldProjectionRoot().fieldName().fieldId().fieldOperator().organisationUnitId().name().root();

  @Autowired
  public FieldService(FieldApi fieldApi) {
    this.fieldApi = fieldApi;
  }

  public List<FieldJson> searchFields(String fieldName, String requestPurpose) {
    return fieldApi.searchFields(fieldName, fieldStatusesAllowed,
            fieldsProjectionRoot,
            requestPurpose)
        .stream()
        .map(this::convertFieldToFieldJson)
        .toList();
  }

  public Optional<FieldJson> findField(Integer fieldId, String requestPurpose) {
    return fieldApi.findFieldById(fieldId, fieldProjectionRoot, requestPurpose)
        .map(this::convertFieldToFieldJson);
  }

  public List<FieldJson> searchFieldsWithOperator(String fieldName, String requestPurpose) {
    return fieldApi.searchFields(fieldName, fieldStatusesAllowed,
            fieldsWithOperatorsProjectionRoot,
            requestPurpose)
        .stream()
        .map(this::convertFieldToFieldJson)
        .toList();
  }

  public Optional<FieldJson> findFieldWithOperator(Integer fieldId, String requestPurpose) {
    return fieldApi.findFieldById(fieldId, fieldWithOperatorProjectionRoot, requestPurpose)
        .map(this::convertFieldToFieldJson);
  }

  public FieldJson getFieldWithOperator(Integer fieldId, String requestPurpose) {
    return findFieldWithOperator(fieldId, requestPurpose)
        .orElseThrow(() -> new EntityNotFoundException("Field not found for field id %s".formatted(fieldId)));
  }

  public FieldJson getField(Integer fieldId, String requestPurpose) {
    return findField(fieldId, requestPurpose)
        .orElseThrow(() -> new EntityNotFoundException("Field not found for field id %s".formatted(fieldId)));
  }

  private FieldJson convertFieldToFieldJson(Field field) {
    return new FieldJson(
        field.getFieldId(),
        field.getFieldName(),
        field.getFieldOperator() != null ? field.getFieldOperator().getOrganisationUnitId() : null,
        field.getFieldOperator() != null ? field.getFieldOperator().getName() : null
    );
  }
}
