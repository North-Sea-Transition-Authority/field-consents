package uk.co.nstauthority.fieldconsents.application.caseprocessing;

import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.ALLOCATE_CONSULTATION;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.ASSIGN_FCS_APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.AUTHORISE_FCS_CONSENTS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.PROCESS_FCS_APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.RESPOND_TO_CONSULTATION;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.TECHNICAL_REVIEW_FCS_APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.VIEW_FCS_APPLICATIONS;

import java.util.EnumSet;
import java.util.Set;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

public enum CaseProcessingTab {

  TASKS(
      "Tasks",
      "tasks",
      "tasks",
      EnumSet.of(PROCESS_FCS_APPLICATIONS, ASSIGN_FCS_APPLICATIONS, TECHNICAL_REVIEW_FCS_APPLICATIONS, AUTHORISE_FCS_CONSENTS)
  ),
  VIEW_APPLICATION(
      "View application",
      "viewApplication",
      "view-application",
      EnumSet.of(VIEW_FCS_APPLICATIONS)
  ),
  CASE_HISTORY(
      "Case history",
      "caseHistory",
      "case-history",
      EnumSet.of(PROCESS_FCS_APPLICATIONS, ASSIGN_FCS_APPLICATIONS, TECHNICAL_REVIEW_FCS_APPLICATIONS, AUTHORISE_FCS_CONSENTS)
  ),
  PAYMENTS(
      "Payments",
      "payments",
      "payments",
      EnumSet.of(VIEW_FCS_APPLICATIONS)
  ),
  FURTHER_INFORMATION(
      "Further information requests",
      "furtherInformationRequests",
      "further-information-requests",
      EnumSet.of(ALLOCATE_CONSULTATION, RESPOND_TO_CONSULTATION)
  );

  private final String label;
  private final String value;
  private final String anchor;
  private final Set<RolePermission> rolePermissions;

  CaseProcessingTab(
      String label,
      String value,
      String anchor,
      Set<RolePermission> rolePermissions
  ) {
    this.label = label;
    this.value = value;
    this.anchor = anchor;
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

  public Set<RolePermission> getRolePermissions() {
    return rolePermissions;
  }

}
