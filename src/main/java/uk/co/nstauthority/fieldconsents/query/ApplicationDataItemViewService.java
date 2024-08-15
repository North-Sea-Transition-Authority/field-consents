package uk.co.nstauthority.fieldconsents.query;

import static org.jooq.impl.DSL.exists;
import static uk.co.nstauthority.fieldconsents.generated.jooq.Tables.APPLICATION_ASSETS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationVersions.APPLICATION_VERSIONS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.assets.AssetRole;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.FieldEquityPartnerPermissionService;
import uk.co.nstauthority.fieldconsents.authorisation.PermissionService;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitJson;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitPermissionService;
import uk.co.nstauthority.fieldconsents.teams.TeamService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@Service
public class ApplicationDataItemViewService {

  private final ApplicationDataItemDtoService applicationDataItemDtoService;
  private final OrganisationUnitPermissionService organisationUnitPermissionService;
  private final TeamService teamService;
  private final PermissionService permissionService;
  private final FieldEquityPartnerPermissionService fieldEquityPartnerPermissionService;
  private final DSLContext dslContext;

  private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationDataItemViewService.class);

  ApplicationDataItemViewService(
      ApplicationDataItemDtoService applicationDataItemDtoService,
      OrganisationUnitPermissionService organisationUnitPermissionService,
      TeamService teamService,
      PermissionService permissionService,
      FieldEquityPartnerPermissionService fieldEquityPartnerPermissionService,
      DSLContext dslContext
  ) {
    this.applicationDataItemDtoService = applicationDataItemDtoService;
    this.organisationUnitPermissionService = organisationUnitPermissionService;
    this.teamService = teamService;
    this.permissionService = permissionService;
    this.fieldEquityPartnerPermissionService = fieldEquityPartnerPermissionService;
    this.dslContext = dslContext;
  }

  public List<ApplicationDataItemView> getItemViewsFromDtos(
      Collection<ApplicationDataItemDto> applicationDataItemDtos,
      Collection<OrganisationUnitJson> organisationUnitJsons,
      TeamType teamType,
      ServiceUserDetail user
  ) {
    if (applicationDataItemDtos.isEmpty()) {
      return Collections.emptyList();
    }

    LOGGER.info("Stitching together ApplicationDataItems");

    var organisationUnitNamesById = organisationUnitJsons.stream()
        .collect(Collectors.toMap(OrganisationUnitJson::organisationUnitId, OrganisationUnitJson::name));

    var fieldJsonById = applicationDataItemDtoService
        .getFieldJsonMapFromApplicationDataItemDtos(applicationDataItemDtos);

    var portalUserDtoByWuaId = applicationDataItemDtoService
        .getEnergyPortalUserDtoMapFromApplicationDataItemDtos(applicationDataItemDtos);

    // TODO: FCS-879 Display appropriate action per application for users on search pages
    var userAction = getApplicationDataItemUserActionFromUser(user);

    List<ApplicationDataItemView> results = applicationDataItemDtos.stream()
        .map(dataItemDto -> applicationDataItemDtoService.getApplicationDataItemView(
            dataItemDto,
            userAction,
            teamType,
            organisationUnitNamesById,
            fieldJsonById,
            portalUserDtoByWuaId
        ))
        .toList();

    LOGGER.info("Stitched together ApplicationDataItems, items: {}, orgs: {}, fields: {}, users: {}",
        applicationDataItemDtos.size(),
        organisationUnitJsons.size(),
        fieldJsonById.size(),
        portalUserDtoByWuaId.size());

    return results;
  }

  public ApplicationDataItemUserAction getApplicationDataItemUserActionFromUser(ServiceUserDetail user) {
    return permissionService.hasPermission(user, EnumSet.of(RolePermission.EDIT_FCS_APPLICATIONS))
        ? ApplicationDataItemUserAction.RESUME_APPLICATION
        : ApplicationDataItemUserAction.VIEW_APPLICATION;
  }

  public List<ApplicationDataItemView> getRegulatorApplicationDataItems(List<Condition> conditions, ServiceUserDetail user) {
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

    return getItemViewsFromDtos(applicationDataItemDtos, organisationUnitJsons, TeamType.REGULATOR, user);
  }

  public List<ApplicationDataItemView> getIndustryApplicationDataItems(List<Condition> conditions, ServiceUserDetail user) {
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
                .and(APPLICATION_ASSETS.ASSET_ID.in(fieldIdsUserHasViewFcsPermissionForInFieldEquityPartnerTeam))
        ));

    lookupConditions.add(accessCondition);

    var applicationDataItemDtos = applicationDataItemDtoService.runGetDataItemDtoQuery(lookupConditions);

    var organisationUnitJsons = applicationDataItemDtoService
        .getOrganisationUnitJsonsFromApplicationDataItemDtos(applicationDataItemDtos);

    return getItemViewsFromDtos(applicationDataItemDtos, organisationUnitJsons, TeamType.INDUSTRY, user);
  }

  public List<ApplicationDataItemView> getConsulteeApplicationDataItemViews(List<Condition> conditions, ServiceUserDetail user) {
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

    return getItemViewsFromDtos(applicationDataItemDtos, organisationUnitJsons, TeamType.OPRED, user);
  }
}
