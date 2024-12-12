package uk.co.nstauthority.fieldconsents.authorisation;

import static uk.co.nstauthority.fieldconsents.authorisation.FieldEquityPartnerAccessService.FIELD_EQUITY_PARTNER_ROLE;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitPermissionService;
import uk.co.nstauthority.fieldconsents.teams.Role;
import uk.co.nstauthority.fieldconsents.teams.TeamQueryService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@Service
class ApplicationAccessService {

  private final OrganisationUnitPermissionService organisationUnitPermissionService;
  private final ConsultationService consultationService;
  private final FieldEquityPartnerAccessService fieldEquityPartnerAccessService;
  private final ApplicationAssetService applicationAssetService;
  private final TeamQueryService teamQueryService;

  @Autowired
  ApplicationAccessService(
      OrganisationUnitPermissionService organisationUnitPermissionService,
      ConsultationService consultationService,
      FieldEquityPartnerAccessService fieldEquityPartnerAccessService,
      ApplicationAssetService applicationAssetService,
      TeamQueryService teamQueryService
  ) {
    this.organisationUnitPermissionService = organisationUnitPermissionService;
    this.consultationService = consultationService;
    this.fieldEquityPartnerAccessService = fieldEquityPartnerAccessService;
    this.applicationAssetService = applicationAssetService;
    this.teamQueryService = teamQueryService;
  }

  boolean userHasAnyIndustryRole(
      ServiceUserDetail userDetail,
      ApplicationVersion applicationVersion,
      Collection<Role> requiredRoles
  ) {
    if (!CollectionUtils.containsAll(TeamType.INDUSTRY.getAllowedRoles(), requiredRoles)) {
      throw new IllegalArgumentException("Invalid industry roles [%s]".formatted(requiredRoles));
    }

    var industryRoles = getIndustryRoles(userDetail, applicationVersion);
    return CollectionUtils.containsAny(industryRoles, requiredRoles);
  }

  Set<Role> getIndustryRoles(ServiceUserDetail userDetail, ApplicationVersion applicationVersion) {
    var userRoles = organisationUnitPermissionService.getUserRolesForOperator(userDetail, applicationVersion);

    if (userRoles.contains(FIELD_EQUITY_PARTNER_ROLE)) {
      return userRoles;
    }

    if (!applicationAssetService.getPrimaryAsset(applicationVersion).isField()) {
      return userRoles;
    }

    if (fieldEquityPartnerAccessService.userIsFieldEquityPartner(userDetail, applicationVersion)) {
      var userRolesWithFieldEquityPartnerRole = new HashSet<>(userRoles);
      userRolesWithFieldEquityPartnerRole.add(FIELD_EQUITY_PARTNER_ROLE);
      return userRolesWithFieldEquityPartnerRole;
    }

    return userRoles;
  }

  boolean userHasAnyConsulteeRole(
      ServiceUserDetail userDetail,
      ApplicationVersion applicationVersion,
      Collection<Role> requiredRoles
  ) {
    if (!CollectionUtils.containsAll(TeamType.CONSULTEE.getAllowedRoles(), requiredRoles)) {
      throw new IllegalArgumentException("Invalid consultee roles [%s]".formatted(requiredRoles));
    }

    var consulteeRoles = getConsulteeRoles(userDetail, applicationVersion);
    return CollectionUtils.containsAny(consulteeRoles, requiredRoles);
  }

  Set<Role> getConsulteeRoles(ServiceUserDetail userDetail, ApplicationVersion applicationVersion) {
    var consultations = consultationService.getConsultationsByApplication(applicationVersion.getApplication());
    if (consultations.isEmpty()) {
      return Set.of();
    }

    return teamQueryService.getStaticRoles(userDetail, TeamType.CONSULTEE);
  }

}
