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
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.teams.Team;
import uk.co.nstauthority.fieldconsents.teams.TeamService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@Service
public class FieldEquityPartnerPermissionService {

  private final FieldEquityPartnerService fieldEquityPartnerService;
  private final TeamService teamService;
  private final ApplicationAssetService applicationAssetService;

  FieldEquityPartnerPermissionService(
      FieldEquityPartnerService fieldEquityPartnerService,
      TeamService teamService,
      ApplicationAssetService applicationAssetService
  ) {
    this.fieldEquityPartnerService = fieldEquityPartnerService;
    this.teamService = teamService;
    this.applicationAssetService = applicationAssetService;
  }

  public boolean userHasPermissionForFieldInFieldEquityPartnerTeam(
      ServiceUserDetail user,
      ApplicationVersion applicationVersion,
      Set<RolePermission> requiredPermissions
  ) {
    var organisationGroupIdsUserHasPermissionFor = getOrganisationGroupIdsUserHasPermissionFor(user, requiredPermissions);
    if (organisationGroupIdsUserHasPermissionFor.isEmpty()) {
      return false;
    }

    return fieldEquityPartnerService.getFieldsWithFieldEquityPartners(applicationVersion)
        .stream()
        .anyMatch(field ->
            fieldHasAnyFieldEquityPartnerWithOrganisationGroupIdIn(field, organisationGroupIdsUserHasPermissionFor));
  }

  public boolean userHasPermissionForFieldInFieldEquityPartnerTeam(
      ServiceUserDetail user,
      Integer fieldId,
      Set<RolePermission> requiredPermissions
  ) {
    var organisationGroupIdsUserHasPermissionFor = getOrganisationGroupIdsUserHasPermissionFor(user, requiredPermissions);
    if (organisationGroupIdsUserHasPermissionFor.isEmpty()) {
      return false;
    }

    return fieldEquityPartnerService.getFieldWithFieldEquityPartners(fieldId)
        .map(field -> fieldHasAnyFieldEquityPartnerWithOrganisationGroupIdIn(field, organisationGroupIdsUserHasPermissionFor))
        .orElse(false);
  }

  public List<Integer> getFieldIdsUserHasPermissionForInFieldEquityPartnerTeam(
      ServiceUserDetail user,
      Set<RolePermission> requiredPermissions
  ) {
    var organisationGroupIdsUserHasPermissionFor = getOrganisationGroupIdsUserHasPermissionFor(user, requiredPermissions);
    if (organisationGroupIdsUserHasPermissionFor.isEmpty()) {
      return List.of();
    }

    var allFieldIds = applicationAssetService.getAllPrimaryAndSecondaryFieldAssets()
        .stream()
        .map(ApplicationAsset::getAssetId)
        .distinct()
        .toList();

    return fieldEquityPartnerService.getFieldsWithFieldEquityPartners(allFieldIds)
        .stream()
        .filter(field -> fieldHasAnyFieldEquityPartnerWithOrganisationGroupIdIn(field, organisationGroupIdsUserHasPermissionFor))
        .map(Field::getFieldId)
        .toList();
  }

  public List<Integer> getFieldIdsUserHasPermissionForInFieldEquityPartnerTeam(
      ServiceUserDetail user,
      List<Integer> fieldIds,
      Set<RolePermission> requiredPermissions
  ) {
    var organisationGroupIdsUserHasPermissionFor = getOrganisationGroupIdsUserHasPermissionFor(user, requiredPermissions);
    if (organisationGroupIdsUserHasPermissionFor.isEmpty()) {
      return List.of();
    }

    return fieldEquityPartnerService.getFieldsWithFieldEquityPartners(fieldIds)
        .stream()
        .filter(field -> fieldHasAnyFieldEquityPartnerWithOrganisationGroupIdIn(field, organisationGroupIdsUserHasPermissionFor))
        .map(Field::getFieldId)
        .toList();
  }

  Set<Integer> getOrganisationGroupIdsUserHasPermissionFor(ServiceUserDetail user, Set<RolePermission> requiredPermissions) {
    return teamService.getTeamsOfTypeThatUserHasPermissionFor(user, TeamType.INDUSTRY, requiredPermissions)
        .stream()
        .map(Team::getOrganisationGroupId)
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
