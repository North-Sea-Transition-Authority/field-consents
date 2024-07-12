package uk.co.nstauthority.fieldconsents.assets.fields;

import jakarta.persistence.EntityNotFoundException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.client.field.FieldApi;
import uk.co.fivium.energyportalapi.generated.client.FieldProjectionRoot;
import uk.co.fivium.energyportalapi.generated.client.FieldsProjectionRoot;
import uk.co.fivium.energyportalapi.generated.types.FieldStatus;

@Service
public class FieldService {

  public static final String FIELD_NOT_FOUND = "Field not found for field id %s";

  // this status list has been taken from the DEVUK fields search
  // screen, we need to understand what these mean
  public static final List<FieldStatus> FIELD_STATUSES_ALLOWED =
      List.of(FieldStatus.STATUS500, FieldStatus.STATUS600, FieldStatus.STATUS700,
          FieldStatus.STATUS799, FieldStatus.STATUS800, FieldStatus.STATUS899);

  public static final String FIELD_STATUSES_ALLOWED_VALIDATION_MESSAGE
      = "does not have a valid 'producing' status";

  static final List<FieldStatus> ALL_FIELD_STATUSES = EnumSet.allOf(FieldStatus.class).stream().toList();

  static final FieldsProjectionRoot fieldsProjectionRoot =
      new FieldsProjectionRoot()
          .fieldId()
          .fieldName()
          .status().root()
          .statusDisplayName()
          .geographicArea().root()
          .geographicAreaDisplayName()
          .shore().root()
          .shoreDisplayName();

  static final FieldProjectionRoot fieldProjectionRoot =
      new FieldProjectionRoot()
          .fieldId()
          .fieldName()
          .status().root()
          .statusDisplayName()
          .geographicArea().root()
          .geographicAreaDisplayName()
          .shore().root()
          .shoreDisplayName();

  static final FieldsProjectionRoot fieldsWithOperatorsProjectionRoot =
      fieldsProjectionRoot
          .fieldOperator().organisationUnitId().name().root();

  static final FieldProjectionRoot fieldWithOperatorProjectionRoot =
      fieldProjectionRoot
          .fieldOperator().organisationUnitId().name().root();

  static final FieldProjectionRoot fieldWithOperatorLicencesProjectionRoot =
      fieldWithOperatorProjectionRoot
          .licences().id().licenceRef().root();

  static final FieldsProjectionRoot fieldsWithOperatorsAndLicensesProjectionRoot =
      fieldsWithOperatorsProjectionRoot
          .licences().id().licenceRef().root();

  private final FieldApi fieldApi;

  FieldService(FieldApi fieldApi) {
    this.fieldApi = fieldApi;
  }

  public Optional<FieldJson> findField(Integer fieldId, String requestPurpose) {
    return fieldApi.findFieldById(fieldId, fieldProjectionRoot, new RequestPurpose(requestPurpose))
        .map(FieldJson::from);
  }

  public FieldJson getField(Integer fieldId, String requestPurpose) {
    return findField(fieldId, requestPurpose)
        .orElseThrow(() -> new EntityNotFoundException(FIELD_NOT_FOUND.formatted(fieldId)));
  }

  public List<FieldJson> findFieldsByIds(List<Integer> fieldIds, String requestPurpose) {
    if (fieldIds.isEmpty()) {
      return Collections.emptyList();
    }

    return fieldApi.getFieldsByIds(fieldIds, fieldsProjectionRoot, new RequestPurpose(requestPurpose))
        .stream()
        .map(FieldJson::from)
        .toList();
  }

  public Optional<FieldWithOperatorJson> findFieldWithOperator(Integer fieldId, String requestPurpose) {
    return fieldApi.findFieldById(fieldId, fieldWithOperatorProjectionRoot, new RequestPurpose(requestPurpose))
        .map(FieldWithOperatorJson::from);
  }

  public FieldWithOperatorJson getFieldWithOperator(Integer fieldId, String requestPurpose) {
    return findFieldWithOperator(fieldId, requestPurpose)
        .orElseThrow(() -> new EntityNotFoundException(FIELD_NOT_FOUND.formatted(fieldId)));
  }

  public Optional<FieldWithOperatorAndLicencesJson> findFieldWithOperatorAndLicences(Integer fieldId,
                                                                                     String requestPurpose) {
    return fieldApi.findFieldById(fieldId, fieldWithOperatorLicencesProjectionRoot, new RequestPurpose(requestPurpose))
        .map(FieldWithOperatorAndLicencesJson::from);
  }

  public List<FieldWithOperatorAndLicencesJson> findFieldsWithOperatorAndLicences(List<Integer> fieldIds,
                                                                                  String epaRequestPurpose) {
    if (fieldIds.isEmpty()) {
      return Collections.emptyList();
    }

    return fieldApi
        .getFieldsByIds(fieldIds, fieldsWithOperatorsAndLicensesProjectionRoot, new RequestPurpose(epaRequestPurpose))
        .stream()
        .map(FieldWithOperatorAndLicencesJson::from)
        .toList();
  }

  public FieldWithOperatorAndLicencesJson getFieldWithOperatorAndLicences(Integer fieldId, String requestPurpose) {
    return findFieldWithOperatorAndLicences(fieldId, requestPurpose)
        .orElseThrow(() -> new EntityNotFoundException(FIELD_NOT_FOUND.formatted(fieldId)));
  }

}
