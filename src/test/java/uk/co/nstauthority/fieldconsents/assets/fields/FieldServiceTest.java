package uk.co.nstauthority.fieldconsents.assets.fields;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldService.fieldStatusesAllowed;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1Json;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1JsonWithOperator;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1JsonWithOperatorAndLicences;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1WithNoOperatorButLicences;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1WithOperator;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1WithOperatorAndLicences;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field2;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field2Json;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field2JsonWithOperator;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field2WithOperator;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field3Json;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field3JsonWithOperator;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.fieldList;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.fieldsWithOperatorList;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit1Json;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit2Json;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.client.field.FieldApi;
import uk.co.fivium.energyportalapi.generated.client.FieldProjectionRoot;
import uk.co.fivium.energyportalapi.generated.client.FieldsProjectionRoot;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitPermissionService;
import uk.co.nstauthority.fieldconsents.teams.Team;
import uk.co.nstauthority.fieldconsents.teams.TeamService;
import uk.co.nstauthority.fieldconsents.teams.TeamTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@ExtendWith(MockitoExtension.class)
public class FieldServiceTest {

  private static final Set<RolePermission> REQUIRED_PERMISSIONS =
      Set.of(RolePermission.VIEW_FCS_APPLICATIONS, RolePermission.VIEW_FCS_CONSENTS);

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  @Mock
  private FieldApi fieldApi;

  @Mock
  private TeamService teamService;

  @Mock
  private OrganisationUnitPermissionService organisationUnitPermissionService;

  @InjectMocks
  private FieldService fieldService;

  private static final String REQUEST_PURPOSE = "Field service test";

  private final RequestPurpose requestPurpose = new RequestPurpose(REQUEST_PURPOSE);

  private final Team regulatorTeam = TeamTestUtil.Builder().build();

  @Test
  void searchFields_allTestFields() {
    when(fieldApi.searchFields(eq("F"), eq(fieldStatusesAllowed),
        any(FieldsProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(fieldList);

    List<FieldJson> allTestFields = fieldService.searchFields("F", REQUEST_PURPOSE);
    assertThat(allTestFields).hasSize(3);
    assertThat(allTestFields.get(0)).usingRecursiveComparison()
        .isEqualTo(field1Json);
    assertThat(allTestFields.get(1)).usingRecursiveComparison()
        .isEqualTo(field2Json);
    assertThat(allTestFields.get(2)).usingRecursiveComparison()
        .isEqualTo(field3Json);
  }

  @Test
  void searchFieldsWithOperator_allTestFields() {
    when(fieldApi.searchFields(eq("F"), eq(fieldStatusesAllowed),
        any(FieldsProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(fieldsWithOperatorList);
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(USER, TeamType.REGULATOR, REQUIRED_PERMISSIONS))
        .thenReturn(List.of(regulatorTeam));

    List<FieldWithOperatorJson> allTestFields =
        fieldService.searchFieldsWithOperator("F", REQUEST_PURPOSE, USER);
    assertThat(allTestFields).hasSize(3);
    assertThat(allTestFields.get(0)).usingRecursiveComparison()
        .isEqualTo(field1JsonWithOperator);
    assertThat(allTestFields.get(1)).usingRecursiveComparison()
        .isEqualTo(field2JsonWithOperator);
    assertThat(allTestFields.get(2)).usingRecursiveComparison()
        .isEqualTo(field3JsonWithOperator);
  }

  @Test
  void searchFieldsWithOperator_industryUser_twoFields() {
    when(fieldApi.searchFields(eq("F"), eq(fieldStatusesAllowed),
        any(FieldsProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(fieldsWithOperatorList);
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(USER, TeamType.REGULATOR, REQUIRED_PERMISSIONS))
        .thenReturn(Collections.emptyList());
    when(organisationUnitPermissionService.getOperatorsUserHasPermissionsFor(USER, REQUIRED_PERMISSIONS))
        .thenReturn(List.of(orgUnit1Json, orgUnit2Json));

    assertThat(fieldService.searchFieldsWithOperator("F", REQUEST_PURPOSE, USER))
        .usingRecursiveComparison()
        .isEqualTo(List.of(field1JsonWithOperator, field2JsonWithOperator));
  }

  @Test
  void searchFields_singleTestField() {
    when(fieldApi.searchFields(eq("F2"), eq(fieldStatusesAllowed),
        any(FieldsProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(List.of(field2));

    List<FieldJson> singleTestField = fieldService.searchFields("F2", REQUEST_PURPOSE);
    assertThat(singleTestField).hasSize(1);
    assertThat(singleTestField.get(0)).usingRecursiveComparison()
        .isEqualTo(field2Json);
  }

  @Test
  void searchFieldsWithOperator_singleTestField() {
    when(fieldApi.searchFields(eq("F2"), eq(fieldStatusesAllowed),
        any(FieldsProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(List.of(field2WithOperator));
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(USER, TeamType.REGULATOR, REQUIRED_PERMISSIONS))
        .thenReturn(List.of(regulatorTeam));

    List<FieldWithOperatorJson> singleTestField =
        fieldService.searchFieldsWithOperator("F2", REQUEST_PURPOSE, USER);
    assertThat(singleTestField).hasSize(1);
    assertThat(singleTestField.get(0)).usingRecursiveComparison()
        .isEqualTo(field2JsonWithOperator);
  }

  @Test
  void searchFieldsWithOperator_industryUser_singleField() {
    when(fieldApi.searchFields(eq("F"), eq(fieldStatusesAllowed),
        any(FieldsProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(fieldsWithOperatorList);
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(USER, TeamType.REGULATOR, REQUIRED_PERMISSIONS))
        .thenReturn(Collections.emptyList());
    when(organisationUnitPermissionService.getOperatorsUserHasPermissionsFor(USER, REQUIRED_PERMISSIONS))
        .thenReturn(List.of(orgUnit1Json));

    assertThat(fieldService.searchFieldsWithOperator("F", REQUEST_PURPOSE, USER))
        .usingRecursiveComparison()
        .isEqualTo(List.of(field1JsonWithOperator));
  }

  @Test
  void searchFieldsWithOperator_industryUser_noPermissions() {
    when(fieldApi.searchFields(eq("F"), eq(fieldStatusesAllowed),
        any(FieldsProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(List.of(field1WithOperator));
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(USER, TeamType.REGULATOR, REQUIRED_PERMISSIONS))
        .thenReturn(Collections.emptyList());
    when(organisationUnitPermissionService.getOperatorsUserHasPermissionsFor(USER, REQUIRED_PERMISSIONS))
        .thenReturn(List.of(orgUnit2Json));

    assertThat(fieldService.searchFieldsWithOperator("F", REQUEST_PURPOSE, USER))
        .usingRecursiveComparison()
        .isEqualTo(Collections.emptyList());
  }

  @Test
  void searchFieldsWithOperator_industryUser_noOperator() {
    when(fieldApi.searchFields(eq("F"), eq(fieldStatusesAllowed),
        any(FieldsProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(List.of(field1WithNoOperatorButLicences));
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(USER, TeamType.REGULATOR, REQUIRED_PERMISSIONS))
        .thenReturn(Collections.emptyList());
    when(organisationUnitPermissionService.getOperatorsUserHasPermissionsFor(USER, REQUIRED_PERMISSIONS))
        .thenReturn(List.of(orgUnit1Json));

    assertThat(fieldService.searchFieldsWithOperator("F", REQUEST_PURPOSE, USER))
        .usingRecursiveComparison()
        .isEqualTo(Collections.emptyList());
  }

  @Test
  void findField_fieldExists() {
    when(fieldApi.findFieldById(eq(field1.getFieldId()), any(FieldProjectionRoot.class), any(RequestPurpose.class)))
        .thenReturn(Optional.of(field1));

    var fieldJsonOptional = fieldService.findField(field1.getFieldId(), REQUEST_PURPOSE);
    assertThat(fieldJsonOptional).usingRecursiveComparison()
        .isEqualTo(Optional.of(field1Json));
  }

  @Test
  void findFieldWithOperator_fieldExists() {
    when(fieldApi.findFieldById(eq(field1WithOperator.getFieldId()), any(FieldProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(Optional.of(field1WithOperator));

    var fieldJsonOptional = fieldService.findFieldWithOperator(field1WithOperator.getFieldId(), REQUEST_PURPOSE);
    assertThat(fieldJsonOptional).usingRecursiveComparison()
        .isEqualTo(Optional.of(field1JsonWithOperator));
  }

  @Test
  void findField_fieldNotExists() {
    when(fieldApi.findFieldById(eq(0), any(FieldProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(Optional.empty());

    var fieldJsonOptional = fieldService.findField(0, REQUEST_PURPOSE);
    assertThat(fieldJsonOptional).isEqualTo(Optional.empty());
  }

  @Test
  void findFieldWithOperator_fieldNotExists() {
    when(fieldApi.findFieldById(eq(0), any(FieldProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(Optional.empty());

    var fieldJsonOptional = fieldService.findFieldWithOperator(0, REQUEST_PURPOSE);
    assertThat(fieldJsonOptional).isNotPresent();
  }

  @Test
  void getField_fieldExists() {
    when(fieldApi.findFieldById(eq(field1.getFieldId()), any(FieldProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(Optional.of(field1));

    var fieldJson = fieldService.getField(field1.getFieldId(), REQUEST_PURPOSE);
    assertThat(fieldJson).usingRecursiveComparison()
        .isEqualTo(field1Json);
  }

  @Test
  void getFieldWithOperator_fieldExists() {
    when(fieldApi.findFieldById(eq(field1WithOperator.getFieldId()), any(FieldProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(Optional.of(field1WithOperator));

    var fieldJson = fieldService.getFieldWithOperator(field1WithOperator.getFieldId(), REQUEST_PURPOSE);
    assertThat(fieldJson).usingRecursiveComparison()
        .isEqualTo(field1JsonWithOperator);
  }

  @Test
  void getField_fieldNotExists() {
    when(fieldApi.findFieldById(eq(0), any(FieldProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> fieldService.getField(0, REQUEST_PURPOSE))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining(FieldService.FIELD_NOT_FOUND.formatted(0));
  }

  @Test
  void getFieldWithOperator_fieldNotExists() {
    when(fieldApi.findFieldById(eq(0), any(FieldProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> fieldService.getFieldWithOperator(0, REQUEST_PURPOSE))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining(FieldService.FIELD_NOT_FOUND.formatted(0));
  }

  @Test
  void findFieldWithOperatorAndLicences_fieldExists() {
    when(fieldApi.findFieldById(eq(field1WithOperatorAndLicences.getFieldId()), any(FieldProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(Optional.of(field1WithOperatorAndLicences));

    var fieldJsonOptional = fieldService.findFieldWithOperatorAndLicences(field1WithOperatorAndLicences.getFieldId(), REQUEST_PURPOSE);
    assertThat(fieldJsonOptional).usingRecursiveComparison()
        .isEqualTo(Optional.of(field1JsonWithOperatorAndLicences));
  }

  @Test
  void findFieldWithOperatorAndLicences_fieldNotExists() {
    when(fieldApi.findFieldById(eq(0), any(FieldProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(Optional.empty());

    var fieldJsonOptional = fieldService.findFieldWithOperatorAndLicences(0, REQUEST_PURPOSE);
    assertThat(fieldJsonOptional).isNotPresent();
  }

  @Test
  void getFieldWithOperatorAndLicences_fieldExists() {
    when(fieldApi.findFieldById(eq(field1WithOperatorAndLicences.getFieldId()), any(FieldProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(Optional.of(field1WithOperatorAndLicences));

    var fieldJson = fieldService.getFieldWithOperatorAndLicences(field1WithOperatorAndLicences.getFieldId(), REQUEST_PURPOSE);
    assertThat(fieldJson).usingRecursiveComparison()
        .isEqualTo(field1JsonWithOperatorAndLicences);
  }

  @Test
  void getFieldWithOperatorAndLicences_fieldNotExists() {
    when(fieldApi.findFieldById(eq(0), any(FieldProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> fieldService.getFieldWithOperatorAndLicences(0, REQUEST_PURPOSE))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining(FieldService.FIELD_NOT_FOUND.formatted(0));
  }
}
