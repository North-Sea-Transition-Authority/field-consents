package uk.co.nstauthority.fieldconsents.application.caseprocessing.action;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.ApplicationType.FLARE;
import static uk.co.nstauthority.fieldconsents.application.ApplicationType.PRODUCTION;
import static uk.co.nstauthority.fieldconsents.application.ApplicationType.VENT;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.APPLICATION_UPDATES;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.APPLICATION_UPDATE_REQUEST;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.APPROVE_FOR_ISSUING;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.BREACH_INFORMATION;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CAM_ASSIGN_OWNERSHIP;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CAM_REASSIGN_OWNERSHIP;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CASE_OFFICER_ASSIGN_OWNERSHIP;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CASE_OFFICER_REASSIGN_OWNERSHIP;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CASE_OFFICER_RELEASE_OWNERSHIP;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CASE_OFFICER_TAKE_OWNERSHIP;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CASE_OFFICER_WITHDRAWAL_RESPONSE;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CHANGE_ACE_STATUS;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CLOSE_APPLICATION;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CONSENT_ISSUING;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CONSENT_PREPARATION;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CONSULTATIONS;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CONSULTATION_FURTHER_INFORMATION_REQUEST;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CONSULTATION_FURTHER_INFORMATION_RESPOND;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CONSULTATION_MANAGE_RESPONDER;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CONSULTATION_REQUEST;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CONSULTATION_RESPONSE;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.EDIT_BREACH_INFORMATION;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.EDIT_CONSENT_DATA;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.EDIT_CONSENT_DOCUMENTS;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.ISSUE_CONSENT;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.OPERATOR_PAY_AND_SUBMIT_APPLICATION;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.OPERATOR_RETURN_APPLICATION_TO_IN_PROGRESS_FROM_AWAITING_PAYMENT;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.OPERATOR_UPDATE_APPLICATION;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.OPERATOR_WITHDRAWAL_REQUEST;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.RECORD_BREACH;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.REGULATOR_ADD_CASE_NOTE;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.REMOVE_BREACH;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.RETURN_TO_CASE_OFFICER;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.REVISE_CONSENT;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.TECHNICAL_REVIEWER_REASSIGN_OWNERSHIP;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.TECHNICAL_REVIEWER_SUBMIT_REVIEW;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.TECHNICAL_REVIEWS;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.TECHNICAL_REVIEW_REQUEST;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.UNAPPROVE_FOR_ISSUING;

import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
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
import uk.co.nstauthority.fieldconsents.authorisation.FieldConsentsAccessService;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.teams.Role;

@ExtendWith(MockitoExtension.class)
class CaseProcessingActionServiceTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();
  private static final WebUserAccountId USER_WUA_ID = WebUserAccountId.from(USER.wuaId());

  @Mock
  private FieldConsentsAccessService fieldConsentsAccessService;

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
    applicationVersion.setCurrentCaseOwner(Role.CASE_OFFICER);
    application = applicationVersion.getApplication();
  }

  @Test
  void userHasAnyAction_hasAction() {
    doReturn(Set.of(APPLICATION_UPDATES))
        .when(caseProcessingActionService)
        .getAvailableUserActions(applicationVersion, USER, Set.of(APPLICATION_UPDATES));

    assertThat(caseProcessingActionService.userHasAnyAction(applicationVersion, USER, APPLICATION_UPDATES)).isTrue();
  }

  @Test
  void userHasAnyAction_doesNotHaveAction() {
    doReturn(Set.of())
        .when(caseProcessingActionService)
        .getAvailableUserActions(applicationVersion, USER, Set.of(APPLICATION_UPDATES));

    assertThat(caseProcessingActionService.userHasAnyAction(applicationVersion, USER, APPLICATION_UPDATES)).isFalse();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("getTaskListActionItems_regulator")
  void getTaskListActionItems_inProgress(Role role, ExpectedActions expectedActions) {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(PRODUCTION);
    getTaskListActionItems(applicationVersion, role, expectedActions.inProgressActions());
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("getTaskListActionItems_regulator")
  void getTaskListActionItems_awaitingPayment(Role role, ExpectedActions expectedActions) {
    var applicationVersion = ApplicationTestUtil.getAwaitingPaymentApplicationVersionWithType(PRODUCTION);
    getTaskListActionItems(applicationVersion, role, expectedActions.awaitingPaymentActions());
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("getTaskListActionItems_regulator")
  void getTaskListActionItems_submitted(Role role, ExpectedActions expectedActions) {
    var applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(PRODUCTION);
    getTaskListActionItems(applicationVersion, role, expectedActions.submittedActions());
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("getTaskListActionItems_regulator")
  void getTaskListActionItems_withdrawn(Role role, ExpectedActions expectedActions) {
    var applicationVersion = ApplicationTestUtil.getWithdrawnApplicationVersionWithType(PRODUCTION);
    getTaskListActionItems(applicationVersion, role, expectedActions.withdrawnActions());
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("getTaskListActionItems_regulator")
  void getTaskListActionItems_completed(Role role, ExpectedActions expectedActions) {
    var applicationVersion = ApplicationTestUtil.getConsentedApplicationVersionWithType(PRODUCTION);
    getTaskListActionItems(applicationVersion, role, expectedActions.completedActions());
  }

  private void getTaskListActionItems(
      ApplicationVersion applicationVersion,
      Role role,
      Set<CaseProcessingActionItem> expectedActionItems
  ) {
    when(fieldConsentsAccessService.getApplicationRolesForUser(applicationVersion, USER)).thenReturn(Set.of(role));

    lenient().when(caseStatusFlagService.isCaseStatusFlagApplicable(eq(applicationVersion), any(CaseStatusFlag.class))).thenReturn(true);

    var webUserAccountIdByTeamRole = Collections.<Role, WebUserAccountId>emptyMap();
    doReturn(webUserAccountIdByTeamRole)
        .when(caseProcessingActionService)
        .constructAssigneeMap(applicationVersion);

    doReturn(true)
        .when(caseProcessingActionService)
        .applicationTypeFeatureFlagAllowed(eq(applicationVersion), any(CaseProcessingActionItem.class));

    lenient().doReturn(true)
        .when(caseProcessingActionService)
        .isActionEnabledForUser(any(CaseProcessingActionItem.class), eq(webUserAccountIdByTeamRole), eq(USER));

    assertThat(caseProcessingActionService.getTaskListActionItems(applicationVersion, USER)).isEqualTo(expectedActionItems);
  }

  private static Stream<Arguments> getTaskListActionItems_regulator() {
    return Stream.of(
        arguments(
            Role.ACCESS_MANAGER,
            ExpectedActions.empty()
        ),
        arguments(
            Role.INDUSTRY_ACCESS_MANAGER,
            ExpectedActions.empty()
        ),
        arguments(
            Role.DOCUMENT_TEMPLATE_MANAGER,
            ExpectedActions.empty()
        ),
        arguments(
            Role.CASE_OFFICER,
            ExpectedActions.newBuilder()
                .inProgressActions(TECHNICAL_REVIEWS, CONSULTATIONS, APPLICATION_UPDATES, REGULATOR_ADD_CASE_NOTE)
                .awaitingPaymentActions(TECHNICAL_REVIEWS, CONSULTATIONS, APPLICATION_UPDATES, REGULATOR_ADD_CASE_NOTE)
                .submittedActions(TECHNICAL_REVIEWS, CONSULTATIONS, APPLICATION_UPDATES, REGULATOR_ADD_CASE_NOTE, CONSENT_PREPARATION, CLOSE_APPLICATION, CHANGE_ACE_STATUS)
                .withdrawnActions(TECHNICAL_REVIEWS, CONSULTATIONS, APPLICATION_UPDATES, REGULATOR_ADD_CASE_NOTE)
                .completedActions(TECHNICAL_REVIEWS, CONSULTATIONS, APPLICATION_UPDATES, REGULATOR_ADD_CASE_NOTE, BREACH_INFORMATION)
                .build()
        ),
        arguments(
            Role.CASE_MANAGER,
            ExpectedActions.newBuilder()
                .inProgressActions(TECHNICAL_REVIEWS, CONSULTATIONS, APPLICATION_UPDATES, REGULATOR_ADD_CASE_NOTE)
                .awaitingPaymentActions(TECHNICAL_REVIEWS, CONSULTATIONS, APPLICATION_UPDATES, REGULATOR_ADD_CASE_NOTE)
                .submittedActions(TECHNICAL_REVIEWS, CONSULTATIONS, APPLICATION_UPDATES, REGULATOR_ADD_CASE_NOTE, CONSENT_PREPARATION)
                .withdrawnActions(TECHNICAL_REVIEWS, CONSULTATIONS, APPLICATION_UPDATES, REGULATOR_ADD_CASE_NOTE)
                .completedActions(TECHNICAL_REVIEWS, CONSULTATIONS, APPLICATION_UPDATES, REGULATOR_ADD_CASE_NOTE, BREACH_INFORMATION)
                .build()
        ),
        arguments(
            Role.CONSENTS_AND_AUTHORISATIONS_MANAGER,
            ExpectedActions.newBuilder()
                .inProgressActions(TECHNICAL_REVIEWS, CONSULTATIONS, APPLICATION_UPDATES, REGULATOR_ADD_CASE_NOTE)
                .awaitingPaymentActions(TECHNICAL_REVIEWS, CONSULTATIONS, APPLICATION_UPDATES, REGULATOR_ADD_CASE_NOTE)
                .submittedActions(TECHNICAL_REVIEWS, CONSULTATIONS, APPLICATION_UPDATES, REGULATOR_ADD_CASE_NOTE, CONSENT_ISSUING)
                .withdrawnActions(TECHNICAL_REVIEWS, CONSULTATIONS, APPLICATION_UPDATES, REGULATOR_ADD_CASE_NOTE)
                .completedActions(TECHNICAL_REVIEWS, CONSULTATIONS, APPLICATION_UPDATES, REGULATOR_ADD_CASE_NOTE, BREACH_INFORMATION)
                .build()
        ),
        arguments(
            Role.TECHNICAL_REVIEWER,
            ExpectedActions.newBuilder()
                .inProgressActions(TECHNICAL_REVIEWS, CONSULTATIONS, APPLICATION_UPDATES, REGULATOR_ADD_CASE_NOTE)
                .awaitingPaymentActions(TECHNICAL_REVIEWS, CONSULTATIONS, APPLICATION_UPDATES, REGULATOR_ADD_CASE_NOTE)
                .submittedActions(TECHNICAL_REVIEWS, CONSULTATIONS, APPLICATION_UPDATES, REGULATOR_ADD_CASE_NOTE)
                .withdrawnActions(TECHNICAL_REVIEWS, CONSULTATIONS, APPLICATION_UPDATES, REGULATOR_ADD_CASE_NOTE)
                .completedActions(TECHNICAL_REVIEWS, CONSULTATIONS, APPLICATION_UPDATES, REGULATOR_ADD_CASE_NOTE, BREACH_INFORMATION)
                .build()
        )
    );
  }

  @Test
  void getTopLevelActionItemViews_excludingTaskListActionItemsAndGroupedActionItems() {
    var taskListActionItems = Set.of(
        TECHNICAL_REVIEWS,
        CONSULTATIONS,
        CONSENT_PREPARATION,
        CONSENT_ISSUING,
        CHANGE_ACE_STATUS,
        APPLICATION_UPDATES,
        REGULATOR_ADD_CASE_NOTE,
        BREACH_INFORMATION
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
        UNAPPROVE_FOR_ISSUING,
        RECORD_BREACH,
        REMOVE_BREACH,
        EDIT_BREACH_INFORMATION,
        CLOSE_APPLICATION
    );

    var applicableActions = EnumSet.allOf(CaseProcessingActionItem.class)
        .stream()
        .filter(action -> !taskListActionItems.contains(action)) // not task list action
        .filter(action -> !groupedActionItems.contains(action)) // not action group (page) action
        .collect(toSet());

    var actionViews = applicableActions.stream()
        .filter(actionItem -> !taskListActionItems.contains(actionItem)) // exclude these actions
        .filter(actionItem -> !groupedActionItems.contains(actionItem)) // exclude these actions
        .sorted(Comparator.comparingInt(CaseProcessingActionItem::getDisplayOrder))
        .map(actionItem -> CaseProcessingActionView.from(actionItem, applicationVersion))
        .toList();

    doReturn(applicableActions)
        .when(caseProcessingActionService)
        .getAvailableUserActions(applicationVersion, USER, applicableActions);

    assertThat(caseProcessingActionService.getTopLevelActionItemViews(applicationVersion, USER))
        .containsExactlyElementsOf(actionViews);
  }

  @ParameterizedTest
  @MethodSource("getUserActionViewsForGroup_arguments")
  void getUserActionViewsForGroup(
      CaseProcessingActionGroup actionGroup,
      List<CaseProcessingActionItem> expectedActionItems
  ) {
    // assume whatever actions are passed in are available to the user
    doAnswer(invocation -> invocation.getArgument(2, Set.class))
        .when(caseProcessingActionService)
        .getAvailableUserActions(eq(applicationVersion), eq(USER), any(Set.class));

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
            CaseProcessingTaskListSection.CASE_TASKS, Set.of(
                TECHNICAL_REVIEWS,
                CONSULTATIONS,
                CONSENT_PREPARATION,
                CONSENT_ISSUING),
            CaseProcessingTaskListSection.OPTIONAL_CASE_TASKS, Set.of(
                CHANGE_ACE_STATUS,
                BREACH_INFORMATION,
                APPLICATION_UPDATES,
                REGULATOR_ADD_CASE_NOTE,
                CLOSE_APPLICATION)
        ));
  }

  @ParameterizedTest
  @MethodSource("constructAssigneeMap_arguments")
  void constructAssigneeMap(
      WebUserAccountId caseOfficerWuaId,
      WebUserAccountId technicalReviewerWuaId,
      WebUserAccountId responderWuaId,
      Map<Role, WebUserAccountId> expectedAssigneeMap
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
    applicationVersion.setCurrentCaseOwner(Role.CONSENTS_AND_AUTHORISATIONS_MANAGER);

    when(camAssignmentService.findCamWuaId(applicationVersion)).thenReturn(Optional.of(USER_WUA_ID));
    when(technicalReviewService.findTechnicalReviewerWuaId(applicationVersion)).thenReturn(Optional.empty());
    when(consultationService.findLatestOpenConsultation(application)).thenReturn(Optional.empty());

    assertThat(caseProcessingActionService.constructAssigneeMap(applicationVersion)).containsExactlyInAnyOrderEntriesOf(Map.of(
        Role.CONSENTS_AND_AUTHORISATIONS_MANAGER, USER_WUA_ID
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
            Map.of(Role.CASE_OFFICER, USER_WUA_ID)
        ),
        arguments(
            null,
            USER_WUA_ID,
            null,
            Map.of(Role.TECHNICAL_REVIEWER, USER_WUA_ID)
        ),
        arguments(
            null,
            null,
            USER_WUA_ID,
            Map.of(Role.RESPONDER, USER_WUA_ID)
        ),
        arguments(
            USER_WUA_ID,
            USER_WUA_ID,
            USER_WUA_ID,
            Map.of(
                Role.CASE_OFFICER, USER_WUA_ID,
                Role.TECHNICAL_REVIEWER, USER_WUA_ID,
                Role.RESPONDER, USER_WUA_ID
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
      Map<Role, WebUserAccountId> assigneeMap,
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
        arguments(REVISE_CONSENT, emptyMap(), true),
        arguments(CONSULTATION_MANAGE_RESPONDER, emptyMap(), true)
    );
  }

  private static Stream<Arguments> isActionEnabledForUser_assigneeOnly_arguments() {
    var caseOfficerAssigneeMap = Map.of(Role.CASE_OFFICER, USER_WUA_ID);
    var technicalReviewerAssigneeMap = Map.of(Role.TECHNICAL_REVIEWER, USER_WUA_ID);
    var responderAssigneeMap = Map.of(Role.RESPONDER, USER_WUA_ID);
    var consentsAndAuthorisationsManagerMap = Map.of(Role.CONSENTS_AND_AUTHORISATIONS_MANAGER, USER_WUA_ID);

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
        arguments(ventApplication, REVISE_CONSENT, true),
        arguments(ventApplication, CONSULTATION_RESPONSE, true),
        arguments(ventApplication, CONSULTATION_MANAGE_RESPONDER, true),
        arguments(ventApplication, CONSULTATION_FURTHER_INFORMATION_REQUEST, true),
        arguments(ventApplication, CONSULTATION_FURTHER_INFORMATION_RESPOND, true)
    );
  }

  private record ExpectedActions(
      Set<CaseProcessingActionItem> inProgressActions,
      Set<CaseProcessingActionItem> awaitingPaymentActions,
      Set<CaseProcessingActionItem> submittedActions,
      Set<CaseProcessingActionItem> withdrawnActions,
      Set<CaseProcessingActionItem> completedActions
  ) {

    public static Builder newBuilder() {
      return new Builder();
    }

    public static ExpectedActions empty() {
      return newBuilder().build();
    }

    public static class Builder {

      private final Set<CaseProcessingActionItem> inProgressActions = new HashSet<>();
      private final Set<CaseProcessingActionItem> awaitingPaymentActions = new HashSet<>();
      private final Set<CaseProcessingActionItem> submittedActions = new HashSet<>();
      private final Set<CaseProcessingActionItem> withdrawnActions = new HashSet<>();
      private final Set<CaseProcessingActionItem> completedActions = new HashSet<>();

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
