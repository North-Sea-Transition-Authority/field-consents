package uk.co.nstauthority.fieldconsents.assets.fields;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldService.ALL_FIELD_STATUSES;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldService.FIELD_STATUSES_ALLOWED;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1Json;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1JsonWithOperator;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1WithNoOperatorButLicences;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1WithOperator;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field2;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field2Json;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field2JsonWithOperator;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field2WithOperator;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field3;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field3Json;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field3JsonWithOperator;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.fieldIdList;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.fieldList;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.fieldsWithOperatorList;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit1Json;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit2Json;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit3Json;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.client.field.FieldApi;
import uk.co.fivium.energyportalapi.generated.client.FieldsProjectionRoot;
import uk.co.fivium.energyportalapi.generated.types.Field;
import uk.co.fivium.energyportalapi.generated.types.FieldGeographicArea;
import uk.co.fivium.energyportalapi.generated.types.FieldShore;
import uk.co.fivium.energyportalapi.generated.types.FieldStatus;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.RoleGroup;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.authorisation.FieldEquityPartnerAccessService;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitPermissionService;
import uk.co.nstauthority.fieldconsents.teams.Role;
import uk.co.nstauthority.fieldconsents.teams.TeamQueryService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@ExtendWith(MockitoExtension.class)
class FieldSearchServiceTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  private static final String REQUEST_PURPOSE = "Field search service test";

  private static final Set<Role> CONSULTEE_ROLES = EnumSet.of(Role.ALLOCATOR, Role.RESPONDER, Role.VIEWER);

  @Mock
  private FieldApi fieldApi;

  @Mock
  private TeamQueryService teamQueryService;

  @Mock
  private OrganisationUnitPermissionService organisationUnitPermissionService;

  @Mock
  private FieldEquityPartnerAccessService fieldEquityPartnerAccessService;

  @Mock
  private ApplicationAssetService applicationAssetService;

  @InjectMocks
  private FieldSearchService fieldSearchService;

  private final RequestPurpose requestPurpose = new RequestPurpose(REQUEST_PURPOSE);

  @Test
  void searchFields_allTestFields() {
    when(fieldApi.searchFields(eq("F"), eq(FIELD_STATUSES_ALLOWED), any(FieldsProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(fieldList);

    assertThat(fieldSearchService.searchFields("F", REQUEST_PURPOSE))
        .usingRecursiveComparison()
        .isEqualTo(List.of(field1Json, field2Json, field3Json));
  }

  @Test
  void searchFields_includeInUseFieldIds() {
    var defaultFieldBuilder = Field.newBuilder().geographicArea(FieldGeographicArea.CNS).shore(FieldShore.OFFSHORE);
    var completeFieldList = List.of(
        defaultFieldBuilder.fieldId(1).status(FieldStatus.STATUS500).build(),
        defaultFieldBuilder.fieldId(2).status(FieldStatus.STATUS600).build(),
        defaultFieldBuilder.fieldId(3).status(FieldStatus.UNKNOWN).build()
    );
    var completeFieldIdSet = completeFieldList.stream().map(Field::getFieldId).collect(Collectors.toSet());
    var completeFieldJsonList = completeFieldList.stream().map(FieldWithOperatorJson::from).toList();

    var fieldsWithAllowedStatuses = List.of(completeFieldList.get(0), completeFieldList.get(1));

    when(fieldApi.searchFields(eq("F"), eq(FIELD_STATUSES_ALLOWED), any(FieldsProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(fieldsWithAllowedStatuses);

    // field 3 doesn't have an ALLOWED_FIELD_STATUSES status, but should still be included because it's used by the service
    when(applicationAssetService.getAllUniqueAssetIdsForAssetType(AssetType.FIELD))
        .thenReturn(completeFieldIdSet);
    when(fieldApi.searchFields(eq("F"), eq(ALL_FIELD_STATUSES), eq(List.of(completeFieldList.get(2).getFieldId())), any(FieldsProjectionRoot.class), eq(requestPurpose), eq(null)))
        .thenReturn(List.of(completeFieldList.get(2)));

    assertThat(fieldSearchService.searchFields("F", REQUEST_PURPOSE))
        .usingRecursiveComparison()
        .isEqualTo(completeFieldJsonList);
  }

  @Test
  void searchFields_singleTestField() {
    when(fieldApi.searchFields(eq("F2"), eq(FIELD_STATUSES_ALLOWED), any(FieldsProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(List.of(field2));

    List<FieldJson> singleTestField = fieldSearchService.searchFields("F2", REQUEST_PURPOSE);
    assertThat(singleTestField).hasSize(1);
    assertThat(singleTestField.getFirst()).usingRecursiveComparison().isEqualTo(field2Json);
  }

  @Test
  void searchFieldsWithOperatorForUser_regulatorUser_allTestFields() {
    when(fieldApi.searchFields(eq("F"), eq(FIELD_STATUSES_ALLOWED), any(FieldsProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(fieldsWithOperatorList);
    when(teamQueryService.userHasAtLeastOneStaticRole(USER, TeamType.REGULATOR, RoleGroup.REGULATOR_VIEW_CASE_PROCESSING_ROLES))
        .thenReturn(true);

    assertThat(fieldSearchService.searchFieldsWithOperatorForUser("F", REQUEST_PURPOSE, USER))
        .usingRecursiveComparison()
        .isEqualTo(List.of(field1JsonWithOperator, field2JsonWithOperator, field3JsonWithOperator));
  }

  @Test
  void searchFieldsWithOperatorForUser_consulteeUser_allTestFields() {
    when(fieldApi.searchFields(eq("F"), eq(FIELD_STATUSES_ALLOWED), any(FieldsProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(fieldsWithOperatorList);
    when(teamQueryService.userHasAtLeastOneStaticRole(USER, TeamType.REGULATOR, RoleGroup.REGULATOR_VIEW_CASE_PROCESSING_ROLES))
        .thenReturn(false);
    when(teamQueryService.userHasAtLeastOneStaticRole(USER, TeamType.CONSULTEE, CONSULTEE_ROLES))
        .thenReturn(true);

    assertThat(fieldSearchService.searchFieldsWithOperatorForUser("F", REQUEST_PURPOSE, USER))
        .usingRecursiveComparison()
        .isEqualTo(List.of(field1JsonWithOperator, field2JsonWithOperator, field3JsonWithOperator));
  }

  @Test
  void searchFieldsWithOperatorForUser_industryUser_twoFields() {
    when(fieldApi.searchFields(eq("F"), eq(FIELD_STATUSES_ALLOWED), any(FieldsProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(fieldsWithOperatorList);
    when(teamQueryService.userHasAtLeastOneStaticRole(USER, TeamType.REGULATOR, RoleGroup.REGULATOR_VIEW_CASE_PROCESSING_ROLES))
        .thenReturn(false);
    when(organisationUnitPermissionService.getOperatorsUserHasRoleFor(USER, RoleGroup.INDUSTRY_VIEW_CASE_PROCESSING_ROLES))
        .thenReturn(List.of(orgUnit1Json, orgUnit2Json));

    assertThat(fieldSearchService.searchFieldsWithOperatorForUser("F", REQUEST_PURPOSE, USER))
        .usingRecursiveComparison()
        .isEqualTo(List.of(field1JsonWithOperator, field2JsonWithOperator));
  }

  @Test
  void searchFieldsWithOperatorForUser_regulatorUser_singleTestField() {
    when(fieldApi.searchFields(eq("F2"), eq(FIELD_STATUSES_ALLOWED), any(FieldsProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(List.of(field2WithOperator));
    when(teamQueryService.userHasAtLeastOneStaticRole(USER, TeamType.REGULATOR, RoleGroup.REGULATOR_VIEW_CASE_PROCESSING_ROLES))
        .thenReturn(true);

    assertThat(fieldSearchService.searchFieldsWithOperatorForUser("F2", REQUEST_PURPOSE, USER))
        .usingRecursiveComparison()
        .isEqualTo(List.of(field2JsonWithOperator));
  }

  @Test
  void searchFieldsWithOperatorForUser_consulteeUser_singleTestField() {
    when(fieldApi.searchFields(eq("F2"), eq(FIELD_STATUSES_ALLOWED), any(FieldsProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(List.of(field2WithOperator));
    when(teamQueryService.userHasAtLeastOneStaticRole(USER, TeamType.REGULATOR, RoleGroup.REGULATOR_VIEW_CASE_PROCESSING_ROLES))
        .thenReturn(false);
    when(teamQueryService.userHasAtLeastOneStaticRole(USER, TeamType.CONSULTEE, CONSULTEE_ROLES))
        .thenReturn(true);

    assertThat(fieldSearchService.searchFieldsWithOperatorForUser("F2", REQUEST_PURPOSE, USER))
        .usingRecursiveComparison()
        .isEqualTo(List.of(field2JsonWithOperator));
  }

  @Test
  void searchFieldsWithOperatorForUser_industryUser_singleField() {
    when(fieldApi.searchFields(eq("F"), eq(FIELD_STATUSES_ALLOWED), any(FieldsProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(fieldsWithOperatorList);
    when(teamQueryService.userHasAtLeastOneStaticRole(USER, TeamType.REGULATOR, RoleGroup.REGULATOR_VIEW_CASE_PROCESSING_ROLES))
        .thenReturn(false);
    when(teamQueryService.userHasAtLeastOneStaticRole(USER, TeamType.CONSULTEE, CONSULTEE_ROLES))
        .thenReturn(false);

    when(organisationUnitPermissionService.getOperatorsUserHasRoleFor(USER, RoleGroup.INDUSTRY_VIEW_CASE_PROCESSING_ROLES))
        .thenReturn(List.of(orgUnit1Json));

    assertThat(fieldSearchService.searchFieldsWithOperatorForUser("F", REQUEST_PURPOSE, USER))
        .usingRecursiveComparison()
        .isEqualTo(List.of(field1JsonWithOperator));
  }

  @Test
  void searchFieldsWithOperatorForUser_industryUser_allFields_remainingFieldIdsEmpty() {
    when(fieldApi.searchFields(eq("F"), eq(FIELD_STATUSES_ALLOWED), any(FieldsProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(fieldsWithOperatorList);
    when(teamQueryService.userHasAtLeastOneStaticRole(USER, TeamType.REGULATOR, RoleGroup.REGULATOR_VIEW_CASE_PROCESSING_ROLES))
        .thenReturn(false);
    when(teamQueryService.userHasAtLeastOneStaticRole(USER, TeamType.CONSULTEE, CONSULTEE_ROLES))
        .thenReturn(false);

    when(organisationUnitPermissionService.getOperatorsUserHasRoleFor(USER, RoleGroup.INDUSTRY_VIEW_CASE_PROCESSING_ROLES))
        .thenReturn(List.of(orgUnit1Json, orgUnit2Json, orgUnit3Json));

    assertThat(fieldSearchService.searchFieldsWithOperatorForUser("F", REQUEST_PURPOSE, USER))
        .usingRecursiveComparison()
        .isEqualTo(List.of(field1JsonWithOperator, field2JsonWithOperator, field3JsonWithOperator));
  }

  @Test
  void searchFieldsWithOperatorForUser_industryUser_twoFields_userHasViewFcsPermissionForInOperatorTeamAndFieldEquityPartnerTeam() {
    when(fieldApi.searchFields(eq("F"), eq(FIELD_STATUSES_ALLOWED), any(FieldsProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(fieldsWithOperatorList);
    when(teamQueryService.userHasAtLeastOneStaticRole(USER, TeamType.REGULATOR, RoleGroup.REGULATOR_VIEW_CASE_PROCESSING_ROLES))
        .thenReturn(false);
    when(teamQueryService.userHasAtLeastOneStaticRole(USER, TeamType.CONSULTEE, CONSULTEE_ROLES))
        .thenReturn(false);

    when(organisationUnitPermissionService.getOperatorsUserHasRoleFor(USER, RoleGroup.INDUSTRY_VIEW_CASE_PROCESSING_ROLES))
        .thenReturn(List.of(orgUnit2Json));

    when(fieldEquityPartnerAccessService.getFieldIdsWhereUserIsFieldEquityPartner(USER, List.of(field1.getFieldId(), field3.getFieldId())))
        .thenReturn(List.of(field1.getFieldId()));

    assertThat(fieldSearchService.searchFieldsWithOperatorForUser("F", REQUEST_PURPOSE, USER))
        .usingRecursiveComparison()
        .isEqualTo(List.of(field1JsonWithOperator, field2JsonWithOperator));
  }


  @Test
  void searchFieldsWithOperatorForUser_industryUser_singleField_userHasViewFcsPermissionForInFieldEquityPartnerTeam() {
    when(fieldApi.searchFields(eq("F"), eq(FIELD_STATUSES_ALLOWED), any(FieldsProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(fieldsWithOperatorList);
    when(teamQueryService.userHasAtLeastOneStaticRole(USER, TeamType.REGULATOR, RoleGroup.REGULATOR_VIEW_CASE_PROCESSING_ROLES))
        .thenReturn(false);
    when(teamQueryService.userHasAtLeastOneStaticRole(USER, TeamType.CONSULTEE, CONSULTEE_ROLES))
        .thenReturn(false);

    when(organisationUnitPermissionService.getOperatorsUserHasRoleFor(USER, RoleGroup.INDUSTRY_VIEW_CASE_PROCESSING_ROLES))
        .thenReturn(List.of());

    when(fieldEquityPartnerAccessService.getFieldIdsWhereUserIsFieldEquityPartner(USER, fieldIdList))
        .thenReturn(List.of(field1.getFieldId()));

    assertThat(fieldSearchService.searchFieldsWithOperatorForUser("F", REQUEST_PURPOSE, USER))
        .usingRecursiveComparison()
        .isEqualTo(List.of(field1JsonWithOperator));
  }

  @Test
  void searchFieldsWithOperatorForUser_industryUser_noPermissions() {
    when(fieldApi.searchFields(eq("F"), eq(FIELD_STATUSES_ALLOWED), any(FieldsProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(List.of(field1WithOperator));
    when(teamQueryService.userHasAtLeastOneStaticRole(USER, TeamType.REGULATOR, RoleGroup.REGULATOR_VIEW_CASE_PROCESSING_ROLES))
        .thenReturn(false);
    when(teamQueryService.userHasAtLeastOneStaticRole(USER, TeamType.CONSULTEE, CONSULTEE_ROLES))
        .thenReturn(false);
    when(organisationUnitPermissionService.getOperatorsUserHasRoleFor(USER, RoleGroup.INDUSTRY_VIEW_CASE_PROCESSING_ROLES))
        .thenReturn(List.of(orgUnit2Json));

    assertThat(fieldSearchService.searchFieldsWithOperatorForUser("F", REQUEST_PURPOSE, USER))
        .usingRecursiveComparison()
        .isEqualTo(Collections.emptyList());
  }

  @Test
  void searchFieldsWithOperatorForUser_industryUser_noOperator() {
    when(fieldApi.searchFields(eq("F"), eq(FIELD_STATUSES_ALLOWED), any(FieldsProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(List.of(field1WithNoOperatorButLicences));
    when(teamQueryService.userHasAtLeastOneStaticRole(USER, TeamType.REGULATOR, RoleGroup.REGULATOR_VIEW_CASE_PROCESSING_ROLES))
        .thenReturn(false);
    when(teamQueryService.userHasAtLeastOneStaticRole(USER, TeamType.CONSULTEE, CONSULTEE_ROLES))
        .thenReturn(false);
    when(organisationUnitPermissionService.getOperatorsUserHasRoleFor(USER, RoleGroup.INDUSTRY_VIEW_CASE_PROCESSING_ROLES))
        .thenReturn(List.of(orgUnit1Json));

    assertThat(fieldSearchService.searchFieldsWithOperatorForUser("F", REQUEST_PURPOSE, USER))
        .usingRecursiveComparison()
        .isEqualTo(Collections.emptyList());
  }
}
