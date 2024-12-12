package uk.co.nstauthority.fieldconsents.application.bulkcaseactions;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Optional;
import org.springframework.core.annotation.AnnotationUtils;
import uk.co.nstauthority.fieldconsents.application.bulkcaseactions.assigncaseofficer.BulkAssignCaseOfficerController;
import uk.co.nstauthority.fieldconsents.application.bulkcaseactions.assigncaseofficer.BulkAssignCaseOfficerSearchController;
import uk.co.nstauthority.fieldconsents.application.bulkcaseactions.bulkissueconsents.BulkIssueConsentsController;
import uk.co.nstauthority.fieldconsents.application.bulkcaseactions.bulkissueconsents.BulkIssueConsentsSearchController;
import uk.co.nstauthority.fieldconsents.authorisation.role.HasRegulatorRole;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.Role;

public enum BulkCaseAction {

  ASSIGN_CASE_OFFICER(
      BulkAssignCaseOfficerController.ASSIGN_CASE_OFFICER,
      "Update the assigned case officer for many applications at the same time",
      BulkAssignCaseOfficerController.class,
      ReverseRouter.route(on(BulkAssignCaseOfficerSearchController.class).getSearchResults(null, null))
  ),
  BULK_ISSUE_CONSENTS(
      BulkIssueConsentsController.BULK_ISSUE_CONSENTS,
      "Issue consents for multiple applications at the same time",
      BulkIssueConsentsController.class,
      ReverseRouter.route(on(BulkIssueConsentsSearchController.class).getSearchResults(null, null))
  ),
  ;

  private final String displayName;
  private final String description;
  private final String searchUrl;
  private final Role requiredRole;

  BulkCaseAction(
      String displayName,
      String description,
      Class<?> actionController,
      String searchUrl
  ) {
    this.displayName = displayName;
    this.description = description;
    this.requiredRole = getRoleFromController(actionController);
    this.searchUrl = searchUrl;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getDescription() {
    return description;
  }

  public String getSearchUrl() {
    return searchUrl;
  }

  public Role getRequiredRole() {
    return requiredRole;
  }

  private static Role getRoleFromController(Class<?> targetClass) {
    return Optional.ofNullable(AnnotationUtils.findAnnotation(targetClass, HasRegulatorRole.class))
        .map(HasRegulatorRole::value)
        .orElseThrow(() -> new IllegalStateException("class %s is not annotated with @%s"
            .formatted(targetClass.getSimpleName(), HasRegulatorRole.class.getSimpleName())));
  }

}
