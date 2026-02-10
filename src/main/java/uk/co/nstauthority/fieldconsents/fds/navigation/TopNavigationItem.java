package uk.co.nstauthority.fieldconsents.fds.navigation;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import jakarta.annotation.Nullable;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import org.springframework.lang.NonNull;
import uk.co.nstauthority.fieldconsents.application.bulkcaseactions.BulkCaseActionController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.RoleGroup;
import uk.co.nstauthority.fieldconsents.assets.AssetSelectionController;
import uk.co.nstauthority.fieldconsents.document.template.DocumentTemplateController;
import uk.co.nstauthority.fieldconsents.fee.FeePeriodController;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.search.SearchController;
import uk.co.nstauthority.fieldconsents.teams.Role;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.management.TeamManagementController;
import uk.co.nstauthority.fieldconsents.util.enumutil.Displayable;
import uk.co.nstauthority.fieldconsents.workarea.WorkAreaController;

public enum TopNavigationItem implements Displayable {

  WORK_AREA("Work area",
      ReverseRouter.route(on(WorkAreaController.class).getWorkArea(null, null)),
      null
  ),
  MANAGE_ASSETS("Fields or facilities",
      ReverseRouter.route(on(AssetSelectionController.class).getAssetSelection()),
      Map.of(
          TeamType.REGULATOR, RoleGroup.REGULATOR_VIEW_CASE_PROCESSING_ROLES,
          TeamType.INDUSTRY, RoleGroup.INDUSTRY_VIEW_CASE_PROCESSING_ROLES
      )
  ),
  SEARCH("Search",
      ReverseRouter.route(on(SearchController.class).getSearch(null, null)),
      null
  ),
  TEAM_MANAGEMENT("Teams",
      ReverseRouter.route(on(TeamManagementController.class).renderTeamTypeList(null)),
      null
  ),
  FEE_PERIODS("Fees",
      ReverseRouter.route(on(FeePeriodController.class).getFeePeriods()),
      Map.of(
          TeamType.REGULATOR, EnumSet.of(Role.CONSENTS_AND_AUTHORISATIONS_MANAGER)
      )
  ),
  DOCUMENT_TEMPLATES("Templates",
      ReverseRouter.route(on(DocumentTemplateController.class).getDocumentTemplates()),
      Map.of(
          TeamType.REGULATOR, EnumSet.of(Role.DOCUMENT_TEMPLATE_MANAGER)
      )
  ),
  BULK_ACTIONS("Bulk actions",
      ReverseRouter.route(on(BulkCaseActionController.class).getBulkCaseActions(null)),
      Map.of(
          TeamType.REGULATOR, EnumSet.of(Role.CASE_MANAGER, Role.CONSENTS_AND_AUTHORISATIONS_MANAGER)
      )
  ),
  ;

  private final String displayName;
  private final String url;
  private final Map<TeamType, Set<Role>> rolesByTeamType;

  TopNavigationItem(String displayName, String url, Map<TeamType, Set<Role>> rolesByTeamType) {
    this.displayName = displayName;
    this.url = url;
    this.rolesByTeamType = rolesByTeamType;
  }

  @Override
  public String getDisplayName() {
    return displayName;
  }

  public String getUrl() {
    return url;
  }

  @Nullable
  public Map<TeamType, Set<Role>> getRolesByTeamType() {
    return rolesByTeamType;
  }

  @NonNull
  public Set<Role> getRoles(TeamType teamType) {
    if (rolesByTeamType == null) {
      return Set.of();
    }

    return rolesByTeamType.getOrDefault(teamType, Collections.emptySet());
  }

}
