package uk.co.nstauthority.fieldconsents.query;

import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationVersions.APPLICATION_VERSIONS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.jooq.Condition;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitJson;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitPermissionService;
import uk.co.nstauthority.fieldconsents.teams.TeamService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@Service
public class ApplicationDataItemService {

  private final ApplicationDataItemDtoService applicationDataItemDtoService;
  private final OrganisationUnitPermissionService organisationUnitPermissionService;
  private final TeamService teamService;

  ApplicationDataItemService(
      ApplicationDataItemDtoService applicationDataItemDtoService,
      OrganisationUnitPermissionService organisationUnitPermissionService,
      TeamService teamService
  ) {
    this.applicationDataItemDtoService = applicationDataItemDtoService;
    this.organisationUnitPermissionService = organisationUnitPermissionService;
    this.teamService = teamService;
  }

  public List<ApplicationDataItem> getItemsFromDtos(
      Collection<ApplicationDataItemDto> applicationDataItemDtos,
      Collection<OrganisationUnitJson> organisationUnitJsons,
      TeamType teamType,
      ServiceUserDetail user
  ) {
    if (applicationDataItemDtos.isEmpty()) {
      return Collections.emptyList();
    }

    var organisationUnitNamesById = organisationUnitJsons.stream()
        .collect(Collectors.toMap(OrganisationUnitJson::organisationUnitId, OrganisationUnitJson::name));

    var fieldJsonById = applicationDataItemDtoService
        .getFieldJsonMapFromApplicationDataItemDtos(applicationDataItemDtos);

    var portalUserDtoByWuaId = applicationDataItemDtoService
        .getEnergyPortalUserDtoMapFromApplicationDataItemDtos(applicationDataItemDtos);

    return applicationDataItemDtos.stream()
        .map(dataItemDto -> applicationDataItemDtoService.getApplicationDataItem(
            dataItemDto,
            user,
            teamType,
            organisationUnitNamesById,
            fieldJsonById,
            portalUserDtoByWuaId
        ))
        .toList();
  }

  public List<ApplicationDataItem> getRegulatorApplicationDataItems(List<Condition> conditions, ServiceUserDetail user) {
    var regulatorTeams = teamService.getTeamsOfTypeThatUserHasPermissionFor(
        user,
        TeamType.REGULATOR,
        RolePermission.VIEW_PERMISSIONS
    );

    if (regulatorTeams.isEmpty()) {
      return Collections.emptyList();
    }

    var applicationDataItemDtos = applicationDataItemDtoService.runGetDataItemDtoQuery(conditions);

    var organisationUnitJsons = applicationDataItemDtoService
        .getOrganisationUnitJsonsFromApplicationDataItemDtos(applicationDataItemDtos);

    return getItemsFromDtoList(applicationDataItemDtos, organisationUnitJsons, TeamType.REGULATOR, user);
  }

  public List<ApplicationDataItem> getIndustryApplicationDataItems(List<Condition> conditions, ServiceUserDetail user) {
    var organisationUnitIds =
        organisationUnitPermissionService.getOperatorsUserHasPermissionsFor(user, RolePermission.VIEW_PERMISSIONS)
            .stream()
            .map(OrganisationUnitJson::organisationUnitId)
            .toList();

    var lookupConditions = new ArrayList<>(conditions);
    lookupConditions.add(APPLICATION_VERSIONS.PRIMARY_OPERATOR_OU_ID.in(organisationUnitIds));
    var applicationDataItemDtos = applicationDataItemDtoService.runGetDataItemDtoQuery(lookupConditions);

    var organisationUnitJsons = applicationDataItemDtoService
        .getOrganisationUnitJsonsFromApplicationDataItemDtos(applicationDataItemDtos);

    return getItemsFromDtoList(applicationDataItemDtos, organisationUnitJsons, TeamType.INDUSTRY, user);
  }

  public List<ApplicationDataItem> getConsulteeApplicationDataItems(List<Condition> conditions, ServiceUserDetail user) {
    var consulteeTeams = teamService.getTeamsOfTypeThatUserHasPermissionFor(
        user,
        TeamType.OPRED,
        RolePermission.VIEW_PERMISSIONS
    );

    if (consulteeTeams.isEmpty()) {
      return Collections.emptyList();
    }

    var applicationDataItemDtos = applicationDataItemDtoService.runGetDataItemDtoQuery(conditions);

    var organisationUnitJsons = applicationDataItemDtoService
        .getOrganisationUnitJsonsFromApplicationDataItemDtos(applicationDataItemDtos);

    return getItemsFromDtoList(applicationDataItemDtos, organisationUnitJsons, TeamType.OPRED, user);
  }

  private List<ApplicationDataItem> getItemsFromDtoList(
      List<ApplicationDataItemDto> applicationDataItemDtos,
      List<OrganisationUnitJson> organisationUnitJsons,
      TeamType teamType,
      ServiceUserDetail user
  ) {
    if (applicationDataItemDtos.isEmpty()) {
      return Collections.emptyList();
    }

    var organisationUnitNamesById = organisationUnitJsons.stream()
        .collect(Collectors.toMap(OrganisationUnitJson::organisationUnitId, OrganisationUnitJson::name));

    var fieldJsonById = applicationDataItemDtoService.getFieldJsonMapFromApplicationDataItemDtos(
        applicationDataItemDtos);

    var portalUserDtoByWuaId = applicationDataItemDtoService
        .getEnergyPortalUserDtoMapFromApplicationDataItemDtos(applicationDataItemDtos);

    return applicationDataItemDtos.stream()
        .map(dataItemDto -> applicationDataItemDtoService.getApplicationDataItem(
            dataItemDto,
            user,
            teamType,
            organisationUnitNamesById,
            fieldJsonById,
            portalUserDtoByWuaId
        )).toList();
  }
}
