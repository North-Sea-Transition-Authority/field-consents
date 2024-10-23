package uk.co.nstauthority.fieldconsents.assets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.assets.AssetTestUtil.field1AssetJson;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldService.FIELD_STATUSES_ALLOWED_VALIDATION_MESSAGE;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.FIELD_ID_1;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1Json;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1JsonWithOperatorAndLicences;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalService.TERMINAL_INACTIVE_VALIDATION_MESSAGE;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1Json;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1JsonWithOperator;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal2JsonWithOperator;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitPermissionService;
import uk.co.nstauthority.fieldconsents.teams.TeamService;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@ExtendWith(MockitoExtension.class)
class AssetServiceTest {

  private static final String FIELD_LOOKUP_PURPOSE = "Determining whether an application can be started for this field";
  private static final String TERMINAL_LOOKUP_PURPOSE = "Determining whether an application can be started for this terminal";
  private static final String ASSET_LOOKUP_PURPOSE = "looking up asset for asset key";
  private static final int FIELD_ID = 1;
  private static final int TERMINAL_ID = 2;
  private static final int OPERATOR_OU_ID = 3;
  private static final ServiceUserDetail SERVICE_USER_DETAIL = ServiceUserDetailTestUtil.Builder().build();
  private static final AssetKey FIELD_ASSET_KEY = new AssetKey(FIELD_ID, AssetType.FIELD);
  private static final AssetKey TERMINAL_ASSET_KEY = new AssetKey(TERMINAL_ID, AssetType.TERMINAL);

  @Mock
  private FieldService fieldService;

  @Mock
  private TerminalService terminalService;

  @Mock
  private OrganisationUnitPermissionService organisationUnitPermissionService;

  @Mock
  private TeamService teamService;

  @Captor
  private ArgumentCaptor<Supplier<FieldWithOperatorAndLicencesJson>> fieldJsonSupplierCaptor;

  @Captor
  private ArgumentCaptor<Supplier<TerminalWithOperatorJson>> terminalJsonSupplierCaptor;

  @Spy
  @InjectMocks
  private AssetService assetService;

  @Test
  void getAssetFromKey_nullAssetKey() {

    Optional<AssetJson> assetJson = assetService.findAsset(null);
    assertThat(assetJson).isEmpty();

  }

  @Test
  void getAssetFromKey_field() {
    when(fieldService.findField(field1Json.getId(), ASSET_LOOKUP_PURPOSE))
        .thenReturn(Optional.of(field1Json));

    Optional<AssetJson> assetJson = assetService.findAsset(field1AssetJson.getAssetKey());
    assertThat(assetJson).isEqualTo(Optional.of(field1Json));
  }

  @Test
  void getAssetFromKey_terminal() {
    when(terminalService.findTerminal(terminal1Json.getId(), ASSET_LOOKUP_PURPOSE))
        .thenReturn(Optional.of(terminal1Json));

    Optional<AssetJson> assetJson = assetService.findAsset(terminal1Json.getAssetKey());
    assertThat(assetJson).isEqualTo(Optional.of(terminal1Json));
  }

  @Test
  void getAssetOld_validKey() {
    when(fieldService.findField(FIELD_ID_1, ASSET_LOOKUP_PURPOSE))
        .thenReturn(Optional.of(field1Json));

    AssetJson assetJson = assetService.getAsset(field1Json.getAssetKey());
    assertThat(assetJson).isEqualTo(field1Json);
  }

  @Test
  void getAsset_field() {
    var assetKey = new AssetKey(1, AssetType.FIELD);

    when(fieldService.findField(assetKey.assetId(), ASSET_LOOKUP_PURPOSE)).thenReturn(Optional.of(field1Json));

    assertThat(assetService.findAsset(assetKey))
        .isPresent()
        .get()
        .isEqualTo(field1Json);
  }

  @Test
  void getAsset_terminal() {
    var assetKey = new AssetKey(1, AssetType.TERMINAL);

    when(terminalService.findTerminal(assetKey.assetId(), ASSET_LOOKUP_PURPOSE)).thenReturn(Optional.of(terminal2JsonWithOperator));

    assertThat(assetService.findAsset(assetKey))
        .isPresent()
        .get()
        .isEqualTo(terminal2JsonWithOperator);
  }

  @Test
  void getAsset_nullAssetKey() {
    assertThat(assetService.findAsset(null)).isEmpty();
  }

  @Test
  void throwForbiddenStatusExceptionIfCannotStartApplicationForAsset_field() {
    when(fieldService.getFieldWithOperatorAndLicences(FIELD_ID, FIELD_LOOKUP_PURPOSE)).thenReturn(field1JsonWithOperatorAndLicences);

    doReturn(StartApplicationDecision.allowed())
        .when(assetService)
        .getStartApplicationDecisionForField(eq(SERVICE_USER_DETAIL), any());

    assertDoesNotThrow(() ->
        assetService.throwForbiddenStatusExceptionIfCannotStartApplicationForAsset(FIELD_ASSET_KEY, SERVICE_USER_DETAIL)
    );

    verify(assetService).getStartApplicationDecisionForField(eq(SERVICE_USER_DETAIL), fieldJsonSupplierCaptor.capture());
    assertThat(fieldJsonSupplierCaptor.getValue().get()).isEqualTo(field1JsonWithOperatorAndLicences);
  }

  @Test
  void throwForbiddenStatusExceptionIfCannotStartApplicationForAsset_field_cannotStartApplication() {
    when(fieldService.getFieldWithOperatorAndLicences(FIELD_ID, FIELD_LOOKUP_PURPOSE)).thenReturn(field1JsonWithOperatorAndLicences);

    doReturn(StartApplicationDecision.notAllowed(List.of("invalid field status")))
        .when(assetService)
        .getStartApplicationDecisionForField(eq(SERVICE_USER_DETAIL), any());

    assertThatThrownBy(() ->
        assetService.throwForbiddenStatusExceptionIfCannotStartApplicationForAsset(FIELD_ASSET_KEY, SERVICE_USER_DETAIL)
    )
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("Cannot start application for field [%d]".formatted(FIELD_ID))
        .asInstanceOf(type(ResponseStatusException.class))
        .matches(e -> e.getStatusCode().equals(HttpStatus.FORBIDDEN));

    verify(assetService).getStartApplicationDecisionForField(eq(SERVICE_USER_DETAIL), fieldJsonSupplierCaptor.capture());
    assertThat(fieldJsonSupplierCaptor.getValue().get()).isEqualTo(field1JsonWithOperatorAndLicences);
  }

  @Test
  @SuppressWarnings("unchecked")
  void getStartApplicationDecisionForField_notIndustryUser() {
    when(teamService.isIndustryUser(SERVICE_USER_DETAIL)).thenReturn(false);

    var mockFieldJsonSupplier = mock(Supplier.class);
    assertThat(assetService.getStartApplicationDecisionForField(SERVICE_USER_DETAIL, mockFieldJsonSupplier))
        .isEqualTo(StartApplicationDecision.notAllowed(List.of()));

    verifyNoInteractions(mockFieldJsonSupplier);
  }

  @Test
  void getStartApplicationDecisionForField() {
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

    when(teamService.isIndustryUser(SERVICE_USER_DETAIL)).thenReturn(true);
    when(organisationUnitPermissionService.hasOperatorPermission(SERVICE_USER_DETAIL, OPERATOR_OU_ID, RolePermission.CREATE_FCS_APPLICATIONS))
        .thenReturn(true);

    var fieldJson = FieldWithOperatorAndLicencesJson.from(field);
    assertThat(assetService.getStartApplicationDecisionForField(SERVICE_USER_DETAIL, () -> fieldJson))
        .isEqualTo(StartApplicationDecision.allowed());
  }

  @Test
  void getStartApplicationDecisionForField_noOperator() {
    var field = Field.newBuilder()
        .fieldId(FIELD_ID)
        .fieldName("Field 1")
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

    when(teamService.isIndustryUser(SERVICE_USER_DETAIL)).thenReturn(true);

    var fieldJson = FieldWithOperatorAndLicencesJson.from(field);
    assertThat(assetService.getStartApplicationDecisionForField(SERVICE_USER_DETAIL, () -> fieldJson))
        .isEqualTo(StartApplicationDecision.notAllowed(
            List.of("An operator does not exist for this field")
        ));
  }

  @Test
  void getStartApplicationDecisionForField_noPermission() {
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

    when(teamService.isIndustryUser(SERVICE_USER_DETAIL)).thenReturn(true);
    when(organisationUnitPermissionService.hasOperatorPermission(SERVICE_USER_DETAIL, OPERATOR_OU_ID, RolePermission.CREATE_FCS_APPLICATIONS))
        .thenReturn(false);

    var fieldJson = FieldWithOperatorAndLicencesJson.from(field);
    assertThat(assetService.getStartApplicationDecisionForField(SERVICE_USER_DETAIL, () -> fieldJson))
        .isEqualTo(StartApplicationDecision.notAllowed(
            List.of("You are missing permissions to create applications for this field")
        ));
  }

  @Test
  void getStartApplicationDecisionForField_noLicences_null() {
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

    when(teamService.isIndustryUser(SERVICE_USER_DETAIL)).thenReturn(true);
    when(organisationUnitPermissionService.hasOperatorPermission(SERVICE_USER_DETAIL, OPERATOR_OU_ID, RolePermission.CREATE_FCS_APPLICATIONS))
        .thenReturn(true);

    var fieldJson = FieldWithOperatorAndLicencesJson.from(field);
    assertThat(assetService.getStartApplicationDecisionForField(SERVICE_USER_DETAIL, () -> fieldJson))
        .isEqualTo(StartApplicationDecision.notAllowed(
            List.of("There are no licences associated to this field")
        ));
  }

  @Test
  void getStartApplicationDecisionForField_noLicences_empty() {
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

    when(teamService.isIndustryUser(SERVICE_USER_DETAIL)).thenReturn(true);
    when(organisationUnitPermissionService.hasOperatorPermission(SERVICE_USER_DETAIL, OPERATOR_OU_ID, RolePermission.CREATE_FCS_APPLICATIONS))
        .thenReturn(true);

    var fieldJson = FieldWithOperatorAndLicencesJson.from(field);
    assertThat(assetService.getStartApplicationDecisionForField(SERVICE_USER_DETAIL, () -> fieldJson))
        .isEqualTo(StartApplicationDecision.notAllowed(
            List.of("There are no licences associated to this field")
        ));
  }

  @Test
  void getStartApplicationDecisionForField_fieldInNonProducingStatus() {
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

    when(teamService.isIndustryUser(SERVICE_USER_DETAIL)).thenReturn(true);
    when(organisationUnitPermissionService.hasOperatorPermission(SERVICE_USER_DETAIL, OPERATOR_OU_ID, RolePermission.CREATE_FCS_APPLICATIONS))
        .thenReturn(true);

    var fieldJson = FieldWithOperatorAndLicencesJson.from(field);
    assertThat(assetService.getStartApplicationDecisionForField(SERVICE_USER_DETAIL, () -> fieldJson))
        .isEqualTo(StartApplicationDecision.notAllowed(
            List.of("The field %s".formatted(FIELD_STATUSES_ALLOWED_VALIDATION_MESSAGE))
        ));
  }

  @Test
  void throwForbiddenStatusExceptionIfCannotStartApplicationForAsset_terminal() {
    when(terminalService.getTerminalWithOperator(TERMINAL_ID, TERMINAL_LOOKUP_PURPOSE)).thenReturn(terminal1JsonWithOperator);

    doReturn(StartApplicationDecision.allowed())
        .when(assetService)
        .getStartApplicationDecisionForTerminal(eq(SERVICE_USER_DETAIL), any());

    assertDoesNotThrow(() ->
        assetService.throwForbiddenStatusExceptionIfCannotStartApplicationForAsset(TERMINAL_ASSET_KEY, SERVICE_USER_DETAIL)
    );

    verify(assetService).getStartApplicationDecisionForTerminal(eq(SERVICE_USER_DETAIL), terminalJsonSupplierCaptor.capture());
    assertThat(terminalJsonSupplierCaptor.getValue().get()).isEqualTo(terminal1JsonWithOperator);
  }

  @Test
  void throwForbiddenStatusExceptionIfCannotStartApplicationForAsset_terminal_cannotStartApplication() {
    when(terminalService.getTerminalWithOperator(TERMINAL_ID, TERMINAL_LOOKUP_PURPOSE)).thenReturn(terminal1JsonWithOperator);

    doReturn(StartApplicationDecision.notAllowed(List.of("invalid field status")))
        .when(assetService)
        .getStartApplicationDecisionForTerminal(eq(SERVICE_USER_DETAIL), any());

    assertThatThrownBy(() ->
        assetService.throwForbiddenStatusExceptionIfCannotStartApplicationForAsset(TERMINAL_ASSET_KEY, SERVICE_USER_DETAIL)
    )
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("Cannot start application for facility [%d]".formatted(TERMINAL_ID))
        .asInstanceOf(type(ResponseStatusException.class))
        .matches(e -> e.getStatusCode().equals(HttpStatus.FORBIDDEN));

    verify(assetService).getStartApplicationDecisionForTerminal(eq(SERVICE_USER_DETAIL), terminalJsonSupplierCaptor.capture());
    assertThat(terminalJsonSupplierCaptor.getValue().get()).isEqualTo(terminal1JsonWithOperator);
  }

  @Test
  @SuppressWarnings("unchecked")
  void getStartApplicationDecisionForTerminal_notIndustryUser() {
    when(teamService.isIndustryUser(SERVICE_USER_DETAIL)).thenReturn(false);

    var mockTerminalJsonSupplier = mock(Supplier.class);
    assertThat(assetService.getStartApplicationDecisionForTerminal(SERVICE_USER_DETAIL, mockTerminalJsonSupplier))
        .isEqualTo(StartApplicationDecision.notAllowed(List.of()));

    verifyNoInteractions(mockTerminalJsonSupplier);
  }

  @Test
  void getStartApplicationDecisionForTerminal() {
    var terminal = Terminal.newBuilder()
        .terminalId(TERMINAL_ID)
        .terminalName("Terminal 1")
        .terminalOperator(OrganisationUnit.newBuilder()
            .organisationUnitId(OPERATOR_OU_ID)
            .build())
        .terminalActive(true)
        .build();

    when(teamService.isIndustryUser(SERVICE_USER_DETAIL)).thenReturn(true);
    when(organisationUnitPermissionService.hasOperatorPermission(SERVICE_USER_DETAIL, OPERATOR_OU_ID, RolePermission.CREATE_FCS_APPLICATIONS))
        .thenReturn(true);

    var terminalJson = TerminalWithOperatorJson.from(terminal);
    assertThat(assetService.getStartApplicationDecisionForTerminal(SERVICE_USER_DETAIL, () -> terminalJson))
        .isEqualTo(StartApplicationDecision.allowed());
  }

  @Test
  void getStartApplicationDecisionForTerminal_noOperator() {
    var terminal = Terminal.newBuilder()
        .terminalId(TERMINAL_ID)
        .terminalName("Terminal 1")
        .terminalActive(true)
        .build();

    when(teamService.isIndustryUser(SERVICE_USER_DETAIL)).thenReturn(true);

    var terminalJson = TerminalWithOperatorJson.from(terminal);
    assertThat(assetService.getStartApplicationDecisionForTerminal(SERVICE_USER_DETAIL, () -> terminalJson))
        .isEqualTo(StartApplicationDecision.notAllowed(
            List.of("An operator does not exist for this facility")
        ));
  }

  @Test
  void getStartApplicationDecisionForTerminal_noPermission() {
    var terminal = Terminal.newBuilder()
        .terminalId(TERMINAL_ID)
        .terminalName("Terminal 1")
        .terminalOperator(OrganisationUnit.newBuilder()
            .organisationUnitId(OPERATOR_OU_ID)
            .build())
        .terminalActive(true)
        .build();

    when(teamService.isIndustryUser(SERVICE_USER_DETAIL)).thenReturn(true);
    when(organisationUnitPermissionService.hasOperatorPermission(SERVICE_USER_DETAIL, OPERATOR_OU_ID, RolePermission.CREATE_FCS_APPLICATIONS))
        .thenReturn(false);

    var terminalJson = TerminalWithOperatorJson.from(terminal);
    assertThat(assetService.getStartApplicationDecisionForTerminal(SERVICE_USER_DETAIL, () -> terminalJson))
        .isEqualTo(StartApplicationDecision.notAllowed(
            List.of("You are missing permissions to create applications for this facility")
        ));
  }

  @Test
  void getStartApplicationDecisionForTerminal_notActive() {
    var terminal = Terminal.newBuilder()
        .terminalId(TERMINAL_ID)
        .terminalName("Terminal 1")
        .terminalOperator(OrganisationUnit.newBuilder()
            .organisationUnitId(OPERATOR_OU_ID)
            .build())
        .terminalActive(false)
        .build();

    when(teamService.isIndustryUser(SERVICE_USER_DETAIL)).thenReturn(true);
    when(organisationUnitPermissionService.hasOperatorPermission(SERVICE_USER_DETAIL, OPERATOR_OU_ID, RolePermission.CREATE_FCS_APPLICATIONS))
        .thenReturn(true);

    var terminalJson = TerminalWithOperatorJson.from(terminal);
    assertThat(assetService.getStartApplicationDecisionForTerminal(SERVICE_USER_DETAIL, () -> terminalJson))
        .isEqualTo(StartApplicationDecision.notAllowed(
            List.of(
                "%s %s".formatted(terminal.getTerminalName(), TERMINAL_INACTIVE_VALIDATION_MESSAGE)
            )
        ));
  }

  @Test
  void getStartApplicationDecisionForTerminal_nullActive() {
    var terminal = Terminal.newBuilder()
        .terminalId(TERMINAL_ID)
        .terminalName("Terminal 1")
        .terminalOperator(OrganisationUnit.newBuilder()
            .organisationUnitId(OPERATOR_OU_ID)
            .build())
        .build();

    when(teamService.isIndustryUser(SERVICE_USER_DETAIL)).thenReturn(true);
    when(organisationUnitPermissionService.hasOperatorPermission(SERVICE_USER_DETAIL, OPERATOR_OU_ID, RolePermission.CREATE_FCS_APPLICATIONS))
        .thenReturn(true);

    var terminalJson = TerminalWithOperatorJson.from(terminal);
    assertThat(assetService.getStartApplicationDecisionForTerminal(SERVICE_USER_DETAIL, () -> terminalJson))
        .isEqualTo(StartApplicationDecision.notAllowed(
            List.of(
                "%s %s".formatted(terminal.getTerminalName(), TERMINAL_INACTIVE_VALIDATION_MESSAGE)
            )
        ));
  }

}
