package uk.co.nstauthority.fieldconsents.topnavigation;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.assets.AssetSelectionController.ASSET_SELECTION_TITLE;
import static uk.co.nstauthority.fieldconsents.workarea.WorkAreaController.WORK_AREA_TITLE;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.assets.AssetSelectionController;
import uk.co.nstauthority.fieldconsents.fds.navigation.TopNavigationItem;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.TeamListController;
import uk.co.nstauthority.fieldconsents.workarea.WorkAreaController;

@Service
public class TopNavigationService {

  static final String TEAM_MANAGEMENT_NAVIGATION_ITEM_TITLE = "Teams";

  public List<TopNavigationItem> getTopNavigationItems() {
    var navigationItems = new ArrayList<TopNavigationItem>();
    navigationItems.add(
        new TopNavigationItem(WORK_AREA_TITLE, ReverseRouter.route(on(WorkAreaController.class).getWorkArea(null, null)))
    );

    navigationItems.add(
        new TopNavigationItem(
            ASSET_SELECTION_TITLE,
            ReverseRouter.route(on(AssetSelectionController.class).getAssetSelection())
        )
    );

    navigationItems.add(
        new TopNavigationItem(
            TEAM_MANAGEMENT_NAVIGATION_ITEM_TITLE,
            ReverseRouter.route(on(TeamListController.class).resolveTeamListEntryRoute())
        )
    );

    return navigationItems;
  }
}
