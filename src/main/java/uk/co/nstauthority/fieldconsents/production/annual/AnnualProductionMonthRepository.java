package uk.co.nstauthority.fieldconsents.production.annual;

import java.time.Month;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

@Repository
public interface AnnualProductionMonthRepository extends CrudRepository<AnnualProductionMonth, Integer> {
  List<AnnualProductionMonth> findAllByApplicationVersion(ApplicationVersion applicationVersion);

  Optional<AnnualProductionMonth> findByApplicationVersionAndMonth(ApplicationVersion applicationVersion, Month month);

  void deleteAllByApplicationVersion(ApplicationVersion applicationVersion);
}
