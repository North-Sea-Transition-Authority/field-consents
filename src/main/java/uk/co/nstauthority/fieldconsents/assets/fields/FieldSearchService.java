package uk.co.nstauthority.fieldconsents.assets.fields;

import static uk.co.nstauthority.fieldconsents.assets.fields.FieldService.fieldStatusesAllowed;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldService.fieldsProjectionRoot;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldService.fieldsWithOperatorsProjectionRoot;

import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.client.field.FieldApi;
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

  FieldSearchService(
      FieldApi fieldApi,
      TeamService teamService,
      OrganisationUnitPermissionService organisationUnitPermissionService,
      FieldEquityPartnerPermissionService fieldEquityPartnerPermissionService
  ) {
    this.fieldApi = fieldApi;
    this.teamService = teamService;
    this.organisationUnitPermissionService = organisationUnitPermissionService;
    this.fieldEquityPartnerPermissionService = fieldEquityPartnerPermissionService;
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

  public List<FieldWithOperatorJson> searchFieldsWithOperatorForUser(
      String fieldName,
      String requestPurpose,
      ServiceUserDetail user
  ) {
    var fieldWithOperatorJsons = fieldApi.searchFields(
            fieldName,
            fieldStatusesAllowed,
            fieldsWithOperatorsProjectionRoot,
            new RequestPurpose(requestPurpose)
        )
        .stream()
        .map(FieldWithOperatorJson::from)
        .toList();

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
}
