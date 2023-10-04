package uk.co.nstauthority.fieldconsents.search;

import static uk.co.nstauthority.fieldconsents.generated.jooq.Tables.APPLICATION_VERSIONS;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldJson;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.energyportal.organisationgroup.OrganisationGroupQueryService;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitJson;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemDto;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemDtoService;
import uk.co.nstauthority.fieldconsents.teams.Team;
import uk.co.nstauthority.fieldconsents.teams.TeamService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@Service
public class SearchService {

  private final SearchFilterService searchFilterService;
  private final ApplicationDataItemDtoService applicationDataItemDtoService;
  private final OrganisationGroupQueryService organisationGroupQueryService;
  private final SearchResultItemDtoService searchResultItemDtoService;
  private final TeamService teamService;

  SearchService(SearchFilterService searchFilterService,
                ApplicationDataItemDtoService applicationDataItemDtoService,
                OrganisationGroupQueryService organisationGroupQueryService,
                SearchResultItemDtoService searchResultItemDtoService,
                TeamService teamService) {
    this.searchFilterService = searchFilterService;
    this.applicationDataItemDtoService = applicationDataItemDtoService;
    this.organisationGroupQueryService = organisationGroupQueryService;
    this.searchResultItemDtoService = searchResultItemDtoService;
    this.teamService = teamService;
  }

  public List<SearchResultItem> getRegulatorSearchResultItems(SearchFilterForm form, ServiceUserDetail user) {
    var conditions = searchFilterService.getConditions(form, TeamType.REGULATOR);
    var regulatorTeams = teamService.getTeamsOfTypeThatUserHasPermissionFor(
        user,
        TeamType.REGULATOR,
        RolePermission.VIEW_PERMISSIONS
    );

    if (regulatorTeams.isEmpty()) {
      return Collections.emptyList();
    }

    List<SearchResultItemDto> applicationDataItemDtos = searchResultItemDtoService.runSearchQuery(conditions);

    var organisationUnitJsons = applicationDataItemDtoService
        .getOrganisationUnitJsonsFromApplicationDataItemDtos(applicationDataItemDtos);

    return getItemsFromDtoList(applicationDataItemDtos, organisationUnitJsons, TeamType.REGULATOR, user);
  }

  public List<SearchResultItem> getIndustrySearchResultItems(SearchFilterForm form, ServiceUserDetail user) {
    var conditions = searchFilterService.getConditions(form, TeamType.INDUSTRY);

    var industryTeams = teamService.getTeamsOfTypeThatUserHasPermissionFor(
        user,
        TeamType.INDUSTRY,
        RolePermission.VIEW_PERMISSIONS
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

    var organisationUnitJsons = organisationGroupQueryService.getOrganisationUnitsByOrganisationGroupIds(organisationGroupIds);
    var organisationUnitIds = organisationUnitJsons
        .stream()
        .map(OrganisationUnitJson::organisationUnitId)
        .toList();

    conditions.add(APPLICATION_VERSIONS.PRIMARY_OPERATOR_OU_ID.in(organisationUnitIds));
    var applicationDataItemDtos = searchResultItemDtoService.runSearchQuery(conditions);

    return getItemsFromDtoList(applicationDataItemDtos, organisationUnitJsons, TeamType.INDUSTRY, user);
  }

  public List<SearchResultItem> getConsulteeSearchResultItems(SearchFilterForm form, ServiceUserDetail user) {
    var conditions = searchFilterService.getConditions(form, TeamType.OPRED);

    var consulteeTeams = teamService.getTeamsOfTypeThatUserHasPermissionFor(
        user,
        TeamType.OPRED,
        RolePermission.VIEW_PERMISSIONS
    );

    if (consulteeTeams.isEmpty()) {
      return Collections.emptyList();
    }

    List<SearchResultItemDto> applicationDataItemDtos = searchResultItemDtoService.runSearchQuery(conditions);

    var organisationUnitJsons = applicationDataItemDtoService
        .getOrganisationUnitJsonsFromApplicationDataItemDtos(applicationDataItemDtos);

    return getItemsFromDtoList(applicationDataItemDtos, organisationUnitJsons, TeamType.OPRED, user);
  }

  private List<SearchResultItem> getItemsFromDtoList(List<? extends ApplicationDataItemDto> applicationDataItemDtos,
                                                     List<OrganisationUnitJson> organisationUnitJsons,
                                                     TeamType teamType, ServiceUserDetail user) {
    if (applicationDataItemDtos.isEmpty()) {
      return Collections.emptyList();
    }

    var organisationUnitsMap = organisationUnitJsons.stream()
        .collect(Collectors.toMap(OrganisationUnitJson::organisationUnitId, OrganisationUnitJson::name));

    Map<Integer, FieldJson> fieldJsonsMap = applicationDataItemDtoService.getFieldJsonMapFromApplicationDataItemDtos(
        applicationDataItemDtos);

    var portalUserDtosMap = applicationDataItemDtoService
        .getEnergyPortalUserDtoMapFromApplicationDataItemDtos(applicationDataItemDtos);

    var userAction = applicationDataItemDtoService.getApplicationDataItemUserActionFromUser(user);

    return applicationDataItemDtos.stream()
        .map(dataItemDto -> new SearchResultItem(
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
                : "",
            ((SearchResultItemDto)dataItemDto).getLicences()
        ))
        .toList();
  }
}
