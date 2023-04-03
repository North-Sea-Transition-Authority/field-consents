package uk.co.nstauthority.fieldconsents.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.USER_WUA_ID;

import java.time.Instant;
import java.util.Optional;
import javax.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ApplicationVersionServiceTest {

  private static ApplicationVersionRepository applicationVersionRepository;

  private static ApplicationVersionService applicationVersionService;

  @BeforeAll
  static void setup() {
    applicationVersionRepository = mock(ApplicationVersionRepository.class);
    applicationVersionService = new ApplicationVersionService(applicationVersionRepository);
  }

  @Test
  void getApplicationVersionById_whenApplicationVersionExists() {
    ApplicationVersion applicationVersion =
        new ApplicationVersion(1, new Application(1, ApplicationType.PRODUCTION, Instant.now(), 1),
            1, ApplicationTestUtil.PRIMARY_OPERATOR_OU_ID_1, ApplicationTestUtil.CACHED_PRIMARY_OPERATOR_NAME_1,
            Instant.now(), USER_WUA_ID, ApplicationVersionStatus.IN_PROGRESS);
    when(applicationVersionRepository.findById(1)).thenReturn(Optional.of(applicationVersion));

    ApplicationVersion actualApplicationVersion = applicationVersionService.getApplicationVersionById(1);

    assertApplicationVersion(applicationVersion, actualApplicationVersion);
  }

  @Test
  void getApplicationVersionById_whenApplicationVersionIsNotFound() {
    when(applicationVersionRepository.findById(any(Integer.class))).thenReturn(Optional.empty());

    var exception = Assertions.assertThrows(EntityNotFoundException.class, () ->
        applicationVersionService.getApplicationVersionById(1));

    Assertions.assertEquals("Application version with id 1 not found", exception.getMessage());
  }

  @Test
  void submit() {
    var applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);

    applicationVersionService.submit(applicationVersion);

    ArgumentCaptor<ApplicationVersion> applicationVersionArgumentCaptor = ArgumentCaptor.forClass(ApplicationVersion.class);
    verify(applicationVersionRepository, times(1)).save(applicationVersionArgumentCaptor.capture());

    var actualApplicationVersion = applicationVersionArgumentCaptor.getValue();

    assertApplicationVersion(applicationVersion, actualApplicationVersion);
    assertThat(actualApplicationVersion.getStatus()).isEqualTo(ApplicationVersionStatus.SUBMITTED);
    assertThat(actualApplicationVersion.getSubmittedDateTime()).isEqualTo(applicationVersion.getSubmittedDateTime());
    assertThat(actualApplicationVersion.getSubmittedByWuaId()).isEqualTo(USER_WUA_ID);
  }

  private void assertApplicationVersion(ApplicationVersion applicationVersion, ApplicationVersion actualApplicationVersion) {
    assertThat(actualApplicationVersion.getId()).isEqualTo(applicationVersion.getId());
    assertThat(actualApplicationVersion.getVersion()).isEqualTo(applicationVersion.getVersion());
    assertThat(actualApplicationVersion.getCreatedByWuaId()).isEqualTo(applicationVersion.getCreatedByWuaId());
    assertThat(actualApplicationVersion.getCreatedDateTime()).isEqualTo(applicationVersion.getCreatedDateTime());
    assertThat(actualApplicationVersion.getPrimaryOperatorOuId()).isEqualTo(applicationVersion.getPrimaryOperatorOuId());
    assertThat(actualApplicationVersion.getCachedPrimaryOperatorName()).isEqualTo(applicationVersion.getCachedPrimaryOperatorName());
  }
}