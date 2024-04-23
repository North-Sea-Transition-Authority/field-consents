package uk.co.nstauthority.fieldconsents.organisations;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
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

  private final TeamService teamService;
  private final OrganisationUnitService organisationUnitService;
  private final OrganisationGroupQueryService organisationGroupQueryService;
  private final PermissionService permissionService;

  OrganisationUnitPermissionService(
      TeamService teamService,
      OrganisationUnitService organisationUnitService,
      OrganisationGroupQueryService organisationGroupQueryService,
      PermissionService permissionService
  ) {
    this.teamService = teamService;
    this.organisationUnitService = organisationUnitService;
    this.organisationGroupQueryService = organisationGroupQueryService;
    this.permissionService = permissionService;
  }


  public boolean hasOperatorPermission(
      ServiceUserDetail user,
      Integer operatorOuId,
      RolePermission... requiredPermissions
  ) {
    return hasOperatorPermission(user, operatorOuId, Set.of(requiredPermissions));
  }

  public boolean hasOperatorPermission(
      ServiceUserDetail user,
      Integer operatorOuId,
      Set<RolePermission> requiredPermissions
  ) {
    return CollectionUtils.containsAny(getUserPermissionsForOperator(user, operatorOuId), requiredPermissions);
  }

  public Set<RolePermission> getUserPermissionsForOperator(ServiceUserDetail user,
                                                           Integer operatorOuId) {
    var organisationUnitWithGroups = organisationUnitService.getOrganisationUnitWithGroupsById(
        operatorOuId,
        "Lookup organisation unit with groups for application security lookup"
    );

    // if the operator doesn't have an organisation group then no user has any permissions
    if (organisationUnitWithGroups.organisationGroups().isEmpty()) {
      return Collections.emptySet();
    }

    var userRolePermissions = new HashSet<RolePermission>();

    for (var orgGroup : organisationUnitWithGroups.organisationGroups()) {
      var teamOptional = teamService.getTeamByOrganisationGroupId(orgGroup.getOrganisationGroupId());
      if (teamOptional.isEmpty()) {
        continue;
      }
      userRolePermissions.addAll(permissionService.getUserPermissionsForTeam(teamOptional.get(), user));
    }

    return userRolePermissions;
  }

  public List<OrganisationUnitJson> getOperatorsUserHasPermissionsFor(
      ServiceUserDetail user,
      Set<RolePermission> requiredPermissions
  ) {
    var organisationGroupIds = teamService.getTeamsOfTypeThatUserBelongsTo(user, TeamType.INDUSTRY)
        .stream()
        .filter(team -> permissionService.hasPermissionForTeam(team, user, requiredPermissions))
        .map(Team::getOrganisationGroupId)
        .toList();

    return organisationGroupQueryService.getOrganisationUnitsByOrganisationGroupIds(organisationGroupIds);
  }
}
