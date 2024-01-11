package uk.co.nstauthority.fieldconsents.document.lib;

import java.util.List;
import java.util.UUID;
import org.springframework.data.repository.ListCrudRepository;
import uk.co.nstauthority.fieldconsents.application.duplication.NotDuplicationSource;

@NotDuplicationSource
interface DocumentInstanceSectionRepository extends ListCrudRepository<DocumentInstanceSection, UUID> {

  List<DocumentInstanceSection> findAllByDocumentInstanceId(UUID documentInstanceId);

  List<DocumentInstanceSection> findAllByParent_IdAndDisplayOrderGreaterThanEqual(UUID parentId, int displayOrder);

  void deleteAllByDocumentInstanceId(UUID documentInstanceId);
}
