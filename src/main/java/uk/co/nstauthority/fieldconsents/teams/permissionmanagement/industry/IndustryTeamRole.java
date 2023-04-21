package uk.co.nstauthority.fieldconsents.teams.permissionmanagement.industry;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.TeamRole;

public enum IndustryTeamRole implements TeamRole {

  ACCESS_MANAGER(
      "Access manager",
      "Can add, remove and update members of this team",
      10,
      EnumSet.of(RolePermission.GRANT_ROLES)
  ),
  VIEWER(
      "Viewer",
      "Can view applications and consents for the organisation group",
      20,
      EnumSet.of(RolePermission.VIEW_FCS_APPLICATION)
  ),
  CREATOR(
      "Creator",
      "Can start applications for the organisation group",
      30,
      EnumSet.of(RolePermission.CREATE_FCS_APPLICATION)
  );

  private final String displayName;
  private final String description;
  private final Integer displayOrder;
  private final Set<RolePermission> rolePermissions;

  IndustryTeamRole(String displayName, String description, Integer displayOrder,
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

  static Optional<IndustryTeamRole> getRoleFromString(String role) {
    try {
      return Optional.of(IndustryTeamRole.valueOf(role.toUpperCase()));
    } catch (IllegalArgumentException exception) {
      return Optional.empty();
    }
  }

}
