package uk.co.nstauthority.fieldconsents.workarea;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.USER_WUA_ID;
import static uk.co.nstauthority.fieldconsents.assets.AssetTestUtil.field1AssetJson;
import static uk.co.nstauthority.fieldconsents.assets.AssetTestUtil.terminal1AssetJson;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.FIELD_ID_1;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.FIELD_ID_2;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.FIELD_ID_3;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1Json;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field2Json;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field3Json;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.TERMINAL_ID_1;
import static uk.co.nstauthority.fieldconsents.generated.jooq.Tables.APPLICATION_CONSULTATIONS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationAssets.APPLICATION_ASSETS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationTechnicalReviews.APPLICATION_TECHNICAL_REVIEWS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationVersions.APPLICATION_VERSIONS;

import java.util.List;
import java.util.Optional;
import org.jooq.Condition;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationFieldService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationStatus;
import uk.co.nstauthority.fieldconsents.assets.AssetKey;
import uk.co.nstauthority.fieldconsents.assets.AssetService;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.assets.fields.GeographicArea;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterFormTestUtil;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterService;
import uk.co.nstauthority.fieldconsents.teams.TeamService;

@ExtendWith(MockitoExtension.class)
class WorkAreaFilterServiceTest {

  private static final Condition SUBMITTED_APPLICATION_CONDITION = APPLICATION_VERSIONS.STATUS.eq(ApplicationVersionStatus.SUBMITTED.name());
  private static final String FIELD_LOOKUP_PURPOSE = "Lookup field for the work-area";

  @Mock
  private AssetService assetService;

  @Mock
  private FieldService fieldService;

  @Mock
  private ApplicationFieldService applicationFieldService;

  @Mock
  private TeamService teamService;

  @Mock
  private ApplicationDataFilterService applicationDataFilterService;

  @InjectMocks
  private WorkAreaFilterService workAreaFilterService;

  private WorkAreaFilter filter;

  private WorkAreaFilterForm form;

  private ServiceUserDetail user;

  @BeforeEach
  void setup() {
    user = ServiceUserDetailTestUtil.Builder().build();
    filter = new WorkAreaFilter();
    form = new WorkAreaFilterForm();
  }

  @Test
  void getConditions_CheckApplicationDataFilterConditions() {
    when(teamService.isRegulatorUser(user)).thenReturn(true);

    var customCondition1 = mock(Condition.class);
    var customCondition2 = mock(Condition.class);
    var customCondition3 = mock(Condition.class);
    when(applicationDataFilterService.getConditions(filter)).thenReturn(List.of(customCondition1, customCondition2, customCondition3));

    var conditions = workAreaFilterService.getConditions(filter, user, null);

    assertThat(conditions).containsExactly(
        SUBMITTED_APPLICATION_CONDITION,
        customCondition1,
        customCondition2,
        customCondition3
    );
  }

  @Test
  void getConditions_UserIsNotRegulatorOrConsulteeOrIndustry() {
    assertThatThrownBy(() -> workAreaFilterService.getConditions(filter, user, null))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Expected user [%s] to be regulator, consultee or industry but was none of these.".formatted(user.wuaId()));
  }

  @Test
  void getConditions_EmptyFilter_Regulator() {
    when(teamService.isRegulatorUser(user)).thenReturn(true);

    var conditions = workAreaFilterService.getConditions(filter, user, null);

    assertThat(conditions).containsExactly(SUBMITTED_APPLICATION_CONDITION);
  }

  @Test
  void getConditions_EmptyFilter_Consultee() {
    when(teamService.isConsulteeUser(user)).thenReturn(true);

    var conditions = workAreaFilterService.getConditions(filter, user, null);

    assertThat(conditions).containsExactly(SUBMITTED_APPLICATION_CONDITION);
  }

  @Test
  void getConditions_EmptyFilter_Industry() {
    when(teamService.isIndustryUser(user)).thenReturn(true);

    var conditions = workAreaFilterService.getConditions(filter, user, null);

    assertThat(conditions).containsExactly(APPLICATION_VERSIONS.STATUS.in(
        ApplicationVersionStatus.IN_PROGRESS.name(),
        ApplicationVersionStatus.AWAITING_PAYMENT.name(),
        ApplicationVersionStatus.SUBMITTED.name()
    ));
  }

  @Test
  void getConditions_AssetNotFound() {
    when(teamService.isRegulatorUser(user)).thenReturn(true);

    form.setAssetKey(ApplicationDataFilterFormTestUtil.FIELD1_ASSET_KEY);
    filter.update(form);

    var assetKey = AssetKey.from(form.getAssetKey());
    doReturn(Optional.empty()).when(assetService).getAsset(assetKey, FIELD_LOOKUP_PURPOSE);

    var conditions = workAreaFilterService.getConditions(filter, user, null);

    assertThat(conditions).containsExactly(SUBMITTED_APPLICATION_CONDITION);
  }

  @Test
  void getConditions_FieldSelected() {
    when(teamService.isRegulatorUser(user)).thenReturn(true);

    form.setAssetKey(ApplicationDataFilterFormTestUtil.FIELD1_ASSET_KEY);
    filter.update(form);

    var assetKey = AssetKey.from(form.getAssetKey());
    doReturn(Optional.of(field1AssetJson)).when(assetService).getAsset(assetKey, FIELD_LOOKUP_PURPOSE);

    var conditions = workAreaFilterService.getConditions(filter, user, null);

    assertThat(conditions).containsExactly(
        SUBMITTED_APPLICATION_CONDITION,
        APPLICATION_ASSETS.FIELD_ID.eq(FIELD_ID_1)
    );
  }

  @Test
  void getConditions_TerminalSelected() {
    when(teamService.isRegulatorUser(user)).thenReturn(true);

    form.setAssetKey(ApplicationDataFilterFormTestUtil.TERMINAL1_ASSET_KEY);
    filter.update(form);

    var assetKey = AssetKey.from(form.getAssetKey());
    doReturn(Optional.of(terminal1AssetJson)).when(assetService).getAsset(assetKey, FIELD_LOOKUP_PURPOSE);

    var conditions = workAreaFilterService.getConditions(filter, user, null);

    assertThat(conditions).containsExactly(
        SUBMITTED_APPLICATION_CONDITION,
        APPLICATION_ASSETS.TERMINAL_ID.eq(TERMINAL_ID_1)
    );
  }

  @Test
  void getConditions_SeaLocationsSelected() {
    when(teamService.isRegulatorUser(user)).thenReturn(true);

    form.setGeographicAreas(List.of(GeographicArea.CNS, GeographicArea.SNS));
    filter.update(form);

    var distinctPrimaryFields = List.of(FIELD_ID_1, FIELD_ID_2);
    var primaryFieldJsonsInGeographicAreas = List.of(field1Json, field2Json);

    when(applicationFieldService.findDistinctPrimaryFieldIds()).thenReturn(distinctPrimaryFields);
    when(fieldService.findFieldsByIds(distinctPrimaryFields, FIELD_LOOKUP_PURPOSE))
        .thenReturn(primaryFieldJsonsInGeographicAreas);

    var conditions = workAreaFilterService.getConditions(filter, user, null);

    assertThat(conditions).containsExactly(
        SUBMITTED_APPLICATION_CONDITION,
        APPLICATION_ASSETS.FIELD_ID.in(distinctPrimaryFields)
    );
  }

  @Test
  void getConditions_twoGeographicAreas_conditionContainsOnlyThoseFieldsThatMatchTheFilter() {
    when(teamService.isRegulatorUser(user)).thenReturn(true);

    form.setGeographicAreas(List.of(GeographicArea.CNS, GeographicArea.SNS));
    filter.update(form);

    var distinctPrimaryFieldIds = List.of(FIELD_ID_1, FIELD_ID_2, FIELD_ID_3);
    var primaryFieldJsons = List.of(field1Json, field2Json, field3Json);
    when(applicationFieldService.findDistinctPrimaryFieldIds()).thenReturn(distinctPrimaryFieldIds);
    when(fieldService.findFieldsByIds(distinctPrimaryFieldIds, FIELD_LOOKUP_PURPOSE))
        .thenReturn(primaryFieldJsons);
    var fieldIdsInFilterGeographicAreas = List.of(FIELD_ID_1, FIELD_ID_2);

    var conditions = workAreaFilterService.getConditions(filter, user, null);

    assertThat(conditions).containsExactly(
        SUBMITTED_APPLICATION_CONDITION,
        APPLICATION_ASSETS.FIELD_ID.in(fieldIdsInFilterGeographicAreas)
    );
  }

  @Test
  void getConditions_MyApplicationsCaseOfficer() {
    when(teamService.isRegulatorUser(user)).thenReturn(true);

    var conditions = workAreaFilterService.getConditions(filter, user, WorkAreaTab.MY_APPLICATIONS);

    assertThat(conditions).containsExactly(
        SUBMITTED_APPLICATION_CONDITION,
        APPLICATION_VERSIONS.CASE_OFFICER_WUA_ID.eq(user.wuaId().intValue())
    );
  }

  @Test
  void getConditions_AllApplications() {
    when(teamService.isRegulatorUser(user)).thenReturn(true);

    var conditions = workAreaFilterService.getConditions(filter, user, WorkAreaTab.ALL_APPLICATIONS);

    assertThat(conditions).containsExactly(
        SUBMITTED_APPLICATION_CONDITION,
        DSL.trueCondition()
    );
  }

  @Test
  void getConditions_MyConsultations() {
    when(teamService.isRegulatorUser(user)).thenReturn(true);

    var conditions = workAreaFilterService.getConditions(filter, user, WorkAreaTab.MY_CONSULTATIONS);

    assertThat(conditions).containsExactly(
        SUBMITTED_APPLICATION_CONDITION,
        APPLICATION_CONSULTATIONS.RESPONDER_WUA_ID.eq(user.wuaId().intValue())
    );
  }

  @Test
  void getConditions_MyTechnicalReviews() {
    when(teamService.isRegulatorUser(user)).thenReturn(true);

    var conditions = workAreaFilterService.getConditions(filter, user, WorkAreaTab.MY_TECHNICAL_REVIEWS);

    assertThat(conditions).containsExactly(
        SUBMITTED_APPLICATION_CONDITION,
        APPLICATION_TECHNICAL_REVIEWS.TECHNICAL_REVIEWER_WUA_ID.eq(user.wuaId().intValue())
    );
  }

  @Test
  void getConditions_AllTechnicalReviews() {
    when(teamService.isRegulatorUser(user)).thenReturn(true);

    var conditions = workAreaFilterService.getConditions(filter, user, WorkAreaTab.ALL_TECHNICAL_REVIEWS);

    assertThat(conditions).containsExactly(
        SUBMITTED_APPLICATION_CONDITION,
        APPLICATION_TECHNICAL_REVIEWS.TECHNICAL_REVIEWER_WUA_ID.isNotNull()
    );
  }

  @Test
  void getConditions_UnassignedCaseOfficer() {
    when(teamService.isRegulatorUser(user)).thenReturn(true);

    var conditions = workAreaFilterService.getConditions(filter, user, WorkAreaTab.UNASSIGNED_APPLICATIONS);

    assertThat(conditions).containsExactly(
        SUBMITTED_APPLICATION_CONDITION,
        APPLICATION_VERSIONS.CASE_OFFICER_WUA_ID.isNull()
    );
  }

  @Test
  void getConditions_AllConsultations() {
    when(teamService.isRegulatorUser(user)).thenReturn(true);

    var conditions = workAreaFilterService.getConditions(filter, user, WorkAreaTab.ALL_CONSULTATIONS);

    assertThat(conditions).containsExactly(
        SUBMITTED_APPLICATION_CONDITION,
        APPLICATION_CONSULTATIONS.CONSULTATION_TEAM_ID.isNotNull()
    );
  }

  @Test
  void getConditions_UnassignedConsultations() {
    when(teamService.isConsulteeUser(user)).thenReturn(true);

    var conditions = workAreaFilterService.getConditions(filter, user, WorkAreaTab.UNASSIGNED_CONSULTATIONS);

    assertThat(conditions).containsExactly(
        SUBMITTED_APPLICATION_CONDITION,
        APPLICATION_CONSULTATIONS.RESPONDER_WUA_ID.isNull().and(APPLICATION_CONSULTATIONS.STATUS.eq(ConsultationStatus.OPEN.name()))
    );
  }

  @Test
  void getConditions_RegulatorApplicationStatusCondition() {
    when(teamService.isRegulatorUser(user)).thenReturn(true);

    var conditions = workAreaFilterService.getConditions(filter, user, WorkAreaTab.MY_APPLICATIONS);

    assertThat(conditions).containsExactly(
        SUBMITTED_APPLICATION_CONDITION,
        APPLICATION_VERSIONS.CASE_OFFICER_WUA_ID.eq(user.wuaId().intValue())
    );
  }

  @Test
  void getConditions_ConsulteeApplicationStatusCondition() {
    when(teamService.isConsulteeUser(user)).thenReturn(true);

    var conditions = workAreaFilterService.getConditions(filter, user, WorkAreaTab.ALL_CONSULTATIONS);

    assertThat(conditions).containsExactly(
        SUBMITTED_APPLICATION_CONDITION,
        APPLICATION_CONSULTATIONS.CONSULTATION_TEAM_ID.isNotNull()
    );
  }

  @Test
  void getConditions_IndustryApplicationStatusCondition() {
    when(teamService.isIndustryUser(user)).thenReturn(true);

    var conditions = workAreaFilterService.getConditions(filter, user, null);

    assertThat(conditions).containsExactly(
        APPLICATION_VERSIONS.STATUS.in(
            ApplicationVersionStatus.IN_PROGRESS.name(),
            ApplicationVersionStatus.AWAITING_PAYMENT.name(),
            ApplicationVersionStatus.SUBMITTED.name()
        )
    );
  }

  @Test
  void getDefaultFilter_forIndustryUser() {
    var serviceUser = ServiceUserDetailTestUtil.Builder()
        .withWuaId(USER_WUA_ID)
        .build();
    when(teamService.isIndustryUser(serviceUser)).thenReturn(true);
    var expectedStatuses = List.of(
        ApplicationVersionStatus.IN_PROGRESS,
        ApplicationVersionStatus.AWAITING_PAYMENT,
        ApplicationVersionStatus.SUBMITTED
    );
    var expectedApplicationTypes = List.of(ApplicationType.PRODUCTION, ApplicationType.FLARE, ApplicationType.VENT);

    var workAreaFilter = workAreaFilterService.getDefaultFilter(serviceUser);

    assertThat(workAreaFilter.getStatuses()).isEqualTo(expectedStatuses);
    assertThat(workAreaFilter.getApplicationTypes()).isEqualTo(expectedApplicationTypes);

    assertNonDefaultFilter(workAreaFilter);
  }

  @Test
  void getDefaultFilter_forNonIndustryUser() {
    var serviceUser = ServiceUserDetailTestUtil.Builder().withWuaId(USER_WUA_ID).build();
    when(teamService.isIndustryUser(serviceUser)).thenReturn(false);
    var expectedApplicationTypes = List.of(ApplicationType.PRODUCTION, ApplicationType.FLARE, ApplicationType.VENT);

    var workAreaFilter = workAreaFilterService.getDefaultFilter(serviceUser);

    assertThat(workAreaFilter.getStatuses()).isNull();
    assertThat(workAreaFilter.getApplicationTypes()).isEqualTo(expectedApplicationTypes);

    assertNonDefaultFilter(workAreaFilter);
  }

  @Test
  void getConditions_caseOfficerAssignedCondition() {
    when(teamService.isRegulatorUser(user)).thenReturn(true);

    form.setCaseOfficerWuaId(123L);
    filter.update(form);

    var conditions = workAreaFilterService.getConditions(filter, user, null);

    assertThat(conditions).containsExactly(
        SUBMITTED_APPLICATION_CONDITION,
        APPLICATION_VERSIONS.CASE_OFFICER_WUA_ID.eq(123)
    );
  }

  @Test
  void getConditions_technicalReviewerAssignedCondition() {
    when(teamService.isRegulatorUser(user)).thenReturn(true);

    form.setTechnicalReviewerWuaId(123L);
    filter.update(form);

    var conditions = workAreaFilterService.getConditions(filter, user, null);

    assertThat(conditions).containsExactly(
        SUBMITTED_APPLICATION_CONDITION,
        APPLICATION_TECHNICAL_REVIEWS.TECHNICAL_REVIEWER_WUA_ID.eq(123)
    );
  }

  private void assertNonDefaultFilter(WorkAreaFilter workAreaFilter) {
    assertThat(workAreaFilter.getReferenceNumber()).isNull();
    assertThat(workAreaFilter.getDurationTypes()).isNull();
    assertThat(workAreaFilter.assetKey).isNull();
    assertThat(workAreaFilter.getOperatorId()).isNull();
    assertThat(workAreaFilter.getGeographicAreas()).isNull();
    assertThat(workAreaFilter.getAssetTypesWithShore()).isNull();
  }
}
