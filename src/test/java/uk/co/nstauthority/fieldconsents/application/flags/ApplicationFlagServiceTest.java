package uk.co.nstauthority.fieldconsents.application.flags;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

@ExtendWith(MockitoExtension.class)
class ApplicationFlagServiceTest {

  @Mock
  private ApplicationFlagRepository applicationFlagRepository;

  private ApplicationFlagService applicationFlagService;

  private ApplicationVersion applicationVersion;

  private ApplicationFlag secondaryAssetsFlag;


  @BeforeEach
  void setUp() {
    applicationFlagService = new ApplicationFlagService(applicationFlagRepository);
    applicationVersion = ApplicationTestUtil.getApplicationVersionWithType(ApplicationType.VENT);
    secondaryAssetsFlag = new ApplicationFlag(1, applicationVersion, ApplicationFlagType.HAS_SECONDARY_ASSETS, true);
  }

  @Test
  void findFlagValue_hasSecondaryAssets_notFound() {
    when(applicationFlagRepository
        .findByApplicationVersionAndFlagType(applicationVersion, ApplicationFlagType.HAS_SECONDARY_ASSETS))
        .thenReturn(Optional.empty());
    assertThat(applicationFlagService.findFlagValue(applicationVersion, ApplicationFlagType.HAS_SECONDARY_ASSETS))
        .isNotPresent();
  }

  @Test
  void findFlagValue_hasSecondaryAssets_isPresent() {
    when(applicationFlagRepository
        .findByApplicationVersionAndFlagType(applicationVersion, ApplicationFlagType.HAS_SECONDARY_ASSETS))
        .thenReturn(Optional.of(secondaryAssetsFlag));
    assertThat(applicationFlagService.findFlagValue(applicationVersion, ApplicationFlagType.HAS_SECONDARY_ASSETS))
        .isPresent();
  }

  @Test
  void saveApplicationFlag_hasSecondaryAssets() {
    applicationFlagService.saveApplicationFlag(applicationVersion, ApplicationFlagType.HAS_SECONDARY_ASSETS, true);

    ArgumentCaptor<ApplicationFlag> flagArgumentCaptor = ArgumentCaptor.forClass(ApplicationFlag.class);
    verify(applicationFlagRepository, times(1)).save(flagArgumentCaptor.capture());

    ApplicationFlag actualFlag = flagArgumentCaptor.getValue();

    assertThat(actualFlag.getFlagType()).isEqualTo(ApplicationFlagType.HAS_SECONDARY_ASSETS);
    assertThat(actualFlag.getFlagValue()).isTrue();

    ApplicationVersion actualVersion = actualFlag.getApplicationVersion();
    assertThat(actualVersion.getId()).isEqualTo(applicationVersion.getId());
    assertThat(actualVersion.getVersion()).isEqualTo(applicationVersion.getVersion());
    assertThat(actualVersion.getApplication().getType()).isEqualTo(applicationVersion.getApplication().getType());
  }
}