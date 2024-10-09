package uk.co.nstauthority.fieldconsents.application.rationale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1Json;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field2Json;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAsset;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.assets.AssetRole;
import uk.co.nstauthority.fieldconsents.assets.AssetJson;
import uk.co.nstauthority.fieldconsents.assets.AssetRestController;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.assets.fields.Shore;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

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

  @ParameterizedTest
  @EnumSource(value = AssetRole.class, names = "PRIMARY", mode= Mode.EXCLUDE)
  void getAssetSearchUrl_nonPrimaryAsset(AssetRole assetRole) {
    var applicationAsset = new ApplicationAsset();
    applicationAsset.setAssetRole(assetRole);

    assertThatThrownBy(() -> applicationRationaleService.getAssetSearchUrl(applicationAsset))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Application asset must be primary. Was %s", assetRole);
  }

  @ParameterizedTest
  @MethodSource("getAssetSearchUrl_field")
  void getAssetSearchUrl_field(AssetType assetType, Shore shore, String expectedSearchUrl) {
    var applicationAsset = new ApplicationAsset();
    applicationAsset.setAssetRole(AssetRole.PRIMARY);
    applicationAsset.setAssetType(assetType);

    when(applicationAssetService.getShore(applicationAsset)).thenReturn(shore);

    assertThat(applicationRationaleService.getAssetSearchUrl(applicationAsset)).isEqualTo(expectedSearchUrl);
  }

  private static Stream<Arguments> getAssetSearchUrl_field() {
    return Stream.of(
        arguments(
            AssetType.FIELD,
            Shore.ONSHORE,
            ReverseRouter.route(on(AssetRestController.class).searchFieldsAndTerminals(null))
        ),
        arguments(
            AssetType.FIELD,
            Shore.UNKNOWN,
            ReverseRouter.route(on(AssetRestController.class).searchFieldsAndTerminals(null))
        ),
        arguments(
            AssetType.FIELD,
            Shore.OFFSHORE,
            ReverseRouter.route(on(AssetRestController.class).searchFacilityAndHubAssets(null))
        )
    );
  }

  @Test
  void getAssetSearchUrl_terminal() {
    var applicationAsset = new ApplicationAsset();
    applicationAsset.setAssetRole(AssetRole.PRIMARY);
    applicationAsset.setAssetType(AssetType.TERMINAL);

    assertThat(applicationRationaleService.getAssetSearchUrl(applicationAsset))
        .isEqualTo(ReverseRouter.route(on(AssetRestController.class).searchTerminalAssets(null)));
  }

}
