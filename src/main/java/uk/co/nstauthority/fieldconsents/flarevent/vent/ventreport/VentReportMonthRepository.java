package uk.co.nstauthority.fieldconsents.flarevent.vent.ventreport;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

@Repository
public interface VentReportMonthRepository extends CrudRepository<VentReportMonth, Integer> {

  List<VentReportMonth> findAllByApplicationVersion(ApplicationVersion applicationVersion);

  void deleteAllByApplicationVersion(ApplicationVersion applicationVersion);
}
