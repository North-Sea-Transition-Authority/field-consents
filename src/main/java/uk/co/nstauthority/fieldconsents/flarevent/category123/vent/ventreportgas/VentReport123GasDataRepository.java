package uk.co.nstauthority.fieldconsents.flarevent.category123.vent.ventreportgas;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.duplication.NotDuplicationSource;

@Repository
@NotDuplicationSource
public interface VentReport123GasDataRepository extends CrudRepository<VentReport123GasData, Integer> {
  Optional<VentReport123GasData> findByApplicationVersion(ApplicationVersion applicationVersion);
}
