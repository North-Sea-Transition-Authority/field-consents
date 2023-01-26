package uk.co.nstauthority.fieldconsents.application.assets;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1JsonWithOperatorAndLicences;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.assetlicences.ApplicationAssetLicenceService;

@ExtendWith(MockitoExtension.class)
class AdditionalAssetsServiceTest {

  @Mock
  private ApplicationAssetService applicationAssetService;

  @Mock
  private ApplicationAssetLicenceService applicationAssetLicenceService;

  private AdditionalAssetsService additionalAssetsService;

  private ApplicationVersion applicationVersion;

  private ApplicationAsset applicationAsset;

  @BeforeEach
  void setUp() {
    additionalAssetsService = new AdditionalAssetsService(
        applicationAssetService,
        applicationAssetLicenceService);
    applicationVersion = ApplicationTestUtil.getApplicationVersionWithType(ApplicationType.FLARE);
    applicationAsset = ApplicationAssetTestUtil.fieldAsset2;
  }

  @Test
  void saveAdditionalAsset() {
    when(applicationAssetService.createSecondaryAsset(applicationVersion, field1JsonWithOperatorAndLicences))
        .thenReturn(applicationAsset);

    additionalAssetsService.saveAdditionalAsset(applicationVersion, field1JsonWithOperatorAndLicences);

    verify(applicationAssetLicenceService, times(1))
        .createAssetLicences(applicationAsset, field1JsonWithOperatorAndLicences);
  }

  @Test
  void deleteAdditionalAsset() {
    additionalAssetsService.deleteAdditionalAsset(applicationAsset);

    verify(applicationAssetLicenceService, times(1)).deleteAssetLicences(applicationAsset);
    verify(applicationAssetService, times(1)).deleteSecondaryAsset(applicationAsset);
  }
}
