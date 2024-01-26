package uk.co.nstauthority.fieldconsents.flarevent.category123.flare.flarereport;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.duplication.NotDuplicationSource;

@Repository
@NotDuplicationSource
public interface FlareReport123MonthRepository extends CrudRepository<FlareReport123Month, Integer> {
  List<FlareReport123Month> findAllByApplicationVersion(ApplicationVersion applicationVersion);
}
