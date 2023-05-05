package uk.co.nstauthority.fieldconsents.workarea;

import static uk.co.nstauthority.fieldconsents.generated.jooq.Tables.APPLICATION_VERSIONS;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.jooq.Condition;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.authentication.UserDetailService;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.organisationgroup.OrganisationGroupQueryService;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitJson;
import uk.co.nstauthority.fieldconsents.teams.Team;
import uk.co.nstauthority.fieldconsents.teams.TeamService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@Service
public class WorkAreaService {

  static final String FIELD_LOOKUP_PURPOSE = "Lookup field to get the asset location";

  private final TeamService teamService;

  private final UserDetailService userDetailService;

  private final WorkAreaFilterService workAreaFilterService;

  private final WorkAreaItemDtoRepository workAreaItemDtoRepository;

  private final OrganisationGroupQueryService organisationGroupQueryService;

  private final EnergyPortalUserService energyPortalUserService;

  private final ApplicationService applicationService;

  private final ApplicationVersionService applicationVersionService;

  private final FieldService fieldService;

  public WorkAreaService(TeamService teamService, UserDetailService userDetailService,
                         WorkAreaFilterService workAreaFilterService,
                         WorkAreaItemDtoRepository workAreaItemDtoRepository,
                         OrganisationGroupQueryService organisationGroupQueryService,
                         EnergyPortalUserService energyPortalUserService,
                         ApplicationService applicationService, ApplicationVersionService applicationVersionService,
                         FieldService fieldService) {
    this.teamService = teamService;
    this.userDetailService = userDetailService;
    this.workAreaFilterService = workAreaFilterService;
    this.workAreaItemDtoRepository = workAreaItemDtoRepository;
    this.organisationGroupQueryService = organisationGroupQueryService;
    this.energyPortalUserService = energyPortalUserService;
    this.applicationService = applicationService;
    this.applicationVersionService = applicationVersionService;
    this.fieldService = fieldService;
  }

  public List<WorkAreaItem> getWorkAreaItems(WorkAreaFilter filter) {
    var conditions = workAreaFilterService.getConditions(filter);
    var userDetail = userDetailService.getUserDetail();
    var requiredPermissions = Set.of(RolePermission.VIEW_FCS_APPLICATIONS);
    var industryTeams =
        teamService.getTeamsOfTypeThatUserHasPermissionFor(userDetail, TeamType.INDUSTRY, requiredPermissions);
    var regulatorTeams =
        teamService.getTeamsOfTypeThatUserHasPermissionFor(userDetail, TeamType.REGULATOR, requiredPermissions);

    if (!industryTeams.isEmpty()) {
      return getIndustryWorkAreaItems(industryTeams, conditions);
    }

    if (!regulatorTeams.isEmpty()) {
      return getRegulatorWorkAreaItems(conditions);
    }

    throw new RuntimeException("User with id %s cannot access the work-area screen.".formatted(userDetail.wuaId()));
  }

  private List<WorkAreaItem> getIndustryWorkAreaItems(List<Team> teams, List<Condition> conditions) {
    var organisationGroupIds = teams.stream().map(Team::getOrganisationGroupId).toList();

    if (organisationGroupIds.isEmpty()) {
      return Collections.emptyList();
    }

    var organisationUnitJsons = organisationGroupQueryService.getOrganisationUnitsByOrganisationGroupIds(organisationGroupIds);
    var organisationUnitIds = organisationUnitJsons
        .stream()
        .map(OrganisationUnitJson::organisationUnitId)
        .toList();

    conditions.add(APPLICATION_VERSIONS.PRIMARY_OPERATOR_OU_ID.in(organisationUnitIds));
    var workAreaItemDtoList = workAreaItemDtoRepository.runQuery(conditions);

    return getItemsFromDtoList(workAreaItemDtoList, organisationUnitJsons);
  }

  private List<WorkAreaItem> getItemsFromDtoList(List<WorkAreaItemDto> workAreaItemDtoList,
                                                 List<OrganisationUnitJson> organisationUnitJsons) {
    if (workAreaItemDtoList.isEmpty()) {
      return Collections.emptyList();
    }

    var organisationUnitsMap = organisationUnitJsons.stream()
        .collect(Collectors.toMap(OrganisationUnitJson::organisationUnitId, OrganisationUnitJson::name));

    return workAreaItemDtoList.stream()
        .filter(workAreaItemDto -> !workAreaItemDto.status().equals(ApplicationVersionStatus.COMPLETED))
        .map(workAreaItemDto -> new WorkAreaItem(
            workAreaItemDto.applicationVersionId(),
            workAreaItemDto.type().getDisplayName(),
            getConsentDuration(workAreaItemDto),
            getApplicationReference(workAreaItemDto),
            organisationUnitsMap.getOrDefault(workAreaItemDto.operatorId(), "MISSING OPERATOR"),
            workAreaItemDto.fieldId() != null ? workAreaItemDto.fieldName() : workAreaItemDto.terminalName(),
            getAssetLocation(workAreaItemDto),
            workAreaItemDto.status().getDisplayName(),
            workAreaItemDto.status().equals(ApplicationVersionStatus.SUBMITTED)
                ? "Submitted %s".formatted(DateUtils.format(workAreaItemDto.submittedDateTime(), DateUtils.DATE_TIME))
                : "",
            getSubmitter(workAreaItemDto)))
        .toList();
  }

  private String getApplicationReference(WorkAreaItemDto workAreaItemDto) {
    if (workAreaItemDto.status().equals(ApplicationVersionStatus.IN_PROGRESS)) {
      return "Resume application";
    }

    return applicationService.generateApplicationReference(
        applicationVersionService.getApplicationVersionById(workAreaItemDto.applicationVersionId())
    );
  }

  private String getConsentDuration(WorkAreaItemDto workAreaItemDto) {
    String info;

    if (workAreaItemDto.duration() == null) {
      return "";
    }

    switch (workAreaItemDto.duration()) {
      case ANNUAL -> info = "Annual %d".formatted(workAreaItemDto.consentYear());
      case LONG_TERM ->
          info = "Long term %d - %d".formatted(workAreaItemDto.longTermStartYear(), workAreaItemDto.longTermEndYear());
      case SHORT_TERM ->
          info = "Short term %s - %s".formatted(
              DateUtils.format(workAreaItemDto.shortTermStartDate(), DateUtils.SHORT_DATE),
              DateUtils.format(workAreaItemDto.shortTermEndDate(), DateUtils.SHORT_DATE)
          );
      default -> info = "";
    }

    return info;
  }

  private String getAssetLocation(WorkAreaItemDto workAreaItemDto) {
    if (workAreaItemDto.fieldId() == null) {
      return "";
    }

    return String.format("%s", fieldService.getField(workAreaItemDto.fieldId(), FIELD_LOOKUP_PURPOSE)
        .getGeographicAreaDisplayName());
  }

  private String getSubmitter(WorkAreaItemDto workAreaItemDto) {
    if (workAreaItemDto.status().equals(ApplicationVersionStatus.IN_PROGRESS)) {
      return "";
    }

    Optional<EnergyPortalUserDto> portalUserDtoOptional = energyPortalUserService
        .findByWuaId(new WebUserAccountId(workAreaItemDto.submittedByWuaId()));
    if (portalUserDtoOptional.isPresent()) {
      var portalUserDto = portalUserDtoOptional.get();
      return "Submitted by %s %s %s".formatted(portalUserDto.title(), portalUserDto.forename(), portalUserDto.surname());
    }

    return "UNKNOWN USER";
  }

  // TODO FCS-78: NSTA Work Area - Unassigned applications
  private List<WorkAreaItem> getRegulatorWorkAreaItems(Object conditions) {
    return Collections.emptyList();
  }
}
