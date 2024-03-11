package uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag;

import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.APPLICATION_UPDATE_OPEN;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.APPLICATION_UPDATE_STARTED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.CAM_ASSIGNED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.CAM_NOT_ASSIGNED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.CASE_NOTES_ALLOWED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.CASE_OFFICER_ASSIGNED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.CASE_OFFICER_NOT_ASSIGNED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.CONSENT_DATA_EXISTS;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.CONSENT_NOT_APPROVED_FOR_ISSUE;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.CONSULTATION_FURTHER_INFORMATION_OPEN;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.CONSULTATION_OPEN;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.CONSULTATION_UNASSIGNED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.NO_APPLICATION_UPDATE_OPEN;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.NO_CONSULTATION_FURTHER_INFORMATION_OPEN;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.NO_CONSULTATION_OPEN;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.NO_TECHNICAL_REVIEW_OPEN;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.NO_WITHDRAWAL_OPEN;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.TECHNICAL_REVIEW_OPEN;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.WITHDRAWAL_OPEN;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamRole.CASE_OFFICER;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamRole.CONSENTS_AND_AUTHORISATIONS_MANAGER;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentDataService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.issuing.approval.ConsentIssuingApprovalService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationStatus;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformation.FurtherInformationService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal.ApplicationWithdrawalService;

@Service
public class CaseStatusFlagService {

  private final ApplicationWithdrawalService applicationWithdrawalService;
  private final TechnicalReviewService technicalReviewService;
  private final ApplicationUpdateService applicationUpdateService;
  private final ConsultationService consultationService;
  private final FurtherInformationService furtherInformationService;
  private final ConsentDataService consentDataService;
  private final ConsentIssuingApprovalService consentIssuingApprovalService;

  CaseStatusFlagService(
      ApplicationWithdrawalService applicationWithdrawalService,
      TechnicalReviewService technicalReviewService,
      ApplicationUpdateService applicationUpdateService,
      ConsultationService consultationService,
      FurtherInformationService furtherInformationService,
      ConsentDataService consentDataService,
      ConsentIssuingApprovalService consentIssuingApprovalService
  ) {
    this.applicationWithdrawalService = applicationWithdrawalService;
    this.technicalReviewService = technicalReviewService;
    this.applicationUpdateService = applicationUpdateService;
    this.consultationService = consultationService;
    this.furtherInformationService = furtherInformationService;
    this.consentDataService = consentDataService;
    this.consentIssuingApprovalService = consentIssuingApprovalService;
  }

  public Set<CaseStatusFlag> getCaseStatusFlags(ApplicationVersion applicationVersion) {
    var caseStatusFlags = new HashSet<CaseStatusFlag>();

    var application = applicationVersion.getApplication();

    caseStatusFlags.addAll(getDefaultFlags());
    caseStatusFlags.addAll(getCaseOfficerAssignmentFlag(applicationVersion));
    caseStatusFlags.addAll(getWithdrawalFlag(applicationVersion));
    caseStatusFlags.addAll(getTechnicalReviewFlag(applicationVersion));
    caseStatusFlags.addAll(getFurtherInformationFlag(applicationVersion));
    caseStatusFlags.addAll(getUpdateRequestFlag(applicationVersion));
    caseStatusFlags.addAll(getConsultationFlags(applicationVersion));
    caseStatusFlags.addAll(getCamAssignmentFlag(applicationVersion));
    caseStatusFlags.addAll(getConsentDataExistsFlag(application));
    caseStatusFlags.addAll(getConsentNotApprovedForIssueFlag(application));

    return caseStatusFlags;
  }

  Set<CaseStatusFlag> getDefaultFlags() {
    return Set.of(CASE_NOTES_ALLOWED);
  }

  Set<CaseStatusFlag> getCaseOfficerAssignmentFlag(ApplicationVersion applicationVersion) {
    if (Objects.nonNull(applicationVersion.getCaseOfficerWuaId())
        && CASE_OFFICER.equals(applicationVersion.getCurrentCaseOwner())) {
      return Collections.singleton(CASE_OFFICER_ASSIGNED);
    }

    return Collections.singleton(CASE_OFFICER_NOT_ASSIGNED);
  }

  Set<CaseStatusFlag> getWithdrawalFlag(ApplicationVersion applicationVersion) {
    if (applicationWithdrawalService.openWithdrawalExists(applicationVersion)) {
      return Collections.singleton(WITHDRAWAL_OPEN);
    }

    return Collections.singleton(NO_WITHDRAWAL_OPEN);
  }

  Set<CaseStatusFlag> getTechnicalReviewFlag(ApplicationVersion applicationVersion) {
    if (technicalReviewService.openTechnicalReviewExists(applicationVersion)) {
      return Collections.singleton(TECHNICAL_REVIEW_OPEN);
    }

    return Collections.singleton(NO_TECHNICAL_REVIEW_OPEN);
  }

  Set<CaseStatusFlag> getFurtherInformationFlag(ApplicationVersion applicationVersion) {
    return consultationService.findLatestOpenConsultation(applicationVersion.getApplication())
        .flatMap(furtherInformationService::findLatestOpenFurtherInformation)
        .map(fir -> Collections.singleton(CONSULTATION_FURTHER_INFORMATION_OPEN))
        .orElse(Collections.singleton(NO_CONSULTATION_FURTHER_INFORMATION_OPEN));
  }

  Set<CaseStatusFlag> getUpdateRequestFlag(ApplicationVersion applicationVersion) {
    if (!applicationUpdateService.openApplicationUpdateExists(applicationVersion)) {
      return Collections.singleton(NO_APPLICATION_UPDATE_OPEN);
    }

    var flags = new HashSet<CaseStatusFlag>();

    flags.add(APPLICATION_UPDATE_OPEN);

    // if the case status is IN_PROGRESS then the update must have been started
    if (ApplicationVersionStatus.IN_PROGRESS.equals(applicationVersion.getStatus())) {
      flags.add(APPLICATION_UPDATE_STARTED);
    }

    return flags;
  }

  Set<CaseStatusFlag> getConsultationFlags(ApplicationVersion applicationVersion) {
    var consultationOptional = consultationService.findLatestOpenConsultation(applicationVersion.getApplication());

    if (consultationOptional.isEmpty()) {
      return Collections.singleton(NO_CONSULTATION_OPEN);
    }

    var consultation = consultationOptional.get();
    var flags = new HashSet<CaseStatusFlag>();

    if (ConsultationStatus.OPEN.equals(consultation.getStatus())) {
      flags.add(CONSULTATION_OPEN);
    }

    if (Objects.isNull(consultation.getResponderWuaId())) {
      flags.add(CONSULTATION_UNASSIGNED);
    }

    return flags;
  }

  Set<CaseStatusFlag> getCamAssignmentFlag(ApplicationVersion applicationVersion) {
    if (Objects.nonNull(applicationVersion.getCamWuaId())
        && CONSENTS_AND_AUTHORISATIONS_MANAGER.equals(applicationVersion.getCurrentCaseOwner())) {
      return Collections.singleton(CAM_ASSIGNED);
    }

    return Collections.singleton(CAM_NOT_ASSIGNED);
  }

  Set<CaseStatusFlag> getConsentDataExistsFlag(Application application) {
    return consentDataService.findConsentData(application)
        .map(consentData -> Set.of(CONSENT_DATA_EXISTS))
        .orElse(Set.of());
  }

  Set<CaseStatusFlag> getConsentNotApprovedForIssueFlag(Application application) {
    if (!consentIssuingApprovalService.isApplicationApprovedForConsentIssuing(application)) {
      return Set.of(CONSENT_NOT_APPROVED_FOR_ISSUE);
    }
    return Set.of();
  }
}
