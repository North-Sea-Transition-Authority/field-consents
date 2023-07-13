package uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.CASE_NOTES_ALLOWED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.CASE_OFFICER_ASSIGNED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.CASE_OFFICER_NOT_ASSIGNED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.NO_TECHNICAL_REVIEW_OPEN;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.NO_WITHDRAWAL_OPEN;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.TECHNICAL_REVIEW_OPEN;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.UPDATE_REQUEST_OPEN;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.WITHDRAWAL_OPEN;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal.ApplicationWithdrawalService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class CaseStatusFlagServiceTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  @InjectMocks
  private CaseStatusFlagService caseStatusFlagService;

  @Mock
  private ApplicationWithdrawalService applicationWithdrawalService;

  @Mock
  private TechnicalReviewService technicalReviewService;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.FLARE);
    when(applicationWithdrawalService.openWithdrawalExists(applicationVersion)).thenReturn(false);
  }

  @Test
  void getCaseStatusFlags_whenCaseOfficerAssigned() {
    applicationVersion.setCaseOfficerWuaId(USER.wuaId());
    assertThat(caseStatusFlagService.getCaseStatusFlags(applicationVersion))
        .containsOnly(
            CASE_OFFICER_ASSIGNED,
            NO_WITHDRAWAL_OPEN,
            NO_TECHNICAL_REVIEW_OPEN,
            CASE_NOTES_ALLOWED,
            UPDATE_REQUEST_OPEN
        );
  }

  @Test
  void getCaseStatusFlags_whenCaseOfficerNotAssigned() {
    assertThat(caseStatusFlagService.getCaseStatusFlags(applicationVersion))
        .containsOnly(
            CASE_OFFICER_NOT_ASSIGNED,
            NO_WITHDRAWAL_OPEN,
            NO_TECHNICAL_REVIEW_OPEN,
            CASE_NOTES_ALLOWED,
            UPDATE_REQUEST_OPEN
        );
  }

  @Test
  void getCaseStatusFlags_whenWithdrawalOpen() {
    when(applicationWithdrawalService.openWithdrawalExists(applicationVersion)).thenReturn(true);
    assertThat(caseStatusFlagService.getCaseStatusFlags(applicationVersion))
        .containsOnly(
            CASE_OFFICER_NOT_ASSIGNED,
            WITHDRAWAL_OPEN,
            NO_TECHNICAL_REVIEW_OPEN,
            CASE_NOTES_ALLOWED,
            UPDATE_REQUEST_OPEN
        );
  }

  @Test
  void getCaseStatusFlags_whenTechnicalReviewOpen() {
    when(technicalReviewService.openTechnicalReviewExists(applicationVersion)).thenReturn(true);
    assertThat(caseStatusFlagService.getCaseStatusFlags(applicationVersion))
        .containsOnly(
            CASE_OFFICER_NOT_ASSIGNED,
            NO_WITHDRAWAL_OPEN,
            TECHNICAL_REVIEW_OPEN,
            CASE_NOTES_ALLOWED,
            UPDATE_REQUEST_OPEN
        );
  }
}
