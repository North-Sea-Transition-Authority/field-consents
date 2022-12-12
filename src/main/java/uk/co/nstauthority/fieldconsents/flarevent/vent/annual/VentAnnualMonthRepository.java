package uk.co.nstauthority.fieldconsents.flarevent.vent.annual;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

@Repository
public interface VentAnnualMonthRepository extends CrudRepository<VentAnnualMonth, Integer> {

  List<VentAnnualMonth> findAllByApplicationVersion(ApplicationVersion applicationVersion);

  boolean existsByApplicationVersion(ApplicationVersion applicationVersion);

  void deleteAllByApplicationVersion(ApplicationVersion applicationVersion);
}
