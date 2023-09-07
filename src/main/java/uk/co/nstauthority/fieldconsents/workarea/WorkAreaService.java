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
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldJson;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.PermissionService;
import uk.co.nstauthority.fieldconsents.energyportal.organisationgroup.OrganisationGroupQueryService;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitJson;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItem;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemDto;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemDtoService;
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

  public WorkAreaService(TeamService teamService,
                         WorkAreaFilterService workAreaFilterService,
                         WorkAreaItemDtoService workAreaItemDtoService,
                         OrganisationGroupQueryService organisationGroupQueryService,
                         PermissionService permissionService,
                         ApplicationDataItemDtoService applicationDataItemDtoService) {
    this.teamService = teamService;
    this.workAreaFilterService = workAreaFilterService;
    this.workAreaItemDtoService = workAreaItemDtoService;
    this.organisationGroupQueryService = organisationGroupQueryService;
    this.permissionService = permissionService;
    this.applicationDataItemDtoService = applicationDataItemDtoService;
  }

  public List<ApplicationDataItem> getIndustryWorkAreaItems(WorkAreaFilter filter, ServiceUserDetail user) {
    var conditions = workAreaFilterService.getConditions(filter, user, null);

    var industryTeams =
        teamService.getTeamsOfTypeThatUserHasPermissionFor(
            user, TeamType.INDUSTRY, EnumSet.of(RolePermission.EDIT_FCS_APPLICATIONS)
        );

    var workAreaItems = new ArrayList<ApplicationDataItem>();
    if (industryTeams.isEmpty()) {
      return workAreaItems;
    }

    var organisationGroupIds = industryTeams.stream()
        .map(Team::getOrganisationGroupId)
        .filter(Objects::nonNull)
        .toList();

    if (organisationGroupIds.isEmpty()) {
      return workAreaItems;
    }

    var organisationUnitJsons = organisationGroupQueryService.getOrganisationUnitsByOrganisationGroupIds(organisationGroupIds);
    var organisationUnitIds = organisationUnitJsons
        .stream()
        .map(OrganisationUnitJson::organisationUnitId)
        .toList();

    conditions.add(APPLICATION_VERSIONS.PRIMARY_OPERATOR_OU_ID.in(organisationUnitIds));
    var workAreaItemDtoList = workAreaItemDtoService.runWorkAreaQuery(conditions, INDUSTRY);

    return getItemsFromDtoList(workAreaItemDtoList, organisationUnitJsons, TeamType.INDUSTRY, user);
  }

  public List<ApplicationDataItem> getRegulatorWorkAreaItems(WorkAreaFilter filter, ServiceUserDetail user,
                                                             WorkAreaTab workAreaTab) {
    var conditions = workAreaFilterService.getConditions(filter, user, workAreaTab);
    var regulatorTeams = teamService.getTeamsOfTypeThatUserHasPermissionFor(
        user,
        TeamType.REGULATOR,
        REGULATOR_PERMISSIONS
    );

    if (regulatorTeams.isEmpty()) {
      return Collections.emptyList();
    }

    var applicationDataItemDtos =
        workAreaItemDtoService.runWorkAreaQuery(conditions, workAreaTab.getApplicationWorkAreaPriorityGroup());

    var organisationUnitJsons = applicationDataItemDtoService
        .getOrganisationUnitJsonsFromApplicationDataItemDtos(applicationDataItemDtos);

    return getItemsFromDtoList(applicationDataItemDtos, organisationUnitJsons, TeamType.REGULATOR, user);
  }

  public List<ApplicationDataItem> getConsulteeWorkAreaItems(WorkAreaFilter filter, ServiceUserDetail user,
                                                             WorkAreaTab workAreaTab) {
    var conditions = workAreaFilterService.getConditions(filter, user, workAreaTab);
    var consulteeTeams = teamService.getTeamsOfTypeThatUserHasPermissionFor(
        user,
        TeamType.OPRED,
        EnumSet.of(RolePermission.ALLOCATE_CONSULTATION, RolePermission.RESPOND_TO_CONSULTATION)
    );

    if (consulteeTeams.isEmpty()) {
      return Collections.emptyList();
    }

    var applicationDataItemDtos =
        workAreaItemDtoService.runWorkAreaQuery(conditions, workAreaTab.getApplicationWorkAreaPriorityGroup());

    var organisationUnitJsons = applicationDataItemDtoService
        .getOrganisationUnitJsonsFromApplicationDataItemDtos(applicationDataItemDtos);

    return getItemsFromDtoList(applicationDataItemDtos, organisationUnitJsons, TeamType.OPRED, user);
  }

  List<ApplicationDataItem> getItemsFromDtoList(List<ApplicationDataItemDto> applicationDataItemDtos,
                                                List<OrganisationUnitJson> organisationUnitJsons,
                                                TeamType teamType, ServiceUserDetail user) {
    if (applicationDataItemDtos.isEmpty()) {
      return Collections.emptyList();
    }

    var organisationUnitsMap = organisationUnitJsons.stream()
        .collect(Collectors.toMap(OrganisationUnitJson::organisationUnitId, OrganisationUnitJson::name));

    // Early call to API to get all fields from applicationDataItemDtos at once
    // instead of calling the EPA for each item in loop.
    Map<Integer, FieldJson> fieldJsonsMap = applicationDataItemDtoService.getFieldJsonMapFromApplicationDataItemDtos(
        applicationDataItemDtos);

    // Early call to API to get all energy portal users from applicationDataItemDtos at once
    // instead of calling the EPA for each item in loop.
    Map<Long, EnergyPortalUserDto> portalUserDtosMap = applicationDataItemDtoService
        .getEnergyPortalUserDtoMapFromApplicationDataItemDtos(applicationDataItemDtos);

    var userAction = applicationDataItemDtoService.getApplicationDataItemUserActionFromUser(user);

    return applicationDataItemDtos.stream()
        .map(dataItemDto -> new ApplicationDataItem(
            dataItemDto.getApplicationId(),
            dataItemDto.getType().getDisplayName(),
            applicationDataItemDtoService.getDisplayConsentDuration(dataItemDto),
            applicationDataItemDtoService.getDisplayReference(dataItemDto, userAction),
            organisationUnitsMap.getOrDefault(dataItemDto.getOperatorId(), "MISSING OPERATOR"),
            dataItemDto.getFieldId() != null ? dataItemDto.getFieldName() : dataItemDto.getTerminalName(),
            applicationDataItemDtoService.getDisplayAssetLocation(dataItemDto, fieldJsonsMap),
            dataItemDto.getStatus().getDisplayName(),
            ApplicationVersionStatus.SUBMITTED.equals(dataItemDto.getStatus())
                ? "Submitted: %s".formatted(DateUtils.format(dataItemDto.getSubmittedDateTime(), DateUtils.DATE_TIME))
                : "",
            ApplicationVersionStatus.SUBMITTED.equals(dataItemDto.getStatus())
                ? applicationDataItemDtoService.getDisplaySubmitter(dataItemDto, portalUserDtosMap)
                : "",
            applicationDataItemDtoService.getDisplayAceFlag(dataItemDto),
            applicationDataItemDtoService.getDisplayCaseOfficer(dataItemDto, portalUserDtosMap),
            dataItemDto.getWithdrawalOpen(),
            applicationDataItemDtoService.getDisplayTechnicalReviewer(dataItemDto, portalUserDtosMap, teamType),
            dataItemDto.getApplicationUpdateOpen(),
            Boolean.TRUE.equals(dataItemDto.getApplicationUpdateOpen())
                ? DateUtils.format(dataItemDto.getApplicationUpdateDeadline(), DateUtils.DATE_TIME)
                : "",
            dataItemDto.getConsultationOpen(),
            Boolean.TRUE.equals(dataItemDto.getConsultationOpen())
                ? DateUtils.format(dataItemDto.getConsultationDeadline(), DateUtils.DATE_TIME)
                : ""
        ))
        .toList();
  }

  public List<WorkAreaTab> getTabsAvailableToUser(ServiceUserDetail user) {
    return Arrays.stream(WorkAreaTab.values())
        .filter(tab -> permissionService.hasPermission(user, tab.getRolePermissions()))
        .sorted(Comparator.comparing(WorkAreaTab::getDisplayOrder))
        .toList();
  }
}
