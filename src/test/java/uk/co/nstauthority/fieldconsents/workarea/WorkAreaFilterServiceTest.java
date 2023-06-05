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
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationAssets.APPLICATION_ASSETS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationVersions.APPLICATION_VERSIONS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.Applications.APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ConsentLengths.CONSENT_LENGTHS;
import static uk.co.nstauthority.fieldconsents.workarea.WorkAreaFormService.FIELD_LOOKUP_PURPOSE;
import static uk.co.nstauthority.fieldconsents.workarea.WorkAreaFormServiceTestUtil.APPLICATION_NO;
import static uk.co.nstauthority.fieldconsents.workarea.WorkAreaFormServiceTestUtil.ORGANISATION_UNIT_ID;

import java.util.Collections;
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
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.assets.AssetService;
import uk.co.nstauthority.fieldconsents.assets.AssetTestUtil;
import uk.co.nstauthority.fieldconsents.assets.AssetTypeWithShore;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.assets.fields.GeographicArea;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
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

  private WorkAreaFilterService workAreaFilterService;

  private WorkAreaFilter filter;
  private WorkAreaForm form;

  private ServiceUserDetail user;

  @BeforeEach
  void setup() {
    workAreaFilterService = new WorkAreaFilterService(
        assetService,
        fieldService,
        applicationFieldService,
        applicationTerminalService,
        teamService
    );

    user = ServiceUserDetailTestUtil.Builder().build();
    filter = new WorkAreaFilter();
    form = new WorkAreaForm();
  }

  @Test
  void getConditions_EmptyFilter_AssertEmpty() {
    var conditions = workAreaFilterService.getConditions(filter, user, null);
    assertThat(conditions).isEmpty();
  }

  @Test
  void getConditions_StatusesSelected() {
    var status = ApplicationVersionStatus.IN_PROGRESS;
    form.setStatuses(Collections.singletonList(status));
    filter.update(form);

    var conditions = workAreaFilterService.getConditions(filter, user, null);

    assertThat(conditions).containsExactly(
        APPLICATION_VERSIONS.STATUS.in(Collections.singletonList(status.getEnumName()))
    );
  }

  @Test
  void getConditions_ApplicationTypesSelected() {
    var applicationType = ApplicationType.PRODUCTION;
    form.setApplicationTypes(Collections.singletonList(applicationType));
    filter.update(form);

    var conditions = workAreaFilterService.getConditions(filter, user, null);

    assertThat(conditions).containsExactly(
        APPLICATIONS.TYPE.in(Collections.singletonList(applicationType.getEnumName()))
    );
  }

  @Test
  void getConditions_DurationTypesSelected() {
    var durationTypes = ConsentLengthType.LONG_TERM;
    form.setDurationTypes(Collections.singletonList(durationTypes));
    filter.update(form);

    var conditions = workAreaFilterService.getConditions(filter, user, null);

    assertThat(conditions).containsExactly(
        CONSENT_LENGTHS.CONSENT_LENGTH.in(Collections.singletonList(durationTypes.getEnumName()))
    );
  }

  @Test
  void getConditions_AssetNotFound() {
    form.setAssetKey(WorkAreaFormServiceTestUtil.FIELD_ASSET_KEY);
    filter.update(form);

    when(assetService.getAssetFromKey(filter.getAssetKey())).thenReturn(Optional.empty());
    var conditions = workAreaFilterService.getConditions(filter, user, null);

    assertThat(conditions).isEmpty();
  }

  @Test
  void getConditions_FieldSelected() {
    form.setAssetKey(WorkAreaFormServiceTestUtil.FIELD_ASSET_KEY);
    filter.update(form);

    when(assetService.getAssetFromKey(filter.getAssetKey())).thenReturn(Optional.of(AssetTestUtil.field1AssetJson));
    var conditions = workAreaFilterService.getConditions(filter, user, null);

    assertThat(conditions).containsExactly(
        APPLICATION_ASSETS.FIELD_ID.eq(FIELD_ID_1)
    );
  }

  @Test
  void getConditions_TerminalSelected() {
    form.setAssetKey(WorkAreaFormServiceTestUtil.TERMINAL_ASSET_KEY);
    filter.update(form);

    when(assetService.getAssetFromKey(filter.getAssetKey())).thenReturn(Optional.of(AssetTestUtil.terminal1AssetJson));
    var conditions = workAreaFilterService.getConditions(filter, user, null);

    assertThat(conditions).containsExactly(
        APPLICATION_ASSETS.TERMINAL_ID.eq(TERMINAL_ID_1)
    );
  }

  @Test
  void getConditions_OperatorSelected() {
    form.setOperatorId(ORGANISATION_UNIT_ID);
    filter.update(form);

    var conditions = workAreaFilterService.getConditions(filter, user, null);

    assertThat(conditions).containsExactly(
        APPLICATION_VERSIONS.PRIMARY_OPERATOR_OU_ID.eq(ORGANISATION_UNIT_ID)
    );
  }

  @Test
  void getConditions_ReferenceNumberSelected() {
    form.setReferenceNumber(APPLICATION_NO);
    filter.update(form);

    var conditions = workAreaFilterService.getConditions(filter, user, null);

    assertThat(conditions).containsExactly(
        APPLICATIONS.APPLICATION_NO.cast(String.class).eq(APPLICATION_NO)
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
  void getConditions_UnassignedCaseOfficer() {
    var conditions = workAreaFilterService.getConditions(filter, user, WorkAreaTab.UNASSIGNED_APPLICATIONS);

    assertThat(conditions).containsExactly(
        APPLICATION_VERSIONS.CASE_OFFICER_WUA_ID.isNull()
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
  void getConditions_IndustryApplicationStatusCondition() {
    when(teamService.isIndustryUser(user)).thenReturn(true);

    var conditions = workAreaFilterService.getConditions(filter, user, null);

    assertThat(conditions).containsExactly(
        APPLICATION_VERSIONS.STATUS.notEqual(ApplicationVersionStatus.COMPLETED.name())
    );
  }

  @Test
  void getDefaultFilter_forIndustryUser() {
    var serviceUser = ServiceUserDetailTestUtil.Builder()
        .withWuaId(USER_WUA_ID)
        .build();
    when(teamService.isRegulatorUser(serviceUser)).thenReturn(false);
    var expectedStatuses = List.of(ApplicationVersionStatus.IN_PROGRESS, ApplicationVersionStatus.SUBMITTED);
    var expectedApplicationTypes = List.of(ApplicationType.PRODUCTION, ApplicationType.FLARE, ApplicationType.VENT);

    var workAreaFilter = workAreaFilterService.getDefaultFilter(serviceUser);

    assertThat(workAreaFilter.statuses).isEqualTo(expectedStatuses);
    assertThat(workAreaFilter.applicationTypes).isEqualTo(expectedApplicationTypes);

    assertNonDefaultFilter(workAreaFilter);
  }

  @Test
  void getDefaultFilter_forRegulatorUser() {
    var serviceUser = ServiceUserDetailTestUtil.Builder()
        .withWuaId(USER_WUA_ID)
        .build();
    when(teamService.isRegulatorUser(serviceUser)).thenReturn(true);
    var expectedApplicationTypes = List.of(ApplicationType.PRODUCTION, ApplicationType.FLARE, ApplicationType.VENT);

    var workAreaFilter = workAreaFilterService.getDefaultFilter(serviceUser);

    assertThat(workAreaFilter.statuses).isNull();
    assertThat(workAreaFilter.applicationTypes).isEqualTo(expectedApplicationTypes);

    assertNonDefaultFilter(workAreaFilter);
  }

  private void assertNonDefaultFilter(WorkAreaFilter workAreaFilter) {
    assertThat(workAreaFilter.referenceNumber).isNull();
    assertThat(workAreaFilter.durationTypes).isNull();
    assertThat(workAreaFilter.assetKey).isNull();
    assertThat(workAreaFilter.operatorId).isNull();
    assertThat(workAreaFilter.geographicAreas).isNull();
    assertThat(workAreaFilter.assetTypesWithShore).isNull();
  }
}
