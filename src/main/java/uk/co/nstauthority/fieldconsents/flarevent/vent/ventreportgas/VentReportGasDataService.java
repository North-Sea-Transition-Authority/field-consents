package uk.co.nstauthority.fieldconsents.flarevent.vent.ventreportgas;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.unit.ApplicationUnitService;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentReportGasDataForm;
import uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionReportGasDataSummaryService;
import uk.co.nstauthority.fieldconsents.flarevent.vent.ventreport.VentReportPeriodService;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;

@Service
public class VentReportGasDataService {

  private final VentReportGasDataRepository ventReportGasDataRepository;

  private final ApplicationUnitService applicationUnitService;

  private final VentReportPeriodService ventReportPeriodService;

  private final EmissionReportGasDataSummaryService emissionReportGasDataSummaryService;

  @Autowired
  public VentReportGasDataService(VentReportGasDataRepository ventReportGasDataRepository,
                                  ApplicationUnitService applicationUnitService,
                                  VentReportPeriodService ventReportPeriodService,
                                  EmissionReportGasDataSummaryService emissionReportGasDataSummaryService) {
    this.ventReportGasDataRepository = ventReportGasDataRepository;
    this.applicationUnitService = applicationUnitService;
    this.ventReportPeriodService = ventReportPeriodService;
    this.emissionReportGasDataSummaryService = emissionReportGasDataSummaryService;
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

  public List<SummaryCard> getVentReportGasDataSummaryCards(ApplicationVersion applicationVersion) {

    var ventReportGasDataOptional = findVentReportGasData(applicationVersion);

    if (ventReportGasDataOptional.isEmpty()) {
      return SummaryCard.emptySummaryCardList();
    }

    var ventReportGasData = ventReportGasDataOptional.get();
    var densityUnit = applicationUnitService.getVentGasDensityUnit(applicationVersion);
    var gasContentUnit = applicationUnitService.getVentGasContentUnit(applicationVersion);
    var ventReportPeriod = ventReportPeriodService.getVentReportPeriodOrError(applicationVersion);

    var summaryCards = new ArrayList<SummaryCard>();

    summaryCards.add(emissionReportGasDataSummaryService.getReportGasDataTableSummaryCard(
        ventReportGasData, ventReportPeriod, densityUnit, gasContentUnit));

    summaryCards.add(emissionReportGasDataSummaryService.getReportGasDataJustificationSummaryCard(ventReportGasData));

    return summaryCards;
  }
}
