package uk.co.nstauthority.fieldconsents.search;

import static uk.co.nstauthority.fieldconsents.generated.jooq.Tables.APPLICATION_VERSIONS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.energyportal.organisationgroup.OrganisationGroupQueryService;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitJson;
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

    var searchResultItemDtos = searchResultItemDtoService.runSearchQuery(conditions);

    var organisationUnitJsons = applicationDataItemDtoService
        .getOrganisationUnitJsonsFromApplicationDataItemDtos(searchResultItemDtos);

    return getItemsFromDtoList(searchResultItemDtos, organisationUnitJsons, TeamType.REGULATOR, user);
  }

  public List<SearchResultItem> getIndustrySearchResultItems(SearchFilterForm form, ServiceUserDetail user) {
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

    var conditions = new ArrayList<>(searchFilterService.getConditions(form, TeamType.INDUSTRY));
    conditions.add(APPLICATION_VERSIONS.PRIMARY_OPERATOR_OU_ID.in(organisationUnitIds));
    var searchResultItemDtos = searchResultItemDtoService.runSearchQuery(conditions);

    return getItemsFromDtoList(searchResultItemDtos, organisationUnitJsons, TeamType.INDUSTRY, user);
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

    var searchResultItemDtos = searchResultItemDtoService.runSearchQuery(conditions);

    var organisationUnitJsons = applicationDataItemDtoService
        .getOrganisationUnitJsonsFromApplicationDataItemDtos(searchResultItemDtos);

    return getItemsFromDtoList(searchResultItemDtos, organisationUnitJsons, TeamType.OPRED, user);
  }

  private List<SearchResultItem> getItemsFromDtoList(
      List<SearchResultItemDto> searchResultItemDtos,
      List<OrganisationUnitJson> organisationUnitJsons,
      TeamType teamType,
      ServiceUserDetail user
  ) {
    if (searchResultItemDtos.isEmpty()) {
      return Collections.emptyList();
    }

    var organisationUnitNamesById = organisationUnitJsons.stream()
        .collect(Collectors.toMap(OrganisationUnitJson::organisationUnitId, OrganisationUnitJson::name));

    var fieldJsonById = applicationDataItemDtoService.getFieldJsonMapFromApplicationDataItemDtos(
        searchResultItemDtos);

    var portalUserDtoByWuaId = applicationDataItemDtoService
        .getEnergyPortalUserDtoMapFromApplicationDataItemDtos(searchResultItemDtos);

    return searchResultItemDtos.stream()
        .map(dataItemDto -> {
          var applicationDataItem = applicationDataItemDtoService.getApplicationDataItem(
              dataItemDto,
              user,
              teamType,
              organisationUnitNamesById,
              fieldJsonById,
              portalUserDtoByWuaId
          );

          var licenses = dataItemDto.getLicences();
          return new SearchResultItem(applicationDataItem, licenses);
        })
        .toList();
  }

}
