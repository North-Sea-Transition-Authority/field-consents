package uk.co.nstauthority.fieldconsents.application.caseprocessing.action;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlagService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.authorisation.ApplicationAccessService;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamRole;

@ExtendWith(MockitoExtension.class)
class CaseProcessingActionServiceTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  private static final Set<RolePermission> CASE_OFFICER_PERMISSIONS =
      RegulatorTeamRole.CASE_OFFICER.getRolePermissions();

  @Mock
  private ApplicationAccessService applicationAccessService;

  @Mock
  private CaseStatusFlagService caseStatusFlagService;

  @InjectMocks
  private CaseProcessingActionService caseProcessingActionService;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.FLARE);
  }

  @Test
  void getUserActionItems_whenNoActionsForCaseStatus_thenThrowException() {
    applicationVersion.setStatus(ApplicationVersionStatus.COMPLETED);
    assertThatThrownBy(() -> caseProcessingActionService.getUserActionItems(applicationVersion, USER))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Cannot find any actions for application version id %s with status %s"
            .formatted(applicationVersion.getId(), applicationVersion.getStatus().name()));
  }

  @Test
  void getUserActionItems_whenNoActionsForUser_thenEmpty() {
    when(applicationAccessService.getApplicationPermissionsForUser(applicationVersion, USER))
        .thenReturn(Collections.emptySet());
    when(caseStatusFlagService.getCaseStatusFlags(applicationVersion))
        .thenReturn(Collections.emptySet());

    assertThat(caseProcessingActionService.getUserActionItems(applicationVersion, USER))
        .isEmpty();
  }

  @Test
  void getUserActionItems_whenNoActionsForUser_permissionsExistButNotFlags_thenEmpty() {
    when(applicationAccessService.getApplicationPermissionsForUser(applicationVersion, USER))
        .thenReturn(CASE_OFFICER_PERMISSIONS);
    when(caseStatusFlagService.getCaseStatusFlags(applicationVersion))
        .thenReturn(Collections.emptySet());

    assertThat(caseProcessingActionService.getUserActionItems(applicationVersion, USER))
        .isEmpty();
  }

  @Test
  void getUserActionItems_whenCaseOfficerUser_thenCanAssignCaseOfficer() {
    when(applicationAccessService.getApplicationPermissionsForUser(applicationVersion, USER))
        .thenReturn(CASE_OFFICER_PERMISSIONS);
    when(caseStatusFlagService.getCaseStatusFlags(applicationVersion))
        .thenReturn(Set.of(CaseStatusFlag.CASE_OFFICER_NOT_ASSIGNED));

    assertThat(caseProcessingActionService.getUserActionItems(applicationVersion, USER))
        .containsExactly(CaseProcessingActionItem.CASE_OFFICER_TAKE_OWNERSHIP);
  }

  @Test
  void getUserActionItems_whenCaseOfficerUser_thenCanUnassignCaseOfficer() {
    when(applicationAccessService.getApplicationPermissionsForUser(applicationVersion, USER))
        .thenReturn(CASE_OFFICER_PERMISSIONS);
    when(caseStatusFlagService.getCaseStatusFlags(applicationVersion))
        .thenReturn(Set.of(CaseStatusFlag.CASE_OFFICER_ASSIGNED));

    assertThat(caseProcessingActionService.getUserActionItems(applicationVersion, USER))
        .containsExactly(CaseProcessingActionItem.CASE_OFFICER_RELEASE_OWNERSHIP);
  }

  @Test
  void getUserActionViews_whenCaseOfficerUser_thenCanAssignCaseOfficer() {
    when(applicationAccessService.getApplicationPermissionsForUser(applicationVersion, USER))
        .thenReturn(CASE_OFFICER_PERMISSIONS);
    when(caseStatusFlagService.getCaseStatusFlags(applicationVersion))
        .thenReturn(Set.of(CaseStatusFlag.CASE_OFFICER_NOT_ASSIGNED));

    var actionViews = caseProcessingActionService.getUserActionViews(applicationVersion, USER);

    assertThat(actionViews).hasSize(1);

    assertThat(actionViews.get(0))
        .usingRecursiveComparison()
        .isEqualTo(
            CaseProcessingActionView.from(
                CaseProcessingActionItem.CASE_OFFICER_TAKE_OWNERSHIP,
                applicationVersion
            )
        );
  }

  @Test
  void getUserActionViews_whenCaseOfficerUser_thenCanUnassignCaseOfficer() {
    when(applicationAccessService.getApplicationPermissionsForUser(applicationVersion, USER))
        .thenReturn(CASE_OFFICER_PERMISSIONS);
    when(caseStatusFlagService.getCaseStatusFlags(applicationVersion))
        .thenReturn(Set.of(CaseStatusFlag.CASE_OFFICER_ASSIGNED));

    var actionViews = caseProcessingActionService.getUserActionViews(applicationVersion, USER);

    assertThat(actionViews).hasSize(1);

    assertThat(actionViews.get(0))
        .usingRecursiveComparison()
        .isEqualTo(
            CaseProcessingActionView.from(
                CaseProcessingActionItem.CASE_OFFICER_RELEASE_OWNERSHIP,
                applicationVersion
            )
        );
  }
}
