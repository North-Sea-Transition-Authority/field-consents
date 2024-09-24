package uk.co.nstauthority.fieldconsents.fds.navigation;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.ASSIGN_FCS_APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.AUTHORISE_FCS_CONSENTS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.MANAGE_DOCUMENT_TEMPLATES;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.MANAGE_FEE_PERIODS;

import java.util.Collections;
import java.util.Set;
import uk.co.nstauthority.fieldconsents.application.bulkcaseactions.BulkCaseActionController;
import uk.co.nstauthority.fieldconsents.assets.AssetSelectionController;
import uk.co.nstauthority.fieldconsents.document.template.DocumentTemplateController;
import uk.co.nstauthority.fieldconsents.fee.FeePeriodController;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.search.SearchController;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.TeamListController;
import uk.co.nstauthority.fieldconsents.workarea.WorkAreaController;

public enum TopNavigationItem {

  WORK_AREA("Work area",
      ReverseRouter.route(on(WorkAreaController.class).getWorkArea(null, null)),
      Collections.emptySet()
  ),
  MANAGE_ASSETS("Manage fields/facilities",
      ReverseRouter.route(on(AssetSelectionController.class).getAssetSelection()),
      Set.of(RolePermission.MANAGE_ASSETS)
  ),
  SEARCH("Search",
      ReverseRouter.route(on(SearchController.class).getSearch(null, null)),
      Collections.emptySet()
  ),
  TEAM_MANAGEMENT("Team management",
      ReverseRouter.route(on(TeamListController.class).resolveTeamListEntryRoute()),
      Collections.emptySet()
  ),
  FEE_PERIODS("Fee periods",
      ReverseRouter.route(on(FeePeriodController.class).getFeePeriods()),
      Set.of(MANAGE_FEE_PERIODS)
  ),
  DOCUMENT_TEMPLATES("Document templates",
      ReverseRouter.route(on(DocumentTemplateController.class).getDocumentTemplates()),
      Set.of(MANAGE_DOCUMENT_TEMPLATES)
  ),
  BULK_ACTIONS("Bulk actions",
      ReverseRouter.route(on(BulkCaseActionController.class).getBulkCaseActions(null)),
      Set.of(ASSIGN_FCS_APPLICATIONS, AUTHORISE_FCS_CONSENTS)
  ),
  ENERGY_PORTAL("UK Energy Portal",
      ReverseRouter.route(on(EnergyPortalRedirectController.class).redirectToEnergyPortal()),
      Collections.emptySet());

  private final String displayName;
  private final String url;
  private final Set<RolePermission> requiredRoles;

  TopNavigationItem(String displayName, String url, Set<RolePermission> requiredRoles) {
    this.displayName = displayName;
    this.url = url;
    this.requiredRoles = requiredRoles;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getUrl() {
    return url;
  }

  public Set<RolePermission> getRequiredRoles() {
    return requiredRoles;
  }

}
