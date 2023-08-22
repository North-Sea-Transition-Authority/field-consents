package uk.co.nstauthority.fieldconsents.teams.permissionmanagement.opred;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.TeamRole;

public enum OpredTeamRole implements TeamRole {

  ACCESS_MANAGER(
      "Access manager",
      "Can add, remove and update members of this team",
      10,
      EnumSet.of(RolePermission.GRANT_ROLES)
  ),
  VIEWER(
      "Viewer",
      "Can view issued Field Consents",
      20,
      EnumSet.of(RolePermission.VIEW_FCS_CONSENTS)
  ),
  ALLOCATOR(
      "Allocator",
      "Can view applications and allocate consultation requests to Responder",
      30,
      EnumSet.of(RolePermission.VIEW_FCS_APPLICATIONS, RolePermission.ALLOCATE_CONSULTATION)
  ),
  RESPONDER(
      "Responder",
      "Can view applications and respond to consent approvals",
      40,
      EnumSet.of(
          RolePermission.VIEW_FCS_APPLICATIONS,
          RolePermission.RESPOND_TO_CONSULTATION
      )
  );

  private final String displayName;
  private final String description;
  private final Integer displayOrder;
  private final Set<RolePermission> rolePermissions;

  OpredTeamRole(String displayName, String description, Integer displayOrder,
                Set<RolePermission> rolePermissions) {
    this.displayName = displayName;
    this.description = description;
    this.displayOrder = displayOrder;
    this.rolePermissions = rolePermissions;
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
  public String getDisplayName() {
    return this.displayName;
  }

  @Override
  public Set<RolePermission> getRolePermissions() {
    return rolePermissions;
  }

  static Optional<OpredTeamRole> getRoleFromString(String role) {
    try {
      return Optional.of(OpredTeamRole.valueOf(role.toUpperCase()));
    } catch (IllegalArgumentException exception) {
      return Optional.empty();
    }
  }

}
