package uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

@Repository
public interface FlareReportPeriodRepository extends CrudRepository<FlareReportPeriod, Integer> {

  Optional<FlareReportPeriod> findByApplicationVersion(ApplicationVersion applicationVersion);

  void deleteByApplicationVersion(ApplicationVersion applicationVersion);
}