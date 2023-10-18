package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformationrequest;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.Consultation;
import uk.co.nstauthority.fieldconsents.application.duplication.NotDuplicationSource;

@NotDuplicationSource
interface FurtherInformationRequestRepository extends CrudRepository<FurtherInformationRequest, Integer> {

  Optional<FurtherInformationRequest> findByConsultationAndStatus(
      Consultation consultation,
      FurtherInformationRequestStatus status
  );

  List<FurtherInformationRequest> findAllByConsultationInOrderById(Collection<Consultation> consultation);

}
