package uk.co.nstauthority.fieldconsents.workarea;

import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityGroup.INDUSTRY;
import static uk.co.nstauthority.fieldconsents.generated.jooq.Tables.APPLICATION_VERSIONS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.REGULATOR_PERMISSIONS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.PermissionService;
import uk.co.nstauthority.fieldconsents.energyportal.organisationgroup.OrganisationGroupQueryService;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitJson;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemDtoService;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemView;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemViewService;
import uk.co.nstauthority.fieldconsents.teams.Team;
import uk.co.nstauthority.fieldconsents.teams.TeamService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@Service
public class WorkAreaService {

  private final TeamService teamService;

  private final WorkAreaFilterService workAreaFilterService;

  private final WorkAreaItemDtoService workAreaItemDtoService;

  private final OrganisationGroupQueryService organisationGroupQueryService;

  private final PermissionService permissionService;

  private final ApplicationDataItemDtoService applicationDataItemDtoService;

  private final ApplicationDataItemViewService applicationDataItemViewService;

  public WorkAreaService(TeamService teamService,
                         WorkAreaFilterService workAreaFilterService,
                         WorkAreaItemDtoService workAreaItemDtoService,
                         OrganisationGroupQueryService organisationGroupQueryService,
                         PermissionService permissionService,
                         ApplicationDataItemDtoService applicationDataItemDtoService,
                         ApplicationDataItemViewService applicationDataItemViewService) {
    this.teamService = teamService;
    this.workAreaFilterService = workAreaFilterService;
    this.workAreaItemDtoService = workAreaItemDtoService;
    this.organisationGroupQueryService = organisationGroupQueryService;
    this.permissionService = permissionService;
    this.applicationDataItemDtoService = applicationDataItemDtoService;
    this.applicationDataItemViewService = applicationDataItemViewService;
  }

  public List<ApplicationDataItemView> getIndustryWorkAreaItems(WorkAreaFilter filter, ServiceUserDetail user) {
    var industryTeams = teamService.getTeamsOfTypeThatUserHasPermissionFor(
        user,
        TeamType.INDUSTRY,
        EnumSet.of(RolePermission.EDIT_FCS_APPLICATIONS, RolePermission.PAY_AND_SUBMIT_FCS_APPLICATIONS)
    );

    if (industryTeams.isEmpty()) {
      return Collections.emptyList();
    }

    var organisationGroupIds = industryTeams.stream()
        .map(Team::getOrganisationGroupId)
        .filter(Objects::nonNull)
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

  public List<ApplicationDataItemView> getRegulatorWorkAreaItems(WorkAreaFilter filter, ServiceUserDetail user,
                                                                 WorkAreaTab workAreaTab) {
    var regulatorTeams = teamService.getTeamsOfTypeThatUserHasPermissionFor(
        user,
        TeamType.REGULATOR,
        REGULATOR_PERMISSIONS
    );

    if (regulatorTeams.isEmpty()) {
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
    var consulteeTeams = teamService.getTeamsOfTypeThatUserHasPermissionFor(
        user,
        TeamType.OPRED,
        EnumSet.of(RolePermission.ALLOCATE_CONSULTATION, RolePermission.RESPOND_TO_CONSULTATION)
    );

    if (consulteeTeams.isEmpty()) {
      return Collections.emptyList();
    }

    var conditions = workAreaFilterService.getConditions(filter, user, workAreaTab);
    var applicationDataItemDtos =
        workAreaItemDtoService.runWorkAreaQuery(conditions, workAreaTab.getApplicationWorkAreaPriorityGroup());

    var organisationUnitJsons = applicationDataItemDtoService
        .getOrganisationUnitJsonsFromApplicationDataItemDtos(applicationDataItemDtos);

    return applicationDataItemViewService
        .getItemViewsFromDtos(applicationDataItemDtos, organisationUnitJsons, TeamType.OPRED, user);
  }

  public List<WorkAreaTab> getTabsAvailableToUser(ServiceUserDetail user) {
    return Arrays.stream(WorkAreaTab.values())
        .filter(tab -> permissionService.hasPermission(user, tab.getRolePermissions()))
        .sorted(Comparator.comparing(WorkAreaTab::getDisplayOrder))
        .toList();
  }

}
