package uk.co.nstauthority.fieldconsents.query;

import static org.jooq.impl.DSL.exists;
import static uk.co.nstauthority.fieldconsents.generated.jooq.Tables.APPLICATION_ASSETS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationVersions.APPLICATION_VERSIONS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.assets.AssetRole;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.FieldEquityPartnerPermissionService;
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
  private final FieldEquityPartnerPermissionService fieldEquityPartnerPermissionService;
  private final DSLContext dslContext;

  ApplicationDataItemService(
      ApplicationDataItemDtoService applicationDataItemDtoService,
      OrganisationUnitPermissionService organisationUnitPermissionService,
      TeamService teamService,
      FieldEquityPartnerPermissionService fieldEquityPartnerPermissionService,
      DSLContext dslContext
  ) {
    this.applicationDataItemDtoService = applicationDataItemDtoService;
    this.organisationUnitPermissionService = organisationUnitPermissionService;
    this.teamService = teamService;
    this.fieldEquityPartnerPermissionService = fieldEquityPartnerPermissionService;
    this.dslContext = dslContext;
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

    var fieldIdsUserHasViewFcsPermissionForInFieldEquityPartnerTeam = fieldEquityPartnerPermissionService
        .getFieldIdsUserHasPermissionForInFieldEquityPartnerTeam(user, Set.of(RolePermission.VIEW_FCS_CONSENTS));

    var accessCondition = APPLICATION_VERSIONS.PRIMARY_OPERATOR_OU_ID.in(organisationUnitIds)
        .or(exists(
            dslContext
                .selectOne()
                .from(APPLICATION_ASSETS)
                .where(APPLICATION_ASSETS.APPLICATION_VERSION_ID.eq(APPLICATION_VERSIONS.ID))
                .and(APPLICATION_ASSETS.ASSET_TYPE.eq(AssetType.FIELD.name()))
                .and(APPLICATION_ASSETS.ASSET_ROLE.in(AssetRole.PRIMARY.name(), AssetRole.SECONDARY.name()))
                .and(APPLICATION_ASSETS.ASSET_ID.isNotNull())
                .and(APPLICATION_ASSETS.ASSET_ID.in(fieldIdsUserHasViewFcsPermissionForInFieldEquityPartnerTeam))
        ));

    lookupConditions.add(accessCondition);

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
