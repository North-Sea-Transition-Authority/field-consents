package uk.co.nstauthority.fieldconsents.application.bulkcaseactions.bulkissueconsents;

import java.util.List;
import java.util.UUID;
import org.springframework.data.repository.ListCrudRepository;
import uk.co.nstauthority.fieldconsents.application.duplication.NotDuplicationSource;

@NotDuplicationSource
public interface BulkIssueConsentTaskRepository extends ListCrudRepository<BulkIssueConsentsTask, UUID> {

  List<BulkIssueConsentsTask> findAllByFinishedAtIsNull();

  List<BulkIssueConsentsTask> findAllByBulkIssueConsentRun(BulkIssueConsentRun bulkIssueConsentRun);

  long countAllByFinishedAtIsNull();

  long countAllByFinishedAtIsNullAndBulkIssueConsentRun(BulkIssueConsentRun bulkIssueConsentRun);

}
