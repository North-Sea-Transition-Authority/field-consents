package uk.co.nstauthority.fieldconsents.application.caseprocessing.action;

import static java.util.Map.entry;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTypeFeature.CONSULTATION;
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
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.APPLICATION_UPDATE_OPEN;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.CAM_ASSIGNED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.CAM_NOT_ASSIGNED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.CASE_NOTES_ALLOWED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.CASE_OFFICER_ASSIGNED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.CASE_OFFICER_NOT_ASSIGNED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.CONSENT_DATA_EXISTS;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.CONSENT_NOT_APPROVED_FOR_ISSUE;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.CONSULTATION_FURTHER_INFORMATION_OPEN;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.CONSULTATION_OPEN;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.NO_APPLICATION_UPDATE_OPEN;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.NO_CONSULTATION_FURTHER_INFORMATION_OPEN;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.NO_CONSULTATION_OPEN;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.NO_TECHNICAL_REVIEW_OPEN;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.NO_WITHDRAWAL_OPEN;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.TECHNICAL_REVIEW_OPEN;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.WITHDRAWAL_OPEN;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.tasklist.CaseProcessingTaskListSection.CASE_TASKS;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.tasklist.CaseProcessingTaskListSection.OPTIONAL_CASE_TASKS;
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

import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationTypeFeature;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment.CaseAssignmentService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment.cam.CamAssignmentService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlagService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.Consultation;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.tasklist.CaseProcessingTaskListSection;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.ApplicationAccessService;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.TeamRole;

@Service
public class CaseProcessingActionService {

  private final ApplicationAccessService applicationAccessService;
  private final CaseStatusFlagService caseStatusFlagService;
  private final TechnicalReviewService technicalReviewService;
  private final ConsultationService consultationService;
  private final CaseAssignmentService caseAssignmentService;
  private final CamAssignmentService camAssignmentService;

  private final Map<ApplicationVersionStatus, Set<CaseProcessingActionItem>> caseStatusToActions =
      Map.of(
          ApplicationVersionStatus.IN_PROGRESS,
          EnumSet.of(
              CHANGE_ACE_STATUS,
              CASE_OFFICER_TAKE_OWNERSHIP,
              CASE_OFFICER_RELEASE_OWNERSHIP,
              CASE_OFFICER_ASSIGN_OWNERSHIP,
              CASE_OFFICER_REASSIGN_OWNERSHIP,
              REGULATOR_ADD_CASE_NOTE,
              TECHNICAL_REVIEWER_REASSIGN_OWNERSHIP,
              OPERATOR_UPDATE_APPLICATION
          ),
          ApplicationVersionStatus.AWAITING_PAYMENT,
          EnumSet.of(
              OPERATOR_PAY_AND_SUBMIT_APPLICATION,
              OPERATOR_RETURN_APPLICATION_TO_IN_PROGRESS_FROM_AWAITING_PAYMENT
          ),
          ApplicationVersionStatus.SUBMITTED,
          EnumSet.of(
              CHANGE_ACE_STATUS,
              CASE_OFFICER_TAKE_OWNERSHIP,
              CASE_OFFICER_RELEASE_OWNERSHIP,
              CASE_OFFICER_WITHDRAWAL_RESPONSE,
              CONSULTATIONS,
              CONSULTATION_REQUEST,
              CONSULTATION_RESPONSE,
              CONSULTATION_MANAGE_RESPONDER,
              CONSULTATION_FURTHER_INFORMATION_REQUEST,
              CONSULTATION_FURTHER_INFORMATION_RESPOND,
              TECHNICAL_REVIEWS,
              TECHNICAL_REVIEW_REQUEST,
              CASE_OFFICER_ASSIGN_OWNERSHIP,
              CASE_OFFICER_REASSIGN_OWNERSHIP,
              REGULATOR_ADD_CASE_NOTE,
              TECHNICAL_REVIEWER_SUBMIT_REVIEW,
              TECHNICAL_REVIEWER_REASSIGN_OWNERSHIP,
              APPLICATION_UPDATES,
              APPLICATION_UPDATE_REQUEST,
              OPERATOR_WITHDRAWAL_REQUEST,
              OPERATOR_UPDATE_APPLICATION,
              CAM_ASSIGN_OWNERSHIP,
              CAM_REASSIGN_OWNERSHIP,
              CONSENT_PREPARATION,
              CONSENT_ISSUING,
              APPROVE_FOR_ISSUING,
              RETURN_TO_CASE_OFFICER
          )
      );

  private final Map<CaseProcessingActionItem, Set<RolePermission>> actionsToPermissions =
      Map.ofEntries(
          entry(CASE_OFFICER_TAKE_OWNERSHIP, EnumSet.of(PROCESS_FCS_APPLICATIONS)),
          entry(CHANGE_ACE_STATUS, EnumSet.of(PROCESS_FCS_APPLICATIONS)),
          entry(CASE_OFFICER_RELEASE_OWNERSHIP, EnumSet.of(PROCESS_FCS_APPLICATIONS)),
          entry(CASE_OFFICER_WITHDRAWAL_RESPONSE, EnumSet.of(PROCESS_FCS_APPLICATIONS)),
          entry(TECHNICAL_REVIEWS, EnumSet.of(VIEW_FCS_CASE_PROCESSING_DOCUMENTS)),
          entry(TECHNICAL_REVIEW_REQUEST, EnumSet.of(PROCESS_FCS_APPLICATIONS)),
          entry(CASE_OFFICER_ASSIGN_OWNERSHIP, EnumSet.of(ASSIGN_FCS_APPLICATIONS)),
          entry(CASE_OFFICER_REASSIGN_OWNERSHIP, EnumSet.of(ASSIGN_FCS_APPLICATIONS)),
          entry(CONSULTATIONS, EnumSet.of(VIEW_FCS_CASE_PROCESSING_DOCUMENTS)),
          entry(CONSULTATION_REQUEST, EnumSet.of(PROCESS_FCS_APPLICATIONS)),
          entry(CONSULTATION_RESPONSE, EnumSet.of(RESPOND_TO_CONSULTATION)),
          entry(CONSULTATION_MANAGE_RESPONDER, EnumSet.of(ALLOCATE_CONSULTATION)),
          entry(REGULATOR_ADD_CASE_NOTE, EnumSet.of(EDIT_FCS_CASE_PROCESSING_DOCUMENTS)),
          entry(TECHNICAL_REVIEWER_SUBMIT_REVIEW, EnumSet.of(TECHNICAL_REVIEW_FCS_APPLICATIONS)),
          entry(TECHNICAL_REVIEWER_REASSIGN_OWNERSHIP, EnumSet.of(TECHNICAL_REVIEW_FCS_APPLICATIONS)),
          entry(APPLICATION_UPDATES, EnumSet.of(VIEW_FCS_CASE_PROCESSING_DOCUMENTS)),
          entry(APPLICATION_UPDATE_REQUEST, EnumSet.of(PROCESS_FCS_APPLICATIONS, TECHNICAL_REVIEW_FCS_APPLICATIONS)),
          entry(CONSENT_PREPARATION, EnumSet.of(PROCESS_FCS_APPLICATIONS, ASSIGN_FCS_APPLICATIONS)),
          entry(OPERATOR_PAY_AND_SUBMIT_APPLICATION, EnumSet.of(PAY_AND_SUBMIT_FCS_APPLICATIONS)),
          entry(OPERATOR_RETURN_APPLICATION_TO_IN_PROGRESS_FROM_AWAITING_PAYMENT, EnumSet.of(
              EDIT_FCS_APPLICATIONS)),
          entry(OPERATOR_WITHDRAWAL_REQUEST, EnumSet.of(EDIT_FCS_APPLICATIONS)),
          entry(OPERATOR_UPDATE_APPLICATION, EnumSet.of(EDIT_FCS_APPLICATIONS)),
          entry(CONSULTATION_FURTHER_INFORMATION_REQUEST, EnumSet.of(RESPOND_TO_CONSULTATION)),
          entry(CONSULTATION_FURTHER_INFORMATION_RESPOND, EnumSet.of(PROCESS_FCS_APPLICATIONS)),
          entry(CAM_ASSIGN_OWNERSHIP, EnumSet.of(PROCESS_FCS_APPLICATIONS)),
          entry(CONSENT_ISSUING, EnumSet.of(AUTHORISE_FCS_CONSENTS)),
          entry(APPROVE_FOR_ISSUING, EnumSet.of(AUTHORISE_FCS_CONSENTS)),
          entry(RETURN_TO_CASE_OFFICER, EnumSet.of(AUTHORISE_FCS_CONSENTS)),
          entry(CAM_REASSIGN_OWNERSHIP, EnumSet.of(AUTHORISE_FCS_CONSENTS, ASSIGN_FCS_APPLICATIONS))
      );

  private final Map<CaseProcessingActionItem, Set<CaseStatusFlag>> actionsToStatusFlags =
      Map.ofEntries(
          entry(CASE_OFFICER_TAKE_OWNERSHIP, EnumSet.of(CASE_OFFICER_NOT_ASSIGNED, CAM_NOT_ASSIGNED)),
          entry(CHANGE_ACE_STATUS, EnumSet.of(CASE_OFFICER_ASSIGNED)),
          entry(CASE_OFFICER_RELEASE_OWNERSHIP, EnumSet.of(CASE_OFFICER_ASSIGNED)),
          entry(CASE_OFFICER_WITHDRAWAL_RESPONSE, EnumSet.of(CASE_OFFICER_ASSIGNED, WITHDRAWAL_OPEN)),
          entry(TECHNICAL_REVIEW_REQUEST,
              EnumSet.of(CASE_OFFICER_ASSIGNED, NO_TECHNICAL_REVIEW_OPEN, NO_APPLICATION_UPDATE_OPEN)),
          entry(CASE_OFFICER_ASSIGN_OWNERSHIP, EnumSet.of(CASE_OFFICER_NOT_ASSIGNED, CAM_NOT_ASSIGNED)),
          entry(CASE_OFFICER_REASSIGN_OWNERSHIP, EnumSet.of(CASE_OFFICER_ASSIGNED, CAM_NOT_ASSIGNED)),
          entry(CONSULTATION_REQUEST,
              EnumSet.of(CASE_OFFICER_ASSIGNED, NO_TECHNICAL_REVIEW_OPEN, NO_CONSULTATION_OPEN)),
          entry(CONSULTATION_RESPONSE, EnumSet.of(CONSULTATION_OPEN, NO_CONSULTATION_FURTHER_INFORMATION_OPEN)),
          entry(CONSULTATION_MANAGE_RESPONDER, EnumSet.of(CONSULTATION_OPEN)),
          entry(REGULATOR_ADD_CASE_NOTE, EnumSet.of(CASE_NOTES_ALLOWED)),
          entry(TECHNICAL_REVIEWER_SUBMIT_REVIEW, EnumSet.of(TECHNICAL_REVIEW_OPEN)),
          entry(TECHNICAL_REVIEWER_REASSIGN_OWNERSHIP, EnumSet.of(TECHNICAL_REVIEW_OPEN)),
          entry(APPLICATION_UPDATE_REQUEST, EnumSet.of(NO_APPLICATION_UPDATE_OPEN)),
          entry(OPERATOR_PAY_AND_SUBMIT_APPLICATION, EnumSet.of(NO_APPLICATION_UPDATE_OPEN)),
          entry(OPERATOR_RETURN_APPLICATION_TO_IN_PROGRESS_FROM_AWAITING_PAYMENT, EnumSet.of(NO_APPLICATION_UPDATE_OPEN)),
          entry(OPERATOR_WITHDRAWAL_REQUEST, EnumSet.of(NO_WITHDRAWAL_OPEN, NO_APPLICATION_UPDATE_OPEN)),
          entry(OPERATOR_UPDATE_APPLICATION, EnumSet.of(APPLICATION_UPDATE_OPEN)),
          entry(CONSULTATION_FURTHER_INFORMATION_REQUEST, EnumSet.of(NO_CONSULTATION_FURTHER_INFORMATION_OPEN)),
          entry(CONSULTATION_FURTHER_INFORMATION_RESPOND,
              EnumSet.of(CONSULTATION_FURTHER_INFORMATION_OPEN, NO_APPLICATION_UPDATE_OPEN)),
          entry(CAM_ASSIGN_OWNERSHIP,
              EnumSet.of(
                  CASE_OFFICER_ASSIGNED,
                  NO_TECHNICAL_REVIEW_OPEN,
                  NO_APPLICATION_UPDATE_OPEN,
                  NO_CONSULTATION_OPEN)
          ),
          entry(CONSENT_ISSUING, EnumSet.of(CONSENT_DATA_EXISTS)),
          entry(APPROVE_FOR_ISSUING, EnumSet.of(CONSENT_DATA_EXISTS, CONSENT_NOT_APPROVED_FOR_ISSUE)),
          entry(RETURN_TO_CASE_OFFICER, EnumSet.of(CAM_ASSIGNED, CASE_OFFICER_NOT_ASSIGNED, CONSENT_NOT_APPROVED_FOR_ISSUE)),
          entry(CAM_REASSIGN_OWNERSHIP, EnumSet.of(CAM_ASSIGNED, CASE_OFFICER_NOT_ASSIGNED))
      );

  private final Map<CaseProcessingActionItem, Set<? extends TeamRole>> actionsToAssigneeOnlyRoles =
      Map.ofEntries(
          entry(CHANGE_ACE_STATUS, EnumSet.of(CASE_OFFICER)),
          entry(CASE_OFFICER_RELEASE_OWNERSHIP, EnumSet.of(CASE_OFFICER)),
          entry(CASE_OFFICER_WITHDRAWAL_RESPONSE, EnumSet.of(CASE_OFFICER)),
          entry(TECHNICAL_REVIEW_REQUEST, EnumSet.of(CASE_OFFICER)),
          entry(TECHNICAL_REVIEWER_SUBMIT_REVIEW, EnumSet.of(TECHNICAL_REVIEWER)),
          entry(APPLICATION_UPDATE_REQUEST, EnumSet.of(CASE_OFFICER, TECHNICAL_REVIEWER)),
          entry(CONSULTATION_REQUEST, EnumSet.of(CASE_OFFICER)),
          entry(CONSULTATION_RESPONSE, EnumSet.of(RESPONDER)),
          entry(CONSULTATION_FURTHER_INFORMATION_REQUEST, EnumSet.of(RESPONDER)),
          entry(CONSULTATION_FURTHER_INFORMATION_RESPOND, EnumSet.of(CASE_OFFICER)),
          entry(CAM_ASSIGN_OWNERSHIP, EnumSet.of(CASE_OFFICER)),
          entry(APPROVE_FOR_ISSUING, EnumSet.of(CONSENTS_AND_AUTHORISATIONS_MANAGER)),
          entry(RETURN_TO_CASE_OFFICER, EnumSet.of(CONSENTS_AND_AUTHORISATIONS_MANAGER))
      );
  /*
   * If an actionItem is not here, it will be allowed by default. If multiple features are present for an action,
   * one of them must match for the action to be allowed.
   */
  private final Map<CaseProcessingActionItem, Set<ApplicationTypeFeature>> actionItemsToFeatures = Map.of(
      CONSULTATIONS, EnumSet.of(CONSULTATION),
      CONSULTATION_REQUEST, EnumSet.of(CONSULTATION)
  );

  // If an action item is here it will be included in the regulator task
  private final Map<CaseProcessingActionItem, CaseProcessingTaskListSection> actionsToTaskListSection =
      Map.of(
          TECHNICAL_REVIEWS, CASE_TASKS,
          CONSULTATIONS, CASE_TASKS,
          CONSENT_PREPARATION, CASE_TASKS,
          CONSENT_ISSUING, CASE_TASKS,
          CHANGE_ACE_STATUS, OPTIONAL_CASE_TASKS,
          APPLICATION_UPDATES, OPTIONAL_CASE_TASKS,
          REGULATOR_ADD_CASE_NOTE, OPTIONAL_CASE_TASKS
      );

  // If an action is here it will be displayed only on the action groups page
  private final Map<CaseProcessingActionItem, Set<CaseProcessingActionGroup>> actionsToCaseProcessingActionGroup =
      Map.of(
          TECHNICAL_REVIEW_REQUEST, EnumSet.of(CaseProcessingActionGroup.TECHNICAL_REVIEWS),
          CONSULTATION_REQUEST, EnumSet.of(CaseProcessingActionGroup.CONSULTATIONS),
          CONSULTATION_FURTHER_INFORMATION_RESPOND, EnumSet.of(CaseProcessingActionGroup.CONSULTATIONS),
          APPLICATION_UPDATE_REQUEST, EnumSet.of(CaseProcessingActionGroup.APPLICATION_UPDATES),
          CAM_ASSIGN_OWNERSHIP, EnumSet.of(CaseProcessingActionGroup.CONSENT_PREPARATION),
          CAM_REASSIGN_OWNERSHIP, EnumSet.of(CaseProcessingActionGroup.CONSENT_PREPARATION,
              CaseProcessingActionGroup.CONSENT_ISSUING),
          RETURN_TO_CASE_OFFICER, EnumSet.of(CaseProcessingActionGroup.CONSENT_ISSUING),
          APPROVE_FOR_ISSUING, EnumSet.of(CaseProcessingActionGroup.CONSENT_ISSUING)
      );

  @Autowired
  public CaseProcessingActionService(ApplicationAccessService applicationAccessService,
                                     CaseStatusFlagService caseStatusFlagService,
                                     TechnicalReviewService technicalReviewService,
                                     ConsultationService consultationService,
                                     CaseAssignmentService caseAssignmentService,
                                     CamAssignmentService camAssignmentService) {
    this.applicationAccessService = applicationAccessService;
    this.caseStatusFlagService = caseStatusFlagService;
    this.technicalReviewService = technicalReviewService;
    this.consultationService = consultationService;
    this.caseAssignmentService = caseAssignmentService;
    this.camAssignmentService = camAssignmentService;
  }

  public List<CaseProcessingActionItem> getUserActionItems(ApplicationVersion applicationVersion,
                                                           ServiceUserDetail user) {
    var actions = caseStatusToActions.get(applicationVersion.getStatus());

    if (actions == null) {
      throw new IllegalStateException("Cannot find any actions for application version id %s with status %s"
          .formatted(applicationVersion.getId(), applicationVersion.getStatus().name()));
    }

    var userRolePermissions = applicationAccessService.getApplicationPermissionsForUser(applicationVersion, user);
    var caseStatusFlags = caseStatusFlagService.getCaseStatusFlags(applicationVersion);
    var assigneeMap = constructAssigneeMap(applicationVersion);

    return actions.stream()
        .filter(action -> applicationTypeFeatureFlagAllowed(applicationVersion, action))
        // filter actions that the user has permissions for
        .filter(action -> CollectionUtils.containsAny(actionsToPermissions.get(action), userRolePermissions))
        // filter actions that the application version has all the status flags for
        .filter(action -> caseStatusFlags.containsAll(actionsToStatusFlags.getOrDefault(action, Set.of())))
        .filter(action -> isActionEnabledForUser(action, assigneeMap, user))
        .toList();
  }

  public List<CaseProcessingActionView> getUserActionViews(ApplicationVersion applicationVersion,
                                                           ServiceUserDetail user) {
    return getUserActionItems(applicationVersion, user)
        .stream()
        .filter(action -> !actionsToTaskListSection.containsKey(action)) // not task list action
        .filter(action -> !actionsToCaseProcessingActionGroup.containsKey(action)) // not action group (page) action
        .sorted(Comparator.comparingInt(CaseProcessingActionItem::getDisplayOrder))
        .map(action -> CaseProcessingActionView.from(action, applicationVersion))
        .toList();
  }

  public List<CaseProcessingActionView> getUserActionViewsForGroup(
      ApplicationVersion applicationVersion,
      ServiceUserDetail user,
      CaseProcessingActionGroup actionGroup
  ) {
    return getUserActionItems(applicationVersion, user)
        .stream()
        .filter(actionsToCaseProcessingActionGroup::containsKey)
        .filter(action -> actionsToCaseProcessingActionGroup.get(action).contains(actionGroup))
        .sorted(Comparator.comparingInt(CaseProcessingActionItem::getDisplayOrder))
        .map(action -> CaseProcessingActionView.from(action, applicationVersion))
        .toList();
  }

  public Map<CaseProcessingTaskListSection, List<CaseProcessingActionItem>> groupActionItemsByTaskListSection(
      Collection<CaseProcessingActionItem> actionItems
  ) {
    return actionItems
        .stream()
        .filter(actionsToTaskListSection::containsKey)
        .collect(Collectors.groupingBy(actionsToTaskListSection::get));
  }

  Map<TeamRole, WebUserAccountId> constructAssigneeMap(ApplicationVersion applicationVersion) {
    var assigneeMap = new HashMap<TeamRole, WebUserAccountId>();

    if (CASE_OFFICER.equals(applicationVersion.getCurrentCaseOwner())) {
      caseAssignmentService.findCaseOfficerWuaId(applicationVersion)
          .ifPresent(caseOfficerWuaId -> assigneeMap.put(CASE_OFFICER, caseOfficerWuaId));
    }

    if (CONSENTS_AND_AUTHORISATIONS_MANAGER.equals(applicationVersion.getCurrentCaseOwner())) {
      camAssignmentService.findCamWuaId(applicationVersion)
          .ifPresent(camUserWuaId -> assigneeMap.put(CONSENTS_AND_AUTHORISATIONS_MANAGER, camUserWuaId));
    }

    technicalReviewService.findTechnicalReviewerWuaId(applicationVersion)
        .ifPresent(technicalReviewerWuaId -> assigneeMap.put(TECHNICAL_REVIEWER, technicalReviewerWuaId));

    consultationService.findLatestOpenConsultation(applicationVersion.getApplication())
        .map(Consultation::getResponderWuaId)
        .map(WebUserAccountId::from)
        .ifPresent(responderWuaId -> assigneeMap.put(RESPONDER, responderWuaId));

    return assigneeMap;
  }

  boolean isActionEnabledForUser(
      CaseProcessingActionItem action,
      Map<TeamRole, WebUserAccountId> assigneeMap,
      ServiceUserDetail user
  ) {
    var assigneeRoles = actionsToAssigneeOnlyRoles.get(action);

    if (!action.isAssigneeOnly() && Objects.isNull(assigneeRoles)) {
      return true;
    } else if (action.isAssigneeOnly() && Objects.isNull(assigneeRoles)) {
      throw new IllegalArgumentException("Action " + action.name() + " is assignee only but no roles are defined for the action");
    }

    for (var assigneeRole : assigneeRoles) {
      var assigneeWuaId = assigneeMap.get(assigneeRole);
      if (Objects.nonNull(assigneeWuaId) && user.wuaId().equals(assigneeWuaId.id())) {
        return true;
      }
    }

    return false;
  }

  boolean applicationTypeFeatureFlagAllowed(ApplicationVersion applicationVersion, CaseProcessingActionItem actionItem) {
    var applicationType = applicationVersion.getApplication().getType();

    var features = actionItemsToFeatures.get(actionItem);
    if (Objects.isNull(features)) {
      return true; // if nothing is found, assume it's allowed
    }

    return features.stream().anyMatch(feature -> feature.allowed(applicationType));
  }

}
