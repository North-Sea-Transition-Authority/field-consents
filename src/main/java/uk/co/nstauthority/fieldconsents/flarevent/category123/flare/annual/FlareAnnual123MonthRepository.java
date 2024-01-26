package uk.co.nstauthority.fieldconsents.flarevent.category123.flare.annual;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.duplication.NotDuplicationSource;

@Repository
@NotDuplicationSource
public interface FlareAnnual123MonthRepository extends CrudRepository<FlareAnnual123Month, Integer> {
  List<FlareAnnual123Month> findAllByApplicationVersion(ApplicationVersion applicationVersion);
}
