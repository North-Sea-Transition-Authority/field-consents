package uk.co.nstauthority.fieldconsents.organisations;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.PermissionService;
import uk.co.nstauthority.fieldconsents.energyportal.organisationgroup.OrganisationGroupQueryService;
import uk.co.nstauthority.fieldconsents.teams.Team;
import uk.co.nstauthority.fieldconsents.teams.TeamService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@Service
public class OrganisationUnitPermissionService {

  private static final Logger LOGGER = LoggerFactory.getLogger(OrganisationUnitPermissionService.class);

  private final TeamService teamService;

  private final OrganisationUnitService organisationUnitService;

  private final OrganisationGroupQueryService organisationGroupQueryService;

  private final PermissionService permissionService;

  OrganisationUnitPermissionService(TeamService teamService,
                                    OrganisationUnitService organisationUnitService,
                                    OrganisationGroupQueryService organisationGroupQueryService,
                                    PermissionService permissionService) {
    this.teamService = teamService;
    this.organisationUnitService = organisationUnitService;
    this.organisationGroupQueryService = organisationGroupQueryService;
    this.permissionService = permissionService;
  }

  public boolean hasOperatorPermission(ServiceUserDetail user,
                                       Integer operatorOuId,
                                       Set<RolePermission> requiredPermissions) {
    var organisationUnitWithGroups = organisationUnitService.getOrganisationUnitWithGroupsById(
        operatorOuId,
        "Lookup organisation unit with groups for application security check"
    );

    // if the operator doesn't have an organisation group then no user has permission
    if (organisationUnitWithGroups.organisationGroups().isEmpty()) {
      LOGGER.warn("No organisation groups found for organisation unit id {}", operatorOuId);
      return false;
    }

    // loop over the organisation groups for the operator
    // (there is typically 1 but can be more, so we have to cater for this here)
    var requiredPermissionNames = requiredPermissions
        .stream()
        .map(RolePermission::name)
        .collect(Collectors.joining(","));

    for (var orgGroup: organisationUnitWithGroups.organisationGroups()) {
      var teamOptional = teamService.getTeamByOrganisationGroupId(orgGroup.getOrganisationGroupId());

      if (teamOptional.isEmpty()) {
        LOGGER.warn("Team not found for organisation group id {}", orgGroup.getOrganisationGroupId());
        continue;
      }

      if (permissionService.hasPermissionForTeam(teamOptional.get(), user, requiredPermissions)) {
        return true; // return as soon as we find a team the user has permissions in
      }

      LOGGER.info("""
              User {} attempted to access application or asset with responsible organisation group id {}. \
              User was expected to have at least one of the following permission(s): {}\
              """,
          user.wuaId(),
          orgGroup.getOrganisationGroupId(),
          requiredPermissionNames
      );
    }

    return false;
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
