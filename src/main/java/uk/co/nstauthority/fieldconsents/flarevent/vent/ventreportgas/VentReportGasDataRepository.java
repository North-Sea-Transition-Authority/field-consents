package uk.co.nstauthority.fieldconsents.flarevent.vent.ventreportgas;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.duplication.DuplicateThisOnUpdate;
import uk.co.nstauthority.fieldconsents.application.duplication.DuplicationSource;

@Repository
public interface VentReportGasDataRepository extends CrudRepository<VentReportGasData, Integer>, DuplicationSource {

  @DuplicateThisOnUpdate
  Optional<VentReportGasData> findByApplicationVersion(ApplicationVersion applicationVersion);

  void deleteByApplicationVersion(ApplicationVersion applicationVersion);
}
