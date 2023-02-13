package uk.co.nstauthority.fieldconsents.flarevent.flare.flarereportgas;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

public interface FlareReportGasDataRepository extends CrudRepository<FlareReportGasData, Integer> {

  Optional<FlareReportGasData> findByApplicationVersion(ApplicationVersion applicationVersion);

  void deleteByApplicationVersion(ApplicationVersion applicationVersion);
}
