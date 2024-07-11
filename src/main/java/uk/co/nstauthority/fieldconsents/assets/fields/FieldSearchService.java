package uk.co.nstauthority.fieldconsents.assets.fields;

import static uk.co.nstauthority.fieldconsents.assets.fields.FieldService.ALL_FIELD_STATUSES;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldService.FIELD_STATUSES_ALLOWED;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldService.fieldsProjectionRoot;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldService.fieldsWithOperatorsProjectionRoot;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.client.field.FieldApi;
import uk.co.fivium.energyportalapi.generated.client.FieldsProjectionRoot;
import uk.co.fivium.energyportalapi.generated.types.Field;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.FieldEquityPartnerPermissionService;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitJson;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitPermissionService;
import uk.co.nstauthority.fieldconsents.teams.TeamService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@Service
public class FieldSearchService {

  private final FieldApi fieldApi;
  private final TeamService teamService;
  private final OrganisationUnitPermissionService organisationUnitPermissionService;
  private final FieldEquityPartnerPermissionService fieldEquityPartnerPermissionService;
  private final ApplicationAssetService applicationAssetService;

  FieldSearchService(
      FieldApi fieldApi,
      TeamService teamService,
      OrganisationUnitPermissionService organisationUnitPermissionService,
      FieldEquityPartnerPermissionService fieldEquityPartnerPermissionService,
      ApplicationAssetService applicationAssetService
  ) {
    this.fieldApi = fieldApi;
    this.teamService = teamService;
    this.organisationUnitPermissionService = organisationUnitPermissionService;
    this.fieldEquityPartnerPermissionService = fieldEquityPartnerPermissionService;
    this.applicationAssetService = applicationAssetService;
  }

  public List<FieldJson> searchFields(String fieldName, String requestPurpose) {
    return searchFields(
        fieldName,
        fieldsProjectionRoot,
        new RequestPurpose(requestPurpose),
        FieldJson::from
    );
  }

  public List<FieldWithOperatorJson> searchFieldsWithOperatorForUser(
      String fieldName,
      String requestPurpose,
      ServiceUserDetail user
  ) {
    var fieldWithOperatorJsons = searchFields(
        fieldName,
        fieldsWithOperatorsProjectionRoot,
        new RequestPurpose(requestPurpose),
        FieldWithOperatorJson::from
    );

    var userRegulatorTeamsWithPermission =
        teamService.getTeamsOfTypeThatUserHasPermissionFor(user, TeamType.REGULATOR, RolePermission.VIEW_PERMISSIONS);

    // short circuit and return all found fields if the user is a regulator with view permissions
    if (!userRegulatorTeamsWithPermission.isEmpty()) {
      return fieldWithOperatorJsons;
    }

    var userConsulteeTeamsWithPermission =
        teamService.getTeamsOfTypeThatUserHasPermissionFor(user, TeamType.OPRED, RolePermission.VIEW_PERMISSIONS);

    // short circuit and return all found fields if the user is a consultee with view permissions
    if (!userConsulteeTeamsWithPermission.isEmpty()) {
      return fieldWithOperatorJsons;
    }

    // industry access

    var organisationUnitIdsUserHasPermissionFor =
        organisationUnitPermissionService.getOperatorsUserHasPermissionsFor(user, RolePermission.VIEW_PERMISSIONS)
            .stream()
            .map(OrganisationUnitJson::organisationUnitId)
            .toList();

    var fieldWithOperatorJsonsUserHasViewFcsPermissionForInOperatorTeam = fieldWithOperatorJsons
        .stream()
        .filter(field -> field.operatorExists()
            && organisationUnitIdsUserHasPermissionFor.contains(field.getOperatorJson().organisationUnitId()))
        .toList();

    var remainingFieldIds = fieldWithOperatorJsons
        .stream()
        .filter(fieldWithOperatorJson ->
            !fieldWithOperatorJsonsUserHasViewFcsPermissionForInOperatorTeam.contains(fieldWithOperatorJson))
        .map(FieldWithOperatorJson::getId)
        .toList();

    if (remainingFieldIds.isEmpty()) {
      return fieldWithOperatorJsonsUserHasViewFcsPermissionForInOperatorTeam;
    }

    // industry access - find addition fields user has view consent permission in a FEP team

    var fieldIdsUserHasViewFcsPermissionForInFieldEquityPartnerTeam =
        fieldEquityPartnerPermissionService
            .getFieldIdsUserHasPermissionForInFieldEquityPartnerTeam(
                user, remainingFieldIds, Set.of(RolePermission.VIEW_FCS_CONSENTS));

    return fieldWithOperatorJsons
        .stream()
        .filter(field -> (
            fieldWithOperatorJsonsUserHasViewFcsPermissionForInOperatorTeam.contains(field)
            || fieldIdsUserHasViewFcsPermissionForInFieldEquityPartnerTeam.contains(field.getId())
            )
        )
        .toList();
  }

  private <T> List<T> searchFields(
      String fieldName,
      FieldsProjectionRoot query,
      RequestPurpose requestPurpose,
      Function<Field, T> mappingFunction
  ) {
    // search over fields with the allowed statuses
    var fieldsWithAllowedStatuses = fieldApi.searchFields(fieldName, FIELD_STATUSES_ALLOWED, query, requestPurpose);
    var fieldsIdsWithAllowedStatuses = fieldsWithAllowedStatuses.stream().map(Field::getFieldId).toList();

    // find any other FCS in use fields which don't currently have one of the allowed statuses
    var otherInUseFieldIds = applicationAssetService.getAllUniqueAssetIdsForAssetType(AssetType.FIELD)
        .stream()
        .filter(fieldId -> !fieldsIdsWithAllowedStatuses.contains(fieldId))
        .toList();

    if (otherInUseFieldIds.isEmpty()) {
      return fieldsWithAllowedStatuses
          .stream()
          .map(mappingFunction)
          .toList();
    }

    // search over the other FCS in use fields with any status
    var inUseFieldsWithAnyStatus = fieldApi
        .searchFields(fieldName, ALL_FIELD_STATUSES, otherInUseFieldIds, query, requestPurpose, null);

    return Stream.concat(fieldsWithAllowedStatuses.stream(), inUseFieldsWithAnyStatus.stream())
        .map(mappingFunction)
        .toList();
  }
}
