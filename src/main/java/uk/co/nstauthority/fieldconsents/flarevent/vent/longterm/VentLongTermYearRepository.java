package uk.co.nstauthority.fieldconsents.flarevent.vent.longterm;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.duplication.NotDuplicationSource;

@Repository
@NotDuplicationSource
public interface VentLongTermYearRepository extends CrudRepository<VentLongTermYear, Integer> {
  List<VentLongTermYear> findAllByApplicationVersion(ApplicationVersion applicationVersion);
}
