package uk.co.nstauthority.fieldconsents.flarevent.flare.flarereportgas;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.unit.ApplicationUnitService;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentReportGasDataForm;
import uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport.FlareReportPeriodService;
import uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionReportGasDataSummaryService;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;

@Service
public class FlareReportGasDataService {

  private final FlareReportGasDataRepository flareReportGasDataRepository;

  private final ApplicationUnitService applicationUnitService;

  private final FlareReportPeriodService flareReportPeriodService;

  private final EmissionReportGasDataSummaryService emissionReportGasDataSummaryService;

  @Autowired
  public FlareReportGasDataService(FlareReportGasDataRepository flareReportGasDataRepository,
                                   ApplicationUnitService applicationUnitService,
                                   FlareReportPeriodService flareReportPeriodService,
                                   EmissionReportGasDataSummaryService emissionReportGasDataSummaryService) {
    this.flareReportGasDataRepository = flareReportGasDataRepository;
    this.applicationUnitService = applicationUnitService;
    this.flareReportPeriodService = flareReportPeriodService;
    this.emissionReportGasDataSummaryService = emissionReportGasDataSummaryService;
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

  public List<SummaryCard> getFlareReportGasDataSummaryCards(ApplicationVersion applicationVersion) {

    var flareReportGasDataOptional = findFlareReportGasData(applicationVersion);

    if (flareReportGasDataOptional.isEmpty()) {
      return SummaryCard.emptySummaryCardList();
    }

    var flareReportGasData = flareReportGasDataOptional.get();
    var densityUnit = applicationUnitService.getFlareGasDensityUnit(applicationVersion);
    var gasContentUnit = applicationUnitService.getFlareGasContentUnit(applicationVersion);
    var flareReportPeriod = flareReportPeriodService.getFlareReportPeriodOrError(applicationVersion);

    var summaryCards = new ArrayList<SummaryCard>();

    summaryCards.add(emissionReportGasDataSummaryService.getReportGasDataTableSummaryCard(
        flareReportGasData, flareReportPeriod, densityUnit, gasContentUnit));

    summaryCards.add(emissionReportGasDataSummaryService.getReportGasDataJustificationSummaryCard(flareReportGasData));

    return summaryCards;
  }
}
