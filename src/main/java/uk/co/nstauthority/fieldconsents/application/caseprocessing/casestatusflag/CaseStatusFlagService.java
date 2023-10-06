package uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag;

import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.APPLICATION_UPDATE_OPEN;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.APPLICATION_UPDATE_STARTED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.CASE_NOTES_ALLOWED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.CASE_OFFICER_ASSIGNED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.CASE_OFFICER_NOT_ASSIGNED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.CONSULTATION_OPEN;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.CONSULTATION_UNASSIGNED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.NO_APPLICATION_UPDATE_OPEN;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.NO_CONSULTATION_OPEN;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.NO_TECHNICAL_REVIEW_OPEN;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.NO_WITHDRAWAL_OPEN;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.TECHNICAL_REVIEWS_PAGE_ENABLED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.TECHNICAL_REVIEW_OPEN;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.WITHDRAWAL_OPEN;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationStatus;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal.ApplicationWithdrawalService;

@Service
public class CaseStatusFlagService {

  private final ApplicationWithdrawalService applicationWithdrawalService;
  private final TechnicalReviewService technicalReviewService;
  private final ApplicationUpdateService applicationUpdateService;
  private final ConsultationService consultationService;

  CaseStatusFlagService(
      ApplicationWithdrawalService applicationWithdrawalService,
      TechnicalReviewService technicalReviewService,
      ApplicationUpdateService applicationUpdateService,
      ConsultationService consultationService
  ) {
    this.applicationWithdrawalService = applicationWithdrawalService;
    this.technicalReviewService = technicalReviewService;
    this.applicationUpdateService = applicationUpdateService;
    this.consultationService = consultationService;
  }

  public Set<CaseStatusFlag> getCaseStatusFlags(ApplicationVersion applicationVersion) {
    var caseStatusFlags = new HashSet<CaseStatusFlag>();

    caseStatusFlags.addAll(getDefaultFlags());
    caseStatusFlags.addAll(getCaseOfficerAssignmentFlag(applicationVersion));
    caseStatusFlags.addAll(getWithdrawalFlag(applicationVersion));
    caseStatusFlags.addAll(getTechnicalReviewFlag(applicationVersion));
    caseStatusFlags.addAll(getUpdateRequestFlag(applicationVersion));
    caseStatusFlags.addAll(getConsultationFlags(applicationVersion));

    return caseStatusFlags;
  }

  Set<CaseStatusFlag> getDefaultFlags() {
    return Set.of(CASE_NOTES_ALLOWED, TECHNICAL_REVIEWS_PAGE_ENABLED);
  }

  Set<CaseStatusFlag> getCaseOfficerAssignmentFlag(ApplicationVersion applicationVersion) {
    if (Objects.nonNull(applicationVersion.getCaseOfficerWuaId())) {
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

}
