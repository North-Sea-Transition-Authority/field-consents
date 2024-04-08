package uk.co.nstauthority.fieldconsents.organisations;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.PermissionService;
import uk.co.nstauthority.fieldconsents.teams.TeamService;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@Service
public class OrganisationUnitPermissionService {

  private static final Logger LOGGER = LoggerFactory.getLogger(OrganisationUnitPermissionService.class);

  private final TeamService teamService;

  private final OrganisationUnitService organisationUnitService;

  private final PermissionService permissionService;

  OrganisationUnitPermissionService(TeamService teamService,
                                    OrganisationUnitService organisationUnitService,
                                    PermissionService permissionService) {
    this.teamService = teamService;
    this.organisationUnitService = organisationUnitService;
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
}
