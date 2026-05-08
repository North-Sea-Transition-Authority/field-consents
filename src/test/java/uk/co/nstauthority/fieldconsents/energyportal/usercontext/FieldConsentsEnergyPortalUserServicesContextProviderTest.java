package uk.co.nstauthority.fieldconsents.energyportal.usercontext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit1;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit2;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.energyportal.starter.usercontext.VersionedUserContext;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.Consultation;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationStatus;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewResponseType;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewStatus;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewTestUtil;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateStatus;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateTestUtil;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.organisationgroup.OrganisationGroupQueryService;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitJson;
import uk.co.nstauthority.fieldconsents.teams.Role;
import uk.co.nstauthority.fieldconsents.teams.Team;
import uk.co.nstauthority.fieldconsents.teams.TeamQueryService;
import uk.co.nstauthority.fieldconsents.teams.TeamRoleTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@ExtendWith(MockitoExtension.class)
class FieldConsentsEnergyPortalUserServicesContextProviderTest {

  private static final Long USER_WUA_ID = 1L;
  private static final String SCOPE_ID_ONE = "100";
  private static final String SCOPE_ID_TWO = "101";
  private static final ServiceUserDetail SERVICE_USER_DETAIL = ServiceUserDetailTestUtil.Builder().build();
  private static final Team INDUSTRY_TEAM_ONE = TeamTestUtil.newBuilder().withTeamType(TeamType.INDUSTRY).withScopeId(SCOPE_ID_ONE).build();
  private static final Team INDUSTRY_TEAM_TWO = TeamTestUtil.newBuilder().withTeamType(TeamType.INDUSTRY).withScopeId(SCOPE_ID_TWO).build();
  private static final Team REGULATOR_TEAM = TeamTestUtil.newBuilder().withTeamType(TeamType.REGULATOR).build();
  private static final Team CONSULTEE_TEAM = TeamTestUtil.newBuilder().withTeamType(TeamType.CONSULTEE).build();
  private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-03-23T05:00:00.00Z"), ZoneId.of("UTC"));
  private static final OrganisationUnitJson ORG_UNIT_ONE = OrganisationUnitJson.from(orgUnit1);
  private static final OrganisationUnitJson ORG_UNIT_TWO = OrganisationUnitJson.from(orgUnit2);
  private static final ApplicationVersion APPLICATION_VERSION = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);

  @Mock
  private EnergyPortalUserService energyPortalUserService;

  @Mock
  private TeamQueryService teamQueryService;

  @Mock
  private ApplicationVersionService applicationVersionService;

  @Mock
  private TechnicalReviewService technicalReviewService;

  @Mock
  private ConsultationService consultationService;

  @Mock
  private OrganisationGroupQueryService organisationGroupQueryService;

  @Mock
  private ApplicationUpdateService applicationUpdateService;

  private FieldConsentsEnergyPortalUserServicesContextProvider contextProvider;

  @BeforeEach
  void setUp() {
    contextProvider = new FieldConsentsEnergyPortalUserServicesContextProvider(
        energyPortalUserService,
        teamQueryService,
        applicationVersionService,
        technicalReviewService,
        consultationService,
        organisationGroupQueryService,
        applicationUpdateService,
        CLOCK
    );

    when(energyPortalUserService.getServiceUserByWuaId(WebUserAccountId.from(USER_WUA_ID))).thenReturn(SERVICE_USER_DETAIL);
  }

  @Nested
  class IndustryUserContextTests {

    @BeforeEach
    void setUp() {
      when(teamQueryService.getTeamRoles(SERVICE_USER_DETAIL)).thenReturn(List.of(
          TeamRoleTestUtil.newBuilder()
            .withRole(Role.SUBMITTER)
            .withTeam(INDUSTRY_TEAM_ONE)
            .build(),
          TeamRoleTestUtil.newBuilder()
            .withRole(Role.FINANCE_ADMINISTRATOR)
            .withTeam(INDUSTRY_TEAM_TWO)
            .build()
      ));

      when(organisationGroupQueryService.getOrganisationUnitsByOrganisationGroupIds(anyList())).thenReturn(
          List.of(ORG_UNIT_ONE, ORG_UNIT_TWO)
      );
    }

    @Test
    void getUserContext_addApplicationsAwaitingPayment_noneAwaitingPayment() {
      when(applicationVersionService.countByPrimaryOperatorInAndStatus(List.of(
          ORG_UNIT_ONE.organisationUnitId(),
          ORG_UNIT_TWO.organisationUnitId()),
          ApplicationVersionStatus.AWAITING_PAYMENT))
          .thenReturn(
            0L
      );

      var expectedUserContext = VersionedUserContext.newBuilder().v1().build();

      assertThat(contextProvider.getUserContext(USER_WUA_ID)).isEqualTo(expectedUserContext);
    }

    @Test
    void getUserContext_addApplicationsAwaitingPayment() {
      when(applicationVersionService.countByPrimaryOperatorInAndStatus(List.of(
              ORG_UNIT_ONE.organisationUnitId(),
              ORG_UNIT_TWO.organisationUnitId()),
          ApplicationVersionStatus.AWAITING_PAYMENT))
          .thenReturn(
              1L
          );

      var expectedUserContext = VersionedUserContext.newBuilder().v1()
          .low(1, "application awaiting payment")
          .build();

      assertThat(contextProvider.getUserContext(USER_WUA_ID)).isEqualTo(expectedUserContext);
    }

    @Test
    void getUserContext_addUpdatesRequestedAndOverdue_noneDueSoonAndOverdue() {
      when(applicationUpdateService.getUpdatesByStatusAndPrimaryOperatorIds(
          ApplicationUpdateStatus.OPEN,
          List.of(
            ORG_UNIT_ONE.organisationUnitId(),
            ORG_UNIT_TWO.organisationUnitId()
      ))).thenReturn(
          List.of()
      );

      var expectedUserContext = VersionedUserContext.newBuilder().v1().build();

      assertThat(contextProvider.getUserContext(USER_WUA_ID)).isEqualTo(expectedUserContext);
    }

    @Test
    void getUserContext_addUpdatesRequestedAndOverdue() {
       var overdueUpdate = ApplicationUpdateTestUtil.getOpenApplicationUpdate(APPLICATION_VERSION, CLOCK);
       overdueUpdate.setDeadlineDateTime(Instant.now(CLOCK).minus(2, ChronoUnit.DAYS));

      when(applicationUpdateService.getUpdatesByStatusAndPrimaryOperatorIds(
          ApplicationUpdateStatus.OPEN,
          List.of(
              ORG_UNIT_ONE.organisationUnitId(),
              ORG_UNIT_TWO.organisationUnitId()
          ))).thenReturn(
          List.of(
              ApplicationUpdateTestUtil.getOpenApplicationUpdate(APPLICATION_VERSION, CLOCK),
              ApplicationUpdateTestUtil.getOpenApplicationUpdate(APPLICATION_VERSION, CLOCK),
              overdueUpdate
      ));

      var expectedUserContext = VersionedUserContext.newBuilder().v1()
          .low(2, "updates requested")
          .high(1, "update overdue")
          .build();

      assertThat(contextProvider.getUserContext(USER_WUA_ID)).isEqualTo(expectedUserContext);
    }
  }

  @Test
  void getUserContext_addApplicationsUnassignedToCaseOfficer_noneUnassigned() {
    when(teamQueryService.getTeamRoles(SERVICE_USER_DETAIL)).thenReturn(List.of(TeamRoleTestUtil.newBuilder()
        .withRole(Role.CASE_MANAGER)
        .withTeam(REGULATOR_TEAM)
        .build()
    ));

    when(applicationVersionService.countLatestSubmittedVersionsWithoutCaseOfficer()).thenReturn(0L);

    var expectedUserContext = VersionedUserContext.newBuilder().v1().build();

    assertThat(contextProvider.getUserContext(USER_WUA_ID)).isEqualTo(expectedUserContext);
  }

  @Test
  void getUserContext_addApplicationsUnassignedToCaseOfficer() {
    when(teamQueryService.getTeamRoles(SERVICE_USER_DETAIL)).thenReturn(List.of(TeamRoleTestUtil.newBuilder()
        .withRole(Role.CASE_MANAGER)
        .withTeam(REGULATOR_TEAM)
        .build()
    ));

    when(applicationVersionService.countLatestSubmittedVersionsWithoutCaseOfficer()).thenReturn(1L);

    var expectedUserContext = VersionedUserContext.newBuilder().v1()
        .low(1, "application awaiting assignment")
        .build();

    assertThat(contextProvider.getUserContext(USER_WUA_ID)).isEqualTo(expectedUserContext);
  }

  @Test
  void getUserContext_addTechnicalReviewsDueSoonAndOverdue_noneDueSoonAndOverdue() {
    var technicalReviewNotDueSoon = TechnicalReviewTestUtil.getOpenTechnicalReview(APPLICATION_VERSION);
    technicalReviewNotDueSoon.setDeadlineDateTime(Instant.now(CLOCK).plus(2, ChronoUnit.DAYS));

    when(teamQueryService.getTeamRoles(SERVICE_USER_DETAIL)).thenReturn(List.of(TeamRoleTestUtil.newBuilder()
        .withRole(Role.TECHNICAL_REVIEWER)
        .withTeam(REGULATOR_TEAM)
        .build()
    ));

    when(technicalReviewService.findTechnicalReviewsByReviewerAndStatus(SERVICE_USER_DETAIL, TechnicalReviewStatus.OPEN)).thenReturn(
        List.of(
            technicalReviewNotDueSoon,
            TechnicalReviewTestUtil.getClosedTechnicalReviewWithResponseType(APPLICATION_VERSION, TechnicalReviewResponseType.APPROVE)
        )
    );

    var expectedUserContext = VersionedUserContext.newBuilder().v1().build();

    assertThat(contextProvider.getUserContext(USER_WUA_ID)).isEqualTo(expectedUserContext);
  }

  @Test
  void getUserContext_addTechnicalReviewsDueSoonAndOverdue() {
    var technicalReviewOverdue = TechnicalReviewTestUtil.getOpenTechnicalReview(APPLICATION_VERSION);
    technicalReviewOverdue.setDeadlineDateTime(Instant.now(CLOCK).minus(2, ChronoUnit.DAYS));

    var technicalReviewDueSoon = TechnicalReviewTestUtil.getOpenTechnicalReview(APPLICATION_VERSION);
    technicalReviewDueSoon.setDeadlineDateTime(Instant.now(CLOCK).plus(5, ChronoUnit.HOURS));

    when(teamQueryService.getTeamRoles(SERVICE_USER_DETAIL)).thenReturn(List.of(TeamRoleTestUtil.newBuilder()
        .withRole(Role.TECHNICAL_REVIEWER)
        .withTeam(REGULATOR_TEAM)
        .build()
    ));

    when(technicalReviewService.findTechnicalReviewsByReviewerAndStatus(SERVICE_USER_DETAIL, TechnicalReviewStatus.OPEN)).thenReturn(
        List.of(
            technicalReviewOverdue,
            technicalReviewDueSoon
        )
    );

    var expectedUserContext = VersionedUserContext.newBuilder().v1()
        .low(1, "review due within 24 hours")
        .high(1, "review overdue")
        .build();

    assertThat(contextProvider.getUserContext(USER_WUA_ID)).isEqualTo(expectedUserContext);
  }

  @Test
  void getUserContext_addUnallocatedConsultations_noneUnallocated() {
    var assignedConsultation = new Consultation();
    assignedConsultation.setStatus(ConsultationStatus.OPEN);
    assignedConsultation.setResponderWuaId(2L);

    when(teamQueryService.getTeamRoles(SERVICE_USER_DETAIL)).thenReturn(List.of(TeamRoleTestUtil.newBuilder()
        .withRole(Role.ALLOCATOR)
        .withTeam(CONSULTEE_TEAM)
        .build()
    ));

    when(consultationService.findAllOpenConsultations()).thenReturn(List.of(
        assignedConsultation
    ));

    var expectedUserContext = VersionedUserContext.newBuilder().v1().build();

    assertThat(contextProvider.getUserContext(USER_WUA_ID)).isEqualTo(expectedUserContext);
  }

  @Test
  void getUserContext_addUnallocatedConsultations() {
    var unAssignedConsultation = new Consultation();
    unAssignedConsultation.setStatus(ConsultationStatus.OPEN);

    when(teamQueryService.getTeamRoles(SERVICE_USER_DETAIL)).thenReturn(List.of(TeamRoleTestUtil.newBuilder()
        .withRole(Role.ALLOCATOR)
        .withTeam(CONSULTEE_TEAM)
        .build()
    ));

    when(consultationService.findAllOpenConsultations()).thenReturn(List.of(
        unAssignedConsultation
    ));

    var expectedUserContext = VersionedUserContext.newBuilder().v1()
        .low(1, "consultation awaiting assignment")
        .build();

    assertThat(contextProvider.getUserContext(USER_WUA_ID)).isEqualTo(expectedUserContext);
  }

  @Test
  void getUserContext_addConsultationsDueSoonAndOverdue_noneDueSoonAndOverdue() {
    var consultationNotDueSoon = new Consultation();
    consultationNotDueSoon.setStatus(ConsultationStatus.OPEN);
    consultationNotDueSoon.setResponderWuaId(SERVICE_USER_DETAIL.wuaId());
    consultationNotDueSoon.setRequestDeadline(Instant.now(CLOCK).plus(2, ChronoUnit.DAYS));

    var differentResponderConsultation = new Consultation();
    differentResponderConsultation.setStatus(ConsultationStatus.CLOSED);
    differentResponderConsultation.setResponderWuaId(2L);
    differentResponderConsultation.setRequestDeadline(Instant.now(CLOCK).minus(2, ChronoUnit.DAYS));

    when(teamQueryService.getTeamRoles(SERVICE_USER_DETAIL)).thenReturn(List.of(TeamRoleTestUtil.newBuilder()
        .withRole(Role.RESPONDER)
        .withTeam(CONSULTEE_TEAM)
        .build()
    ));

    when(consultationService.findAllOpenConsultations()).thenReturn(List.of(
        consultationNotDueSoon,
        differentResponderConsultation
    ));

    var expectedUserContext = VersionedUserContext.newBuilder().v1().build();

    assertThat(contextProvider.getUserContext(USER_WUA_ID)).isEqualTo(expectedUserContext);
  }

  @Test
  void getUserContext_addConsultationsDueSoonAndOverdue() {
    var consultationDueSoon = new Consultation();
    consultationDueSoon.setStatus(ConsultationStatus.OPEN);
    consultationDueSoon.setResponderWuaId(SERVICE_USER_DETAIL.wuaId());
    consultationDueSoon.setRequestDeadline(Instant.now(CLOCK).plus(5, ChronoUnit.HOURS));

    var consultationOverdue = new Consultation();
    consultationOverdue.setStatus(ConsultationStatus.OPEN);
    consultationOverdue.setResponderWuaId(SERVICE_USER_DETAIL.wuaId());
    consultationOverdue.setRequestDeadline(Instant.now(CLOCK).minus(2, ChronoUnit.DAYS));

    when(teamQueryService.getTeamRoles(SERVICE_USER_DETAIL)).thenReturn(List.of(TeamRoleTestUtil.newBuilder()
        .withRole(Role.RESPONDER)
        .withTeam(CONSULTEE_TEAM)
        .build()
    ));

    when(consultationService.findAllOpenConsultations()).thenReturn(List.of(
        consultationDueSoon,
        consultationOverdue
    ));

    var expectedUserContext = VersionedUserContext.newBuilder().v1()
        .low(1, "consultation due within 24 hours")
        .high(1, "consultation overdue")
        .build();

    assertThat(contextProvider.getUserContext(USER_WUA_ID)).isEqualTo(expectedUserContext);
  }
}