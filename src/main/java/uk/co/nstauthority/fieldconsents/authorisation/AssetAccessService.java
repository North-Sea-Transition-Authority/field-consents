package uk.co.nstauthority.fieldconsents.authorisation;

import static uk.co.nstauthority.fieldconsents.authorisation.FieldEquityPartnerAccessService.FIELD_EQUITY_PARTNER_ROLE;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.assets.AssetWithOperatorJson;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldWithOperatorJson;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitPermissionService;
import uk.co.nstauthority.fieldconsents.teams.Role;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@Service
class AssetAccessService {

  private final OrganisationUnitPermissionService organisationUnitPermissionService;
  private final FieldEquityPartnerAccessService fieldEquityPartnerAccessService;

  AssetAccessService(
      OrganisationUnitPermissionService organisationUnitPermissionService,
      FieldEquityPartnerAccessService fieldEquityPartnerAccessService
  ) {
    this.organisationUnitPermissionService = organisationUnitPermissionService;
    this.fieldEquityPartnerAccessService = fieldEquityPartnerAccessService;
  }

  boolean userHasAnyIndustryRole(
      ServiceUserDetail userDetail,
      AssetWithOperatorJson assetWithOperatorJson,
      Collection<Role> requiredRoles
  ) {
    if (!CollectionUtils.containsAll(TeamType.INDUSTRY.getAllowedRoles(), requiredRoles)) {
      throw new IllegalArgumentException("Invalid industry roles [%s]".formatted(requiredRoles));
    }

    var industryRoles = getIndustryRoles(userDetail, assetWithOperatorJson);
    return CollectionUtils.containsAny(industryRoles, requiredRoles);
  }

  Set<Role> getIndustryRoles(ServiceUserDetail userDetail, AssetWithOperatorJson assetWithOperatorJson) {
    if (!assetWithOperatorJson.operatorExists()) {
      return Set.of();
    }

    var userRoles = organisationUnitPermissionService.getUserRolesForOperator(userDetail, assetWithOperatorJson);

    if (userRoles.contains(FIELD_EQUITY_PARTNER_ROLE)) {
      return userRoles;
    }

    if (assetWithOperatorJson instanceof FieldWithOperatorJson fieldWithOperatorJson) {
      var userHasRoleForFieldInFieldEquityPartnerTeam =
          fieldEquityPartnerAccessService.userIsFieldEquityPartner(userDetail, fieldWithOperatorJson);

      if (userHasRoleForFieldInFieldEquityPartnerTeam) {
        var userRolesWithFieldEquityPartnerRole = new HashSet<>(userRoles);
        userRolesWithFieldEquityPartnerRole.add(FIELD_EQUITY_PARTNER_ROLE);
        return userRolesWithFieldEquityPartnerRole;
      }
    }

    return userRoles;
  }
}
