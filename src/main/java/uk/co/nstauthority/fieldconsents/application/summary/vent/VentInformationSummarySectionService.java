package uk.co.nstauthority.fieldconsents.application.summary.vent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.application.unit.ApplicationUnitService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.charts.EmissionsChartDataService;
import uk.co.nstauthority.fieldconsents.flarevent.EmissionCategoryType;
import uk.co.nstauthority.fieldconsents.flarevent.category123.vent.annual.VentAnnual123SummaryService;
import uk.co.nstauthority.fieldconsents.flarevent.category123.vent.shortterm.VentShortTerm123SummaryService;
import uk.co.nstauthority.fieldconsents.flarevent.category123.vent.ventreport.VentReport123SummaryService;
import uk.co.nstauthority.fieldconsents.flarevent.category123.vent.ventreportgas.VentReport123GasDataSummaryService;
import uk.co.nstauthority.fieldconsents.flarevent.vent.annual.VentAnnualService;
import uk.co.nstauthority.fieldconsents.flarevent.vent.longterm.VentLongTermSummaryService;
import uk.co.nstauthority.fieldconsents.flarevent.vent.shortterm.VentShortTermService;
import uk.co.nstauthority.fieldconsents.flarevent.vent.ventreport.VentReportSummaryService;
import uk.co.nstauthority.fieldconsents.flarevent.vent.ventreportgas.VentReportGasDataService;
import uk.co.nstauthority.fieldconsents.flarevent.vent.vents.VentSummaryService;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.summary.SummaryItem;
import uk.co.nstauthority.fieldconsents.summary.SummarySection;
import uk.co.nstauthority.fieldconsents.summary.SummarySectionService;

@Service
public class VentInformationSummarySectionService implements SummarySectionService<ApplicationVersion> {

  private final ConsentLengthService consentLengthService;
  private final ApplicationUnitService applicationUnitService;
  private final VentAnnualService ventAnnualService;
  private final VentShortTermService ventShortTermService;
  private final VentSummaryService ventSummaryService;
  private final VentReportGasDataService ventReportGasDataService;
  private final VentAnnual123SummaryService ventAnnual123SummaryService;
  private final VentShortTerm123SummaryService ventShortTerm123SummaryService;
  private final VentReport123SummaryService ventReport123SummaryService;
  private final VentReport123GasDataSummaryService ventReport123GasDataSummaryService;
  private final VentLongTermSummaryService ventLongTermSummaryService;
  private final EmissionsChartDataService emissionsChartDataService;
  private final VentReportSummaryService ventReportSummaryService;

  VentInformationSummarySectionService(
      ConsentLengthService consentLengthService,
      ApplicationUnitService applicationUnitService,
      VentAnnualService ventAnnualService,
      VentShortTermService ventShortTermService,
      VentSummaryService ventSummaryService,
      VentReportGasDataService ventReportGasDataService,
      VentAnnual123SummaryService ventAnnual123SummaryService,
      VentShortTerm123SummaryService ventShortTerm123SummaryService,
      VentReport123SummaryService ventReport123SummaryService,
      VentReport123GasDataSummaryService ventReport123GasDataSummaryService,
      VentLongTermSummaryService ventLongTermSummaryService,
      EmissionsChartDataService emissionsChartDataService,
      VentReportSummaryService ventReportSummaryService
  ) {
    this.consentLengthService = consentLengthService;
    this.applicationUnitService = applicationUnitService;
    this.ventAnnualService = ventAnnualService;
    this.ventShortTermService = ventShortTermService;
    this.ventSummaryService = ventSummaryService;
    this.ventReportGasDataService = ventReportGasDataService;
    this.ventAnnual123SummaryService = ventAnnual123SummaryService;
    this.ventShortTerm123SummaryService = ventShortTerm123SummaryService;
    this.ventReport123SummaryService = ventReport123SummaryService;
    this.ventReport123GasDataSummaryService = ventReport123GasDataSummaryService;
    this.ventLongTermSummaryService = ventLongTermSummaryService;
    this.emissionsChartDataService = emissionsChartDataService;
    this.ventReportSummaryService = ventReportSummaryService;
  }

  @Override
  public Optional<SummarySection> getSummarySection(ApplicationVersion applicationVersion, ServiceUserDetail user) {

    if (!ApplicationType.VENT.equals(applicationVersion.getApplication().getType())) {
      return Optional.empty();
    }

    var consentLengthDetailsOptional =
        consentLengthService.findConsentLengthDetails(applicationVersion);

    if (consentLengthDetailsOptional.isEmpty()) {
      return Optional.empty();
    }

    var consentLengthType = consentLengthDetailsOptional.get().getConsentLength();
    var emissionCategoryType = applicationUnitService.getEmissionCategoryType(applicationVersion);

    var summaryItems = new ArrayList<SummaryItem>();

    // vents and reports exist for
    // - all annual cases (category ABC and 123)
    // - only category ABC short term case
    // - no long term cases
    if (ConsentLengthType.ANNUAL == consentLengthType
        || (ConsentLengthType.SHORT_TERM == consentLengthType && EmissionCategoryType.CATEGORY_ABC == emissionCategoryType)) {
      summaryItems.add(getVentsSummaryItem(applicationVersion));
      summaryItems.add(getVentReportSummaryItem(applicationVersion, emissionCategoryType));
      summaryItems.add(getVentReportGasDataSummaryItem(applicationVersion, emissionCategoryType));
    }

    summaryItems.add(getVentConsentSummaryItem(applicationVersion, consentLengthType, emissionCategoryType));

    return Optional.of(new SummarySection(20, summaryItems));
  }

  private SummaryItem getVentsSummaryItem(ApplicationVersion applicationVersion) {
    return SummaryItem.withCards("Vents",
        ventSummaryService.getSummariesForVents(applicationVersion)
    );
  }

  private SummaryItem getVentReportSummaryItem(ApplicationVersion applicationVersion,
                                               EmissionCategoryType emissionCategoryType) {
    return SummaryItem.withCards("Vent report",
        switch (emissionCategoryType) {
          case CATEGORY_123 -> List.of(ventReport123SummaryService.getVentReport123SummaryCard(applicationVersion));
          case CATEGORY_ABC -> ventReportSummaryService.getVentReportSummaryCards(applicationVersion);
          case LEGACY_LONG_TERM -> throw emissionCategoryType.unsupportedOperation();
        }
    );
  }

  private SummaryItem getVentReportGasDataSummaryItem(ApplicationVersion applicationVersion,
                                                      EmissionCategoryType emissionCategoryType) {
    return SummaryItem.withCards("Vent report gas properties",
        switch (emissionCategoryType) {
          case CATEGORY_123 ->
              List.of(ventReport123GasDataSummaryService.getVentReport123GasDataSummaryCard(applicationVersion));
          case CATEGORY_ABC -> ventReportGasDataService.getVentReportGasDataSummaryCards(applicationVersion);
          case LEGACY_LONG_TERM -> throw emissionCategoryType.unsupportedOperation();
        }
    );
  }

  private SummaryItem getVentConsentSummaryItem(
      ApplicationVersion applicationVersion,
      ConsentLengthType consentLengthType,
      EmissionCategoryType emissionCategoryType
  ) {
    var summaryCards = new ArrayList<SummaryCard>();

    emissionsChartDataService.getConsentChartData(applicationVersion)
        .map(SummaryCard::stackedBarChartSummaryCard)
        .ifPresent(summaryCards::add);

    var tableCard = switch (consentLengthType) {
      case SHORT_TERM -> switch (emissionCategoryType) {
        case CATEGORY_123 -> ventShortTerm123SummaryService.getVentShortTerm123SummaryCard(applicationVersion);
        case CATEGORY_ABC -> ventShortTermService.getVentShortTermSummaryCard(applicationVersion);
        case LEGACY_LONG_TERM -> throw emissionCategoryType.unsupportedOperation();
      };
      case ANNUAL -> switch (emissionCategoryType) {
        case CATEGORY_123 -> ventAnnual123SummaryService.getVentAnnual123SummaryCard(applicationVersion);
        case CATEGORY_ABC -> ventAnnualService.getVentAnnualSummaryCard(applicationVersion);
        case LEGACY_LONG_TERM -> throw emissionCategoryType.unsupportedOperation();
      };
      case LONG_TERM -> switch (emissionCategoryType) {
        case CATEGORY_ABC, CATEGORY_123 -> throw emissionCategoryType.unsupportedOperation();
        case LEGACY_LONG_TERM -> ventLongTermSummaryService.getVentLongTermSummaryCard(applicationVersion);
      };
    };
    summaryCards.add(tableCard);

    return SummaryItem.withCards(consentLengthType.getDisplayName(), summaryCards);
  }
}
