package uk.co.nstauthority.fieldconsents.application.caseprocessing.update;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateService.OPEN_APPLICATION_UPDATE_EXISTS;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateStatus.OPEN;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateTestUtil.APPLICATION_UPDATE_REQUEST_TEXT;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateTestUtil.CURRENT_INSTANT;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateTestUtil.DEADLINE_AHEAD_HOURS;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityGroup.INDUSTRY;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityReason.APPLICATION_UPDATE_REQUEST;

import jakarta.persistence.EntityNotFoundException;
import java.time.Clock;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.duplication.ApplicationDuplicationService;
import uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class ApplicationUpdateServiceTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  @Mock
  private ApplicationService applicationService;

  @Mock
  private ApplicationDuplicationService applicationDuplicationService;

  @Mock
  private ApplicationUpdateRepository applicationUpdateRepository;

  @Mock
  private Clock clock;

  @Mock
  private ApplicationWorkAreaPriorityService applicationWorkAreaPriorityService;

  @InjectMocks
  private ApplicationUpdateService applicationUpdateService;

  private ApplicationVersion applicationVersion;

  private ApplicationUpdate applicationUpdate;

  @BeforeEach
  void setup() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    when(clock.instant()).thenReturn(CURRENT_INSTANT);
    applicationUpdate = ApplicationUpdateTestUtil.getOpenApplicationUpdate(applicationVersion, clock);
  }

  @Test
  void openApplicationUpdateExists_whenExists() {
    when(applicationUpdateRepository
        .existsByApplicationVersion_ApplicationAndApplicationUpdateStatus(applicationVersion.getApplication(), OPEN))
        .thenReturn(true);

    assertTrue(applicationUpdateService.openApplicationUpdateExists(applicationVersion));
  }

  @Test
  void openApplicationUpdateExists_whenDoesNotExist() {
    when(applicationUpdateRepository
        .existsByApplicationVersion_ApplicationAndApplicationUpdateStatus(applicationVersion.getApplication(), OPEN))
        .thenReturn(false);

    assertFalse(applicationUpdateService.openApplicationUpdateExists(applicationVersion));
  }

  @Test
  void findOpenApplicationUpdate_whenExists() {
    when(applicationUpdateRepository
        .findByApplicationVersion_ApplicationAndApplicationUpdateStatus(applicationVersion.getApplication(), OPEN))
        .thenReturn(Optional.of(applicationUpdate));

    assertThat(applicationUpdateService.findOpenApplicationUpdate(applicationVersion))
        .contains(applicationUpdate);
  }

  @Test
  void findOpenApplicationUpdate_whenDoesNotExist() {
    when(applicationUpdateRepository
        .findByApplicationVersion_ApplicationAndApplicationUpdateStatus(applicationVersion.getApplication(), OPEN))
        .thenReturn(Optional.empty());

    assertThat(applicationUpdateService.findOpenApplicationUpdate(applicationVersion))
        .isEmpty();
  }

  @Test
  void getOpenApplicationUpdate_whenExists() {
    when(applicationUpdateRepository
        .findByApplicationVersion_ApplicationAndApplicationUpdateStatus(applicationVersion.getApplication(), OPEN))
        .thenReturn(Optional.of(applicationUpdate));

    assertThat(applicationUpdateService.getOpenApplicationUpdate(applicationVersion))
        .isEqualTo(applicationUpdate);
  }

  @Test
  void getOpenApplicationUpdate_whenDoesNotExist() {
    when(applicationUpdateRepository
        .findByApplicationVersion_ApplicationAndApplicationUpdateStatus(applicationVersion.getApplication(), OPEN))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> applicationUpdateService.getOpenApplicationUpdate(applicationVersion))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage(ApplicationUpdateService.NO_OPEN_APPLICATION_UPDATE_EXISTS.apply(String.valueOf(applicationVersion.getId())));
  }

  @Test
  void getApplicationUpdateRequestForm_noOpenApplicationUpdateExists() {
    when(applicationUpdateRepository.existsByApplicationVersion_ApplicationAndApplicationUpdateStatus(applicationVersion.getApplication(), OPEN))
        .thenReturn(false);

    assertThat(applicationUpdateService.getApplicationUpdateRequestForm(applicationVersion))
        .usingRecursiveComparison()
        .isEqualTo(new ApplicationUpdateRequestForm());
  }

  @Test
  void getApplicationUpdateRequestForm_applicationUpdateAlreadyOpen() {
    when(applicationUpdateRepository.existsByApplicationVersion_ApplicationAndApplicationUpdateStatus(applicationVersion.getApplication(), OPEN))
        .thenReturn(true);

    assertThatThrownBy(() -> applicationUpdateService.getApplicationUpdateRequestForm(applicationVersion))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage(OPEN_APPLICATION_UPDATE_EXISTS.apply(String.valueOf(applicationVersion.getId())));
  }

  @Test
  void saveApplicationUpdateRequest() {
    when(clock.instant()).thenReturn(CURRENT_INSTANT);

    applicationUpdateService.saveApplicationUpdateRequest(applicationVersion,
        clock.instant().plus(DEADLINE_AHEAD_HOURS, ChronoUnit.HOURS),
        APPLICATION_UPDATE_REQUEST_TEXT,
        USER
    );

    ArgumentCaptor<ApplicationUpdate> applicationUpdateArgumentCaptor = ArgumentCaptor.forClass(ApplicationUpdate.class);
    verify(applicationUpdateRepository, times(1)).save(applicationUpdateArgumentCaptor.capture());
    var actualApplicationUpdate = applicationUpdateArgumentCaptor.getValue();

    assertThat(actualApplicationUpdate)
        .usingRecursiveComparison()
        .isEqualTo(applicationUpdate);

    verify(applicationWorkAreaPriorityService, times(1))
        .prioritiseApplicationInWorkArea(applicationVersion, USER, APPLICATION_UPDATE_REQUEST, INDUSTRY);
  }

  @Test
  void startApplicationUpdate() {
    var newApplicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
    when(applicationService.startApplicationUpdate(applicationVersion, USER))
        .thenReturn(newApplicationVersion);

    applicationUpdateService.startApplicationUpdate(applicationVersion, USER);

    verify(applicationDuplicationService, times(1))
        .duplicateApplicationSections(applicationVersion, newApplicationVersion);
  }
}
