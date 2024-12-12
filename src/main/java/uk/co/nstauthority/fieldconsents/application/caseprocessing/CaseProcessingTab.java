package uk.co.nstauthority.fieldconsents.application.caseprocessing;

import static uk.co.nstauthority.fieldconsents.teams.Role.CONSENT_RECIPIENT;
import static uk.co.nstauthority.fieldconsents.teams.Role.CREATOR;
import static uk.co.nstauthority.fieldconsents.teams.Role.EDITOR;
import static uk.co.nstauthority.fieldconsents.teams.Role.FINANCE_ADMINISTRATOR;
import static uk.co.nstauthority.fieldconsents.teams.Role.SUBMITTER;
import static uk.co.nstauthority.fieldconsents.teams.Role.VIEWER;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.RoleGroup;
import uk.co.nstauthority.fieldconsents.teams.Role;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

public enum CaseProcessingTab {

  TASKS(
      "Tasks",
      "tasks",
      "tasks",
      Map.of(
          TeamType.REGULATOR, RoleGroup.REGULATOR_CASE_PROCESSING_ROLES
      )
  ),
  VIEW_APPLICATION(
      "View application",
      "viewApplication",
      "view-application",
      Map.of(
          TeamType.REGULATOR, RoleGroup.REGULATOR_VIEW_CASE_PROCESSING_ROLES,
          TeamType.CONSULTEE, RoleGroup.CONSULTEE_VIEW_CASE_PROCESSING_ROLES,
          TeamType.INDUSTRY, EnumSet.of(CREATOR, EDITOR, SUBMITTER, FINANCE_ADMINISTRATOR, VIEWER)
      )
  ),
  CASE_HISTORY(
      "Case history",
      "caseHistory",
      "case-history",
      Map.of(
          TeamType.REGULATOR, RoleGroup.REGULATOR_CASE_PROCESSING_ROLES
      )
  ),
  PAYMENTS(
      "Payments",
      "payments",
      "payments",
      Map.of(
          TeamType.REGULATOR, RoleGroup.REGULATOR_CASE_PROCESSING_ROLES,
          TeamType.INDUSTRY, RoleGroup.union(
              RoleGroup.INDUSTRY_EDIT_APPLICATION_ROLES,
              RoleGroup.INDUSTRY_PAY_AND_SUBMIT_APPLICATION_ROLES
          )
      )
  ),
  CONSULTATIONS(
      "Consultations",
      "consultations",
      "consultations",
      Map.of(
          TeamType.CONSULTEE, RoleGroup.CONSULTEE_VIEW_CASE_PROCESSING_ROLES
      )
  ),
  CONSENT(
      "Consent",
      "consent",
      "consent",
      Map.of(
          TeamType.REGULATOR, RoleGroup.REGULATOR_VIEW_CASE_PROCESSING_ROLES,
          TeamType.INDUSTRY, EnumSet.of(CREATOR, EDITOR, SUBMITTER, VIEWER, CONSENT_RECIPIENT)
      )
  );

  static final EnumSet<CaseProcessingTab> REGULATOR_TABS = EnumSet.of(TASKS, VIEW_APPLICATION, CASE_HISTORY, PAYMENTS, CONSENT);
  static final EnumSet<CaseProcessingTab> CONSULTEE_TABS = EnumSet.of(VIEW_APPLICATION, CONSULTATIONS);
  static final EnumSet<CaseProcessingTab> INDUSTRY_TABS = EnumSet.of(VIEW_APPLICATION, PAYMENTS, CONSENT);

  private final String label;
  private final String value;
  private final String anchor;
  private final Map<TeamType, Set<Role>> rolesByTeamType;

  CaseProcessingTab(
      String label,
      String value,
      String anchor,
      Map<TeamType, Set<Role>> rolesByTeamType
  ) {
    this.label = label;
    this.value = value;
    this.anchor = anchor;
    this.rolesByTeamType = rolesByTeamType;
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

  public Set<Role> getRoles(TeamType teamType) {
    return rolesByTeamType.getOrDefault(teamType, Collections.emptySet());
  }

}
