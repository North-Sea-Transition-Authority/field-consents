package uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment;

import static uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType.CASE_OFFICER_ASSIGNED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType.CASE_OFFICER_OWNERSHIP_RELEASED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType.CASE_OFFICER_OWNERSHIP_TAKEN;

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

      // previous audit event
      var previousApplicationVersionAudit = index > 0 ? applicationVersionAudits.get(index - 1) : null;
      var previousCaseOfficer = Objects.nonNull(previousApplicationVersionAudit)
          ? previousApplicationVersionAudit.caseOfficerWuaId()
          : null;
      var previousCaseOfficerWasAssigned = Objects.nonNull(previousCaseOfficer);

      // current audit event
      var currentCaseOfficer = applicationVersionAudit.caseOfficerWuaId();
      var currentCaseOfficerIsAssigned = Objects.nonNull(currentCaseOfficer);
      var auditUserIsCurrentCaseOfficer =
          applicationVersionAudit.auditUserWuaId().equals(currentCaseOfficer);
      // first assignment - null to non null case officer
      // release ownership - non null to null case officer
      // assignment change - previous and current case officer both non null and different
      var caseOfficerHasChanged = (!previousCaseOfficerWasAssigned && currentCaseOfficerIsAssigned)
          || (previousCaseOfficerWasAssigned && !currentCaseOfficerIsAssigned)
          || (previousCaseOfficerWasAssigned && !currentCaseOfficer.equals(previousCaseOfficer));

      // short circuit if the case officer hasn't changed (we can ignore this audit row)
      if (!caseOfficerHasChanged) {
        continue;
      }

      // we now know that the case officer has changed

      // release ownership event
      if (previousCaseOfficerWasAssigned && !currentCaseOfficerIsAssigned) {
        caseAssignmentEvents.add(
            getCaseOwnershipEvent(applicationVersionAudit, applicationVersion, CASE_OFFICER_OWNERSHIP_RELEASED)
        );
      } else if (auditUserIsCurrentCaseOfficer) {
        // take ownership event
        caseAssignmentEvents.add(
            getCaseOwnershipEvent(applicationVersionAudit, applicationVersion, CASE_OFFICER_OWNERSHIP_TAKEN)
        );
      } else {
        // we must have an assignment event (case officer assigned by another user)
        caseAssignmentEvents.add(getCaseAssignmentEvent(applicationVersionAudit, applicationVersion));
      }
    }

    return caseAssignmentEvents;
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
}
