package uk.co.nstauthority.fieldconsents.teams.permissionmanagement;

import java.util.Set;

public enum RolePermission {
  GRANT_ROLES,
  MANAGE_INDUSTRY_TEAMS,
  CREATE_FCS_APPLICATIONS,
  VIEW_FCS_APPLICATIONS,
  EDIT_FCS_APPLICATIONS,
  SUBMIT_FCS_APPLICATIONS,
  VIEW_FCS_CONSENTS,
  PROCESS_FCS_APPLICATIONS;

  public static final Set<RolePermission> VIEW_PERMISSIONS =
      Set.of(RolePermission.VIEW_FCS_APPLICATIONS, RolePermission.VIEW_FCS_CONSENTS);
}
