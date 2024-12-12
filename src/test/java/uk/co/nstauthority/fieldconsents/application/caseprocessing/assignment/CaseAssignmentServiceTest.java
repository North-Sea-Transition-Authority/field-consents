package uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.USER_WUA_ID;
import static uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus.SUBMITTED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.ENERGY_PORTAL_USER_1;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.ENERGY_PORTAL_USER_2;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.ENERGY_PORTAL_USER_3;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment.CaseAssignmentService.USER_NOT_IN_CASE_OFFICER_ROLE;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityGroup.REGULATOR;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityReason.CASE_OFFICER_ASSIGN_OWNERSHIP;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityReason.CASE_OFFICER_RELEASE_OWNERSHIP;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityReason.CASE_OFFICER_TAKE_OWNERSHIP;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionRepository;
import uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.email.FieldConsentsEmailRecipient;
import uk.co.nstauthority.fieldconsents.email.GovukNotifyTemplate;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.teams.Role;
import uk.co.nstauthority.fieldconsents.teams.Team;
import uk.co.nstauthority.fieldconsents.teams.TeamQueryService;
import uk.co.nstauthority.fieldconsents.teams.TeamRole;
import uk.co.nstauthority.fieldconsents.teams.TeamRoleTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@ExtendWith(MockitoExtension.class)
class CaseAssignmentServiceTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().withWuaId(1L).build();
  private static final ServiceUserDetail USER2 = ServiceUserDetailTestUtil.Builder().withWuaId(2L).build();
  private static final WebUserAccountId WEB_USER_ACCOUNT_ID = WebUserAccountId.from(USER);
  private static final Team REGULATOR_TEAM = TeamTestUtil.newBuilder().build();

  @Mock
  private ApplicationVersionRepository applicationVersionRepository;

  @Mock
  private ApplicationWorkAreaPriorityService applicationWorkAreaPriorityService;

  @Mock
  private EnergyPortalUserService energyPortalUserService;

  @Mock
  private CaseAssignmentEmailService caseAssignmentEmailService;

  @Mock
  private TeamQueryService teamQueryService;

  @InjectMocks
  private CaseAssignmentService caseAssignmentService;

  private ApplicationVersion applicationVersion;

  private Set<WebUserAccountId> allCaseOfficersWuaIds;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    allCaseOfficersWuaIds = new HashSet<>();
    allCaseOfficersWuaIds.add(WebUserAccountId.from(1L));
    allCaseOfficersWuaIds.add(WebUserAccountId.from(2L));
  }

  @Test
  void assignCaseOfficer_whenNotInCaseOfficerRole_thenThrowException() {
    when(teamQueryService.userHasStaticRole(USER, TeamType.REGULATOR, Role.CASE_OFFICER))
        .thenReturn(false);

    assertThatThrownBy(() ->
        caseAssignmentService.assignCaseOfficer(applicationVersion, USER, USER))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(USER_NOT_IN_CASE_OFFICER_ROLE.apply(String.valueOf(USER.wuaId())));
  }

  @Test
  void assignCaseOfficer_whenInCaseOfficerRoleAndTakingOwnership_thenApplicationVersionCaseOfficerWuaUpdated() {
    when(teamQueryService.userHasStaticRole(USER, TeamType.REGULATOR, Role.CASE_OFFICER))
        .thenReturn(true);

    caseAssignmentService.assignCaseOfficer(applicationVersion, USER, USER);

    var applicationVersionArgumentCaptor = ArgumentCaptor.forClass(ApplicationVersion.class);

    verify(applicationVersionRepository).save(applicationVersionArgumentCaptor.capture());

    assertThat(applicationVersionArgumentCaptor.getValue().getCaseOfficerWuaId())
        .isEqualTo(WEB_USER_ACCOUNT_ID.id());

    verify(applicationWorkAreaPriorityService).prioritiseApplicationInWorkArea(applicationVersion, USER, CASE_OFFICER_TAKE_OWNERSHIP, REGULATOR);

    verifyNoInteractions(caseAssignmentEmailService);
  }

  @Test
  void assignCaseOfficer_whenAssigneeInCaseOfficerRoleAndBeingAssignedOwnership_thenApplicationVersionCaseOfficerWuaUpdated() {
    when(teamQueryService.userHasStaticRole(USER, TeamType.REGULATOR, Role.CASE_OFFICER))
        .thenReturn(true);

    caseAssignmentService.assignCaseOfficer(applicationVersion, USER, USER2);

    var applicationVersionArgumentCaptor = ArgumentCaptor.forClass(ApplicationVersion.class);

    verify(applicationVersionRepository).save(applicationVersionArgumentCaptor.capture());

    var actualApplicationVersion = applicationVersionArgumentCaptor.getValue();

    assertThat(actualApplicationVersion.getCaseOfficerWuaId()).isEqualTo(WEB_USER_ACCOUNT_ID.id());
    assertThat(actualApplicationVersion.getCurrentCaseOwner()).isEqualTo(Role.CASE_OFFICER);

    verify(applicationWorkAreaPriorityService).prioritiseApplicationInWorkArea(applicationVersion, USER2, CASE_OFFICER_ASSIGN_OWNERSHIP, REGULATOR);

    verify(caseAssignmentEmailService).sendCaseAssignmentEmail(
        applicationVersion,
        GovukNotifyTemplate.CASE_ASSIGNED_TO_CASE_OFFICER,
        FieldConsentsEmailRecipient.from(USER),
        USER2);
  }

  @Test
  void assignCaseOfficer_whenSendCaseAssignmentEmailFails_thenApplicationVersionCaseOfficerWuaIsStillUpdated() {
    when(teamQueryService.userHasStaticRole(USER, TeamType.REGULATOR, Role.CASE_OFFICER))
        .thenReturn(true);

    // WHEN the email service call throws an exception
    doThrow(new RuntimeException("Failed to send email"))
        .when(caseAssignmentEmailService)
        .sendCaseAssignmentEmail(
            applicationVersion,
            GovukNotifyTemplate.CASE_ASSIGNED_TO_CASE_OFFICER,
            FieldConsentsEmailRecipient.from(USER),
            USER2);

    // THEN it will be caught by the caller and not re-thrown
    assertDoesNotThrow(
        () -> caseAssignmentService.assignCaseOfficer(applicationVersion, USER, USER2)
    );

    var applicationVersionArgumentCaptor = ArgumentCaptor.forClass(ApplicationVersion.class);

    verify(applicationVersionRepository).save(applicationVersionArgumentCaptor.capture());

    var actualApplicationVersion = applicationVersionArgumentCaptor.getValue();

    assertThat(actualApplicationVersion.getCaseOfficerWuaId()).isEqualTo(WEB_USER_ACCOUNT_ID.id());
    assertThat(actualApplicationVersion.getCurrentCaseOwner()).isEqualTo(Role.CASE_OFFICER);

    verify(applicationWorkAreaPriorityService).prioritiseApplicationInWorkArea(applicationVersion, USER2, CASE_OFFICER_ASSIGN_OWNERSHIP, REGULATOR);

    verify(caseAssignmentEmailService).sendCaseAssignmentEmail(
        applicationVersion,
        GovukNotifyTemplate.CASE_ASSIGNED_TO_CASE_OFFICER,
        FieldConsentsEmailRecipient.from(USER),
        USER2);
  }

  @Test
  void unassignCaseOfficer_thenApplicationVersionCaseOfficerWuaNulled() {
    applicationVersion.setCaseOfficerWuaId(1L);
    caseAssignmentService.unassignCaseOfficer(applicationVersion, USER);

    var applicationVersionArgumentCaptor = ArgumentCaptor.forClass(ApplicationVersion.class);

    verify(applicationVersionRepository).save(applicationVersionArgumentCaptor.capture());

    var actualApplicationVersion = applicationVersionArgumentCaptor.getValue();

    assertThat(actualApplicationVersion.getCaseOfficerWuaId()).isNull();
    assertThat(actualApplicationVersion.getCurrentCaseOwner()).isNull();

    verify(applicationWorkAreaPriorityService).prioritiseApplicationInWorkArea(applicationVersion, USER, CASE_OFFICER_RELEASE_OWNERSHIP, REGULATOR);

    verify(caseAssignmentEmailService).sendCaseOwnershipReleasedEmail(applicationVersion, USER);
  }

  @Test
  void unassignCaseOfficer_whenSendCaseOwnershipReleasedEmailFails_thenApplicationVersionCaseOfficerWuaIsStillNulled() {
    applicationVersion.setCaseOfficerWuaId(1L);

    // WHEN the email service call throws an exception
    doThrow(new RuntimeException("Failed to send email"))
        .when(caseAssignmentEmailService)
        .sendCaseOwnershipReleasedEmail(applicationVersion, USER);

    // THEN it will be caught by the caller and not re-thrown
    assertDoesNotThrow(
        () -> caseAssignmentService.unassignCaseOfficer(applicationVersion, USER)
    );

    var applicationVersionArgumentCaptor = ArgumentCaptor.forClass(ApplicationVersion.class);

    verify(applicationVersionRepository).save(applicationVersionArgumentCaptor.capture());

    var actualApplicationVersion = applicationVersionArgumentCaptor.getValue();

    assertThat(actualApplicationVersion.getCaseOfficerWuaId()).isNull();
    assertThat(actualApplicationVersion.getCurrentCaseOwner()).isNull();

    verify(applicationWorkAreaPriorityService).prioritiseApplicationInWorkArea(applicationVersion, USER, CASE_OFFICER_RELEASE_OWNERSHIP, REGULATOR);

    verify(caseAssignmentEmailService).sendCaseOwnershipReleasedEmail(applicationVersion, USER);
  }

  @Test
  void isCaseOfficerAssigned() {
    var applicationVersion = new ApplicationVersion();
    applicationVersion.setCaseOfficerWuaId(123L);
    applicationVersion.setCurrentCaseOwner(Role.CASE_OFFICER);

    assertThat(caseAssignmentService.isCaseOfficerAssigned(applicationVersion)).isTrue();
  }

  @Test
  void isCaseOfficerAssigned_noWuaId() {
    var applicationVersion = new ApplicationVersion();
    applicationVersion.setCaseOfficerWuaId(null);
    applicationVersion.setCurrentCaseOwner(Role.CASE_OFFICER);

    assertThat(caseAssignmentService.isCaseOfficerAssigned(applicationVersion)).isFalse();
  }

  @ParameterizedTest
  @EnumSource(value = Role.class, names = "CASE_OFFICER", mode= EnumSource.Mode.EXCLUDE)
  void isCaseOfficerAssigned_currentCaseOwnerNotCaseOfficer(Role regulatorTeamRole) {
    var applicationVersion = new ApplicationVersion();
    applicationVersion.setCaseOfficerWuaId(123L);
    applicationVersion.setCurrentCaseOwner(regulatorTeamRole);

    assertThat(caseAssignmentService.isCaseOfficerAssigned(applicationVersion)).isFalse();
  }

  @Test
  void isCamAssigned() {
    var applicationVersion = new ApplicationVersion();
    applicationVersion.setCamWuaId(123L);
    applicationVersion.setCurrentCaseOwner(Role.CONSENTS_AND_AUTHORISATIONS_MANAGER);

    assertThat(caseAssignmentService.isCamAssigned(applicationVersion)).isTrue();
  }

  @Test
  void isCamAssigned_noWuaId() {
    var applicationVersion = new ApplicationVersion();
    applicationVersion.setCamWuaId(null);
    applicationVersion.setCurrentCaseOwner(Role.CONSENTS_AND_AUTHORISATIONS_MANAGER);

    assertThat(caseAssignmentService.isCamAssigned(applicationVersion)).isFalse();
  }

  @ParameterizedTest
  @EnumSource(value = Role.class, names = "CONSENTS_AND_AUTHORISATIONS_MANAGER", mode= EnumSource.Mode.EXCLUDE)
  void isCaseOfficerAssigned_currentCaseOwnerNotCam(Role regulatorTeamRole) {
    var applicationVersion = new ApplicationVersion();
    applicationVersion.setCamWuaId(123L);
    applicationVersion.setCurrentCaseOwner(regulatorTeamRole);

    assertThat(caseAssignmentService.isCamAssigned(applicationVersion)).isFalse();
  }

  @Test
  void getCaseOfficerCandidates_whenRegulatorUserAndCaseOfficersDontExist_thenEmpty() {
    when(teamQueryService.getStaticTeamRoles(USER, TeamType.REGULATOR)).thenReturn(List.of());
    assertThat(caseAssignmentService.getCaseOfficerAssignmentCandidates(applicationVersion, USER)).isEmpty();
  }

  @Test
  void getCurrentCaseOfficers_whenUserNoRegulatorInCaseOfficerRole() {
    var caseOfficerWuaIds = List.of(1L, 2L, 3L);
    var caseOfficerWebUserAccountIds = caseOfficerWuaIds
        .stream()
        .map(WebUserAccountId::new)
        .collect(Collectors.toSet());

    when(teamQueryService.getTeamRoles(TeamType.REGULATOR)).thenReturn(List.of());
    when(applicationVersionRepository.findAllCaseOfficerWuaIdsByApplicationVersionStatus(SUBMITTED)).thenReturn(caseOfficerWuaIds);
    when(energyPortalUserService.findByWuaIds(caseOfficerWebUserAccountIds)).thenReturn(List.of(ENERGY_PORTAL_USER_1, ENERGY_PORTAL_USER_2));

    assertThat(caseAssignmentService.getCurrentCaseOfficers())
        .containsExactly(
            ENERGY_PORTAL_USER_1,
            ENERGY_PORTAL_USER_2
        );
  }

  @Test
  void getCurrentCaseOfficers_withNoCurrentNorPreviouslyAssignedCaseOfficersAvailable() {
    when(teamQueryService.getTeamRoles(TeamType.REGULATOR)).thenReturn(List.of());
    when(applicationVersionRepository.findAllCaseOfficerWuaIdsByApplicationVersionStatus(SUBMITTED)).thenReturn(Collections.emptyList());

    assertThat(caseAssignmentService.getCurrentCaseOfficers()).isEmpty();
  }

  @Test
  void getCurrentCaseOfficers_withCurrentCaseOfficersButNoneAssigned() {
    var teamRoles = List.of(
        TeamRoleTestUtil.newBuilder()
            .withWuaId(ENERGY_PORTAL_USER_1.webUserAccountId())
            .withTeam(REGULATOR_TEAM)
            .withRole(Role.CASE_OFFICER)
            .build(),
        TeamRoleTestUtil.newBuilder()
            .withWuaId(ENERGY_PORTAL_USER_2.webUserAccountId())
            .withTeam(REGULATOR_TEAM)
            .withRole(Role.CASE_OFFICER)
            .build()
    );

    when(teamQueryService.getTeamRoles(TeamType.REGULATOR)).thenReturn(teamRoles);
    when(applicationVersionRepository.findAllCaseOfficerWuaIdsByApplicationVersionStatus(SUBMITTED)).thenReturn(List.of());
    when(energyPortalUserService.findByWuaIds(allCaseOfficersWuaIds)).thenReturn(List.of(ENERGY_PORTAL_USER_1, ENERGY_PORTAL_USER_2));

    assertThat(caseAssignmentService.getCurrentCaseOfficers())
        .containsExactly(
            ENERGY_PORTAL_USER_1,
            ENERGY_PORTAL_USER_2
        );
  }

  @Test
  void getCurrentCaseOfficers_withPreviouslyAssignedCaseOfficers() {
    var teamRoles = List.of(
        TeamRoleTestUtil.newBuilder()
            .withWuaId(ENERGY_PORTAL_USER_1.webUserAccountId())
            .withTeam(REGULATOR_TEAM)
            .withRole(Role.CASE_OFFICER)
            .build(),
        TeamRoleTestUtil.newBuilder()
            .withWuaId(ENERGY_PORTAL_USER_2.webUserAccountId())
            .withTeam(REGULATOR_TEAM)
            .withRole(Role.CASE_OFFICER)
            .build()
    );

    when(teamQueryService.getTeamRoles(TeamType.REGULATOR)).thenReturn(teamRoles);
    when(applicationVersionRepository.findAllCaseOfficerWuaIdsByApplicationVersionStatus(SUBMITTED)).thenReturn(List.of(3L));

    allCaseOfficersWuaIds.add(WebUserAccountId.from(3L));
    when(energyPortalUserService.findByWuaIds(allCaseOfficersWuaIds)).thenReturn(List.of(ENERGY_PORTAL_USER_1, ENERGY_PORTAL_USER_2, ENERGY_PORTAL_USER_3));

    assertThat(caseAssignmentService.getCurrentCaseOfficers())
        .containsExactly(
            ENERGY_PORTAL_USER_1,
            ENERGY_PORTAL_USER_2,
            ENERGY_PORTAL_USER_3
        );
  }

  @Test
  void getActiveCaseOfficers() {
    var caseOfficers = List.of(ENERGY_PORTAL_USER_1, ENERGY_PORTAL_USER_2, ENERGY_PORTAL_USER_3);

    var teamRoles = List.of(
        TeamRoleTestUtil.newBuilder()
            .withWuaId(ENERGY_PORTAL_USER_1.webUserAccountId())
            .withTeam(REGULATOR_TEAM)
            .withRole(Role.CASE_OFFICER)
            .build(),
        TeamRoleTestUtil.newBuilder()
            .withWuaId(ENERGY_PORTAL_USER_2.webUserAccountId())
            .withTeam(REGULATOR_TEAM)
            .withRole(Role.CASE_OFFICER)
            .build(),
        TeamRoleTestUtil.newBuilder()
            .withWuaId(ENERGY_PORTAL_USER_3.webUserAccountId())
            .withTeam(REGULATOR_TEAM)
            .withRole(Role.CASE_OFFICER)
            .build()
    );

    var caseOfficerWebUserAccountIds = teamRoles.stream()
        .map(TeamRole::getWuaId)
        .map(WebUserAccountId::from)
        .toList();

    when(teamQueryService.getTeamRoles(TeamType.REGULATOR)).thenReturn(teamRoles);
    when(energyPortalUserService.findByWuaIds(caseOfficerWebUserAccountIds)).thenReturn(caseOfficers);

    assertThat(caseAssignmentService.getActiveCaseOfficers()).isEqualTo(caseOfficers);
  }

  @Test
  void returnToCaseOfficer_thenApplicationVersionCamWuaIdNulled() {
    applicationVersion.setCaseOfficerWuaId(WEB_USER_ACCOUNT_ID.id());

    caseAssignmentService.returnToCaseOfficer(applicationVersion, USER2);

    var applicationVersionArgumentCaptor = ArgumentCaptor.forClass(ApplicationVersion.class);

    verify(applicationVersionRepository).save(applicationVersionArgumentCaptor.capture());

    var actualApplicationVersion = applicationVersionArgumentCaptor.getValue();

    assertThat(actualApplicationVersion.getCamWuaId()).isNull();
    assertThat(actualApplicationVersion.getCurrentCaseOwner()).isEqualTo(Role.CASE_OFFICER);

    verify(applicationWorkAreaPriorityService).prioritiseApplicationInWorkArea(applicationVersion, USER2, CASE_OFFICER_ASSIGN_OWNERSHIP, REGULATOR);

    verify(caseAssignmentEmailService).sendCaseReturnedToCaseOfficerByCamEmail(applicationVersion, USER2);
  }

  @Test
  void returnToCaseOfficer_whenSendCaseReturnedToCaseOfficerByCamEmail_thenApplicationVersionCamWuaIdIsStillNulled() {
    applicationVersion.setCaseOfficerWuaId(WEB_USER_ACCOUNT_ID.id());

    // WHEN the email service call throws an exception
    doThrow(new RuntimeException("Failed to send email"))
        .when(caseAssignmentEmailService)
        .sendCaseReturnedToCaseOfficerByCamEmail(applicationVersion, USER2);

    // THEN it will be caught by the caller and not re-thrown
    assertDoesNotThrow(
        () -> caseAssignmentService.returnToCaseOfficer(applicationVersion, USER2)
    );

    var applicationVersionArgumentCaptor = ArgumentCaptor.forClass(ApplicationVersion.class);

    verify(applicationVersionRepository).save(applicationVersionArgumentCaptor.capture());

    var actualApplicationVersion = applicationVersionArgumentCaptor.getValue();

    assertThat(actualApplicationVersion.getCamWuaId()).isNull();
    assertThat(actualApplicationVersion.getCurrentCaseOwner()).isEqualTo(Role.CASE_OFFICER);

    verify(applicationWorkAreaPriorityService).prioritiseApplicationInWorkArea(applicationVersion, USER2, CASE_OFFICER_ASSIGN_OWNERSHIP, REGULATOR);

    verify(caseAssignmentEmailService).sendCaseReturnedToCaseOfficerByCamEmail(applicationVersion, USER2);
  }

  @Test
  void findCaseOfficerWuaId_whenNotAssigned() {
    assertThat(caseAssignmentService.findCaseOfficerWuaId(applicationVersion))
        .isEmpty();
  }

  @Test
  void findCaseOfficerWuaId_whenAssigned() {
    applicationVersion.setCaseOfficerWuaId(USER_WUA_ID);
    assertThat(caseAssignmentService.findCaseOfficerWuaId(applicationVersion))
        .contains(WebUserAccountId.from(USER_WUA_ID));
  }
}
