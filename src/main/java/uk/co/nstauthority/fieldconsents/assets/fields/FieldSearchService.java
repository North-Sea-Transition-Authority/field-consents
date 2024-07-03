package uk.co.nstauthority.fieldconsents.assets.fields;

import static uk.co.nstauthority.fieldconsents.assets.fields.FieldService.ALL_FIELD_STATUSES;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldService.FIELD_STATUSES_ALLOWED;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldService.fieldsProjectionRoot;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldService.fieldsWithOperatorsProjectionRoot;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
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

    var organisationUnitIdsUserHasPermissionFor =
        organisationUnitPermissionService.getOperatorsUserHasPermissionsFor(user, RolePermission.VIEW_PERMISSIONS)
            .stream()
            .map(OrganisationUnitJson::organisationUnitId)
            .toList();

    var fieldIds = fieldWithOperatorJsons.stream()
        .map(FieldWithOperatorJson::getId)
        .toList();

    var fieldIdsUserHasViewFcsPermissionForInFieldEquityPartnerTeam = fieldEquityPartnerPermissionService
        .getFieldIdsUserHasPermissionForInFieldEquityPartnerTeam(user, fieldIds, Set.of(RolePermission.VIEW_FCS_CONSENTS));

    return fieldWithOperatorJsons
        .stream()
        .filter(field ->
            (field.operatorExists()
                && organisationUnitIdsUserHasPermissionFor.contains(field.getOperatorJson().organisationUnitId()))
            || fieldIdsUserHasViewFcsPermissionForInFieldEquityPartnerTeam.contains(field.getId())
        )
        .toList();
  }

  private <T> List<T> searchFields(
      String fieldName,
      FieldsProjectionRoot query,
      RequestPurpose requestPurpose,
      Function<Field, T> mappingFunction
  ) {
    var inUseFieldIds = applicationAssetService.getAllUniqueAssetIdsForAssetType(AssetType.FIELD);
    return fieldApi.searchFields(fieldName, ALL_FIELD_STATUSES, query, requestPurpose)
        .stream()
        .filter(field -> inUseFieldIds.contains(field.getFieldId()) || FIELD_STATUSES_ALLOWED.contains(field.getStatus()))
        .map(mappingFunction)
        .toList();
  }
}
