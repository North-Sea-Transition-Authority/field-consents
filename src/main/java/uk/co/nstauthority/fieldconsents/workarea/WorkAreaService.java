package uk.co.nstauthority.fieldconsents.workarea;

import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityGroup.INDUSTRY;
import static uk.co.nstauthority.fieldconsents.generated.jooq.Tables.APPLICATION_VERSIONS;
import static uk.co.nstauthority.fieldconsents.workarea.WorkAreaFormService.FIELD_LOOKUP_PURPOSE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldJson;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.PermissionService;
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

  private final PermissionService permissionService;

  public WorkAreaService(TeamService teamService,
                         WorkAreaFilterService workAreaFilterService,
                         WorkAreaItemDtoRepository workAreaItemDtoRepository,
                         OrganisationGroupQueryService organisationGroupQueryService,
                         EnergyPortalUserService energyPortalUserService,
                         ApplicationService applicationService,
                         ApplicationVersionService applicationVersionService,
                         FieldService fieldService, OrganisationUnitService organisationUnitService,
                         PermissionService permissionService) {
    this.teamService = teamService;
    this.workAreaFilterService = workAreaFilterService;
    this.workAreaItemDtoRepository = workAreaItemDtoRepository;
    this.organisationGroupQueryService = organisationGroupQueryService;
    this.energyPortalUserService = energyPortalUserService;
    this.applicationService = applicationService;
    this.applicationVersionService = applicationVersionService;
    this.fieldService = fieldService;
    this.organisationUnitService = organisationUnitService;
    this.permissionService = permissionService;
  }

  public List<WorkAreaItem> getIndustryWorkAreaItems(WorkAreaFilter filter, ServiceUserDetail user) {
    var conditions = workAreaFilterService.getConditions(filter, user, null);

    var industryTeams =
        teamService.getTeamsOfTypeThatUserHasPermissionFor(
            user, TeamType.INDUSTRY, EnumSet.of(RolePermission.EDIT_FCS_APPLICATIONS)
        );

    var workAreaItems = new ArrayList<WorkAreaItem>();
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
    var workAreaItemDtoList = workAreaItemDtoRepository.runQuery(conditions, INDUSTRY);

    return getItemsFromDtoList(workAreaItemDtoList, organisationUnitJsons, WorkAreaGroup.INDUSTRY);
  }

  public List<WorkAreaItem> getRegulatorWorkAreaItems(WorkAreaFilter filter, ServiceUserDetail user,
                                                      WorkAreaTab workAreaTab) {
    var conditions = workAreaFilterService.getConditions(filter, user, workAreaTab);

    var regulatorTeams = teamService.getTeamsOfTypeThatUserHasPermissionFor(
        user,
        TeamType.REGULATOR,
        EnumSet.of(
            RolePermission.PROCESS_FCS_APPLICATIONS,
            RolePermission.ASSIGN_FCS_APPLICATIONS,
            RolePermission.TECHNICAL_REVIEW_FCS_APPLICATIONS
        )
    );

    if (regulatorTeams.isEmpty()) {
      return Collections.emptyList();
    }

    var workAreaItemDtoList =
        workAreaItemDtoRepository.runQuery(conditions, workAreaTab.getApplicationWorkAreaPriorityGroup());

    var organisationUnitJsons = organisationUnitService.getOrganisationUnitsByIds(
        workAreaItemDtoList
            .stream()
            .map(WorkAreaItemDto::operatorId)
            .toList(),
        ALL_ORG_UNITS_WORK_AREA_PURPOSE);

    return getItemsFromDtoList(workAreaItemDtoList, organisationUnitJsons, WorkAreaGroup.REGULATOR);
  }

  private List<WorkAreaItem> getItemsFromDtoList(List<WorkAreaItemDto> workAreaItemDtoList,
                                                 List<OrganisationUnitJson> organisationUnitJsons,
                                                 WorkAreaGroup workAreaGroup) {
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
    var submitterWuaIdsStream = workAreaItemDtoList
        .stream()
        .filter(workAreaItemDto -> workAreaItemDto.submittedByWuaId() != null)
        .map(workAreaItemDto -> new WebUserAccountId(workAreaItemDto.submittedByWuaId()));

    var caseOfficerWuaIdsStream = workAreaItemDtoList
        .stream()
        .filter(workAreaItemDto -> workAreaItemDto.caseOfficerWuaId() != null)
        .map(workAreaItemDto -> new WebUserAccountId(workAreaItemDto.caseOfficerWuaId()));

    var technicalReviewerWuaIdsStream = workAreaItemDtoList
        .stream()
        .filter(workAreaItemDto -> workAreaItemDto.technicalReviewerWuaId() != null)
        .map(workAreaItemDto -> new WebUserAccountId(workAreaItemDto.technicalReviewerWuaId()));

    var portalUserDtosMap =
        energyPortalUserService.findByWuaIds(
            Stream.concat(Stream.concat(submitterWuaIdsStream, caseOfficerWuaIdsStream), technicalReviewerWuaIdsStream)
                .distinct()
                .toList()
            )
            .stream()
            .collect(Collectors.toMap(
                EnergyPortalUserDto::webUserAccountId,
                Function.identity())
            );

    return workAreaItemDtoList.stream()
        .map(workAreaItemDto -> new WorkAreaItem(
            workAreaItemDto.applicationId(),
            workAreaItemDto.type().getDisplayName(),
            getConsentDuration(workAreaItemDto),
            getApplicationReference(workAreaItemDto),
            organisationUnitsMap.getOrDefault(workAreaItemDto.operatorId(), "MISSING OPERATOR"),
            workAreaItemDto.fieldId() != null ? workAreaItemDto.fieldName() : workAreaItemDto.terminalName(),
            getAssetLocation(workAreaItemDto, fieldJsonsMap),
            workAreaItemDto.status().getDisplayName(),
            ApplicationVersionStatus.SUBMITTED.equals(workAreaItemDto.status())
                ? "Submitted: %s".formatted(DateUtils.format(workAreaItemDto.submittedDateTime(), DateUtils.DATE_TIME))
                : "",
            ApplicationVersionStatus.SUBMITTED.equals(workAreaItemDto.status())
                ? getSubmitter(workAreaItemDto, portalUserDtosMap)
                : "",
            getAceFlag(workAreaItemDto),
            getCaseOfficer(workAreaItemDto, portalUserDtosMap),
            workAreaItemDto.withdrawalOpen(),
            getTechnicalReviewer(workAreaItemDto, portalUserDtosMap, workAreaGroup),
            workAreaItemDto.applicationUpdateOpen(),
            workAreaItemDto.applicationUpdateOpen()
                ? DateUtils.format(workAreaItemDto.applicationUpdateDeadline(), DateUtils.DATE_TIME)
                : ""
        ))
        .toList();
  }

  private String getApplicationReference(WorkAreaItemDto workAreaItemDto) {
    if (ApplicationVersionStatus.IN_PROGRESS.equals(workAreaItemDto.status())
        && workAreaItemDto.versionNo() == 1) {
      return "Resume application";
    } else if (ApplicationVersionStatus.IN_PROGRESS.equals(workAreaItemDto.status())
        && workAreaItemDto.versionNo() > 1) {
      return "Resume %s".formatted(
          applicationService.generateApplicationReference(
              applicationVersionService.getApplicationVersionById(workAreaItemDto.applicationVersionId())));
    }

    return applicationService.generateApplicationReference(
        applicationVersionService.getApplicationVersionById(workAreaItemDto.applicationVersionId()));
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
    return "Submitter: %s".formatted(matchingPortalUserDto.displayName());
  }

  private String getAceFlag(WorkAreaItemDto workAreaItemDto) {
    return Boolean.TRUE.equals(workAreaItemDto.aceFlag()) ? "ACE" : "";
  }

  private String getCaseOfficer(WorkAreaItemDto workAreaItemDto,
                                Map<Long, EnergyPortalUserDto> portalUserDtosMap) {
    return Objects.nonNull(workAreaItemDto.caseOfficerWuaId())
        ? "Case officer: %s".formatted(
            portalUserDtosMap.get(workAreaItemDto.caseOfficerWuaId()).displayName())
        : "";
  }

  private String getTechnicalReviewer(WorkAreaItemDto workAreaItemDto,
                                      Map<Long, EnergyPortalUserDto> portalUserDtosMap,
                                      WorkAreaGroup workAreaGroup) {
    return WorkAreaGroup.REGULATOR.equals(workAreaGroup)
        && Objects.nonNull(workAreaItemDto.technicalReviewerWuaId())
        ? "Technical reviewer: %s".formatted(
            portalUserDtosMap.get(workAreaItemDto.technicalReviewerWuaId()).displayName())
        : "";
  }

  public List<WorkAreaTab> getTabsAvailableToUser(ServiceUserDetail user) {
    return Arrays.stream(WorkAreaTab.values())
        .filter(tab -> permissionService.hasPermission(user, tab.getRolePermissions()))
        .sorted(Comparator.comparing(WorkAreaTab::getDisplayOrder))
        .toList();
  }
}
