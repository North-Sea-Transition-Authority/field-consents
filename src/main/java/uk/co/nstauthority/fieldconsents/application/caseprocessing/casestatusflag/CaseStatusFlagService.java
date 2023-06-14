package uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal.ApplicationWithdrawalService;

@Service
public class CaseStatusFlagService {

  private final ApplicationWithdrawalService applicationWithdrawalService;

  public CaseStatusFlagService(ApplicationWithdrawalService applicationWithdrawalService) {
    this.applicationWithdrawalService = applicationWithdrawalService;
  }

  public Set<CaseStatusFlag> getCaseStatusFlags(ApplicationVersion applicationVersion) {
    var caseStatusFlags = new HashSet<CaseStatusFlag>();

    if (Objects.nonNull(applicationVersion.getCaseOfficerWuaId())) {
      caseStatusFlags.add(CaseStatusFlag.CASE_OFFICER_ASSIGNED);
    } else {
      caseStatusFlags.add(CaseStatusFlag.CASE_OFFICER_NOT_ASSIGNED);
    }

    if (applicationWithdrawalService.openWithdrawalExists(applicationVersion)) {
      caseStatusFlags.add(CaseStatusFlag.WITHDRAWAL_OPEN);
    } else {
      caseStatusFlags.add(CaseStatusFlag.NO_WITHDRAWAL_OPEN);
    }

    return caseStatusFlags;
  }
}
