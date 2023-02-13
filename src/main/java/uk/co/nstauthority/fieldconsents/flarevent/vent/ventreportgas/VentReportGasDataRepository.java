package uk.co.nstauthority.fieldconsents.flarevent.vent.ventreportgas;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

public interface VentReportGasDataRepository extends CrudRepository<VentReportGasData, Integer> {

  Optional<VentReportGasData> findByApplicationVersion(ApplicationVersion applicationVersion);

  void deleteByApplicationVersion(ApplicationVersion applicationVersion);
}
