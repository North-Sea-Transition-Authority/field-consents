package uk.co.nstauthority.fieldconsents.application.assets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetTestUtil.fieldAsset1;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ApplicationFieldServiceTest {

  @Mock
  private ApplicationAssetService applicationAssetService;

  private ApplicationFieldService applicationFieldService;

  @BeforeEach
  void setUp() {
    applicationFieldService = new ApplicationFieldService(applicationAssetService);
  }

  @Test
  void findDistinctPrimaryFieldIds_whenEmpty() {
    when(applicationAssetService.findAllPrimaryFieldAssets()).thenReturn(Collections.emptyList());

    assertThat(applicationFieldService.findDistinctPrimaryFieldIds()).isEmpty();
  }

  @Test
  void findDistinctPrimaryFieldIds_whenNotEmpty() {
    when(applicationAssetService.findAllPrimaryFieldAssets()).thenReturn(List.of(fieldAsset1, fieldAsset1));

    var primaryFields = applicationFieldService.findDistinctPrimaryFieldIds();
    assertThat(primaryFields).containsExactly(fieldAsset1.getAssetId());
  }
}
