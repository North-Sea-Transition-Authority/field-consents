package uk.co.nstauthority.fieldconsents.production.shortterm;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.duplication.DuplicateThisOnUpdate;
import uk.co.nstauthority.fieldconsents.application.duplication.DuplicationSource;

@Repository
public interface ShortTermProductionMonthRepository extends CrudRepository<ShortTermProductionMonth, Integer>, DuplicationSource {

  @DuplicateThisOnUpdate
  List<ShortTermProductionMonth> findAllByApplicationVersion(ApplicationVersion applicationVersion);

  boolean existsByApplicationVersion(ApplicationVersion applicationVersion);

  List<ShortTermProductionMonth> findAllByApplicationVersionOrderByStartDate(ApplicationVersion applicationVersion);

  void deleteAllByApplicationVersion(ApplicationVersion applicationVersion);
}
