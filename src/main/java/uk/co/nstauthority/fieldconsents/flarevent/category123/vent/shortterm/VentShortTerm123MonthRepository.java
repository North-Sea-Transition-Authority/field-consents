package uk.co.nstauthority.fieldconsents.flarevent.category123.vent.shortterm;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.duplication.NotDuplicationSource;

@Repository
@NotDuplicationSource
public interface VentShortTerm123MonthRepository extends CrudRepository<VentShortTerm123Month, Integer> {
  List<VentShortTerm123Month> findAllByApplicationVersion(ApplicationVersion applicationVersion);
}
