package uk.co.nstauthority.fieldconsents.flarevent.vent.shortterm;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

@Repository
public interface VentShortTermMonthRepository extends CrudRepository<VentShortTermMonth, Integer> {

  List<VentShortTermMonth> findAllByApplicationVersion(ApplicationVersion applicationVersion);

  boolean existsByApplicationVersion(ApplicationVersion applicationVersion);

  void deleteAllByApplicationVersion(ApplicationVersion applicationVersion);
}
