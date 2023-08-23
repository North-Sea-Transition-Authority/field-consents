package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation;

import org.springframework.data.repository.CrudRepository;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.duplication.NotDuplicationSource;

@NotDuplicationSource
interface ConsultationRepository extends CrudRepository<Consultation, Integer> {

  boolean existsByRequestApplicationVersion(ApplicationVersion applicationVersion);

}
