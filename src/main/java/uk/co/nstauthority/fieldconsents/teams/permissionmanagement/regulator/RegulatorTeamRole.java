package uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator;

import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.ASSIGN_FCS_APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.AUTHORISE_FCS_CONSENTS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.EDIT_FCS_CASE_PROCESSING_DOCUMENTS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.GRANT_ROLES;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.MANAGE_ASSETS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.MANAGE_DOCUMENT_TEMPLATES;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.MANAGE_FEE_PERIODS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.MANAGE_INDUSTRY_TEAMS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.PROCESS_FCS_APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.TECHNICAL_REVIEW_FCS_APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.VIEW_FCS_APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.VIEW_FCS_CASE_PROCESSING_DOCUMENTS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.VIEW_FCS_CONSENTS;

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
  DOCUMENT_TEMPLATE_MANAGER(
      "Document template manager",
      "Can manage document templates for case output letters and consents",
      30,
      EnumSet.of(
          MANAGE_DOCUMENT_TEMPLATES
      )
  ),
  CASE_OFFICER(
      "Case officer",
      "Can process applications and run technical reviews and consultations",
      40,
      EnumSet.of(
          PROCESS_FCS_APPLICATIONS,
          VIEW_FCS_APPLICATIONS,
          VIEW_FCS_CONSENTS,
          VIEW_FCS_CASE_PROCESSING_DOCUMENTS,
          EDIT_FCS_CASE_PROCESSING_DOCUMENTS,
          MANAGE_ASSETS
      )
  ),
  CASE_MANAGER(
      "Case manager",
      "Can view all applications and consents and assign case officers",
      50,
      EnumSet.of(
          ASSIGN_FCS_APPLICATIONS,
          VIEW_FCS_APPLICATIONS,
          VIEW_FCS_CONSENTS,
          VIEW_FCS_CASE_PROCESSING_DOCUMENTS,
          EDIT_FCS_CASE_PROCESSING_DOCUMENTS,
          MANAGE_ASSETS
      )
  ),
  CONSENTS_AND_AUTHORISATIONS_MANAGER(
      "Consents and authorisations manager",
      "Can authorise consents and manage fee periods",
      60,
      EnumSet.of(
          MANAGE_FEE_PERIODS,
          AUTHORISE_FCS_CONSENTS,
          VIEW_FCS_APPLICATIONS,
          VIEW_FCS_CONSENTS,
          VIEW_FCS_CASE_PROCESSING_DOCUMENTS,
          EDIT_FCS_CASE_PROCESSING_DOCUMENTS,
          MANAGE_ASSETS
      )
  ),
  TECHNICAL_REVIEWER(
      "Technical reviewer",
      "Can perform technical reviews and view all applications and consents",
      70,
      EnumSet.of(
          TECHNICAL_REVIEW_FCS_APPLICATIONS,
          VIEW_FCS_APPLICATIONS,
          VIEW_FCS_CONSENTS,
          VIEW_FCS_CASE_PROCESSING_DOCUMENTS,
          EDIT_FCS_CASE_PROCESSING_DOCUMENTS,
          MANAGE_ASSETS
      )
  ),
  VIEWER(
      "Viewer",
      "Can view all applications and consents",
      80,
      EnumSet.of(
          VIEW_FCS_APPLICATIONS,
          VIEW_FCS_CONSENTS,
          MANAGE_ASSETS
      )
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
