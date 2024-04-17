package uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag;

import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment.CaseAssignmentService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentDataService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.issuing.approval.ConsentIssuingApprovalService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformation.FurtherInformationService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance.ApplicationDocumentInstanceService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal.ApplicationWithdrawalService;

@Service
public class CaseStatusFlagService {

  private final CaseAssignmentService caseAssignmentService;
  private final ApplicationWithdrawalService applicationWithdrawalService;
  private final TechnicalReviewService technicalReviewService;
  private final ApplicationUpdateService applicationUpdateService;
  private final ConsultationService consultationService;
  private final ConsentDataService consentDataService;
  private final ConsentIssuingApprovalService consentIssuingApprovalService;
  private final FurtherInformationService furtherInformationService;
  private final ApplicationDocumentInstanceService applicationDocumentInstanceService;

  CaseStatusFlagService(
      CaseAssignmentService caseAssignmentService,
      ApplicationWithdrawalService applicationWithdrawalService,
      TechnicalReviewService technicalReviewService,
      ApplicationUpdateService applicationUpdateService,
      ConsultationService consultationService,
      ConsentDataService consentDataService,
      ConsentIssuingApprovalService consentIssuingApprovalService,
      FurtherInformationService furtherInformationService,
      ApplicationDocumentInstanceService applicationDocumentInstanceService
  ) {
    this.caseAssignmentService = caseAssignmentService;
    this.applicationWithdrawalService = applicationWithdrawalService;
    this.technicalReviewService = technicalReviewService;
    this.applicationUpdateService = applicationUpdateService;
    this.consultationService = consultationService;
    this.consentDataService = consentDataService;
    this.consentIssuingApprovalService = consentIssuingApprovalService;
    this.furtherInformationService = furtherInformationService;
    this.applicationDocumentInstanceService = applicationDocumentInstanceService;
  }

  public boolean isCaseStatusFlagApplicable(ApplicationVersion applicationVersion, CaseStatusFlag caseStatusFlag) {
    var application = applicationVersion.getApplication();

    return switch (caseStatusFlag) {
      // Application update
      case APPLICATION_UPDATE_OPEN -> applicationUpdateService.openApplicationUpdateExists(applicationVersion);
      case APPLICATION_UPDATE_NOT_OPEN -> !applicationUpdateService.openApplicationUpdateExists(applicationVersion);
      case APPLICATION_UPDATE_STARTED -> applicationUpdateService.openApplicationUpdateExists(applicationVersion)
          && ApplicationVersionStatus.IN_PROGRESS.equals(applicationVersion.getStatus());

      // Assignment
      case CAM_ASSIGNED -> caseAssignmentService.isCamAssigned(applicationVersion);
      case CAM_NOT_ASSIGNED -> !caseAssignmentService.isCamAssigned(applicationVersion);
      case CASE_OFFICER_ASSIGNED -> caseAssignmentService.isCaseOfficerAssigned(applicationVersion);
      case CASE_OFFICER_NOT_ASSIGNED -> !caseAssignmentService.isCaseOfficerAssigned(applicationVersion);

      // Case notes
      case CASE_NOTES_ALLOWED -> true;

      // Consent
      case CONSENT_DATA_EXISTS -> consentDataService.findConsentData(application).isPresent();
      case CONSENT_APPROVED_FOR_ISSUE -> consentIssuingApprovalService.isApplicationApprovedForConsentIssuing(application);
      case CONSENT_NOT_APPROVED_FOR_ISSUE -> !consentIssuingApprovalService.isApplicationApprovedForConsentIssuing(application);

      // Consultation
      case CONSULTATION_OPEN -> consultationService.findLatestOpenConsultation(application).isPresent();
      case CONSULTATION_NOT_OPEN -> consultationService.findLatestOpenConsultation(application).isEmpty();
      case CONSULTATION_FURTHER_INFORMATION_OPEN ->
          furtherInformationService.isFurtherInformationRequestOpen(applicationVersion);
      case CONSULTATION_FURTHER_INFORMATION_NOT_OPEN ->
          !furtherInformationService.isFurtherInformationRequestOpen(applicationVersion);

      // Mail merge
      case MAIL_MERGE_ERROR_PRESENT -> applicationDocumentInstanceService.mailMergeErrorPresent(application);
      case MAIL_MERGE_ERROR_NOT_PRESENT -> !applicationDocumentInstanceService.mailMergeErrorPresent(application);

      // Technical review
      case TECHNICAL_REVIEW_OPEN -> technicalReviewService.openTechnicalReviewExists(applicationVersion);
      case TECHNICAL_REVIEW_NOT_OPEN -> !technicalReviewService.openTechnicalReviewExists(applicationVersion);

      // Withdrawal
      case WITHDRAWAL_OPEN -> applicationWithdrawalService.openWithdrawalExists(applicationVersion);
      case WITHDRAWAL_NOT_OPEN -> !applicationWithdrawalService.openWithdrawalExists(applicationVersion);
    };
  }

}
