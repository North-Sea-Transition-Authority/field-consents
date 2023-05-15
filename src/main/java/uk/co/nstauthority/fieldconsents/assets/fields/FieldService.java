package uk.co.nstauthority.fieldconsents.assets.fields;

import java.util.List;
import java.util.Optional;
import javax.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.client.field.FieldApi;
import uk.co.fivium.energyportalapi.generated.client.FieldProjectionRoot;
import uk.co.fivium.energyportalapi.generated.client.FieldsProjectionRoot;
import uk.co.fivium.energyportalapi.generated.types.FieldStatus;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitJson;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitService;
import uk.co.nstauthority.fieldconsents.teams.TeamService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@Service
public class FieldService {

  public static final String FIELD_NOT_FOUND = "Field not found for field id %s";

  private final FieldApi fieldApi;

  private final TeamService teamService;

  private final OrganisationUnitService organisationUnitService;

  // this status list has been taken from the DEVUK fields search
  // screen, we need to understand what these mean
  static final List<FieldStatus> fieldStatusesAllowed =
      List.of(FieldStatus.STATUS500, FieldStatus.STATUS600, FieldStatus.STATUS700,
          FieldStatus.STATUS799, FieldStatus.STATUS800, FieldStatus.STATUS899);

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

  @Autowired
  public FieldService(FieldApi fieldApi,
                      TeamService teamService,
                      OrganisationUnitService organisationUnitService) {
    this.fieldApi = fieldApi;
    this.teamService = teamService;
    this.organisationUnitService = organisationUnitService;
  }

  public List<FieldJson> searchFields(String fieldName, String requestPurpose) {
    return fieldApi.searchFields(fieldName,
            fieldStatusesAllowed,
            fieldsProjectionRoot,
            new RequestPurpose(requestPurpose))
        .stream()
        .map(FieldJson::from)
        .toList();
  }

  public Optional<FieldJson> findField(Integer fieldId, String requestPurpose) {
    return fieldApi.findFieldById(fieldId, fieldProjectionRoot, new RequestPurpose(requestPurpose))
        .map(FieldJson::from);
  }

  public FieldJson getField(Integer fieldId, String requestPurpose) {
    return findField(fieldId, requestPurpose)
        .orElseThrow(() -> new EntityNotFoundException(FIELD_NOT_FOUND.formatted(fieldId)));
  }

  public List<FieldWithOperatorJson> searchFieldsWithOperatorForUser(String fieldName,
                                                                     String requestPurpose,
                                                                     ServiceUserDetail user) {
    var userRegulatorTeamsWithPermission =
        teamService.getTeamsOfTypeThatUserHasPermissionFor(user, TeamType.REGULATOR, RolePermission.VIEW_PERMISSIONS);

    var fieldWithOperatorJsons = fieldApi.searchFields(
            fieldName,
            fieldStatusesAllowed,
            fieldsWithOperatorsProjectionRoot,
            new RequestPurpose(requestPurpose)
        )
        .stream()
        .map(FieldWithOperatorJson::from)
        .toList();

    if (!userRegulatorTeamsWithPermission.isEmpty()) {
      return fieldWithOperatorJsons;
    }

    var organisationUnitIdsUserHasPermissionFor =
        organisationUnitService.getOperatorsUserHasPermissionsFor(user, RolePermission.VIEW_PERMISSIONS)
            .stream()
            .map(OrganisationUnitJson::organisationUnitId)
            .toList();

    return fieldWithOperatorJsons
        .stream()
        .filter(field ->
            field.operatorExists()
            && organisationUnitIdsUserHasPermissionFor.contains(field.getOperatorJson().organisationUnitId()))
        .toList();
  }

  public List<FieldJson> findFieldsByIds(List<Integer> fieldIds, String requestPurpose) {
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

  public FieldWithOperatorAndLicencesJson getFieldWithOperatorAndLicences(Integer fieldId, String requestPurpose) {
    return findFieldWithOperatorAndLicences(fieldId, requestPurpose)
        .orElseThrow(() -> new EntityNotFoundException(FIELD_NOT_FOUND.formatted(fieldId)));
  }
}
