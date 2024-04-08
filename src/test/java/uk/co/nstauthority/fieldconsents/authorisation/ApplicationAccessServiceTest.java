package uk.co.nstauthority.fieldconsents.authorisation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.ALLOCATE_CONSULTATION;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.PAY_AND_SUBMIT_FCS_APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.PROCESS_FCS_APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.VIEW_FCS_APPLICATIONS;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.Consultation;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitPermissionService;
import uk.co.nstauthority.fieldconsents.teams.TeamService;
import uk.co.nstauthority.fieldconsents.teams.TeamTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@ExtendWith(MockitoExtension.class)
class ApplicationAccessServiceTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  @Mock
  private OrganisationUnitPermissionService organisationUnitPermissionService;

  @Mock
  private TeamService teamService;

  @Mock
  private ConsultationService consultationService;

  @InjectMocks
  @Spy
  private ApplicationAccessService applicationAccessService;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
  }

  @ParameterizedTest
  @ValueSource(booleans = { true, false })
  void hasApplicationPermission_withVarargs(boolean hasApplicationPermission) {
    doReturn(hasApplicationPermission)
        .when(applicationAccessService)
        .hasApplicationPermission(USER, applicationVersion, Set.of(PAY_AND_SUBMIT_FCS_APPLICATIONS));

    assertThat(applicationAccessService.hasApplicationPermission(USER, applicationVersion, PAY_AND_SUBMIT_FCS_APPLICATIONS))
        .isEqualTo(hasApplicationPermission);
  }

  @Test
  void hasApplicationPermission_withSet_whenDoesntHasPermission_thenFalse() {
    var requiredPermissions = Set.of(PAY_AND_SUBMIT_FCS_APPLICATIONS);

    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(USER, TeamType.REGULATOR, requiredPermissions))
        .thenReturn(Collections.emptyList());
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(USER, TeamType.OPRED, requiredPermissions))
        .thenReturn(Collections.emptyList());

    when(organisationUnitPermissionService
        .hasOperatorPermission(
            USER,
            applicationVersion.getPrimaryOperatorOuId(),
            requiredPermissions))
        .thenReturn(false);

    assertThat(
        applicationAccessService
            .hasApplicationPermission(USER, applicationVersion, requiredPermissions)
    ).isFalse();
  }

  @Test
  void hasApplicationPermission_withSet_whenPermissionForTeam_thenTrue() {
    var requiredPermissions = Set.of(PAY_AND_SUBMIT_FCS_APPLICATIONS);

    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(USER, TeamType.REGULATOR, requiredPermissions))
        .thenReturn(Collections.emptyList());
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(USER, TeamType.OPRED, requiredPermissions))
        .thenReturn(Collections.emptyList());

    when(organisationUnitPermissionService
        .hasOperatorPermission(
            USER,
            applicationVersion.getPrimaryOperatorOuId(),
            requiredPermissions))
        .thenReturn(true);

    assertThat(
        applicationAccessService
            .hasApplicationPermission(USER, applicationVersion, requiredPermissions)
    ).isTrue();
  }

  @Test
  void hasApplicationPermission_withSet_whenUserHasPermissionForRegulatorTeam_thenTrue() {
    var requiredPermissions = Set.of(PROCESS_FCS_APPLICATIONS);

    var regulatorTeam = TeamTestUtil.Builder().withTeamType(TeamType.REGULATOR).build();
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(USER, TeamType.REGULATOR, requiredPermissions))
        .thenReturn(List.of(regulatorTeam));

    assertThat(applicationAccessService.hasApplicationPermission(USER, applicationVersion, requiredPermissions))
        .isTrue();
  }

  @Test
  void hasApplicationPermission_withSet_whenUserHasPermissionForConsulteeTeam_thenTrue() {
    var requiredPermissions = Set.of(ALLOCATE_CONSULTATION);

    var consulteeTeam = TeamTestUtil.Builder().withTeamType(TeamType.OPRED).build();
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(USER, TeamType.REGULATOR, requiredPermissions))
        .thenReturn(Collections.emptyList());
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(USER, TeamType.OPRED, requiredPermissions))
        .thenReturn(List.of(consulteeTeam));
    when(consultationService.getConsultationsByApplication(applicationVersion.getApplication()))
        .thenReturn(List.of(new Consultation()));

    assertThat(applicationAccessService.hasApplicationPermission(USER, applicationVersion, requiredPermissions))
        .isTrue();
  }

  @Test
  void hasApplicationPermission_withSet_whenUserHasPermissionForConsulteeTeamButNotForApplication_thenFalse() {
    var requiredPermissions = Set.of(ALLOCATE_CONSULTATION);

    var consulteeTeam = TeamTestUtil.Builder().withTeamType(TeamType.OPRED).build();
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(USER, TeamType.REGULATOR, requiredPermissions))
        .thenReturn(Collections.emptyList());
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(USER, TeamType.OPRED, requiredPermissions))
        .thenReturn(List.of(consulteeTeam));
    when(consultationService.getConsultationsByApplication(applicationVersion.getApplication()))
        .thenReturn(Collections.emptyList());

    assertThat(applicationAccessService.hasApplicationPermission(USER, applicationVersion, requiredPermissions))
        .isFalse();
  }

  @Test
  void getApplicationPermissionsForUser_whenNotRegulatorAndNoOperatorPermissions_thenEmpty() {
    when(teamService.isRegulatorUser(USER)).thenReturn(false);
    when(teamService.isConsulteeUser(USER)).thenReturn(false);
    when(organisationUnitPermissionService
        .getUserPermissionsForOperator(USER, applicationVersion.getPrimaryOperatorOuId()))
        .thenReturn(Collections.emptySet());

    assertThat(applicationAccessService.getApplicationPermissionsForUser(applicationVersion, USER))
        .isEmpty();
  }

  @Test
  void getApplicationPermissionsForUser_whenRegulatorButNoPermissionAndNoOperatorPermissions_thenEmpty() {
    var regulatorTeam = TeamTestUtil.Builder().build();
    when(teamService.isRegulatorUser(USER)).thenReturn(true);
    when(teamService.getTeamsOfTypeThatUserBelongsTo(USER, TeamType.REGULATOR))
        .thenReturn(List.of(regulatorTeam));
    when(teamService.getUserPermissionsForTeam(regulatorTeam, USER))
        .thenReturn(Collections.emptySet());
    when(teamService.isConsulteeUser(USER)).thenReturn(false);
    when(organisationUnitPermissionService
        .getUserPermissionsForOperator(USER, applicationVersion.getPrimaryOperatorOuId()))
        .thenReturn(Collections.emptySet());

    assertThat(applicationAccessService.getApplicationPermissionsForUser(applicationVersion, USER))
        .isEmpty();
  }

  @Test
  void getApplicationPermissionsForUser_whenRegulatorWithPermissionsAndNoOperatorPermissions_thenReturnPermissions() {
    var regulatorTeam = TeamTestUtil.Builder().build();
    var regulatorPermissions = Set.of(RolePermission.PROCESS_FCS_APPLICATIONS, VIEW_FCS_APPLICATIONS);
    when(teamService.isRegulatorUser(USER)).thenReturn(true);
    when(teamService.getTeamsOfTypeThatUserBelongsTo(USER, TeamType.REGULATOR))
        .thenReturn(List.of(regulatorTeam));
    when(teamService.getUserPermissionsForTeam(regulatorTeam, USER))
        .thenReturn(regulatorPermissions);
    when(teamService.isConsulteeUser(USER)).thenReturn(false);
    when(organisationUnitPermissionService
        .getUserPermissionsForOperator(USER, applicationVersion.getPrimaryOperatorOuId()))
        .thenReturn(Collections.emptySet());

    assertThat(applicationAccessService.getApplicationPermissionsForUser(applicationVersion, USER))
        .containsAll(regulatorPermissions);
  }

  @Test
  void getApplicationPermissionsForUser_whenRegulatorWithNoPermissionsButWithOperatorPermissions_thenReturnPermissions() {
    var regulatorTeam = TeamTestUtil.Builder().build();
    var operatorPermissions = Set.of(RolePermission.EDIT_FCS_APPLICATIONS, VIEW_FCS_APPLICATIONS);
    when(teamService.isRegulatorUser(USER)).thenReturn(true);
    when(teamService.getTeamsOfTypeThatUserBelongsTo(USER, TeamType.REGULATOR))
        .thenReturn(List.of(regulatorTeam));
    when(teamService.getUserPermissionsForTeam(regulatorTeam, USER))
        .thenReturn(Collections.emptySet());
    when(teamService.isConsulteeUser(USER)).thenReturn(false);
    when(organisationUnitPermissionService
        .getUserPermissionsForOperator(USER, applicationVersion.getPrimaryOperatorOuId()))
        .thenReturn(operatorPermissions);

    assertThat(applicationAccessService.getApplicationPermissionsForUser(applicationVersion, USER))
        .containsAll(operatorPermissions);
  }

  @Test
  void getApplicationPermissionsForUser_whenRegulatorWithPermissionsAndWithOperatorPermissions_thenReturnPermissions() {
    var regulatorTeam = TeamTestUtil.Builder().build();
    var regulatorPermissions = Set.of(RolePermission.PROCESS_FCS_APPLICATIONS, VIEW_FCS_APPLICATIONS);
    var operatorPermissions = Set.of(RolePermission.EDIT_FCS_APPLICATIONS, VIEW_FCS_APPLICATIONS);
    var allPermissions = Stream.of(regulatorPermissions, operatorPermissions)
        .flatMap(Collection::stream)
        .collect(Collectors.toSet());
    when(teamService.isRegulatorUser(USER)).thenReturn(true);
    when(teamService.getTeamsOfTypeThatUserBelongsTo(USER, TeamType.REGULATOR))
        .thenReturn(List.of(regulatorTeam));
    when(teamService.getUserPermissionsForTeam(regulatorTeam, USER))
        .thenReturn(regulatorPermissions);
    when(teamService.isConsulteeUser(USER)).thenReturn(false);
    when(organisationUnitPermissionService
        .getUserPermissionsForOperator(USER, applicationVersion.getPrimaryOperatorOuId()))
        .thenReturn(operatorPermissions);

    assertThat(applicationAccessService.getApplicationPermissionsForUser(applicationVersion, USER))
        .containsAll(allPermissions);
  }

  @Test
  void getApplicationPermissionsForUser_whenConsulteeWithPermissionsAndNoOperatorPermissions_thenReturnPermissions() {
    var consulteeTeam = TeamTestUtil.Builder().withTeamType(TeamType.OPRED).build();
    var consulteePermissions = Set.of(ALLOCATE_CONSULTATION, VIEW_FCS_APPLICATIONS);
    when(teamService.isRegulatorUser(USER)).thenReturn(false);
    when(teamService.isConsulteeUser(USER)).thenReturn(true);
    when(teamService.getTeamsOfTypeThatUserBelongsTo(USER, TeamType.OPRED))
        .thenReturn(List.of(consulteeTeam));
    when(teamService.getUserPermissionsForTeam(consulteeTeam, USER))
        .thenReturn(consulteePermissions);
    when(organisationUnitPermissionService
        .getUserPermissionsForOperator(USER, applicationVersion.getPrimaryOperatorOuId()))
        .thenReturn(Collections.emptySet());
    when(consultationService.getConsultationsByApplication(applicationVersion.getApplication()))
        .thenReturn(List.of(new Consultation()));

    assertThat(applicationAccessService.getApplicationPermissionsForUser(applicationVersion, USER))
        .containsAll(consulteePermissions);
  }

  @Test
  void getApplicationPermissionsForUser_whenConsulteeWithPermissionsButNoConsultationForApplication_thenEmpty() {
    when(teamService.isRegulatorUser(USER)).thenReturn(false);
    when(teamService.isConsulteeUser(USER)).thenReturn(true);
    when(organisationUnitPermissionService
        .getUserPermissionsForOperator(USER, applicationVersion.getPrimaryOperatorOuId()))
        .thenReturn(Collections.emptySet());
    when(consultationService.getConsultationsByApplication(applicationVersion.getApplication()))
        .thenReturn(Collections.emptyList());

    assertThat(applicationAccessService.getApplicationPermissionsForUser(applicationVersion, USER))
        .isEmpty();
  }
}
