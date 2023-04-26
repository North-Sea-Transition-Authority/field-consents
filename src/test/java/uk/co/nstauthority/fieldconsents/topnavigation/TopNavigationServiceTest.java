package uk.co.nstauthority.fieldconsents.topnavigation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.assets.AssetSelectionController;
import uk.co.nstauthority.fieldconsents.fds.navigation.TopNavigationItem;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.TeamListController;
import uk.co.nstauthority.fieldconsents.workarea.WorkAreaController;

@ExtendWith(MockitoExtension.class)
class TopNavigationServiceTest {

  @InjectMocks
  private TopNavigationService topNavigationService;

  @Test
  void getTopNavigationItems_verifyAllTopNavigationItems() {
    var topNavigationItems = topNavigationService.getTopNavigationItems();

    assertThat(topNavigationItems)
        .extracting(
            TopNavigationItem::getDisplayName,
            TopNavigationItem::getUrl
        )
        .containsExactly(
            tuple(
                WorkAreaController.WORK_AREA_TITLE,
                StringUtils.stripEnd(ReverseRouter.route(on(WorkAreaController.class).getWorkArea()), "/")
            ),
            tuple(
                AssetSelectionController.ASSET_SELECTION_TITLE,
                StringUtils.stripEnd(ReverseRouter.route(on(AssetSelectionController.class).getAssetSelection()), "/")
            ),
            tuple(
                TopNavigationService.TEAM_MANAGEMENT_NAVIGATION_ITEM_TITLE,
                StringUtils.stripEnd(
                    ReverseRouter.route(on(TeamListController.class).resolveTeamListEntryRoute()), "/")
            )
        );
  }
}