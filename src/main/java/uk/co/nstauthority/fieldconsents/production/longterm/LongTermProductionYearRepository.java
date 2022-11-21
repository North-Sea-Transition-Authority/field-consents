package uk.co.nstauthority.fieldconsents.production.longterm;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

@Repository
public interface LongTermProductionYearRepository extends CrudRepository<LongTermProductionYear, Integer> {

  List<LongTermProductionYear> findAllByApplicationVersionOrderByYearAsc(ApplicationVersion applicationVersion);

  boolean existsByApplicationVersion(ApplicationVersion applicationVersion);

  void deleteAllByApplicationVersion(ApplicationVersion applicationVersion);
}
