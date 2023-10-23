package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEvent;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationService;

@Service
class FurtherInformationEventService implements CaseEventService<Application> {

  private final ConsultationService consultationService;
  private final FurtherInformationService furtherInformationService;

  FurtherInformationEventService(
      ConsultationService consultationService,
      FurtherInformationService furtherInformationService
  ) {
    this.consultationService = consultationService;
    this.furtherInformationService = furtherInformationService;
  }

  @Override
  public List<CaseEvent> getCaseEvents(Application application) {
    var consultations = consultationService.getConsultationsByApplication(application);
    var caseEvents = new ArrayList<CaseEvent>();

    for (var furtherInformation : furtherInformationService.getAllFurtherInformation(consultations)) {
      getRequestedCaseEvent(furtherInformation).ifPresent(caseEvents::add);
      getRespondedCaseEvent(furtherInformation).ifPresent(caseEvents::add);
    }

    return caseEvents;
  }

  Optional<CaseEvent> getRequestedCaseEvent(FurtherInformation furtherInformation) {
    if (Objects.isNull(furtherInformation.getRequestedAtDatetime())) {
      return Optional.empty();
    }

    if (Objects.isNull(furtherInformation.getRequestedByWuaId())) {
      return Optional.empty();
    }

    var applicationVersion = furtherInformation.getConsultation().getRequestApplicationVersion();
    var caseEvent = CaseEvent.builder(applicationVersion)
        .withEventType(CaseEventType.FURTHER_INFORMATION_REQUEST_OPENED)
        .withMainEventUserWuaId(furtherInformation.getRequestedByWuaId())
        .withEventDateTime(furtherInformation.getRequestedAtDatetime())
        .withEventText(furtherInformation.getRequestText())
        .build();

    return Optional.of(caseEvent);
  }

  Optional<CaseEvent> getRespondedCaseEvent(FurtherInformation furtherInformation) {
    if (Objects.isNull(furtherInformation.getRespondedAtDatetime())) {
      return Optional.empty();
    }

    if (Objects.isNull(furtherInformation.getRespondedByWuaId())) {
      return Optional.empty();
    }

    var applicationVersion = furtherInformation.getConsultation().getRequestApplicationVersion();
    var caseEvent = CaseEvent.builder(applicationVersion)
        .withEventType(CaseEventType.FURTHER_INFORMATION_REQUEST_CLOSED)
        .withMainEventUserWuaId(furtherInformation.getRespondedByWuaId())
        .withEventDateTime(furtherInformation.getRespondedAtDatetime())
        .withEventText(furtherInformation.getResponseText())
        .build();

    return Optional.of(caseEvent);
  }

}
