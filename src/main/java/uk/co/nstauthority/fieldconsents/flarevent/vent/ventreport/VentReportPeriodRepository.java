package uk.co.nstauthority.fieldconsents.flarevent.vent.ventreport;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.duplication.DuplicateThisOnUpdate;
import uk.co.nstauthority.fieldconsents.application.duplication.DuplicationSource;

@Repository
public interface VentReportPeriodRepository extends CrudRepository<VentReportPeriod, Integer>, DuplicationSource {

  @DuplicateThisOnUpdate
  Optional<VentReportPeriod> findByApplicationVersion(ApplicationVersion applicationVersion);

  void deleteByApplicationVersion(ApplicationVersion applicationVersion);
}
