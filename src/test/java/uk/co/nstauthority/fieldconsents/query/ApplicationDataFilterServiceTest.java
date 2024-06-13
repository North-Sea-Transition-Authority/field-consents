package uk.co.nstauthority.fieldconsents.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.jooq.impl.DSL.exists;
import static org.jooq.impl.DSL.falseCondition;
import static org.jooq.impl.DSL.year;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetTestUtil.fieldAsset1;
import static uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetTestUtil.fieldAsset1a;
import static uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetTestUtil.fieldAsset2;
import static uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetTestUtil.fieldAsset2a;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1Json;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1JsonWithOperatorAndLicences;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field2Json;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field2JsonWithOperatorAndLicences;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1Json;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationAssets.APPLICATION_ASSETS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationConsentIssuingApprovals.APPLICATION_CONSENT_ISSUING_APPROVALS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationFlags.APPLICATION_FLAGS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationVersions.APPLICATION_VERSIONS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.Applications.APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ConsentLengths.CONSENT_LENGTHS;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterFormTestUtil.APPLICATION_NO;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterFormTestUtil.ORGANISATION_UNIT_ID;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterService.FIELD_LOOKUP_PURPOSE;

import java.util.Collections;
import java.util.List;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultDSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationFieldService;
import uk.co.nstauthority.fieldconsents.application.assets.AssetRole;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.application.flags.ApplicationFlagType;
import uk.co.nstauthority.fieldconsents.assets.AssetKey;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.assets.AssetTypeWithShore;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldJson;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.assets.fields.GeographicArea;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalService;
import uk.co.nstauthority.fieldconsents.search.AceFlagStatus;

@ExtendWith(MockitoExtension.class)
class ApplicationDataFilterServiceTest {

  private DSLContext context;

  @Mock
  private ApplicationAssetService applicationAssetService;

  @Mock
  private FieldService fieldService;

  @Mock
  private TerminalService terminalService;

  @Mock
  private ApplicationFieldService applicationFieldService;

  private ApplicationDataFilterService applicationDataFilterService;

  private ApplicationDataFilterForm dataFilterForm;

  @BeforeEach
  void setUp() {
    context = new DefaultDSLContext(SQLDialect.DEFAULT);
    applicationDataFilterService = new ApplicationDataFilterService(
        context,
        applicationAssetService,
        fieldService,
        terminalService,
        applicationFieldService
    );

    dataFilterForm = new ApplicationDataFilterForm();
  }

  @Test
  void getConditions_ReferenceNumberSelected() {
    dataFilterForm.setReferenceNumber(String.valueOf(APPLICATION_NO));

    var conditions = applicationDataFilterService.getConditions(dataFilterForm);

    assertThat(conditions).containsExactly(
        APPLICATIONS.APPLICATION_NO.eq(APPLICATION_NO)
    );
  }

  @Test
  void getConditions_whenReferenceNumberSelectedIsNotAValidNumber() {
    dataFilterForm.setReferenceNumber("abc");

    var conditions = applicationDataFilterService.getConditions(dataFilterForm);

    assertThat(conditions).containsExactly(
        DSL.falseCondition()
    );
  }

  @Test
  void getConditions_StatusesSelected() {
    var status = ApplicationVersionStatus.IN_PROGRESS;
    dataFilterForm.setStatuses(Collections.singletonList(status));

    var conditions = applicationDataFilterService.getConditions(dataFilterForm);

    assertThat(conditions).containsExactly(
        APPLICATION_VERSIONS.STATUS.in(Collections.singletonList(status.getEnumName()))
    );
  }

  @Test
  void getConditions_ApplicationTypesSelected() {
    var applicationType = ApplicationType.PRODUCTION;
    dataFilterForm.setApplicationTypes(Collections.singletonList(applicationType));

    var conditions = applicationDataFilterService.getConditions(dataFilterForm);

    assertThat(conditions).containsExactly(
        APPLICATIONS.TYPE.in(Collections.singletonList(applicationType.getEnumName()))
    );
  }

  @Test
  void getConditions_DurationTypesSelected() {
    var durationTypes = ConsentLengthType.LONG_TERM;
    dataFilterForm.setDurationTypes(Collections.singletonList(durationTypes));

    var conditions = applicationDataFilterService.getConditions(dataFilterForm);

    assertThat(conditions).containsExactly(
        CONSENT_LENGTHS.CONSENT_LENGTH.in(Collections.singletonList(durationTypes.getEnumName()))
    );
  }

  @Test
  void getConditions_OperatorSelected() {
    dataFilterForm.setOperatorId(ORGANISATION_UNIT_ID);

    var conditions = applicationDataFilterService.getConditions(dataFilterForm);

    assertThat(conditions).containsExactly(
        APPLICATION_VERSIONS.PRIMARY_OPERATOR_OU_ID.eq(ORGANISATION_UNIT_ID)
    );
  }

  @Test
  void getConditions_assetTypesSelected_withFieldOffshoreOnly() {
    var assetTypeWithShore = List.of(AssetTypeWithShore.FIELD_OFFSHORE);
    dataFilterForm.setAssetTypesWithShore(assetTypeWithShore);
    when(applicationAssetService.getPrimaryAndSecondaryFieldJsonsOfShoreType(assetTypeWithShore, FIELD_LOOKUP_PURPOSE))
        .thenReturn(List.of(field1Json));
    var fieldJsonIds = List.of(field1Json.getId());

    var conditions = applicationDataFilterService.getConditions(dataFilterForm);

    assertThat(conditions).containsExactly(
        exists(context.select(APPLICATION_ASSETS.ASSET_ID)
            .from(APPLICATION_ASSETS)
            .where(APPLICATION_ASSETS.APPLICATION_VERSION_ID.eq(APPLICATION_VERSIONS.ID)
                .and(APPLICATION_ASSETS.ASSET_ROLE.in(AssetRole.PRIMARY.name(), AssetRole.SECONDARY.name()))
                .and(APPLICATION_ASSETS.ASSET_TYPE.eq(AssetType.FIELD.name()))
                .and(APPLICATION_ASSETS.ASSET_ID.in(fieldJsonIds)))
        )
    );
  }

  @Test
  void getConditions_assetTypesSelected_withFieldOnShoreAndOffshore() {
    var assetTypeWithShore = List.of(AssetTypeWithShore.FIELD_OFFSHORE, AssetTypeWithShore.FIELD_ONSHORE);
    dataFilterForm.setAssetTypesWithShore(assetTypeWithShore);
    var fieldJsons = List.of(field1Json, field2Json);
    var fieldJsonIds = fieldJsons.stream().map(FieldJson::getId).toList();
    when(applicationAssetService.getPrimaryAndSecondaryFieldJsonsOfShoreType(assetTypeWithShore, FIELD_LOOKUP_PURPOSE)).thenReturn(fieldJsons);

    var conditions = applicationDataFilterService.getConditions(dataFilterForm);

    assertThat(conditions).containsExactly(
        exists(context.select(APPLICATION_ASSETS.ASSET_ID)
            .from(APPLICATION_ASSETS)
            .where(APPLICATION_ASSETS.APPLICATION_VERSION_ID.eq(APPLICATION_VERSIONS.ID)
                .and(APPLICATION_ASSETS.ASSET_ROLE.in(AssetRole.PRIMARY.name(), AssetRole.SECONDARY.name()))
                .and(APPLICATION_ASSETS.ASSET_TYPE.eq(AssetType.FIELD.name()))
                .and(APPLICATION_ASSETS.ASSET_ID.in(fieldJsonIds)))
        )
    );
  }

  @Test
  void getConditions_assetTypesSelected_containsTerminalsOnly() {
    dataFilterForm.setAssetTypesWithShore(List.of(AssetTypeWithShore.TERMINAL));

    var conditions = applicationDataFilterService.getConditions(dataFilterForm);

    assertThat(conditions).containsExactly(APPLICATION_ASSETS.ASSET_TYPE.eq(AssetType.TERMINAL.name()));
  }

  @Test
  void getConditions_assetTypesSelected_containsFieldsAndTerminals() {
    var assetTypeWithShore = List.of(AssetTypeWithShore.FIELD_OFFSHORE, AssetTypeWithShore.TERMINAL);
    dataFilterForm.setAssetTypesWithShore(assetTypeWithShore);
    var fieldJsons = List.of(field1Json, field2Json);
    var fieldJsonIds = fieldJsons.stream().map(FieldJson::getId).toList();
    when(applicationAssetService.getPrimaryAndSecondaryFieldJsonsOfShoreType(assetTypeWithShore, FIELD_LOOKUP_PURPOSE)).thenReturn(fieldJsons);

    var conditions = applicationDataFilterService.getConditions(dataFilterForm);

    assertThat(conditions)
        .containsExactly(
            APPLICATION_ASSETS.ASSET_TYPE.eq(AssetType.TERMINAL.name())
                .or(
                    exists(context.select(APPLICATION_ASSETS.ASSET_ID)
                        .from(APPLICATION_ASSETS)
                        .where(APPLICATION_ASSETS.APPLICATION_VERSION_ID.eq(APPLICATION_VERSIONS.ID)
                            .and(APPLICATION_ASSETS.ASSET_ROLE.in(AssetRole.PRIMARY.name(), AssetRole.SECONDARY.name()))
                            .and(APPLICATION_ASSETS.ASSET_TYPE.eq(AssetType.FIELD.name()))
                            .and(APPLICATION_ASSETS.ASSET_ID.in(fieldJsonIds)))
                    )
                )
        );
  }

  @Test
  void getConditions_withSubmittedYearNonNumeric() {
    dataFilterForm.setSubmittedYear("abc");

    assertThat(applicationDataFilterService.getConditions(dataFilterForm)).containsExactly(falseCondition());
  }

  @Test
  void getConditions_withValidSubmittedYear() {
    dataFilterForm.setSubmittedYear("2023");

    assertThat(applicationDataFilterService.getConditions(dataFilterForm)).containsExactly(
        year(APPLICATION_VERSIONS.SUBMITTED_DATE_TIME).eq(2023)
    );
  }

  @Test
  void getConditions_withLicenceReference_whenNoPrimaryOrSecondaryFieldsFound() {
    dataFilterForm.setLicenceReference("P123");

    when(applicationAssetService.getAllPrimaryAndSecondaryFieldAssets()).thenReturn(Collections.emptyList());

    assertThat(applicationDataFilterService.getConditions(dataFilterForm))
        .containsExactly(
            falseCondition()
        );
  }

  @Test
  void getConditions_withLicenceReference_whenNoMatchingFieldsFound() {
    dataFilterForm.setLicenceReference("P123");
    var fieldsWithOperatorAndLicences = List.of(field1JsonWithOperatorAndLicences, field2JsonWithOperatorAndLicences);

    when(applicationAssetService.getAllPrimaryAndSecondaryFieldAssets())
        .thenReturn(List.of(fieldAsset1, fieldAsset2, fieldAsset1a, fieldAsset2a));
    when(fieldService
        .findFieldsWithOperatorAndLicences(List.of(fieldAsset1.getAssetId(), fieldAsset2.getAssetId()), FIELD_LOOKUP_PURPOSE)).thenReturn(fieldsWithOperatorAndLicences);


    assertThat(applicationDataFilterService.getConditions(dataFilterForm))
        .containsExactly(
            falseCondition()
        );
  }

  @Test
  void getConditions_withLicenceReference_whenMatchingFieldsFound() {
    dataFilterForm.setLicenceReference("P1");
    var fieldsWithOperatorAndLicences = List.of(field1JsonWithOperatorAndLicences, field2JsonWithOperatorAndLicences);

    when(applicationAssetService.getAllPrimaryAndSecondaryFieldAssets())
        .thenReturn(List.of(fieldAsset1, fieldAsset2, fieldAsset1a, fieldAsset2a));
    when(fieldService
        .findFieldsWithOperatorAndLicences(List.of(fieldAsset1.getAssetId(), fieldAsset2.getAssetId()), FIELD_LOOKUP_PURPOSE)).thenReturn(fieldsWithOperatorAndLicences);


    assertThat(applicationDataFilterService.getConditions(dataFilterForm))
        .containsExactly(
            exists(context.select(APPLICATION_ASSETS.ASSET_ID)
                .from(APPLICATION_ASSETS)
                .where(APPLICATION_ASSETS.APPLICATION_VERSION_ID.eq(APPLICATION_VERSIONS.ID)
                    .and(APPLICATION_ASSETS.ASSET_ROLE.in(AssetRole.PRIMARY.name(), AssetRole.SECONDARY.name()))
                    .and(APPLICATION_ASSETS.ASSET_TYPE.eq(AssetType.FIELD.name()))
                    .and(APPLICATION_ASSETS.ASSET_ID.in(List.of(fieldAsset1.getAssetId(), fieldAsset2.getAssetId())))))
        );
  }

  @Test
  void getFieldCondition() {
    var assetKey = new AssetKey(1, AssetType.FIELD);

    when(fieldService.getField(eq(assetKey.assetId()), anyString())).thenReturn(field1Json);

    assertThat(applicationDataFilterService.getFieldCondition(assetKey)).isEqualTo(
        exists(context.select(APPLICATION_ASSETS.ASSET_ID)
            .from(APPLICATION_ASSETS)
            .where(APPLICATION_ASSETS.APPLICATION_VERSION_ID.eq(APPLICATION_VERSIONS.ID)
                .and(APPLICATION_ASSETS.ASSET_ROLE.in(AssetRole.PRIMARY.name(), AssetRole.SECONDARY.name()))
                .and(APPLICATION_ASSETS.ASSET_TYPE.eq(AssetType.FIELD.name()))
                .and(APPLICATION_ASSETS.ASSET_ID.eq(field1Json.getId())))));
  }

  @Test
  void getFieldCondition_invalidAssetType() {
    var assetKey = new AssetKey(1, AssetType.TERMINAL);
    assertThatThrownBy(() -> applicationDataFilterService.getFieldCondition(assetKey))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Expected AssetKey.assetType to be [FIELD] but was [TERMINAL]");
  }

  @Test
  void getTerminalCondition() {
    var assetKey = new AssetKey(1, AssetType.TERMINAL);

    when(terminalService.getTerminal(eq(assetKey.assetId()), anyString())).thenReturn(terminal1Json);

    assertThat(applicationDataFilterService.getTerminalCondition(assetKey)).isEqualTo(
        exists(context.select(APPLICATION_ASSETS.ASSET_ID)
            .from(APPLICATION_ASSETS)
            .where(APPLICATION_ASSETS.APPLICATION_VERSION_ID.eq(APPLICATION_VERSIONS.ID)
                .and(APPLICATION_ASSETS.ASSET_ROLE.eq(AssetRole.PRIMARY.name()))
                .and(APPLICATION_ASSETS.ASSET_TYPE.eq(AssetType.TERMINAL.name()))
                .and(APPLICATION_ASSETS.ASSET_ID.eq(terminal1Json.getId())))));
  }

  @Test
  void getTerminalCondition_invalidAssetType() {
    var assetKey = new AssetKey(1, AssetType.FIELD);
    assertThatThrownBy(() -> applicationDataFilterService.getTerminalCondition(assetKey))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Expected AssetKey.assetType to be [TERMINAL] but was [FIELD]");
  }

  @Test
  void getGeographicAreasQueryCondition() {
    var distinctPrimaryFieldIds = List.of(1, 2, 3);
    when(applicationFieldService.findDistinctPrimaryFieldIds()).thenReturn(distinctPrimaryFieldIds);

    var field1 = mock(FieldJson.class);
    when(field1.getId()).thenReturn(1);
    when(field1.getGeographicArea()).thenReturn(GeographicArea.CNS);

    var field2 = mock(FieldJson.class);
    when(field2.getId()).thenReturn(2);
    when(field2.getGeographicArea()).thenReturn(GeographicArea.IS);

    // this one should be excluded because it wasn't selected
    var field3 = mock(FieldJson.class);
    when(field3.getGeographicArea()).thenReturn(GeographicArea.LAND);

    var fieldJsonList = List.of(field1, field2, field3);
    when(fieldService.findFieldsByIds(eq(distinctPrimaryFieldIds), anyString())).thenReturn(fieldJsonList);

    var geographicAreas = List.of(GeographicArea.CNS, GeographicArea.IS);
    assertThat(applicationDataFilterService.getGeographicAreasQueryCondition(geographicAreas))
        .isEqualTo(APPLICATION_ASSETS.ASSET_TYPE.eq(AssetType.FIELD.name())
            .and(APPLICATION_ASSETS.ASSET_ID.in(List.of(1, 2))));
  }

  @ParameterizedTest
  @EnumSource(AceFlagStatus.class)
  void getAceStatusCondition(AceFlagStatus aceFlagStatus) {
    var isAceApplication = aceFlagStatus.isAceApplication();
    var expectedCondition = exists(context.select(APPLICATION_FLAGS.FLAG_VALUE)
        .from(APPLICATION_FLAGS)
        .where(APPLICATION_FLAGS.FLAG_TYPE.eq(ApplicationFlagType.IS_ACE_APPLICATION.name())
            .and(APPLICATION_FLAGS.APPLICATION_VERSION_ID.eq(APPLICATION_VERSIONS.ID))
            .and(APPLICATION_FLAGS.FLAG_VALUE.in(Collections.singletonList(isAceApplication)))));

    assertThat(applicationDataFilterService.getAceStatusCondition(List.of(aceFlagStatus))).isEqualTo(expectedCondition);
  }

  @Test
  void getApprovedForIssueCondition() {
    assertThat(applicationDataFilterService.getApprovedForIssueCondition()).isEqualTo(APPLICATION_CONSENT_ISSUING_APPROVALS.ID.isNotNull());
  }

}
