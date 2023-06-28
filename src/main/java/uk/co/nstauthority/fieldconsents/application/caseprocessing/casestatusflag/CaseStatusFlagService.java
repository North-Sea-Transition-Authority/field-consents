package uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal.ApplicationWithdrawalService;

@Service
public class CaseStatusFlagService {

  private final ApplicationWithdrawalService applicationWithdrawalService;

  private final TechnicalReviewService technicalReviewService;

  public CaseStatusFlagService(ApplicationWithdrawalService applicationWithdrawalService,
                               TechnicalReviewService technicalReviewService) {
    this.applicationWithdrawalService = applicationWithdrawalService;
    this.technicalReviewService = technicalReviewService;
  }

  public Set<CaseStatusFlag> getCaseStatusFlags(ApplicationVersion applicationVersion) {
    var caseStatusFlags = new HashSet<CaseStatusFlag>();

    addCaseOfficerAssignmentFlag(applicationVersion, caseStatusFlags);

    addWithdrawalFlag(applicationVersion, caseStatusFlags);

    addTechnicalReviewFlag(applicationVersion, caseStatusFlags);

    return caseStatusFlags;
  }

  private void addCaseOfficerAssignmentFlag(ApplicationVersion applicationVersion,
                                            HashSet<CaseStatusFlag> caseStatusFlags) {
    if (Objects.nonNull(applicationVersion.getCaseOfficerWuaId())) {
      caseStatusFlags.add(CaseStatusFlag.CASE_OFFICER_ASSIGNED);
    } else {
      caseStatusFlags.add(CaseStatusFlag.CASE_OFFICER_NOT_ASSIGNED);
    }
  }

  private void addWithdrawalFlag(ApplicationVersion applicationVersion,
                                 HashSet<CaseStatusFlag> caseStatusFlags) {
    if (applicationWithdrawalService.openWithdrawalExists(applicationVersion)) {
      caseStatusFlags.add(CaseStatusFlag.WITHDRAWAL_OPEN);
    } else {
      caseStatusFlags.add(CaseStatusFlag.NO_WITHDRAWAL_OPEN);
    }
  }

  private void addTechnicalReviewFlag(ApplicationVersion applicationVersion,
                                      HashSet<CaseStatusFlag> caseStatusFlags) {
    if (technicalReviewService.openTechnicalReviewExists(applicationVersion)) {
      caseStatusFlags.add(CaseStatusFlag.TECHNICAL_REVIEW_OPEN);
    } else {
      caseStatusFlags.add(CaseStatusFlag.NO_TECHNICAL_REVIEW_OPEN);
    }
  }
}
