package uk.co.nstauthority.fieldconsents.topnavigation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.ASSIGN_FCS_APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.MANAGE_ASSETS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.MANAGE_FEE_PERIODS;

import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.bulkcaseactions.BulkCaseActionSearchController;
import uk.co.nstauthority.fieldconsents.assets.AssetSelectionController;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.authorisation.PermissionService;
import uk.co.nstauthority.fieldconsents.fds.navigation.TopNavigationItem;
import uk.co.nstauthority.fieldconsents.fee.FeePeriodController;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.search.SearchController;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.TeamListController;
import uk.co.nstauthority.fieldconsents.workarea.WorkAreaController;

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
  void getTopNavigationItems_userCannotManageAssetsOrFeePeriods() {
    when(permissionService.hasPermission(user, Set.of(MANAGE_ASSETS))).thenReturn(false);
    when(permissionService.hasPermission(user, Set.of(MANAGE_FEE_PERIODS))).thenReturn(false);

    var topNavigationItems = topNavigationService.getTopNavigationItems(user);

    assertThat(topNavigationItems)
        .extracting(
            TopNavigationItem::getDisplayName,
            TopNavigationItem::getUrl
        )
        .containsExactly(
            tuple(
                WorkAreaController.WORK_AREA_TITLE,
                ReverseRouter.route(on(WorkAreaController.class).getWorkArea(null, null))
            ),
            tuple(
                SearchController.SEARCH_TITLE,
                ReverseRouter.route(on(SearchController.class).getSearch(null, null))
            ),
            tuple(
                TopNavigationService.TEAM_MANAGEMENT_NAVIGATION_ITEM_TITLE,
                ReverseRouter.route(on(TeamListController.class).resolveTeamListEntryRoute())
            )
        );
  }

  @Test
  void getTopNavigationItems_userCanManageAssets() {
    when(permissionService.hasPermission(user, Set.of(MANAGE_ASSETS))).thenReturn(true);
    when(permissionService.hasPermission(user, Set.of(MANAGE_FEE_PERIODS))).thenReturn(false);

    var topNavigationItems = topNavigationService.getTopNavigationItems(user);

    assertThat(topNavigationItems)
        .extracting(
            TopNavigationItem::getDisplayName,
            TopNavigationItem::getUrl
        )
        .containsExactly(
            tuple(
                WorkAreaController.WORK_AREA_TITLE,
                ReverseRouter.route(on(WorkAreaController.class).getWorkArea(null, null))
            ),
            tuple(
                AssetSelectionController.ASSET_SELECTION_TITLE,
                ReverseRouter.route(on(AssetSelectionController.class).getAssetSelection())
            ),
            tuple(
                SearchController.SEARCH_TITLE,
                ReverseRouter.route(on(SearchController.class).getSearch(null, null))
            ),
            tuple(
                TopNavigationService.TEAM_MANAGEMENT_NAVIGATION_ITEM_TITLE,
                ReverseRouter.route(on(TeamListController.class).resolveTeamListEntryRoute())
            )
        );
  }

  @Test
  void getTopNavigationItems_userCanManageFeePeriods() {
    var user = ServiceUserDetailTestUtil.Builder().build();

    when(permissionService.hasPermission(user, Set.of(MANAGE_ASSETS))).thenReturn(false);
    when(permissionService.hasPermission(user, Set.of(ASSIGN_FCS_APPLICATIONS))).thenReturn(false);
    when(permissionService.hasPermission(user, Set.of(MANAGE_FEE_PERIODS))).thenReturn(true);

    var topNavigationItems = topNavigationService.getTopNavigationItems(user);

    assertThat(topNavigationItems)
        .extracting(
            TopNavigationItem::getDisplayName,
            TopNavigationItem::getUrl
        )
        .containsExactly(
            tuple(
                WorkAreaController.WORK_AREA_TITLE,
                ReverseRouter.route(on(WorkAreaController.class).getWorkArea(null, null))
            ),
            tuple(
                SearchController.SEARCH_TITLE,
                ReverseRouter.route(on(SearchController.class).getSearch(null, null))
            ),
            tuple(
                TopNavigationService.TEAM_MANAGEMENT_NAVIGATION_ITEM_TITLE,
                ReverseRouter.route(on(TeamListController.class).resolveTeamListEntryRoute())
            ),
            tuple(
                TopNavigationService.FEE_PERIODS_NAVIGATION_ITEM_TITLE,
                ReverseRouter.route(on(FeePeriodController.class).getFeePeriods())
            )
        );
  }

  @Test
  void getTopNavigationItems_userCanSeeBulkCaseActions() {
    var user = ServiceUserDetailTestUtil.Builder().build();

    when(permissionService.hasPermission(user, Set.of(MANAGE_ASSETS))).thenReturn(false);
    when(permissionService.hasPermission(user, Set.of(MANAGE_FEE_PERIODS))).thenReturn(false);
    when(permissionService.hasPermission(user, Set.of(ASSIGN_FCS_APPLICATIONS))).thenReturn(true);

    var topNavigationItems = topNavigationService.getTopNavigationItems(user);

    assertThat(topNavigationItems)
        .extracting(
            TopNavigationItem::getDisplayName,
            TopNavigationItem::getUrl
        )
        .containsExactly(
            tuple(
                WorkAreaController.WORK_AREA_TITLE,
                ReverseRouter.route(on(WorkAreaController.class).getWorkArea(null, null))
            ),
            tuple(
                SearchController.SEARCH_TITLE,
                ReverseRouter.route(on(SearchController.class).getSearch(null, null))
            ),
            tuple(
                TopNavigationService.TEAM_MANAGEMENT_NAVIGATION_ITEM_TITLE,
                ReverseRouter.route(on(TeamListController.class).resolveTeamListEntryRoute())
            ),
            tuple(
                BulkCaseActionSearchController.PAGE_TITLE,
                ReverseRouter.route(on(BulkCaseActionSearchController.class).getSearchResults(null, null))
            )
        );
  }
}
