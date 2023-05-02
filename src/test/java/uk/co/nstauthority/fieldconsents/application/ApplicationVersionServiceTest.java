package uk.co.nstauthority.fieldconsents.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_ID;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_VERSION_ID;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ApplicationVersionServiceTest {

  @Mock
  private ApplicationVersionRepository applicationVersionRepository;

  @InjectMocks
  private ApplicationVersionService applicationVersionService;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setup() {
    applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
  }

  @Test
  void getApplicationVersionById_whenApplicationVersionExists() {
    when(applicationVersionRepository.findById(APPLICATION_VERSION_ID)).thenReturn(Optional.of(applicationVersion));

    assertThat(applicationVersionService.getApplicationVersionById(APPLICATION_VERSION_ID))
        .isEqualTo(applicationVersion);
  }

  @Test
  void getApplicationVersionById_whenApplicationVersionIsNotFound() {
    when(applicationVersionRepository.findById(APPLICATION_VERSION_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> applicationVersionService.getApplicationVersionById(APPLICATION_VERSION_ID))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("Application version with id %s not found".formatted(APPLICATION_VERSION_ID));
  }

  @Test
  void getLatestApplicationVersionByApplicationId_whenOneApplicationVersionExists() {
    when(applicationVersionRepository.findAllByApplicationIdOrderByVersion(APPLICATION_ID))
        .thenReturn(List.of(applicationVersion));

    assertThat(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .isEqualTo(applicationVersion);
  }

  @Test
  void getLatestApplicationVersionByApplicationId_whenManyApplicationVersionsExists() {
    var applicationVersion2 = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
    applicationVersion2.setVersion(2);
    var applicationVersion3 = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
    applicationVersion3.setVersion(3);

    when(applicationVersionRepository.findAllByApplicationIdOrderByVersion(APPLICATION_ID))
        .thenReturn(List.of(applicationVersion3, applicationVersion, applicationVersion2));

    assertThat(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .isEqualTo(applicationVersion3);
  }

  @Test
  void getLatestApplicationVersionByApplicationId_whenApplicationVersionsNotFound() {
    when(applicationVersionRepository.findAllByApplicationIdOrderByVersion(APPLICATION_ID))
        .thenReturn(Collections.emptyList());

    assertThatThrownBy(() -> applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("Application version not found for application with id %s".formatted(APPLICATION_ID));
  }

  @Test
  void findLatestApplicationVersion_whenOneApplicationVersionExists() {
    when(applicationVersionRepository.findAllByApplicationIdOrderByVersion(APPLICATION_ID))
        .thenReturn(List.of(applicationVersion));

    assertThat(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .isEqualTo(Optional.of(applicationVersion));
  }

  @Test
  void findLatestApplicationVersion_whenManyApplicationVersionsExists() {
    var applicationVersion2 = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
    applicationVersion2.setVersion(2);
    var applicationVersion3 = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
    applicationVersion3.setVersion(3);

    when(applicationVersionRepository.findAllByApplicationIdOrderByVersion(APPLICATION_ID))
        .thenReturn(List.of(applicationVersion3, applicationVersion, applicationVersion2));

    assertThat(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .isEqualTo(Optional.of(applicationVersion3));
  }

  @Test
  void findLatestApplicationVersion_whenApplicationVersionsNotFound() {
    when(applicationVersionRepository.findAllByApplicationIdOrderByVersion(APPLICATION_ID))
        .thenReturn(Collections.emptyList());

    assertThat(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .isNotPresent();
  }
}