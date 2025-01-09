package uk.co.nstauthority.fieldconsents.query;

import static uk.co.nstauthority.fieldconsents.generated.jooq.Tables.APPLICATION_ASSETS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationVersions.APPLICATION_VERSIONS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.assets.AssetRole;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.RoleGroup;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.FieldEquityPartnerAccessService;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitJson;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitPermissionService;
import uk.co.nstauthority.fieldconsents.teams.TeamQueryService;
import uk.co.nstauthority.fieldconsents.teams.TeamRole;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@Service
public class ApplicationDataItemViewService {

  private final ApplicationDataItemDtoService applicationDataItemDtoService;
  private final OrganisationUnitPermissionService organisationUnitPermissionService;
  private final FieldEquityPartnerAccessService fieldEquityPartnerAccessService;
  private final DSLContext dslContext;
  private final TeamQueryService teamQueryService;

  private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationDataItemViewService.class);

  ApplicationDataItemViewService(
      ApplicationDataItemDtoService applicationDataItemDtoService,
      OrganisationUnitPermissionService organisationUnitPermissionService,
      FieldEquityPartnerAccessService fieldEquityPartnerAccessService,
      DSLContext dslContext,
      TeamQueryService teamQueryService
  ) {
    this.applicationDataItemDtoService = applicationDataItemDtoService;
    this.organisationUnitPermissionService = organisationUnitPermissionService;
    this.fieldEquityPartnerAccessService = fieldEquityPartnerAccessService;
    this.dslContext = dslContext;
    this.teamQueryService = teamQueryService;
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
    var hasEditApplicationRole = teamQueryService.getTeamRoles(user)
        .stream()
        .map(TeamRole::getRole)
        .anyMatch(RoleGroup.INDUSTRY_EDIT_APPLICATION_ROLES::contains);

    return hasEditApplicationRole
        ? ApplicationDataItemUserAction.RESUME_APPLICATION
        : ApplicationDataItemUserAction.VIEW_APPLICATION;
  }

  public List<ApplicationDataItemView> getRegulatorApplicationDataItems(List<Condition> conditions, ServiceUserDetail user) {
    var regulatorViewer = teamQueryService.userHasAtLeastOneStaticRole(
        user,
        TeamType.REGULATOR,
        RoleGroup.REGULATOR_VIEW_CASE_PROCESSING_ROLES
    );

    if (!regulatorViewer) {
      return Collections.emptyList();
    }

    var applicationDataItemDtos = applicationDataItemDtoService.runGetDataItemDtoQuery(conditions);

    var organisationUnitJsons = applicationDataItemDtoService
        .getOrganisationUnitJsonsFromApplicationDataItemDtos(applicationDataItemDtos);

    return getItemViewsFromDtos(applicationDataItemDtos, organisationUnitJsons, TeamType.REGULATOR, user);
  }

  public List<ApplicationDataItemView> getIndustryApplicationDataItems(List<Condition> conditions, ServiceUserDetail user) {
    var organisationUnitIds =
        organisationUnitPermissionService.getOperatorsUserHasRoleFor(user, RoleGroup.INDUSTRY_VIEW_CASE_PROCESSING_ROLES)
            .stream()
            .map(OrganisationUnitJson::organisationUnitId)
            .toList();

    var lookupConditions = new ArrayList<>(conditions);

    var fieldIdsWhereUserIsFieldEquityPartner =
        fieldEquityPartnerAccessService.getFieldIdsWhereUserIsFieldEquityPartner(user);

    var applicationVersionIdsWithFieldWhereUserIsFieldEquityPartner = dslContext
        .select(APPLICATION_ASSETS.APPLICATION_VERSION_ID)
        .from(APPLICATION_ASSETS)
        .where(APPLICATION_ASSETS.ASSET_TYPE.eq(AssetType.FIELD.name()))
        .and(APPLICATION_ASSETS.ASSET_ROLE.in(AssetRole.PRIMARY.name(), AssetRole.SECONDARY.name()))
        .and(APPLICATION_ASSETS.ASSET_ID.in(fieldIdsWhereUserIsFieldEquityPartner));

    var accessCondition = APPLICATION_VERSIONS.PRIMARY_OPERATOR_OU_ID.in(organisationUnitIds)
        .or(APPLICATION_VERSIONS.ID.in(applicationVersionIdsWithFieldWhereUserIsFieldEquityPartner));

    lookupConditions.add(accessCondition);

    var applicationDataItemDtos = applicationDataItemDtoService.runGetDataItemDtoQuery(lookupConditions);

    var organisationUnitJsons = applicationDataItemDtoService
        .getOrganisationUnitJsonsFromApplicationDataItemDtos(applicationDataItemDtos);

    return getItemViewsFromDtos(applicationDataItemDtos, organisationUnitJsons, TeamType.INDUSTRY, user);
  }

  public List<ApplicationDataItemView> getConsulteeApplicationDataItemViews(List<Condition> conditions, ServiceUserDetail user) {
    var consulteeViewer = teamQueryService.userHasAtLeastOneStaticRole(
        user,
        TeamType.CONSULTEE,
        RoleGroup.CONSULTEE_WITH_VIEWER_ROLES
    );

    if (!consulteeViewer) {
      return Collections.emptyList();
    }

    var applicationDataItemDtos = applicationDataItemDtoService.runGetDataItemDtoQuery(conditions);

    var organisationUnitJsons = applicationDataItemDtoService
        .getOrganisationUnitJsonsFromApplicationDataItemDtos(applicationDataItemDtos);

    return getItemViewsFromDtos(applicationDataItemDtos, organisationUnitJsons, TeamType.CONSULTEE, user);
  }
}
