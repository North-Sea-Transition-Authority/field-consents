package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformation;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.Consultation;
import uk.co.nstauthority.fieldconsents.application.duplication.NotDuplicationSource;

@NotDuplicationSource
interface FurtherInformationRepository extends CrudRepository<FurtherInformation, Integer> {

  Optional<FurtherInformation> findByConsultationAndStatus(
      Consultation consultation,
      FurtherInformationStatus status
  );

  List<FurtherInformation> findAllByConsultationInOrderById(Collection<Consultation> consultation);

}
