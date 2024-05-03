package uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents;

import java.util.Collection;

public interface CaseEventService<T> {

  Collection<CaseEvent> getCaseEvents(T source);

}
