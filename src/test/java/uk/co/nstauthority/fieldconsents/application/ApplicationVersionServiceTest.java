package uk.co.nstauthority.fieldconsents.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_ID;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_VERSION_ID;

import jakarta.persistence.EntityNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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
  void getLatestApplicationVersionByApplicationId_whenManyApplicationVersionsExistsSomeDeleted() {
    var applicationVersion2 = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
    applicationVersion2.setVersion(2);
    applicationVersion2.setStatus(ApplicationVersionStatus.DELETED);
    var applicationVersion2i = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
    applicationVersion2i.setVersion(2);
    var applicationVersion3 = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
    applicationVersion3.setVersion(3);
    applicationVersion3.setStatus(ApplicationVersionStatus.DELETED);
    var applicationVersion3i = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
    applicationVersion3i.setVersion(3);

    when(applicationVersionRepository.findAllByApplicationIdOrderByVersion(APPLICATION_ID))
        .thenReturn(List.of(applicationVersion3, applicationVersion, applicationVersion2i, applicationVersion2, applicationVersion3i));

    assertThat(applicationVersionService.getLatestApplicationVersionByApplicationId(APPLICATION_ID))
        .isEqualTo(applicationVersion3i);
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

  @Test
  void deleteApplicationVersion_whenCalled_thenVerifyEntityUpdatedAndSaved() {
    applicationVersionService.deleteApplicationVersion(applicationVersion);

    assertThat(applicationVersion.getStatus()).isEqualTo(ApplicationVersionStatus.DELETED);
    verify(applicationVersionRepository).save(applicationVersion);
  }

  @ParameterizedTest
  @EnumSource(ApplicationVersionStatus.class)
  void deleteApplicationVersion_ensureOnlyDraftCanBeDeleted(ApplicationVersionStatus applicationVersionStatus) {
    if (applicationVersionStatus == ApplicationVersionStatus.IN_PROGRESS) {
      applicationVersionService.deleteApplicationVersion(applicationVersion);
      verify(applicationVersionRepository).save(applicationVersion);
    } else {
      applicationVersion.setStatus(applicationVersionStatus);

      assertThrows(IllegalStateException.class,
          () -> applicationVersionService.deleteApplicationVersion(applicationVersion));
      verify(applicationVersionRepository, never()).save(applicationVersion);
    }
  }

  @Test
  void withdrawApplicationVersion_whenCalled_thenVerifyEntityUpdatedAndSaved() {
    var applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    applicationVersionService.withdrawApplicationVersion(applicationVersion);

    assertThat(applicationVersion.getStatus()).isEqualTo(ApplicationVersionStatus.WITHDRAWN);
    verify(applicationVersionRepository).save(applicationVersion);
  }

  @ParameterizedTest
  @EnumSource(ApplicationVersionStatus.class)
  void withdrawApplicationVersion_ensureOnlySubmittedApplicationsCanBeWithdrawn(ApplicationVersionStatus applicationVersionStatus) {
    var applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    if (applicationVersionStatus == ApplicationVersionStatus.SUBMITTED) {
      applicationVersionService.withdrawApplicationVersion(applicationVersion);
      verify(applicationVersionRepository).save(applicationVersion);
    } else {
      applicationVersion.setStatus(applicationVersionStatus);

      assertThrows(IllegalStateException.class,
          () -> applicationVersionService.withdrawApplicationVersion(applicationVersion));
      verify(applicationVersionRepository, never()).save(applicationVersion);
    }
  }
}
