package uk.co.nstauthority.fieldconsents.workarea;

import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityGroup.INDUSTRY;
import static uk.co.nstauthority.fieldconsents.generated.jooq.Tables.APPLICATION_VERSIONS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.RoleGroup;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.energyportal.organisationgroup.OrganisationGroupQueryService;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitJson;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemDtoService;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemView;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemViewService;
import uk.co.nstauthority.fieldconsents.teams.TeamQueryService;
import uk.co.nstauthority.fieldconsents.teams.TeamRole;
import uk.co.nstauthority.fieldconsents.teams.TeamScopeReference;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@Service
public class WorkAreaService {

  private final WorkAreaFilterService workAreaFilterService;
  private final WorkAreaItemDtoService workAreaItemDtoService;
  private final OrganisationGroupQueryService organisationGroupQueryService;
  private final ApplicationDataItemDtoService applicationDataItemDtoService;
  private final ApplicationDataItemViewService applicationDataItemViewService;
  private final TeamQueryService teamQueryService;

  WorkAreaService(
      WorkAreaFilterService workAreaFilterService,
      WorkAreaItemDtoService workAreaItemDtoService,
      OrganisationGroupQueryService organisationGroupQueryService,
      ApplicationDataItemDtoService applicationDataItemDtoService,
      ApplicationDataItemViewService applicationDataItemViewService,
      TeamQueryService teamQueryService
  ) {
    this.workAreaFilterService = workAreaFilterService;
    this.workAreaItemDtoService = workAreaItemDtoService;
    this.organisationGroupQueryService = organisationGroupQueryService;
    this.applicationDataItemDtoService = applicationDataItemDtoService;
    this.applicationDataItemViewService = applicationDataItemViewService;
    this.teamQueryService = teamQueryService;
  }

  public List<ApplicationDataItemView> getIndustryWorkAreaItems(WorkAreaFilter filter, ServiceUserDetail user) {
    var teamRoles = teamQueryService.getTeamRoles(user)
        .stream()
        .filter(teamRole -> {
          var team = teamRole.getTeam();
          return team.getTeamType() == TeamType.INDUSTRY
              && team.getScopeType().equals(TeamScopeReference.ORGANISATION_GROUP_ID);
        })
        .filter(teamRole -> {
          var role = teamRole.getRole();
          return RoleGroup.INDUSTRY_EDIT_APPLICATION_ROLES.contains(role)
              || RoleGroup.INDUSTRY_PAY_AND_SUBMIT_APPLICATION_ROLES.contains(role);
        })
        .collect(Collectors.toSet());

    if (teamRoles.isEmpty()) {
      return Collections.emptyList();
    }

    var organisationGroupIds = teamRoles.stream()
        .map(teamRole -> teamRole.getTeam().getScopeId())
        .map(Integer::parseInt)
        .toList();

    if (organisationGroupIds.isEmpty()) {
      return Collections.emptyList();
    }

    var organisationUnitJsonById = organisationGroupQueryService
        .getOrganisationUnitsByOrganisationGroupIds(organisationGroupIds)
        .stream()
        .collect(Collectors.toMap(
            OrganisationUnitJson::organisationUnitId,
            Function.identity()
        ));

    var conditions = new ArrayList<>(workAreaFilterService.getConditions(filter, user, null));
    conditions.add(APPLICATION_VERSIONS.PRIMARY_OPERATOR_OU_ID.in(organisationUnitJsonById.keySet()));
    var workAreaItemDtoList = workAreaItemDtoService.runWorkAreaQuery(conditions, INDUSTRY);

    return applicationDataItemViewService.getItemViewsFromDtos(
        workAreaItemDtoList,
        organisationUnitJsonById.values(),
        TeamType.INDUSTRY,
        user
    );
  }

  public List<ApplicationDataItemView> getRegulatorWorkAreaItems(
      WorkAreaFilter filter,
      ServiceUserDetail user,
      WorkAreaTab workAreaTab
  ) {
    if (!teamQueryService.userHasAtLeastOneStaticRole(user, TeamType.REGULATOR, RoleGroup.REGULATOR_VIEW_CASE_PROCESSING_ROLES)) {
      return Collections.emptyList();
    }

    var conditions = workAreaFilterService.getConditions(filter, user, workAreaTab);
    var applicationDataItemDtos =
        workAreaItemDtoService.runWorkAreaQuery(conditions, workAreaTab.getApplicationWorkAreaPriorityGroup());

    var organisationUnitJsons = applicationDataItemDtoService
        .getOrganisationUnitJsonsFromApplicationDataItemDtos(applicationDataItemDtos);

    return applicationDataItemViewService
        .getItemViewsFromDtos(applicationDataItemDtos, organisationUnitJsons, TeamType.REGULATOR, user);
  }

  public List<ApplicationDataItemView> getConsulteeWorkAreaItems(WorkAreaFilter filter, ServiceUserDetail user,
                                                                 WorkAreaTab workAreaTab) {
    if (!teamQueryService.userHasAtLeastOneStaticRole(user, TeamType.CONSULTEE, RoleGroup.CONSULTEE_VIEW_CASE_PROCESSING_ROLES)) {
      return Collections.emptyList();
    }

    var conditions = workAreaFilterService.getConditions(filter, user, workAreaTab);
    var applicationDataItemDtos =
        workAreaItemDtoService.runWorkAreaQuery(conditions, workAreaTab.getApplicationWorkAreaPriorityGroup());

    var organisationUnitJsons = applicationDataItemDtoService
        .getOrganisationUnitJsonsFromApplicationDataItemDtos(applicationDataItemDtos);

    return applicationDataItemViewService
        .getItemViewsFromDtos(applicationDataItemDtos, organisationUnitJsons, TeamType.CONSULTEE, user);
  }

  public List<WorkAreaTab> getTabsAvailableToUser(ServiceUserDetail user) {
    var roles = teamQueryService.getTeamRoles(user).stream().map(TeamRole::getRole).collect(Collectors.toSet());
    return Arrays.stream(WorkAreaTab.values())
        .filter(tab -> CollectionUtils.containsAny(roles, tab.getRoles()))
        .sorted(Comparator.comparing(WorkAreaTab::getDisplayOrder))
        .toList();
  }

}
