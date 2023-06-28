package uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator;

import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.ASSIGN_FCS_APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.GRANT_ROLES;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.MANAGE_INDUSTRY_TEAMS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.PROCESS_FCS_APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.TECHNICAL_REVIEW_FCS_APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.VIEW_FCS_APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.VIEW_FCS_CONSENTS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.VIEW_PERMISSIONS;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.TeamRole;

public enum RegulatorTeamRole implements TeamRole {

  ACCESS_MANAGER(
      "Access manager",
      "Can add, remove and update members of this team",
      10,
      EnumSet.of(GRANT_ROLES)
  ),
  INDUSTRY_ACCESS_MANAGER(
      "Industry access manager",
      "Manage industry access to the service",
      20,
      EnumSet.of(MANAGE_INDUSTRY_TEAMS)
  ),
  CASE_OFFICER(
      "Case officer",
      "Can process applications and run technical reviews and consultations",
      30,
      EnumSet.of(
          PROCESS_FCS_APPLICATIONS,
          VIEW_FCS_APPLICATIONS,
          VIEW_FCS_CONSENTS
      )
  ),
  CASE_MANAGER(
      "Case manager",
      "Can view all applications and consents and assign case officers",
      40,
      EnumSet.of(
          ASSIGN_FCS_APPLICATIONS,
          VIEW_FCS_APPLICATIONS,
          VIEW_FCS_CONSENTS
      )
  ),
  TECHNICAL_REVIEWER(
      "Technical reviewer",
      "Can perform technical reviews and view all applications and consents",
      50,
      EnumSet.of(
          TECHNICAL_REVIEW_FCS_APPLICATIONS,
          VIEW_FCS_APPLICATIONS,
          VIEW_FCS_CONSENTS
      )
  ),
  VIEWER(
      "Viewer",
      "Can view all applications and consents",
      60,
      VIEW_PERMISSIONS
  );

  private final String displayName;
  private final String description;
  private final Integer displayOrder;
  private final Set<RolePermission> rolePermissions;

  RegulatorTeamRole(String displayName, String description, Integer displayOrder,
                    Set<RolePermission> rolePermissions) {
    this.displayName = displayName;
    this.description = description;
    this.displayOrder = displayOrder;
    this.rolePermissions = rolePermissions;
  }

  public String getDisplayName() {
    return displayName;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public int getDisplayOrder() {
    return displayOrder;
  }

  @Override
  public Set<RolePermission> getRolePermissions() {
    return rolePermissions;
  }

  static Optional<RegulatorTeamRole> getRoleFromString(String role) {
    try {
      return Optional.of(RegulatorTeamRole.valueOf(role.toUpperCase()));
    } catch (IllegalArgumentException exception) {
      return Optional.empty();
    }
  }
}
