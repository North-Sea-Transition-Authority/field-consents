package uk.co.nstauthority.fieldconsents.authorisation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.energyportalapi.generated.types.Field;
import uk.co.fivium.energyportalapi.generated.types.FieldEquityPartner;
import uk.co.fivium.energyportalapi.generated.types.OrganisationGroup;
import uk.co.fivium.energyportalapi.generated.types.OrganisationUnit;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetTestUtil;
import uk.co.nstauthority.fieldconsents.application.fieldequitypartner.FieldEquityPartnerService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamService;
import uk.co.nstauthority.fieldconsents.teams.TeamTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@ExtendWith(MockitoExtension.class)
class FieldEquityPartnerPermissionServiceTest {

  @Mock
  private FieldEquityPartnerService fieldEquityPartnerService;

  @Mock
  private TeamService teamService;

  @Mock
  private ApplicationAssetService applicationAssetService;

  @InjectMocks
  @Spy
  private FieldEquityPartnerPermissionService fieldEquityPartnerPermissionService;

  @Test
  void userHasPermissionForFieldInFieldEquityPartnerTeam_withApplicationVersion_userDoesNotHavePermissionForFieldInFieldEquityPartnerTeam() {
    var user = ServiceUserDetailTestUtil.Builder().build();
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
    var requiredPermissions = Set.of(RolePermission.VIEW_FCS_CONSENTS);

    var organisationGroupIdsUserHasPermissionFor = Set.of(10);

    var field1 = Field.newBuilder()
        .fieldId(1)
        .build();
    var field2 = Field.newBuilder()
        .fieldId(2)
        .build();

    doReturn(organisationGroupIdsUserHasPermissionFor)
        .when(fieldEquityPartnerPermissionService)
        .getOrganisationGroupIdsUserHasPermissionFor(user, requiredPermissions);

    when(fieldEquityPartnerService.getFieldsWithFieldEquityPartners(applicationVersion)).thenReturn(List.of(field1, field2));

    doReturn(false)
        .when(fieldEquityPartnerPermissionService)
        .fieldHasAnyFieldEquityPartnerWithOrganisationGroupIdIn(field1, organisationGroupIdsUserHasPermissionFor);
    doReturn(false)
        .when(fieldEquityPartnerPermissionService)
        .fieldHasAnyFieldEquityPartnerWithOrganisationGroupIdIn(field2, organisationGroupIdsUserHasPermissionFor);

    assertThat(fieldEquityPartnerPermissionService.userHasPermissionForFieldInFieldEquityPartnerTeam(user, applicationVersion, requiredPermissions))
        .isFalse();
  }

  @Test
  void userHasPermissionForFieldInFieldEquityPartnerTeam_withApplicationVersion_userHasPermissionForFieldInFieldEquityPartnerTeam() {
    var user = ServiceUserDetailTestUtil.Builder().build();
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
    var requiredPermissions = Set.of(RolePermission.VIEW_FCS_CONSENTS);

    var organisationGroupIdsUserHasPermissionFor = Set.of(10);

    var field1 = Field.newBuilder()
        .fieldId(1)
        .build();
    var field2 = Field.newBuilder()
        .fieldId(2)
        .build();

    doReturn(organisationGroupIdsUserHasPermissionFor)
        .when(fieldEquityPartnerPermissionService)
        .getOrganisationGroupIdsUserHasPermissionFor(user, requiredPermissions);

    when(fieldEquityPartnerService.getFieldsWithFieldEquityPartners(applicationVersion)).thenReturn(List.of(field1, field2));

    doReturn(false)
        .when(fieldEquityPartnerPermissionService)
        .fieldHasAnyFieldEquityPartnerWithOrganisationGroupIdIn(field1, organisationGroupIdsUserHasPermissionFor);
    doReturn(true)
        .when(fieldEquityPartnerPermissionService)
        .fieldHasAnyFieldEquityPartnerWithOrganisationGroupIdIn(field2, organisationGroupIdsUserHasPermissionFor);

    assertThat(fieldEquityPartnerPermissionService.userHasPermissionForFieldInFieldEquityPartnerTeam(user, applicationVersion, requiredPermissions))
        .isTrue();
  }

  @Test
  void userHasPermissionForFieldInFieldEquityPartnerTeam_withFieldId_fieldDoesNotExist() {
    var user = ServiceUserDetailTestUtil.Builder().build();
    var fieldId = 1;
    var requiredPermissions = Set.of(RolePermission.VIEW_FCS_CONSENTS);

    var organisationGroupIdsUserHasPermissionFor = Set.of(10);

    doReturn(organisationGroupIdsUserHasPermissionFor)
        .when(fieldEquityPartnerPermissionService)
        .getOrganisationGroupIdsUserHasPermissionFor(user, requiredPermissions);

    when(fieldEquityPartnerService.getFieldWithFieldEquityPartners(fieldId)).thenReturn(Optional.empty());

    assertThat(fieldEquityPartnerPermissionService.userHasPermissionForFieldInFieldEquityPartnerTeam(user, fieldId, requiredPermissions))
        .isFalse();
  }

  @ParameterizedTest
  @ValueSource(booleans = { false, true, })
  void userHasPermissionForFieldInFieldEquityPartnerTeam_withFieldId_fieldExists(boolean hasPermission) {
    var user = ServiceUserDetailTestUtil.Builder().build();
    var fieldId = 1;
    var requiredPermissions = Set.of(RolePermission.VIEW_FCS_CONSENTS);

    var organisationGroupIdsUserHasPermissionFor = Set.of(10);

    var field = Field.newBuilder().build();

    doReturn(organisationGroupIdsUserHasPermissionFor)
        .when(fieldEquityPartnerPermissionService)
        .getOrganisationGroupIdsUserHasPermissionFor(user, requiredPermissions);

    when(fieldEquityPartnerService.getFieldWithFieldEquityPartners(fieldId)).thenReturn(Optional.of(field));

    doReturn(hasPermission)
        .when(fieldEquityPartnerPermissionService)
        .fieldHasAnyFieldEquityPartnerWithOrganisationGroupIdIn(field, organisationGroupIdsUserHasPermissionFor);

    assertThat(fieldEquityPartnerPermissionService.userHasPermissionForFieldInFieldEquityPartnerTeam(user, fieldId, requiredPermissions))
        .isEqualTo(hasPermission);
  }

  @Test
  void getFieldIdsUserHasPermissionForInFieldEquityPartnerTeam_withFieldIds() {
    var user = ServiceUserDetailTestUtil.Builder().build();
    var fieldIds = List.of(1, 2);
    var requiredPermissions = Set.of(RolePermission.VIEW_FCS_CONSENTS);

    var organisationGroupIdsUserHasPermissionFor = Set.of(10);

    var field1 = Field.newBuilder()
        .fieldId(1)
        .build();
    var field2 = Field.newBuilder()
        .fieldId(2)
        .build();

    doReturn(organisationGroupIdsUserHasPermissionFor)
        .when(fieldEquityPartnerPermissionService)
        .getOrganisationGroupIdsUserHasPermissionFor(user, requiredPermissions);

    when(fieldEquityPartnerService.getFieldsWithFieldEquityPartners(List.of(field1.getFieldId(), field2.getFieldId())))
        .thenReturn(List.of(field1, field2));

    doReturn(false)
        .when(fieldEquityPartnerPermissionService)
        .fieldHasAnyFieldEquityPartnerWithOrganisationGroupIdIn(field1, organisationGroupIdsUserHasPermissionFor);
    doReturn(true)
        .when(fieldEquityPartnerPermissionService)
        .fieldHasAnyFieldEquityPartnerWithOrganisationGroupIdIn(field2, organisationGroupIdsUserHasPermissionFor);

    assertThat(fieldEquityPartnerPermissionService.getFieldIdsUserHasPermissionForInFieldEquityPartnerTeam(user, fieldIds, requiredPermissions))
        .containsExactly(2);
  }

  @Test
  void getFieldIdsUserHasPermissionForInFieldEquityPartnerTeam_withoutFieldIds() {
    var user = ServiceUserDetailTestUtil.Builder().build();
    var requiredPermissions = Set.of(RolePermission.VIEW_FCS_CONSENTS);

    var organisationGroupIdsUserHasPermissionFor = Set.of(10);

    var fieldAsset1 = ApplicationAssetTestUtil.newBuilder()
        .withAssetId(1)
        .build();
    var fieldAsset2 = ApplicationAssetTestUtil.newBuilder()
        .withAssetId(2)
        .build();
    var fieldAsset1a = ApplicationAssetTestUtil.newBuilder()
        .withAssetId(1)
        .build();
    var fieldAsset2a = ApplicationAssetTestUtil.newBuilder()
        .withAssetId(2)
        .build();

    var field1 = Field.newBuilder()
        .fieldId(1)
        .build();
    var field2 = Field.newBuilder()
        .fieldId(2)
        .build();

    doReturn(organisationGroupIdsUserHasPermissionFor)
        .when(fieldEquityPartnerPermissionService)
        .getOrganisationGroupIdsUserHasPermissionFor(user, requiredPermissions);

    when(applicationAssetService.getAllPrimaryAndSecondaryFieldAssets())
        .thenReturn(List.of(fieldAsset1, fieldAsset2, fieldAsset1a, fieldAsset2a));

    when(fieldEquityPartnerService.getFieldsWithFieldEquityPartners(
        List.of(field1.getFieldId(), field2.getFieldId())))
        .thenReturn(List.of(field1, field2));

    doReturn(false)
        .when(fieldEquityPartnerPermissionService)
        .fieldHasAnyFieldEquityPartnerWithOrganisationGroupIdIn(field1, organisationGroupIdsUserHasPermissionFor);
    doReturn(true)
        .when(fieldEquityPartnerPermissionService)
        .fieldHasAnyFieldEquityPartnerWithOrganisationGroupIdIn(field2, organisationGroupIdsUserHasPermissionFor);

    assertThat(fieldEquityPartnerPermissionService.getFieldIdsUserHasPermissionForInFieldEquityPartnerTeam(user, requiredPermissions))
        .containsExactly(2);
  }

  @Test
  void getOrganisationGroupIdsUserHasPermissionFor() {
    var user = ServiceUserDetailTestUtil.Builder().build();
    var requiredPermissions = Set.of(RolePermission.VIEW_FCS_CONSENTS);

    var team1 = TeamTestUtil.Builder()
        .withOrganisationGroupId(1)
        .build();
    var team2 = TeamTestUtil.Builder()
        .withOrganisationGroupId(2)
        .build();

    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(user, TeamType.INDUSTRY, requiredPermissions))
        .thenReturn(List.of(team1, team2));

    assertThat(fieldEquityPartnerPermissionService.getOrganisationGroupIdsUserHasPermissionFor(user, requiredPermissions))
        .containsExactly(team1.getOrganisationGroupId(), team2.getOrganisationGroupId());
  }

  @Test
  void fieldHasAnyFieldEquityPartnerWithOrganisationGroupIdIn_fieldDoesNotHaveFieldEquityPartnerWithOrganisationGroupIdIn() {
    var organisationGroup = OrganisationGroup.newBuilder()
        .organisationGroupId(1)
        .build();
    var organisationUnit = OrganisationUnit.newBuilder()
        .organisationGroups(List.of(organisationGroup))
        .build();
    var fieldEquityPartner = FieldEquityPartner.newBuilder()
        .organisationUnit(organisationUnit)
        .build();

    var field = Field.newBuilder()
        .fieldEquityPartners(List.of(fieldEquityPartner))
        .build();
    var organisationGroupIds = Set.of(2, 3);

    assertThat(fieldEquityPartnerPermissionService.fieldHasAnyFieldEquityPartnerWithOrganisationGroupIdIn(field, organisationGroupIds))
        .isFalse();
  }

  @Test
  void fieldHasAnyFieldEquityPartnerWithOrganisationGroupIdIn_fieldHasFieldEquityPartnerWithOrganisationGroupIdIn() {
    var organisationGroup = OrganisationGroup.newBuilder()
        .organisationGroupId(1)
        .build();
    var organisationUnit = OrganisationUnit.newBuilder()
        .organisationGroups(List.of(organisationGroup))
        .build();
    var fieldEquityPartner = FieldEquityPartner.newBuilder()
        .organisationUnit(organisationUnit)
        .build();

    var field = Field.newBuilder()
        .fieldEquityPartners(List.of(fieldEquityPartner))
        .build();
    var organisationGroupIds = Set.of(2, organisationGroup.getOrganisationGroupId());

    assertThat(fieldEquityPartnerPermissionService.fieldHasAnyFieldEquityPartnerWithOrganisationGroupIdIn(field, organisationGroupIds))
        .isTrue();
  }
}
