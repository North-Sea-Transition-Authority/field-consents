package uk.co.nstauthority.fieldconsents.organisations;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.client.organisation.OrganisationApi;
import uk.co.fivium.energyportalapi.generated.client.OrganisationUnitProjectionRoot;
import uk.co.fivium.energyportalapi.generated.client.OrganisationUnitsProjectionRoot;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.PermissionService;
import uk.co.nstauthority.fieldconsents.energyportal.organisationgroup.OrganisationGroupQueryService;
import uk.co.nstauthority.fieldconsents.teams.Team;
import uk.co.nstauthority.fieldconsents.teams.TeamService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@Service
public class OrganisationUnitService {

  private final OrganisationApi organisationApi;

  private final TeamService teamService;

  private final PermissionService permissionService;

  private final OrganisationGroupQueryService organisationGroupQueryService;

  @Autowired
  public OrganisationUnitService(OrganisationApi organisationApi,
                                 TeamService teamService,
                                 PermissionService permissionService,
                                 OrganisationGroupQueryService organisationGroupQueryService) {
    this.organisationApi = organisationApi;
    this.teamService = teamService;
    this.permissionService = permissionService;
    this.organisationGroupQueryService = organisationGroupQueryService;
  }

  public List<OrganisationUnitJson> searchOrganisationUnitsForUser(String searchTerm,
                                                                   String purpose,
                                                                   ServiceUserDetail user,
                                                                   RolePermission... requiredPermissions) {
    var requiredPermissionsSet = Set.of(requiredPermissions);

    var userRegulatorTeamsWithPermission =
        teamService.getTeamsOfTypeThatUserHasPermissionFor(user, TeamType.REGULATOR, requiredPermissionsSet);

    var requestPurpose = new RequestPurpose(purpose);
    var requestedFields = new OrganisationUnitsProjectionRoot()
        .organisationUnitId().name();

    var organisationUnitJsons = organisationApi.searchOrganisationUnits(searchTerm, requestedFields, requestPurpose)
        .stream()
        .map(OrganisationUnitJson::from)
        .toList();

    if (!userRegulatorTeamsWithPermission.isEmpty()) {
      return organisationUnitJsons;
    }

    var organisationUnitIdsUserHasPermissionFor =
        getOperatorsUserHasPermissionsFor(user, requiredPermissionsSet)
            .stream()
            .map(OrganisationUnitJson::organisationUnitId)
            .toList();

    return organisationUnitJsons
        .stream()
        .filter(organisationUnitJson ->
            organisationUnitIdsUserHasPermissionFor.contains(organisationUnitJson.organisationUnitId()))
        .toList();
  }

  public Optional<OrganisationUnitJson> findOrganisationUnitById(Integer organisationUnitId, String purpose) {
    var requestPurpose = new RequestPurpose(purpose);
    var requestedFields = new OrganisationUnitProjectionRoot()
        .organisationUnitId()
        .name();

    return organisationApi.findOrganisationUnit(organisationUnitId, requestedFields, requestPurpose)
        .map(OrganisationUnitJson::from);
  }

  public OrganisationUnitJson getOrganisationUnitById(Integer organisationUnitId, String purpose) {
    return findOrganisationUnitById(organisationUnitId, purpose)
        .orElseThrow(() -> new EntityNotFoundException("Organisation unit not found for id %s".formatted(organisationUnitId)));
  }

  public OrganisationUnitJson getOrganisationUnitByIdOrFallback(Integer organisationUnitId, String purpose,
                                                                String cachedOrganisationUnitName) {
    return findOrganisationUnitById(organisationUnitId, purpose)
        .orElseGet(() -> OrganisationUnitJson.fromCachedInformation(organisationUnitId, cachedOrganisationUnitName));
  }

  public Optional<OrganisationUnitWithGroupsJson> findOrganisationUnitWithGroupsById(Integer organisationUnitId, String purpose) {
    var requestPurpose = new RequestPurpose(purpose);
    var requestedFields = new OrganisationUnitProjectionRoot()
        .organisationUnitId()
        .name()
        .organisationGroups().organisationGroupId().name().root();

    return organisationApi.findOrganisationUnit(organisationUnitId, requestedFields, requestPurpose)
        .map(OrganisationUnitWithGroupsJson::from);
  }

  public OrganisationUnitWithGroupsJson getOrganisationUnitWithGroupsById(Integer organisationUnitId, String purpose) {
    return findOrganisationUnitWithGroupsById(organisationUnitId, purpose)
        .orElseThrow(() -> new EntityNotFoundException("Organisation unit not found for id %s".formatted(organisationUnitId)));
  }

  public List<OrganisationUnitJson> getOperatorsUserHasPermissionsFor(ServiceUserDetail user,
                                                                      Set<RolePermission> requiredPermissions) {

    var organisationGroupIds = teamService.getTeamsOfTypeThatUserBelongsTo(user, TeamType.INDUSTRY)
        .stream()
        .filter(team -> permissionService.hasPermissionForTeam(team, user, requiredPermissions))
        .map(Team::getOrganisationGroupId)
        .toList();

    return organisationGroupQueryService.getOrganisationUnitsByOrganisationGroupIds(organisationGroupIds);
  }
}
