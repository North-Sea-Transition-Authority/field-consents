package uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.duplication.DuplicateThisOnUpdate;
import uk.co.nstauthority.fieldconsents.application.duplication.DuplicationSource;

@Repository
public interface FlareReportPeriodRepository extends CrudRepository<FlareReportPeriod, Integer>, DuplicationSource {

  @DuplicateThisOnUpdate
  Optional<FlareReportPeriod> findByApplicationVersion(ApplicationVersion applicationVersion);

  void deleteByApplicationVersion(ApplicationVersion applicationVersion);
}
