package uk.co.nstauthority.fieldconsents.application.caseprocessing.action;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.function.Function;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.aceflag.AceFlagController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment.CaseAssignmentController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.casenotes.CaseNotesController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewAssignmentController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationStartUpdateController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal.ApplicationWithdrawalController;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.util.enumutil.Displayable;

public enum CaseProcessingActionItem implements Displayable {

  CASE_OFFICER_TAKE_OWNERSHIP("Take ownership", 1, false, true,
      applicationId -> ReverseRouter.route(on(CaseAssignmentController.class)
          .takeOwnershipCaseOfficer(applicationId, null, null)), null),
  CHANGE_ACE_STATUS("Change ACE status", 1, true, false, null,
      applicationId -> ReverseRouter.route(on(AceFlagController.class)
          .getAceFlagForm(applicationId))),
  CASE_OFFICER_RELEASE_OWNERSHIP("Release ownership", 2, true, false,
      applicationId -> ReverseRouter.route(on(CaseAssignmentController.class)
          .releaseOwnershipCaseOfficer(applicationId, null, null)), null),
  CASE_OFFICER_WITHDRAWAL_RESPONSE("Respond to withdrawal", 3, true, false, null,
      applicationId -> ReverseRouter.route(on(ApplicationWithdrawalController.class)
          .getApplicationWithdrawalResponse(applicationId))),
  TECHNICAL_REVIEW_REQUEST("Request technical review", 4, true, false, null,
      applicationId -> ReverseRouter.route(on(TechnicalReviewController.class)
          .getTechnicalReviewRequest(applicationId, null))),
  // Case manager actions
  CASE_OFFICER_ASSIGN_OWNERSHIP("Assign ownership", 1, false, true, null,
      applicationId -> ReverseRouter.route(on(CaseAssignmentController.class)
          .getCaseAssignment(applicationId, null))),
  CASE_OFFICER_REASSIGN_OWNERSHIP("Reassign ownership", 1, false, true, null,
      applicationId -> ReverseRouter.route(on(CaseAssignmentController.class)
          .getCaseAssignment(applicationId, null))),
  // Regulator user actions
  REGULATOR_ADD_CASE_NOTE("Add case note", 99, false, false, null,
      applicationId -> ReverseRouter.route(on(CaseNotesController.class)
          .getNewCaseNote(applicationId))),
  // Technical reviewer actions
  TECHNICAL_REVIEWER_REASSIGN_OWNERSHIP("Reassign technical reviewer", 1, false, false, null,
      applicationId -> ReverseRouter.route(on(TechnicalReviewAssignmentController.class)
          .getTechnicalReviewAssignment(applicationId, null))),
  // Case officer and Technical reviewer actions
  APPLICATION_UPDATE_REQUEST("Request application update", 5, true, false, null,
      applicationId -> ReverseRouter.route(on(ApplicationUpdateController.class)
          .getApplicationUpdateRequest(applicationId))),
  // Operator actions
  OPERATOR_WITHDRAWAL_REQUEST("Request withdrawal", 1, false, true, null,
      applicationId -> ReverseRouter.route(on(ApplicationWithdrawalController.class)
          .getApplicationWithdrawalRequest(applicationId))),
  OPERATOR_UPDATE_APPLICATION("Update application", 1, false, true, null,
      applicationId -> ReverseRouter.route(on(ApplicationStartUpdateController.class)
          .updateApplicationEntryPoint(applicationId)));

  private final String displayName;
  private final int displayOrder;
  private final boolean assigneeOnly;
  private final boolean primaryAction;
  private final Function<Integer, String> postUrl;
  private final Function<Integer, String> redirectUrl;

  CaseProcessingActionItem(String displayName,
                           int displayOrder,
                           boolean assigneeOnly,
                           boolean primaryAction,
                           Function<Integer, String> postUrl,
                           Function<Integer, String> redirectUrl) {
    this.displayName = displayName;
    this.displayOrder = displayOrder;
    this.assigneeOnly = assigneeOnly;
    this.primaryAction = primaryAction;
    this.postUrl = postUrl;
    this.redirectUrl = redirectUrl;
  }

  @Override
  public String getDisplayName() {
    return displayName;
  }

  @Override
  public int getDisplayOrder() {
    return displayOrder;
  }

  public boolean isAssigneeOnly() {
    return assigneeOnly;
  }

  public boolean isPrimaryAction() {
    return primaryAction;
  }

  public String getActionPostUrl(int applicationId) {
    return postUrl == null ? null : postUrl.apply(applicationId);
  }

  public String getActionRedirectUrl(int applicationId) {
    return redirectUrl == null ? null : redirectUrl.apply(applicationId);
  }
}
