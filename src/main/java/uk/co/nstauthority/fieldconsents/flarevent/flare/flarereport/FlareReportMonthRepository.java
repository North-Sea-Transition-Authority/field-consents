package uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.duplication.DuplicateThisOnUpdate;
import uk.co.nstauthority.fieldconsents.application.duplication.DuplicationSource;

@Repository
public interface FlareReportMonthRepository extends CrudRepository<FlareReportMonth, Integer>, DuplicationSource {

  @DuplicateThisOnUpdate
  List<FlareReportMonth> findAllByApplicationVersion(ApplicationVersion applicationVersion);

  void deleteAllByApplicationVersion(ApplicationVersion applicationVersion);
}
