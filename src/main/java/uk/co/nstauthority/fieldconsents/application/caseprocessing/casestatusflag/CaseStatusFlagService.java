package uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag;

import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.APPLICATION_UPDATE_OPEN;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.APPLICATION_UPDATE_STARTED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.CASE_OFFICER_ASSIGNED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.CASE_OFFICER_NOT_ASSIGNED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.NO_APPLICATION_UPDATE_OPEN;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.NO_TECHNICAL_REVIEW_OPEN;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.NO_WITHDRAWAL_OPEN;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.TECHNICAL_REVIEW_OPEN;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.WITHDRAWAL_OPEN;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal.ApplicationWithdrawalService;

@Service
public class CaseStatusFlagService {

  private final ApplicationWithdrawalService applicationWithdrawalService;
  private final TechnicalReviewService technicalReviewService;
  private final ApplicationUpdateService applicationUpdateService;
  private final ConsultationService consultationService;

  public CaseStatusFlagService(ApplicationWithdrawalService applicationWithdrawalService,
                               TechnicalReviewService technicalReviewService,
                               ApplicationUpdateService applicationUpdateService,
                               ConsultationService consultationService) {
    this.applicationWithdrawalService = applicationWithdrawalService;
    this.technicalReviewService = technicalReviewService;
    this.applicationUpdateService = applicationUpdateService;
    this.consultationService = consultationService;
  }

  public Set<CaseStatusFlag> getCaseStatusFlags(ApplicationVersion applicationVersion) {
    var caseStatusFlags = new HashSet<CaseStatusFlag>();

    addCaseOfficerAssignmentFlag(applicationVersion, caseStatusFlags);
    addWithdrawalFlag(applicationVersion, caseStatusFlags);
    addTechnicalReviewFlag(applicationVersion, caseStatusFlags);
    addUpdateRequestFlag(applicationVersion, caseStatusFlags);
    addConsultationOpenFlag(applicationVersion, caseStatusFlags);
    caseStatusFlags.add(CaseStatusFlag.CASE_NOTES_ALLOWED);

    return caseStatusFlags;
  }

  private void addCaseOfficerAssignmentFlag(ApplicationVersion applicationVersion,
                                            HashSet<CaseStatusFlag> caseStatusFlags) {
    if (Objects.nonNull(applicationVersion.getCaseOfficerWuaId())) {
      caseStatusFlags.add(CASE_OFFICER_ASSIGNED);
    } else {
      caseStatusFlags.add(CASE_OFFICER_NOT_ASSIGNED);
    }
  }

  private void addWithdrawalFlag(ApplicationVersion applicationVersion,
                                 HashSet<CaseStatusFlag> caseStatusFlags) {
    if (applicationWithdrawalService.openWithdrawalExists(applicationVersion)) {
      caseStatusFlags.add(WITHDRAWAL_OPEN);
    } else {
      caseStatusFlags.add(NO_WITHDRAWAL_OPEN);
    }
  }

  private void addTechnicalReviewFlag(ApplicationVersion applicationVersion,
                                      HashSet<CaseStatusFlag> caseStatusFlags) {
    if (technicalReviewService.openTechnicalReviewExists(applicationVersion)) {
      caseStatusFlags.add(TECHNICAL_REVIEW_OPEN);
    } else {
      caseStatusFlags.add(NO_TECHNICAL_REVIEW_OPEN);
    }
  }

  private void addUpdateRequestFlag(ApplicationVersion applicationVersion,
                                    HashSet<CaseStatusFlag> caseStatusFlags) {
    if (applicationUpdateService.openApplicationUpdateExists(applicationVersion)) {
      caseStatusFlags.add(APPLICATION_UPDATE_OPEN);
      // if the case status is IN_PROGRESS then the update must have been started
      if (ApplicationVersionStatus.IN_PROGRESS.equals(applicationVersion.getStatus())) {
        caseStatusFlags.add(APPLICATION_UPDATE_STARTED);
      }
    } else {
      caseStatusFlags.add(NO_APPLICATION_UPDATE_OPEN);
    }
  }

  void addConsultationOpenFlag(ApplicationVersion applicationVersion, HashSet<CaseStatusFlag> caseStatusFlags) {
    var flag = consultationService.openConsultationExistsForApplicationVersion(applicationVersion)
        ? CaseStatusFlag.CONSULTATION_OPEN
        : CaseStatusFlag.NO_CONSULTATION_OPEN;

    caseStatusFlags.add(flag);
  }

}
