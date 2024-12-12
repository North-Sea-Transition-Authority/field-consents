package uk.co.nstauthority.fieldconsents.authorisation;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.assets.AssetWithOperatorJson;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.teams.Role;
import uk.co.nstauthority.fieldconsents.teams.TeamQueryService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@Service
public class FieldConsentsAccessService {

  private final ApplicationAccessService applicationAccessService;
  private final AssetAccessService assetAccessService;
  private final TeamQueryService teamQueryService;

  FieldConsentsAccessService(
      ApplicationAccessService applicationAccessService,
      AssetAccessService assetAccessService,
      TeamQueryService teamQueryService
  ) {
    this.applicationAccessService = applicationAccessService;
    this.assetAccessService = assetAccessService;
    this.teamQueryService = teamQueryService;
  }

  public Set<Role> getApplicationRolesForUser(ApplicationVersion applicationVersion, ServiceUserDetail user) {
    var userRoles = new HashSet<Role>();

    var regulatorRoles = getRegulatorRoles(user);
    userRoles.addAll(regulatorRoles);

    var consulteeRoles = getConsulteeRoles(user, applicationVersion);
    userRoles.addAll(consulteeRoles);

    var industryRoles = getIndustryRoles(user, applicationVersion);
    userRoles.addAll(industryRoles);

    return userRoles;
  }

  public boolean userHasAnyRegulatorRole(ServiceUserDetail userDetail, Collection<Role> requiredRoles) {
    if (!CollectionUtils.containsAll(TeamType.REGULATOR.getAllowedRoles(), requiredRoles)) {
      throw new IllegalArgumentException("Invalid regulator roles [%s]".formatted(requiredRoles));
    }

    var regulatorRoles = getRegulatorRoles(userDetail);
    return CollectionUtils.containsAny(regulatorRoles, requiredRoles);
  }

  public Set<Role> getRegulatorRoles(ServiceUserDetail userDetail) {
    return teamQueryService.getStaticRoles(userDetail, TeamType.REGULATOR);
  }

  public boolean userHasAnyConsulteeRole(
      ServiceUserDetail userDetail,
      ApplicationVersion applicationVersion,
      Collection<Role> requiredRoles
  ) {
    return applicationAccessService.userHasAnyConsulteeRole(userDetail, applicationVersion, requiredRoles);
  }

  public Set<Role> getConsulteeRoles(ServiceUserDetail userDetail, ApplicationVersion applicationVersion) {
    return applicationAccessService.getConsulteeRoles(userDetail, applicationVersion);
  }

  public boolean userHasAnyIndustryRole(
      ServiceUserDetail userDetail,
      ApplicationVersion applicationVersion,
      Collection<Role> requiredRoles
  ) {
    return applicationAccessService.userHasAnyIndustryRole(userDetail, applicationVersion, requiredRoles);
  }

  public Set<Role> getIndustryRoles(ServiceUserDetail userDetail, ApplicationVersion applicationVersion) {
    return applicationAccessService.getIndustryRoles(userDetail, applicationVersion);
  }

  public boolean userHasAnyIndustryRole(
      ServiceUserDetail userDetail,
      AssetWithOperatorJson assetWithOperatorJson,
      Collection<Role> requiredRoles
  ) {
    return assetAccessService.userHasAnyIndustryRole(userDetail, assetWithOperatorJson, requiredRoles);
  }

}
