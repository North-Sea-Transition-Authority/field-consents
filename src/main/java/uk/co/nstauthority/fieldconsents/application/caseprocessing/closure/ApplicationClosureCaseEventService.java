package uk.co.nstauthority.fieldconsents.application.caseprocessing.closure;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionAudit;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionAuditService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEvent;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType;

@Service
public class ApplicationClosureCaseEventService implements CaseEventService<Application> {

  private final ApplicationVersionService applicationVersionService;
  private final ApplicationVersionAuditService applicationVersionAuditService;

  public ApplicationClosureCaseEventService(ApplicationVersionService applicationVersionService,
                                            ApplicationVersionAuditService applicationVersionAuditService) {
    this.applicationVersionService = applicationVersionService;
    this.applicationVersionAuditService = applicationVersionAuditService;
  }

  @Override
  public List<CaseEvent> getCaseEvents(Application application) {
    var caseEvents = new ArrayList<CaseEvent>();
    var applicationId = application.getId();

    var allApplicationVersions =
        applicationVersionService.getAllApplicationVersionsByApplicationId(applicationId);

    var allApplicationVersionsByApplicationVersionId = allApplicationVersions.stream()
        .collect(Collectors.toMap(
            ApplicationVersion::getId,
            Function.identity()
        ));

    var applicationVersionClosedAuditsByApplicationVersionId =
        applicationVersionAuditService.getApplicationVersionAudits(allApplicationVersions).stream()
            .filter((applicationVersionAudit -> ApplicationVersionStatus.CLOSED.equals(applicationVersionAudit.status())))
            .collect(Collectors.toMap(
                ApplicationVersionAudit::applicationVersionId,
                Function.identity()
            ));

    applicationVersionClosedAuditsByApplicationVersionId.forEach(
        (applicationVersionId, applicationVersionAudit) -> caseEvents.add(
            CaseEvent.builder(allApplicationVersionsByApplicationVersionId
                    .get(applicationVersionId))
                .withEventType(CaseEventType.APPLICATION_CLOSED)
                .withMainEventUserWuaId(applicationVersionAudit.caseOfficerWuaId())
                .withEventDateTime(applicationVersionAudit.auditDateTime())
                .build()
        )
    );

    return caseEvents;
  }
}
