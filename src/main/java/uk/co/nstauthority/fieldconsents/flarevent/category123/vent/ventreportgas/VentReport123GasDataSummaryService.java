package uk.co.nstauthority.fieldconsents.flarevent.category123.vent.ventreportgas;

import static uk.co.nstauthority.fieldconsents.flarevent.category123.vent.summary.Vent123SummaryUtil.CATEGORY_1_HEADING;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionReportGasDataSummaryService.HYDROCARBON_CONTENT_PROMPT_WITH_UNIT;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionReportGasDataSummaryService.INERT_GAS_CONTENT_PROMPT_WITH_UNIT;
import static uk.co.nstauthority.fieldconsents.formatting.DateUtils.LONG_MONTH_YEAR;

import java.util.Optional;
import java.util.function.UnaryOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.unit.ApplicationUnitService;
import uk.co.nstauthority.fieldconsents.flarevent.category123.vent.ventreport.VentReport123Service;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.summary.SummaryTableView;

@Service
public class VentReport123GasDataSummaryService {

  private static final UnaryOperator<String> DENSITY_PROMPT_WITH_UNIT = "Stream mol wt (%s)"::formatted;

  private final VentReport123GasDataRepository ventReport123GasDataRepository;
  private final VentReport123Service ventReport123Service;
  private final ApplicationUnitService applicationUnitService;

  @Autowired
  public VentReport123GasDataSummaryService(VentReport123GasDataRepository ventReport123GasDataRepository,
                                            VentReport123Service ventReport123Service,
                                            ApplicationUnitService applicationUnitService) {
    this.ventReport123GasDataRepository = ventReport123GasDataRepository;
    this.ventReport123Service = ventReport123Service;
    this.applicationUnitService = applicationUnitService;
  }

  private Optional<VentReport123GasData> findVentReport123GasData(ApplicationVersion applicationVersion) {
    return ventReport123GasDataRepository.findByApplicationVersion(applicationVersion);
  }

  public SummaryCard getVentReport123GasDataSummaryCard(ApplicationVersion applicationVersion) {

    var ventReport123GasDataOptional = findVentReport123GasData(applicationVersion);

    if (ventReport123GasDataOptional.isEmpty()) {
      return SummaryCard.emptySummaryCard();
    }

    var reportGasData = ventReport123GasDataOptional.get();
    var densityUnit = applicationUnitService.getVentGasDensityUnit(applicationVersion);
    var gasContentUnit = applicationUnitService.getVentGasContentUnit(applicationVersion);

    var summaryTable = SummaryTableView
        .newWithHeading(
            null,
            CATEGORY_1_HEADING
        )
        .addRow(
            DENSITY_PROMPT_WITH_UNIT.apply(densityUnit.getDisplayName()),
            reportGasData.getCategory1Density()
        )
        .addRow(
            INERT_GAS_CONTENT_PROMPT_WITH_UNIT.apply(gasContentUnit.getDisplayName()),
            reportGasData.getCategory1InertGasPercentage()
        )
        .addRow(
            HYDROCARBON_CONTENT_PROMPT_WITH_UNIT.apply(gasContentUnit.getDisplayName()),
            reportGasData.getCategory1HydrocarbonPercentage()
        );

    var reportStart = ventReport123Service.getStartYearMonth(applicationVersion);
    var reportEnd = ventReport123Service.getEndYearMonth(applicationVersion);
    return SummaryCard.tableSummaryCardWithHeading(
        DateUtils.format(reportStart, LONG_MONTH_YEAR) + " to " +
            DateUtils.format(reportEnd, LONG_MONTH_YEAR),
        summaryTable);
  }
}
