package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.duplication.NotDuplicationSource;

@NotDuplicationSource
interface ConsultationRepository extends CrudRepository<Consultation, Integer> {

  Optional<Consultation> findByRequestApplicationVersion_ApplicationAndStatus(
      Application application,
      ConsultationStatus status
  );

  List<Consultation> findAllByRequestApplicationVersion_ApplicationOrderById(Application application);
}
