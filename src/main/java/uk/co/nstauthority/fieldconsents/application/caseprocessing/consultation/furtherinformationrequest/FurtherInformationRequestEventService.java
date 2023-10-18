package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformationrequest;

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
class FurtherInformationRequestEventService implements CaseEventService<Application> {

  private final ConsultationService consultationService;
  private final FurtherInformationRequestService furtherInformationRequestService;

  FurtherInformationRequestEventService(
      ConsultationService consultationService,
      FurtherInformationRequestService furtherInformationRequestService
  ) {
    this.consultationService = consultationService;
    this.furtherInformationRequestService = furtherInformationRequestService;
  }

  @Override
  public List<CaseEvent> getCaseEvents(Application application) {
    var consultations = consultationService.getConsultationsByApplication(application);
    var furtherInformationRequests = furtherInformationRequestService.getAllFurtherInformationRequests(consultations);

    var caseEvents = new ArrayList<CaseEvent>();

    for (var furtherInformationRequest : furtherInformationRequests) {
      getRequestedCaseEvent(furtherInformationRequest).ifPresent(caseEvents::add);
      // TODO: FCS-451 - add response event
    }

    return caseEvents;
  }

  Optional<CaseEvent> getRequestedCaseEvent(FurtherInformationRequest furtherInformationRequest) {
    if (Objects.isNull(furtherInformationRequest.getRequestedAtDatetime())) {
      return Optional.empty();
    }

    if (Objects.isNull(furtherInformationRequest.getRequestedByWuaId())) {
      return Optional.empty();
    }

    var applicationVersion = furtherInformationRequest.getConsultation().getRequestApplicationVersion();
    var caseEvent = CaseEvent.builder(applicationVersion)
        .withEventType(CaseEventType.FURTHER_INFORMATION_REQUEST_OPENED)
        .withMainEventUserWuaId(furtherInformationRequest.getRequestedByWuaId())
        .withEventDateTime(furtherInformationRequest.getRequestedAtDatetime())
        .withEventText(furtherInformationRequest.getRequestText())
        .build();

    return Optional.of(caseEvent);
  }
}
