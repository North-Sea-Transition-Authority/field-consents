package uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport;

import java.util.Optional;
import javax.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentReportPeriodForm;

@Service
public class FlareReportPeriodService {

  private final FlareReportPeriodRepository flareReportPeriodRepository;

  private final FlareReportCleanupService flareReportCleanupService;

  @Autowired
  FlareReportPeriodService(FlareReportPeriodRepository flareReportPeriodRepository,
                           FlareReportCleanupService flareReportCleanupService) {
    this.flareReportPeriodRepository = flareReportPeriodRepository;
    this.flareReportCleanupService = flareReportCleanupService;
  }

  Optional<FlareReportPeriod> findFlareReportPeriod(ApplicationVersion applicationVersion) {
    return flareReportPeriodRepository.findByApplicationVersion(applicationVersion);
  }

  FlareVentReportPeriodForm getFlareReportPeriodForm(ApplicationVersion applicationVersion) {
    return findFlareReportPeriod(applicationVersion)
        .map(FlareVentReportPeriodForm::from)
        .orElseGet(FlareVentReportPeriodForm::new);
  }

  public boolean flareReportPeriodExists(ApplicationVersion applicationVersion) {
    return findFlareReportPeriod(applicationVersion).isPresent();
  }

  public FlareReportPeriod getFlareReportPeriodOrError(ApplicationVersion applicationVersion) {
    return findFlareReportPeriod(applicationVersion)
        .orElseThrow(() ->
            new EntityNotFoundException("Flare report period with application_version_id %s not found"
                .formatted(applicationVersion.getId()))
        );
  }

  @Transactional
  public void saveFlareReportPeriod(ApplicationVersion applicationVersion,
                                    FlareVentReportPeriodForm reportPeriodForm) {
    flareReportPeriodRepository.deleteByApplicationVersion(applicationVersion);
    var flareReportPeriod = FlareReportPeriod.from(applicationVersion, reportPeriodForm);
    flareReportPeriodRepository.save(flareReportPeriod);
    flareReportCleanupService.removeObsoleteReportDataOnPeriodSave(applicationVersion, flareReportPeriod);
  }
}
