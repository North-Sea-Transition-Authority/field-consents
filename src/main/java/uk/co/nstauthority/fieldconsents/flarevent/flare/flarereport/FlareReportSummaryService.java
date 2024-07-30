package uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.unit.ApplicationUnitService;
import uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionReportSummaryService;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;

@Service
public class FlareReportSummaryService {

  private final FlareReportService flareReportService;
  private final FlareReportPeriodService flareReportPeriodService;
  private final EmissionReportSummaryService emissionReportSummaryService;
  private final ApplicationUnitService applicationUnitService;

  FlareReportSummaryService(
      FlareReportService flareReportService,
      FlareReportPeriodService flareReportPeriodService,
      EmissionReportSummaryService emissionReportSummaryService,
      ApplicationUnitService applicationUnitService
  ) {
    this.flareReportService = flareReportService;
    this.flareReportPeriodService = flareReportPeriodService;
    this.emissionReportSummaryService = emissionReportSummaryService;
    this.applicationUnitService = applicationUnitService;
  }

  public List<SummaryCard> getFlareReportSummaryCards(ApplicationVersion applicationVersion) {
    var flareReportPeriodOptional = flareReportPeriodService.findFlareReportPeriod(applicationVersion);

    if (flareReportPeriodOptional.isEmpty()) {
      return SummaryCard.emptySummaryCardList();
    }

    var summaryCards = new ArrayList<SummaryCard>();

    summaryCards.add(emissionReportSummaryService.getReportPeriodSummaryCard(
        flareReportPeriodOptional.get(),
        applicationVersion.getApplication().getType()));

    var flareReportMonths = flareReportService.getFlareReportMonths(applicationVersion);

    if (flareReportMonths.isEmpty()) {
      return summaryCards;
    }

    var categoryUnit = applicationUnitService.getFlareCategoryUnit(applicationVersion);
    var averageUnit = applicationUnitService.getFlareAverageUnit(applicationVersion);

    emissionReportSummaryService.getEmissionsReportChartSummaryCard(applicationVersion).ifPresent(summaryCards::add);

    summaryCards.add(emissionReportSummaryService.getReportTableSummaryCard(flareReportMonths, categoryUnit, averageUnit));

    return summaryCards;
  }

}
