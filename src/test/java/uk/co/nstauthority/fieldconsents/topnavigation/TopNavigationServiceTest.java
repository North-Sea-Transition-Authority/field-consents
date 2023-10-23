package uk.co.nstauthority.fieldconsents.topnavigation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.MANAGE_ASSETS;

import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.assets.AssetSelectionController;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.authorisation.PermissionService;
import uk.co.nstauthority.fieldconsents.fds.navigation.TopNavigationItem;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.search.SearchController;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.TeamListController;
import uk.co.nstauthority.fieldconsents.workarea.WorkAreaController;

@ExtendWith(MockitoExtension.class)
class TopNavigationServiceTest {

  @InjectMocks
  private TopNavigationService topNavigationService;

  @Mock
  private PermissionService permissionService;

  private ServiceUserDetail user;

  @BeforeEach
  void setUp() {
    user = ServiceUserDetailTestUtil.Builder().build();
  }

  @Test
  void getTopNavigationItems_userCannotManageAssets() {
    when(permissionService.hasPermission(user, Set.of(MANAGE_ASSETS))).thenReturn(false);

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
                ReverseRouter.route(on(SearchController.class).getSearch( null, null))
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
                ReverseRouter.route(on(SearchController.class).getSearch( null, null))
            ),
            tuple(
                TopNavigationService.TEAM_MANAGEMENT_NAVIGATION_ITEM_TITLE,
                ReverseRouter.route(on(TeamListController.class).resolveTeamListEntryRoute())
            )
        );
  }
}
