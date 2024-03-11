package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.issuing.approval;

import java.util.Optional;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.fieldconsents.application.duplication.NotDuplicationSource;

@Repository
@NotDuplicationSource
public interface ConsentIssuingApprovalRepository extends ListCrudRepository<ConsentIssuingApproval, Integer> {

  boolean existsByApplicationId(Integer applicationId);

  Optional<ConsentIssuingApproval> findByApplicationId(Integer applicationId);
}
