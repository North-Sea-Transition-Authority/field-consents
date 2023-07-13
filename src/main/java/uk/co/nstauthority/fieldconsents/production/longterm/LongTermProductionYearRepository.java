package uk.co.nstauthority.fieldconsents.production.longterm;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.duplication.DuplicateThisOnUpdate;
import uk.co.nstauthority.fieldconsents.application.duplication.DuplicationSource;

@Repository
public interface LongTermProductionYearRepository extends CrudRepository<LongTermProductionYear, Integer>, DuplicationSource {

  @DuplicateThisOnUpdate
  List<LongTermProductionYear> findAllByApplicationVersion(ApplicationVersion applicationVersion);

  List<LongTermProductionYear> findAllByApplicationVersionOrderByYearAsc(ApplicationVersion applicationVersion);

  boolean existsByApplicationVersion(ApplicationVersion applicationVersion);

  void deleteAllByApplicationVersion(ApplicationVersion applicationVersion);
}
