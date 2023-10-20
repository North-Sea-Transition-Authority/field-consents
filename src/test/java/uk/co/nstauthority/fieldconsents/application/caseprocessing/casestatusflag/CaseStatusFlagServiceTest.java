package uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag;

import static java.util.Collections.EMPTY_SET;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.APPLICATION_UPDATE_OPEN;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.APPLICATION_UPDATE_STARTED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.CASE_NOTES_ALLOWED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.CASE_OFFICER_ASSIGNED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.CASE_OFFICER_NOT_ASSIGNED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.CONSULTATION_FURTHER_INFORMATION_OPEN;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.CONSULTATION_OPEN;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.CONSULTATION_UNASSIGNED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.NO_APPLICATION_UPDATE_OPEN;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.NO_CONSULTATION_FURTHER_INFORMATION_OPEN;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.NO_CONSULTATION_OPEN;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.NO_TECHNICAL_REVIEW_OPEN;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.NO_WITHDRAWAL_OPEN;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.TECHNICAL_REVIEWS_PAGE_ENABLED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.TECHNICAL_REVIEW_OPEN;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.WITHDRAWAL_OPEN;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.Consultation;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationStatus;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformation.FurtherInformation;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformation.FurtherInformationService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformation.FurtherInformationStatus;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal.ApplicationWithdrawalService;

@ExtendWith(MockitoExtension.class)
class CaseStatusFlagServiceTest {

  private static final CaseStatusFlag DEFAULT_TEST_FLAG = CONSULTATION_OPEN;

  @Mock
  private ApplicationWithdrawalService applicationWithdrawalService;

  @Mock
  private TechnicalReviewService technicalReviewService;

  @Mock
  private ApplicationUpdateService applicationUpdateService;

  @Mock
  private ConsultationService consultationService;

  @Mock
  private FurtherInformationService furtherInformationService;

  @Spy
  @InjectMocks
  private CaseStatusFlagService caseStatusFlagService;

  private Application application;

  private ApplicationVersion applicationVersion;

  private Consultation consultation;

  @BeforeEach
  void setUp() {
    application = new Application();

    applicationVersion = new ApplicationVersion();
    applicationVersion.setApplication(application);

    consultation = new Consultation();
  }

  @Test
  void getCaseStatusFlags() {
    doReturn(EMPTY_SET).when(caseStatusFlagService).getDefaultFlags();
    doReturn(EMPTY_SET).when(caseStatusFlagService).getCaseOfficerAssignmentFlag(applicationVersion);
    doReturn(EMPTY_SET).when(caseStatusFlagService).getWithdrawalFlag(applicationVersion);
    doReturn(EMPTY_SET).when(caseStatusFlagService).getTechnicalReviewFlag(applicationVersion);
    doReturn(EMPTY_SET).when(caseStatusFlagService).getFurtherInformationFlag(applicationVersion);
    doReturn(EMPTY_SET).when(caseStatusFlagService).getUpdateRequestFlag(applicationVersion);
    doReturn(EMPTY_SET).when(caseStatusFlagService).getConsultationFlags(applicationVersion);

    assertThat(caseStatusFlagService.getCaseStatusFlags(applicationVersion)).isEmpty();

    verify(caseStatusFlagService).getDefaultFlags();
    verify(caseStatusFlagService).getCaseOfficerAssignmentFlag(applicationVersion);
    verify(caseStatusFlagService).getWithdrawalFlag(applicationVersion);
    verify(caseStatusFlagService).getTechnicalReviewFlag(applicationVersion);
    verify(caseStatusFlagService).getFurtherInformationFlag(applicationVersion);
    verify(caseStatusFlagService).getUpdateRequestFlag(applicationVersion);
    verify(caseStatusFlagService).getConsultationFlags(applicationVersion);
  }

  @Test
  void getCaseStatusFlags_getDefaultFlags() {
    doReturn(singleton(DEFAULT_TEST_FLAG)).when(caseStatusFlagService).getDefaultFlags();
    doReturn(EMPTY_SET).when(caseStatusFlagService).getCaseOfficerAssignmentFlag(applicationVersion);
    doReturn(EMPTY_SET).when(caseStatusFlagService).getWithdrawalFlag(applicationVersion);
    doReturn(EMPTY_SET).when(caseStatusFlagService).getTechnicalReviewFlag(applicationVersion);
    doReturn(EMPTY_SET).when(caseStatusFlagService).getFurtherInformationFlag(applicationVersion);
    doReturn(EMPTY_SET).when(caseStatusFlagService).getUpdateRequestFlag(applicationVersion);
    doReturn(EMPTY_SET).when(caseStatusFlagService).getConsultationFlags(applicationVersion);

    getCaseStatusFlags_assertFlagAndVerify();
  }

  @Test
  void getCaseStatusFlags_getCaseOfficerAssignmentFlag() {
    doReturn(EMPTY_SET).when(caseStatusFlagService).getDefaultFlags();
    doReturn(singleton(DEFAULT_TEST_FLAG)).when(caseStatusFlagService).getCaseOfficerAssignmentFlag(applicationVersion);
    doReturn(EMPTY_SET).when(caseStatusFlagService).getWithdrawalFlag(applicationVersion);
    doReturn(EMPTY_SET).when(caseStatusFlagService).getTechnicalReviewFlag(applicationVersion);
    doReturn(EMPTY_SET).when(caseStatusFlagService).getFurtherInformationFlag(applicationVersion);
    doReturn(EMPTY_SET).when(caseStatusFlagService).getUpdateRequestFlag(applicationVersion);
    doReturn(EMPTY_SET).when(caseStatusFlagService).getConsultationFlags(applicationVersion);

    getCaseStatusFlags_assertFlagAndVerify();
  }

  @Test
  void getCaseStatusFlags_getWithdrawalFlag() {
    doReturn(EMPTY_SET).when(caseStatusFlagService).getDefaultFlags();
    doReturn(EMPTY_SET).when(caseStatusFlagService).getCaseOfficerAssignmentFlag(applicationVersion);
    doReturn(singleton(DEFAULT_TEST_FLAG)).when(caseStatusFlagService).getWithdrawalFlag(applicationVersion);
    doReturn(EMPTY_SET).when(caseStatusFlagService).getTechnicalReviewFlag(applicationVersion);
    doReturn(EMPTY_SET).when(caseStatusFlagService).getFurtherInformationFlag(applicationVersion);
    doReturn(EMPTY_SET).when(caseStatusFlagService).getUpdateRequestFlag(applicationVersion);
    doReturn(EMPTY_SET).when(caseStatusFlagService).getConsultationFlags(applicationVersion);

    getCaseStatusFlags_assertFlagAndVerify();
  }

  @Test
  void getCaseStatusFlags_getFurtherInformationFlag() {
    doReturn(EMPTY_SET).when(caseStatusFlagService).getDefaultFlags();
    doReturn(EMPTY_SET).when(caseStatusFlagService).getCaseOfficerAssignmentFlag(applicationVersion);
    doReturn(EMPTY_SET).when(caseStatusFlagService).getWithdrawalFlag(applicationVersion);
    doReturn(singleton(DEFAULT_TEST_FLAG)).when(caseStatusFlagService).getTechnicalReviewFlag(applicationVersion);
    doReturn(EMPTY_SET).when(caseStatusFlagService).getFurtherInformationFlag(applicationVersion);
    doReturn(EMPTY_SET).when(caseStatusFlagService).getUpdateRequestFlag(applicationVersion);
    doReturn(EMPTY_SET).when(caseStatusFlagService).getConsultationFlags(applicationVersion);

    getCaseStatusFlags_assertFlagAndVerify();
  }

  @Test
  void getCaseStatusFlags_getTechnicalReviewFlag() {
    doReturn(EMPTY_SET).when(caseStatusFlagService).getDefaultFlags();
    doReturn(EMPTY_SET).when(caseStatusFlagService).getCaseOfficerAssignmentFlag(applicationVersion);
    doReturn(EMPTY_SET).when(caseStatusFlagService).getWithdrawalFlag(applicationVersion);
    doReturn(EMPTY_SET).when(caseStatusFlagService).getTechnicalReviewFlag(applicationVersion);
    doReturn(singleton(DEFAULT_TEST_FLAG)).when(caseStatusFlagService).getFurtherInformationFlag(applicationVersion);
    doReturn(EMPTY_SET).when(caseStatusFlagService).getUpdateRequestFlag(applicationVersion);
    doReturn(EMPTY_SET).when(caseStatusFlagService).getConsultationFlags(applicationVersion);

    getCaseStatusFlags_assertFlagAndVerify();
  }


  @Test
  void getCaseStatusFlags_getUpdateRequestFlag() {
    doReturn(EMPTY_SET).when(caseStatusFlagService).getDefaultFlags();
    doReturn(EMPTY_SET).when(caseStatusFlagService).getCaseOfficerAssignmentFlag(applicationVersion);
    doReturn(EMPTY_SET).when(caseStatusFlagService).getWithdrawalFlag(applicationVersion);
    doReturn(EMPTY_SET).when(caseStatusFlagService).getTechnicalReviewFlag(applicationVersion);
    doReturn(EMPTY_SET).when(caseStatusFlagService).getFurtherInformationFlag(applicationVersion);
    doReturn(singleton(DEFAULT_TEST_FLAG)).when(caseStatusFlagService).getUpdateRequestFlag(applicationVersion);
    doReturn(EMPTY_SET).when(caseStatusFlagService).getConsultationFlags(applicationVersion);

    getCaseStatusFlags_assertFlagAndVerify();
  }

  @Test
  void getCaseStatusFlags_getConsultationFlags() {
    doReturn(EMPTY_SET).when(caseStatusFlagService).getDefaultFlags();
    doReturn(EMPTY_SET).when(caseStatusFlagService).getCaseOfficerAssignmentFlag(applicationVersion);
    doReturn(EMPTY_SET).when(caseStatusFlagService).getWithdrawalFlag(applicationVersion);
    doReturn(EMPTY_SET).when(caseStatusFlagService).getTechnicalReviewFlag(applicationVersion);
    doReturn(EMPTY_SET).when(caseStatusFlagService).getFurtherInformationFlag(applicationVersion);
    doReturn(EMPTY_SET).when(caseStatusFlagService).getUpdateRequestFlag(applicationVersion);
    doReturn(singleton(DEFAULT_TEST_FLAG)).when(caseStatusFlagService).getConsultationFlags(applicationVersion);

    getCaseStatusFlags_assertFlagAndVerify();
  }

  private void getCaseStatusFlags_assertFlagAndVerify() {
    assertThat(caseStatusFlagService.getCaseStatusFlags(applicationVersion)).containsExactly(DEFAULT_TEST_FLAG);

    verify(caseStatusFlagService).getDefaultFlags();
    verify(caseStatusFlagService).getCaseOfficerAssignmentFlag(applicationVersion);
    verify(caseStatusFlagService).getWithdrawalFlag(applicationVersion);
    verify(caseStatusFlagService).getTechnicalReviewFlag(applicationVersion);
    verify(caseStatusFlagService).getUpdateRequestFlag(applicationVersion);
    verify(caseStatusFlagService).getConsultationFlags(applicationVersion);
  }

  @Test
  void getDefaultFlags() {
    assertThat(caseStatusFlagService.getDefaultFlags()).containsExactlyInAnyOrder(CASE_NOTES_ALLOWED, TECHNICAL_REVIEWS_PAGE_ENABLED);
  }

  @Test
  void getCaseOfficerAssignmentFlag_assigned() {
    applicationVersion.setCaseOfficerWuaId(1L);
    assertThat(caseStatusFlagService.getCaseOfficerAssignmentFlag(applicationVersion)).containsExactly(CASE_OFFICER_ASSIGNED);
  }

  @Test
  void getCaseOfficerAssignmentFlag_notAssigned() {
    applicationVersion.setCaseOfficerWuaId(null);
    assertThat(caseStatusFlagService.getCaseOfficerAssignmentFlag(applicationVersion)).containsExactly(CASE_OFFICER_NOT_ASSIGNED);
  }

  @Test
  void getWithdrawalFlag_withdrawalExists() {
    when(applicationWithdrawalService.openWithdrawalExists(applicationVersion)).thenReturn(true);
    assertThat(caseStatusFlagService.getWithdrawalFlag(applicationVersion)).containsExactly(WITHDRAWAL_OPEN);
  }

  @Test
  void getWithdrawalFlag_withdrawalDoesNotExist() {
    when(applicationWithdrawalService.openWithdrawalExists(applicationVersion)).thenReturn(false);
    assertThat(caseStatusFlagService.getWithdrawalFlag(applicationVersion)).containsExactly(NO_WITHDRAWAL_OPEN);
  }

  @Test
  void getTechnicalReviewFlag_technicalReviewOpen() {
    when(technicalReviewService.openTechnicalReviewExists(applicationVersion)).thenReturn(true);
    assertThat(caseStatusFlagService.getTechnicalReviewFlag(applicationVersion)).containsExactly(TECHNICAL_REVIEW_OPEN);
  }

  @Test
  void getTechnicalReviewFlag_technicalReviewNotOpen() {
    when(technicalReviewService.openTechnicalReviewExists(applicationVersion)).thenReturn(false);
    assertThat(caseStatusFlagService.getTechnicalReviewFlag(applicationVersion)).containsExactly(NO_TECHNICAL_REVIEW_OPEN);
  }

  @Test
  void getFurtherInformationFlag_furtherInformationOpen() {
    var consultation = new Consultation();
    when(consultationService.findLatestOpenConsultation(application)).thenReturn(Optional.of(consultation));

    var furtherInformation = new FurtherInformation();
    furtherInformation.setStatus(FurtherInformationStatus.OPEN);
    when(furtherInformationService.findLatestOpenFurtherInformation(consultation)).thenReturn(Optional.of(furtherInformation));

    assertThat(caseStatusFlagService.getFurtherInformationFlag(applicationVersion)).containsExactly(
        CONSULTATION_FURTHER_INFORMATION_OPEN);
  }

  @Test
  void getFurtherInformationFlag_noConsultationsExist() {
    when(consultationService.findLatestOpenConsultation(application)).thenReturn(Optional.empty());
    assertThat(caseStatusFlagService.getFurtherInformationFlag(applicationVersion)).containsExactly(
        NO_CONSULTATION_FURTHER_INFORMATION_OPEN);
  }

  @Test
  void getUpdateRequestFlag_noApplicationUpdateExists() {
    when(applicationUpdateService.openApplicationUpdateExists(applicationVersion)).thenReturn(false);
    assertThat(caseStatusFlagService.getUpdateRequestFlag(applicationVersion)).containsExactly(NO_APPLICATION_UPDATE_OPEN);
  }

  @ParameterizedTest
  @EnumSource(value = ApplicationVersionStatus.class, names = "IN_PROGRESS", mode = EXCLUDE)
  void getUpdateRequestFlag_applicationUpdateExists_notInProgress(ApplicationVersionStatus applicationVersionStatus) {
    applicationVersion.setStatus(applicationVersionStatus);
    when(applicationUpdateService.openApplicationUpdateExists(applicationVersion)).thenReturn(true);
    assertThat(caseStatusFlagService.getUpdateRequestFlag(applicationVersion)).containsExactly(APPLICATION_UPDATE_OPEN);
  }

  @Test
  void getUpdateRequestFlag_applicationUpdateExists_inProgress() {
    applicationVersion.setStatus(ApplicationVersionStatus.IN_PROGRESS);
    when(applicationUpdateService.openApplicationUpdateExists(applicationVersion)).thenReturn(true);
    assertThat(caseStatusFlagService.getUpdateRequestFlag(applicationVersion)).containsExactlyInAnyOrder(APPLICATION_UPDATE_OPEN, APPLICATION_UPDATE_STARTED);
  }

  @Test
  void getConsultationFlags_consultationDoesNotExist() {
    when(consultationService.findLatestOpenConsultation(application)).thenReturn(Optional.empty());
    assertThat(caseStatusFlagService.getConsultationFlags(applicationVersion)).containsExactly(NO_CONSULTATION_OPEN);
  }

  @Test
  void getConsultationFlags_consultationExists_isOpen_noResponder() {
    consultation.setStatus(ConsultationStatus.OPEN);
    consultation.setResponderWuaId(null);

    when(consultationService.findLatestOpenConsultation(application)).thenReturn(Optional.of(consultation));
    assertThat(caseStatusFlagService.getConsultationFlags(applicationVersion)).containsExactlyInAnyOrder(CONSULTATION_OPEN, CONSULTATION_UNASSIGNED);
  }

  @Test
  void getConsultationFlags_consultationExists_isOpen_withResponder() {
    consultation.setStatus(ConsultationStatus.OPEN);
    consultation.setResponderWuaId(1L);

    when(consultationService.findLatestOpenConsultation(application)).thenReturn(Optional.of(consultation));
    assertThat(caseStatusFlagService.getConsultationFlags(applicationVersion)).containsExactly(CONSULTATION_OPEN);
  }

}
