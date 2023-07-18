package uk.co.nstauthority.fieldconsents.application.caseprocessing;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.ASSIGN_FCS_APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.PROCESS_FCS_APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.TECHNICAL_REVIEW_FCS_APPLICATIONS;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.Function;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

public enum CaseProcessingTab {

  VIEW_APPLICATION(
      "View application",
      "viewApplication",
      "view-application",
          applicationId -> ReverseRouter.route(on(ApplicationCaseProcessingController.class)
              .getViewApplicationTab(applicationId, null)),
      10,
      EnumSet.of(PROCESS_FCS_APPLICATIONS, ASSIGN_FCS_APPLICATIONS, TECHNICAL_REVIEW_FCS_APPLICATIONS)
  ),
  CASE_HISTORY(
      "Case history",
      "caseHistory",
      "case-history",
          applicationId -> ReverseRouter.route(on(ApplicationCaseProcessingController.class)
              .getCaseHistoryTab(applicationId, null)),
      20,
      EnumSet.of(PROCESS_FCS_APPLICATIONS, ASSIGN_FCS_APPLICATIONS, TECHNICAL_REVIEW_FCS_APPLICATIONS)
  );

  private final String label;
  private final String value;
  private final String anchor;
  private final Function<Integer, String> url;
  private final int displayOrder;
  private final Set<RolePermission> rolePermissions;

  CaseProcessingTab(String label,
                    String value,
                    String anchor,
                    Function<Integer, String> url,
                    int displayOrder,
                    Set<RolePermission> rolePermissions) {
    this.label = label;
    this.value = value;
    this.anchor = anchor;
    this.url = url;
    this.displayOrder = displayOrder;
    this.rolePermissions = rolePermissions;
  }

  public String getLabel() {
    return label;
  }

  public String getValue() {
    return value;
  }

  public String getAnchor() {
    return anchor;
  }

  public Function<Integer, String> getUrl() {
    return url;
  }

  public int getDisplayOrder() {
    return displayOrder;
  }

  public Set<RolePermission> getRolePermissions() {
    return rolePermissions;
  }
}
