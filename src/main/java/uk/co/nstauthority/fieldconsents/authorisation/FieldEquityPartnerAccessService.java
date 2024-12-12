package uk.co.nstauthority.fieldconsents.authorisation;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalapi.generated.types.Field;
import uk.co.fivium.energyportalapi.generated.types.FieldEquityPartner;
import uk.co.fivium.energyportalapi.generated.types.OrganisationGroup;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAsset;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.fieldequitypartner.FieldEquityPartnerService;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldJson;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.teams.Role;
import uk.co.nstauthority.fieldconsents.teams.TeamQueryService;
import uk.co.nstauthority.fieldconsents.teams.TeamScopeReference;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@Service
public class FieldEquityPartnerAccessService {

  public static final Role FIELD_EQUITY_PARTNER_ROLE = Role.CONSENT_RECIPIENT;

  private final FieldEquityPartnerService fieldEquityPartnerService;
  private final ApplicationAssetService applicationAssetService;
  private final TeamQueryService teamQueryService;

  FieldEquityPartnerAccessService(
      FieldEquityPartnerService fieldEquityPartnerService,
      ApplicationAssetService applicationAssetService,
      TeamQueryService teamQueryService
  ) {
    this.fieldEquityPartnerService = fieldEquityPartnerService;
    this.applicationAssetService = applicationAssetService;
    this.teamQueryService = teamQueryService;
  }

  boolean userIsFieldEquityPartner(ServiceUserDetail user, ApplicationVersion applicationVersion) {
    var organisationGroupIds = getOrganisationGroupIdsWhereUserIsFieldEquityPartner(user);
    if (organisationGroupIds.isEmpty()) {
      return false;
    }

    return fieldEquityPartnerService.getFieldsWithFieldEquityPartners(applicationVersion)
        .stream()
        .anyMatch(field -> fieldHasAnyFieldEquityPartnerWithOrganisationGroupIdIn(field, organisationGroupIds));
  }

  boolean userIsFieldEquityPartner(ServiceUserDetail user, FieldJson fieldJson) {
    var organisationGroupIds = getOrganisationGroupIdsWhereUserIsFieldEquityPartner(user);
    if (organisationGroupIds.isEmpty()) {
      return false;
    }

    return fieldEquityPartnerService.getFieldWithFieldEquityPartners(fieldJson.getId())
        .stream()
        .anyMatch(field -> fieldHasAnyFieldEquityPartnerWithOrganisationGroupIdIn(field, organisationGroupIds));
  }

  public List<Integer> getFieldIdsWhereUserIsFieldEquityPartner(ServiceUserDetail user) {
    var organisationGroupIds = getOrganisationGroupIdsWhereUserIsFieldEquityPartner(user);
    if (organisationGroupIds.isEmpty()) {
      return List.of();
    }

    var allFieldIds = applicationAssetService.getAllPrimaryAndSecondaryFieldAssets()
        .stream()
        .map(ApplicationAsset::getAssetId)
        .distinct()
        .toList();

    return fieldEquityPartnerService.getFieldsWithFieldEquityPartners(allFieldIds)
        .stream()
        .filter(field -> fieldHasAnyFieldEquityPartnerWithOrganisationGroupIdIn(field, organisationGroupIds))
        .map(Field::getFieldId)
        .toList();
  }

  public List<Integer> getFieldIdsWhereUserIsFieldEquityPartner(ServiceUserDetail user, List<Integer> fieldIds) {
    var organisationGroupIds = getOrganisationGroupIdsWhereUserIsFieldEquityPartner(user);
    if (organisationGroupIds.isEmpty()) {
      return List.of();
    }

    return fieldEquityPartnerService.getFieldsWithFieldEquityPartners(fieldIds)
        .stream()
        .filter(field -> fieldHasAnyFieldEquityPartnerWithOrganisationGroupIdIn(field, organisationGroupIds))
        .map(Field::getFieldId)
        .toList();
  }

  Set<Integer> getOrganisationGroupIdsWhereUserIsFieldEquityPartner(ServiceUserDetail user) {
    return teamQueryService.getTeamRoles(user)
        .stream()
        .filter(teamRole ->
            teamRole.getTeam().getTeamType() == TeamType.INDUSTRY
                && teamRole.getTeam().getScopeType().equals(TeamScopeReference.ORGANISATION_GROUP_ID)
                && teamRole.getRole() == FIELD_EQUITY_PARTNER_ROLE
        )
        .map(teamRole -> teamRole.getTeam().getScopeId())
        .map(Integer::valueOf)
        .collect(Collectors.toSet());
  }

  boolean fieldHasAnyFieldEquityPartnerWithOrganisationGroupIdIn(Field field, Set<Integer> organisationGroupIds) {
    return Optional.ofNullable(field.getFieldEquityPartners()).orElse(List.of()).stream()
        .map(FieldEquityPartner::getOrganisationUnit)
        .flatMap(ou -> Optional.ofNullable(ou.getOrganisationGroups()).orElse(List.of()).stream())
        .map(OrganisationGroup::getOrganisationGroupId)
        .anyMatch(organisationGroupIds::contains);
  }
}
