package uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityGroup.INDUSTRY;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityGroup.REGULATOR;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityReason.OPERATOR_WITHDRAWAL_REQUEST;

import java.time.Clock;
import java.time.Instant;
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
import uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class ApplicationWithdrawalServiceTest {

  private static final Instant CURRENT_INSTANT = Instant.now();

  @Mock
  private Clock clock;

  @Mock
  private ApplicationWithdrawalRepository applicationWithdrawalRepository;

  @Mock
  private ApplicationWorkAreaPriorityService applicationWorkAreaPriorityService;

  @InjectMocks
  private ApplicationWithdrawalService applicationWithdrawalService;

  private ApplicationVersion applicationVersion;

  private ServiceUserDetail user;

  private WithdrawalRequestForm form;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.FLARE);
    user = ServiceUserDetailTestUtil.Builder().build();
    form = new WithdrawalRequestForm();
    form.setRequestText("test");
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
    when(applicationWithdrawalRepository.findByApplicationVersionAndWithdrawalStatus(applicationVersion, WithdrawalStatus.OPEN))
        .thenReturn(Optional.empty());

    var form = applicationWithdrawalService.getWithdrawalRequestForm(applicationVersion);
    assertThat(form)
        .usingRecursiveComparison()
        .isEqualTo(new WithdrawalRequestForm());
  }

  @Test
  void getWithdrawalRequestForm_withdrawalRequestAlreadyOpen() {
    when(applicationWithdrawalRepository.findByApplicationVersionAndWithdrawalStatus(applicationVersion, WithdrawalStatus.OPEN))
        .thenReturn(Optional.of(new ApplicationWithdrawal()));

    assertThatThrownBy(() -> applicationWithdrawalService.getWithdrawalRequestForm(applicationVersion))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("A withdrawal request has already been submitted for the application with id 1");
  }

  @Test
  void saveWithdrawalRequest() {
    when(clock.instant()).thenReturn(CURRENT_INSTANT);
    var applicationWithdrawal = new ApplicationWithdrawal();
    applicationWithdrawal.setApplicationVersion(applicationVersion);
    applicationWithdrawal.setRequestedDateTime(CURRENT_INSTANT);
    applicationWithdrawal.setRequestText("test");
    applicationWithdrawal.setRequestedByWuaId(user.wuaId());
    applicationWithdrawal.setWithdrawalStatus(WithdrawalStatus.OPEN);

    applicationWithdrawalService.saveWithdrawalRequest(applicationVersion, form, user);

    ArgumentCaptor<ApplicationWithdrawal> applicationWithdrawalArgumentCaptor = ArgumentCaptor.forClass(ApplicationWithdrawal.class);

    verify(applicationWithdrawalRepository, times(1)).save(applicationWithdrawalArgumentCaptor.capture());

    ApplicationWithdrawal actualApplicationWithdrawal = applicationWithdrawalArgumentCaptor.getValue();

    assertThat(actualApplicationWithdrawal).usingRecursiveComparison().isEqualTo(applicationWithdrawal);

    verify(applicationWorkAreaPriorityService, times(1))
        .prioritiseApplicationInWorkArea(applicationVersion, user, OPERATOR_WITHDRAWAL_REQUEST, INDUSTRY);
    verify(applicationWorkAreaPriorityService, times(1))
        .prioritiseApplicationInWorkArea(applicationVersion, user, OPERATOR_WITHDRAWAL_REQUEST, REGULATOR);
  }
}
