package uk.co.nstauthority.fieldconsents.application.rationale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1Json;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1Json;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetView;
import uk.co.nstauthority.fieldconsents.application.rationale.flare.ApplicationRationaleFlareForm;
import uk.co.nstauthority.fieldconsents.assets.AssetKey;
import uk.co.nstauthority.fieldconsents.assets.AssetService;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.fds.searchselector.RestSearchItem;

@ExtendWith(MockitoExtension.class)
class ApplicationRationaleFormServiceTest {

  @Mock
  private AssetService assetService;

  @InjectMocks
  private ApplicationRationaleFormService applicationRationaleFormService;

  @Test
  void getFlaringLocationsFromForm_containsInvalid() {
    var flaringLocations = List.of("1FIELD", "2T");

    var form = new ApplicationRationaleFlareForm(
        null,
        null,
        null,
        flaringLocations,
        null
    );

    doReturn(Optional.of(field1Json)).when(assetService).getAsset(eq(new AssetKey(1, AssetType.FIELD)), anyString());

    assertThat(applicationRationaleFormService.getFlaringLocationsFromForm(form))
        .containsExactly(ApplicationAssetView.from(field1Json));
  }

  @Test
  void getFlaringLocationsFromForm_allValid() {
    var flaringLocations = List.of("1FIELD", "1TERMINAL");

    var form = new ApplicationRationaleFlareForm(
        null,
        null,
        null,
        flaringLocations,
        null
    );

    doReturn(Optional.of(field1Json)).when(assetService).getAsset(eq(new AssetKey(1, AssetType.FIELD)), anyString());
    doReturn(Optional.of(terminal1Json)).when(assetService).getAsset(eq(new AssetKey(1, AssetType.TERMINAL)), anyString());

    assertThat(applicationRationaleFormService.getFlaringLocationsFromForm(form))
        .containsExactly(
            ApplicationAssetView.from(field1Json),
            ApplicationAssetView.from(terminal1Json)
        );
  }


  @Test
  void getFlaringLocationsFromForm_allValid_butAssetNotFound() {
    var flaringLocations = List.of("1FIELD", "1TERMINAL");

    var form = new ApplicationRationaleFlareForm(
        null,
        null,
        null,
        flaringLocations,
        null
    );

    doReturn(Optional.of(field1Json)).when(assetService).getAsset(eq(new AssetKey(1, AssetType.FIELD)), anyString());
    doReturn(Optional.empty()).when(assetService).getAsset(eq(new AssetKey(1, AssetType.TERMINAL)), anyString());

    assertThat(applicationRationaleFormService.getFlaringLocationsFromForm(form))
        .containsExactly(ApplicationAssetView.from(field1Json));
  }

  @ParameterizedTest
  @ValueSource(strings = {"1T", "1F", "", " "})
  @NullSource
  void getHostLocationFormForm_invalidAssetKey(String hostLocationAssetKey) {
    var form = new ApplicationRationaleFlareForm(
        null,
        null,
        null,
        null,
        hostLocationAssetKey
    );

    assertThat(applicationRationaleFormService.getHostLocationFormForm(form))
        .isEqualTo(RestSearchItem.EMPTY_REST_SEARCH_ITEM);
  }

  @Test
  void getHostLocationFormForm_validKey() {
    var form = new ApplicationRationaleFlareForm(
        null,
        null,
        null,
        null,
        "1FIELD"
    );

    doReturn(Optional.of(field1Json)).when(assetService).getAsset(eq(new AssetKey(1, AssetType.FIELD)), anyString());

    assertThat(applicationRationaleFormService.getHostLocationFormForm(form)).isEqualTo(RestSearchItem.from(field1Json));
  }

  @Test
  void getHostLocationFormForm_validKey_butNoAsset() {
    var form = new ApplicationRationaleFlareForm(
        null,
        null,
        null,
        null,
        "1FIELD"
    );

    doReturn(Optional.empty()).when(assetService).getAsset(eq(new AssetKey(1, AssetType.FIELD)), anyString());

    assertThat(applicationRationaleFormService.getHostLocationFormForm(form)).isEqualTo(RestSearchItem.EMPTY_REST_SEARCH_ITEM);
  }
}
