package uk.co.nstauthority.fieldconsents.assets.fields;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldService.fieldStatusesAllowed;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1Json;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1JsonWithOperator;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1WithNoOperatorButLicences;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1WithOperator;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field2;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field2Json;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field2JsonWithOperator;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field2WithOperator;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field3Json;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field3JsonWithOperator;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.fieldIdList;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.fieldList;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.fieldsWithOperatorList;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit1Json;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit2Json;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.client.field.FieldApi;
import uk.co.fivium.energyportalapi.generated.client.FieldsProjectionRoot;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.authorisation.FieldEquityPartnerPermissionService;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitPermissionService;
import uk.co.nstauthority.fieldconsents.teams.Team;
import uk.co.nstauthority.fieldconsents.teams.TeamService;
import uk.co.nstauthority.fieldconsents.teams.TeamTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@ExtendWith(MockitoExtension.class)
class FieldSearchServiceTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  private static final String REQUEST_PURPOSE = "Field search service test";

  @Mock
  private FieldApi fieldApi;

  @Mock
  private TeamService teamService;

  @Mock
  private OrganisationUnitPermissionService organisationUnitPermissionService;

  @Mock
  private FieldEquityPartnerPermissionService fieldEquityPartnerPermissionService;

  @InjectMocks
  private FieldSearchService fieldSearchService;

  private final RequestPurpose requestPurpose = new RequestPurpose(REQUEST_PURPOSE);

  private final Team regulatorTeam = TeamTestUtil.Builder().withTeamType(TeamType.REGULATOR).build();

  private final Team consulteeTeam = TeamTestUtil.Builder().withTeamType(TeamType.OPRED).build();

  @Test
  void searchFields_allTestFields() {
    when(fieldApi.searchFields(eq("F"), eq(fieldStatusesAllowed),
        any(FieldsProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(fieldList);

    List<FieldJson> allTestFields = fieldSearchService.searchFields("F", REQUEST_PURPOSE);
    assertThat(allTestFields).hasSize(3);
    assertThat(allTestFields.get(0)).usingRecursiveComparison()
        .isEqualTo(field1Json);
    assertThat(allTestFields.get(1)).usingRecursiveComparison()
        .isEqualTo(field2Json);
    assertThat(allTestFields.get(2)).usingRecursiveComparison()
        .isEqualTo(field3Json);
  }

  @Test
  void searchFields_singleTestField() {
    when(fieldApi.searchFields(eq("F2"), eq(fieldStatusesAllowed),
        any(FieldsProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(List.of(field2));

    List<FieldJson> singleTestField = fieldSearchService.searchFields("F2", REQUEST_PURPOSE);
    assertThat(singleTestField).hasSize(1);
    assertThat(singleTestField.get(0)).usingRecursiveComparison()
        .isEqualTo(field2Json);
  }

  @Test
  void searchFieldsWithOperatorForUser_regulatorUser_allTestFields() {
    when(fieldApi.searchFields(eq("F"), eq(fieldStatusesAllowed),
        any(FieldsProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(fieldsWithOperatorList);
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(USER, TeamType.REGULATOR, RolePermission.VIEW_PERMISSIONS))
        .thenReturn(List.of(regulatorTeam));

    assertThat(fieldSearchService.searchFieldsWithOperatorForUser("F", REQUEST_PURPOSE, USER))
        .usingRecursiveComparison()
        .isEqualTo(List.of(field1JsonWithOperator, field2JsonWithOperator, field3JsonWithOperator));
  }

  @Test
  void searchFieldsWithOperatorForUser_consulteeUser_allTestFields() {
    when(fieldApi.searchFields(eq("F"), eq(fieldStatusesAllowed),
        any(FieldsProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(fieldsWithOperatorList);
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(USER, TeamType.REGULATOR, RolePermission.VIEW_PERMISSIONS))
        .thenReturn(Collections.emptyList());
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(USER, TeamType.OPRED, RolePermission.VIEW_PERMISSIONS))
        .thenReturn(List.of(consulteeTeam));

    assertThat(fieldSearchService.searchFieldsWithOperatorForUser("F", REQUEST_PURPOSE, USER))
        .usingRecursiveComparison()
        .isEqualTo(List.of(field1JsonWithOperator, field2JsonWithOperator, field3JsonWithOperator));
  }

  @Test
  void searchFieldsWithOperatorForUser_industryUser_twoFields() {
    when(fieldApi.searchFields(eq("F"), eq(fieldStatusesAllowed),
        any(FieldsProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(fieldsWithOperatorList);
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(USER, TeamType.REGULATOR, RolePermission.VIEW_PERMISSIONS))
        .thenReturn(Collections.emptyList());
    when(organisationUnitPermissionService.getOperatorsUserHasPermissionsFor(USER, RolePermission.VIEW_PERMISSIONS))
        .thenReturn(List.of(orgUnit1Json, orgUnit2Json));

    assertThat(fieldSearchService.searchFieldsWithOperatorForUser("F", REQUEST_PURPOSE, USER))
        .usingRecursiveComparison()
        .isEqualTo(List.of(field1JsonWithOperator, field2JsonWithOperator));
  }

  @Test
  void searchFieldsWithOperatorForUser_regulatorUser_singleTestField() {
    when(fieldApi.searchFields(eq("F2"), eq(fieldStatusesAllowed),
        any(FieldsProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(List.of(field2WithOperator));
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(USER, TeamType.REGULATOR, RolePermission.VIEW_PERMISSIONS))
        .thenReturn(List.of(regulatorTeam));

    assertThat(fieldSearchService.searchFieldsWithOperatorForUser("F2", REQUEST_PURPOSE, USER))
        .usingRecursiveComparison()
        .isEqualTo(List.of(field2JsonWithOperator));
  }

  @Test
  void searchFieldsWithOperatorForUser_consulteeUser_singleTestField() {
    when(fieldApi.searchFields(eq("F2"), eq(fieldStatusesAllowed),
        any(FieldsProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(List.of(field2WithOperator));
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(USER, TeamType.REGULATOR, RolePermission.VIEW_PERMISSIONS))
        .thenReturn(Collections.emptyList());
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(USER, TeamType.OPRED, RolePermission.VIEW_PERMISSIONS))
        .thenReturn(List.of(consulteeTeam));

    assertThat(fieldSearchService.searchFieldsWithOperatorForUser("F2", REQUEST_PURPOSE, USER))
        .usingRecursiveComparison()
        .isEqualTo(List.of(field2JsonWithOperator));
  }

  @Test
  void searchFieldsWithOperatorForUser_industryUser_singleField() {
    when(fieldApi.searchFields(eq("F"), eq(fieldStatusesAllowed),
        any(FieldsProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(fieldsWithOperatorList);
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(USER, TeamType.REGULATOR, RolePermission.VIEW_PERMISSIONS))
        .thenReturn(Collections.emptyList());
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(USER, TeamType.OPRED, RolePermission.VIEW_PERMISSIONS))
        .thenReturn(Collections.emptyList());

    when(organisationUnitPermissionService.getOperatorsUserHasPermissionsFor(USER, RolePermission.VIEW_PERMISSIONS))
        .thenReturn(List.of(orgUnit1Json));

    assertThat(fieldSearchService.searchFieldsWithOperatorForUser("F", REQUEST_PURPOSE, USER))
        .usingRecursiveComparison()
        .isEqualTo(List.of(field1JsonWithOperator));
  }

  @Test
  void searchFieldsWithOperatorForUser_industryUser_singleField_userHasViewFcsPermissionForInFieldEquityPartnerTeam() {
    when(fieldApi.searchFields(eq("F"), eq(fieldStatusesAllowed),
        any(FieldsProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(fieldsWithOperatorList);
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(USER, TeamType.REGULATOR, RolePermission.VIEW_PERMISSIONS))
        .thenReturn(Collections.emptyList());
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(USER, TeamType.OPRED, RolePermission.VIEW_PERMISSIONS))
        .thenReturn(Collections.emptyList());

    when(organisationUnitPermissionService.getOperatorsUserHasPermissionsFor(USER, RolePermission.VIEW_PERMISSIONS))
        .thenReturn(List.of());

    when(fieldEquityPartnerPermissionService.getFieldIdsUserHasPermissionForInFieldEquityPartnerTeam(USER, fieldIdList, Set.of(RolePermission.VIEW_FCS_CONSENTS)))
        .thenReturn(List.of(field1.getFieldId()));

    assertThat(fieldSearchService.searchFieldsWithOperatorForUser("F", REQUEST_PURPOSE, USER))
        .usingRecursiveComparison()
        .isEqualTo(List.of(field1JsonWithOperator));
  }

  @Test
  void searchFieldsWithOperatorForUser_industryUser_noPermissions() {
    when(fieldApi.searchFields(eq("F"), eq(fieldStatusesAllowed),
        any(FieldsProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(List.of(field1WithOperator));
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(USER, TeamType.REGULATOR, RolePermission.VIEW_PERMISSIONS))
        .thenReturn(Collections.emptyList());
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(USER, TeamType.OPRED, RolePermission.VIEW_PERMISSIONS))
        .thenReturn(Collections.emptyList());
    when(organisationUnitPermissionService.getOperatorsUserHasPermissionsFor(USER, RolePermission.VIEW_PERMISSIONS))
        .thenReturn(List.of(orgUnit2Json));

    assertThat(fieldSearchService.searchFieldsWithOperatorForUser("F", REQUEST_PURPOSE, USER))
        .usingRecursiveComparison()
        .isEqualTo(Collections.emptyList());
  }

  @Test
  void searchFieldsWithOperatorForUser_industryUser_noOperator() {
    when(fieldApi.searchFields(eq("F"), eq(fieldStatusesAllowed),
        any(FieldsProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(List.of(field1WithNoOperatorButLicences));
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(USER, TeamType.REGULATOR, RolePermission.VIEW_PERMISSIONS))
        .thenReturn(Collections.emptyList());
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(USER, TeamType.OPRED, RolePermission.VIEW_PERMISSIONS))
        .thenReturn(Collections.emptyList());
    when(organisationUnitPermissionService.getOperatorsUserHasPermissionsFor(USER, RolePermission.VIEW_PERMISSIONS))
        .thenReturn(List.of(orgUnit1Json));

    assertThat(fieldSearchService.searchFieldsWithOperatorForUser("F", REQUEST_PURPOSE, USER))
        .usingRecursiveComparison()
        .isEqualTo(Collections.emptyList());
  }
}
