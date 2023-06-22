package uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal.ApplicationWithdrawalService.NO_OPEN_WITHDRAWAL_FOUND_FOR_APPLICATION_WITH_ID;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal.ApplicationWithdrawalService.OPEN_WITHDRAWAL_FOUND_FOR_APPLICATION_WITH_ID;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal.ApplicationWithdrawalTestUtil.CURRENT_INSTANT;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal.ApplicationWithdrawalTestUtil.WITHDRAWAL_REQUEST_TEXT;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal.ApplicationWithdrawalTestUtil.getOpenApplicationWithdrawal;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityGroup.INDUSTRY;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityGroup.REGULATOR;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityReason.OPERATOR_WITHDRAWAL_REQUEST;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityReason.REGULATOR_REJECT_WITHDRAWAL_REQUEST;

import java.time.Clock;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class ApplicationWithdrawalServiceTest {

  @Mock
  private Clock clock;

  @Mock
  private ApplicationWithdrawalRepository applicationWithdrawalRepository;

  @Mock
  private ApplicationWorkAreaPriorityService applicationWorkAreaPriorityService;

  @Mock
  private ApplicationVersionService applicationVersionService;

  @InjectMocks
  private ApplicationWithdrawalService applicationWithdrawalService;

  private ApplicationVersion applicationVersion;

  private ServiceUserDetail user;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.FLARE);
    user = ServiceUserDetailTestUtil.Builder().build();
  }

  @Test
  void openWithdrawalExists_whenExists() {
    when(applicationWithdrawalRepository
        .existsByApplicationVersionAndWithdrawalStatus(applicationVersion, WithdrawalStatus.OPEN))
        .thenReturn(true);

    assertTrue(applicationWithdrawalService.openWithdrawalExists(applicationVersion));
  }

  @Test
  void openWithdrawalExists_whenDoesNotExist() {
    when(applicationWithdrawalRepository
        .existsByApplicationVersionAndWithdrawalStatus(applicationVersion, WithdrawalStatus.OPEN))
        .thenReturn(false);

    assertFalse(applicationWithdrawalService.openWithdrawalExists(applicationVersion));
  }

  @Test
  void getWithdrawalRequestForm_noOpenWithdrawalRequest() {
    when(applicationWithdrawalRepository.existsByApplicationVersionAndWithdrawalStatus(applicationVersion, WithdrawalStatus.OPEN))
        .thenReturn(false);

    var form = applicationWithdrawalService.getWithdrawalRequestForm(applicationVersion);
    assertThat(form)
        .usingRecursiveComparison()
        .isEqualTo(new WithdrawalRequestForm());
  }

  @Test
  void getWithdrawalRequestForm_withdrawalRequestAlreadyOpen() {
    when(applicationWithdrawalRepository.existsByApplicationVersionAndWithdrawalStatus(applicationVersion, WithdrawalStatus.OPEN))
        .thenReturn(true);

    assertThatThrownBy(() -> applicationWithdrawalService.getWithdrawalRequestForm(applicationVersion))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage(String.format(OPEN_WITHDRAWAL_FOUND_FOR_APPLICATION_WITH_ID, 1));
  }

  @Test
  void saveWithdrawalRequest() {
    when(clock.instant()).thenReturn(CURRENT_INSTANT);

    var applicationWithdrawal = getOpenApplicationWithdrawal(applicationVersion);

    applicationWithdrawalService.saveWithdrawalRequest(applicationVersion, WITHDRAWAL_REQUEST_TEXT, user);

    ApplicationWithdrawal actualApplicationWithdrawal = getCapturedApplicationWithdrawal();

    assertThat(actualApplicationWithdrawal).usingRecursiveComparison().isEqualTo(applicationWithdrawal);

    verify(applicationWorkAreaPriorityService, times(1))
        .prioritiseApplicationInWorkArea(applicationVersion, user, OPERATOR_WITHDRAWAL_REQUEST, INDUSTRY);
    verify(applicationWorkAreaPriorityService, times(1))
        .prioritiseApplicationInWorkArea(applicationVersion, user, OPERATOR_WITHDRAWAL_REQUEST, REGULATOR);
  }

  @Test
  void getOpenApplicationWithdrawal_whenItExists() {
    var applicationWithdrawal = getOpenApplicationWithdrawal(applicationVersion);
    when(applicationWithdrawalRepository.findByApplicationVersionAndWithdrawalStatus(applicationVersion, WithdrawalStatus.OPEN))
        .thenReturn(Optional.of(applicationWithdrawal));

    assertThat(applicationWithdrawalService.getOpenApplicationWithdrawal(applicationVersion))
        .isEqualTo(applicationWithdrawal);
  }

  @Test
  void getOpenApplicationWithdrawal_whenItDoesNotExist() {
    when(applicationWithdrawalRepository.findByApplicationVersionAndWithdrawalStatus(applicationVersion, WithdrawalStatus.OPEN))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> applicationWithdrawalService.getOpenApplicationWithdrawal(applicationVersion))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage(String.format(NO_OPEN_WITHDRAWAL_FOUND_FOR_APPLICATION_WITH_ID, 1));
  }

  @Test
  void getWithdrawalResponseForm_withOpenWithdrawalRequest() {
    when(applicationWithdrawalRepository.findByApplicationVersionAndWithdrawalStatus(applicationVersion, WithdrawalStatus.OPEN))
        .thenReturn(Optional.of(new ApplicationWithdrawal()));

    var form = applicationWithdrawalService.getWithdrawalResponseForm(applicationVersion);
    assertThat(form)
        .usingRecursiveComparison()
        .isEqualTo(new WithdrawalResponseForm());
  }

  @Test
  void getWithdrawalResponseForm_withNoOpenWithdrawalRequest() {
    when(applicationWithdrawalRepository.findByApplicationVersionAndWithdrawalStatus(applicationVersion, WithdrawalStatus.OPEN))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> applicationWithdrawalService.getWithdrawalResponseForm(applicationVersion))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage(String.format(NO_OPEN_WITHDRAWAL_FOUND_FOR_APPLICATION_WITH_ID, 1));
  }

  @Test
  void saveWithdrawalResponse_whenRequestAccepted() {
    when(clock.instant()).thenReturn(CURRENT_INSTANT);

    var applicationWithdrawal = getOpenApplicationWithdrawal(applicationVersion);
    when(applicationWithdrawalRepository.findByApplicationVersionAndWithdrawalStatus(applicationVersion, WithdrawalStatus.OPEN))
        .thenReturn(Optional.of(applicationWithdrawal));

    applicationWithdrawalService.saveWithdrawalResponse(applicationVersion, WithdrawalStatus.ACCEPTED, null, user);

    ApplicationWithdrawal actualApplicationWithdrawal = getCapturedApplicationWithdrawal();

    assertThat(actualApplicationWithdrawal).usingRecursiveComparison().isEqualTo(applicationWithdrawal);

    verify(applicationVersionService, times(1)).withdrawApplicationVersion(applicationVersion);
  }

  @Test
  void saveWithdrawalResponse_whenRequestRejected() {
    when(clock.instant()).thenReturn(CURRENT_INSTANT);

    var applicationWithdrawal = getOpenApplicationWithdrawal(applicationVersion);
    when(applicationWithdrawalRepository.findByApplicationVersionAndWithdrawalStatus(applicationVersion,
        WithdrawalStatus.OPEN))
        .thenReturn(Optional.of(applicationWithdrawal));

    applicationWithdrawalService.saveWithdrawalResponse(applicationVersion, WithdrawalStatus.REJECTED, "request rejected", user);

    ApplicationWithdrawal actualApplicationWithdrawal = getCapturedApplicationWithdrawal();

    assertThat(actualApplicationWithdrawal).usingRecursiveComparison().isEqualTo(applicationWithdrawal);

    verify(applicationWorkAreaPriorityService, times(1))
        .prioritiseApplicationInWorkArea(applicationVersion, user, REGULATOR_REJECT_WITHDRAWAL_REQUEST, INDUSTRY);
  }

  private ApplicationWithdrawal getCapturedApplicationWithdrawal() {
    ArgumentCaptor<ApplicationWithdrawal> applicationWithdrawalArgumentCaptor = ArgumentCaptor.forClass(ApplicationWithdrawal.class);

    verify(applicationWithdrawalRepository, times(1)).save(applicationWithdrawalArgumentCaptor.capture());

    return applicationWithdrawalArgumentCaptor.getValue();
  }
}
