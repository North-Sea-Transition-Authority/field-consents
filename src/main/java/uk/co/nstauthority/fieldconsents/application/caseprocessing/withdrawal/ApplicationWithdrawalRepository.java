package uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.duplication.NotDuplicationSource;

@Repository
@NotDuplicationSource
public interface ApplicationWithdrawalRepository extends CrudRepository<ApplicationWithdrawal, Integer> {


  boolean existsByApplicationVersion_ApplicationAndWithdrawalStatus(Application application, WithdrawalStatus status);

  Optional<ApplicationWithdrawal> findByApplicationVersion_ApplicationAndWithdrawalStatus(
      Application application,
      WithdrawalStatus status
  );

  List<ApplicationWithdrawal> findByApplicationVersion_Application(Application application);
}
