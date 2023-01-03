package uk.co.nstauthority.fieldconsents.flarevent.vent.ventreport;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

@Repository
public interface VentReportPeriodRepository extends CrudRepository<VentReportPeriod, Integer> {

  Optional<VentReportPeriod> findByApplicationVersion(ApplicationVersion applicationVersion);

  void deleteByApplicationVersion(ApplicationVersion applicationVersion);
}