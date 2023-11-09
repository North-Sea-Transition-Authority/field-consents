package uk.co.nstauthority.fieldconsents.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jooq.impl.DSL.coalesce;
import static org.jooq.impl.DSL.exists;
import static org.jooq.impl.DSL.falseCondition;
import static org.jooq.impl.DSL.year;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetTestUtil.fieldAsset1;
import static uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetTestUtil.fieldAsset2;
import static uk.co.nstauthority.fieldconsents.assets.AssetTestUtil.FIELD1_ASSET_KEY;
import static uk.co.nstauthority.fieldconsents.assets.AssetTestUtil.TERMINAL1_ASSET_KEY;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1Json;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1JsonWithOperatorAndLicences;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field2JsonWithOperatorAndLicences;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1Json;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationAssets.APPLICATION_ASSETS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationConsultations.APPLICATION_CONSULTATIONS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationFlags.APPLICATION_FLAGS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationVersions.APPLICATION_VERSIONS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.Applications.APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ConsentLengths.CONSENT_LENGTHS;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterFormTestUtil.APPLICATION_NO;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterFormTestUtil.ORGANISATION_UNIT_ID;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterService.FIELD_LOOKUP_PURPOSE;
import static uk.co.nstauthority.fieldconsents.search.SearchFilterService.TERMINAL_LOOKUP_PURPOSE;

import java.util.Collections;
import java.util.List;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
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
import uk.co.nstauthority.fieldconsents.application.assets.AssetRole;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.application.flags.ApplicationFlagType;
import uk.co.nstauthority.fieldconsents.assets.AssetKey;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalService;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterFormTestUtil;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@ExtendWith(MockitoExtension.class)
class SearchFilterServiceTest {

  private DSLContext context;
  @Mock
  private FieldService fieldService;
  @Mock
  private TerminalService terminalService;
  @Mock
  private ApplicationDataFilterService applicationDataFilterService;
  @Mock
  private ApplicationAssetService applicationAssetService;

  private SearchFilterService searchFilterService;
  private SearchFilterForm form;

  @BeforeEach
  void setUp() {
    form = new SearchFilterForm();
    context = new DefaultDSLContext(SQLDialect.DEFAULT);
    searchFilterService = new SearchFilterService(
        context,
        fieldService,
        terminalService,
        applicationDataFilterService,
        applicationAssetService
    );
  }

  @Test
  void getConditions_withEmptyFilter() {
    assertThat(searchFilterService.getConditions(new SearchFilterForm(), TeamType.INDUSTRY)).isEmpty();
  }

  @Test
  void getConditions_withBasicSearchFilter() {
    form = ApplicationDataFilterFormTestUtil.getBasicSearchFilterForm();
    when(applicationDataFilterService.getConditions(form)).thenReturn(
        List.of(
            APPLICATIONS.APPLICATION_NO.eq(APPLICATION_NO),
            APPLICATION_VERSIONS.STATUS.in(List.of(ApplicationVersionStatus.SUBMITTED, ApplicationVersionStatus.IN_PROGRESS)),
            APPLICATIONS.TYPE.in(List.of(ApplicationType.PRODUCTION, ApplicationType.VENT)),
            CONSENT_LENGTHS.CONSENT_LENGTH.in(List.of(ConsentLengthType.ANNUAL, ConsentLengthType.SHORT_TERM, ConsentLengthType.LONG_TERM)),
            APPLICATION_VERSIONS.PRIMARY_OPERATOR_OU_ID.eq(ORGANISATION_UNIT_ID)
        )
    );

    assertThat(searchFilterService.getConditions(form, TeamType.INDUSTRY))
        .containsExactly(
            APPLICATIONS.APPLICATION_NO.eq(APPLICATION_NO),
            APPLICATION_VERSIONS.STATUS.in(List.of(ApplicationVersionStatus.SUBMITTED, ApplicationVersionStatus.IN_PROGRESS)),
            APPLICATIONS.TYPE.in(List.of(ApplicationType.PRODUCTION, ApplicationType.VENT)),
            CONSENT_LENGTHS.CONSENT_LENGTH.in(List.of(ConsentLengthType.ANNUAL, ConsentLengthType.SHORT_TERM, ConsentLengthType.LONG_TERM)),
            APPLICATION_VERSIONS.PRIMARY_OPERATOR_OU_ID.eq(ORGANISATION_UNIT_ID)
        );
  }

  @Test
  void getConditions_withAceFlagTrue() {
    form.setAceFlagStatuses(List.of(AceFlagStatus.ACE));

    when(applicationDataFilterService.getConditions(form)).thenReturn(Collections.emptyList());

    assertThat(searchFilterService.getConditions(form, TeamType.REGULATOR))
        .containsExactly(
            exists(context.select(APPLICATION_FLAGS.FLAG_VALUE)
                .from(APPLICATION_FLAGS)
                .where(APPLICATION_FLAGS.FLAG_TYPE.eq(ApplicationFlagType.IS_ACE_APPLICATION.name())
                    .and(APPLICATION_FLAGS.APPLICATION_VERSION_ID.eq(APPLICATION_VERSIONS.ID))
                    .and(APPLICATION_FLAGS.FLAG_VALUE.in(true))))
          );
  }

  @Test
  void getConditions_withAceFlagFalse() {
    form.setAceFlagStatuses(List.of(AceFlagStatus.NON_ACE));

    when(applicationDataFilterService.getConditions(form)).thenReturn(Collections.emptyList());

    assertThat(searchFilterService.getConditions(form, TeamType.REGULATOR))
        .containsExactly(
            exists(context.select(APPLICATION_FLAGS.FLAG_VALUE)
                .from(APPLICATION_FLAGS)
                .where(APPLICATION_FLAGS.FLAG_TYPE.eq(ApplicationFlagType.IS_ACE_APPLICATION.name())
                    .and(APPLICATION_FLAGS.APPLICATION_VERSION_ID.eq(APPLICATION_VERSIONS.ID))
                    .and(APPLICATION_FLAGS.FLAG_VALUE.in(false))))
        );
  }

  @Test
  void getConditions_withNoAceFlag() {
    when(applicationDataFilterService.getConditions(form)).thenReturn(Collections.emptyList());

    assertThat(searchFilterService.getConditions(form, TeamType.INDUSTRY)).isEmpty();
  }

  @Test
  void getConditions_withFieldAsset() {
    form.setFieldAssetKey(FIELD1_ASSET_KEY);

    when(applicationDataFilterService.getConditions(form)).thenReturn(Collections.emptyList());
    when(fieldService.getField(AssetKey.from(FIELD1_ASSET_KEY).assetId(), FIELD_LOOKUP_PURPOSE)).thenReturn(field1Json);

    assertThat(searchFilterService.getConditions(form, TeamType.REGULATOR))
        .containsExactly(
            exists(context.select(APPLICATION_ASSETS.ASSET_ID)
                .from(APPLICATION_ASSETS)
                .where(APPLICATION_ASSETS.APPLICATION_VERSION_ID.eq(APPLICATION_VERSIONS.ID)
                    .and(APPLICATION_ASSETS.ASSET_ROLE.in(AssetRole.PRIMARY.name(), AssetRole.SECONDARY.name()))
                    .and(APPLICATION_ASSETS.ASSET_TYPE.eq(AssetType.FIELD.name()))
                    .and(APPLICATION_ASSETS.ASSET_ID.eq(field1Json.getId()))))
        );
  }

  @Test
  void getConditions_withTerminalAsset() {
    form.setTerminalAssetKey(TERMINAL1_ASSET_KEY);

    when(applicationDataFilterService.getConditions(form)).thenReturn(Collections.emptyList());
    when(terminalService.getTerminal(AssetKey.from(TERMINAL1_ASSET_KEY).assetId(), TERMINAL_LOOKUP_PURPOSE)).thenReturn(terminal1Json);

    assertThat(searchFilterService.getConditions(form, TeamType.REGULATOR))
        .containsExactly(
            exists(context.select(APPLICATION_ASSETS.ASSET_ID)
                .from(APPLICATION_ASSETS)
                .where(APPLICATION_ASSETS.APPLICATION_VERSION_ID.eq(APPLICATION_VERSIONS.ID)
                    .and(APPLICATION_ASSETS.ASSET_ROLE.eq(AssetRole.PRIMARY.name()))
                    .and(APPLICATION_ASSETS.ASSET_TYPE.eq(AssetType.TERMINAL.name()))
                    .and(APPLICATION_ASSETS.ASSET_ID.eq(terminal1Json.getId()))))
        );
  }

  @Test
  void getConditions_withConsultationsCondition() {
    when(applicationDataFilterService.getConditions(form)).thenReturn(Collections.emptyList());

    assertThat(searchFilterService.getConditions(form, TeamType.OPRED))
        .containsExactly(
            exists(context.select(APPLICATION_CONSULTATIONS.ID)
                .from(APPLICATION_CONSULTATIONS)
                .join(APPLICATION_VERSIONS)
                    .onKey(APPLICATION_CONSULTATIONS.REQUEST_APPLICATION_VERSION_ID)
                .where(APPLICATION_VERSIONS.APPLICATION_ID.eq(APPLICATIONS.ID)))
        );
  }

  @ParameterizedTest
  @EnumSource(value = TeamType.class)
  void getConditions_withValidConsentStartYear(TeamType teamType) {
    form.setConsentStartYear("2023");

    assertThat(searchFilterService.getConditions(form, teamType)).contains(
        coalesce(
            year(CONSENT_LENGTHS.SHORT_TERM_START_DATE),
            CONSENT_LENGTHS.LONG_TERM_START_YEAR,
            CONSENT_LENGTHS.ANNUAL_CONSENT_YEAR
        ).eq(2023)
    );
  }

  @ParameterizedTest
  @EnumSource(value = TeamType.class)
  void getConditions_withConsentStartYearNonNumeric(TeamType teamType) {
    form.setConsentStartYear("abc");

    assertThat(searchFilterService.getConditions(form, teamType)).contains(
        falseCondition()
    );
  }

  @Test
  void getConditions_withLicenceReference_whenNoPrimaryOrSecondaryFieldsFound() {
    form.setLicenceReference("P123");
    when(applicationDataFilterService.getConditions(form)).thenReturn(Collections.emptyList());

    when(applicationAssetService.getAllPrimaryAndSecondaryFieldAssets()).thenReturn(Collections.emptyList());

    assertThat(searchFilterService.getConditions(form, TeamType.REGULATOR))
        .containsExactly(
            falseCondition()
        );
  }

  @Test
  void getConditions_withLicenceReference_whenNoMatchingFieldsFound() {
    form.setLicenceReference("P123");
    var fieldsWithOperatorAndLicences = List.of(field1JsonWithOperatorAndLicences, field2JsonWithOperatorAndLicences);

    when(applicationDataFilterService.getConditions(form)).thenReturn(Collections.emptyList());
    when(applicationAssetService.getAllPrimaryAndSecondaryFieldAssets()).thenReturn(List.of(fieldAsset1, fieldAsset2));
    when(fieldService
        .findFieldsWithOperatorAndLicences(List.of(fieldAsset1.getAssetId(), fieldAsset2.getAssetId()), FIELD_LOOKUP_PURPOSE)).thenReturn(fieldsWithOperatorAndLicences);


    assertThat(searchFilterService.getConditions(form, TeamType.REGULATOR))
        .containsExactly(
            falseCondition()
        );
  }

  @Test
  void getConditions_withLicenceReference_whenMatchingFieldsFound() {
    form.setLicenceReference("P1");
    var fieldsWithOperatorAndLicences = List.of(field1JsonWithOperatorAndLicences, field2JsonWithOperatorAndLicences);

    when(applicationDataFilterService.getConditions(form)).thenReturn(Collections.emptyList());
    when(applicationAssetService.getAllPrimaryAndSecondaryFieldAssets()).thenReturn(List.of(fieldAsset1, fieldAsset2));
    when(fieldService
        .findFieldsWithOperatorAndLicences(List.of(fieldAsset1.getAssetId(), fieldAsset2.getAssetId()), FIELD_LOOKUP_PURPOSE)).thenReturn(fieldsWithOperatorAndLicences);


    assertThat(searchFilterService.getConditions(form, TeamType.REGULATOR))
        .containsExactly(
            exists(context.select(APPLICATION_ASSETS.ASSET_ID)
                .from(APPLICATION_ASSETS)
                .where(APPLICATION_ASSETS.APPLICATION_VERSION_ID.eq(APPLICATION_VERSIONS.ID)
                    .and(APPLICATION_ASSETS.ASSET_ROLE.in(AssetRole.PRIMARY.name(), AssetRole.SECONDARY.name()))
                    .and(APPLICATION_ASSETS.ASSET_TYPE.eq(AssetType.FIELD.name()))
                    .and(APPLICATION_ASSETS.ASSET_ID.in(List.of(1, 2)))))
        );
  }
}
