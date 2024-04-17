package uk.co.nstauthority.fieldconsents.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jooq.impl.DSL.coalesce;
import static org.jooq.impl.DSL.exists;
import static org.jooq.impl.DSL.falseCondition;
import static org.jooq.impl.DSL.year;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.assets.AssetTestUtil.FIELD1_ASSET_KEY;
import static uk.co.nstauthority.fieldconsents.assets.AssetTestUtil.TERMINAL1_ASSET_KEY;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationConsentIssuingApprovals.APPLICATION_CONSENT_ISSUING_APPROVALS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationConsultations.APPLICATION_CONSULTATIONS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationVersions.APPLICATION_VERSIONS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.Applications.APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ConsentLengths.CONSENT_LENGTHS;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterFormTestUtil.APPLICATION_NO;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterFormTestUtil.ORGANISATION_UNIT_ID;

import java.util.Collections;
import java.util.List;
import org.jooq.Condition;
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
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.assets.AssetKey;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterFormTestUtil;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@ExtendWith(MockitoExtension.class)
class SearchFilterServiceTest {

  private DSLContext context;

  @Mock
  private ApplicationDataFilterService applicationDataFilterService;

  private SearchFilterService searchFilterService;
  private SearchFilterForm form;

  @BeforeEach
  void setUp() {
    form = new SearchFilterForm();
    context = new DefaultDSLContext(SQLDialect.DEFAULT);
    searchFilterService = new SearchFilterService(context, applicationDataFilterService);
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

  @ParameterizedTest
  @EnumSource(AceFlagStatus.class)
  void getConditions_aceFlagStatus(AceFlagStatus aceFlagStatus) {
    form.setAceFlagStatuses(List.of(aceFlagStatus));

    when(applicationDataFilterService.getConditions(form)).thenReturn(Collections.emptyList());

    var aceStatusFlagCondition = mock(Condition.class);
    when(applicationDataFilterService.getAceStatusCondition(form.getAceFlagStatuses())).thenReturn(aceStatusFlagCondition);

    assertThat(searchFilterService.getConditions(form, TeamType.REGULATOR)).containsExactly(aceStatusFlagCondition);
  }

  @Test
  void getConditions_withNoAceFlag() {
    when(applicationDataFilterService.getConditions(form)).thenReturn(Collections.emptyList());

    assertThat(searchFilterService.getConditions(form, TeamType.INDUSTRY)).isEmpty();
  }

  @Test
  void getConditions_withFieldAsset() {
    form.setFieldAssetKey(FIELD1_ASSET_KEY);

    var jooqCondition = mock(Condition.class);
    when(applicationDataFilterService.getFieldCondition(AssetKey.from(form.getFieldAssetKey()))).thenReturn(jooqCondition);

    assertThat(searchFilterService.getConditions(form, TeamType.REGULATOR)).containsExactly(jooqCondition);
  }

  @Test
  void getConditions_withTerminalAsset() {
    form.setTerminalAssetKey(TERMINAL1_ASSET_KEY);

    var jooqCondition = mock(Condition.class);
    when(applicationDataFilterService.getTerminalCondition(AssetKey.from(form.getTerminalAssetKey()))).thenReturn(jooqCondition);

    assertThat(searchFilterService.getConditions(form, TeamType.REGULATOR)).containsExactly(jooqCondition);
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
  void getConditions_whenRegulatorWithApprovedForIssueConditionIsTrue_thenApprovedForIssueConditionIsAdded() {
    when(applicationDataFilterService.getApprovedForIssueCondition()).thenReturn(APPLICATION_CONSENT_ISSUING_APPROVALS.ID.isNotNull());

    form.setApprovedForIssue(true);

    assertThat(searchFilterService.getConditions(form, TeamType.REGULATOR)).containsExactly(
        APPLICATION_CONSENT_ISSUING_APPROVALS.ID.isNotNull());
  }

  @Test
  void getConditions_whenRegulatorWithApprovedForIssueConditionIsFalse_thenApprovedForIssueConditionIsNotAdded() {
    form.setApprovedForIssue(false);

    assertThat(searchFilterService.getConditions(form, TeamType.REGULATOR)).isEmpty();
  }

  @Test
  void getConditions_whenIndustry_thenApprovedForIssueConditionIsNotAdded() {
    assertThat(searchFilterService.getConditions(form, TeamType.INDUSTRY)).isEmpty();
  }

  @Test
  void getConditions_whenConsultee_thenApprovedForIssueConditionIsNotAdded() {
    assertThat(searchFilterService.getConditions(form, TeamType.OPRED)).doesNotContain(
        APPLICATION_CONSENT_ISSUING_APPROVALS.ID.isNotNull());
  }
}
