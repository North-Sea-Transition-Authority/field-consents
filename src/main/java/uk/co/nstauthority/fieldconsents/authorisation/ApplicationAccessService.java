package uk.co.nstauthority.fieldconsents.authorisation;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitPermissionService;
import uk.co.nstauthority.fieldconsents.teams.TeamService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@Service
public class ApplicationAccessService {

  private final OrganisationUnitPermissionService organisationUnitPermissionService;

  private final TeamService teamService;

  @Autowired
  ApplicationAccessService(OrganisationUnitPermissionService organisationUnitPermissionService, TeamService teamService) {
    this.organisationUnitPermissionService = organisationUnitPermissionService;
    this.teamService = teamService;
  }

  public boolean hasApplicationPermission(ServiceUserDetail user,
                                          ApplicationVersion applicationVersion,
                                          RolePermission... requiredPermissions) {

    var requiredPermissionsSet = Set.of(requiredPermissions);
    var userRegulatorTeamsWithPermission =
        teamService.getTeamsOfTypeThatUserHasPermissionFor(user, TeamType.REGULATOR, requiredPermissionsSet);

    // user has permission as a regulator so has access to all applications
    if (!userRegulatorTeamsWithPermission.isEmpty()) {
      return true;
    }

    var userConsulteeTeamsWithPermission =
        teamService.getTeamsOfTypeThatUserHasPermissionFor(user, TeamType.OPRED, requiredPermissionsSet);

    // user has permission as a consultee so has access to all applications
    if (!userConsulteeTeamsWithPermission.isEmpty()) {
      return true;
    }

    return organisationUnitPermissionService
        .hasOperatorPermission(user, applicationVersion.getPrimaryOperatorOuId(), requiredPermissions);
  }

  public Set<RolePermission> getApplicationPermissionsForUser(ApplicationVersion applicationVersion,
                                                              ServiceUserDetail user) {

    var userRolePermissions = new HashSet<RolePermission>();

    if (teamService.isRegulatorUser(user)) {
      teamService.getTeamsOfTypeThatUserBelongsTo(user, TeamType.REGULATOR)
          .stream()
          .map(team -> teamService.getUserPermissionsForTeam(team, user))
          .flatMap(Collection::stream)
          .forEach(userRolePermissions::add);
    }

    if (teamService.isConsulteeUser(user)) {
      teamService.getTeamsOfTypeThatUserBelongsTo(user, TeamType.OPRED)
          .stream()
          .map(team -> teamService.getUserPermissionsForTeam(team, user))
          .flatMap(Collection::stream)
          .forEach(userRolePermissions::add);
    }

    userRolePermissions.addAll(organisationUnitPermissionService
        .getUserPermissionsForOperator(user, applicationVersion.getPrimaryOperatorOuId()));

    return userRolePermissions;
  }
}
