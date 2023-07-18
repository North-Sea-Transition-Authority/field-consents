package uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents;

import java.util.List;

public interface CaseEventService<T> {

  List<CaseEvent> getCaseEvents(T source);

}
