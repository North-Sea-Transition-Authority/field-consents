package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.document;

import org.springframework.data.repository.ListCrudRepository;
import uk.co.nstauthority.fieldconsents.application.duplication.NotDuplicationSource;

@NotDuplicationSource
interface ConsentDocumentGenerationDataRepository extends ListCrudRepository<ConsentDocumentGenerationData, Integer> {
}
