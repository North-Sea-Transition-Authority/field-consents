package uk.co.nstauthority.fieldconsents.application.caseprocessing.action;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.ApplicationType.FLARE;
import static uk.co.nstauthority.fieldconsents.application.ApplicationType.PRODUCTION;
import static uk.co.nstauthority.fieldconsents.application.ApplicationType.VENT;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.APPLICATION_UPDATES;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.APPLICATION_UPDATE_REQUEST;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.APPROVE_FOR_ISSUING;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CAM_ASSIGN_OWNERSHIP;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CAM_REASSIGN_OWNERSHIP;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CASE_OFFICER_ASSIGN_OWNERSHIP;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CASE_OFFICER_REASSIGN_OWNERSHIP;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CASE_OFFICER_RELEASE_OWNERSHIP;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CASE_OFFICER_TAKE_OWNERSHIP;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CASE_OFFICER_WITHDRAWAL_RESPONSE;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CHANGE_ACE_STATUS;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CONSENT_ISSUING;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CONSENT_PREPARATION;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CONSULTATIONS;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CONSULTATION_FURTHER_INFORMATION_REQUEST;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CONSULTATION_FURTHER_INFORMATION_RESPOND;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CONSULTATION_MANAGE_RESPONDER;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CONSULTATION_REQUEST;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CONSULTATION_RESPONSE;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.EDIT_CONSENT_DATA;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.EDIT_CONSENT_DOCUMENTS;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.ISSUE_CONSENT;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.OPERATOR_PAY_AND_SUBMIT_APPLICATION;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.OPERATOR_RETURN_APPLICATION_TO_IN_PROGRESS_FROM_AWAITING_PAYMENT;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.OPERATOR_UPDATE_APPLICATION;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.OPERATOR_WITHDRAWAL_REQUEST;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.REGULATOR_ADD_CASE_NOTE;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.RETURN_TO_CASE_OFFICER;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.TECHNICAL_REVIEWER_REASSIGN_OWNERSHIP;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.TECHNICAL_REVIEWER_SUBMIT_REVIEW;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.TECHNICAL_REVIEWS;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.TECHNICAL_REVIEW_REQUEST;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.UNAPPROVE_FOR_ISSUING;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.APPLICATION_UPDATE_OPEN;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.CAM_ASSIGNED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.CAM_NOT_ASSIGNED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.CASE_NOTES_ALLOWED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.CASE_OFFICER_ASSIGNED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.CASE_OFFICER_NOT_ASSIGNED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.CONSENT_APPROVED_FOR_ISSUE;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.CONSENT_DATA_EXISTS;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.CONSENT_NOT_APPROVED_FOR_ISSUE;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.CONSULTATION_FURTHER_INFORMATION_OPEN;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.CONSULTATION_OPEN;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.NO_APPLICATION_UPDATE_OPEN;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.NO_CONSULTATION_FURTHER_INFORMATION_OPEN;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.NO_CONSULTATION_OPEN;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.NO_TECHNICAL_REVIEW_OPEN;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.NO_WITHDRAWAL_OPEN;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.WITHDRAWAL_OPEN;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.ALLOCATE_CONSULTATION;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.ASSIGN_FCS_APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.AUTHORISE_FCS_CONSENTS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.EDIT_FCS_APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.EDIT_FCS_CASE_PROCESSING_DOCUMENTS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.PAY_AND_SUBMIT_FCS_APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.PROCESS_FCS_APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.RESPOND_TO_CONSULTATION;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.TECHNICAL_REVIEW_FCS_APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.VIEW_FCS_CASE_PROCESSING_DOCUMENTS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.opred.OpredTeamRole.RESPONDER;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamRole.CASE_OFFICER;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamRole.CONSENTS_AND_AUTHORISATIONS_MANAGER;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamRole.TECHNICAL_REVIEWER;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment.CaseAssignmentService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment.cam.CamAssignmentService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlagService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.Consultation;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.tasklist.CaseProcessingTaskListSection;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.authorisation.ApplicationAccessService;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.TeamRole;

@ExtendWith(MockitoExtension.class)
class CaseProcessingActionServiceTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();
  private static final WebUserAccountId USER_WUA_ID = WebUserAccountId.from(USER.wuaId());

  @Mock
  private ApplicationAccessService applicationAccessService;

  @Mock
  private CaseStatusFlagService caseStatusFlagService;

  @Mock
  private TechnicalReviewService technicalReviewService;

  @Mock
  private ConsultationService consultationService;

  @Mock
  private CaseAssignmentService caseAssignmentService;

  @Mock
  private CamAssignmentService camAssignmentService;

  @Spy
  @InjectMocks
  private CaseProcessingActionService caseProcessingActionService;

  private ApplicationVersion applicationVersion;

  private Application application;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.FLARE);
    applicationVersion.setCurrentCaseOwner(CASE_OFFICER);
    application = applicationVersion.getApplication();
  }

  @ParameterizedTest
  @MethodSource("getUserActionItems_arguments")
  void getUserActionItems_inProgress(Set<RolePermission> rolePermissions, Set<CaseStatusFlag> caseStatusFlags, ExpectedActions expectedActions) {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(PRODUCTION);
    getUserActionItems(applicationVersion, rolePermissions, caseStatusFlags, expectedActions.inProgressActions());
  }

  @ParameterizedTest
  @MethodSource("getUserActionItems_arguments")
  void getUserActionItems_awaitingPayment(Set<RolePermission> rolePermissions, Set<CaseStatusFlag> caseStatusFlags, ExpectedActions expectedActions) {
    var applicationVersion = ApplicationTestUtil.getAwaitingPaymentApplicationVersionWithType(PRODUCTION);
    getUserActionItems(applicationVersion, rolePermissions, caseStatusFlags, expectedActions.awaitingPaymentActions());
  }

  @ParameterizedTest
  @MethodSource("getUserActionItems_arguments")
  void getUserActionItems_submitted(Set<RolePermission> rolePermissions, Set<CaseStatusFlag> caseStatusFlags, ExpectedActions expectedActions) {
    var applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(PRODUCTION);
    getUserActionItems(applicationVersion, rolePermissions, caseStatusFlags, expectedActions.submittedActions());
  }

  @ParameterizedTest
  @MethodSource("getUserActionItems_arguments")
  void getUserActionItems_withdrawn(Set<RolePermission> rolePermissions, Set<CaseStatusFlag> caseStatusFlags, ExpectedActions expectedActions) {
    var applicationVersion = ApplicationTestUtil.getWithdrawnApplicationVersionWithType(PRODUCTION);
    getUserActionItems(applicationVersion, rolePermissions, caseStatusFlags, expectedActions.withdrawnActions());
  }

  @ParameterizedTest
  @MethodSource("getUserActionItems_arguments")
  void getUserActionItems_completed(Set<RolePermission> rolePermissions, Set<CaseStatusFlag> caseStatusFlags, ExpectedActions expectedActions) {
    var applicationVersion = ApplicationTestUtil.getCompletedApplicationVersionWithType(PRODUCTION);
    getUserActionItems(applicationVersion, rolePermissions, caseStatusFlags, expectedActions.completedActions());
  }

  private void getUserActionItems(
      ApplicationVersion applicationVersion,
      Set<RolePermission> rolePermissions,
      Set<CaseStatusFlag> caseStatusFlags,
      List<CaseProcessingActionItem> expectedActionItems
  ) {
    when(applicationAccessService.getApplicationPermissionsForUser(applicationVersion, USER)).thenReturn(rolePermissions);
    when(caseStatusFlagService.getCaseStatusFlags(applicationVersion)).thenReturn(caseStatusFlags);

    var webUserAccountIdByTeamRole = Collections.<TeamRole, WebUserAccountId>emptyMap();
    doReturn(webUserAccountIdByTeamRole)
        .when(caseProcessingActionService)
        .constructAssigneeMap(applicationVersion);

    doReturn(true)
        .when(caseProcessingActionService)
        .applicationTypeFeatureFlagAllowed(eq(applicationVersion), any(CaseProcessingActionItem.class));

    lenient().doReturn(true)
        .when(caseProcessingActionService)
        .isActionEnabledForUser(any(CaseProcessingActionItem.class), eq(webUserAccountIdByTeamRole), eq(USER));

    assertThat(caseProcessingActionService.getUserActionItems(applicationVersion, USER)).containsExactlyElementsOf(expectedActionItems);
  }

  private static Stream<Arguments> getUserActionItems_arguments() {
    return Stream.of(
        arguments(
            Set.of(PROCESS_FCS_APPLICATIONS),
            Set.of(CASE_OFFICER_ASSIGNED),
            ExpectedActions.newBuilder()
                .inProgressActions(CHANGE_ACE_STATUS, CASE_OFFICER_RELEASE_OWNERSHIP)
                .submittedActions(CHANGE_ACE_STATUS, CASE_OFFICER_RELEASE_OWNERSHIP, CONSENT_PREPARATION)
                .build()
        ),
        arguments(
            Set.of(PROCESS_FCS_APPLICATIONS),
            Set.of(CASE_OFFICER_ASSIGNED, WITHDRAWAL_OPEN),
            ExpectedActions.newBuilder()
                .inProgressActions(CHANGE_ACE_STATUS, CASE_OFFICER_RELEASE_OWNERSHIP)
                .submittedActions(CHANGE_ACE_STATUS, CASE_OFFICER_RELEASE_OWNERSHIP, CASE_OFFICER_WITHDRAWAL_RESPONSE, CONSENT_PREPARATION)
                .build()
        ),
        arguments(
            Set.of(PROCESS_FCS_APPLICATIONS),
            Set.of(CASE_OFFICER_ASSIGNED, NO_TECHNICAL_REVIEW_OPEN, NO_APPLICATION_UPDATE_OPEN),
            ExpectedActions.newBuilder()
                .inProgressActions(CHANGE_ACE_STATUS, CASE_OFFICER_RELEASE_OWNERSHIP)
                .submittedActions(CHANGE_ACE_STATUS, CASE_OFFICER_RELEASE_OWNERSHIP, TECHNICAL_REVIEW_REQUEST, CONSENT_PREPARATION, APPLICATION_UPDATE_REQUEST)
                .build()
        ),
        arguments(
            Set.of(TECHNICAL_REVIEW_FCS_APPLICATIONS),
            Set.of(CaseStatusFlag.TECHNICAL_REVIEW_OPEN),
            ExpectedActions.newBuilder()
                .submittedActions(TECHNICAL_REVIEWER_SUBMIT_REVIEW, TECHNICAL_REVIEWER_REASSIGN_OWNERSHIP)
                .build()
        ),
        arguments(
            Set.of(PROCESS_FCS_APPLICATIONS, TECHNICAL_REVIEW_FCS_APPLICATIONS),
            Set.of(NO_APPLICATION_UPDATE_OPEN),
            ExpectedActions.newBuilder()
                .submittedActions(CONSENT_PREPARATION, APPLICATION_UPDATE_REQUEST)
                .build()
        ),
        arguments(
            Set.of(PROCESS_FCS_APPLICATIONS),
            Set.of(CASE_OFFICER_ASSIGNED, NO_TECHNICAL_REVIEW_OPEN, NO_CONSULTATION_OPEN),
            ExpectedActions.newBuilder()
                .inProgressActions(CHANGE_ACE_STATUS, CASE_OFFICER_RELEASE_OWNERSHIP)
                .submittedActions(CHANGE_ACE_STATUS, CASE_OFFICER_RELEASE_OWNERSHIP, CONSENT_PREPARATION, CONSULTATION_REQUEST)
                .build()
        ),
        arguments(
            Set.of(RESPOND_TO_CONSULTATION),
            Set.of(CONSULTATION_OPEN, NO_CONSULTATION_FURTHER_INFORMATION_OPEN),
            ExpectedActions.newBuilder()
                .submittedActions(CONSULTATION_RESPONSE, CONSULTATION_FURTHER_INFORMATION_REQUEST)
                .build()
        ),
        arguments(
            Set.of(RESPOND_TO_CONSULTATION),
            Set.of(NO_CONSULTATION_FURTHER_INFORMATION_OPEN),
            ExpectedActions.newBuilder()
                .submittedActions(CONSULTATION_FURTHER_INFORMATION_REQUEST)
                .build()
        ),
        arguments(
            Set.of(PROCESS_FCS_APPLICATIONS),
            Set.of(CONSULTATION_FURTHER_INFORMATION_OPEN, NO_APPLICATION_UPDATE_OPEN),
            ExpectedActions.newBuilder()
                .submittedActions(CONSENT_PREPARATION, APPLICATION_UPDATE_REQUEST, CONSULTATION_FURTHER_INFORMATION_RESPOND)
                .build()
        ),
        arguments(
            Set.of(PROCESS_FCS_APPLICATIONS),
            Set.of(CASE_OFFICER_NOT_ASSIGNED, CAM_NOT_ASSIGNED),
            ExpectedActions.newBuilder()
                .inProgressActions(CASE_OFFICER_TAKE_OWNERSHIP)
                .submittedActions(CASE_OFFICER_TAKE_OWNERSHIP, CONSENT_PREPARATION)
                .build()
        ),
        arguments(
            Set.of(PROCESS_FCS_APPLICATIONS),
            Set.of(CASE_OFFICER_ASSIGNED, CAM_NOT_ASSIGNED, CONSENT_NOT_APPROVED_FOR_ISSUE),
            ExpectedActions.newBuilder()
                .inProgressActions(CHANGE_ACE_STATUS, CASE_OFFICER_RELEASE_OWNERSHIP)
                .submittedActions(
                    CHANGE_ACE_STATUS,
                    CASE_OFFICER_RELEASE_OWNERSHIP,
                    EDIT_CONSENT_DATA,
                    EDIT_CONSENT_DOCUMENTS,
                    CONSENT_PREPARATION
                )
                .build()
        ),
        arguments(
            Set.of(ASSIGN_FCS_APPLICATIONS),
            Set.of(CASE_OFFICER_NOT_ASSIGNED, CAM_NOT_ASSIGNED),
            ExpectedActions.newBuilder()
                .inProgressActions(CASE_OFFICER_ASSIGN_OWNERSHIP)
                .submittedActions(CASE_OFFICER_ASSIGN_OWNERSHIP, CONSENT_PREPARATION)
                .build()
        ),
        arguments(
            Set.of(ASSIGN_FCS_APPLICATIONS),
            Set.of(CASE_OFFICER_ASSIGNED, CAM_NOT_ASSIGNED),
            ExpectedActions.newBuilder()
                .inProgressActions(CASE_OFFICER_REASSIGN_OWNERSHIP)
                .submittedActions(CASE_OFFICER_REASSIGN_OWNERSHIP, CONSENT_PREPARATION)
                .build()
        ),
        arguments(
            Set.of(AUTHORISE_FCS_CONSENTS),
            Set.of(CASE_OFFICER_NOT_ASSIGNED, CAM_ASSIGNED),
            ExpectedActions.newBuilder()
                .submittedActions(CAM_REASSIGN_OWNERSHIP)
                .build()
        ),
        arguments(
            Set.of(ASSIGN_FCS_APPLICATIONS),
            Set.of(CASE_OFFICER_NOT_ASSIGNED, CAM_ASSIGNED),
            ExpectedActions.newBuilder()
                .submittedActions(CAM_REASSIGN_OWNERSHIP, CONSENT_PREPARATION)
                .build()
        ),
        arguments(
            Set.of(VIEW_FCS_CASE_PROCESSING_DOCUMENTS),
            Set.of(),
            ExpectedActions.newBuilder()
                .inProgressActions(TECHNICAL_REVIEWS, CONSULTATIONS, APPLICATION_UPDATES)
                .submittedActions(TECHNICAL_REVIEWS, CONSULTATIONS, APPLICATION_UPDATES)
                .withdrawnActions(TECHNICAL_REVIEWS, CONSULTATIONS, APPLICATION_UPDATES)
                .completedActions(TECHNICAL_REVIEWS, CONSULTATIONS, APPLICATION_UPDATES)
                .build()
        ),
        arguments(
            Set.of(EDIT_FCS_CASE_PROCESSING_DOCUMENTS),
            Set.of(CASE_NOTES_ALLOWED),
            ExpectedActions.newBuilder()
                .inProgressActions(REGULATOR_ADD_CASE_NOTE)
                .submittedActions(REGULATOR_ADD_CASE_NOTE)
                .withdrawnActions(REGULATOR_ADD_CASE_NOTE)
                .completedActions(REGULATOR_ADD_CASE_NOTE)
                .build()
        ),
        arguments(
            Set.of(TECHNICAL_REVIEW_FCS_APPLICATIONS),
            Set.of(CaseStatusFlag.TECHNICAL_REVIEW_OPEN),
            ExpectedActions.newBuilder()
                .submittedActions(TECHNICAL_REVIEWER_SUBMIT_REVIEW, TECHNICAL_REVIEWER_REASSIGN_OWNERSHIP)
                .build()
        ),
        arguments(
            Set.of(EDIT_FCS_APPLICATIONS),
            Set.of(NO_WITHDRAWAL_OPEN, NO_APPLICATION_UPDATE_OPEN),
            ExpectedActions.newBuilder()
                .awaitingPaymentActions(OPERATOR_RETURN_APPLICATION_TO_IN_PROGRESS_FROM_AWAITING_PAYMENT)
                .submittedActions(OPERATOR_WITHDRAWAL_REQUEST)
                .build()
        ),
        arguments(
            Set.of(EDIT_FCS_APPLICATIONS),
            Set.of(APPLICATION_UPDATE_OPEN),
            ExpectedActions.newBuilder()
                .inProgressActions(OPERATOR_UPDATE_APPLICATION)
                .submittedActions(OPERATOR_UPDATE_APPLICATION)
                .build()
        ),
        arguments(
            Set.of(ALLOCATE_CONSULTATION),
            Set.of(CONSULTATION_OPEN),
            ExpectedActions.newBuilder()
                .submittedActions(CONSULTATION_MANAGE_RESPONDER)
                .build()
        ),
        arguments(
            Set.of(PAY_AND_SUBMIT_FCS_APPLICATIONS),
            Set.of(NO_APPLICATION_UPDATE_OPEN),
            ExpectedActions.newBuilder()
                .awaitingPaymentActions(OPERATOR_PAY_AND_SUBMIT_APPLICATION)
                .build()
        ),
        arguments(
            Set.of(AUTHORISE_FCS_CONSENTS),
            Set.of(CASE_OFFICER_NOT_ASSIGNED, CAM_ASSIGNED),
            ExpectedActions.newBuilder()
                .submittedActions(CAM_REASSIGN_OWNERSHIP)
                .build()
        ),
        arguments(
            Set.of(AUTHORISE_FCS_CONSENTS),
            Set.of(CONSENT_DATA_EXISTS),
            ExpectedActions.newBuilder()
                .submittedActions(CONSENT_ISSUING)
                .build()
        ),
        arguments(
            Set.of(AUTHORISE_FCS_CONSENTS),
            Set.of(CONSENT_DATA_EXISTS, CONSENT_NOT_APPROVED_FOR_ISSUE),
            ExpectedActions.newBuilder()
                .submittedActions(CONSENT_ISSUING, APPROVE_FOR_ISSUING)
                .build()
        ),
        arguments(
            Set.of(AUTHORISE_FCS_CONSENTS),
            Set.of(CAM_ASSIGNED, CASE_OFFICER_NOT_ASSIGNED, CONSENT_NOT_APPROVED_FOR_ISSUE),
            ExpectedActions.newBuilder()
                .submittedActions(CAM_REASSIGN_OWNERSHIP, RETURN_TO_CASE_OFFICER)
                .build()
        ),
        arguments(
            Set.of(AUTHORISE_FCS_CONSENTS),
            Set.of(CAM_ASSIGNED, CASE_OFFICER_NOT_ASSIGNED, CONSENT_APPROVED_FOR_ISSUE),
            ExpectedActions.newBuilder()
                .submittedActions(CAM_REASSIGN_OWNERSHIP, ISSUE_CONSENT)
                .build()
        )
    );
  }

  @Test
  void getUserActionViews_excludingTaskListActionItemsAndGroupedActionItems() {
    var actionItems = EnumSet.allOf(CaseProcessingActionItem.class)
        .stream()
        .sorted(Comparator.comparingInt(CaseProcessingActionItem::getDisplayOrder))
        .toList();

    var taskListActionItems = Set.of(
        TECHNICAL_REVIEWS,
        CONSULTATIONS,
        CONSENT_PREPARATION,
        CONSENT_ISSUING,
        CHANGE_ACE_STATUS,
        APPLICATION_UPDATES,
        REGULATOR_ADD_CASE_NOTE
    );

    var groupedActionItems = Set.of(
        TECHNICAL_REVIEW_REQUEST,
        CONSULTATION_REQUEST,
        CONSULTATION_FURTHER_INFORMATION_RESPOND,
        APPLICATION_UPDATE_REQUEST,
        EDIT_CONSENT_DATA,
        EDIT_CONSENT_DOCUMENTS,
        CAM_ASSIGN_OWNERSHIP,
        CAM_REASSIGN_OWNERSHIP,
        RETURN_TO_CASE_OFFICER,
        APPROVE_FOR_ISSUING,
        ISSUE_CONSENT,
        UNAPPROVE_FOR_ISSUING
    );

    var actionViews = actionItems.stream()
        .filter(actionItem -> !taskListActionItems.contains(actionItem)) // exclude these actions
        .filter(actionItem -> !groupedActionItems.contains(actionItem)) // exclude these actions
        .map(actionItem -> CaseProcessingActionView.from(actionItem, applicationVersion))
        .toList();

    doReturn(actionItems)
        .when(caseProcessingActionService)
        .getUserActionItems(applicationVersion, USER);

    assertThat(caseProcessingActionService.getUserActionViews(applicationVersion, USER))
        .containsExactlyElementsOf(actionViews);
  }

  @ParameterizedTest
  @MethodSource("getUserActionViewsForGroup_arguments")
  void getUserActionViewsForGroup(
      CaseProcessingActionGroup actionGroup,
      List<CaseProcessingActionItem> expectedActionItems
  ) {
    // get all available actions
    doReturn(Arrays.asList(CaseProcessingActionItem.values()))
        .when(caseProcessingActionService)
        .getUserActionItems(applicationVersion, USER);

    var expectedActionViews = expectedActionItems.stream()
        .map(actionItem -> CaseProcessingActionView.from(actionItem, applicationVersion))
        .toList();

    assertThat(caseProcessingActionService.getUserActionViewsForGroup(applicationVersion, USER, actionGroup))
        .containsExactlyInAnyOrderElementsOf(expectedActionViews);
  }

  private static Stream<Arguments> getUserActionViewsForGroup_arguments() {
    return Stream.of(
        arguments(
            CaseProcessingActionGroup.CONSULTATIONS,
            List.of(CONSULTATION_FURTHER_INFORMATION_RESPOND, CONSULTATION_REQUEST)
        ),
        arguments(
            CaseProcessingActionGroup.TECHNICAL_REVIEWS,
            List.of(TECHNICAL_REVIEW_REQUEST)
        ),
        arguments(
            CaseProcessingActionGroup.APPLICATION_UPDATES,
            List.of(APPLICATION_UPDATE_REQUEST)
        ),
        arguments(
            CaseProcessingActionGroup.CONSENT_PREPARATION,
            List.of(CAM_ASSIGN_OWNERSHIP, CAM_REASSIGN_OWNERSHIP)
        ),
        arguments(
            CaseProcessingActionGroup.CONSENT_PREPARATION_CONSENT_DATA_CARD,
            List.of(EDIT_CONSENT_DATA)
        ),
        arguments(
            CaseProcessingActionGroup.CONSENT_PREPARATION_CONSENT_DOCUMENTS_CARD,
            List.of(EDIT_CONSENT_DOCUMENTS)
        ),
        arguments(
            CaseProcessingActionGroup.CONSENT_ISSUING,
            List.of(CAM_REASSIGN_OWNERSHIP, RETURN_TO_CASE_OFFICER, APPROVE_FOR_ISSUING, ISSUE_CONSENT, UNAPPROVE_FOR_ISSUING)
        )
    );
  }

  @Test
  void groupActionItemsByTaskListSection() {
    // we don't care about the ordering here because it's not going directly into a view
    assertThat(caseProcessingActionService.groupActionItemsByTaskListSection(EnumSet.allOf(CaseProcessingActionItem.class)))
        .containsExactlyInAnyOrderEntriesOf(Map.of(
            CaseProcessingTaskListSection.CASE_TASKS, List.of(TECHNICAL_REVIEWS, CONSULTATIONS, CONSENT_PREPARATION, CONSENT_ISSUING),
            CaseProcessingTaskListSection.OPTIONAL_CASE_TASKS, List.of(CHANGE_ACE_STATUS, APPLICATION_UPDATES, REGULATOR_ADD_CASE_NOTE)
        ));
  }

  @ParameterizedTest
  @MethodSource("constructAssigneeMap_arguments")
  void constructAssigneeMap(
      WebUserAccountId caseOfficerWuaId,
      WebUserAccountId technicalReviewerWuaId,
      WebUserAccountId responderWuaId,
      Map<TeamRole, WebUserAccountId> expectedAssigneeMap
  ) {
    when(caseAssignmentService.findCaseOfficerWuaId(applicationVersion)).thenReturn(Optional.ofNullable(caseOfficerWuaId));
    when(technicalReviewService.findTechnicalReviewerWuaId(applicationVersion)).thenReturn(Optional.ofNullable(technicalReviewerWuaId));

    var consultation = new Consultation();
    Optional.ofNullable(responderWuaId).map(WebUserAccountId::id).ifPresent(consultation::setResponderWuaId);
    when(consultationService.findLatestOpenConsultation(application)).thenReturn(Optional.of(consultation));

    assertThat(caseProcessingActionService.constructAssigneeMap(applicationVersion)).containsExactlyInAnyOrderEntriesOf(expectedAssigneeMap);
  }

  @Test
  void constructAssigneeMap_whenAssignedToCam() {
    applicationVersion.setCamWuaId(USER_WUA_ID.id());
    applicationVersion.setCurrentCaseOwner(CONSENTS_AND_AUTHORISATIONS_MANAGER);

    when(camAssignmentService.findCamWuaId(applicationVersion)).thenReturn(Optional.of(USER_WUA_ID));
    when(technicalReviewService.findTechnicalReviewerWuaId(applicationVersion)).thenReturn(Optional.empty());
    when(consultationService.findLatestOpenConsultation(application)).thenReturn(Optional.empty());

    assertThat(caseProcessingActionService.constructAssigneeMap(applicationVersion)).containsExactlyInAnyOrderEntriesOf(Map.of(
        CONSENTS_AND_AUTHORISATIONS_MANAGER, USER_WUA_ID
    ));
  }

  private static Stream<Arguments> constructAssigneeMap_arguments() {
    return Stream.of(
        arguments(
            null,
            null,
            null,
            emptyMap()
        ),
        arguments(
            USER_WUA_ID,
            null,
            null,
            Map.of(CASE_OFFICER, USER_WUA_ID)
        ),
        arguments(
            null,
            USER_WUA_ID,
            null,
            Map.of(TECHNICAL_REVIEWER, USER_WUA_ID)
        ),
        arguments(
            null,
            null,
            USER_WUA_ID,
            Map.of(RESPONDER, USER_WUA_ID)
        ),
        arguments(
            USER_WUA_ID,
            USER_WUA_ID,
            USER_WUA_ID,
            Map.of(
                CASE_OFFICER, USER_WUA_ID,
                TECHNICAL_REVIEWER, USER_WUA_ID,
                RESPONDER, USER_WUA_ID
            )
        )
    );
  }

  @ParameterizedTest
  @MethodSource({
      "isActionEnabledForUser_assigneeOnly_arguments",
      "isActionEnabledForUser_nonAssignee_arguments"
  })
  void isActionEnabledForUser(
      CaseProcessingActionItem actionItem,
      Map<TeamRole, WebUserAccountId> assigneeMap,
      boolean isActionEnabledForUser
  ) {
    assertThat(caseProcessingActionService.isActionEnabledForUser(actionItem, assigneeMap, USER)).isEqualTo(isActionEnabledForUser);
  }

  private static Stream<Arguments> isActionEnabledForUser_nonAssignee_arguments() {
    return Stream.of(
        arguments(CASE_OFFICER_TAKE_OWNERSHIP, emptyMap(), true),
        arguments(CASE_OFFICER_ASSIGN_OWNERSHIP, emptyMap(), true),
        arguments(CASE_OFFICER_REASSIGN_OWNERSHIP, emptyMap(), true),
        arguments(TECHNICAL_REVIEWS, emptyMap(), true),
        arguments(REGULATOR_ADD_CASE_NOTE, emptyMap(), true),
        arguments(TECHNICAL_REVIEWER_REASSIGN_OWNERSHIP, emptyMap(), true),
        arguments(OPERATOR_PAY_AND_SUBMIT_APPLICATION, emptyMap(), true),
        arguments(OPERATOR_RETURN_APPLICATION_TO_IN_PROGRESS_FROM_AWAITING_PAYMENT, emptyMap(), true),
        arguments(OPERATOR_WITHDRAWAL_REQUEST, emptyMap(), true),
        arguments(OPERATOR_UPDATE_APPLICATION, emptyMap(), true),
        arguments(CONSULTATION_MANAGE_RESPONDER, emptyMap(), true)
    );
  }

  private static Stream<Arguments> isActionEnabledForUser_assigneeOnly_arguments() {
    var caseOfficerAssigneeMap = Map.of(CASE_OFFICER, USER_WUA_ID);
    var technicalReviewerAssigneeMap = Map.of(TECHNICAL_REVIEWER, USER_WUA_ID);
    var responderAssigneeMap = Map.of(RESPONDER, USER_WUA_ID);
    var consentsAndAuthorisationsManagerMap = Map.of(CONSENTS_AND_AUTHORISATIONS_MANAGER, USER_WUA_ID);

    return Stream.of(
        arguments(CHANGE_ACE_STATUS, caseOfficerAssigneeMap, true),
        arguments(CASE_OFFICER_RELEASE_OWNERSHIP, caseOfficerAssigneeMap, true),
        arguments(CASE_OFFICER_WITHDRAWAL_RESPONSE, caseOfficerAssigneeMap, true),
        arguments(TECHNICAL_REVIEW_REQUEST, caseOfficerAssigneeMap, true),
        arguments(APPLICATION_UPDATE_REQUEST, caseOfficerAssigneeMap, true),
        arguments(APPLICATION_UPDATE_REQUEST, technicalReviewerAssigneeMap, true),
        arguments(TECHNICAL_REVIEWER_SUBMIT_REVIEW, technicalReviewerAssigneeMap, true),
        arguments(CONSULTATION_REQUEST, caseOfficerAssigneeMap, true),
        arguments(CONSULTATION_RESPONSE, responderAssigneeMap, true),
        arguments(CONSULTATION_FURTHER_INFORMATION_REQUEST, responderAssigneeMap, true),
        arguments(CONSULTATION_FURTHER_INFORMATION_RESPOND, caseOfficerAssigneeMap, true),
        arguments(EDIT_CONSENT_DATA, caseOfficerAssigneeMap, true),
        arguments(EDIT_CONSENT_DOCUMENTS, caseOfficerAssigneeMap, true),
        arguments(APPROVE_FOR_ISSUING, consentsAndAuthorisationsManagerMap, true),
        arguments(RETURN_TO_CASE_OFFICER, consentsAndAuthorisationsManagerMap, true),
        arguments(ISSUE_CONSENT, consentsAndAuthorisationsManagerMap, true),

        // when not assigned
        arguments(CHANGE_ACE_STATUS, emptyMap(), false),
        arguments(CASE_OFFICER_RELEASE_OWNERSHIP, emptyMap(), false),
        arguments(CASE_OFFICER_WITHDRAWAL_RESPONSE, emptyMap(), false),
        arguments(TECHNICAL_REVIEW_REQUEST, emptyMap(), false),
        arguments(APPLICATION_UPDATE_REQUEST, emptyMap(), false),
        arguments(APPLICATION_UPDATE_REQUEST, emptyMap(), false),
        arguments(TECHNICAL_REVIEWER_SUBMIT_REVIEW, emptyMap(), false),
        arguments(CONSULTATION_REQUEST, emptyMap(), false),
        arguments(CONSULTATION_RESPONSE, emptyMap(), false),
        arguments(CONSULTATION_FURTHER_INFORMATION_REQUEST, emptyMap(), false),
        arguments(CONSULTATION_FURTHER_INFORMATION_RESPOND, emptyMap(), false),
        arguments(EDIT_CONSENT_DATA, emptyMap(), false),
        arguments(EDIT_CONSENT_DOCUMENTS, emptyMap(), false),
        arguments(APPROVE_FOR_ISSUING, emptyMap(), false),
        arguments(RETURN_TO_CASE_OFFICER, emptyMap(), false),
        arguments(ISSUE_CONSENT, emptyMap(), false)
    );
  }

  @ParameterizedTest
  @MethodSource({
      "applicationTypeFeatureFlagAllowed_production_arguments",
      "applicationTypeFeatureFlagAllowed_flare_arguments",
      "applicationTypeFeatureFlagAllowed_vent_arguments",
  })
  void applicationTypeFeatureFlagAllowed(
      ApplicationVersion applicationVersion,
      CaseProcessingActionItem actionItem,
      boolean isAllowed
  ) {
    assertThat(caseProcessingActionService.applicationTypeFeatureFlagAllowed(applicationVersion, actionItem)).isEqualTo(isAllowed);
  }

  private static Stream<Arguments> applicationTypeFeatureFlagAllowed_production_arguments() {
    var productionApplication = ApplicationTestUtil.getNewApplicationVersionWithType(PRODUCTION);

    return EnumSet.allOf(CaseProcessingActionItem.class)
        .stream()
        .map(actionItem -> arguments(productionApplication, actionItem, true));
  }

  private static Stream<Arguments> applicationTypeFeatureFlagAllowed_flare_arguments() {
    var flareApplication = ApplicationTestUtil.getNewApplicationVersionWithType(FLARE);

    return EnumSet.allOf(CaseProcessingActionItem.class)
        .stream()
        .map(actionItem -> arguments(flareApplication, actionItem, true));
  }

  private static Stream<Arguments> applicationTypeFeatureFlagAllowed_vent_arguments() {
    var ventApplication = ApplicationTestUtil.getNewApplicationVersionWithType(VENT);

    return Stream.of(
        arguments(ventApplication, CONSULTATION_REQUEST, false),

        arguments(ventApplication, CASE_OFFICER_TAKE_OWNERSHIP, true),
        arguments(ventApplication, CHANGE_ACE_STATUS, true),
        arguments(ventApplication, CASE_OFFICER_RELEASE_OWNERSHIP, true),
        arguments(ventApplication, CASE_OFFICER_WITHDRAWAL_RESPONSE, true),
        arguments(ventApplication, TECHNICAL_REVIEW_REQUEST, true),
        arguments(ventApplication, CASE_OFFICER_ASSIGN_OWNERSHIP, true),
        arguments(ventApplication, CASE_OFFICER_REASSIGN_OWNERSHIP, true),
        arguments(ventApplication, TECHNICAL_REVIEWS, true),
        arguments(ventApplication, REGULATOR_ADD_CASE_NOTE, true),
        arguments(ventApplication, TECHNICAL_REVIEWER_SUBMIT_REVIEW, true),
        arguments(ventApplication, TECHNICAL_REVIEWER_REASSIGN_OWNERSHIP, true),
        arguments(ventApplication, APPLICATION_UPDATE_REQUEST, true),
        arguments(ventApplication, OPERATOR_PAY_AND_SUBMIT_APPLICATION, true),
        arguments(ventApplication, OPERATOR_RETURN_APPLICATION_TO_IN_PROGRESS_FROM_AWAITING_PAYMENT, true),
        arguments(ventApplication, OPERATOR_WITHDRAWAL_REQUEST, true),
        arguments(ventApplication, OPERATOR_UPDATE_APPLICATION, true),
        arguments(ventApplication, CONSULTATION_RESPONSE, true),
        arguments(ventApplication, CONSULTATION_MANAGE_RESPONDER, true),
        arguments(ventApplication, CONSULTATION_FURTHER_INFORMATION_REQUEST, true),
        arguments(ventApplication, CONSULTATION_FURTHER_INFORMATION_RESPOND, true)
    );
  }

  private record ExpectedActions(
      List<CaseProcessingActionItem> inProgressActions,
      List<CaseProcessingActionItem> awaitingPaymentActions,
      List<CaseProcessingActionItem> submittedActions,
      List<CaseProcessingActionItem> withdrawnActions,
      List<CaseProcessingActionItem> completedActions
  ) {

    public static Builder newBuilder() {
      return new Builder();
    }

    public static class Builder {

      private final List<CaseProcessingActionItem> inProgressActions = new ArrayList<>();
      private final List<CaseProcessingActionItem> awaitingPaymentActions = new ArrayList<>();
      private final List<CaseProcessingActionItem> submittedActions = new ArrayList<>();
      private final List<CaseProcessingActionItem> withdrawnActions = new ArrayList<>();
      private final List<CaseProcessingActionItem> completedActions = new ArrayList<>();

      public Builder inProgressActions(CaseProcessingActionItem... caseProcessingActionItems) {
        Collections.addAll(inProgressActions, caseProcessingActionItems);
        return this;
      }

      public Builder awaitingPaymentActions(CaseProcessingActionItem... caseProcessingActionItems) {
        Collections.addAll(awaitingPaymentActions, caseProcessingActionItems);
        return this;
      }

      public Builder submittedActions(CaseProcessingActionItem... caseProcessingActionItems) {
        Collections.addAll(submittedActions, caseProcessingActionItems);
        return this;
      }

      public Builder withdrawnActions(CaseProcessingActionItem... caseProcessingActionItems) {
        Collections.addAll(withdrawnActions, caseProcessingActionItems);
        return this;
      }

      public Builder completedActions(CaseProcessingActionItem... caseProcessingActionItems) {
        Collections.addAll(completedActions, caseProcessingActionItems);
        return this;
      }

      public ExpectedActions build() {
        return new ExpectedActions(
            inProgressActions,
            awaitingPaymentActions,
            submittedActions,
            withdrawnActions,
            completedActions);
      }

      private Builder() {
      }
    }

  }

}
