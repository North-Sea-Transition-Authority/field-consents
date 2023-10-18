package uk.co.nstauthority.fieldconsents.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jooq.impl.DSL.exists;
import static org.jooq.impl.DSL.falseCondition;
import static org.jooq.impl.DSL.year;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1Json;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field2Json;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationAssets.APPLICATION_ASSETS;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.assets.AssetRole;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.assets.AssetTypeWithShore;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldJson;

@ExtendWith(MockitoExtension.class)
class ApplicationDataFilterServiceTest {

  private DSLContext context;
  @Mock
  private ApplicationAssetService applicationAssetService;
  private ApplicationDataFilterService applicationDataFilterService;
  private ApplicationDataFilterForm dataFilterForm;

  @BeforeEach
  void setUp() {
    context = new DefaultDSLContext(SQLDialect.DEFAULT);
    applicationDataFilterService = new ApplicationDataFilterService(
        context,
        applicationAssetService
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
        exists(context.select(APPLICATION_ASSETS.FIELD_ID)
            .from(APPLICATION_ASSETS)
            .where(APPLICATION_ASSETS.APPLICATION_VERSION_ID.eq(APPLICATION_VERSIONS.ID)
                .and(APPLICATION_ASSETS.ASSET_ROLE.in(AssetRole.PRIMARY.name(), AssetRole.SECONDARY.name()))
                .and(APPLICATION_ASSETS.FIELD_ID.in(fieldJsonIds)))
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
        exists(context.select(APPLICATION_ASSETS.FIELD_ID)
            .from(APPLICATION_ASSETS)
            .where(APPLICATION_ASSETS.APPLICATION_VERSION_ID.eq(APPLICATION_VERSIONS.ID)
                .and(APPLICATION_ASSETS.ASSET_ROLE.in(AssetRole.PRIMARY.name(), AssetRole.SECONDARY.name()))
                .and(APPLICATION_ASSETS.FIELD_ID.in(fieldJsonIds)))
        )
    );
  }

  @Test
  void getConditions_assetTypesSelected_containsTerminalsOnly() {
    dataFilterForm.setAssetTypesWithShore(List.of(AssetTypeWithShore.TERMINAL));

    var conditions = applicationDataFilterService.getConditions(dataFilterForm);

    assertThat(conditions).containsExactly(APPLICATION_ASSETS.TERMINAL_ID.isNotNull());
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
            APPLICATION_ASSETS.TERMINAL_ID.isNotNull()
                .or(
                    exists(context.select(APPLICATION_ASSETS.FIELD_ID)
                        .from(APPLICATION_ASSETS)
                        .where(APPLICATION_ASSETS.APPLICATION_VERSION_ID.eq(APPLICATION_VERSIONS.ID)
                            .and(APPLICATION_ASSETS.ASSET_ROLE.in(AssetRole.PRIMARY.name(), AssetRole.SECONDARY.name()))
                            .and(APPLICATION_ASSETS.FIELD_ID.in(fieldJsonIds)))
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
}
