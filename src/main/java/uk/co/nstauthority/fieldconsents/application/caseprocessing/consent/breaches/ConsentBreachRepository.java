package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.breaches;


import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.ListCrudRepository;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.Consent;
import uk.co.nstauthority.fieldconsents.application.duplication.NotDuplicationSource;

@NotDuplicationSource
public interface ConsentBreachRepository extends ListCrudRepository<ConsentBreach, UUID> {

  Optional<ConsentBreach> findByConsent(Consent consent);

  Optional<ConsentBreach> findByConsent_Application(Application application);

  boolean existsByConsent_Application(Application application);
}
