package uk.co.nstauthority.fieldconsents.topnavigation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.ASSIGN_FCS_APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.MANAGE_ASSETS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.MANAGE_DOCUMENT_TEMPLATES;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.MANAGE_FEE_PERIODS;

import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.authorisation.PermissionService;
import uk.co.nstauthority.fieldconsents.fds.navigation.TopNavigationItem;

@ExtendWith(MockitoExtension.class)
class TopNavigationServiceTest {

  @Mock
  private PermissionService permissionService;

  @InjectMocks
  private TopNavigationService topNavigationService;

  private ServiceUserDetail user;

  @BeforeEach
  void setUp() {
    user = ServiceUserDetailTestUtil.Builder().build();
  }

  @Test
  void getTopNavigationItems_userCannotManageAssetsOrFeePeriodsOrDocumentTemplatesOrSeeBulkCaseActions() {
    when(permissionService.hasPermission(user, Set.of(MANAGE_ASSETS))).thenReturn(false);
    when(permissionService.hasPermission(user, Set.of(MANAGE_FEE_PERIODS))).thenReturn(false);
    when(permissionService.hasPermission(user, Set.of(MANAGE_DOCUMENT_TEMPLATES))).thenReturn(false);
    when(permissionService.hasPermission(user, Set.of(ASSIGN_FCS_APPLICATIONS))).thenReturn(false);

    var topNavigationItems = topNavigationService.getTopNavigationItems(user);

    assertThat(topNavigationItems).containsExactly(
        TopNavigationItem.WORK_AREA,
        TopNavigationItem.SEARCH,
        TopNavigationItem.TEAM_MANAGEMENT
    );
  }

  @Test
  void getTopNavigationItems_userCanManageAssets() {
    when(permissionService.hasPermission(user, Set.of(MANAGE_ASSETS))).thenReturn(true);
    when(permissionService.hasPermission(user, Set.of(MANAGE_FEE_PERIODS))).thenReturn(false);
    when(permissionService.hasPermission(user, Set.of(MANAGE_DOCUMENT_TEMPLATES))).thenReturn(false);
    when(permissionService.hasPermission(user, Set.of(ASSIGN_FCS_APPLICATIONS))).thenReturn(false);

    var topNavigationItems = topNavigationService.getTopNavigationItems(user);

    assertThat(topNavigationItems)
        .containsExactly(
            TopNavigationItem.WORK_AREA,
            TopNavigationItem.MANAGE_ASSETS,
            TopNavigationItem.SEARCH,
            TopNavigationItem.TEAM_MANAGEMENT
        );
  }

  @Test
  void getTopNavigationItems_userCanManageFeePeriods() {
    var user = ServiceUserDetailTestUtil.Builder().build();

    when(permissionService.hasPermission(user, Set.of(MANAGE_ASSETS))).thenReturn(false);
    when(permissionService.hasPermission(user, Set.of(MANAGE_FEE_PERIODS))).thenReturn(true);
    when(permissionService.hasPermission(user, Set.of(MANAGE_DOCUMENT_TEMPLATES))).thenReturn(false);
    when(permissionService.hasPermission(user, Set.of(ASSIGN_FCS_APPLICATIONS))).thenReturn(false);

    var topNavigationItems = topNavigationService.getTopNavigationItems(user);

    assertThat(topNavigationItems)
        .containsExactly(
            TopNavigationItem.WORK_AREA,
            TopNavigationItem.SEARCH,
            TopNavigationItem.TEAM_MANAGEMENT,
            TopNavigationItem.FEE_PERIODS
        );
  }

  @Test
  void getTopNavigationItems_userCanManageDocumentTemplates() {
    var user = ServiceUserDetailTestUtil.Builder().build();

    when(permissionService.hasPermission(user, Set.of(MANAGE_ASSETS))).thenReturn(false);
    when(permissionService.hasPermission(user, Set.of(MANAGE_FEE_PERIODS))).thenReturn(false);
    when(permissionService.hasPermission(user, Set.of(MANAGE_DOCUMENT_TEMPLATES))).thenReturn(true);
    when(permissionService.hasPermission(user, Set.of(ASSIGN_FCS_APPLICATIONS))).thenReturn(false);

    var topNavigationItems = topNavigationService.getTopNavigationItems(user);

    assertThat(topNavigationItems)
        .containsExactly(
            TopNavigationItem.WORK_AREA,
            TopNavigationItem.SEARCH,
            TopNavigationItem.TEAM_MANAGEMENT,
            TopNavigationItem.DOCUMENT_TEMPLATES
        );
  }

  @Test
  void getTopNavigationItems_userCanSeeBulkCaseActions() {
    var user = ServiceUserDetailTestUtil.Builder().build();

    when(permissionService.hasPermission(user, Set.of(MANAGE_ASSETS))).thenReturn(false);
    when(permissionService.hasPermission(user, Set.of(MANAGE_FEE_PERIODS))).thenReturn(false);
    when(permissionService.hasPermission(user, Set.of(MANAGE_DOCUMENT_TEMPLATES))).thenReturn(false);
    when(permissionService.hasPermission(user, Set.of(ASSIGN_FCS_APPLICATIONS))).thenReturn(true);

    var topNavigationItems = topNavigationService.getTopNavigationItems(user);

    assertThat(topNavigationItems)
        .containsExactly(
            TopNavigationItem.WORK_AREA,
            TopNavigationItem.SEARCH,
            TopNavigationItem.TEAM_MANAGEMENT,
            TopNavigationItem.BULK_ACTIONS
        );
  }

  @Test
  void getTopNavigationItems_withoutUser() {
    var topNavigationItems = topNavigationService.getTopNavigationItems(null);
    assertThat(topNavigationItems)
        .containsExactly(
            TopNavigationItem.WORK_AREA,
            TopNavigationItem.SEARCH,
            TopNavigationItem.TEAM_MANAGEMENT
        );
  }
}
