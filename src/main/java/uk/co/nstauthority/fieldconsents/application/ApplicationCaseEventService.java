package uk.co.nstauthority.fieldconsents.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEvent;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType;


@Service
public class ApplicationCaseEventService implements CaseEventService<Application> {

  private final ApplicationVersionService applicationVersionService;

  public ApplicationCaseEventService(ApplicationVersionService applicationVersionService) {
    this.applicationVersionService = applicationVersionService;
  }

  @Override
  public List<CaseEvent> getCaseEvents(Application application) {
    var caseEvents = new ArrayList<CaseEvent>();

    var allApplicationVersions =
        applicationVersionService.getAllApplicationVersionsByApplicationId(application.getId());

    for (ApplicationVersion applicationVersion : allApplicationVersions) {
      caseEvents.add(
          CaseEvent.builder(applicationVersion)
              .withEventType(CaseEventType.APPLICATION_CREATED)
              .withMainEventUserWuaId(applicationVersion.getCreatedByWuaId())
              .withEventDateTime(applicationVersion.getCreatedDateTime())
              .build()
      );

      if (Objects.nonNull(applicationVersion.getSubmittedByWuaId())
          && Objects.nonNull(applicationVersion.getSubmittedDateTime())) {
        caseEvents.add(
            CaseEvent.builder(applicationVersion)
                .withEventType(CaseEventType.APPLICATION_SUBMITTED)
                .withMainEventUserWuaId(applicationVersion.getSubmittedByWuaId())
                .withEventDateTime(applicationVersion.getSubmittedDateTime())
                .build()
        );
      }
    }
    return caseEvents;
  }
}
