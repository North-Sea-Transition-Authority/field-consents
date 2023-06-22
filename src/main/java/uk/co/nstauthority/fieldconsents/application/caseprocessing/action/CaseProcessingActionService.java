package uk.co.nstauthority.fieldconsents.application.caseprocessing.action;

import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CASE_OFFICER_ASSIGN_OWNERSHIP;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CASE_OFFICER_REASSIGN_OWNERSHIP;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CASE_OFFICER_RELEASE_OWNERSHIP;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CASE_OFFICER_TAKE_OWNERSHIP;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CASE_OFFICER_WITHDRAWAL_RESPONSE;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CHANGE_ACE_STATUS;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.OPERATOR_WITHDRAWAL_REQUEST;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.CASE_OFFICER_ASSIGNED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.CASE_OFFICER_NOT_ASSIGNED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.NO_WITHDRAWAL_OPEN;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag.WITHDRAWAL_OPEN;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.ASSIGN_FCS_APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.EDIT_FCS_APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.PROCESS_FCS_APPLICATIONS;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlag;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag.CaseStatusFlagService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.ApplicationAccessService;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@Service
public class CaseProcessingActionService {

  private final ApplicationAccessService applicationAccessService;

  private final CaseStatusFlagService caseStatusFlagService;

  private final Map<ApplicationVersionStatus, Set<CaseProcessingActionItem>> caseStatusToActions =
      Map.of(
          ApplicationVersionStatus.SUBMITTED,
          EnumSet.of(
              CASE_OFFICER_TAKE_OWNERSHIP,
              CHANGE_ACE_STATUS,
              CASE_OFFICER_RELEASE_OWNERSHIP,
              CASE_OFFICER_WITHDRAWAL_RESPONSE,
              CASE_OFFICER_ASSIGN_OWNERSHIP,
              CASE_OFFICER_REASSIGN_OWNERSHIP,
              OPERATOR_WITHDRAWAL_REQUEST)
      );

  private final Map<CaseProcessingActionItem, Set<RolePermission>> actionsToPermissions =
      Map.of(
          CASE_OFFICER_TAKE_OWNERSHIP, EnumSet.of(PROCESS_FCS_APPLICATIONS),
          CHANGE_ACE_STATUS, EnumSet.of(PROCESS_FCS_APPLICATIONS),
          CASE_OFFICER_RELEASE_OWNERSHIP, EnumSet.of(PROCESS_FCS_APPLICATIONS),
          CASE_OFFICER_WITHDRAWAL_RESPONSE, EnumSet.of(PROCESS_FCS_APPLICATIONS),
          CASE_OFFICER_ASSIGN_OWNERSHIP, EnumSet.of(ASSIGN_FCS_APPLICATIONS),
          CASE_OFFICER_REASSIGN_OWNERSHIP, EnumSet.of(ASSIGN_FCS_APPLICATIONS),
          OPERATOR_WITHDRAWAL_REQUEST, EnumSet.of(EDIT_FCS_APPLICATIONS)
      );

  private final Map<CaseProcessingActionItem, Set<CaseStatusFlag>> actionsToStatusFlags =
      Map.of(
          CASE_OFFICER_TAKE_OWNERSHIP, EnumSet.of(CASE_OFFICER_NOT_ASSIGNED),
          CHANGE_ACE_STATUS, EnumSet.of(CASE_OFFICER_ASSIGNED),
          CASE_OFFICER_RELEASE_OWNERSHIP, EnumSet.of(CASE_OFFICER_ASSIGNED),
          CASE_OFFICER_WITHDRAWAL_RESPONSE, EnumSet.of(WITHDRAWAL_OPEN),
          CASE_OFFICER_ASSIGN_OWNERSHIP, EnumSet.of(CASE_OFFICER_NOT_ASSIGNED),
          CASE_OFFICER_REASSIGN_OWNERSHIP, EnumSet.of(CASE_OFFICER_ASSIGNED),
          OPERATOR_WITHDRAWAL_REQUEST, EnumSet.of(NO_WITHDRAWAL_OPEN)
      );

  @Autowired
  public CaseProcessingActionService(ApplicationAccessService applicationAccessService,
                                     CaseStatusFlagService caseStatusFlagService) {
    this.applicationAccessService = applicationAccessService;
    this.caseStatusFlagService = caseStatusFlagService;
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

    return actions.stream()
        // filter actions that the user has permissions for
        .filter(action -> CollectionUtils.containsAny(actionsToPermissions.get(action), userRolePermissions))
        // filter actions that the application version has status flag for
        .filter(action -> CollectionUtils.containsAny(actionsToStatusFlags.get(action), caseStatusFlags))
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
}
