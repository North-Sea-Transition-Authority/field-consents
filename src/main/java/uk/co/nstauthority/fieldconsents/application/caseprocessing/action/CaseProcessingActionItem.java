package uk.co.nstauthority.fieldconsents.application.caseprocessing.action;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.function.Function;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.aceflag.AceFlagController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment.CaseAssignmentController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment.cam.CamAssignmentController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.casenotes.CaseNotesController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.closure.ApplicationClosureController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.breaches.ConsentBreachController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentDataController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.issuing.ConsentIssuingController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.preparation.ConsentPreparationController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.preparation.documents.ConsentPreparationDocumentsController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.allocation.ConsultationAllocationController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformation.request.FurtherInformationRequestController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformation.response.FurtherInformationResponseController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.request.ConsultationRequestController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.response.ConsultationResponseController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.revision.ApplicationRevisionController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewAssignmentController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.response.TechnicalReviewResponseController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationStartUpdateController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.update.request.ApplicationUpdateRequestController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal.ApplicationWithdrawalController;
import uk.co.nstauthority.fieldconsents.application.payment.ApplicationPaymentController;
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
  CASE_OFFICER_WITHDRAWAL_RESPONSE("Respond to withdrawal", 1, true, true, null,
      applicationId -> ReverseRouter.route(on(ApplicationWithdrawalController.class)
          .getApplicationWithdrawalResponse(applicationId))),
  TECHNICAL_REVIEW_REQUEST("Request technical review", 5, true, false, null,
      applicationId -> ReverseRouter.route(on(TechnicalReviewController.class)
          .getTechnicalReviewRequest(applicationId, null))),
  EDIT_CONSENT_DATA("Edit", 1, true, false, null,
      applicationId -> ReverseRouter.route(on(ConsentDataController.class).editConsentData(applicationId))),
  EDIT_CONSENT_DOCUMENTS("Edit", 1, true, false, null,
      applicationId -> ReverseRouter.route(on(ConsentPreparationDocumentsController.class).editDocuments(applicationId))),
  CAM_ASSIGN_OWNERSHIP("Assign to CAM", 6, true, false, null,
      applicationId -> ReverseRouter.route(on(CamAssignmentController.class)
          .getCamAssignment(applicationId, null))),
  CAM_REASSIGN_OWNERSHIP("Reassign CAM", 7, false, false, null,
      applicationId -> ReverseRouter.route(on(CamAssignmentController.class)
          .getCamReassignment(applicationId, null))),
  // Case manager actions
  CASE_OFFICER_ASSIGN_OWNERSHIP("Assign ownership", 1, false, true, null,
      applicationId -> ReverseRouter.route(on(CaseAssignmentController.class)
          .getCaseAssignment(applicationId, null))),
  CASE_OFFICER_REASSIGN_OWNERSHIP("Reassign ownership", 1, false, false, null,
      applicationId -> ReverseRouter.route(on(CaseAssignmentController.class)
          .getCaseAssignment(applicationId, null))),
  // Regulator user actions
  TECHNICAL_REVIEWS("Technical reviews", 4, false, false, null,
      applicationId -> ReverseRouter.route(on(TechnicalReviewController.class)
          .getTechnicalReviews(applicationId, null))),
  CONSULTATIONS("Consultations", 5, false, false, null,
      applicationId -> ReverseRouter.route(on(ConsultationController.class)
          .getConsultations(applicationId, null))),
  APPLICATION_UPDATES("Application updates", 6, false, false, null,
      applicationId -> ReverseRouter.route(on(ApplicationUpdateController.class)
          .getApplicationUpdates(applicationId, null))),
  CONSENT_PREPARATION("Consent preparation", 7, false, false, null,
      applicationId -> ReverseRouter.route(on(ConsentPreparationController.class)
          .viewConsentPreparationPage(applicationId, null))),
  REGULATOR_ADD_CASE_NOTE("Add case note", 99, false, false, null,
      applicationId -> ReverseRouter.route(on(CaseNotesController.class)
          .getNewCaseNote(applicationId))),
  BREACH_INFORMATION("Breach information", 100, false, false, null,
      applicationId -> ReverseRouter.route(on(ConsentBreachController.class)
          .breachInformation(applicationId, null))),
  RECORD_BREACH("Record breach", 1, false, true, null,
      applicationId -> ReverseRouter.route(on(ConsentBreachController.class)
          .getConsentBreachForm(applicationId))),
  REMOVE_BREACH("Remove breach", 1, false, false, null,
      applicationId -> ReverseRouter.route(on(ConsentBreachController.class)
          .getDeleteBreachConfirmation(applicationId))),
  EDIT_BREACH_INFORMATION("Edit", 1, false, false, null,
      applicationId -> ReverseRouter.route(on(ConsentBreachController.class)
          .getConsentBreachForm(applicationId))),
  CLOSE_APPLICATION("Close application", 100, true, false, null,
      applicationId -> ReverseRouter.route(on(ApplicationClosureController.class).getConfirmation(applicationId, null))),
  // Technical reviewer actions
  TECHNICAL_REVIEWER_SUBMIT_REVIEW("Submit technical review", 1, true, true, null,
      applicationId -> ReverseRouter.route(on(TechnicalReviewResponseController.class).getForm(applicationId, null))),
  TECHNICAL_REVIEWER_REASSIGN_OWNERSHIP("Reassign technical reviewer", 2, false, false, null,
      applicationId -> ReverseRouter.route(on(TechnicalReviewAssignmentController.class)
          .getTechnicalReviewAssignment(applicationId))),
  // Case officer and Technical reviewer actions
  APPLICATION_UPDATE_REQUEST("Request application update", 6, true, false, null,
      applicationId -> ReverseRouter.route(on(ApplicationUpdateRequestController.class)
          .getApplicationUpdateRequest(applicationId, null))),
  // Operator actions
  OPERATOR_PAY_AND_SUBMIT_APPLICATION("Pay and submit application", 1, false, true, null,
      applicationId -> ReverseRouter.route(on(ApplicationPaymentController.class)
          .getStartPayment(applicationId, null))),
  OPERATOR_RETURN_APPLICATION_TO_IN_PROGRESS_FROM_AWAITING_PAYMENT("Edit application", 2, false, false,
      applicationId -> ReverseRouter.route(on(ApplicationPaymentController.class)
          .returnToInProgress(applicationId, null)), null),
  OPERATOR_WITHDRAWAL_REQUEST("Request withdrawal", 1, false, true, null,
      applicationId -> ReverseRouter.route(on(ApplicationWithdrawalController.class)
          .getApplicationWithdrawalRequest(applicationId))),
  OPERATOR_UPDATE_APPLICATION("Update application", 1, false, true, null,
      applicationId -> ReverseRouter.route(on(ApplicationStartUpdateController.class)
          .updateApplicationEntryPoint(applicationId))),
  REVISE_CONSENT("Revise consent", 1, false, false, null,
      applicationId -> ReverseRouter.route(on(ApplicationRevisionController.class).getStartRevision(applicationId, null))),
  // Consultation actions
  CONSULTATION_REQUEST("Request consultation", 99, true, false, null,
      applicationId -> ReverseRouter.route(on(ConsultationRequestController.class).getConsultationRequestForm(applicationId))),
  CONSULTATION_RESPONSE("Respond to consultation", 1, true, true, null,
      applicationId -> ReverseRouter.route(on(ConsultationResponseController.class).getResponseForm(applicationId, null))),
  CONSULTATION_MANAGE_RESPONDER("Manage consultation responder", 1, false, true, null,
      applicationId -> ReverseRouter.route(on(ConsultationAllocationController.class)
          .getResponderAllocationForm(applicationId))),
  CONSULTATION_FURTHER_INFORMATION_REQUEST("Request further information", 2, true, false, null,
      applicationId -> ReverseRouter.route(on(FurtherInformationRequestController.class)
          .getRequestForm(applicationId, null))),
  CONSULTATION_FURTHER_INFORMATION_RESPOND("Respond to further information request", 3, true, false, null,
      applicationId -> ReverseRouter.route(on(FurtherInformationResponseController.class)
          .getResponseForm(applicationId, null))),
  // CAM actions
  CONSENT_ISSUING("Consent issuing", 8, false, false, null,
      applicationId -> ReverseRouter.route(on(ConsentIssuingController.class).getConsentIssuing(applicationId, null))),
  APPROVE_FOR_ISSUING("Ready to grant and issue", 1, true, true,
      applicationId -> ReverseRouter.route(on(ConsentIssuingController.class).approveForIssuing(applicationId, null, null)),
      null),
  RETURN_TO_CASE_OFFICER("Return to case officer", 2, true, false,
      applicationId -> ReverseRouter.route(on(CaseAssignmentController.class)
          .returnToCaseOfficer(applicationId, null, null)), null),
  ISSUE_CONSENT("Grant consent and issue", 1, true, true, null,
      applicationId -> ReverseRouter.route(on(ConsentIssuingController.class)
          .getIssueConsent(applicationId))),
  UNAPPROVE_FOR_ISSUING("Unmark as ready to grant and issue", 2, true, false,
      applicationId -> ReverseRouter.route(on(ConsentIssuingController.class).unapproveForIssuing(applicationId, null)),
      null)
  ;

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
