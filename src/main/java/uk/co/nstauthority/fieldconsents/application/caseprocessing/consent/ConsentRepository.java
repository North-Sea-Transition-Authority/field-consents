package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent;

import java.util.Optional;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.fieldconsents.application.duplication.NotDuplicationSource;

@Repository
@NotDuplicationSource
public interface ConsentRepository extends ListCrudRepository<Consent, Integer> {

  Optional<Consent> findByApplication_Id(Integer applicationId);
}
