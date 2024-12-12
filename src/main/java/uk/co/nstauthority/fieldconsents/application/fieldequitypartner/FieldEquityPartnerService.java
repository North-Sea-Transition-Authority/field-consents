package uk.co.nstauthority.fieldconsents.application.fieldequitypartner;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.client.field.FieldApi;
import uk.co.fivium.energyportalapi.generated.client.FieldProjectionRoot;
import uk.co.fivium.energyportalapi.generated.client.FieldsProjectionRoot;
import uk.co.fivium.energyportalapi.generated.types.Field;
import uk.co.fivium.energyportalapi.generated.types.OrganisationGroup;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAsset;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.assets.AssetRole;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.teams.Role;
import uk.co.nstauthority.fieldconsents.teams.TeamQueryService;
import uk.co.nstauthority.fieldconsents.teams.TeamRole;
import uk.co.nstauthority.fieldconsents.teams.TeamScopeReference;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@Service
public class FieldEquityPartnerService {

  private static final RequestPurpose FIELD_EQUITY_PARTNER_LOOKUP_REQUEST_PURPOSE =
      new RequestPurpose("Field equity partner names lookup");

  private final ApplicationAssetService applicationAssetService;
  private final FieldApi fieldApi;
  private final TeamQueryService teamQueryService;

  FieldEquityPartnerService(
      ApplicationAssetService applicationAssetService,
      FieldApi fieldApi,
      TeamQueryService teamQueryService
  ) {
    this.applicationAssetService = applicationAssetService;
    this.fieldApi = fieldApi;
    this.teamQueryService = teamQueryService;
  }

  public FieldEquityPartnersView getFieldEquityPartnersView(ApplicationVersion applicationVersion) {
    var fields = getFieldsWithFieldEquityPartners(applicationVersion);
    var formattedFieldEquityPartners = getFormattedFieldEquityPartners(fields);
    var organisationGroupNamesWithoutConsentRecipients = getOrganisationGroupNamesWithoutConsentRecipients(fields);

    return new FieldEquityPartnersView(formattedFieldEquityPartners, organisationGroupNamesWithoutConsentRecipients);
  }

  public List<FormattedFieldEquityPartner> getFormattedFieldEquityPartners(ApplicationVersion applicationVersion) {
    var fields = getFieldsWithFieldEquityPartners(applicationVersion);
    return getFormattedFieldEquityPartners(fields);
  }

  public List<Field> getFieldsWithFieldEquityPartners(ApplicationVersion applicationVersion) {
    var fieldApplicationAssets = applicationAssetService.findAssetsByApplicationVersionAndAssetTypeAndAssetRoles(
        applicationVersion,
        AssetType.FIELD,
        EnumSet.of(AssetRole.PRIMARY, AssetRole.SECONDARY)
    );

    if (fieldApplicationAssets.stream().noneMatch(ApplicationAsset::isPrimary)) {
      throw new IllegalStateException("Unable to find a primary field application asset for application version [%s]"
          .formatted(applicationVersion.getId()));
    }

    var fieldIds = fieldApplicationAssets.stream()
        .map(ApplicationAsset::getAssetId)
        .distinct()
        .toList();

    return getFieldsWithFieldEquityPartners(fieldIds);
  }

  public List<Field> getFieldsWithFieldEquityPartners(List<Integer> fieldIds) {
    // @formatter:off
    var query = new FieldsProjectionRoot()
        .fieldId()
        .fieldEquityPartners()
          .organisationUnit()
            .organisationUnitId()
            .name()
            .registeredNumber()
            .organisationGroups()
              .organisationGroupId()
              .name()
        .root();
    // @formatter:on

    return fieldApi.getFieldsByIds(fieldIds, query, FIELD_EQUITY_PARTNER_LOOKUP_REQUEST_PURPOSE);
  }

  public Optional<Field> getFieldWithFieldEquityPartners(Integer fieldId) {
    // @formatter:off
    var query = new FieldProjectionRoot()
        .fieldId()
        .fieldEquityPartners()
          .organisationUnit()
            .organisationUnitId()
            .name()
            .registeredNumber()
            .organisationGroups()
              .organisationGroupId()
              .name()
        .root();
    // @formatter:on

    return fieldApi.findFieldById(fieldId, query, FIELD_EQUITY_PARTNER_LOOKUP_REQUEST_PURPOSE);
  }

  List<FormattedFieldEquityPartner> getFormattedFieldEquityPartners(List<Field> fields) {
    return fields
        .stream()
        .flatMap(field -> field.getFieldEquityPartners().stream())
        .map(FormattedFieldEquityPartner::from)
        .distinct()
        .sorted()
        .toList();
  }

  List<String> getOrganisationGroupNamesWithoutConsentRecipients(List<Field> fields) {
    var organisationGroups = fields
        .stream()
        .flatMap(field -> field.getFieldEquityPartners().stream())
        .flatMap(fieldEquityPartner -> fieldEquityPartner.getOrganisationUnit().getOrganisationGroups().stream())
        .collect(Collectors.toSet());

    var teamScopeIds = organisationGroups
        .stream()
        .map(OrganisationGroup::getOrganisationGroupId)
        .map(String::valueOf)
        .collect(Collectors.toSet());

    var rolesByTeamScopeId = teamQueryService
        .getTeamRoles(TeamType.INDUSTRY, TeamScopeReference.ORGANISATION_GROUP_ID, teamScopeIds)
        .stream()
        .collect(Collectors.groupingBy(
            teamRole -> teamRole.getTeam().getScopeId(),
            Collectors.mapping(TeamRole::getRole, Collectors.toSet())
        ));

    var teamScopeIdsWithoutConsentRecipients = new HashSet<String>();

    for (var teamScopeId : teamScopeIds) {
      if (!rolesByTeamScopeId.containsKey(teamScopeId)) {
        teamScopeIdsWithoutConsentRecipients.add(teamScopeId);
      }
    }

    for (var entry : rolesByTeamScopeId.entrySet()) {
      if (entry.getValue().stream().noneMatch(role -> role == Role.CONSENT_RECIPIENT)) {
        teamScopeIdsWithoutConsentRecipients.add(entry.getKey());
      }
    }

    return organisationGroups
        .stream()
        .filter(organisationGroup ->
            teamScopeIdsWithoutConsentRecipients.contains(organisationGroup.getOrganisationGroupId().toString()))
        .map(OrganisationGroup::getName)
        .sorted()
        .toList();
  }
}
