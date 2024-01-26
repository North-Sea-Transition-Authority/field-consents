package uk.co.nstauthority.fieldconsents.flarevent.category123.flare.shortterm;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.duplication.NotDuplicationSource;

@Repository
@NotDuplicationSource
public interface FlareShortTerm123MonthRepository extends CrudRepository<FlareShortTerm123Month, Integer> {
  List<FlareShortTerm123Month> findAllByApplicationVersion(ApplicationVersion applicationVersion);
}
