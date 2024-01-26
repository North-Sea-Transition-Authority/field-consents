package uk.co.nstauthority.fieldconsents.flarevent.category123.vent.annual;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.duplication.NotDuplicationSource;

@Repository
@NotDuplicationSource
public interface VentAnnual123MonthRepository extends CrudRepository<VentAnnual123Month, Integer> {
  List<VentAnnual123Month> findAllByApplicationVersion(ApplicationVersion applicationVersion);
}
