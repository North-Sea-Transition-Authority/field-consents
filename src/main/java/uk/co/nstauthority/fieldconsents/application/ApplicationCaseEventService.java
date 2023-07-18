package uk.co.nstauthority.fieldconsents.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEvent;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventView;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;

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

      // TODO: The CASE_OFFICER_ASSIGNED event needs to be worked out by using envers on FCS-397
    }
    return caseEvents;
  }

  public CaseEventView getCaseEventViewForApplication(CaseEvent caseEvent, Map<Long, EnergyPortalUserDto> portalUserDtosMap) {
    var eventType = caseEvent.eventType();
    return CaseEventView.builder()
        .withApplicationVersionNumber(String.valueOf(caseEvent.applicationVersion().getVersion()))
        .withHeaderText(eventType.getCaseEventHeader())
        .withMainUserInvolvedLabel(eventType.getCaseEventUserLabel())
        .withMainUserInvolvedFullName(portalUserDtosMap.get(caseEvent.mainEventUserWuaId()).displayName())
        .withEventDateTimeLabel(eventType.getCaseEventDateTimeLabel())
        .withEventDateTimeText(DateUtils.format(caseEvent.eventDateTime(), DateUtils.DATE_TIME))
        .build();
  }
}
