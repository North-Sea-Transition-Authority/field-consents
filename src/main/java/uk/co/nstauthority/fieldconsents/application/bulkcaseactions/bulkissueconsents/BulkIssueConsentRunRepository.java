package uk.co.nstauthority.fieldconsents.application.bulkcaseactions.bulkissueconsents;

import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import uk.co.nstauthority.fieldconsents.application.duplication.NotDuplicationSource;

@NotDuplicationSource
public interface BulkIssueConsentRunRepository extends CrudRepository<BulkIssueConsentRun, UUID> {
}
