package uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamRole.ACCESS_MANAGER;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamRole.CASE_OFFICER;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamRole.VIEWER;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionRepository;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.teams.Team;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberView;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberViewService;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberViewTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamTestUtil;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamService;

@ExtendWith(MockitoExtension.class)
class CaseAssignmentServiceTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  private static final WebUserAccountId WEB_USER_ACCOUNT_ID = WebUserAccountId.from(USER);

  private static final Team REGULATOR_TEAM = TeamTestUtil.Builder().build();

  private static final TeamMemberView CASE_OFFICER_TEAM_MEMBER_VIEW_1 =
      TeamMemberViewTestUtil.Builder()
          .withRole(CASE_OFFICER)
          .withWebUserAccountId(new WebUserAccountId(1L))
          .withTitle("MR")
          .withFirstName("A")
          .withLastName("B")
          .build();

  private static final TeamMemberView VIEWER_TEAM_MEMBER_VIEW =
      TeamMemberViewTestUtil.Builder()
          .withRole(VIEWER)
          .withWebUserAccountId(new WebUserAccountId(2L))
          .withTitle("MR")
          .withFirstName("C")
          .withLastName("D")
          .build();

  private static final TeamMemberView CASE_OFFICER_TEAM_MEMBER_VIEW_2 =
      TeamMemberViewTestUtil.Builder()
          .withRole(CASE_OFFICER)
          .withWebUserAccountId(new WebUserAccountId(3L))
          .withTitle("MR")
          .withFirstName("E")
          .withLastName("F")
          .build();

  private static final TeamMemberView ACCESS_MANGER_TEAM_MEMBER_VIEW =
      TeamMemberViewTestUtil.Builder()
          .withRole(ACCESS_MANAGER)
          .withWebUserAccountId(new WebUserAccountId(4L))
          .withTitle("MR")
          .withFirstName("G")
          .withLastName("H")
          .build();

  private static final List<TeamMemberView> TEAM_MEMBER_VIEW_LIST =
      List.of(CASE_OFFICER_TEAM_MEMBER_VIEW_1, VIEWER_TEAM_MEMBER_VIEW, CASE_OFFICER_TEAM_MEMBER_VIEW_2,
          ACCESS_MANGER_TEAM_MEMBER_VIEW);

  @Mock
  private ApplicationVersionRepository applicationVersionRepository;

  @Mock
  private RegulatorTeamService regulatorTeamService;

  @Mock
  private TeamMemberViewService teamMemberViewService;

  @InjectMocks
  private CaseAssignmentService caseAssignmentService;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
  }

  @Test
  void assignCaseOfficer_whenNotInCaseOfficerRole_thenThrowException() {
    when(regulatorTeamService.isCaseOfficer(WEB_USER_ACCOUNT_ID))
        .thenReturn(false);

    assertThatThrownBy(() -> caseAssignmentService.assignCaseOfficer(applicationVersion, WEB_USER_ACCOUNT_ID))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Cannot assign case officer as user with wua id %s is not in a regulator case officer role"
            .formatted(USER.wuaId()));
  }

  @Test
  void assignCaseOfficer_whenInCaseOfficerRole_thenApplicationVersionCaseOfficerWuaUpdated() {
    when(regulatorTeamService.isCaseOfficer(WEB_USER_ACCOUNT_ID))
        .thenReturn(true);

    caseAssignmentService.assignCaseOfficer(applicationVersion, WEB_USER_ACCOUNT_ID);

    var applicationVersionArgumentCaptor = ArgumentCaptor.forClass(ApplicationVersion.class);

    verify(applicationVersionRepository, times(1))
        .save(applicationVersionArgumentCaptor.capture());

    assertThat(applicationVersionArgumentCaptor.getValue().getCaseOfficerWuaId())
        .isEqualTo(WEB_USER_ACCOUNT_ID.id());
  }

  @Test
  void unassignCaseOfficer_thenApplicationVersionCaseOfficerWuaNulled() {
    applicationVersion.setCaseOfficerWuaId(1L);
    caseAssignmentService.unassignCaseOfficer(applicationVersion);

    var applicationVersionArgumentCaptor = ArgumentCaptor.forClass(ApplicationVersion.class);

    verify(applicationVersionRepository, times(1))
        .save(applicationVersionArgumentCaptor.capture());

    assertThat(applicationVersionArgumentCaptor.getValue().getCaseOfficerWuaId())
        .isNull();
  }

  @Test
  void getCaseOfficerCandidates_whenUserNotRegulatorTeam_thenEmpty() {
    when(regulatorTeamService.getRegulatorTeamForUser(USER))
        .thenReturn(Optional.empty());

    assertThat(caseAssignmentService.getCaseOfficerCandidates(USER))
        .isEmpty();
  }

  @Test
  void getCaseOfficerCandidates_whenRegulatorUserAndCaseOfficersExist() {
    when(regulatorTeamService.getRegulatorTeamForUser(USER))
        .thenReturn(Optional.of(REGULATOR_TEAM));
    when(teamMemberViewService.getTeamMemberViewsForTeam(REGULATOR_TEAM))
        .thenReturn(TEAM_MEMBER_VIEW_LIST);

    assertThat(caseAssignmentService.getCaseOfficerCandidates(USER))
        .containsExactly(
            entry(CASE_OFFICER_TEAM_MEMBER_VIEW_1.wuaId().toString(), CASE_OFFICER_TEAM_MEMBER_VIEW_1.getDisplayName()),
            entry(CASE_OFFICER_TEAM_MEMBER_VIEW_2.wuaId().toString(), CASE_OFFICER_TEAM_MEMBER_VIEW_2.getDisplayName())
        );
  }

  @Test
  void getCaseOfficerCandidates_whenRegulatorUserButNoMembersExist_thenEmpty() {
    when(regulatorTeamService.getRegulatorTeamForUser(USER))
        .thenReturn(Optional.of(REGULATOR_TEAM));
    when(teamMemberViewService.getTeamMemberViewsForTeam(REGULATOR_TEAM))
        .thenReturn(Collections.emptyList());

    assertThat(caseAssignmentService.getCaseOfficerCandidates(USER))
        .isEmpty();
  }

  @Test
  void getCaseOfficerCandidates_whenRegulatorUserAndCaseOfficersDontExist_thenEmpty() {
    when(regulatorTeamService.getRegulatorTeamForUser(USER))
        .thenReturn(Optional.of(REGULATOR_TEAM));
    when(teamMemberViewService.getTeamMemberViewsForTeam(REGULATOR_TEAM))
        .thenReturn(List.of(VIEWER_TEAM_MEMBER_VIEW, ACCESS_MANGER_TEAM_MEMBER_VIEW));

    assertThat(caseAssignmentService.getCaseOfficerCandidates(USER))
        .isEmpty();
  }
}
