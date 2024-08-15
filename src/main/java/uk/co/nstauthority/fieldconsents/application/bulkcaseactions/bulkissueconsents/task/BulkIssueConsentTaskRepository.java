package uk.co.nstauthority.fieldconsents.application.bulkcaseactions.bulkissueconsents.task;

import java.util.List;
import java.util.UUID;
import org.springframework.data.repository.ListCrudRepository;
import uk.co.nstauthority.fieldconsents.application.duplication.NotDuplicationSource;

@NotDuplicationSource
interface BulkIssueConsentTaskRepository extends ListCrudRepository<BulkIssueConsentsTask, UUID> {

  List<BulkIssueConsentsTask> findAllByFinishedAtIsNull();

  long countAllByFinishedAtIsNull();

}
