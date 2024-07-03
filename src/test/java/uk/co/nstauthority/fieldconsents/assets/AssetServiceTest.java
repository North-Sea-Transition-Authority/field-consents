package uk.co.nstauthority.fieldconsents.assets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.assets.AssetTestUtil.BAD_ASSET_KEY;
import static uk.co.nstauthority.fieldconsents.assets.AssetTestUtil.FIELD1_ASSET_KEY;
import static uk.co.nstauthority.fieldconsents.assets.AssetTestUtil.TERMINAL1_ASSET_KEY;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.FIELD_ID_1;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1Json;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1JsonWithOperatorAndLicences;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1Json;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1JsonWithOperator;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal2JsonWithOperator;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import uk.co.fivium.energyportalapi.generated.types.Field;
import uk.co.fivium.energyportalapi.generated.types.FieldGeographicArea;
import uk.co.fivium.energyportalapi.generated.types.FieldShore;
import uk.co.fivium.energyportalapi.generated.types.FieldStatus;
import uk.co.fivium.energyportalapi.generated.types.Licence;
import uk.co.fivium.energyportalapi.generated.types.OrganisationUnit;
import uk.co.fivium.energyportalapi.generated.types.Terminal;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldWithOperatorAndLicencesJson;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalService;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalWithOperatorJson;

@ExtendWith(MockitoExtension.class)
public class AssetServiceTest {

  private static final String FIELD_LOOKUP_PURPOSE = "Determining whether an application can be started for this field";
  private static final String TERMINAL_LOOKUP_PURPOSE = "Determining whether an application can be started for this terminal";
  private static final int FIELD_ID = 1;
  private static final int TERMINAL_ID = 2;
  private static final int OPERATOR_OU_ID = 3;
  private static final AssetKey FIELD_ASSET_KEY = new AssetKey(FIELD_ID, AssetType.FIELD);
  private static final AssetKey TERMINAL_ASSET_KEY = new AssetKey(TERMINAL_ID, AssetType.TERMINAL);

  @Mock
  private FieldService fieldService;

  @Mock
  private TerminalService terminalService;

  @Spy
  @InjectMocks
  private AssetService assetService;

  @Test
  void getAssetFromKey_nullAssetKey() {

    Optional<AssetJson> assetJson = assetService.getAssetFromKey(null);
    assertThat(assetJson).isEqualTo(Optional.empty());

  }

  @Test
  void getAssetFromKey_field() {
    when(fieldService.findField(field1Json.getId(), "Field asset picked from search selector"))
        .thenReturn(Optional.of(field1Json));

    Optional<AssetJson> assetJson = assetService.getAssetFromKey(FIELD1_ASSET_KEY);
    assertThat(assetJson).isEqualTo(Optional.of(field1Json));
  }

  @Test
  void getAssetFromKey_terminal() {
    when(terminalService.findTerminal(terminal1Json.getId(), "Terminal asset picked from search selector"))
        .thenReturn(Optional.of(terminal1Json));

    Optional<AssetJson> assetJson = assetService.getAssetFromKey(TERMINAL1_ASSET_KEY);
    assertThat(assetJson).isEqualTo(Optional.of(terminal1Json));
  }

  @Test
  void getAssetFromKey_invalidKey_thenException() {

    assertThatThrownBy(() -> assetService.getAssetFromKey(BAD_ASSET_KEY))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Not a valid AssetKey: " + BAD_ASSET_KEY);

  }

  @Test
  void getAssetOld_validKey() {
    when(fieldService.findField(FIELD_ID_1, "Field asset picked from search selector"))
        .thenReturn(Optional.of(field1Json));

    AssetJson assetJson = assetService.getAsset(FIELD1_ASSET_KEY);
    assertThat(assetJson).isEqualTo(field1Json);
  }

  @Test
  void getAssetOld_invalidKey_thenException() {

    assertThatThrownBy(() -> assetService.getAsset(BAD_ASSET_KEY))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Not a valid AssetKey: " + BAD_ASSET_KEY);

  }

  @Test
  void getAsset_field() {
    var assetKey = new AssetKey(1, AssetType.FIELD);
    var requestPurpose = "request purpose";

    when(fieldService.findField(assetKey.assetId(), requestPurpose)).thenReturn(Optional.of(field1Json));

    assertThat(assetService.getAsset(assetKey, requestPurpose))
        .isPresent()
        .get()
        .isEqualTo(field1Json);
  }

  @Test
  void getAsset_terminal() {
    var assetKey = new AssetKey(1, AssetType.TERMINAL);
    var requestPurpose = "request purpose";

    when(terminalService.findTerminal(assetKey.assetId(), requestPurpose)).thenReturn(Optional.of(terminal2JsonWithOperator));

    assertThat(assetService.getAsset(assetKey, requestPurpose))
        .isPresent()
        .get()
        .isEqualTo(terminal2JsonWithOperator);
  }

  @Test
  void getAsset_nullAssetKey() {
    assertThat(assetService.getAsset(null, "request purpose")).isEmpty();
  }

  @Test
  void throwForbiddenStatusExceptionIfCannotStartApplicationForAsset_field() {
    when(fieldService.getFieldWithOperatorAndLicences(FIELD_ID, FIELD_LOOKUP_PURPOSE)).thenReturn(field1JsonWithOperatorAndLicences);

    doReturn(new StartApplicationDecision(List.of()))
        .when(assetService)
        .getStartApplicationDecision(field1JsonWithOperatorAndLicences);

    assertDoesNotThrow(() ->
        assetService.throwForbiddenStatusExceptionIfCannotStartApplicationForAsset(FIELD_ASSET_KEY)
    );
  }

  @Test
  void throwForbiddenStatusExceptionIfCannotStartApplicationForAsset_field_cannotStartApplication() {
    when(fieldService.getFieldWithOperatorAndLicences(FIELD_ID, FIELD_LOOKUP_PURPOSE)).thenReturn(field1JsonWithOperatorAndLicences);

    doReturn(new StartApplicationDecision(List.of("invalid field status")))
        .when(assetService)
        .getStartApplicationDecision(field1JsonWithOperatorAndLicences);

    assertThatThrownBy(() ->
        assetService.throwForbiddenStatusExceptionIfCannotStartApplicationForAsset(FIELD_ASSET_KEY)
    )
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("Cannot start application for field [%d]".formatted(FIELD_ID))
        .asInstanceOf(type(ResponseStatusException.class))
        .matches(e -> e.getStatusCode().equals(HttpStatus.FORBIDDEN));
  }

  @Test
  void getStartApplicationDecision_field() {
    var field = Field.newBuilder()
        .fieldId(FIELD_ID)
        .fieldName("Field 1")
        .fieldOperator(OrganisationUnit.newBuilder()
            .organisationUnitId(OPERATOR_OU_ID)
            .build())
        .licences(List.of(
            Licence.newBuilder()
                .id(1)
                .licenceRef("L1")
                .build()
        ))
        .status(FieldStatus.STATUS500)
        .shore(FieldShore.OFFSHORE)
        .geographicArea(FieldGeographicArea.CNS)
        .build();

    var fieldJson = FieldWithOperatorAndLicencesJson.from(field);
    assertThat(assetService.getStartApplicationDecision(fieldJson))
        .isEqualTo(new StartApplicationDecision(List.of()));
  }

  @Test
  void getStartApplicationDecision_field_noLicences_null() {
    var field = Field.newBuilder()
        .fieldId(FIELD_ID)
        .fieldName("Field 1")
        .fieldOperator(OrganisationUnit.newBuilder()
            .organisationUnitId(OPERATOR_OU_ID)
            .build())
        .status(FieldStatus.STATUS500)
        .shore(FieldShore.OFFSHORE)
        .geographicArea(FieldGeographicArea.CNS)
        .build();

    var fieldJson = FieldWithOperatorAndLicencesJson.from(field);
    assertThat(assetService.getStartApplicationDecision(fieldJson))
        .isEqualTo(new StartApplicationDecision(List.of(
            "There are no licences associated to this field"
        )));
  }

  @Test
  void getStartApplicationDecision_field_noLicences_empty() {
    var field = Field.newBuilder()
        .fieldId(FIELD_ID)
        .fieldName("Field 1")
        .fieldOperator(OrganisationUnit.newBuilder()
            .organisationUnitId(OPERATOR_OU_ID)
            .build())
        .licences(List.of())
        .status(FieldStatus.STATUS500)
        .shore(FieldShore.OFFSHORE)
        .geographicArea(FieldGeographicArea.CNS)
        .build();

    var fieldJson = FieldWithOperatorAndLicencesJson.from(field);
    assertThat(assetService.getStartApplicationDecision(fieldJson))
        .isEqualTo(new StartApplicationDecision(List.of(
            "There are no licences associated to this field"
        )));
  }

  @Test
  void getStartApplicationDecision_field_fieldInNonProducingStatus() {
    var field = Field.newBuilder()
        .fieldId(FIELD_ID)
        .fieldName("Field 1")
        .fieldOperator(OrganisationUnit.newBuilder()
            .organisationUnitId(OPERATOR_OU_ID)
            .build())
        .licences(List.of(
            Licence.newBuilder()
                .id(1)
                .licenceRef("L1")
                .build()
        ))
        .status(FieldStatus.UNKNOWN)
        .shore(FieldShore.OFFSHORE)
        .geographicArea(FieldGeographicArea.CNS)
        .build();

    var fieldJson = FieldWithOperatorAndLicencesJson.from(field);
    assertThat(assetService.getStartApplicationDecision(fieldJson))
        .isEqualTo(new StartApplicationDecision(List.of(
            "The field is not in a valid 'producing' status"
        )));
  }

  @Test
  void throwForbiddenStatusExceptionIfCannotStartApplicationForAsset_terminal() {
    when(terminalService.getTerminalWithOperator(TERMINAL_ID, TERMINAL_LOOKUP_PURPOSE)).thenReturn(terminal1JsonWithOperator);

    doReturn(new StartApplicationDecision(List.of()))
        .when(assetService)
        .getStartApplicationDecision(terminal1JsonWithOperator);

    assertDoesNotThrow(() ->
        assetService.throwForbiddenStatusExceptionIfCannotStartApplicationForAsset(TERMINAL_ASSET_KEY)
    );
  }

  @Test
  void throwForbiddenStatusExceptionIfCannotStartApplicationForAsset_terminal_cannotStartApplication() {
    when(terminalService.getTerminalWithOperator(TERMINAL_ID, TERMINAL_LOOKUP_PURPOSE)).thenReturn(terminal1JsonWithOperator);

    doReturn(new StartApplicationDecision(List.of("invalid field status")))
        .when(assetService)
        .getStartApplicationDecision(terminal1JsonWithOperator);

    assertThatThrownBy(() ->
        assetService.throwForbiddenStatusExceptionIfCannotStartApplicationForAsset(TERMINAL_ASSET_KEY)
    )
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("Cannot start application for facility [%d]".formatted(TERMINAL_ID))
        .asInstanceOf(type(ResponseStatusException.class))
        .matches(e -> e.getStatusCode().equals(HttpStatus.FORBIDDEN));
  }

  @Test
  void getStartApplicationDecision_terminal() {
    var terminal = Terminal.newBuilder()
        .terminalId(TERMINAL_ID)
        .terminalName("Terminal 1")
        .terminalOperator(OrganisationUnit.newBuilder()
            .organisationUnitId(OPERATOR_OU_ID)
            .build())
        .terminalActive(true)
        .build();

    var terminalJson = TerminalWithOperatorJson.from(terminal);
    assertThat(assetService.getStartApplicationDecision(terminalJson))
        .isEqualTo(new StartApplicationDecision(List.of()));
  }

  @Test
  void getStartApplicationDecision_terminal_notActive() {
    var terminal = Terminal.newBuilder()
        .terminalId(TERMINAL_ID)
        .terminalName("Terminal 1")
        .terminalOperator(OrganisationUnit.newBuilder()
            .organisationUnitId(OPERATOR_OU_ID)
            .build())
        .terminalActive(false)
        .build();

    var terminalJson = TerminalWithOperatorJson.from(terminal);
    assertThat(assetService.getStartApplicationDecision(terminalJson))
        .isEqualTo(new StartApplicationDecision(List.of(
            "This facility is inactive"
        )));
  }

  @Test
  void getStartApplicationDecision_terminal_nullActive() {
    var terminal = Terminal.newBuilder()
        .terminalId(TERMINAL_ID)
        .terminalName("Terminal 1")
        .terminalOperator(OrganisationUnit.newBuilder()
            .organisationUnitId(OPERATOR_OU_ID)
            .build())
        .build();

    var terminalJson = TerminalWithOperatorJson.from(terminal);
    assertThat(assetService.getStartApplicationDecision(terminalJson))
        .isEqualTo(new StartApplicationDecision(List.of(
            "This facility is inactive"
        )));
  }

}
