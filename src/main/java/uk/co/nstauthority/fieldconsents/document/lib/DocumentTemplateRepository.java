package uk.co.nstauthority.fieldconsents.document.lib;

import java.util.UUID;
import org.springframework.data.repository.ListCrudRepository;
import uk.co.nstauthority.fieldconsents.application.duplication.NotDuplicationSource;

@NotDuplicationSource
interface DocumentTemplateRepository extends ListCrudRepository<DocumentTemplate, UUID> {
}
