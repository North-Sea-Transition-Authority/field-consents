package uk.co.nstauthority.fieldconsents.document.lib;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.ListCrudRepository;
import uk.co.nstauthority.fieldconsents.application.duplication.NotDuplicationSource;

@NotDuplicationSource
interface DocumentInstanceRepository extends ListCrudRepository<DocumentInstance, UUID> {

  List<DocumentInstance> findAllByItemReference(String itemReference);

  Optional<DocumentInstance> findByItemReferenceAndItemTypeAndDocumentTemplate_Id(
      String itemReference,
      String itemType,
      UUID documentTemplateId
  );

}
