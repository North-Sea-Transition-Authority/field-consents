package uk.co.nstauthority.fieldconsents.authorisation;

import java.util.Set;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.assets.AssetWithOperatorJson;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldJson;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitPermissionService;
import uk.co.nstauthority.fieldconsents.teams.TeamService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@Service
public class AssetAccessService {

  private final TeamService teamService;
  private final OrganisationUnitPermissionService organisationUnitPermissionService;
  private final FieldEquityPartnerPermissionService fieldEquityPartnerPermissionService;

  AssetAccessService(
      TeamService teamService,
      OrganisationUnitPermissionService organisationUnitPermissionService,
      FieldEquityPartnerPermissionService fieldEquityPartnerPermissionService
  ) {
    this.teamService = teamService;
    this.organisationUnitPermissionService = organisationUnitPermissionService;
    this.fieldEquityPartnerPermissionService = fieldEquityPartnerPermissionService;
  }

  public boolean hasAssetPermission(ServiceUserDetail user,
                                    AssetWithOperatorJson assetWithOperatorJson,
                                    RolePermission... requiredPermissions) {
    var requiredPermissionsSet = Set.of(requiredPermissions);
    var userRegulatorTeamsWithPermission =
        teamService.getTeamsOfTypeThatUserHasPermissionFor(user, TeamType.REGULATOR, requiredPermissionsSet);

    // short circuit and return true if the user is a regulator with the required permissions
    if (!userRegulatorTeamsWithPermission.isEmpty()) {
      return true;
    }

    var userConsulteeTeamsWithPermission =
        teamService.getTeamsOfTypeThatUserHasPermissionFor(user, TeamType.OPRED, requiredPermissionsSet);

    // short circuit and return true if the user is a consultee with the required permissions
    if (!userConsulteeTeamsWithPermission.isEmpty()) {
      return true;
    }

    if (assetWithOperatorJson.operatorExists() && organisationUnitPermissionService
        .hasOperatorPermission(user, assetWithOperatorJson.getOperatorJson().organisationUnitId(), requiredPermissions)) {
      return true;
    }

    if (assetWithOperatorJson instanceof FieldJson && requiredPermissionsSet.contains(RolePermission.VIEW_FCS_CONSENTS)) {
      return fieldEquityPartnerPermissionService.userHasPermissionForFieldInFieldEquityPartnerTeam(
          user,
          assetWithOperatorJson.getId(),
          Set.of(RolePermission.VIEW_FCS_CONSENTS)
      );
    }

    return false;
  }
}
