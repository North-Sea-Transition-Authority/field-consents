package uk.co.nstauthority.fieldconsents.workarea;

import static uk.co.nstauthority.fieldconsents.generated.jooq.Tables.APPLICATION_VERSIONS;
import static uk.co.nstauthority.fieldconsents.workarea.WorkAreaFormService.FIELD_LOOKUP_PURPOSE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.jooq.Condition;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldJson;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.organisationgroup.OrganisationGroupQueryService;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitJson;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitService;
import uk.co.nstauthority.fieldconsents.teams.Team;
import uk.co.nstauthority.fieldconsents.teams.TeamService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@Service
public class WorkAreaService {

  public static final String ALL_ORG_UNITS_WORK_AREA_PURPOSE = "All organisation units available in work-area";

  private final TeamService teamService;

  private final WorkAreaFilterService workAreaFilterService;

  private final WorkAreaItemDtoRepository workAreaItemDtoRepository;

  private final OrganisationGroupQueryService organisationGroupQueryService;

  private final EnergyPortalUserService energyPortalUserService;

  private final ApplicationService applicationService;

  private final ApplicationVersionService applicationVersionService;

  private final FieldService fieldService;

  private final OrganisationUnitService organisationUnitService;

  public WorkAreaService(TeamService teamService,
                         WorkAreaFilterService workAreaFilterService,
                         WorkAreaItemDtoRepository workAreaItemDtoRepository,
                         OrganisationGroupQueryService organisationGroupQueryService,
                         EnergyPortalUserService energyPortalUserService,
                         ApplicationService applicationService,
                         ApplicationVersionService applicationVersionService,
                         FieldService fieldService, OrganisationUnitService organisationUnitService) {
    this.teamService = teamService;
    this.workAreaFilterService = workAreaFilterService;
    this.workAreaItemDtoRepository = workAreaItemDtoRepository;
    this.organisationGroupQueryService = organisationGroupQueryService;
    this.energyPortalUserService = energyPortalUserService;
    this.applicationService = applicationService;
    this.applicationVersionService = applicationVersionService;
    this.fieldService = fieldService;
    this.organisationUnitService = organisationUnitService;
  }

  public Set<WorkAreaItem> getWorkAreaItemsForUser(WorkAreaFilter filter, ServiceUserDetail user) {
    var conditions = workAreaFilterService.getConditions(filter);
    var workAreaItems = new HashSet<WorkAreaItem>();

    var industryTeams =
        teamService.getTeamsOfTypeThatUserHasPermissionFor(
            user, TeamType.INDUSTRY, Set.of(RolePermission.EDIT_FCS_APPLICATIONS)
        );

    if (!industryTeams.isEmpty()) {
      workAreaItems.addAll(getIndustryWorkAreaItems(industryTeams, conditions));
    }

    var regulatorTeams =
        teamService.getTeamsOfTypeThatUserHasPermissionFor(
            user, TeamType.REGULATOR, Set.of(RolePermission.PROCESS_FCS_APPLICATIONS));

    if (!regulatorTeams.isEmpty()) {
      workAreaItems.addAll(getRegulatorWorkAreaItems(conditions));
    }

    // TODO: what would happen in practice if a regulator was also an industry user (even if temporarily)?
    //       Where should we redirect the user to when they click the work-area link for the application?
    return workAreaItems;
  }

  private List<WorkAreaItem> getIndustryWorkAreaItems(List<Team> teams, List<Condition> conditions) {
    var organisationGroupIds = teams.stream()
        .map(Team::getOrganisationGroupId)
        .filter(Objects::nonNull)
        .toList();

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

    return getItemsFromDtoList(workAreaItemDtoList, organisationUnitJsons,
        workAreaItemDto -> !workAreaItemDto.status().equals(ApplicationVersionStatus.COMPLETED)
    );
  }

  // TODO: at the moment this returns every application for a regulator user with PROCESS_FCS_APPLICATIONS permission.
  //      We need to understand how this would work the unassigned/my/all tabs.
  private List<WorkAreaItem> getRegulatorWorkAreaItems(ArrayList<Condition> conditions) {
    var workAreaItemDtoList = workAreaItemDtoRepository.runQuery(conditions);

    var organisationUnitJsons = organisationUnitService.getOrganisationUnitsByIds(
        workAreaItemDtoList
            .stream()
            .map(WorkAreaItemDto::operatorId)
            .toList(),
        ALL_ORG_UNITS_WORK_AREA_PURPOSE);

    return getItemsFromDtoList(workAreaItemDtoList, organisationUnitJsons,
        workAreaItemDto -> workAreaItemDto.status().equals(ApplicationVersionStatus.SUBMITTED)
    );
  }

  private List<WorkAreaItem> getItemsFromDtoList(List<WorkAreaItemDto> workAreaItemDtoList,
                                                 List<OrganisationUnitJson> organisationUnitJsons,
                                                 Predicate<WorkAreaItemDto> workAreaItemDtoPredicate) {
    if (workAreaItemDtoList.isEmpty()) {
      return Collections.emptyList();
    }

    var organisationUnitsMap = organisationUnitJsons.stream()
        .collect(Collectors.toMap(OrganisationUnitJson::organisationUnitId, OrganisationUnitJson::name));

    // Early call to API to get all fields from workAreaItemDtoList at once
    // instead of calling the EPA for each item in loop.
    var fieldJsons = fieldService.findFieldsByIds(workAreaItemDtoList
        .stream()
        .map(WorkAreaItemDto::fieldId)
        .distinct()
        .toList(), FIELD_LOOKUP_PURPOSE);

    var fieldJsonsMap = fieldJsons
        .stream()
        .collect(Collectors.toMap(
            FieldJson::getId,
            Function.identity())
        );

    // Early call to API to get all energy portal users from workAreaItemDtoList at once
    // instead of calling the EPA for each item in loop.
    var portalUserDtos = energyPortalUserService.findByWuaIds(workAreaItemDtoList
        .stream()
        .filter(workAreaItemDto -> workAreaItemDto.submittedByWuaId() != null)
        .map(workAreaItemDto ->
            new WebUserAccountId(workAreaItemDto.submittedByWuaId())
        )
        .distinct()
        .toList());

    var portalUserDtosMap = portalUserDtos
        .stream()
        .collect(Collectors.toMap(
            EnergyPortalUserDto::webUserAccountId,
            Function.identity())
        );

    return workAreaItemDtoList.stream()
        .filter(workAreaItemDtoPredicate)
        .map(workAreaItemDto -> new WorkAreaItem(
            workAreaItemDto.applicationVersionId(),
            workAreaItemDto.type().getDisplayName(),
            getConsentDuration(workAreaItemDto),
            getApplicationReference(workAreaItemDto),
            organisationUnitsMap.getOrDefault(workAreaItemDto.operatorId(), "MISSING OPERATOR"),
            workAreaItemDto.fieldId() != null ? workAreaItemDto.fieldName() : workAreaItemDto.terminalName(),
            getAssetLocation(workAreaItemDto, fieldJsonsMap),
            workAreaItemDto.status().getDisplayName(),
            workAreaItemDto.status().equals(ApplicationVersionStatus.SUBMITTED)
                ? "Submitted %s".formatted(DateUtils.format(workAreaItemDto.submittedDateTime(), DateUtils.DATE_TIME))
                : "",
            workAreaItemDto.status().equals(ApplicationVersionStatus.SUBMITTED)
                ? getSubmitter(workAreaItemDto, portalUserDtosMap)
                : ""))
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
    var consentDuration = workAreaItemDto.duration();

    if (consentDuration == null) {
      return "";
    }

    switch (consentDuration) {
      case ANNUAL -> info = "%s %d".formatted(consentDuration.getShortDisplayName(), workAreaItemDto.consentYear());
      case LONG_TERM ->
          info = "%s %d - %d".formatted(consentDuration.getShortDisplayName(),
              workAreaItemDto.longTermStartYear(),
              workAreaItemDto.longTermEndYear()
          );
      case SHORT_TERM ->
          info = "%s %s - %s".formatted(consentDuration.getShortDisplayName(),
              DateUtils.format(workAreaItemDto.shortTermStartDate(), DateUtils.SHORT_DATE),
              DateUtils.format(workAreaItemDto.shortTermEndDate(), DateUtils.SHORT_DATE)
          );
      default -> info = "";
    }

    return info;
  }

  private String getAssetLocation(WorkAreaItemDto workAreaItemDto, Map<Integer, FieldJson> fieldJsonsMap) {
    if (workAreaItemDto.fieldId() == null) {
      return "";
    }

    var matchingFieldJson = fieldJsonsMap.get(workAreaItemDto.fieldId());
    return
        matchingFieldJson != null
            ? matchingFieldJson.getGeographicArea().getDisplayName()
            : "Unknown area";
  }

  private String getSubmitter(WorkAreaItemDto workAreaItemDto, Map<Long, EnergyPortalUserDto> portalUserDtosMap) {
    var matchingPortalUserDto = portalUserDtosMap.get(workAreaItemDto.submittedByWuaId());
    return "Submitted by %s %s %s"
        .formatted(matchingPortalUserDto.title(), matchingPortalUserDto.forename(), matchingPortalUserDto.surname());
  }
}
