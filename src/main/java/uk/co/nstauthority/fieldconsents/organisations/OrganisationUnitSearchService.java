package uk.co.nstauthority.fieldconsents.organisations;

import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.client.organisation.OrganisationApi;
import uk.co.fivium.energyportalapi.generated.client.OrganisationUnitsProjectionRoot;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.teams.TeamService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@Service
public class OrganisationUnitSearchService {

  private final OrganisationApi organisationApi;
  private final OrganisationUnitPermissionService organisationUnitPermissionService;
  private final TeamService teamService;

  OrganisationUnitSearchService(
      OrganisationApi organisationApi,
      OrganisationUnitPermissionService organisationUnitPermissionService,
      TeamService teamService
  ) {
    this.organisationApi = organisationApi;
    this.organisationUnitPermissionService = organisationUnitPermissionService;
    this.teamService = teamService;
  }

  public List<OrganisationUnitJson> searchOrganisationUnitsForUser(
      String searchTerm,
      String purpose,
      ServiceUserDetail user,
      RolePermission... requiredPermissions
  ) {
    var requiredPermissionsSet = Set.of(requiredPermissions);

    var requestPurpose = new RequestPurpose(purpose);
    var requestedFields = new OrganisationUnitsProjectionRoot()
        .organisationUnitId().name();

    var organisationUnitJsons = organisationApi.searchOrganisationUnits(searchTerm, requestedFields, requestPurpose)
        .stream()
        .map(OrganisationUnitJson::from)
        .toList();

    var userRegulatorTeamsWithPermission =
        teamService.getTeamsOfTypeThatUserHasPermissionFor(user, TeamType.REGULATOR, requiredPermissionsSet);

    // short circuit and return all org units found is the user is a regulator with the required permissions
    if (!userRegulatorTeamsWithPermission.isEmpty()) {
      return organisationUnitJsons;
    }

    var userConsulteeTeamsWithPermission =
        teamService.getTeamsOfTypeThatUserHasPermissionFor(user, TeamType.OPRED, requiredPermissionsSet);

    // short circuit and return all org units found is the user is a consultee with the required permissions
    if (!userConsulteeTeamsWithPermission.isEmpty()) {
      return organisationUnitJsons;
    }

    var organisationUnitIdsUserHasPermissionFor =
        organisationUnitPermissionService.getOperatorsUserHasPermissionsFor(user, requiredPermissionsSet)
            .stream()
            .map(OrganisationUnitJson::organisationUnitId)
            .toList();

    return organisationUnitJsons
        .stream()
        .filter(organisationUnitJson ->
            organisationUnitIdsUserHasPermissionFor.contains(organisationUnitJson.organisationUnitId()))
        .toList();
  }
}
