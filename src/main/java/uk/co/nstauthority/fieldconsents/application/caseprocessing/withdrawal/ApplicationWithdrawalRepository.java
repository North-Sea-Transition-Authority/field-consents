package uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.duplication.NotDuplicationSource;

@Repository
@NotDuplicationSource
public interface ApplicationWithdrawalRepository extends CrudRepository<ApplicationWithdrawal, Integer> {


  boolean existsByApplicationVersionAndWithdrawalStatus(ApplicationVersion applicationVersion, WithdrawalStatus status);

  Optional<ApplicationWithdrawal> findByApplicationVersionAndWithdrawalStatus(
      ApplicationVersion applicationVersion,
      WithdrawalStatus status
  );
}
