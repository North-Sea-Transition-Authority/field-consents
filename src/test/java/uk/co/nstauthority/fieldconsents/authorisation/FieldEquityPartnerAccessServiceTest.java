package uk.co.nstauthority.fieldconsents.authorisation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
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
import uk.co.nstauthority.fieldconsents.assets.fields.FieldWithOperatorJson;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.teams.Role;
import uk.co.nstauthority.fieldconsents.teams.TeamQueryService;
import uk.co.nstauthority.fieldconsents.teams.TeamRoleTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamScopeReference;
import uk.co.nstauthority.fieldconsents.teams.TeamTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@ExtendWith(MockitoExtension.class)
class FieldEquityPartnerAccessServiceTest {

  @Mock
  private FieldEquityPartnerService fieldEquityPartnerService;

  @Mock
  private ApplicationAssetService applicationAssetService;

  @Mock
  private TeamQueryService teamQueryService;

  @Spy
  @InjectMocks
  private FieldEquityPartnerAccessService fieldEquityPartnerAccessService;

  @Test
  void userIsFieldEquityPartner_applicationVersion_isNotFieldEquityPartner() {
    var user = ServiceUserDetailTestUtil.Builder().build();
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);

    var organisationGroupIdsUserHasPermissionFor = Set.of(10);

    var field1 = Field.newBuilder()
        .fieldId(1)
        .build();
    var field2 = Field.newBuilder()
        .fieldId(2)
        .build();

    doReturn(organisationGroupIdsUserHasPermissionFor)
        .when(fieldEquityPartnerAccessService)
        .getOrganisationGroupIdsWhereUserIsFieldEquityPartner(user);

    when(fieldEquityPartnerService.getFieldsWithFieldEquityPartners(applicationVersion)).thenReturn(List.of(field1, field2));

    doReturn(false)
        .when(fieldEquityPartnerAccessService)
        .fieldHasAnyFieldEquityPartnerWithOrganisationGroupIdIn(field1, organisationGroupIdsUserHasPermissionFor);
    doReturn(false)
        .when(fieldEquityPartnerAccessService)
        .fieldHasAnyFieldEquityPartnerWithOrganisationGroupIdIn(field2, organisationGroupIdsUserHasPermissionFor);

    assertThat(fieldEquityPartnerAccessService.userIsFieldEquityPartner(user, applicationVersion))
        .isFalse();
  }

  @Test
  void userIsFieldEquityPartner_applicationVersion_isFieldEquityPartner() {
    var user = ServiceUserDetailTestUtil.Builder().build();
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);

    var organisationGroupIdsUserHasPermissionFor = Set.of(10);

    var field1 = Field.newBuilder()
        .fieldId(1)
        .build();
    var field2 = Field.newBuilder()
        .fieldId(2)
        .build();

    doReturn(organisationGroupIdsUserHasPermissionFor)
        .when(fieldEquityPartnerAccessService)
        .getOrganisationGroupIdsWhereUserIsFieldEquityPartner(user);

    when(fieldEquityPartnerService.getFieldsWithFieldEquityPartners(applicationVersion)).thenReturn(List.of(field1, field2));

    doReturn(false)
        .when(fieldEquityPartnerAccessService)
        .fieldHasAnyFieldEquityPartnerWithOrganisationGroupIdIn(field1, organisationGroupIdsUserHasPermissionFor);
    doReturn(true)
        .when(fieldEquityPartnerAccessService)
        .fieldHasAnyFieldEquityPartnerWithOrganisationGroupIdIn(field2, organisationGroupIdsUserHasPermissionFor);

    assertThat(fieldEquityPartnerAccessService.userIsFieldEquityPartner(user, applicationVersion))
        .isTrue();
  }

  @Test
  void userIsFieldEquityPartner_field_fieldDoesNotExist() {
    var user = ServiceUserDetailTestUtil.Builder().build();
    var fieldId = 1;

    var organisationGroupIdsUserHasPermissionFor = Set.of(10);

    doReturn(organisationGroupIdsUserHasPermissionFor)
        .when(fieldEquityPartnerAccessService)
        .getOrganisationGroupIdsWhereUserIsFieldEquityPartner(user);

    when(fieldEquityPartnerService.getFieldWithFieldEquityPartners(fieldId)).thenReturn(Optional.empty());

    var field = mock(FieldWithOperatorJson.class);
    when(field.getId()).thenReturn(fieldId);

    assertThat(fieldEquityPartnerAccessService.userIsFieldEquityPartner(user, field))
        .isFalse();
  }

  @ParameterizedTest
  @ValueSource(booleans = { false, true, })
  void userIsFieldEquityPartner_field_withFieldId_fieldExists(boolean hasPermission) {
    var user = ServiceUserDetailTestUtil.Builder().build();
    var fieldId = 1;

    var organisationGroupIdsUserHasPermissionFor = Set.of(10);

    var field = Field.newBuilder().build();

    doReturn(organisationGroupIdsUserHasPermissionFor)
        .when(fieldEquityPartnerAccessService)
        .getOrganisationGroupIdsWhereUserIsFieldEquityPartner(user);

    when(fieldEquityPartnerService.getFieldWithFieldEquityPartners(fieldId)).thenReturn(Optional.of(field));

    doReturn(hasPermission)
        .when(fieldEquityPartnerAccessService)
        .fieldHasAnyFieldEquityPartnerWithOrganisationGroupIdIn(field, organisationGroupIdsUserHasPermissionFor);

    var fieldWithOperatorJson = mock(FieldWithOperatorJson.class);
    when(fieldWithOperatorJson.getId()).thenReturn(fieldId);

    assertThat(fieldEquityPartnerAccessService.userIsFieldEquityPartner(user, fieldWithOperatorJson))
        .isEqualTo(hasPermission);
  }

  @Test
  void getFieldIdsWhereUserIsFieldEquityPartner_userIsFieldEquityPartner_notAllFields() {
    var user = ServiceUserDetailTestUtil.Builder().build();

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
        .when(fieldEquityPartnerAccessService)
        .getOrganisationGroupIdsWhereUserIsFieldEquityPartner(user);

    when(applicationAssetService.getAllPrimaryAndSecondaryFieldAssets())
        .thenReturn(List.of(fieldAsset1, fieldAsset2, fieldAsset1a, fieldAsset2a));

    when(fieldEquityPartnerService.getFieldsWithFieldEquityPartners(
        List.of(field1.getFieldId(), field2.getFieldId())))
        .thenReturn(List.of(field1, field2));

    doReturn(false)
        .when(fieldEquityPartnerAccessService)
        .fieldHasAnyFieldEquityPartnerWithOrganisationGroupIdIn(field1, organisationGroupIdsUserHasPermissionFor);
    doReturn(true)
        .when(fieldEquityPartnerAccessService)
        .fieldHasAnyFieldEquityPartnerWithOrganisationGroupIdIn(field2, organisationGroupIdsUserHasPermissionFor);

    assertThat(fieldEquityPartnerAccessService.getFieldIdsWhereUserIsFieldEquityPartner(user))
        .containsExactly(2);
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

    assertThat(fieldEquityPartnerAccessService.fieldHasAnyFieldEquityPartnerWithOrganisationGroupIdIn(field, organisationGroupIds))
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

    assertThat(fieldEquityPartnerAccessService.fieldHasAnyFieldEquityPartnerWithOrganisationGroupIdIn(field, organisationGroupIds))
        .isTrue();
  }

  @Test
  void userIsFieldEquityPartner_noFieldEquityPartners() {
    var user = ServiceUserDetailTestUtil.Builder().build();
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);

    doReturn(Set.of())
        .when(fieldEquityPartnerAccessService)
        .getOrganisationGroupIdsWhereUserIsFieldEquityPartner(user);

    assertThat(fieldEquityPartnerAccessService.userIsFieldEquityPartner(user, applicationVersion)).isFalse();
  }

  @Test
  void getOrganisationGroupIdsWhereUserIsFieldEquityPartner() {
    var user = ServiceUserDetailTestUtil.Builder().build();

    var industryTeam = TeamTestUtil.newBuilder()
        .withTeamType(TeamType.INDUSTRY)
        .withScopeType(TeamScopeReference.ORGANISATION_GROUP_ID)
        .withScopeId("1")
        .build();

    when(teamQueryService.getTeamRoles(user))
        .thenReturn(List.of(
            TeamRoleTestUtil.newBuilder()
                .withTeam(industryTeam)
                .withRole(Role.CONSENT_RECIPIENT)
                .build(),
            TeamRoleTestUtil.newBuilder()
                .withTeam(industryTeam)
                .withRole(Role.ACCESS_MANAGER) // another random role the user has
                .build()
        ));

    assertThat(fieldEquityPartnerAccessService.getOrganisationGroupIdsWhereUserIsFieldEquityPartner(user)).contains(1);
  }

  @Test
  void getOrganisationGroupIdsWhereUserIsFieldEquityPartner_invalidTeamType() {
    var user = ServiceUserDetailTestUtil.Builder().build();

    var consulteeTeam = TeamTestUtil.newBuilder()
        .withTeamType(TeamType.CONSULTEE)
        .build();

    when(teamQueryService.getTeamRoles(user))
        .thenReturn(List.of(
            TeamRoleTestUtil.newBuilder()
                .withTeam(consulteeTeam)
                .withRole(Role.RESPONDER)
                .build()
        ));

    assertThat(fieldEquityPartnerAccessService.getOrganisationGroupIdsWhereUserIsFieldEquityPartner(user)).isEmpty();
  }

  @Test
  void getOrganisationGroupIdsWhereUserIsFieldEquityPartner_invalidScopeType() {
    var user = ServiceUserDetailTestUtil.Builder().build();

    var industryTeam = TeamTestUtil.newBuilder()
        .withTeamType(TeamType.INDUSTRY)
        .withScopeType("invalid scope type")
        .withScopeId("1")
        .build();

    when(teamQueryService.getTeamRoles(user))
        .thenReturn(List.of(
            TeamRoleTestUtil.newBuilder()
                .withTeam(industryTeam)
                .withRole(Role.ACCESS_MANAGER)
                .build()
        ));

    assertThat(fieldEquityPartnerAccessService.getOrganisationGroupIdsWhereUserIsFieldEquityPartner(user)).isEmpty();
  }

  @Test
  void getOrganisationGroupIdsWhereUserIsFieldEquityPartner_invalidRole() {
    var user = ServiceUserDetailTestUtil.Builder().build();

    var industryTeam = TeamTestUtil.newBuilder()
        .withTeamType(TeamType.INDUSTRY)
        .withScopeType(TeamScopeReference.ORGANISATION_GROUP_ID)
        .withScopeId("1")
        .build();

    when(teamQueryService.getTeamRoles(user))
        .thenReturn(List.of(
            TeamRoleTestUtil.newBuilder()
                .withTeam(industryTeam)
                .withRole(Role.ACCESS_MANAGER)
                .build()
        ));

    assertThat(fieldEquityPartnerAccessService.getOrganisationGroupIdsWhereUserIsFieldEquityPartner(user)).isEmpty();
  }

}
