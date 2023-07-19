package uk.co.nstauthority.fieldconsents.application.caseprocessing.action;

import static java.util.Map.entry;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.APPLICATION_UPDATE_REQUEST;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CASE_OFFICER_ASSIGN_OWNERSHIP;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CASE_OFFICER_REASSIGN_OWNERSHIP;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CASE_OFFICER_RELEASE_OWNERSHIP;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CASE_OFFICER_TAKE_OWNERSHIP;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CASE_OFFICER_WITHDRAWAL_RESPONSE;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CHANGE_ACE_STATUS;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.OPERATOR_UPDATE_APPLICATION;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.OPERATOR_WITHDRAWAL_REQUEST;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.REGULATOR_ADD_CASE_NOTE;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.TECHNICAL_REVIEWER_REASSIGN_OWNERSHIP;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.TECHNICAL_REVIEW_REQUEST;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.APPLICATION_UPDATE_OPEN;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.CASE_NOTES_ALLOWED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.CASE_OFFICER_ASSIGNED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.CASE_OFFICER_NOT_ASSIGNED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.NO_APPLICATION_UPDATE_OPEN;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.NO_TECHNICAL_REVIEW_OPEN;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.NO_WITHDRAWAL_OPEN;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.TECHNICAL_REVIEW_OPEN;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.WITHDRAWAL_OPEN;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.ASSIGN_FCS_APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.EDIT_FCS_APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.EDIT_FCS_CASE_PROCESSING_DOCUMENTS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.PROCESS_FCS_APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.TECHNICAL_REVIEW_FCS_APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamRole.CASE_OFFICER;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamRole.TECHNICAL_REVIEWER;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlagService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.ApplicationAccessService;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamRole;

@Service
public class CaseProcessingActionService {

  private final ApplicationAccessService applicationAccessService;

  private final CaseStatusFlagService caseStatusFlagService;

  private final ApplicationVersionService applicationVersionService;

  private final TechnicalReviewService technicalReviewService;

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
              OPERATOR_UPDATE_APPLICATION),
          ApplicationVersionStatus.SUBMITTED,
          EnumSet.of(
              CHANGE_ACE_STATUS,
              CASE_OFFICER_TAKE_OWNERSHIP,
              CASE_OFFICER_RELEASE_OWNERSHIP,
              CASE_OFFICER_WITHDRAWAL_RESPONSE,
              TECHNICAL_REVIEW_REQUEST,
              CASE_OFFICER_ASSIGN_OWNERSHIP,
              CASE_OFFICER_REASSIGN_OWNERSHIP,
              REGULATOR_ADD_CASE_NOTE,
              TECHNICAL_REVIEWER_REASSIGN_OWNERSHIP,
              APPLICATION_UPDATE_REQUEST,
              OPERATOR_WITHDRAWAL_REQUEST,
              OPERATOR_UPDATE_APPLICATION)
      );

  private final Map<CaseProcessingActionItem, Set<RolePermission>> actionsToPermissions =
      Map.ofEntries(
          entry(CASE_OFFICER_TAKE_OWNERSHIP, EnumSet.of(PROCESS_FCS_APPLICATIONS)),
          entry(CHANGE_ACE_STATUS, EnumSet.of(PROCESS_FCS_APPLICATIONS)),
          entry(CASE_OFFICER_RELEASE_OWNERSHIP, EnumSet.of(PROCESS_FCS_APPLICATIONS)),
          entry(CASE_OFFICER_WITHDRAWAL_RESPONSE, EnumSet.of(PROCESS_FCS_APPLICATIONS)),
          entry(TECHNICAL_REVIEW_REQUEST, EnumSet.of(PROCESS_FCS_APPLICATIONS)),
          entry(CASE_OFFICER_ASSIGN_OWNERSHIP, EnumSet.of(ASSIGN_FCS_APPLICATIONS)),
          entry(CASE_OFFICER_REASSIGN_OWNERSHIP, EnumSet.of(ASSIGN_FCS_APPLICATIONS)),
          entry(REGULATOR_ADD_CASE_NOTE, EnumSet.of(EDIT_FCS_CASE_PROCESSING_DOCUMENTS)),
          entry(TECHNICAL_REVIEWER_REASSIGN_OWNERSHIP, EnumSet.of(TECHNICAL_REVIEW_FCS_APPLICATIONS)),
          entry(APPLICATION_UPDATE_REQUEST, EnumSet.of(TECHNICAL_REVIEW_FCS_APPLICATIONS)),
          entry(OPERATOR_WITHDRAWAL_REQUEST, EnumSet.of(EDIT_FCS_APPLICATIONS)),
          entry(OPERATOR_UPDATE_APPLICATION, EnumSet.of(EDIT_FCS_APPLICATIONS))
      );

  private final Map<CaseProcessingActionItem, Set<CaseStatusFlag>> actionsToStatusFlags =
      Map.ofEntries(
          entry(CASE_OFFICER_TAKE_OWNERSHIP, EnumSet.of(CASE_OFFICER_NOT_ASSIGNED)),
          entry(CHANGE_ACE_STATUS, EnumSet.of(CASE_OFFICER_ASSIGNED)),
          entry(CASE_OFFICER_RELEASE_OWNERSHIP, EnumSet.of(CASE_OFFICER_ASSIGNED)),
          entry(CASE_OFFICER_WITHDRAWAL_RESPONSE, EnumSet.of(CASE_OFFICER_ASSIGNED, WITHDRAWAL_OPEN)),
          entry(TECHNICAL_REVIEW_REQUEST,
              EnumSet.of(CASE_OFFICER_ASSIGNED, NO_TECHNICAL_REVIEW_OPEN, NO_APPLICATION_UPDATE_OPEN)),
          entry(CASE_OFFICER_ASSIGN_OWNERSHIP, EnumSet.of(CASE_OFFICER_NOT_ASSIGNED)),
          entry(CASE_OFFICER_REASSIGN_OWNERSHIP, EnumSet.of(CASE_OFFICER_ASSIGNED)),
          entry(REGULATOR_ADD_CASE_NOTE, EnumSet.of(CASE_NOTES_ALLOWED)),
          entry(TECHNICAL_REVIEWER_REASSIGN_OWNERSHIP, EnumSet.of(TECHNICAL_REVIEW_OPEN)),
          entry(APPLICATION_UPDATE_REQUEST, EnumSet.of(TECHNICAL_REVIEW_OPEN, NO_APPLICATION_UPDATE_OPEN)),
          entry(OPERATOR_WITHDRAWAL_REQUEST, EnumSet.of(NO_WITHDRAWAL_OPEN, NO_APPLICATION_UPDATE_OPEN)),
          // TODO FCS-381
          //  the below flags set will never happen together so this effectively disables the operator starting the update
          //  this needs to changed when we do FCS-381
          entry(OPERATOR_UPDATE_APPLICATION, EnumSet.of(APPLICATION_UPDATE_OPEN, NO_APPLICATION_UPDATE_OPEN))
      );

  private final Map<CaseProcessingActionItem, Set<RegulatorTeamRole>> actionsToAssigneeOnlyRoles =
      Map.of(
          CHANGE_ACE_STATUS, EnumSet.of(CASE_OFFICER),
          CASE_OFFICER_RELEASE_OWNERSHIP, EnumSet.of(CASE_OFFICER),
          CASE_OFFICER_WITHDRAWAL_RESPONSE, EnumSet.of(CASE_OFFICER),
          TECHNICAL_REVIEW_REQUEST, EnumSet.of(CASE_OFFICER),
          APPLICATION_UPDATE_REQUEST, EnumSet.of(TECHNICAL_REVIEWER)
      );

  @Autowired
  public CaseProcessingActionService(ApplicationAccessService applicationAccessService,
                                     CaseStatusFlagService caseStatusFlagService,
                                     ApplicationVersionService applicationVersionService,
                                     TechnicalReviewService technicalReviewService) {
    this.applicationAccessService = applicationAccessService;
    this.caseStatusFlagService = caseStatusFlagService;
    this.applicationVersionService = applicationVersionService;
    this.technicalReviewService = technicalReviewService;
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
    var regulatorRoleCurrentAssigneeMap = constructAssigneeMap(applicationVersion);

    return actions.stream()
        // filter actions that the user has permissions for
        .filter(action -> CollectionUtils.containsAny(actionsToPermissions.get(action), userRolePermissions))
        // filter actions that the application version has all the status flags for
        .filter(action -> caseStatusFlags.containsAll(actionsToStatusFlags.get(action)))
        .filter(action -> assigneeCheck(action, regulatorRoleCurrentAssigneeMap, user))
        .toList();
  }

  public List<CaseProcessingActionView> getUserActionViews(ApplicationVersion applicationVersion,
                                                           ServiceUserDetail user) {
    return getUserActionItems(applicationVersion, user)
        .stream()
        .map(action -> CaseProcessingActionView.from(action, applicationVersion))
        .sorted(Comparator.comparing(CaseProcessingActionView::getDisplayOrder))
        .toList();
  }

  private Map<RegulatorTeamRole, WebUserAccountId> constructAssigneeMap(ApplicationVersion applicationVersion) {
    var assigneeMap = new EnumMap<RegulatorTeamRole, WebUserAccountId>(RegulatorTeamRole.class);

    applicationVersionService.findCaseOfficerWuaId(applicationVersion)
        .ifPresent(caseOfficerWuaId -> assigneeMap.put(CASE_OFFICER, caseOfficerWuaId));

    technicalReviewService.findTechnicalReviewerWuaId(applicationVersion)
        .ifPresent(technicalReviewerWuaId -> assigneeMap.put(TECHNICAL_REVIEWER, technicalReviewerWuaId));

    return assigneeMap;
  }

  private boolean assigneeCheck(CaseProcessingActionItem action,
                                Map<RegulatorTeamRole, WebUserAccountId> assigneeMap,
                                ServiceUserDetail user) {

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
}
