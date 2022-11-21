package uk.co.nstauthority.fieldconsents.production.shortterm;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

@Repository
public interface ShortTermProductionMonthRepository extends CrudRepository<ShortTermProductionMonth, Integer> {
  List<ShortTermProductionMonth> findAllByApplicationVersion(ApplicationVersion applicationVersion);

  boolean existsByApplicationVersion(ApplicationVersion applicationVersion);

  void deleteAllByApplicationVersion(ApplicationVersion applicationVersion);
}
