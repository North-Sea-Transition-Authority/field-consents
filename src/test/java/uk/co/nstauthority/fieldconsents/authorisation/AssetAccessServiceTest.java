package uk.co.nstauthority.fieldconsents.authorisation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1JsonWithNullOperatorAndLicences;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1JsonWithOperator;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.VIEW_FCS_APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.VIEW_FCS_CONSENTS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.VIEW_PERMISSIONS;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitPermissionService;
import uk.co.nstauthority.fieldconsents.teams.Team;
import uk.co.nstauthority.fieldconsents.teams.TeamService;
import uk.co.nstauthority.fieldconsents.teams.TeamTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@ExtendWith(MockitoExtension.class)
class AssetAccessServiceTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();
  private static final Team REGULATOR_TEAM = TeamTestUtil.Builder().withTeamType(TeamType.REGULATOR).build();
  private static final Team CONSULTEE_TEAM = TeamTestUtil.Builder().withTeamType(TeamType.OPRED).build();
  private static final RolePermission[] requiredPermissions = new RolePermission[]{VIEW_FCS_APPLICATIONS, VIEW_FCS_CONSENTS};

  @Mock
  private OrganisationUnitPermissionService organisationUnitPermissionService;
  @Mock
  private TeamService teamService;
  @InjectMocks
  private AssetAccessService assetAccessService;

  @Test
  void hasAssetPermission_whenUserDoesntHavePermissions_thenFalse() {
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(USER, TeamType.REGULATOR, VIEW_PERMISSIONS))
        .thenReturn(Collections.emptyList());
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(USER, TeamType.OPRED, VIEW_PERMISSIONS))
        .thenReturn(Collections.emptyList());

    when(organisationUnitPermissionService
        .hasOperatorPermission(USER, field1JsonWithOperator.getOperatorJson().organisationUnitId(), requiredPermissions))
        .thenReturn(false);

    assertThat(assetAccessService.hasAssetPermission(USER, field1JsonWithOperator, requiredPermissions))
        .isFalse();
  }

  @Test
  void hasAssetPermission_whenUserDoesHavePermissionsForIndustryTeam_thenTrue() {
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(USER, TeamType.REGULATOR, VIEW_PERMISSIONS))
        .thenReturn(Collections.emptyList());
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(USER, TeamType.OPRED, VIEW_PERMISSIONS))
        .thenReturn(Collections.emptyList());

    when(organisationUnitPermissionService
        .hasOperatorPermission(USER, field1JsonWithOperator.getOperatorJson().organisationUnitId(), requiredPermissions))
        .thenReturn(true);

    assertThat(assetAccessService.hasAssetPermission(USER, field1JsonWithOperator, requiredPermissions))
        .isTrue();
  }

  @Test
  void hasAssetPermission_whenFieldDoesntHaveAnOperator_thenFalse() {
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(USER, TeamType.REGULATOR, VIEW_PERMISSIONS))
        .thenReturn(Collections.emptyList());
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(USER, TeamType.OPRED, VIEW_PERMISSIONS))
        .thenReturn(Collections.emptyList());

    assertThat(assetAccessService.hasAssetPermission(USER, field1JsonWithNullOperatorAndLicences, requiredPermissions))
        .isFalse();
  }

  @Test
  void hasAssetPermission_whenUserDoesHavePermissionsForRegulatorTeam_thenTrue() {
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(USER, TeamType.REGULATOR, VIEW_PERMISSIONS))
        .thenReturn(List.of(REGULATOR_TEAM));

    assertThat(assetAccessService.hasAssetPermission(USER, field1JsonWithOperator, requiredPermissions))
        .isTrue();
  }

  @Test
  void hasAssetPermission_whenUserDoesHavePermissionsForConsulteeTeam_thenTrue() {
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(USER, TeamType.REGULATOR, VIEW_PERMISSIONS))
        .thenReturn(Collections.emptyList());
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(USER, TeamType.OPRED, VIEW_PERMISSIONS))
        .thenReturn(List.of(CONSULTEE_TEAM));

    assertThat(assetAccessService.hasAssetPermission(USER, field1JsonWithOperator, requiredPermissions))
        .isTrue();
  }
}
