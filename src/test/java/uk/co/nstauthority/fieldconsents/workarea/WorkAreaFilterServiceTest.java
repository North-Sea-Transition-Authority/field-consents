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
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.TERMINAL_ID_1;
import static uk.co.nstauthority.fieldconsents.generated.jooq.Tables.APPLICATION_CONSULTATIONS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationAssets.APPLICATION_ASSETS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationConsentIssuingApprovals.APPLICATION_CONSENT_ISSUING_APPROVALS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationTechnicalReviews.APPLICATION_TECHNICAL_REVIEWS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationVersions.APPLICATION_VERSIONS;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataItemViewQueryService.APPLICATION_CONSULTATIONS_QUERY;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataItemViewQueryService.APPLICATION_TECHNICAL_REVIEWS_QUERY;

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
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationStatus;
import uk.co.nstauthority.fieldconsents.assets.AssetKey;
import uk.co.nstauthority.fieldconsents.assets.AssetService;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.assets.fields.GeographicArea;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterFormTestUtil;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterService;
import uk.co.nstauthority.fieldconsents.teams.Role;
import uk.co.nstauthority.fieldconsents.teams.TeamQueryService;
import uk.co.nstauthority.fieldconsents.teams.TeamRoleTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@ExtendWith(MockitoExtension.class)
class WorkAreaFilterServiceTest {

  private static final Condition SUBMITTED_APPLICATION_CONDITION = APPLICATION_VERSIONS.STATUS.eq(ApplicationVersionStatus.SUBMITTED.name());

  @Mock
  private AssetService assetService;

  @Mock
  private ApplicationDataFilterService applicationDataFilterService;

  @Mock
  private TeamQueryService teamQueryService;

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
    var teamRoles = List.of(
        TeamRoleTestUtil.newBuilder()
            .withTeam(TeamTestUtil.newBuilder()
                .withTeamType(TeamType.REGULATOR)
                .build())
            .build()
    );

    when(teamQueryService.getTeamRoles(user)).thenReturn(teamRoles);
    when(applicationDataFilterService.getSubmittedApplicationStatusCondition()).thenReturn(SUBMITTED_APPLICATION_CONDITION);

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
    var teamRoles = List.of(
        TeamRoleTestUtil.newBuilder()
            .withTeam(TeamTestUtil.newBuilder()
                .withTeamType(TeamType.REGULATOR)
                .build())
            .build()
    );

    when(teamQueryService.getTeamRoles(user)).thenReturn(teamRoles);
    when(applicationDataFilterService.getSubmittedApplicationStatusCondition()).thenReturn(SUBMITTED_APPLICATION_CONDITION);

    var conditions = workAreaFilterService.getConditions(filter, user, null);

    assertThat(conditions).containsExactly(SUBMITTED_APPLICATION_CONDITION);
  }

  @Test
  void getConditions_EmptyFilter_Consultee() {
    var teamRoles = List.of(
        TeamRoleTestUtil.newBuilder()
            .withTeam(TeamTestUtil.newBuilder()
                .withTeamType(TeamType.CONSULTEE)
                .build())
            .build()
    );

    when(teamQueryService.getTeamRoles(user)).thenReturn(teamRoles);
    when(applicationDataFilterService.getSubmittedApplicationStatusCondition()).thenReturn(SUBMITTED_APPLICATION_CONDITION);

    var conditions = workAreaFilterService.getConditions(filter, user, null);

    assertThat(conditions).containsExactly(SUBMITTED_APPLICATION_CONDITION);
  }

  @Test
  void getConditions_EmptyFilter_Industry() {
    var teamRoles = List.of(
        TeamRoleTestUtil.newBuilder()
            .withTeam(TeamTestUtil.newBuilder()
                .withTeamType(TeamType.INDUSTRY)
                .build())
            .build()
    );

    when(teamQueryService.getTeamRoles(user)).thenReturn(teamRoles);

    var conditions = workAreaFilterService.getConditions(filter, user, null);

    assertThat(conditions).containsExactly(APPLICATION_VERSIONS.STATUS.in(
        ApplicationVersionStatus.IN_PROGRESS.name(),
        ApplicationVersionStatus.AWAITING_PAYMENT.name(),
        ApplicationVersionStatus.SUBMITTED.name()
    ));
  }

  @Test
  void getConditions_AssetNotFound() {
    var teamRoles = List.of(
        TeamRoleTestUtil.newBuilder()
            .withTeam(TeamTestUtil.newBuilder()
                .withTeamType(TeamType.REGULATOR)
                .build())
            .build()
    );

    when(teamQueryService.getTeamRoles(user)).thenReturn(teamRoles);
    when(applicationDataFilterService.getSubmittedApplicationStatusCondition()).thenReturn(SUBMITTED_APPLICATION_CONDITION);

    form.setAssetKey(ApplicationDataFilterFormTestUtil.FIELD1_ASSET_KEY);
    filter.update(form);

    var assetKey = AssetKey.from(form.getAssetKey());
    doReturn(Optional.empty()).when(assetService).findAsset(assetKey);

    var conditions = workAreaFilterService.getConditions(filter, user, null);

    assertThat(conditions).containsExactly(SUBMITTED_APPLICATION_CONDITION);
  }

  @Test
  void getConditions_FieldSelected() {
    var teamRoles = List.of(
        TeamRoleTestUtil.newBuilder()
            .withTeam(TeamTestUtil.newBuilder()
                .withTeamType(TeamType.REGULATOR)
                .build())
            .build()
    );

    when(teamQueryService.getTeamRoles(user)).thenReturn(teamRoles);
    when(applicationDataFilterService.getSubmittedApplicationStatusCondition()).thenReturn(SUBMITTED_APPLICATION_CONDITION);

    form.setAssetKey(ApplicationDataFilterFormTestUtil.FIELD1_ASSET_KEY);
    filter.update(form);

    var assetKey = AssetKey.from(form.getAssetKey());
    doReturn(Optional.of(field1AssetJson)).when(assetService).findAsset(assetKey);

    var conditions = workAreaFilterService.getConditions(filter, user, null);

    assertThat(conditions).containsExactly(
        SUBMITTED_APPLICATION_CONDITION,
        APPLICATION_ASSETS.ASSET_TYPE.eq(AssetType.FIELD.name())
            .and(APPLICATION_ASSETS.ASSET_ID.eq(FIELD_ID_1))
    );
  }

  @Test
  void getConditions_TerminalSelected() {
    var teamRoles = List.of(
        TeamRoleTestUtil.newBuilder()
            .withTeam(TeamTestUtil.newBuilder()
                .withTeamType(TeamType.REGULATOR)
                .build())
            .build()
    );

    when(teamQueryService.getTeamRoles(user)).thenReturn(teamRoles);
    when(applicationDataFilterService.getSubmittedApplicationStatusCondition()).thenReturn(SUBMITTED_APPLICATION_CONDITION);

    form.setAssetKey(ApplicationDataFilterFormTestUtil.TERMINAL1_ASSET_KEY);
    filter.update(form);

    var assetKey = AssetKey.from(form.getAssetKey());
    doReturn(Optional.of(terminal1AssetJson)).when(assetService).findAsset(assetKey);

    var conditions = workAreaFilterService.getConditions(filter, user, null);

    assertThat(conditions).containsExactly(
        SUBMITTED_APPLICATION_CONDITION,
        APPLICATION_ASSETS.ASSET_TYPE.eq(AssetType.TERMINAL.name())
            .and(APPLICATION_ASSETS.ASSET_ID.eq(TERMINAL_ID_1))
    );
  }

  @Test
  void getConditions_SeaLocationsSelected() {
    var teamRoles = List.of(
        TeamRoleTestUtil.newBuilder()
            .withTeam(TeamTestUtil.newBuilder()
                .withTeamType(TeamType.REGULATOR)
                .build())
            .build()
    );

    when(teamQueryService.getTeamRoles(user)).thenReturn(teamRoles);
    when(applicationDataFilterService.getSubmittedApplicationStatusCondition()).thenReturn(SUBMITTED_APPLICATION_CONDITION);

    form.setGeographicAreas(List.of(GeographicArea.CNS, GeographicArea.SNS));
    filter.update(form);

    var geographicAreasCondition = mock(Condition.class);
    when(applicationDataFilterService.getGeographicAreasQueryCondition(form.getGeographicAreas()))
        .thenReturn(geographicAreasCondition);

    var conditions = workAreaFilterService.getConditions(filter, user, null);

    assertThat(conditions).containsExactly(SUBMITTED_APPLICATION_CONDITION, geographicAreasCondition);
  }

  @Test
  void getConditions_MyApplicationsCaseOfficer() {
    var teamRoles = List.of(
        TeamRoleTestUtil.newBuilder()
            .withTeam(TeamTestUtil.newBuilder()
                .withTeamType(TeamType.REGULATOR)
                .build())
            .build()
    );

    when(teamQueryService.getTeamRoles(user)).thenReturn(teamRoles);
    when(applicationDataFilterService.getSubmittedApplicationStatusCondition()).thenReturn(SUBMITTED_APPLICATION_CONDITION);

    var conditions = workAreaFilterService.getConditions(filter, user, WorkAreaTab.MY_APPLICATIONS);

    assertThat(conditions).containsExactly(
        SUBMITTED_APPLICATION_CONDITION,
        APPLICATION_VERSIONS.CASE_OFFICER_WUA_ID.eq(user.wuaId().intValue())
            .and(APPLICATION_VERSIONS.CURRENT_CASE_OWNER.eq(Role.CASE_OFFICER.name()))
    );
  }

  @Test
  void getConditions_AllApplications_whenCaseManager() {
    var teamRoles = List.of(
        TeamRoleTestUtil.newBuilder()
            .withTeam(TeamTestUtil.newBuilder()
                .withTeamType(TeamType.REGULATOR)
                .build())
            .build()
    );

    when(teamQueryService.getTeamRoles(user)).thenReturn(teamRoles);
    when(applicationDataFilterService.getSubmittedApplicationStatusCondition()).thenReturn(SUBMITTED_APPLICATION_CONDITION);

    var conditions = workAreaFilterService.getConditions(filter, user, WorkAreaTab.ALL_APPLICATIONS);

    assertThat(conditions).containsExactly(
        SUBMITTED_APPLICATION_CONDITION,
        DSL.trueCondition()
    );
  }

  @Test
  void getConditions_AllApplications_whenCamUser() {
    var teamRoles = List.of(
        TeamRoleTestUtil.newBuilder()
            .withTeam(TeamTestUtil.newBuilder()
                .withTeamType(TeamType.REGULATOR)
                .build())
            .build()
    );

    when(teamQueryService.getTeamRoles(user)).thenReturn(teamRoles);
    when(applicationDataFilterService.getSubmittedApplicationStatusCondition()).thenReturn(
        APPLICATION_VERSIONS.STATUS.eq(ApplicationVersionStatus.SUBMITTED.name())
    );

    var conditions = workAreaFilterService.getConditions(filter, user, WorkAreaTab.ALL_APPLICATIONS);

    assertThat(conditions).containsExactly(
        SUBMITTED_APPLICATION_CONDITION,
        DSL.trueCondition()
    );
  }

  @Test
  void getConditions_MyApplications_whenCamUser() {
    var teamRoles = List.of(
        TeamRoleTestUtil.newBuilder()
            .withTeam(TeamTestUtil.newBuilder()
                .withTeamType(TeamType.REGULATOR)
                .build())
            .build()
    );

    when(teamQueryService.getTeamRoles(user)).thenReturn(teamRoles);
    when(applicationDataFilterService.getSubmittedApplicationStatusCondition()).thenReturn(
        APPLICATION_VERSIONS.STATUS.eq(ApplicationVersionStatus.SUBMITTED.name())
    );

    var conditions = workAreaFilterService.getConditions(filter, user, WorkAreaTab.MY_CAM_APPLICATIONS);

    assertThat(conditions).containsExactly(
        SUBMITTED_APPLICATION_CONDITION,
        APPLICATION_VERSIONS.CAM_WUA_ID.eq(user.wuaId().intValue())
            .and(APPLICATION_VERSIONS.CURRENT_CASE_OWNER.eq(Role.CONSENTS_AND_AUTHORISATIONS_MANAGER.name()))
    );
  }

  @Test
  void getConditions_MyConsultations() {
    var teamRoles = List.of(
        TeamRoleTestUtil.newBuilder()
            .withTeam(TeamTestUtil.newBuilder()
                .withTeamType(TeamType.REGULATOR)
                .build())
            .build()
    );

    when(teamQueryService.getTeamRoles(user)).thenReturn(teamRoles);
    when(applicationDataFilterService.getSubmittedApplicationStatusCondition()).thenReturn(SUBMITTED_APPLICATION_CONDITION);

    var conditions = workAreaFilterService.getConditions(filter, user, WorkAreaTab.MY_CONSULTATIONS);

    assertThat(conditions).containsExactly(
        SUBMITTED_APPLICATION_CONDITION,
        APPLICATION_CONSULTATIONS_QUERY.field(APPLICATION_CONSULTATIONS.RESPONDER_WUA_ID).eq(user.wuaId().intValue())
    );
  }

  @Test
  void getConditions_MyTechnicalReviews() {
    var teamRoles = List.of(
        TeamRoleTestUtil.newBuilder()
            .withTeam(TeamTestUtil.newBuilder()
                .withTeamType(TeamType.REGULATOR)
                .build())
            .build()
    );

    when(teamQueryService.getTeamRoles(user)).thenReturn(teamRoles);
    when(applicationDataFilterService.getSubmittedApplicationStatusCondition()).thenReturn(SUBMITTED_APPLICATION_CONDITION);

    var conditions = workAreaFilterService.getConditions(filter, user, WorkAreaTab.MY_TECHNICAL_REVIEWS);

    assertThat(conditions).containsExactly(
        SUBMITTED_APPLICATION_CONDITION,
        APPLICATION_TECHNICAL_REVIEWS_QUERY.field(APPLICATION_TECHNICAL_REVIEWS.TECHNICAL_REVIEWER_WUA_ID).eq(user.wuaId().intValue())
    );
  }

  @Test
  void getConditions_AllTechnicalReviews() {
    var teamRoles = List.of(
        TeamRoleTestUtil.newBuilder()
            .withTeam(TeamTestUtil.newBuilder()
                .withTeamType(TeamType.REGULATOR)
                .build())
            .build()
    );

    when(teamQueryService.getTeamRoles(user)).thenReturn(teamRoles);
    when(applicationDataFilterService.getSubmittedApplicationStatusCondition()).thenReturn(SUBMITTED_APPLICATION_CONDITION);

    var conditions = workAreaFilterService.getConditions(filter, user, WorkAreaTab.ALL_TECHNICAL_REVIEWS);

    assertThat(conditions).containsExactly(
        SUBMITTED_APPLICATION_CONDITION,
        APPLICATION_TECHNICAL_REVIEWS_QUERY.field(APPLICATION_TECHNICAL_REVIEWS.TECHNICAL_REVIEWER_WUA_ID).isNotNull()
    );
  }

  @Test
  void getConditions_UnassignedCaseOfficer() {
    var teamRoles = List.of(
        TeamRoleTestUtil.newBuilder()
            .withTeam(TeamTestUtil.newBuilder()
                .withTeamType(TeamType.REGULATOR)
                .build())
            .build()
    );

    when(teamQueryService.getTeamRoles(user)).thenReturn(teamRoles);
    when(applicationDataFilterService.getSubmittedApplicationStatusCondition()).thenReturn(SUBMITTED_APPLICATION_CONDITION);

    var conditions = workAreaFilterService.getConditions(filter, user, WorkAreaTab.UNASSIGNED_APPLICATIONS);

    assertThat(conditions).containsExactly(
        SUBMITTED_APPLICATION_CONDITION,
        APPLICATION_VERSIONS.CASE_OFFICER_WUA_ID.isNull()
    );
  }

  @Test
  void getConditions_AllConsultations() {
    var teamRoles = List.of(
        TeamRoleTestUtil.newBuilder()
            .withTeam(TeamTestUtil.newBuilder()
                .withTeamType(TeamType.REGULATOR)
                .build())
            .build()
    );

    when(teamQueryService.getTeamRoles(user)).thenReturn(teamRoles);
    when(applicationDataFilterService.getSubmittedApplicationStatusCondition()).thenReturn(SUBMITTED_APPLICATION_CONDITION);

    var conditions = workAreaFilterService.getConditions(filter, user, WorkAreaTab.ALL_CONSULTATIONS);

    assertThat(conditions).containsExactly(
        SUBMITTED_APPLICATION_CONDITION,
        APPLICATION_CONSULTATIONS_QUERY.field(APPLICATION_CONSULTATIONS.CONSULTATION_TEAM_ID).isNotNull()
    );
  }

  @Test
  void getConditions_UnassignedConsultations() {
    var teamRoles = List.of(
        TeamRoleTestUtil.newBuilder()
            .withTeam(TeamTestUtil.newBuilder()
                .withTeamType(TeamType.REGULATOR)
                .build())
            .build()
    );

    when(teamQueryService.getTeamRoles(user)).thenReturn(teamRoles);
    when(applicationDataFilterService.getSubmittedApplicationStatusCondition()).thenReturn(SUBMITTED_APPLICATION_CONDITION);

    var conditions = workAreaFilterService.getConditions(filter, user, WorkAreaTab.UNASSIGNED_CONSULTATIONS);

    assertThat(conditions).containsExactly(
        SUBMITTED_APPLICATION_CONDITION,
        APPLICATION_CONSULTATIONS_QUERY.field(APPLICATION_CONSULTATIONS.RESPONDER_WUA_ID).isNull()
            .and(APPLICATION_CONSULTATIONS_QUERY.field(APPLICATION_CONSULTATIONS.STATUS).eq(ConsultationStatus.OPEN.name()))
    );
  }

  @Test
  void getConditions_ConsulteeApplicationStatusCondition() {
    var teamRoles = List.of(
        TeamRoleTestUtil.newBuilder()
            .withTeam(TeamTestUtil.newBuilder()
                .withTeamType(TeamType.CONSULTEE)
                .build())
            .build()
    );

    when(teamQueryService.getTeamRoles(user)).thenReturn(teamRoles);
    when(applicationDataFilterService.getSubmittedApplicationStatusCondition()).thenReturn(SUBMITTED_APPLICATION_CONDITION);

    var conditions = workAreaFilterService.getConditions(filter, user, WorkAreaTab.ALL_CONSULTATIONS);

    assertThat(conditions).containsExactly(
        SUBMITTED_APPLICATION_CONDITION,
        APPLICATION_CONSULTATIONS_QUERY.field(APPLICATION_CONSULTATIONS.CONSULTATION_TEAM_ID).isNotNull()
    );
  }

  @Test
  void getConditions_IndustryApplicationStatusCondition() {
    var teamRoles = List.of(
        TeamRoleTestUtil.newBuilder()
            .withTeam(TeamTestUtil.newBuilder()
                .withTeamType(TeamType.INDUSTRY)
                .build())
            .build()
    );

    when(teamQueryService.getTeamRoles(user)).thenReturn(teamRoles);

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

    when(teamQueryService.userIsMemberOfTeamType(user, TeamType.INDUSTRY)).thenReturn(true);

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

    when(teamQueryService.userIsMemberOfTeamType(user, TeamType.INDUSTRY)).thenReturn(false);

    var expectedApplicationTypes = List.of(ApplicationType.PRODUCTION, ApplicationType.FLARE, ApplicationType.VENT);

    var workAreaFilter = workAreaFilterService.getDefaultFilter(serviceUser);

    assertThat(workAreaFilter.getStatuses()).isNull();
    assertThat(workAreaFilter.getApplicationTypes()).isEqualTo(expectedApplicationTypes);

    assertNonDefaultFilter(workAreaFilter);
  }

  @Test
  void getConditions_caseOfficerAssignedCondition() {
    var teamRoles = List.of(
        TeamRoleTestUtil.newBuilder()
            .withTeam(TeamTestUtil.newBuilder()
                .withTeamType(TeamType.REGULATOR)
                .build())
            .build()
    );

    when(teamQueryService.getTeamRoles(user)).thenReturn(teamRoles);
    when(applicationDataFilterService.getSubmittedApplicationStatusCondition()).thenReturn(SUBMITTED_APPLICATION_CONDITION);

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
    var teamRoles = List.of(
        TeamRoleTestUtil.newBuilder()
            .withTeam(TeamTestUtil.newBuilder()
                .withTeamType(TeamType.REGULATOR)
                .build())
            .build()
    );

    when(teamQueryService.getTeamRoles(user)).thenReturn(teamRoles);
    when(applicationDataFilterService.getSubmittedApplicationStatusCondition()).thenReturn(SUBMITTED_APPLICATION_CONDITION);

    form.setTechnicalReviewerWuaId(123L);
    filter.update(form);

    var conditions = workAreaFilterService.getConditions(filter, user, null);

    assertThat(conditions).containsExactly(
        SUBMITTED_APPLICATION_CONDITION,
        APPLICATION_TECHNICAL_REVIEWS_QUERY.field(APPLICATION_TECHNICAL_REVIEWS.TECHNICAL_REVIEWER_WUA_ID).eq(123)
    );
  }

  @Test
  void getConditions_whenRegulatorWithApprovedForIssueConditionIsTrue_thenApprovedForIssueConditionIsAdded() {
    var teamRoles = List.of(
        TeamRoleTestUtil.newBuilder()
            .withTeam(TeamTestUtil.newBuilder()
                .withTeamType(TeamType.REGULATOR)
                .build())
            .build()
    );

    when(teamQueryService.getTeamRoles(user)).thenReturn(teamRoles);
    when(applicationDataFilterService.getSubmittedApplicationStatusCondition()).thenReturn(SUBMITTED_APPLICATION_CONDITION);
    when(applicationDataFilterService.getApprovedForIssueCondition()).thenReturn(APPLICATION_CONSENT_ISSUING_APPROVALS.ID.isNotNull());

    filter.setApprovedForIssue(true);

    var conditions = workAreaFilterService.getConditions(filter, user, null);

    assertThat(conditions).containsExactly(
        SUBMITTED_APPLICATION_CONDITION,
        APPLICATION_CONSENT_ISSUING_APPROVALS.ID.isNotNull());
  }

  @Test
  void getConditions_whenRegulatorWithApprovedForIssueConditionIsFalse_thenApprovedForIssueConditionIsNotAdded() {
    var teamRoles = List.of(
        TeamRoleTestUtil.newBuilder()
            .withTeam(TeamTestUtil.newBuilder()
                .withTeamType(TeamType.REGULATOR)
                .build())
            .build()
    );

    when(teamQueryService.getTeamRoles(user)).thenReturn(teamRoles);
    when(applicationDataFilterService.getSubmittedApplicationStatusCondition()).thenReturn(SUBMITTED_APPLICATION_CONDITION);

    filter.setApprovedForIssue(false);

    var conditions = workAreaFilterService.getConditions(filter, user, null);

    assertThat(conditions).containsExactly(SUBMITTED_APPLICATION_CONDITION);
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
