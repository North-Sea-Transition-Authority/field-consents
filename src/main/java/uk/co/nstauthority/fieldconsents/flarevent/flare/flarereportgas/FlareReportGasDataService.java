package uk.co.nstauthority.fieldconsents.flarevent.flare.flarereportgas;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentReportGasDataForm;

@Service
public class FlareReportGasDataService {

  private final FlareReportGasDataRepository flareReportGasDataRepository;

  @Autowired
  public FlareReportGasDataService(FlareReportGasDataRepository flareReportGasDataRepository) {
    this.flareReportGasDataRepository = flareReportGasDataRepository;
  }

  public Optional<FlareReportGasData> findFlareReportGasData(ApplicationVersion applicationVersion) {
    return flareReportGasDataRepository.findByApplicationVersion(applicationVersion);
  }

  public FlareVentReportGasDataForm getFlareVentReportGasDataForm(ApplicationVersion applicationVersion) {
    return findFlareReportGasData(applicationVersion)
        .map(FlareVentReportGasDataForm::from)
        .orElseGet(FlareVentReportGasDataForm::new);
  }

  @Transactional
  public void saveFlareReportGasData(ApplicationVersion applicationVersion,
                                     FlareVentReportGasDataForm form) {
    flareReportGasDataRepository.deleteByApplicationVersion(applicationVersion);
    flareReportGasDataRepository.save(FlareReportGasData.from(applicationVersion, form));
  }
}
