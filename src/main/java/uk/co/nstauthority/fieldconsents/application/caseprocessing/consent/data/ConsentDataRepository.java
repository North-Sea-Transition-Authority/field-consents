package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.ListCrudRepository;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.duplication.NotDuplicationSource;

@NotDuplicationSource
public interface ConsentDataRepository extends ListCrudRepository<ConsentData, UUID> {

  Optional<ConsentData> findByApplication(Application application);

}
