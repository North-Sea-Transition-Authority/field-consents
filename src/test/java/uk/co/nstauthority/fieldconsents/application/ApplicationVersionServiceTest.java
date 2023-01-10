package uk.co.nstauthority.fieldconsents.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import javax.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
            1, ApplicationTestUtil.PRIMARY_OPERATOR_OU_ID, ApplicationTestUtil.CACHED_PRIMARY_OPERATOR_NAME);
    when(applicationVersionRepository.findById(1)).thenReturn(Optional.of(applicationVersion));

    ApplicationVersion expectedApplicationVersion = applicationVersionService.getApplicationVersionById(1);

    assertThat(expectedApplicationVersion.getId()).isEqualTo(applicationVersion.getId());

  }

  @Test
  void getApplicationVersionById_whenApplicationVersionIsNotFound() {
    when(applicationVersionRepository.findById(any(Integer.class))).thenReturn(Optional.empty());

    var exception = Assertions.assertThrows(EntityNotFoundException.class, () ->
        applicationVersionService.getApplicationVersionById(1));

    Assertions.assertEquals("Application version with id 1 not found", exception.getMessage());
  }
}