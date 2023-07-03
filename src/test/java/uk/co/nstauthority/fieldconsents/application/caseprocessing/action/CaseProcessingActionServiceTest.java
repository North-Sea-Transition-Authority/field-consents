package uk.co.nstauthority.fieldconsents.application.caseprocessing.action;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CASE_OFFICER_RELEASE_OWNERSHIP;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CASE_OFFICER_WITHDRAWAL_RESPONSE;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CHANGE_ACE_STATUS;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.TECHNICAL_REVIEW_REQUEST;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
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

  private static final Set<RolePermission> CASE_MANAGER_PERMISSIONS =
      RegulatorTeamRole.CASE_MANAGER.getRolePermissions();

  private static final Set<RolePermission> TECHNICAL_REVIEWER_PERMISSIONS =
      RegulatorTeamRole.TECHNICAL_REVIEWER.getRolePermissions();

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
  void getUserActionItems_whenCaseOfficerUser_thenCanTakeOwnership() {
    when(applicationAccessService.getApplicationPermissionsForUser(applicationVersion, USER))
        .thenReturn(CASE_OFFICER_PERMISSIONS);
    when(caseStatusFlagService.getCaseStatusFlags(applicationVersion))
        .thenReturn(Set.of(CaseStatusFlag.CASE_OFFICER_NOT_ASSIGNED));

    assertThat(caseProcessingActionService.getUserActionItems(applicationVersion, USER))
        .containsExactly(CaseProcessingActionItem.CASE_OFFICER_TAKE_OWNERSHIP);
  }

  @Test
  void getUserActionItems_whenCaseOfficerUser_thenCanReleaseOwnership() {
    when(applicationAccessService.getApplicationPermissionsForUser(applicationVersion, USER))
        .thenReturn(CASE_OFFICER_PERMISSIONS);
    when(caseStatusFlagService.getCaseStatusFlags(applicationVersion))
        .thenReturn(Set.of(CaseStatusFlag.CASE_OFFICER_ASSIGNED));

    assertThat(caseProcessingActionService.getUserActionItems(applicationVersion, USER))
        .containsOnly(
            CaseProcessingActionItem.CHANGE_ACE_STATUS,
            CaseProcessingActionItem.CASE_OFFICER_RELEASE_OWNERSHIP
        );
  }

  @Test
  void getUserActionViews_whenCaseOfficerUser_thenCanTakeOwnership() {
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
  void getUserActionViews_whenCaseOfficerUser_thenCanReleaseOwnership() {
    when(applicationAccessService.getApplicationPermissionsForUser(applicationVersion, USER))
        .thenReturn(CASE_OFFICER_PERMISSIONS);
    when(caseStatusFlagService.getCaseStatusFlags(applicationVersion))
        .thenReturn(Set.of(CaseStatusFlag.CASE_OFFICER_ASSIGNED));

    var actionViews = caseProcessingActionService.getUserActionViews(applicationVersion, USER);

    assertThat(actionViews).hasSize(2);

    assertThat(actionViews)
        .usingRecursiveComparison()
        .isEqualTo(
            List.of(
                CaseProcessingActionView.from(
                    CaseProcessingActionItem.CHANGE_ACE_STATUS,
                    applicationVersion
                ),
                CaseProcessingActionView.from(
                    CaseProcessingActionItem.CASE_OFFICER_RELEASE_OWNERSHIP,
                    applicationVersion
                )
            )
        );
  }

  @Test
  void getUserActionItems_whenCaseManagerUser_thenCanAssignCaseOfficer() {
    when(applicationAccessService.getApplicationPermissionsForUser(applicationVersion, USER))
        .thenReturn(CASE_MANAGER_PERMISSIONS);
    when(caseStatusFlagService.getCaseStatusFlags(applicationVersion))
        .thenReturn(Set.of(CaseStatusFlag.CASE_OFFICER_NOT_ASSIGNED));

    assertThat(caseProcessingActionService.getUserActionItems(applicationVersion, USER))
        .containsExactly(CaseProcessingActionItem.CASE_OFFICER_ASSIGN_OWNERSHIP);
  }

  @Test
  void getUserActionItems_whenCaseManagerUser_thenCanReassignCaseOfficer() {
    when(applicationAccessService.getApplicationPermissionsForUser(applicationVersion, USER))
        .thenReturn(CASE_MANAGER_PERMISSIONS);
    when(caseStatusFlagService.getCaseStatusFlags(applicationVersion))
        .thenReturn(Set.of(CaseStatusFlag.CASE_OFFICER_ASSIGNED));

    assertThat(caseProcessingActionService.getUserActionItems(applicationVersion, USER))
        .containsExactly(CaseProcessingActionItem.CASE_OFFICER_REASSIGN_OWNERSHIP);
  }

  @Test
  void getUserActionItems_whenTechnicalReviewerAndReviewOpen_thenCanReassignTechnicalReviewer() {
    when(applicationAccessService.getApplicationPermissionsForUser(applicationVersion, USER))
        .thenReturn(TECHNICAL_REVIEWER_PERMISSIONS);
    when(caseStatusFlagService.getCaseStatusFlags(applicationVersion))
        .thenReturn(Set.of(CaseStatusFlag.TECHNICAL_REVIEW_OPEN));

    assertThat(caseProcessingActionService.getUserActionItems(applicationVersion, USER))
        .containsExactly(CaseProcessingActionItem.TECHNICAL_REVIEWER_REASSIGN_OWNERSHIP);

  }

  @Test
  void getUserActionItems_whenTechnicalReviewerAndNoReviewOpen_thenCannotReassignTechnicalReviewer() {
    when(applicationAccessService.getApplicationPermissionsForUser(applicationVersion, USER))
        .thenReturn(TECHNICAL_REVIEWER_PERMISSIONS);
    when(caseStatusFlagService.getCaseStatusFlags(applicationVersion))
        .thenReturn(Set.of(CaseStatusFlag.NO_TECHNICAL_REVIEW_OPEN));

    assertThat(caseProcessingActionService.getUserActionItems(applicationVersion, USER))
        .isEmpty();
  }

  @Test
  void getUserActionViews_whenCaseManagerUser_thenCanAssignCaseOfficer() {
    when(applicationAccessService.getApplicationPermissionsForUser(applicationVersion, USER))
        .thenReturn(CASE_MANAGER_PERMISSIONS);
    when(caseStatusFlagService.getCaseStatusFlags(applicationVersion))
        .thenReturn(Set.of(CaseStatusFlag.CASE_OFFICER_NOT_ASSIGNED));

    var actionViews = caseProcessingActionService.getUserActionViews(applicationVersion, USER);

    assertThat(actionViews).hasSize(1);

    assertThat(actionViews.get(0))
        .usingRecursiveComparison()
        .isEqualTo(
            CaseProcessingActionView.from(
                CaseProcessingActionItem.CASE_OFFICER_ASSIGN_OWNERSHIP,
                applicationVersion
            )
        );
  }

  @Test
  void getUserActionViews_whenCaseManagerUser_thenCanReassignCaseOfficer() {
    when(applicationAccessService.getApplicationPermissionsForUser(applicationVersion, USER))
        .thenReturn(CASE_MANAGER_PERMISSIONS);
    when(caseStatusFlagService.getCaseStatusFlags(applicationVersion))
        .thenReturn(Set.of(CaseStatusFlag.CASE_OFFICER_ASSIGNED));

    var actionViews = caseProcessingActionService.getUserActionViews(applicationVersion, USER);

    assertThat(actionViews).hasSize(1);

    assertThat(actionViews.get(0))
        .usingRecursiveComparison()
        .isEqualTo(
            CaseProcessingActionView.from(
                CaseProcessingActionItem.CASE_OFFICER_REASSIGN_OWNERSHIP,
                applicationVersion
            )
        );
  }

  @Test
  void getUserActionViews_whenIndustryUser_thenCanRequestCaseWithdrawal() {
    when(applicationAccessService.getApplicationPermissionsForUser(applicationVersion, USER))
        .thenReturn(EnumSet.of(RolePermission.EDIT_FCS_APPLICATIONS));
    when(caseStatusFlagService.getCaseStatusFlags(applicationVersion))
        .thenReturn(Set.of(CaseStatusFlag.NO_WITHDRAWAL_OPEN));

    var actionViews = caseProcessingActionService.getUserActionViews(applicationVersion, USER);

    assertThat(actionViews).hasSize(1);

    assertThat(actionViews.get(0))
        .usingRecursiveComparison()
        .isEqualTo(
            CaseProcessingActionView.from(
                CaseProcessingActionItem.OPERATOR_WITHDRAWAL_REQUEST,
                applicationVersion
            )
        );
  }

  @Test
  void getUserActionViews_whenIndustryUser_thenCannotRequestCaseWithdrawal() {
    when(applicationAccessService.getApplicationPermissionsForUser(applicationVersion, USER))
        .thenReturn(EnumSet.of(RolePermission.EDIT_FCS_APPLICATIONS));
    when(caseStatusFlagService.getCaseStatusFlags(applicationVersion))
        .thenReturn(Set.of(CaseStatusFlag.WITHDRAWAL_OPEN));

    var actionViews = caseProcessingActionService.getUserActionViews(applicationVersion, USER);

    assertThat(actionViews).isEmpty();
  }

  @Test
  void getUserActionViews_canRespondToCaseWithdrawalAndStartTechnicalReview() {
    when(applicationAccessService.getApplicationPermissionsForUser(applicationVersion, USER))
        .thenReturn(CASE_OFFICER_PERMISSIONS);
    when(caseStatusFlagService.getCaseStatusFlags(applicationVersion))
        .thenReturn(Set.of(
            CaseStatusFlag.CASE_OFFICER_ASSIGNED,
            CaseStatusFlag.WITHDRAWAL_OPEN,
            CaseStatusFlag.NO_TECHNICAL_REVIEW_OPEN
        ));

    var actionViews = caseProcessingActionService.getUserActionViews(applicationVersion, USER);

    assertThat(actionViews).hasSize(4);

    assertThat(actionViews.get(0))
        .usingRecursiveComparison()
        .isEqualTo(CaseProcessingActionView.from(CHANGE_ACE_STATUS, applicationVersion));

    assertThat(actionViews.get(1))
        .usingRecursiveComparison()
        .isEqualTo(CaseProcessingActionView.from(CASE_OFFICER_RELEASE_OWNERSHIP, applicationVersion));

    assertThat(actionViews.get(2))
        .usingRecursiveComparison()
        .isEqualTo(CaseProcessingActionView.from(CASE_OFFICER_WITHDRAWAL_RESPONSE, applicationVersion));

    assertThat(actionViews.get(3))
        .usingRecursiveComparison()
        .isEqualTo(CaseProcessingActionView.from(TECHNICAL_REVIEW_REQUEST, applicationVersion));
  }

  @Test
  void getUserActionViews_whenCaseOfficerUser_andNotOpenWithdrawal_thenCannotRespondToCaseWithdrawal() {
    when(applicationAccessService.getApplicationPermissionsForUser(applicationVersion, USER))
        .thenReturn(CASE_OFFICER_PERMISSIONS);
    when(caseStatusFlagService.getCaseStatusFlags(applicationVersion))
        .thenReturn(Set.of(CaseStatusFlag.NO_WITHDRAWAL_OPEN));

    var actionViews = caseProcessingActionService.getUserActionViews(applicationVersion, USER);

    assertThat(actionViews).isEmpty();
  }

  @Test
  void getUserActionViews_whenIndustryUser_thenCannotAddCaseNotes() {
    when(applicationAccessService.getApplicationPermissionsForUser(applicationVersion, USER))
        .thenReturn(EnumSet.of(RolePermission.EDIT_FCS_APPLICATIONS));
    when(caseStatusFlagService.getCaseStatusFlags(applicationVersion))
        .thenReturn(Set.of());

    var actionViews = caseProcessingActionService.getUserActionViews(applicationVersion, USER);

    assertThat(actionViews).isEmpty();
  }

  @Test
  void getUserActionViews_whenCaseOfficer_thenCanAddCaseNotes() {
    when(applicationAccessService.getApplicationPermissionsForUser(applicationVersion, USER))
        .thenReturn(CASE_OFFICER_PERMISSIONS);
    when(caseStatusFlagService.getCaseStatusFlags(applicationVersion))
        .thenReturn(Set.of(CaseStatusFlag.CASE_NOTES_ALLOWED));

    var actionViews = caseProcessingActionService.getUserActionViews(applicationVersion, USER);

    assertThat(actionViews).hasSize(1);

    assertThat(actionViews.get(0))
        .usingRecursiveComparison()
        .isEqualTo(
            CaseProcessingActionView.from(
                CaseProcessingActionItem.REGULATOR_ADD_CASE_NOTE,
                applicationVersion
            )
        );
  }

  @Test
  void getUserActionViews_whenCaseManager_thenCanAddCaseNotes() {
    when(applicationAccessService.getApplicationPermissionsForUser(applicationVersion, USER))
        .thenReturn(CASE_MANAGER_PERMISSIONS);
    when(caseStatusFlagService.getCaseStatusFlags(applicationVersion))
        .thenReturn(Set.of(CaseStatusFlag.CASE_NOTES_ALLOWED));

    var actionViews = caseProcessingActionService.getUserActionViews(applicationVersion, USER);

    assertThat(actionViews).hasSize(1);

    assertThat(actionViews.get(0))
        .usingRecursiveComparison()
        .isEqualTo(
            CaseProcessingActionView.from(
                CaseProcessingActionItem.REGULATOR_ADD_CASE_NOTE,
                applicationVersion
            )
        );
  }

  @Test
  void getUserActionViews_whenTechnicalReviewer_thenCanAddCaseNotes() {
    when(applicationAccessService.getApplicationPermissionsForUser(applicationVersion, USER))
        .thenReturn(TECHNICAL_REVIEWER_PERMISSIONS);
    when(caseStatusFlagService.getCaseStatusFlags(applicationVersion))
        .thenReturn(Set.of(CaseStatusFlag.CASE_NOTES_ALLOWED));

    var actionViews = caseProcessingActionService.getUserActionViews(applicationVersion, USER);

    assertThat(actionViews).hasSize(1);

    assertThat(actionViews.get(0))
        .usingRecursiveComparison()
        .isEqualTo(
            CaseProcessingActionView.from(
                CaseProcessingActionItem.REGULATOR_ADD_CASE_NOTE,
                applicationVersion
            )
        );
  }
}
