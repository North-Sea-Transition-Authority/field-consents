package uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.Application;

@Service
public class CaseHistoryEventService {

  private final List<CaseEventService<Application>> caseEventServices;

  @Autowired
  public CaseHistoryEventService(List<CaseEventService<Application>> caseEventServices) {
    this.caseEventServices = caseEventServices;
  }

  public List<CaseEvent> getCaseHistoryEvents(Application application) {
    return caseEventServices
        .stream()
        .map(caseEventService -> caseEventService.getCaseEvents(application))
        .flatMap(Collection::stream)
        .sorted(Comparator.comparing(CaseEvent::eventDateTime).reversed())
        .toList();
  }
}
