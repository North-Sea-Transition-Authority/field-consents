package uk.co.nstauthority.fieldconsents.flarevent.vent.ventreport;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.unit.ApplicationUnitService;
import uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionReportSummaryService;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;

@Service
public class VentReportSummaryService {

  private final VentReportService ventReportService;
  private final VentReportPeriodService ventReportPeriodService;
  private final EmissionReportSummaryService emissionReportSummaryService;
  private final ApplicationUnitService applicationUnitService;

  VentReportSummaryService(
      VentReportService ventReportService,
      VentReportPeriodService ventReportPeriodService,
      EmissionReportSummaryService emissionReportSummaryService,
      ApplicationUnitService applicationUnitService
  ) {
    this.ventReportService = ventReportService;
    this.ventReportPeriodService = ventReportPeriodService;
    this.emissionReportSummaryService = emissionReportSummaryService;
    this.applicationUnitService = applicationUnitService;
  }

  public List<SummaryCard> getVentReportSummaryCards(ApplicationVersion applicationVersion) {

    var ventReportPeriodOptional = ventReportPeriodService.findVentReportPeriod(applicationVersion);

    if (ventReportPeriodOptional.isEmpty()) {
      return SummaryCard.emptySummaryCardList();
    }

    var summaryCards = new ArrayList<SummaryCard>();

    summaryCards.add(emissionReportSummaryService.getReportPeriodSummaryCard(
        ventReportPeriodOptional.get(),
        applicationVersion.getApplication().getType()));

    var ventReportMonths = ventReportService.getVentReportMonths(applicationVersion);

    if (ventReportMonths.isEmpty()) {
      return summaryCards;
    }

    var categoryUnit = applicationUnitService.getVentCategoryUnit(applicationVersion);
    var averageUnit = applicationUnitService.getVentAverageUnit(applicationVersion);

    emissionReportSummaryService.getEmissionsReportChartSummaryCard(applicationVersion).ifPresent(summaryCards::add);

    summaryCards.add(emissionReportSummaryService.getReportTableSummaryCard(ventReportMonths, categoryUnit, averageUnit));

    return summaryCards;
  }

}
