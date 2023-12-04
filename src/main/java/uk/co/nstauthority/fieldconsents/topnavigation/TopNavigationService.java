package uk.co.nstauthority.fieldconsents.topnavigation;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.assets.AssetSelectionController.ASSET_SELECTION_TITLE;
import static uk.co.nstauthority.fieldconsents.search.SearchController.SEARCH_TITLE;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.ASSIGN_FCS_APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.MANAGE_DOCUMENT_TEMPLATES;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.MANAGE_FEE_PERIODS;
import static uk.co.nstauthority.fieldconsents.workarea.WorkAreaController.WORK_AREA_TITLE;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.bulkcaseactions.BulkCaseActionSearchController;
import uk.co.nstauthority.fieldconsents.assets.AssetSelectionController;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.PermissionService;
import uk.co.nstauthority.fieldconsents.document.DocumentTemplateController;
import uk.co.nstauthority.fieldconsents.fds.navigation.TopNavigationItem;
import uk.co.nstauthority.fieldconsents.fee.FeePeriodController;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.search.SearchController;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.TeamListController;
import uk.co.nstauthority.fieldconsents.workarea.WorkAreaController;

@Service
public class TopNavigationService {

  static final String TEAM_MANAGEMENT_NAVIGATION_ITEM_TITLE = "Teams";
  static final String FEE_PERIODS_NAVIGATION_ITEM_TITLE = "Fee periods";
  static final String DOCUMENT_TEMPLATES_NAVIGATION_ITEM_TITLE = "Document templates";

  private final PermissionService permissionService;

  @Autowired
  public TopNavigationService(PermissionService permissionService) {
    this.permissionService = permissionService;
  }

  public List<TopNavigationItem> getTopNavigationItems(ServiceUserDetail user) {
    var navigationItems = new ArrayList<TopNavigationItem>();
    navigationItems.add(
        new TopNavigationItem(WORK_AREA_TITLE, ReverseRouter.route(on(WorkAreaController.class).getWorkArea(null, null)))
    );

    if (permissionService.hasPermission(user, Set.of(RolePermission.MANAGE_ASSETS))) {
      navigationItems.add(
          new TopNavigationItem(
              ASSET_SELECTION_TITLE,
              ReverseRouter.route(on(AssetSelectionController.class).getAssetSelection())
          )
      );
    }

    navigationItems.add(
        new TopNavigationItem(
            SEARCH_TITLE,
            ReverseRouter.route(on(SearchController.class).getSearch(null, null))
        )
    );

    navigationItems.add(
        new TopNavigationItem(
            TEAM_MANAGEMENT_NAVIGATION_ITEM_TITLE,
            ReverseRouter.route(on(TeamListController.class).resolveTeamListEntryRoute())
        )
    );

    if (permissionService.hasPermission(user, Set.of(MANAGE_FEE_PERIODS))) {
      navigationItems.add(
          new TopNavigationItem(
              FEE_PERIODS_NAVIGATION_ITEM_TITLE,
              ReverseRouter.route(on(FeePeriodController.class).getFeePeriods())
          )
      );
    }

    if (permissionService.hasPermission(user, Set.of(MANAGE_DOCUMENT_TEMPLATES))) {
      navigationItems.add(
          new TopNavigationItem(
              DOCUMENT_TEMPLATES_NAVIGATION_ITEM_TITLE,
              ReverseRouter.route(on(DocumentTemplateController.class).getDocumentTemplates())
          )
      );
    }

    if (permissionService.hasPermission(user, Set.of(ASSIGN_FCS_APPLICATIONS))) {
      navigationItems.add(
          new TopNavigationItem(
              BulkCaseActionSearchController.PAGE_TITLE,
              ReverseRouter.route(on(BulkCaseActionSearchController.class).getSearchResults(null, null))
          )
      );
    }

    return navigationItems;
  }
}
