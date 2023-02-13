package uk.co.nstauthority.fieldconsents.flarevent.vent.ventreport;

import java.util.Optional;
import javax.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentReportPeriodForm;

@Service
public class VentReportPeriodService {

  private final VentReportPeriodRepository ventReportPeriodRepository;

  private final VentReportCleanupService ventReportCleanupService;

  @Autowired
  VentReportPeriodService(VentReportPeriodRepository flareReportPeriodRepository,
                          VentReportCleanupService ventReportCleanupService) {
    this.ventReportPeriodRepository = flareReportPeriodRepository;
    this.ventReportCleanupService = ventReportCleanupService;
  }

  Optional<VentReportPeriod> findVentReportPeriod(ApplicationVersion applicationVersion) {
    return ventReportPeriodRepository.findByApplicationVersion(applicationVersion);
  }

  FlareVentReportPeriodForm getVentReportPeriodForm(ApplicationVersion applicationVersion) {
    return findVentReportPeriod(applicationVersion)
        .map(FlareVentReportPeriodForm::from)
        .orElseGet(FlareVentReportPeriodForm::new);
  }

  public boolean ventReportPeriodExists(ApplicationVersion applicationVersion) {
    return findVentReportPeriod(applicationVersion).isPresent();
  }

  public VentReportPeriod getVentReportPeriodOrError(ApplicationVersion applicationVersion) {
    return findVentReportPeriod(applicationVersion)
        .orElseThrow(() ->
            new EntityNotFoundException("Vent report period with application_version_id %s not found"
                .formatted(applicationVersion.getId()))
        );
  }

  @Transactional
  public void saveVentReportPeriod(ApplicationVersion applicationVersion,
                                   FlareVentReportPeriodForm reportPeriodForm) {
    ventReportPeriodRepository.deleteByApplicationVersion(applicationVersion);
    var ventReportPeriod = VentReportPeriod.from(applicationVersion, reportPeriodForm);
    ventReportPeriodRepository.save(ventReportPeriod);
    ventReportCleanupService.removeObsoleteReportDataOnPeriodSave(applicationVersion, ventReportPeriod);
  }
}
