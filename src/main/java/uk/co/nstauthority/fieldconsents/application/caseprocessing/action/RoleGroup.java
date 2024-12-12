package uk.co.nstauthority.fieldconsents.application.caseprocessing.action;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import uk.co.nstauthority.fieldconsents.teams.Role;

public class RoleGroup {

  /* The roles here should match with the summary controller and their respective case processing controllers. I.e.
   *
   * ApplicationSummaryController
   * ApplicationCaseProcessingController
   * ConsulteeCaseProcessingController
   * IndustryCaseProcessingController
   */
  public static final Set<Role> REGULATOR_VIEW_CASE_PROCESSING_ROLES = EnumSet.of(
      Role.CASE_OFFICER,
      Role.CASE_MANAGER,
      Role.CONSENTS_AND_AUTHORISATIONS_MANAGER,
      Role.TECHNICAL_REVIEWER,
      Role.VIEWER
  );

  public static final Set<Role> CONSULTEE_VIEW_CASE_PROCESSING_ROLES = EnumSet.of(
      Role.ALLOCATOR,
      Role.RESPONDER
  );

  public static final Set<Role> CONSULTEE_WITH_VIEWER_ROLES = EnumSet.of(
      Role.ALLOCATOR, Role.RESPONDER, Role.VIEWER
  );

  public static final Set<Role> INDUSTRY_VIEW_CASE_PROCESSING_ROLES = EnumSet.of(
      Role.CREATOR,
      Role.EDITOR,
      Role.SUBMITTER,
      Role.FINANCE_ADMINISTRATOR,
      Role.VIEWER,
      Role.CONSENT_RECIPIENT
  );

  public static final Set<Role> REGULATOR_CASE_PROCESSING_ROLES = EnumSet.of(
      Role.CASE_OFFICER,
      Role.CASE_MANAGER,
      Role.CONSENTS_AND_AUTHORISATIONS_MANAGER,
      Role.TECHNICAL_REVIEWER
  );

  public static final Set<Role> INDUSTRY_EDIT_APPLICATION_ROLES = EnumSet.of(
      Role.CREATOR,
      Role.EDITOR,
      Role.SUBMITTER
  );

  public static final Set<Role> CASE_OFFICER_AND_TECHNICAL_REVIEWER = EnumSet.of(
      Role.CASE_OFFICER,
      Role.TECHNICAL_REVIEWER
  );

  public static final Set<Role> INDUSTRY_PAY_AND_SUBMIT_APPLICATION_ROLES = EnumSet.of(
      Role.SUBMITTER,
      Role.FINANCE_ADMINISTRATOR
  );

  @SafeVarargs
  public static Set<Role> union(Set<Role>... roleSets) {
    return Stream.of(roleSets).flatMap(Set::stream).collect(Collectors.toSet());
  }

  private RoleGroup() {
    throw new UnsupportedOperationException("Cannot instantiate utility class");
  }

}
