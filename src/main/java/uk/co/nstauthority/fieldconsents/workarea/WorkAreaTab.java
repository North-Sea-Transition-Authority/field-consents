package uk.co.nstauthority.fieldconsents.workarea;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.ASSIGN_FCS_APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.PROCESS_FCS_APPLICATIONS;

import java.util.EnumSet;
import java.util.Set;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

public enum WorkAreaTab {
  MY_APPLICATIONS(
      "My applications",
      "myApplications",
      "my-applications",
      ReverseRouter.route(on(WorkAreaController.class).getWorkAreaCaseOfficerMyApplications(null, null)),
      10,
      EnumSet.of(PROCESS_FCS_APPLICATIONS)
  ),
  ALL_APPLICATIONS(
      "All applications",
      "allApplications",
      "all-applications",
      ReverseRouter.route(on(WorkAreaController.class).getWorkAreaRegulatorAllApplications(null, null)),
      20,
      EnumSet.of(ASSIGN_FCS_APPLICATIONS)
  ),
  UNASSIGNED_APPLICATIONS(
      "Unassigned",
      "unassigned",
      "unassigned",
      ReverseRouter.route(on(WorkAreaController.class).getWorkAreaCaseOfficerUnassignedApplications(null, null)),
      30,
      EnumSet.of(PROCESS_FCS_APPLICATIONS, ASSIGN_FCS_APPLICATIONS)
  );

  private final String label;
  private final String value;
  private final String anchor;
  private final String url;
  private final int displayOrder;
  private final Set<RolePermission> rolePermissions;


  WorkAreaTab(String label, String value, String anchor, String url, int displayOrder, Set<RolePermission> rolePermissions) {
    this.label = label;
    this.value = value;
    this.anchor = anchor;
    this.displayOrder = displayOrder;
    this.url = url;
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

  public String getUrl() {
    return url;
  }

  public int getDisplayOrder() {
    return displayOrder;
  }

  public Set<RolePermission> getRolePermissions() {
    return rolePermissions;
  }
}
