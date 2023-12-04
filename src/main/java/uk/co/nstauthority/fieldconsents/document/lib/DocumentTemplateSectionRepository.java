package uk.co.nstauthority.fieldconsents.document.lib;

import java.util.List;
import java.util.UUID;
import org.springframework.data.repository.ListCrudRepository;
import uk.co.nstauthority.fieldconsents.application.duplication.NotDuplicationSource;

@NotDuplicationSource
interface DocumentTemplateSectionRepository extends ListCrudRepository<DocumentTemplateSection, UUID> {

  List<DocumentTemplateSection> findAllByDocumentTemplateId(UUID documentTemplateId);
}
