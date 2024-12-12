package uk.co.nstauthority.fieldconsents.organisations;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.assets.AssetWithOperatorJson;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.energyportal.organisationgroup.OrganisationGroupDto;
import uk.co.nstauthority.fieldconsents.energyportal.organisationgroup.OrganisationGroupQueryService;
import uk.co.nstauthority.fieldconsents.teams.Role;
import uk.co.nstauthority.fieldconsents.teams.TeamQueryService;
import uk.co.nstauthority.fieldconsents.teams.TeamRole;
import uk.co.nstauthority.fieldconsents.teams.TeamScopeReference;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@Service
public class OrganisationUnitPermissionService {

  private final OrganisationUnitService organisationUnitService;
  private final OrganisationGroupQueryService organisationGroupQueryService;
  private final TeamQueryService teamQueryService;

  OrganisationUnitPermissionService(
      OrganisationUnitService organisationUnitService,
      OrganisationGroupQueryService organisationGroupQueryService,
      TeamQueryService teamQueryService
  ) {
    this.organisationUnitService = organisationUnitService;
    this.organisationGroupQueryService = organisationGroupQueryService;
    this.teamQueryService = teamQueryService;
  }

  public boolean hasOperatorRole(
      ServiceUserDetail user,
      AssetWithOperatorJson assetWithOperatorJson,
      Collection<Role> requiredRoles
  ) {
    return CollectionUtils.containsAny(getUserRolesForOperator(user, assetWithOperatorJson), requiredRoles);
  }

  public Set<Role> getUserRolesForOperator(ServiceUserDetail user, AssetWithOperatorJson assetWithOperatorJson) {
    return getUserRolesForOperator(user, assetWithOperatorJson.getOperatorJson().organisationUnitId());
  }

  public Set<Role> getUserRolesForOperator(ServiceUserDetail user, ApplicationVersion applicationVersion) {
    return getUserRolesForOperator(user, applicationVersion.getPrimaryOperatorOuId());
  }

  private Set<Role> getUserRolesForOperator(ServiceUserDetail userDetail, Integer operatorOuId) {
    var organisationUnitWithGroups = organisationUnitService.getOrganisationUnitWithGroupsById(
        operatorOuId,
        "Lookup organisation unit with groups for application security lookup"
    );

    // if the operator doesn't have an organisation group then no user has any permissions
    if (organisationUnitWithGroups.organisationGroups().isEmpty()) {
      return Collections.emptySet();
    }

    var teamScopeIds = organisationUnitWithGroups.organisationGroups()
        .stream()
        .map(OrganisationGroupDto::getOrganisationGroupId)
        .map(String::valueOf)
        .collect(Collectors.toSet());

    return teamQueryService.getTeamRoles(TeamType.INDUSTRY, TeamScopeReference.ORGANISATION_GROUP_ID, teamScopeIds)
        .stream()
        .filter(teamRole -> teamRole.getWuaId().equals(userDetail.wuaId()))
        .map(TeamRole::getRole)
        .collect(Collectors.toSet());
  }

  public List<OrganisationUnitJson> getOperatorsUserHasRoleFor(
      ServiceUserDetail user,
      Collection<Role> requiredRoles
  ) {
    var operatorsUserHasRoleFor = teamQueryService.getTeamRoles(user)
        .stream()
        .filter(teamRole -> teamRole.getTeam().getTeamType() == TeamType.INDUSTRY)
        .filter(teamRole -> requiredRoles.contains(teamRole.getRole()))
        .map(teamRole -> teamRole.getTeam().getScopeId())
        .map(Integer::parseInt)
        .distinct()
        .toList();

    return organisationGroupQueryService.getOrganisationUnitsByOrganisationGroupIds(operatorsUserHasRoleFor);
  }
}
