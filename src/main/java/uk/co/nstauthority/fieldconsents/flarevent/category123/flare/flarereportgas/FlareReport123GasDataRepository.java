package uk.co.nstauthority.fieldconsents.flarevent.category123.flare.flarereportgas;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.duplication.NotDuplicationSource;

@Repository
@NotDuplicationSource
public interface FlareReport123GasDataRepository extends CrudRepository<FlareReport123GasData, Integer> {
  Optional<FlareReport123GasData> findByApplicationVersion(ApplicationVersion applicationVersion);
}
