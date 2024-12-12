package uk.co.nstauthority.fieldconsents.assets.fields;

import static uk.co.nstauthority.fieldconsents.assets.fields.FieldService.ALL_FIELD_STATUSES;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldService.FIELD_STATUSES_ALLOWED;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldService.fieldsProjectionRoot;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldService.fieldsWithOperatorsProjectionRoot;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.client.field.FieldApi;
import uk.co.fivium.energyportalapi.generated.client.FieldsProjectionRoot;
import uk.co.fivium.energyportalapi.generated.types.Field;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.RoleGroup;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.FieldEquityPartnerAccessService;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitJson;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitPermissionService;
import uk.co.nstauthority.fieldconsents.teams.TeamQueryService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@Service
public class FieldSearchService {

  private final FieldApi fieldApi;
  private final OrganisationUnitPermissionService organisationUnitPermissionService;
  private final FieldEquityPartnerAccessService fieldEquityPartnerAccessService;
  private final ApplicationAssetService applicationAssetService;
  private final TeamQueryService teamQueryService;

  FieldSearchService(
      FieldApi fieldApi,
      OrganisationUnitPermissionService organisationUnitPermissionService,
      FieldEquityPartnerAccessService fieldEquityPartnerAccessService,
      ApplicationAssetService applicationAssetService,
      TeamQueryService teamQueryService
  ) {
    this.fieldApi = fieldApi;
    this.organisationUnitPermissionService = organisationUnitPermissionService;
    this.fieldEquityPartnerAccessService = fieldEquityPartnerAccessService;
    this.applicationAssetService = applicationAssetService;
    this.teamQueryService = teamQueryService;
  }

  public List<FieldJson> searchFields(String fieldName, String requestPurpose) {
    return searchFields(
        fieldName,
        fieldsProjectionRoot,
        new RequestPurpose(requestPurpose),
        FieldJson::from
    );
  }

  public List<FieldWithOperatorJson> searchFieldsWithOperatorForUser(
      String fieldName,
      String requestPurpose,
      ServiceUserDetail user
  ) {
    var fieldWithOperatorJsons = searchFields(
        fieldName,
        fieldsWithOperatorsProjectionRoot,
        new RequestPurpose(requestPurpose),
        FieldWithOperatorJson::from
    );

    if (teamQueryService.userHasAtLeastOneStaticRole(user, TeamType.REGULATOR, RoleGroup.REGULATOR_VIEW_CASE_PROCESSING_ROLES)) {
      return fieldWithOperatorJsons;
    }

    if (teamQueryService.userHasAtLeastOneStaticRole(user, TeamType.CONSULTEE, RoleGroup.CONSULTEE_WITH_VIEWER_ROLES)) {
      return fieldWithOperatorJsons;
    }

    // industry access

    var organisationUnitIdsUserHasPermissionFor =
        organisationUnitPermissionService.getOperatorsUserHasRoleFor(user, RoleGroup.INDUSTRY_VIEW_CASE_PROCESSING_ROLES)
            .stream()
            .map(OrganisationUnitJson::organisationUnitId)
            .toList();

    var fieldWithOperatorJsonsUserHasViewFcsPermissionForInOperatorTeam = fieldWithOperatorJsons
        .stream()
        .filter(field -> field.operatorExists()
            && organisationUnitIdsUserHasPermissionFor.contains(field.getOperatorJson().organisationUnitId()))
        .toList();

    var remainingFieldIds = fieldWithOperatorJsons
        .stream()
        .filter(fieldWithOperatorJson ->
            !fieldWithOperatorJsonsUserHasViewFcsPermissionForInOperatorTeam.contains(fieldWithOperatorJson))
        .map(FieldWithOperatorJson::getId)
        .toList();

    if (remainingFieldIds.isEmpty()) {
      return fieldWithOperatorJsonsUserHasViewFcsPermissionForInOperatorTeam;
    }

    // industry access - find addition fields user has view consent permission in a FEP team

    var fieldIdsWhereUserIsFieldEquityPartner =
        fieldEquityPartnerAccessService.getFieldIdsWhereUserIsFieldEquityPartner(user, remainingFieldIds);

    return fieldWithOperatorJsons
        .stream()
        .filter(field -> (
            fieldWithOperatorJsonsUserHasViewFcsPermissionForInOperatorTeam.contains(field)
            || fieldIdsWhereUserIsFieldEquityPartner.contains(field.getId())
            )
        )
        .toList();
  }

  private <T> List<T> searchFields(
      String fieldName,
      FieldsProjectionRoot query,
      RequestPurpose requestPurpose,
      Function<Field, T> mappingFunction
  ) {
    // search over fields with the allowed statuses
    var fieldsWithAllowedStatuses = fieldApi.searchFields(fieldName, FIELD_STATUSES_ALLOWED, query, requestPurpose);
    var fieldsIdsWithAllowedStatuses = fieldsWithAllowedStatuses.stream().map(Field::getFieldId).toList();

    // find any other FCS in use fields which don't currently have one of the allowed statuses
    var otherInUseFieldIds = applicationAssetService.getAllUniqueAssetIdsForAssetType(AssetType.FIELD)
        .stream()
        .filter(fieldId -> !fieldsIdsWithAllowedStatuses.contains(fieldId))
        .toList();

    if (otherInUseFieldIds.isEmpty()) {
      return fieldsWithAllowedStatuses
          .stream()
          .map(mappingFunction)
          .toList();
    }

    // search over the other FCS in use fields with any status
    var inUseFieldsWithAnyStatus = fieldApi
        .searchFields(fieldName, ALL_FIELD_STATUSES, otherInUseFieldIds, query, requestPurpose, null);

    return Stream.concat(fieldsWithAllowedStatuses.stream(), inUseFieldsWithAnyStatus.stream())
        .map(mappingFunction)
        .toList();
  }
}
