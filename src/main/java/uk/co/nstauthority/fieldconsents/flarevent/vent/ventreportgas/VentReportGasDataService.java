package uk.co.nstauthority.fieldconsents.flarevent.vent.ventreportgas;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentReportGasDataForm;

@Service
public class VentReportGasDataService {

  private final VentReportGasDataRepository ventReportGasDataRepository;

  @Autowired
  public VentReportGasDataService(VentReportGasDataRepository ventReportGasDataRepository) {
    this.ventReportGasDataRepository = ventReportGasDataRepository;
  }

  public Optional<VentReportGasData> findVentReportGasData(ApplicationVersion applicationVersion) {
    return ventReportGasDataRepository.findByApplicationVersion(applicationVersion);
  }

  public FlareVentReportGasDataForm getFlareVentReportGasDataForm(ApplicationVersion applicationVersion) {
    return findVentReportGasData(applicationVersion)
        .map(FlareVentReportGasDataForm::from)
        .orElseGet(FlareVentReportGasDataForm::new);
  }

  @Transactional
  public void saveVentReportGasData(ApplicationVersion applicationVersion,
                                    FlareVentReportGasDataForm form) {
    ventReportGasDataRepository.deleteByApplicationVersion(applicationVersion);
    ventReportGasDataRepository.save(VentReportGasData.from(applicationVersion, form));
  }
}
