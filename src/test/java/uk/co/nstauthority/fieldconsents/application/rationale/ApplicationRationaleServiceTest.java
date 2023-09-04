package uk.co.nstauthority.fieldconsents.application.rationale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1Json;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field2Json;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.assets.AssetRole;
import uk.co.nstauthority.fieldconsents.assets.AssetJson;

@ExtendWith(MockitoExtension.class)
class ApplicationRationaleServiceTest {

  @Mock
  private ApplicationRationaleRepository repository;

  @Mock
  private ApplicationAssetService applicationAssetService;

  @InjectMocks
  private ApplicationRationaleService applicationRationaleService;

  private ApplicationVersion applicationVersion;

  private ApplicationRationale applicationRationale;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.FLARE);

    applicationRationale = new ApplicationRationale();
    applicationRationale.setApplicationVersion(applicationVersion);
    applicationRationale.setId(1);
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void doesApplicationRationaleExistFor(boolean exists) {
    when(repository.existsApplicationRationaleByApplicationVersion(applicationVersion)).thenReturn(exists);
    assertThat(applicationRationaleService.doesApplicationRationaleExistFor(applicationVersion)).isEqualTo(exists);
  }

  @Test
  void findByApplicationVersion() {
    when(repository.findByApplicationVersion(applicationVersion)).thenReturn(Optional.of(applicationRationale));
    assertThat(applicationRationaleService.findByApplicationVersion(applicationVersion))
        .isPresent()
        .get()
        .isEqualTo(applicationRationale);
  }

  @Test
  void getNonHostLocations() {
    var nonHostAssets = List.<AssetJson>of(field1Json, field2Json);

    when(applicationAssetService.getAssetJsonListFor(applicationVersion, AssetRole.LOCATION)).thenReturn(nonHostAssets);

    assertThat(applicationRationaleService.getLocations(applicationVersion)).isEqualTo(nonHostAssets);
  }

  @Test
  void getNonHostLocations_noneFound() {
    when(applicationAssetService.getAssetJsonListFor(applicationVersion, AssetRole.LOCATION)).thenReturn(Collections.emptyList());
    assertThat(applicationRationaleService.getLocations(applicationVersion)).isEmpty();
  }

  @Test
  void getHostLocation() {
    when(applicationAssetService.getAssetJsonListFor(applicationVersion, AssetRole.HOST)).thenReturn(Collections.singletonList(field1Json));
    assertThat(applicationRationaleService.getHostLocation(applicationVersion)).isPresent().get().isEqualTo(field1Json);
  }

  @Test
  void getHostLocation_noneFound() {
    when(applicationAssetService.getAssetJsonListFor(applicationVersion, AssetRole.HOST)).thenReturn(Collections.emptyList());
    assertThat(applicationRationaleService.getHostLocation(applicationVersion)).isEmpty();
  }
}
