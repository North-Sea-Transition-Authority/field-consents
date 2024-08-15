package uk.co.nstauthority.fieldconsents.application.bulkcaseactions;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Optional;
import java.util.Set;
import org.springframework.core.annotation.AnnotationUtils;
import uk.co.nstauthority.fieldconsents.application.bulkcaseactions.assigncaseofficer.BulkAssignCaseOfficerController;
import uk.co.nstauthority.fieldconsents.application.bulkcaseactions.assigncaseofficer.BulkAssignCaseOfficerSearchController;
import uk.co.nstauthority.fieldconsents.application.bulkcaseactions.bulkissueconsents.BulkIssueConsentsController;
import uk.co.nstauthority.fieldconsents.application.bulkcaseactions.bulkissueconsents.BulkIssueConsentsSearchController;
import uk.co.nstauthority.fieldconsents.authorisation.HasPermission;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

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
  private final Set<RolePermission> requiredPermissions;

  BulkCaseAction(
      String displayName,
      String description,
      Class<?> actionController,
      String searchUrl
  ) {
    this.displayName = displayName;
    this.description = description;
    this.requiredPermissions = getRolePermissionsFromController(actionController);
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

  public Set<RolePermission> getRequiredPermissions() {
    return requiredPermissions;
  }

  private static Set<RolePermission> getRolePermissionsFromController(Class<?> targetClass) {
    return Optional.ofNullable(AnnotationUtils.findAnnotation(targetClass, HasPermission.class))
        .map(hasPermission -> Set.of(hasPermission.permissions()))
        .orElseThrow(() -> new IllegalStateException("class %s is not annotated with @%s"
            .formatted(targetClass.getSimpleName(), HasPermission.class.getSimpleName())));
  }

}
