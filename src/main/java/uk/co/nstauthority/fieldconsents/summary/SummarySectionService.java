package uk.co.nstauthority.fieldconsents.summary;

import java.util.Optional;

public interface SummarySectionService<T> {
  Optional<SummarySection> getSummarySection(T source);
}
