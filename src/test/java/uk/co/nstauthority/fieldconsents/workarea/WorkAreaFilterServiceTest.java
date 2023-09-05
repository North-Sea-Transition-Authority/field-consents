package uk.co.nstauthority.fieldconsents.workarea;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.USER_WUA_ID;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.FIELD_ID_1;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.FIELD_ID_2;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.FIELD_ID_3;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1Json;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field2Json;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field3Json;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.TERMINAL_ID_1;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.TERMINAL_ID_2;
import static uk.co.nstauthority.fieldconsents.generated.jooq.Tables.APPLICATION_CONSULTATIONS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationAssets.APPLICATION_ASSETS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationTechnicalReviews.APPLICATION_TECHNICAL_REVIEWS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationVersions.APPLICATION_VERSIONS;
import static uk.co.nstauthority.fieldconsents.workarea.WorkAreaFilterService.FIELD_LOOKUP_PURPOSE;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationFieldService;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationTerminalService;
import uk.co.nstauthority.fieldconsents.assets.AssetService;
import uk.co.nstauthority.fieldconsents.assets.AssetTestUtil;
import uk.co.nstauthority.fieldconsents.assets.AssetTypeWithShore;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.assets.fields.GeographicArea;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterFormTestUtil;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterService;
import uk.co.nstauthority.fieldconsents.teams.TeamService;

@ExtendWith(MockitoExtension.class)
class WorkAreaFilterServiceTest {

  @Mock
  private AssetService assetService;

  @Mock
  private FieldService fieldService;

  @Mock
  private ApplicationFieldService applicationFieldService;

  @Mock
  private ApplicationTerminalService applicationTerminalService;

  @Mock
  private TeamService teamService;

  @Mock
  private ApplicationDataFilterService applicationDataFilterService;

  private WorkAreaFilterService workAreaFilterService;

  private WorkAreaFilter filter;

  private WorkAreaFilterForm form;

  private ServiceUserDetail user;

  @BeforeEach
  void setup() {
    workAreaFilterService = new WorkAreaFilterService(
        assetService,
        fieldService,
        applicationFieldService,
        applicationTerminalService,
        teamService,
        applicationDataFilterService);

    user = ServiceUserDetailTestUtil.Builder().build();
    filter = new WorkAreaFilter();
    form = new WorkAreaFilterForm();
  }

  @Test
  void getConditions_EmptyFilter_AssertEmpty() {
    var conditions = workAreaFilterService.getConditions(filter, user, null);
    assertThat(conditions).isEmpty();
  }

  @Test
  void getConditions_AssetNotFound() {
    form.setAssetKey(ApplicationDataFilterFormTestUtil.FIELD_ASSET_KEY);
    filter.update(form);

    when(assetService.getAssetFromKey(filter.getAssetKey())).thenReturn(Optional.empty());
    var conditions = workAreaFilterService.getConditions(filter, user, null);

    assertThat(conditions).isEmpty();
  }

  @Test
  void getConditions_FieldSelected() {
    form.setAssetKey(ApplicationDataFilterFormTestUtil.FIELD_ASSET_KEY);
    filter.update(form);

    when(assetService.getAssetFromKey(filter.getAssetKey())).thenReturn(Optional.of(AssetTestUtil.field1AssetJson));
    var conditions = workAreaFilterService.getConditions(filter, user, null);

    assertThat(conditions).containsExactly(
        APPLICATION_ASSETS.FIELD_ID.eq(FIELD_ID_1)
    );
  }

  @Test
  void getConditions_TerminalSelected() {
    form.setAssetKey(ApplicationDataFilterFormTestUtil.TERMINAL_ASSET_KEY);
    filter.update(form);

    when(assetService.getAssetFromKey(filter.getAssetKey())).thenReturn(Optional.of(AssetTestUtil.terminal1AssetJson));
    var conditions = workAreaFilterService.getConditions(filter, user, null);

    assertThat(conditions).containsExactly(
        APPLICATION_ASSETS.TERMINAL_ID.eq(TERMINAL_ID_1)
    );
  }

  @Test
  void getConditions_SeaLocationsSelected() {
    form.setGeographicAreas(List.of(GeographicArea.CNS, GeographicArea.SNS));
    filter.update(form);

    var distinctPrimaryFields = List.of(FIELD_ID_1, FIELD_ID_2);
    var primaryFieldJsonsInGeographicAreas = List.of(field1Json, field2Json);

    when(applicationFieldService.findDistinctPrimaryFieldIds()).thenReturn(distinctPrimaryFields);
    when(fieldService.findFieldsByIds(distinctPrimaryFields, FIELD_LOOKUP_PURPOSE))
        .thenReturn(primaryFieldJsonsInGeographicAreas);

    var conditions = workAreaFilterService.getConditions(filter, user, null);

    assertThat(conditions).containsExactly(APPLICATION_ASSETS.FIELD_ID.in(distinctPrimaryFields));
  }

  @Test
  void getConditions_twoGeographicAreas_conditionContainsOnlyThoseFieldsThatMatchTheFilter() {
    form.setGeographicAreas(List.of(GeographicArea.CNS, GeographicArea.SNS));
    filter.update(form);

    var distinctPrimaryFieldIds = List.of(FIELD_ID_1, FIELD_ID_2, FIELD_ID_3);
    var primaryFieldJsons = List.of(field1Json, field2Json, field3Json);
    when(applicationFieldService.findDistinctPrimaryFieldIds()).thenReturn(distinctPrimaryFieldIds);
    when(fieldService.findFieldsByIds(distinctPrimaryFieldIds, FIELD_LOOKUP_PURPOSE))
        .thenReturn(primaryFieldJsons);
    var fieldIdsInFilterGeographicAreas = List.of(FIELD_ID_1, FIELD_ID_2);

    var conditions = workAreaFilterService.getConditions(filter, user, null);

    assertThat(conditions).containsExactly(APPLICATION_ASSETS.FIELD_ID.in(fieldIdsInFilterGeographicAreas));
  }

  @Test
  void getConditions_AssetTypesSelected_containsFieldsOnly() {
    form.setAssetTypesWithShore(List.of(AssetTypeWithShore.FIELD_OFFSHORE, AssetTypeWithShore.FIELD_ONSHORE));
    filter.update(form);

    var distinctPrimaryFieldIds = List.of(FIELD_ID_1, FIELD_ID_2);
    var primaryFieldJsonsOfShoreTypes = List.of(field1Json, field2Json);

    when(applicationFieldService.findDistinctPrimaryFieldIds()).thenReturn(distinctPrimaryFieldIds);
    when(fieldService.findFieldsByIds(distinctPrimaryFieldIds, FIELD_LOOKUP_PURPOSE))
        .thenReturn(primaryFieldJsonsOfShoreTypes);

    var conditions = workAreaFilterService.getConditions(filter, user, null);

    assertThat(conditions).containsExactly(APPLICATION_ASSETS.FIELD_ID.in(distinctPrimaryFieldIds));
  }

  @Test
  void getConditions_AssetTypesSelected_conditionContainsOnlyThoseFieldsThatMatchTheFilter() {
    form.setAssetTypesWithShore(List.of(AssetTypeWithShore.FIELD_OFFSHORE, AssetTypeWithShore.FIELD_ONSHORE));
    filter.update(form);

    var distinctPrimaryFieldIds = List.of(FIELD_ID_1, FIELD_ID_2, FIELD_ID_3);
    var primaryFieldJsons = List.of(field1Json, field2Json, field3Json);

    when(applicationFieldService.findDistinctPrimaryFieldIds()).thenReturn(distinctPrimaryFieldIds);
    when(fieldService.findFieldsByIds(distinctPrimaryFieldIds, FIELD_LOOKUP_PURPOSE))
        .thenReturn(primaryFieldJsons);

    var fieldIdsInFilterShoreTypes = List.of(FIELD_ID_1, FIELD_ID_2);
    var conditions = workAreaFilterService.getConditions(filter, user, null);

    assertThat(conditions).containsExactly(APPLICATION_ASSETS.FIELD_ID.in(fieldIdsInFilterShoreTypes));
  }

  @Test
  void getConditions_AssetTypesSelected_containsTerminalsOnly() {
    form.setAssetTypesWithShore(List.of(AssetTypeWithShore.TERMINAL));
    filter.update(form);

    var distinctPrimaryTerminalIds = List.of(TERMINAL_ID_1, TERMINAL_ID_2);
    when(applicationTerminalService.findDistinctPrimaryTerminalIds()).thenReturn(distinctPrimaryTerminalIds);

    var conditions = workAreaFilterService.getConditions(filter, user, null);

    assertThat(conditions).containsExactly(APPLICATION_ASSETS.TERMINAL_ID.in(distinctPrimaryTerminalIds));
  }

  @Test
  void getConditions_AssetTypesSelected_containsFieldsAndTerminals() {
    form.setAssetTypesWithShore(List.of(AssetTypeWithShore.FIELD_OFFSHORE, AssetTypeWithShore.FIELD_ONSHORE, AssetTypeWithShore.TERMINAL));
    filter.update(form);

    var distinctPrimaryFieldIds = List.of(FIELD_ID_1, FIELD_ID_2);
    var primaryFieldJsonsOfShoreTypes = List.of(field1Json, field2Json);

    when(applicationFieldService.findDistinctPrimaryFieldIds()).thenReturn(distinctPrimaryFieldIds);
    when(fieldService.findFieldsByIds(distinctPrimaryFieldIds, FIELD_LOOKUP_PURPOSE))
        .thenReturn(primaryFieldJsonsOfShoreTypes);

    var distinctPrimaryTerminalIds = List.of(TERMINAL_ID_1, TERMINAL_ID_2);
    when(applicationTerminalService.findDistinctPrimaryTerminalIds()).thenReturn(distinctPrimaryTerminalIds);

    var conditions = workAreaFilterService.getConditions(filter, user, null);

    assertThat(conditions).containsExactly(APPLICATION_ASSETS.FIELD_ID.in(distinctPrimaryFieldIds)
        .or(APPLICATION_ASSETS.TERMINAL_ID.in(distinctPrimaryTerminalIds)));
  }

  @Test
  void getConditions_MyApplicationsCaseOfficer() {
    var conditions = workAreaFilterService.getConditions(filter, user, WorkAreaTab.MY_APPLICATIONS);

    assertThat(conditions).containsExactly(
        APPLICATION_VERSIONS.CASE_OFFICER_WUA_ID.eq(user.wuaId().intValue())
    );
  }

  @Test
  void getConditions_MyTechnicalReviews() {
    var conditions = workAreaFilterService.getConditions(filter, user, WorkAreaTab.MY_TECHNICAL_REVIEWS);

    assertThat(conditions).containsExactly(
        APPLICATION_TECHNICAL_REVIEWS.TECHNICAL_REVIEWER_WUA_ID.eq(user.wuaId().intValue())
    );
  }

  @Test
  void getConditions_AllTechnicalReviews() {
    var conditions = workAreaFilterService.getConditions(filter, user, WorkAreaTab.ALL_TECHNICAL_REVIEWS);

    assertThat(conditions).containsExactly(
        APPLICATION_TECHNICAL_REVIEWS.TECHNICAL_REVIEWER_WUA_ID.isNotNull()
    );
  }

  @Test
  void getConditions_UnassignedCaseOfficer() {
    var conditions = workAreaFilterService.getConditions(filter, user, WorkAreaTab.UNASSIGNED_APPLICATIONS);

    assertThat(conditions).containsExactly(
        APPLICATION_VERSIONS.CASE_OFFICER_WUA_ID.isNull()
    );
  }

  @Test
  void getConditions_AllConsultations() {
    var conditions = workAreaFilterService.getConditions(filter, user, WorkAreaTab.ALL_CONSULTATIONS);

    assertThat(conditions).containsExactly(
        APPLICATION_CONSULTATIONS.CONSULTATION_TEAM_ID.isNotNull()
    );
  }

  // TODO FCS-394 this needs changing when allocation is done
  @Test
  void getConditions_UnassignedConsultations() {
    var conditions = workAreaFilterService.getConditions(filter, user, WorkAreaTab.UNASSIGNED_CONSULTATIONS);

    assertThat(conditions).containsExactly(
        APPLICATION_CONSULTATIONS.CONSULTATION_TEAM_ID.isNotNull()
    );
  }

  @Test
  void getConditions_RegulatorApplicationStatusCondition() {
    when(teamService.isRegulatorUser(user)).thenReturn(true);

    var conditions = workAreaFilterService.getConditions(filter, user, WorkAreaTab.MY_APPLICATIONS);

    assertThat(conditions).contains(
        APPLICATION_VERSIONS.STATUS.eq(ApplicationVersionStatus.SUBMITTED.name())
    );
  }

  @Test
  void getConditions_ConsulteeApplicationStatusCondition() {
    when(teamService.isConsulteeUser(user)).thenReturn(true);

    var conditions = workAreaFilterService.getConditions(filter, user, WorkAreaTab.ALL_CONSULTATIONS);

    assertThat(conditions).contains(
        APPLICATION_VERSIONS.STATUS.eq(ApplicationVersionStatus.SUBMITTED.name())
    );
  }

  @Test
  void getConditions_IndustryApplicationStatusCondition() {
    when(teamService.isIndustryUser(user)).thenReturn(true);

    var conditions = workAreaFilterService.getConditions(filter, user, null);

    assertThat(conditions).containsExactly(
        APPLICATION_VERSIONS.STATUS.in(
            ApplicationVersionStatus.IN_PROGRESS.name(),
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
    var expectedStatuses = List.of(ApplicationVersionStatus.IN_PROGRESS, ApplicationVersionStatus.SUBMITTED);
    var expectedApplicationTypes = List.of(ApplicationType.PRODUCTION, ApplicationType.FLARE, ApplicationType.VENT);

    var workAreaFilter = workAreaFilterService.getDefaultFilter(serviceUser);

    assertThat(workAreaFilter.getStatuses()).isEqualTo(expectedStatuses);
    assertThat(workAreaFilter.getApplicationTypes()).isEqualTo(expectedApplicationTypes);

    assertNonDefaultFilter(workAreaFilter);
  }

  @Test
  void getDefaultFilter_forNonIndustryUser() {
    var serviceUser = ServiceUserDetailTestUtil.Builder()
        .withWuaId(USER_WUA_ID)
        .build();
    when(teamService.isIndustryUser(serviceUser)).thenReturn(false);
    var expectedApplicationTypes = List.of(ApplicationType.PRODUCTION, ApplicationType.FLARE, ApplicationType.VENT);

    var workAreaFilter = workAreaFilterService.getDefaultFilter(serviceUser);

    assertThat(workAreaFilter.getStatuses()).isNull();
    assertThat(workAreaFilter.getApplicationTypes()).isEqualTo(expectedApplicationTypes);

    assertNonDefaultFilter(workAreaFilter);
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
