package uk.co.nstauthority.fieldconsents.flarevent.category123.flare.flarereportgas;

import static uk.co.nstauthority.fieldconsents.flarevent.category123.flare.summary.Flare123SummaryUtil.CATEGORY_1_HEADING;
import static uk.co.nstauthority.fieldconsents.flarevent.category123.flare.summary.Flare123SummaryUtil.CATEGORY_2_HEADING;
import static uk.co.nstauthority.fieldconsents.flarevent.category123.flare.summary.Flare123SummaryUtil.CATEGORY_3_HEADING;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionReportGasDataSummaryService.HYDROCARBON_CONTENT_PROMPT_WITH_UNIT;
import static uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionReportGasDataSummaryService.INERT_GAS_CONTENT_PROMPT_WITH_UNIT;
import static uk.co.nstauthority.fieldconsents.formatting.DateUtils.LONG_MONTH_YEAR;

import java.util.Optional;
import java.util.function.UnaryOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.unit.ApplicationUnitService;
import uk.co.nstauthority.fieldconsents.flarevent.category123.flare.flarereport.FlareReport123Service;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.summary.SummaryTableView;

@Service
public class FlareReport123GasDataSummaryService {

  private static final UnaryOperator<String> DENSITY_PROMPT_WITH_UNIT = "Stream mol wt (%s)"::formatted;

  private final FlareReport123GasDataRepository flareReport123GasDataRepository;
  private final FlareReport123Service flareReport123Service;
  private final ApplicationUnitService applicationUnitService;

  @Autowired
  public FlareReport123GasDataSummaryService(FlareReport123GasDataRepository flareReport123GasDataRepository,
                                             FlareReport123Service flareReport123Service,
                                             ApplicationUnitService applicationUnitService) {
    this.flareReport123GasDataRepository = flareReport123GasDataRepository;
    this.flareReport123Service = flareReport123Service;
    this.applicationUnitService = applicationUnitService;
  }

  private Optional<FlareReport123GasData> findFlareReport123GasData(ApplicationVersion applicationVersion) {
    return flareReport123GasDataRepository.findByApplicationVersion(applicationVersion);
  }

  public SummaryCard getFlareReport123GasDataSummaryCard(ApplicationVersion applicationVersion) {

    var flareReport123GasDataOptional = findFlareReport123GasData(applicationVersion);

    if (flareReport123GasDataOptional.isEmpty()) {
      return SummaryCard.emptySummaryCard();
    }

    var reportGasData = flareReport123GasDataOptional.get();
    var densityUnit = applicationUnitService.getFlareGasDensityUnit(applicationVersion);
    var gasContentUnit = applicationUnitService.getFlareGasContentUnit(applicationVersion);

    var summaryTable = SummaryTableView
        .newWithHeading(
            null,
            CATEGORY_1_HEADING,
            CATEGORY_2_HEADING,
            CATEGORY_3_HEADING
        )
        .addRow(
            DENSITY_PROMPT_WITH_UNIT.apply(densityUnit.getDisplayName()),
            reportGasData.getCategory1Density(),
            reportGasData.getCategory2Density(),
            reportGasData.getCategory3Density()
        )
        .addRow(
            INERT_GAS_CONTENT_PROMPT_WITH_UNIT.apply(gasContentUnit.getDisplayName()),
            reportGasData.getCategory1InertGasPercentage(),
            reportGasData.getCategory2InertGasPercentage(),
            reportGasData.getCategory3InertGasPercentage()
        )
        .addRow(
            HYDROCARBON_CONTENT_PROMPT_WITH_UNIT.apply(gasContentUnit.getDisplayName()),
            reportGasData.getCategory1HydrocarbonPercentage(),
            reportGasData.getCategory2HydrocarbonPercentage(),
            reportGasData.getCategory3HydrocarbonPercentage()
        );

    var reportStart = flareReport123Service.getStartYearMonth(applicationVersion);
    var reportEnd = flareReport123Service.getEndYearMonth(applicationVersion);
    return SummaryCard.tableSummaryCardWithHeading(
        DateUtils.format(reportStart, LONG_MONTH_YEAR) + " to " +
            DateUtils.format(reportEnd, LONG_MONTH_YEAR),
        summaryTable);
  }
}
