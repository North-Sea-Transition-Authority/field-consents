package uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
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
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.TECHNICAL_REVIEW_OPEN;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.WITHDRAWAL_OPEN;

import java.util.HashSet;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.Consultation;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationStatus;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal.ApplicationWithdrawalService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class CaseStatusFlagServiceTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  @Mock
  private ApplicationWithdrawalService applicationWithdrawalService;

  @Mock
  private TechnicalReviewService technicalReviewService;

  @Mock
  private ApplicationUpdateService applicationUpdateService;

  @Mock
  private ConsultationService consultationService;

  @InjectMocks
  private CaseStatusFlagService caseStatusFlagService;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.FLARE);
  }

  @Test
  void getCaseStatusFlags_whenCaseOfficerAssigned() {
    when(applicationWithdrawalService.openWithdrawalExists(applicationVersion)).thenReturn(false);
    applicationVersion.setCaseOfficerWuaId(USER.wuaId());
    assertThat(caseStatusFlagService.getCaseStatusFlags(applicationVersion))
        .containsOnly(
            CASE_OFFICER_ASSIGNED,
            NO_WITHDRAWAL_OPEN,
            NO_TECHNICAL_REVIEW_OPEN,
            CASE_NOTES_ALLOWED,
            NO_APPLICATION_UPDATE_OPEN,
            NO_CONSULTATION_OPEN
        );
  }

  @Test
  void getCaseStatusFlags_whenCaseOfficerNotAssigned() {
    when(applicationWithdrawalService.openWithdrawalExists(applicationVersion)).thenReturn(false);
    assertThat(caseStatusFlagService.getCaseStatusFlags(applicationVersion))
        .containsOnly(
            CASE_OFFICER_NOT_ASSIGNED,
            NO_WITHDRAWAL_OPEN,
            NO_TECHNICAL_REVIEW_OPEN,
            CASE_NOTES_ALLOWED,
            NO_APPLICATION_UPDATE_OPEN,
            NO_CONSULTATION_OPEN
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
            NO_APPLICATION_UPDATE_OPEN,
            NO_CONSULTATION_OPEN
        );
  }

  @Test
  void getCaseStatusFlags_whenTechnicalReviewOpen() {
    when(applicationWithdrawalService.openWithdrawalExists(applicationVersion)).thenReturn(false);
    when(technicalReviewService.openTechnicalReviewExists(applicationVersion)).thenReturn(true);
    assertThat(caseStatusFlagService.getCaseStatusFlags(applicationVersion))
        .containsOnly(
            CASE_OFFICER_NOT_ASSIGNED,
            NO_WITHDRAWAL_OPEN,
            TECHNICAL_REVIEW_OPEN,
            CASE_NOTES_ALLOWED,
            NO_APPLICATION_UPDATE_OPEN,
            NO_CONSULTATION_OPEN
        );
  }


  @Test
  void getCaseStatusFlags_whenApplicationUpdateOpen() {
    when(applicationWithdrawalService.openWithdrawalExists(applicationVersion)).thenReturn(false);
    when(applicationUpdateService.openApplicationUpdateExists(applicationVersion)).thenReturn(true);
    assertThat(caseStatusFlagService.getCaseStatusFlags(applicationVersion))
        .containsOnly(
            CASE_OFFICER_NOT_ASSIGNED,
            NO_WITHDRAWAL_OPEN,
            NO_TECHNICAL_REVIEW_OPEN,
            CASE_NOTES_ALLOWED,
            APPLICATION_UPDATE_OPEN,
            NO_CONSULTATION_OPEN
        );
  }

  @Test
  void getCaseStatusFlags_whenApplicationUpdateInProgress() {
    var applicationUpdateVersionInProgress =
        ApplicationTestUtil.getNewApplicationVersionWithTypeIdAndVersionNumber(ApplicationType.VENT, 2, 2);
    when(applicationWithdrawalService.openWithdrawalExists(applicationUpdateVersionInProgress)).thenReturn(false);
    when(applicationUpdateService.openApplicationUpdateExists(applicationUpdateVersionInProgress)).thenReturn(true);
    assertThat(caseStatusFlagService.getCaseStatusFlags(applicationUpdateVersionInProgress))
        .containsOnly(
            CASE_OFFICER_NOT_ASSIGNED,
            NO_WITHDRAWAL_OPEN,
            NO_TECHNICAL_REVIEW_OPEN,
            CASE_NOTES_ALLOWED,
            APPLICATION_UPDATE_OPEN,
            APPLICATION_UPDATE_STARTED,
            NO_CONSULTATION_OPEN
        );
  }

  @Test
  void addConsultationFlags_openConsultationExists_withResponder() {
    var applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    var caseStatusFlags = new HashSet<CaseStatusFlag>();
    var consultation = new Consultation();
    consultation.setResponderWuaId(123L);
    consultation.setStatus(ConsultationStatus.OPEN);

    when(consultationService.findLatestOpenConsultation(applicationVersion.getApplication()))
        .thenReturn(Optional.of(consultation));

    caseStatusFlagService.addConsultationFlags(applicationVersion, caseStatusFlags);

    assertThat(caseStatusFlags).containsExactly(CONSULTATION_OPEN);
  }

  @Test
  void addConsultationFlags_openConsultationExists_withoutResponder() {
    var applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    var caseStatusFlags = new HashSet<CaseStatusFlag>();
    var consultation = new Consultation();
    consultation.setStatus(ConsultationStatus.OPEN);

    when(consultationService.findLatestOpenConsultation(applicationVersion.getApplication()))
        .thenReturn(Optional.of(consultation));

    caseStatusFlagService.addConsultationFlags(applicationVersion, caseStatusFlags);

    assertThat(caseStatusFlags).containsExactlyInAnyOrder(CONSULTATION_OPEN, CONSULTATION_UNASSIGNED);
  }

  @Test
  void addConsultationFlags_consultationDoesNotExist() {
    var applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    var caseStatusFlags = new HashSet<CaseStatusFlag>();

    when(consultationService.findLatestOpenConsultation(applicationVersion.getApplication()))
        .thenReturn(Optional.empty());

    caseStatusFlagService.addConsultationFlags(applicationVersion, caseStatusFlags);

    assertThat(caseStatusFlags).containsExactly(NO_CONSULTATION_OPEN);
  }

}
