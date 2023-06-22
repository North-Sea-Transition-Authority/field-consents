package uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal;

import static uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment.CaseAssignmentTestUtil.ENERGY_PORTAL_USER_1;

import java.time.Instant;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;

public class ApplicationWithdrawalTestUtil {

  static final Instant CURRENT_INSTANT = Instant.now();

  static final String WITHDRAWAL_REQUEST_TEXT = "test request";

  static final Long WITHDRAWAL_REQUEST_USER_WUA_ID = 1L;

  static final String DUMMY_APP_REF = "DUMMY_APP_REF";

  static ApplicationWithdrawal getOpenApplicationWithdrawal(ApplicationVersion applicationVersion) {
    var applicationWithdrawal = new ApplicationWithdrawal();
    applicationWithdrawal.setApplicationVersion(applicationVersion);
    applicationWithdrawal.setRequestedDateTime(CURRENT_INSTANT);
    applicationWithdrawal.setRequestText(WITHDRAWAL_REQUEST_TEXT);
    applicationWithdrawal.setRequestedByWuaId(WITHDRAWAL_REQUEST_USER_WUA_ID);
    applicationWithdrawal.setWithdrawalStatus(WithdrawalStatus.OPEN);
    return applicationWithdrawal;
  }

  static WithdrawalRequestView getWithdrawalRequestView() {
    return new WithdrawalRequestView(
        DUMMY_APP_REF,
        String.format("%s %s", ENERGY_PORTAL_USER_1.forename(), ENERGY_PORTAL_USER_1.surname()),
        DateUtils.format(CURRENT_INSTANT, DateUtils.DATE_TIME),
        WITHDRAWAL_REQUEST_TEXT
        );
  }
}
