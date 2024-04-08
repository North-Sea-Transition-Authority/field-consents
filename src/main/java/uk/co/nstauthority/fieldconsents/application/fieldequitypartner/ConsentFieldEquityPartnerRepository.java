package uk.co.nstauthority.fieldconsents.application.fieldequitypartner;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.fieldconsents.application.duplication.NotDuplicationSource;

@Repository
@NotDuplicationSource
public interface ConsentFieldEquityPartnerRepository extends CrudRepository<ConsentFieldEquityPartner, Integer> {
}
