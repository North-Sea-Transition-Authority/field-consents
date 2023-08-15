package uk.co.nstauthority.fieldconsents.application;

import static uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType.APPLICATION_CREATED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType.APPLICATION_DELETED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType.APPLICATION_SUBMITTED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType.APPLICATION_UPDATE_STARTED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType.DRAFT_APPLICATION_UPDATE_DELETED;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEvent;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventService;


@Service
public class ApplicationCaseEventService implements CaseEventService<Application> {

  private final ApplicationVersionService applicationVersionService;

  private final ApplicationVersionAuditService applicationVersionAuditService;

  public ApplicationCaseEventService(ApplicationVersionService applicationVersionService,
                                     ApplicationVersionAuditService applicationVersionAuditService) {
    this.applicationVersionService = applicationVersionService;
    this.applicationVersionAuditService = applicationVersionAuditService;
  }

  @Override
  public List<CaseEvent> getCaseEvents(Application application) {
    var caseEvents = new ArrayList<CaseEvent>();

    var allApplicationVersions =
        applicationVersionService.getAllApplicationVersionsByApplicationId(application.getId());

    // we will only get 1 audit event for each deleted application version
    Map<Integer, ApplicationVersionAudit> applicationVersionDeleteAuditsMap = applicationVersionAuditService
        .getApplicationVersionAudits(allApplicationVersions)
        .stream()
        .filter(applicationVersionAudit -> ApplicationVersionStatus.DELETED.equals(applicationVersionAudit.status()))
        .collect(Collectors.toMap(
            ApplicationVersionAudit::applicationVersionId,
            Function.identity()
        ));

    for (ApplicationVersion applicationVersion : allApplicationVersions) {
      caseEvents.add(
          CaseEvent.builder(applicationVersion)
              .withEventType(applicationVersion.isUpdateVersion() ? APPLICATION_UPDATE_STARTED : APPLICATION_CREATED)
              .withMainEventUserWuaId(applicationVersion.getCreatedByWuaId())
              .withEventDateTime(applicationVersion.getCreatedDateTime())
              .build()
      );

      // this is only for the first version being submitted
      // application updates being submitted is catered for in ApplicationUpdateCaseEventService
      if (Objects.nonNull(applicationVersion.getSubmittedByWuaId())
          && Objects.nonNull(applicationVersion.getSubmittedDateTime())
          && applicationVersion.isFirstVersion()) {
        caseEvents.add(
            CaseEvent.builder(applicationVersion)
                .withEventType(APPLICATION_SUBMITTED)
                .withMainEventUserWuaId(applicationVersion.getSubmittedByWuaId())
                .withEventDateTime(applicationVersion.getSubmittedDateTime())
                .build()
        );
      }

      if (ApplicationVersionStatus.DELETED.equals(applicationVersion.getStatus())) {
        var applicationVersionDeleteAudit = applicationVersionDeleteAuditsMap.get(applicationVersion.getId());
        caseEvents.add(
            CaseEvent.builder(applicationVersion)
                .withEventType(applicationVersion.isUpdateVersion() ? DRAFT_APPLICATION_UPDATE_DELETED : APPLICATION_DELETED)
                .withMainEventUserWuaId(applicationVersionDeleteAudit.auditUserWuaId())
                .withEventDateTime(applicationVersionDeleteAudit.auditDateTime())
                .build()
        );
      }
    }
    return caseEvents;
  }
}
