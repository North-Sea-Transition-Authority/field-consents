package uk.co.nstauthority.fieldconsents.flarevent.flare.annual;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

@Repository
public interface FlareAnnualMonthRepository extends CrudRepository<FlareAnnualMonth, Integer> {

  List<FlareAnnualMonth> findAllByApplicationVersion(ApplicationVersion applicationVersion);

  boolean existsByApplicationVersion(ApplicationVersion applicationVersion);

  void deleteAllByApplicationVersion(ApplicationVersion applicationVersion);
}
