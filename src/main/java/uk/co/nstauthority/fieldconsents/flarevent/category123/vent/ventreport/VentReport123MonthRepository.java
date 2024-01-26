package uk.co.nstauthority.fieldconsents.flarevent.category123.vent.ventreport;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.duplication.NotDuplicationSource;

@Repository
@NotDuplicationSource
public interface VentReport123MonthRepository extends CrudRepository<VentReport123Month, Integer> {
  List<VentReport123Month> findAllByApplicationVersion(ApplicationVersion applicationVersion);
}
