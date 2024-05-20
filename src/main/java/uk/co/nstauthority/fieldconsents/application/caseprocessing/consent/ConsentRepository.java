package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.fieldconsents.application.duplication.NotDuplicationSource;

@Repository
@NotDuplicationSource
public interface ConsentRepository extends ListCrudRepository<Consent, Integer> {

  Optional<Consent> findByApplicationId(int applicationId);

  @Query(
      """
      SELECT c
      FROM Application a
      JOIN Application ra ON ra.applicationNo = a.applicationNo AND ra.variationNo < a.variationNo
      JOIN Consent c ON c.application = ra
      WHERE a.id = :applicationId
      ORDER BY ra.variationNo DESC LIMIT 1
      """
  )
  Optional<Consent> findPreviousConsentByApplicationId(int applicationId);
}
