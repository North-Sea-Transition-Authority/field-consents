package uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment;

import static uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType.CAM_ASSIGNED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType.CAM_REASSIGNED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType.CASE_OFFICER_ASSIGNED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType.CASE_OFFICER_OWNERSHIP_RELEASED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType.CASE_OFFICER_OWNERSHIP_TAKEN;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType.CASE_OFFICER_REASSIGNED;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionAudit;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionAuditService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEvent;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamRole;

@Service
public class CaseAssignmentEventService implements CaseEventService<Application> {

  private final ApplicationVersionService applicationVersionService;
  private final ApplicationVersionAuditService applicationVersionAuditService;

  @Autowired
  public CaseAssignmentEventService(ApplicationVersionService applicationVersionService,
                                    ApplicationVersionAuditService caseAssignmentAuditService) {
    this.applicationVersionService = applicationVersionService;
    this.applicationVersionAuditService = caseAssignmentAuditService;
  }

  @Override
  public List<CaseEvent> getCaseEvents(Application application) {
    var caseAssignmentEvents = new ArrayList<CaseEvent>();

    var applicationVersions = applicationVersionService.getAllApplicationVersionsByApplicationId(application.getId());

    var applicationVersionsMap = applicationVersions
        .stream()
        .collect(Collectors.toMap(
            ApplicationVersion::getId,
            Function.identity()
        ));

    List<ApplicationVersionAudit> applicationVersionAudits =
        applicationVersionAuditService.getApplicationVersionAudits(applicationVersions);

    for (int index = 0; index < applicationVersionAudits.size(); index++) {
      var applicationVersionAudit = applicationVersionAudits.get(index);
      var applicationVersion = applicationVersionsMap.get(applicationVersionAudit.applicationVersionId());
      var previousApplicationVersionAudit = index > 0 ? applicationVersionAudits.get(index - 1) : null;
      var previousAuditAssignment = getAuditEventAssignment(previousApplicationVersionAudit);
      var currentAuditAssignment = getAuditEventAssignment(applicationVersionAudit);
      // first assignment / reassignment from CAM
      // release ownership / CAM assignment
      // case officer assignment change - previous and current case officer both non null and different
      var caseOfficerHasChanged =
          (!previousAuditAssignment.caseOfficerAssigned && currentAuditAssignment.caseOfficerAssigned)
          || (previousAuditAssignment.caseOfficerAssigned && !currentAuditAssignment.caseOfficerAssigned)
          || (previousAuditAssignment.caseOfficerAssigned && currentAuditAssignment.caseOfficerAssigned
            && !currentAuditAssignment.caseOfficer.equals(previousAuditAssignment.caseOfficer));

      // CAM assignment from case officer
      // CAM returns case to case officer
      // CAM reassigned - previous and current CAM both non null and different
      var camHasChanged =
          (!previousAuditAssignment.camUserAssigned && currentAuditAssignment.camUserAssigned)
          || (previousAuditAssignment.camUserAssigned && !currentAuditAssignment.camUserAssigned)
          || (previousAuditAssignment.camUserAssigned && currentAuditAssignment.camUserAssigned
            && !currentAuditAssignment.camUser.equals(previousAuditAssignment.camUser));

      // short circuit if the neither case officer nor CAM has changed,
      // we can ignore this audit row - case ownership hasn't changed
      if (!caseOfficerHasChanged && !camHasChanged) {
        continue;
      }

      // the case ownership has changed

      if (previousAuditAssignment.caseOfficerAssigned && !currentAuditAssignment.caseOfficerAssigned) {
        // if current case owner is CAM, then CAM is assigned otherwise case officer has released ownership
        if (currentAuditAssignment.camUserAssigned) {
          caseAssignmentEvents.add(
              getCamOwnershipEvent(applicationVersionAudit, applicationVersion)
          );
        } else {
          caseAssignmentEvents.add(
              getCaseOwnershipEvent(applicationVersionAudit, applicationVersion, CASE_OFFICER_OWNERSHIP_RELEASED)
          );
        }
      } else if (previousAuditAssignment.camUserAssigned) {
        if (currentAuditAssignment.caseOfficerAssigned) {
          // the case has returned to the previously assigned case officer from CAM
          caseAssignmentEvents.add(
              getCaseOfficerReassignmentEvent(applicationVersionAudit, applicationVersion)
          );
        } else if (currentAuditAssignment.camUserAssigned) {
          // the case has been reassigned to a different CAM user
          caseAssignmentEvents.add(
              getCamReassignmentEvent(applicationVersionAudit, applicationVersion)
          );
        }
      } else if (currentAuditAssignment.caseOfficerAssigned
          && applicationVersionAudit.auditUserWuaId() != null
          && applicationVersionAudit.auditUserWuaId().equals(currentAuditAssignment.caseOfficer())) {
        // take ownership event
        caseAssignmentEvents.add(
            getCaseOwnershipEvent(applicationVersionAudit, applicationVersion, CASE_OFFICER_OWNERSHIP_TAKEN)
        );
      } else if (currentAuditAssignment.caseOfficerAssigned) {
        // we must have a case officer assignment event (case officer assigned by another user)
        caseAssignmentEvents.add(getCaseAssignmentEvent(applicationVersionAudit, applicationVersion));
      }
    }

    return caseAssignmentEvents;
  }

  private static AuditEventAssignment getAuditEventAssignment(ApplicationVersionAudit applicationVersionAudit) {
    // case officer assignment details
    var caseOfficerId = Objects.nonNull(applicationVersionAudit)
        ? applicationVersionAudit.caseOfficerWuaId()
        : null;
    var caseOfficerAssigned = Objects.nonNull(caseOfficerId)
        && RegulatorTeamRole.CASE_OFFICER.equals(applicationVersionAudit.currentCaseOwner());

    // cam assignment details
    var camUserId = Objects.nonNull(applicationVersionAudit)
        ? applicationVersionAudit.camWuaId()
        : null;
    var camUserAssigned = Objects.nonNull(camUserId)
        && RegulatorTeamRole.CONSENTS_AND_AUTHORISATIONS_MANAGER.equals(applicationVersionAudit.currentCaseOwner());
    
    return new AuditEventAssignment(caseOfficerId, caseOfficerAssigned, camUserId, camUserAssigned);
  }

  private record AuditEventAssignment(Long caseOfficer, boolean caseOfficerAssigned, Long camUser, boolean camUserAssigned) {
  }

  private CaseEvent getCaseOwnershipEvent(ApplicationVersionAudit applicationVersionAudit,
                                          ApplicationVersion applicationVersion,
                                          CaseEventType eventType) {
    return CaseEvent.builder(applicationVersion)
        .withEventType(eventType)
        .withMainEventUserWuaId(applicationVersionAudit.auditUserWuaId())
        .withEventDateTime(applicationVersionAudit.auditDateTime())
        .build();
  }

  private CaseEvent getCaseAssignmentEvent(ApplicationVersionAudit applicationVersionAudit,
                                           ApplicationVersion applicationVersion) {
    return CaseEvent.builder(applicationVersion)
        .withEventType(CASE_OFFICER_ASSIGNED)
        .withMainEventUserWuaId(applicationVersionAudit.auditUserWuaId())
        .withEventDateTime(applicationVersionAudit.auditDateTime())
        .withOtherEventUserWuaId(applicationVersionAudit.caseOfficerWuaId())
        .build();
  }

  private CaseEvent getCamOwnershipEvent(ApplicationVersionAudit applicationVersionAudit,
                                         ApplicationVersion applicationVersion) {
    return CaseEvent.builder(applicationVersion)
        .withEventType(CAM_ASSIGNED)
        .withMainEventUserWuaId(applicationVersionAudit.auditUserWuaId())
        .withEventDateTime(applicationVersionAudit.auditDateTime())
        .withOtherEventUserWuaId(applicationVersionAudit.camWuaId())
        .build();
  }

  private CaseEvent getCaseOfficerReassignmentEvent(ApplicationVersionAudit applicationVersionAudit,
                                                    ApplicationVersion applicationVersion) {
    return CaseEvent.builder(applicationVersion)
        .withEventType(CASE_OFFICER_REASSIGNED)
        .withMainEventUserWuaId(applicationVersionAudit.auditUserWuaId())
        .withEventDateTime(applicationVersionAudit.auditDateTime())
        .withOtherEventUserWuaId(applicationVersionAudit.caseOfficerWuaId())
        .build();
  }

  private CaseEvent getCamReassignmentEvent(ApplicationVersionAudit applicationVersionAudit,
                                            ApplicationVersion applicationVersion) {
    return CaseEvent.builder(applicationVersion)
        .withEventType(CAM_REASSIGNED)
        .withMainEventUserWuaId(applicationVersionAudit.auditUserWuaId())
        .withEventDateTime(applicationVersionAudit.auditDateTime())
        .withOtherEventUserWuaId(applicationVersionAudit.camWuaId())
        .build();
  }
}
