package uk.co.nstauthority.fieldconsents.organisations;

import java.util.Collection;
import java.util.List;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.client.organisation.OrganisationApi;
import uk.co.fivium.energyportalapi.generated.client.OrganisationUnitsProjectionRoot;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.teams.Role;
import uk.co.nstauthority.fieldconsents.teams.TeamQueryService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@Service
public class OrganisationUnitSearchService {

  private final OrganisationApi organisationApi;
  private final OrganisationUnitPermissionService organisationUnitPermissionService;
  private final TeamQueryService teamQueryService;

  OrganisationUnitSearchService(
      OrganisationApi organisationApi,
      OrganisationUnitPermissionService organisationUnitPermissionService,
      TeamQueryService teamQueryService
  ) {
    this.organisationApi = organisationApi;
    this.organisationUnitPermissionService = organisationUnitPermissionService;
    this.teamQueryService = teamQueryService;
  }

  public List<OrganisationUnitJson> searchOrganisationUnitsForUser(
      String searchTerm,
      String purpose,
      ServiceUserDetail user,
      Collection<Role> requiredRoles
  ) {
    var requestPurpose = new RequestPurpose(purpose);
    var requestedFields = new OrganisationUnitsProjectionRoot()
        .organisationUnitId().name();

    var organisationUnitJsons = organisationApi.searchOrganisationUnits(searchTerm, requestedFields, requestPurpose)
        .stream()
        .map(OrganisationUnitJson::from)
        .toList();

    if (teamQueryService.userHasAtLeastOneStaticRole(user, TeamType.REGULATOR, requiredRoles)) {
      return organisationUnitJsons;
    }

    if (teamQueryService.userHasAtLeastOneStaticRole(user, TeamType.CONSULTEE, requiredRoles)) {
      return organisationUnitJsons;
    }

    var organisationUnitIdsUserHasPermissionFor =
        organisationUnitPermissionService.getOperatorsUserHasRoleFor(user, requiredRoles)
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
