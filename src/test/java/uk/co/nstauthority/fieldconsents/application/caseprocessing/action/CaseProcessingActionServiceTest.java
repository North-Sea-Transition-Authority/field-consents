package uk.co.nstauthority.fieldconsents.application.caseprocessing.action;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.APPLICATION_UPDATE_REQUEST;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CASE_OFFICER_ASSIGN_OWNERSHIP;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CASE_OFFICER_REASSIGN_OWNERSHIP;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CASE_OFFICER_RELEASE_OWNERSHIP;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CASE_OFFICER_TAKE_OWNERSHIP;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CASE_OFFICER_WITHDRAWAL_RESPONSE;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CHANGE_ACE_STATUS;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CONSULTATION_MANAGE_RESPONDER;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CONSULTATION_REQUEST;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.OPERATOR_UPDATE_APPLICATION;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.OPERATOR_WITHDRAWAL_REQUEST;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.REGULATOR_ADD_CASE_NOTE;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.TECHNICAL_REVIEWER_REASSIGN_OWNERSHIP;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.TECHNICAL_REVIEWER_SUBMIT_REVIEW;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.TECHNICAL_REVIEWS;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.TECHNICAL_REVIEW_REQUEST;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.APPLICATION_UPDATE_OPEN;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.CASE_OFFICER_ASSIGNED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.CONSULTATION_OPEN;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.NO_APPLICATION_UPDATE_OPEN;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.NO_CONSULTATION_OPEN;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.NO_TECHNICAL_REVIEW_OPEN;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.NO_WITHDRAWAL_OPEN;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.TECHNICAL_REVIEW_OPEN;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.WITHDRAWAL_OPEN;

import java.util.Collections;
import java.util.Optional;
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
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlagService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.authorisation.ApplicationAccessService;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.industry.IndustryTeamRole;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.opred.OpredTeamRole;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamRole;

@ExtendWith(MockitoExtension.class)
class CaseProcessingActionServiceTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  private static final WebUserAccountId USER_WUA_ID = WebUserAccountId.from(USER.wuaId());

  private static final WebUserAccountId OTHER_USER_WUA_ID = WebUserAccountId.from(999L);

  private static final Set<RolePermission> CASE_OFFICER_PERMISSIONS =
      RegulatorTeamRole.CASE_OFFICER.getRolePermissions();

  private static final Set<RolePermission> CASE_MANAGER_PERMISSIONS =
      RegulatorTeamRole.CASE_MANAGER.getRolePermissions();

  private static final Set<RolePermission> TECHNICAL_REVIEWER_PERMISSIONS =
      RegulatorTeamRole.TECHNICAL_REVIEWER.getRolePermissions();

  private static final Set<RolePermission> INDUSTRY_EDITOR_PERMISSIONS =
      IndustryTeamRole.EDITOR.getRolePermissions();

  @Mock
  private ApplicationAccessService applicationAccessService;

  @Mock
  private CaseStatusFlagService caseStatusFlagService;

  @Mock
  private ApplicationVersionService applicationVersionService;

  @Mock
  private TechnicalReviewService technicalReviewService;

  @Mock
  private ConsultationService consultationService;

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
        .containsExactly(CASE_OFFICER_TAKE_OWNERSHIP);
  }

  @Test
  void getUserActionItems_whenAssignedCaseOfficerUser_thenCanReleaseOwnershipAndChangeAceStatus() {
    when(applicationAccessService.getApplicationPermissionsForUser(applicationVersion, USER))
        .thenReturn(CASE_OFFICER_PERMISSIONS);
    when(caseStatusFlagService.getCaseStatusFlags(applicationVersion))
        .thenReturn(Set.of(CASE_OFFICER_ASSIGNED));
    when(applicationVersionService.findCaseOfficerWuaId(applicationVersion))
        .thenReturn(Optional.of(USER_WUA_ID));

    assertThat(caseProcessingActionService.getUserActionItems(applicationVersion, USER))
        .containsOnly(
            CHANGE_ACE_STATUS,
            CASE_OFFICER_RELEASE_OWNERSHIP
        );
  }

  @Test
  void getUserActionItems_whenAssignedCaseOfficerUserNotCurrentUser_thenCannotReleaseOwnershipOrChangeAceStatus() {
    when(applicationAccessService.getApplicationPermissionsForUser(applicationVersion, USER))
        .thenReturn(CASE_OFFICER_PERMISSIONS);
    when(caseStatusFlagService.getCaseStatusFlags(applicationVersion))
        .thenReturn(Set.of(CASE_OFFICER_ASSIGNED));
    when(applicationVersionService.findCaseOfficerWuaId(applicationVersion))
        .thenReturn(Optional.of(OTHER_USER_WUA_ID));

    assertThat(caseProcessingActionService.getUserActionItems(applicationVersion, USER))
        .isEmpty();
  }

  @Test
  void getUserActionViews_whenCaseOfficerUser_thenCanTakeOwnership() {
    when(applicationAccessService.getApplicationPermissionsForUser(applicationVersion, USER))
        .thenReturn(CASE_OFFICER_PERMISSIONS);
    when(caseStatusFlagService.getCaseStatusFlags(applicationVersion))
        .thenReturn(Set.of(CaseStatusFlag.CASE_OFFICER_NOT_ASSIGNED));

    assertThat(caseProcessingActionService.getUserActionViews(applicationVersion, USER))
        .hasSize(1)
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactly(CaseProcessingActionView.from(CASE_OFFICER_TAKE_OWNERSHIP, applicationVersion));
  }

  @Test
  void getUserActionViews_whenAssignedCaseOfficerUser_thenCanReleaseOwnership() {
    when(applicationAccessService.getApplicationPermissionsForUser(applicationVersion, USER))
        .thenReturn(CASE_OFFICER_PERMISSIONS);
    when(caseStatusFlagService.getCaseStatusFlags(applicationVersion))
        .thenReturn(Set.of(CASE_OFFICER_ASSIGNED));
    when(applicationVersionService.findCaseOfficerWuaId(applicationVersion))
        .thenReturn(Optional.of(USER_WUA_ID));

    assertThat(caseProcessingActionService.getUserActionViews(applicationVersion, USER))
        .hasSize(2)
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactly(
            CaseProcessingActionView.from(CHANGE_ACE_STATUS, applicationVersion),
            CaseProcessingActionView.from(CASE_OFFICER_RELEASE_OWNERSHIP, applicationVersion)
        );
  }

  @Test
  void getUserActionViews_whenAssignedCaseOfficerUserNotCurrentUser_thenCannotReleaseOwnershipOrChangeAceStatus() {
    when(applicationAccessService.getApplicationPermissionsForUser(applicationVersion, USER))
        .thenReturn(CASE_OFFICER_PERMISSIONS);
    when(caseStatusFlagService.getCaseStatusFlags(applicationVersion))
        .thenReturn(Set.of(CASE_OFFICER_ASSIGNED));
    when(applicationVersionService.findCaseOfficerWuaId(applicationVersion))
        .thenReturn(Optional.of(OTHER_USER_WUA_ID));

    assertThat(caseProcessingActionService.getUserActionViews(applicationVersion, USER))
        .isEmpty();
  }

  @Test
  void getUserActionItems_whenCaseManagerUser_thenCanAssignCaseOfficer() {
    when(applicationAccessService.getApplicationPermissionsForUser(applicationVersion, USER))
        .thenReturn(CASE_MANAGER_PERMISSIONS);
    when(caseStatusFlagService.getCaseStatusFlags(applicationVersion))
        .thenReturn(Set.of(CaseStatusFlag.CASE_OFFICER_NOT_ASSIGNED));

    assertThat(caseProcessingActionService.getUserActionItems(applicationVersion, USER))
        .containsExactly(CASE_OFFICER_ASSIGN_OWNERSHIP);
  }

  @Test
  void getUserActionItems_whenCaseManagerUser_thenCanReassignCaseOfficer() {
    when(applicationAccessService.getApplicationPermissionsForUser(applicationVersion, USER))
        .thenReturn(CASE_MANAGER_PERMISSIONS);
    when(caseStatusFlagService.getCaseStatusFlags(applicationVersion))
        .thenReturn(Set.of(CASE_OFFICER_ASSIGNED));

    assertThat(caseProcessingActionService.getUserActionItems(applicationVersion, USER))
        .containsExactly(CASE_OFFICER_REASSIGN_OWNERSHIP);
  }

  @Test
  void getUserActionItems_whenTechnicalReviewerAndReviewOpen_thenCanReassignTechnicalReviewer() {
    when(applicationAccessService.getApplicationPermissionsForUser(applicationVersion, USER))
        .thenReturn(TECHNICAL_REVIEWER_PERMISSIONS);
    when(caseStatusFlagService.getCaseStatusFlags(applicationVersion))
        .thenReturn(Set.of(TECHNICAL_REVIEW_OPEN));

    assertThat(caseProcessingActionService.getUserActionItems(applicationVersion, USER))
        .containsExactly(CaseProcessingActionItem.TECHNICAL_REVIEWER_REASSIGN_OWNERSHIP);

  }

  @Test
  void getUserActionItems_whenTechnicalReviewerAndNoReviewOpen_thenCannotReassignTechnicalReviewer() {
    when(applicationAccessService.getApplicationPermissionsForUser(applicationVersion, USER))
        .thenReturn(TECHNICAL_REVIEWER_PERMISSIONS);
    when(caseStatusFlagService.getCaseStatusFlags(applicationVersion))
        .thenReturn(Set.of(NO_TECHNICAL_REVIEW_OPEN));

    assertThat(caseProcessingActionService.getUserActionItems(applicationVersion, USER))
        .isEmpty();
  }

  @Test
  void getUserActionItems_whenCaseOfficer_thenCanSeeTechnicalReviewsPage() {
    when(applicationAccessService.getApplicationPermissionsForUser(applicationVersion, USER))
        .thenReturn(CASE_OFFICER_PERMISSIONS);
    when(caseStatusFlagService.getCaseStatusFlags(applicationVersion))
        .thenReturn(Set.of(CaseStatusFlag.TECHNICAL_REVIEWS_PAGE_ENABLED));

    assertThat(caseProcessingActionService.getUserActionItems(applicationVersion, USER))
        .containsExactly(TECHNICAL_REVIEWS);
  }

  @Test
  void getUserActionItems_whenCaseManager_thenCanSeeTechnicalReviewsPage() {
    when(applicationAccessService.getApplicationPermissionsForUser(applicationVersion, USER))
        .thenReturn(CASE_MANAGER_PERMISSIONS);
    when(caseStatusFlagService.getCaseStatusFlags(applicationVersion))
        .thenReturn(Set.of(CaseStatusFlag.TECHNICAL_REVIEWS_PAGE_ENABLED));

    assertThat(caseProcessingActionService.getUserActionItems(applicationVersion, USER))
        .containsExactly(TECHNICAL_REVIEWS);
  }

  @Test
  void getUserActionItems_whenTechnicalReviewer_thenCanSeeTechnicalReviewsPage() {
    when(applicationAccessService.getApplicationPermissionsForUser(applicationVersion, USER))
        .thenReturn(TECHNICAL_REVIEWER_PERMISSIONS);
    when(caseStatusFlagService.getCaseStatusFlags(applicationVersion))
        .thenReturn(Set.of(CaseStatusFlag.TECHNICAL_REVIEWS_PAGE_ENABLED));

    assertThat(caseProcessingActionService.getUserActionItems(applicationVersion, USER))
        .containsExactly(TECHNICAL_REVIEWS);
  }

  @Test
  void getUserActionItems_whenIndustryEditor_thenCannotSeeTechnicalReviewsPage() {
    when(applicationAccessService.getApplicationPermissionsForUser(applicationVersion, USER))
        .thenReturn(INDUSTRY_EDITOR_PERMISSIONS);
    when(caseStatusFlagService.getCaseStatusFlags(applicationVersion))
        .thenReturn(Set.of(CaseStatusFlag.TECHNICAL_REVIEWS_PAGE_ENABLED));

    assertThat(caseProcessingActionService.getUserActionItems(applicationVersion, USER))
        .isEmpty();
  }

  @Test
  void getUserActionViews_whenCaseManagerUser_thenCanAssignCaseOfficer() {
    when(applicationAccessService.getApplicationPermissionsForUser(applicationVersion, USER))
        .thenReturn(CASE_MANAGER_PERMISSIONS);
    when(caseStatusFlagService.getCaseStatusFlags(applicationVersion))
        .thenReturn(Set.of(CaseStatusFlag.CASE_OFFICER_NOT_ASSIGNED));

    assertThat(caseProcessingActionService.getUserActionViews(applicationVersion, USER))
        .hasSize(1)
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactly(CaseProcessingActionView.from(CASE_OFFICER_ASSIGN_OWNERSHIP, applicationVersion));
  }

  @Test
  void getUserActionViews_whenCaseManagerUser_thenCanReassignCaseOfficer() {
    when(applicationAccessService.getApplicationPermissionsForUser(applicationVersion, USER))
        .thenReturn(CASE_MANAGER_PERMISSIONS);
    when(caseStatusFlagService.getCaseStatusFlags(applicationVersion))
        .thenReturn(Set.of(CASE_OFFICER_ASSIGNED));

    assertThat(caseProcessingActionService.getUserActionViews(applicationVersion, USER))
        .hasSize(1)
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactly(CaseProcessingActionView.from(CASE_OFFICER_REASSIGN_OWNERSHIP, applicationVersion));
  }

  @Test
  void getUserActionViews_whenIndustryEditorAndNoWithdrawalAndNoAppUpdateOpen_thenCanRequestCaseWithdrawal() {
    when(applicationAccessService.getApplicationPermissionsForUser(applicationVersion, USER))
        .thenReturn(INDUSTRY_EDITOR_PERMISSIONS);
    when(caseStatusFlagService.getCaseStatusFlags(applicationVersion))
        .thenReturn(Set.of(NO_WITHDRAWAL_OPEN, NO_APPLICATION_UPDATE_OPEN));

    assertThat(caseProcessingActionService.getUserActionViews(applicationVersion, USER))
        .hasSize(1)
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactly(CaseProcessingActionView.from(OPERATOR_WITHDRAWAL_REQUEST, applicationVersion));
  }

  @Test
  void getUserActionViews_whenIndustryEditor_thenCannotRequestCaseWithdrawal() {
    when(applicationAccessService.getApplicationPermissionsForUser(applicationVersion, USER))
        .thenReturn(INDUSTRY_EDITOR_PERMISSIONS);
    when(caseStatusFlagService.getCaseStatusFlags(applicationVersion))
        .thenReturn(Set.of(WITHDRAWAL_OPEN));

    assertThat(caseProcessingActionService.getUserActionViews(applicationVersion, USER))
        .isEmpty();
  }

  @Test
  void getUserActionViews_whenAssignedCaseOfficerAndWithdrawalOpen_canRespondToCaseWithdrawal() {
    when(applicationAccessService.getApplicationPermissionsForUser(applicationVersion, USER))
        .thenReturn(CASE_OFFICER_PERMISSIONS);
    when(caseStatusFlagService.getCaseStatusFlags(applicationVersion))
        .thenReturn(Set.of(CASE_OFFICER_ASSIGNED, WITHDRAWAL_OPEN));
    when(applicationVersionService.findCaseOfficerWuaId(applicationVersion))
        .thenReturn(Optional.of(USER_WUA_ID));

    assertThat(caseProcessingActionService.getUserActionViews(applicationVersion, USER))
        .hasSize(3)
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactly(
            CaseProcessingActionView.from(CHANGE_ACE_STATUS, applicationVersion),
            CaseProcessingActionView.from(CASE_OFFICER_RELEASE_OWNERSHIP, applicationVersion),
            CaseProcessingActionView.from(CASE_OFFICER_WITHDRAWAL_RESPONSE, applicationVersion)
        );
  }

  @Test
  void getUserActionViews_whenAssignedCaseOfficerNotCurrentUserAndWithdrawalOpen_cannotRespondToCaseWithdrawal() {
    when(applicationAccessService.getApplicationPermissionsForUser(applicationVersion, USER))
        .thenReturn(CASE_OFFICER_PERMISSIONS);
    when(caseStatusFlagService.getCaseStatusFlags(applicationVersion))
        .thenReturn(Set.of(CASE_OFFICER_ASSIGNED, WITHDRAWAL_OPEN));
    when(applicationVersionService.findCaseOfficerWuaId(applicationVersion))
        .thenReturn(Optional.of(OTHER_USER_WUA_ID));

    assertThat(caseProcessingActionService.getUserActionViews(applicationVersion, USER))
        .isEmpty();
  }

  @Test
  void getUserActionViews_whenAssignedCaseOfficerUserAndNotOpenWithdrawal_thenCannotRespondToCaseWithdrawal() {
    when(applicationAccessService.getApplicationPermissionsForUser(applicationVersion, USER))
        .thenReturn(CASE_OFFICER_PERMISSIONS);
    when(caseStatusFlagService.getCaseStatusFlags(applicationVersion))
        .thenReturn(Set.of(CASE_OFFICER_ASSIGNED, NO_WITHDRAWAL_OPEN));
    when(applicationVersionService.findCaseOfficerWuaId(applicationVersion))
        .thenReturn(Optional.of(USER_WUA_ID));

    assertThat(caseProcessingActionService.getUserActionViews(applicationVersion, USER))
        .hasSize(2)
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactly(
            CaseProcessingActionView.from(CHANGE_ACE_STATUS, applicationVersion),
            CaseProcessingActionView.from(CASE_OFFICER_RELEASE_OWNERSHIP, applicationVersion)
        );
  }

  @Test
  void getUserActionViews_whenIndustryEditor_thenCannotAddCaseNotes() {
    when(applicationAccessService.getApplicationPermissionsForUser(applicationVersion, USER))
        .thenReturn(INDUSTRY_EDITOR_PERMISSIONS);
    when(caseStatusFlagService.getCaseStatusFlags(applicationVersion))
        .thenReturn(Collections.emptySet());

    assertThat(caseProcessingActionService.getUserActionViews(applicationVersion, USER))
        .isEmpty();
  }

  @Test
  void getUserActionViews_whenCaseOfficer_thenCanAddCaseNotes() {
    when(applicationAccessService.getApplicationPermissionsForUser(applicationVersion, USER))
        .thenReturn(CASE_OFFICER_PERMISSIONS);
    when(caseStatusFlagService.getCaseStatusFlags(applicationVersion))
        .thenReturn(Set.of(CaseStatusFlag.CASE_NOTES_ALLOWED));

    assertThat(caseProcessingActionService.getUserActionViews(applicationVersion, USER))
        .hasSize(1)
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactly(CaseProcessingActionView.from(REGULATOR_ADD_CASE_NOTE, applicationVersion));
  }

  @Test
  void getUserActionViews_whenCaseManager_thenCanAddCaseNotes() {
    when(applicationAccessService.getApplicationPermissionsForUser(applicationVersion, USER))
        .thenReturn(CASE_MANAGER_PERMISSIONS);
    when(caseStatusFlagService.getCaseStatusFlags(applicationVersion))
        .thenReturn(Set.of(CaseStatusFlag.CASE_NOTES_ALLOWED));

    assertThat(caseProcessingActionService.getUserActionViews(applicationVersion, USER))
        .hasSize(1)
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactly(CaseProcessingActionView.from(REGULATOR_ADD_CASE_NOTE, applicationVersion));
  }

  @Test
  void getUserActionViews_whenTechnicalReviewer_thenCanAddCaseNotes() {
    when(applicationAccessService.getApplicationPermissionsForUser(applicationVersion, USER))
        .thenReturn(TECHNICAL_REVIEWER_PERMISSIONS);
    when(caseStatusFlagService.getCaseStatusFlags(applicationVersion))
        .thenReturn(Set.of(CaseStatusFlag.CASE_NOTES_ALLOWED));

    assertThat(caseProcessingActionService.getUserActionViews(applicationVersion, USER))
        .hasSize(1)
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactly(CaseProcessingActionView.from(REGULATOR_ADD_CASE_NOTE, applicationVersion));
  }

  @Test
  void getUserActionViews_whenAssignedCaseOfficer_thenCanStartTechnicalReviewAndAppUpdate() {
    when(applicationAccessService.getApplicationPermissionsForUser(applicationVersion, USER))
        .thenReturn(CASE_OFFICER_PERMISSIONS);
    when(caseStatusFlagService.getCaseStatusFlags(applicationVersion))
        .thenReturn(Set.of(CASE_OFFICER_ASSIGNED, NO_TECHNICAL_REVIEW_OPEN, NO_APPLICATION_UPDATE_OPEN,
            NO_CONSULTATION_OPEN));
    when(applicationVersionService.findCaseOfficerWuaId(applicationVersion))
        .thenReturn(Optional.of(USER_WUA_ID));

    assertThat(caseProcessingActionService.getUserActionViews(applicationVersion, USER))
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactly(
            CaseProcessingActionView.from(CHANGE_ACE_STATUS, applicationVersion),
            CaseProcessingActionView.from(CASE_OFFICER_RELEASE_OWNERSHIP, applicationVersion),
            CaseProcessingActionView.from(TECHNICAL_REVIEW_REQUEST, applicationVersion),
            CaseProcessingActionView.from(APPLICATION_UPDATE_REQUEST, applicationVersion),
            CaseProcessingActionView.from(CONSULTATION_REQUEST, applicationVersion)
        );
  }

  @Test
  void getUserActionViews_whenConsultationAllocator_thenCanAssignResponder() {
    var allocatorRoles = OpredTeamRole.ALLOCATOR.getRolePermissions();
    var caseFlags = Set.of(CASE_OFFICER_ASSIGNED, NO_TECHNICAL_REVIEW_OPEN, NO_APPLICATION_UPDATE_OPEN, CONSULTATION_OPEN);

    when(applicationAccessService.getApplicationPermissionsForUser(applicationVersion, USER)).thenReturn(allocatorRoles);
    when(caseStatusFlagService.getCaseStatusFlags(applicationVersion)).thenReturn(caseFlags);
    when(applicationVersionService.findCaseOfficerWuaId(applicationVersion)).thenReturn(Optional.of(USER_WUA_ID));

    assertThat(caseProcessingActionService.getUserActionViews(applicationVersion, USER))
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactly(CaseProcessingActionView.from(CONSULTATION_MANAGE_RESPONDER, applicationVersion));
  }

  @Test
  void getUserActionViews_whenAssignedCaseOfficerNotCurrentUser_thenCannotStartTechnicalReviewOrAppUpdate() {
    when(applicationAccessService.getApplicationPermissionsForUser(applicationVersion, USER))
        .thenReturn(CASE_OFFICER_PERMISSIONS);
    when(caseStatusFlagService.getCaseStatusFlags(applicationVersion))
        .thenReturn(Set.of(CASE_OFFICER_ASSIGNED, NO_TECHNICAL_REVIEW_OPEN, NO_APPLICATION_UPDATE_OPEN, NO_CONSULTATION_OPEN));
    when(applicationVersionService.findCaseOfficerWuaId(applicationVersion))
        .thenReturn(Optional.of(OTHER_USER_WUA_ID));

    assertThat(caseProcessingActionService.getUserActionViews(applicationVersion, USER)).isEmpty();
  }

  @Test
  void getUserActionViews_whenAssignedCaseOfficerAndAppUpdateOpen_thenCannotStartTechnicalReviewOrAppUpdate() {
    when(applicationAccessService.getApplicationPermissionsForUser(applicationVersion, USER))
        .thenReturn(CASE_OFFICER_PERMISSIONS);
    when(caseStatusFlagService.getCaseStatusFlags(applicationVersion))
        .thenReturn(Set.of(CASE_OFFICER_ASSIGNED, NO_TECHNICAL_REVIEW_OPEN, APPLICATION_UPDATE_OPEN));
    when(applicationVersionService.findCaseOfficerWuaId(applicationVersion))
        .thenReturn(Optional.of(USER_WUA_ID));

    assertThat(caseProcessingActionService.getUserActionViews(applicationVersion, USER))
        .hasSize(2)
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactly(
            CaseProcessingActionView.from(CHANGE_ACE_STATUS, applicationVersion),
            CaseProcessingActionView.from(CASE_OFFICER_RELEASE_OWNERSHIP, applicationVersion)
        );
  }

  @Test
  void getUserActionViews_whenAssignedCaseOfficerAndTechReviewOpenAndAppUpdateOpen_thenCannotStartTechnicalReviewOrAppUpdate() {
    when(applicationAccessService.getApplicationPermissionsForUser(applicationVersion, USER))
        .thenReturn(CASE_OFFICER_PERMISSIONS);
    when(caseStatusFlagService.getCaseStatusFlags(applicationVersion))
        .thenReturn(Set.of(CASE_OFFICER_ASSIGNED, TECHNICAL_REVIEW_OPEN, APPLICATION_UPDATE_OPEN));
    when(applicationVersionService.findCaseOfficerWuaId(applicationVersion))
        .thenReturn(Optional.of(USER_WUA_ID));

    assertThat(caseProcessingActionService.getUserActionViews(applicationVersion, USER))
        .hasSize(2)
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactly(
            CaseProcessingActionView.from(CHANGE_ACE_STATUS, applicationVersion),
            CaseProcessingActionView.from(CASE_OFFICER_RELEASE_OWNERSHIP, applicationVersion)
        );
  }

  @Test
  void getUserActionViews_whenAssignedTechnicalReviewer_thenCanRequestAppUpdate() {
    when(applicationAccessService.getApplicationPermissionsForUser(applicationVersion, USER))
        .thenReturn(TECHNICAL_REVIEWER_PERMISSIONS);
    when(caseStatusFlagService.getCaseStatusFlags(applicationVersion))
        .thenReturn(Set.of(TECHNICAL_REVIEW_OPEN, NO_APPLICATION_UPDATE_OPEN, NO_CONSULTATION_OPEN));
    when(technicalReviewService.findTechnicalReviewerWuaId(applicationVersion))
        .thenReturn(Optional.of(USER_WUA_ID));

    assertThat(caseProcessingActionService.getUserActionViews(applicationVersion, USER))
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactly(
            CaseProcessingActionView.from(TECHNICAL_REVIEWER_SUBMIT_REVIEW, applicationVersion),
            CaseProcessingActionView.from(TECHNICAL_REVIEWER_REASSIGN_OWNERSHIP, applicationVersion),
            CaseProcessingActionView.from(APPLICATION_UPDATE_REQUEST, applicationVersion)
        );
  }

  @Test
  void getUserActionViews_whenAssignedTechnicalReviewerNotCurrentUser_thenCannotRequestAppUpdate() {
    when(applicationAccessService.getApplicationPermissionsForUser(applicationVersion, USER))
        .thenReturn(TECHNICAL_REVIEWER_PERMISSIONS);
    when(caseStatusFlagService.getCaseStatusFlags(applicationVersion))
        .thenReturn(Set.of(TECHNICAL_REVIEW_OPEN, NO_APPLICATION_UPDATE_OPEN));
    when(technicalReviewService.findTechnicalReviewerWuaId(applicationVersion))
        .thenReturn(Optional.of(OTHER_USER_WUA_ID));

    assertThat(caseProcessingActionService.getUserActionViews(applicationVersion, USER))
        .hasSize(1)
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactly(CaseProcessingActionView.from(TECHNICAL_REVIEWER_REASSIGN_OWNERSHIP, applicationVersion));
  }

  @Test
  void getUserActionViews_whenAssignedTechnicalReviewerAndAppUpdateOpen_thenCannotRequestAppUpdate() {
    when(applicationAccessService.getApplicationPermissionsForUser(applicationVersion, USER))
        .thenReturn(TECHNICAL_REVIEWER_PERMISSIONS);
    when(caseStatusFlagService.getCaseStatusFlags(applicationVersion))
        .thenReturn(Set.of(TECHNICAL_REVIEW_OPEN, APPLICATION_UPDATE_OPEN));
    when(technicalReviewService.findTechnicalReviewerWuaId(applicationVersion))
        .thenReturn(Optional.of(USER_WUA_ID));

    assertThat(caseProcessingActionService.getUserActionViews(applicationVersion, USER))
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactly(
            CaseProcessingActionView.from(TECHNICAL_REVIEWER_SUBMIT_REVIEW, applicationVersion),
            CaseProcessingActionView.from(TECHNICAL_REVIEWER_REASSIGN_OWNERSHIP, applicationVersion)
        );
  }

  @Test
  void getUserActionViews_whenIndustryEditorAndAppUpdateOpen_thenCannotUpdateApplication() {
    when(applicationAccessService.getApplicationPermissionsForUser(applicationVersion, USER))
        .thenReturn(INDUSTRY_EDITOR_PERMISSIONS);
    when(caseStatusFlagService.getCaseStatusFlags(applicationVersion))
        .thenReturn(Set.of(APPLICATION_UPDATE_OPEN));

    assertThat(caseProcessingActionService.getUserActionViews(applicationVersion, USER))
        .hasSize(1)
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactly(CaseProcessingActionView.from(OPERATOR_UPDATE_APPLICATION, applicationVersion));
  }

  @Test
  void getUserActionViews_whenIndustryEditorAndNoAppUpdateOpen_thenCannotUpdateApplication() {
    when(applicationAccessService.getApplicationPermissionsForUser(applicationVersion, USER))
        .thenReturn(INDUSTRY_EDITOR_PERMISSIONS);
    when(caseStatusFlagService.getCaseStatusFlags(applicationVersion))
        .thenReturn(Set.of(NO_APPLICATION_UPDATE_OPEN));

    assertThat(caseProcessingActionService.getUserActionViews(applicationVersion, USER))
        .isEmpty();
  }
}
