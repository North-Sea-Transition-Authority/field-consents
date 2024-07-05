package uk.co.nstauthority.fieldconsents.summary;

import java.util.Optional;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;

public interface SummarySectionService<T> {
  Optional<SummarySection> getSummarySection(T source, ServiceUserDetail user);
}
