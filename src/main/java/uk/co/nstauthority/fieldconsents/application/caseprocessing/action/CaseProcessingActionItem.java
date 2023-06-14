package uk.co.nstauthority.fieldconsents.application.caseprocessing.action;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.function.Function;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.aceflag.AceFlagController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment.CaseAssignmentController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal.ApplicationWithdrawalController;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.util.enumutil.Displayable;

public enum CaseProcessingActionItem implements Displayable {

  CASE_OFFICER_TAKE_OWNERSHIP("Take ownership", 1, true,
      applicationId -> ReverseRouter.route(on(CaseAssignmentController.class)
          .takeOwnershipCaseOfficer(applicationId, null, null)), null),
  CHANGE_ACE_STATUS("Change ACE status", 1, false, null,
      applicationId -> ReverseRouter.route(on(AceFlagController.class)
          .getAceFlagForm(applicationId))),
  CASE_OFFICER_RELEASE_OWNERSHIP("Release ownership", 2, false,
      applicationId -> ReverseRouter.route(on(CaseAssignmentController.class)
          .releaseOwnershipCaseOfficer(applicationId, null)), null),
  CASE_OFFICER_ASSIGN_OWNERSHIP("Assign ownership", 1, true, null,
      applicationId -> ReverseRouter.route(on(CaseAssignmentController.class)
          .getCaseAssignment(applicationId, null))),
  CASE_OFFICER_REASSIGN_OWNERSHIP("Reassign ownership", 1, true, null,
      applicationId -> ReverseRouter.route(on(CaseAssignmentController.class)
          .getCaseAssignment(applicationId, null))),
  OPERATOR_WITHDRAWAL_REQUEST("Request withdrawal", 1, true, null,
      applicationId -> ReverseRouter.route(on(ApplicationWithdrawalController.class)
          .getApplicationWithdrawalRequest(applicationId)));

  private final String displayName;
  private final int displayOrder;
  private final boolean primaryAction;
  private final Function<Integer, String> postUrl;
  private final Function<Integer, String> redirectUrl;

  CaseProcessingActionItem(String displayName,
                           int displayOrder,
                           boolean primaryAction,
                           Function<Integer, String> postUrl,
                           Function<Integer, String> redirectUrl) {
    this.displayName = displayName;
    this.displayOrder = displayOrder;
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
