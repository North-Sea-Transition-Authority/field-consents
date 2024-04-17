package uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment.CaseAssignmentService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentData;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentDataService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.issuing.approval.ConsentIssuingApprovalService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.Consultation;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformation.FurtherInformationService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance.ApplicationDocumentInstanceService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal.ApplicationWithdrawalService;

@ExtendWith(MockitoExtension.class)
class CaseStatusFlagServiceTest {

  @Mock
  private CaseAssignmentService caseAssignmentService;

  @Mock
  private ApplicationWithdrawalService applicationWithdrawalService;

  @Mock
  private TechnicalReviewService technicalReviewService;

  @Mock
  private ApplicationUpdateService applicationUpdateService;

  @Mock
  private ConsultationService consultationService;

  @Mock
  private ConsentDataService consentDataService;

  @Mock
  private ConsentIssuingApprovalService consentIssuingApprovalService;

  @Mock
  private FurtherInformationService furtherInformationService;

  @Mock
  private ApplicationDocumentInstanceService applicationDocumentInstanceService;

  @InjectMocks
  private CaseStatusFlagService caseStatusFlagService;

  private Application application;
  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
   application = new Application();
   applicationVersion = new ApplicationVersion();
   applicationVersion.setApplication(application);
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void isCaseStatusFlagApplicable_APPLICATION_UPDATE_OPEN(boolean applicationUpdateOpen) {
    when(applicationUpdateService.openApplicationUpdateExists(applicationVersion)).thenReturn(applicationUpdateOpen);

    assertThat(caseStatusFlagService.isCaseStatusFlagApplicable(applicationVersion, CaseStatusFlag.APPLICATION_UPDATE_OPEN))
        .isEqualTo(applicationUpdateOpen);
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void isCaseStatusFlagApplicable_APPLICATION_UPDATE_NOT_OPEN(boolean applicationUpdateOpen) {
    when(applicationUpdateService.openApplicationUpdateExists(applicationVersion)).thenReturn(applicationUpdateOpen);

    assertThat(caseStatusFlagService.isCaseStatusFlagApplicable(applicationVersion, CaseStatusFlag.APPLICATION_UPDATE_NOT_OPEN))
        .isEqualTo(!applicationUpdateOpen);
  }

  @Test
  void isCaseStatusFlagApplicable_APPLICATION_UPDATE_STARTED_update_not_open() {
    var applicationVersion = new ApplicationVersion();
    applicationVersion.setStatus(ApplicationVersionStatus.IN_PROGRESS);

    when(applicationUpdateService.openApplicationUpdateExists(applicationVersion)).thenReturn(false);

    assertThat(caseStatusFlagService.isCaseStatusFlagApplicable(applicationVersion, CaseStatusFlag.APPLICATION_UPDATE_STARTED)).isFalse();
  }

  @ParameterizedTest
  @EnumSource(value = ApplicationVersionStatus.class, names = "IN_PROGRESS", mode = EnumSource.Mode.EXCLUDE)
  void isCaseStatusFlagApplicable_APPLICATION_UPDATE_STARTED_not_in_progress(ApplicationVersionStatus applicationVersionStatus) {
    var applicationVersion = new ApplicationVersion();
    applicationVersion.setStatus(applicationVersionStatus);

    when(applicationUpdateService.openApplicationUpdateExists(applicationVersion)).thenReturn(true);

    assertThat(caseStatusFlagService.isCaseStatusFlagApplicable(applicationVersion, CaseStatusFlag.APPLICATION_UPDATE_STARTED)).isFalse();
  }

  @Test
  void isCaseStatusFlagApplicable_APPLICATION_UPDATE_STARTED_in_progress() {
    var applicationVersion = new ApplicationVersion();
    applicationVersion.setStatus(ApplicationVersionStatus.IN_PROGRESS);

    when(applicationUpdateService.openApplicationUpdateExists(applicationVersion)).thenReturn(true);

    assertThat(caseStatusFlagService.isCaseStatusFlagApplicable(applicationVersion, CaseStatusFlag.APPLICATION_UPDATE_STARTED)).isTrue();
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void isCaseStatusFlagApplicable_CAM_ASSIGNED(boolean camAssigned) {
    when(caseAssignmentService.isCamAssigned(applicationVersion)).thenReturn(camAssigned);

    assertThat(caseStatusFlagService.isCaseStatusFlagApplicable(applicationVersion, CaseStatusFlag.CAM_ASSIGNED))
        .isEqualTo(camAssigned);
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void isCaseStatusFlagApplicable_CAM_NOT_ASSIGNED(boolean camAssigned) {
    when(caseAssignmentService.isCamAssigned(applicationVersion)).thenReturn(camAssigned);

    assertThat(caseStatusFlagService.isCaseStatusFlagApplicable(applicationVersion, CaseStatusFlag.CAM_NOT_ASSIGNED))
        .isEqualTo(!camAssigned);
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void isCaseStatusFlagApplicable_CASE_OFFICER_ASSIGNED(boolean caseOfficerAssigned) {
    when(caseAssignmentService.isCaseOfficerAssigned(applicationVersion)).thenReturn(caseOfficerAssigned);

    assertThat(caseStatusFlagService.isCaseStatusFlagApplicable(applicationVersion, CaseStatusFlag.CASE_OFFICER_ASSIGNED))
        .isEqualTo(caseOfficerAssigned);
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void isCaseStatusFlagApplicable_CASE_OFFICER_NOT_ASSIGNED(boolean caseOfficerAssigned) {
    when(caseAssignmentService.isCaseOfficerAssigned(applicationVersion)).thenReturn(caseOfficerAssigned);

    assertThat(caseStatusFlagService.isCaseStatusFlagApplicable(applicationVersion, CaseStatusFlag.CASE_OFFICER_NOT_ASSIGNED))
        .isEqualTo(!caseOfficerAssigned);
  }

  @Test
  void isCaseStatusFlagApplicable_CASE_NOTES_ALLOWED() {
    assertThat(caseStatusFlagService.isCaseStatusFlagApplicable(applicationVersion, CaseStatusFlag.CASE_NOTES_ALLOWED)).isTrue();
  }

  @Test
  void isCaseStatusFlagApplicable_CONSENT_DATA_EXISTS_exists() {
    when(consentDataService.findConsentData(application)).thenReturn(Optional.of(new ConsentData()));
    assertThat(caseStatusFlagService.isCaseStatusFlagApplicable(applicationVersion, CaseStatusFlag.CONSENT_DATA_EXISTS)).isTrue();
  }

  @Test
  void isCaseStatusFlagApplicable_CONSENT_DATA_EXISTS_does_not_exist() {
    when(consentDataService.findConsentData(application)).thenReturn(Optional.empty());
    assertThat(caseStatusFlagService.isCaseStatusFlagApplicable(applicationVersion, CaseStatusFlag.CONSENT_DATA_EXISTS)).isFalse();
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void isCaseStatusFlagApplicable_CONSENT_APPROVED_FOR_ISSUE(boolean consentApprovedForIssue) {
    when(consentIssuingApprovalService.isApplicationApprovedForConsentIssuing(application)).thenReturn(consentApprovedForIssue);

    assertThat(caseStatusFlagService.isCaseStatusFlagApplicable(applicationVersion, CaseStatusFlag.CONSENT_APPROVED_FOR_ISSUE))
        .isEqualTo(consentApprovedForIssue);
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void isCaseStatusFlagApplicable_CONSENT_NOT_APPROVED_FOR_ISSUE(boolean consentApprovedForIssue) {
    when(consentIssuingApprovalService.isApplicationApprovedForConsentIssuing(application)).thenReturn(consentApprovedForIssue);

    assertThat(caseStatusFlagService.isCaseStatusFlagApplicable(applicationVersion, CaseStatusFlag.CONSENT_NOT_APPROVED_FOR_ISSUE))
        .isEqualTo(!consentApprovedForIssue);
  }

  @Test
  void isCaseStatusFlagApplicable_CONSULTATION_OPEN_open() {
    when(consultationService.findLatestOpenConsultation(application)).thenReturn(Optional.of(new Consultation()));
    assertThat(caseStatusFlagService.isCaseStatusFlagApplicable(applicationVersion, CaseStatusFlag.CONSULTATION_OPEN)).isTrue();
  }

  @Test
  void isCaseStatusFlagApplicable_CONSULTATION_OPEN_not_open() {
    when(consultationService.findLatestOpenConsultation(application)).thenReturn(Optional.empty());
    assertThat(caseStatusFlagService.isCaseStatusFlagApplicable(applicationVersion, CaseStatusFlag.CONSULTATION_OPEN)).isFalse();
  }

  @Test
  void isCaseStatusFlagApplicable_CONSULTATION_NOT_OPEN_open() {
    when(consultationService.findLatestOpenConsultation(application)).thenReturn(Optional.of(new Consultation()));
    assertThat(caseStatusFlagService.isCaseStatusFlagApplicable(applicationVersion, CaseStatusFlag.CONSULTATION_NOT_OPEN)).isFalse();
  }

  @Test
  void isCaseStatusFlagApplicable_CONSULTATION_NOT_OPEN_not_open() {
    when(consultationService.findLatestOpenConsultation(application)).thenReturn(Optional.empty());
    assertThat(caseStatusFlagService.isCaseStatusFlagApplicable(applicationVersion, CaseStatusFlag.CONSULTATION_NOT_OPEN)).isTrue();
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void isCaseStatusFlagApplicable_CONSULTATION_FURTHER_INFORMATION_OPEN(boolean consultationFurtherInformationRequestOpen) {
    when(furtherInformationService.isFurtherInformationRequestOpen(applicationVersion)).thenReturn(consultationFurtherInformationRequestOpen);

    assertThat(caseStatusFlagService.isCaseStatusFlagApplicable(applicationVersion, CaseStatusFlag.CONSULTATION_FURTHER_INFORMATION_OPEN))
        .isEqualTo(consultationFurtherInformationRequestOpen);
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void isCaseStatusFlagApplicable_CONSULTATION_FURTHER_INFORMATION_NOT_OPEN(boolean consultationFurtherInformationRequestOpen) {
    when(furtherInformationService.isFurtherInformationRequestOpen(applicationVersion)).thenReturn(
        consultationFurtherInformationRequestOpen);

    assertThat(caseStatusFlagService.isCaseStatusFlagApplicable(applicationVersion, CaseStatusFlag.CONSULTATION_FURTHER_INFORMATION_NOT_OPEN))
        .isEqualTo(!consultationFurtherInformationRequestOpen);
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void isCaseStatusFlagApplicable_MAIL_MERGE_ERROR_PRESENT(boolean mailMergeErrorPresent) {
    when(applicationDocumentInstanceService.mailMergeErrorPresent(application)).thenReturn(mailMergeErrorPresent);

    assertThat(caseStatusFlagService.isCaseStatusFlagApplicable(applicationVersion, CaseStatusFlag.MAIL_MERGE_ERROR_PRESENT))
        .isEqualTo(mailMergeErrorPresent);
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void isCaseStatusFlagApplicable_MAIL_MERGE_ERROR_NOT_PRESENT(boolean mailMergeErrorPresent) {
    when(applicationDocumentInstanceService.mailMergeErrorPresent(application)).thenReturn(mailMergeErrorPresent);

    assertThat(caseStatusFlagService.isCaseStatusFlagApplicable(applicationVersion, CaseStatusFlag.MAIL_MERGE_ERROR_NOT_PRESENT))
        .isEqualTo(!mailMergeErrorPresent);
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void isCaseStatusFlagApplicable_TECHNICAL_REVIEW_OPEN(boolean technicalReviewOpen) {
    when(technicalReviewService.openTechnicalReviewExists(applicationVersion)).thenReturn(technicalReviewOpen);

    assertThat(caseStatusFlagService.isCaseStatusFlagApplicable(applicationVersion, CaseStatusFlag.TECHNICAL_REVIEW_OPEN))
        .isEqualTo(technicalReviewOpen);
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void isCaseStatusFlagApplicable_TECHNICAL_REVIEW_NOT_OPEN(boolean technicalReviewOpen) {
    when(technicalReviewService.openTechnicalReviewExists(applicationVersion)).thenReturn(technicalReviewOpen);

    assertThat(caseStatusFlagService.isCaseStatusFlagApplicable(applicationVersion, CaseStatusFlag.TECHNICAL_REVIEW_NOT_OPEN))
        .isEqualTo(!technicalReviewOpen);
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void isCaseStatusFlagApplicable_WITHDRAWAL_OPEN(boolean withdrawalOpen) {
    when(applicationWithdrawalService.openWithdrawalExists(applicationVersion)).thenReturn(withdrawalOpen);

    assertThat(caseStatusFlagService.isCaseStatusFlagApplicable(applicationVersion, CaseStatusFlag.WITHDRAWAL_OPEN))
        .isEqualTo(withdrawalOpen);
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void isCaseStatusFlagApplicable_WITHDRAWAL_NOT_OPEN(boolean withdrawalOpen) {
    when(applicationWithdrawalService.openWithdrawalExists(applicationVersion)).thenReturn(withdrawalOpen);

    assertThat(caseStatusFlagService.isCaseStatusFlagApplicable(applicationVersion, CaseStatusFlag.WITHDRAWAL_NOT_OPEN))
        .isEqualTo(!withdrawalOpen);
  }

}
