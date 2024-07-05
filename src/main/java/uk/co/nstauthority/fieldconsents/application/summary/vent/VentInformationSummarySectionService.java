package uk.co.nstauthority.fieldconsents.application.summary.vent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.application.unit.ApplicationUnitService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.flarevent.EmissionCategoryType;
import uk.co.nstauthority.fieldconsents.flarevent.category123.vent.annual.VentAnnual123SummaryService;
import uk.co.nstauthority.fieldconsents.flarevent.category123.vent.shortterm.VentShortTerm123SummaryService;
import uk.co.nstauthority.fieldconsents.flarevent.category123.vent.ventreport.VentReport123SummaryService;
import uk.co.nstauthority.fieldconsents.flarevent.category123.vent.ventreportgas.VentReport123GasDataSummaryService;
import uk.co.nstauthority.fieldconsents.flarevent.vent.annual.VentAnnualService;
import uk.co.nstauthority.fieldconsents.flarevent.vent.longterm.VentLongTermSummaryService;
import uk.co.nstauthority.fieldconsents.flarevent.vent.shortterm.VentShortTermService;
import uk.co.nstauthority.fieldconsents.flarevent.vent.ventreport.VentReportService;
import uk.co.nstauthority.fieldconsents.flarevent.vent.ventreportgas.VentReportGasDataService;
import uk.co.nstauthority.fieldconsents.flarevent.vent.vents.VentSummaryService;
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
  private final VentReportService ventReportService;
  private final VentReportGasDataService ventReportGasDataService;
  private final VentAnnual123SummaryService ventAnnual123SummaryService;
  private final VentShortTerm123SummaryService ventShortTerm123SummaryService;
  private final VentReport123SummaryService ventReport123SummaryService;
  private final VentReport123GasDataSummaryService ventReport123GasDataSummaryService;
  private final VentLongTermSummaryService ventLongTermSummaryService;

  @Autowired
  public VentInformationSummarySectionService(ConsentLengthService consentLengthService,
                                              ApplicationUnitService applicationUnitService,
                                              VentAnnualService ventAnnualService,
                                              VentShortTermService ventShortTermService,
                                              VentSummaryService ventSummaryService,
                                              VentReportService ventReportService,
                                              VentReportGasDataService ventReportGasDataService,
                                              VentAnnual123SummaryService ventAnnual123SummaryService,
                                              VentShortTerm123SummaryService ventShortTerm123SummaryService,
                                              VentReport123SummaryService ventReport123SummaryService,
                                              VentReport123GasDataSummaryService ventReport123GasDataSummaryService,
                                              VentLongTermSummaryService ventLongTermSummaryService) {
    this.consentLengthService = consentLengthService;
    this.applicationUnitService = applicationUnitService;
    this.ventAnnualService = ventAnnualService;
    this.ventShortTermService = ventShortTermService;
    this.ventSummaryService = ventSummaryService;
    this.ventReportService = ventReportService;
    this.ventReportGasDataService = ventReportGasDataService;
    this.ventAnnual123SummaryService = ventAnnual123SummaryService;
    this.ventShortTerm123SummaryService = ventShortTerm123SummaryService;
    this.ventReport123SummaryService = ventReport123SummaryService;
    this.ventReport123GasDataSummaryService = ventReport123GasDataSummaryService;
    this.ventLongTermSummaryService = ventLongTermSummaryService;
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
    if (ConsentLengthType.ANNUAL.equals(consentLengthType)
        || (ConsentLengthType.SHORT_TERM.equals(consentLengthType)
        && EmissionCategoryType.CATEGORY_ABC.equals(emissionCategoryType))) {
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
          case CATEGORY_ABC -> ventReportService.getVentReportSummaryCards(applicationVersion);
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

  private SummaryItem getVentConsentSummaryItem(ApplicationVersion applicationVersion,
                                                ConsentLengthType consentLengthType,
                                                EmissionCategoryType emissionCategoryType) {

    return switch (consentLengthType) {
      case SHORT_TERM ->
          SummaryItem.withCard(consentLengthType.getDisplayName(),
              switch (emissionCategoryType) {
                case CATEGORY_123 -> ventShortTerm123SummaryService.getVentShortTerm123SummaryCard(applicationVersion);
                case CATEGORY_ABC -> ventShortTermService.getVentShortTermSummaryCard(applicationVersion);
                case LEGACY_LONG_TERM -> throw emissionCategoryType.unsupportedOperation();
              }
          );
      case ANNUAL ->
          SummaryItem.withCard(consentLengthType.getDisplayName(),
              switch (emissionCategoryType) {
                case CATEGORY_123 -> ventAnnual123SummaryService.getVentAnnual123SummaryCard(applicationVersion);
                case CATEGORY_ABC -> ventAnnualService.getVentAnnualSummaryCard(applicationVersion);
                case LEGACY_LONG_TERM -> throw emissionCategoryType.unsupportedOperation();
              }
          );
      case LONG_TERM -> switch (emissionCategoryType) {
        case LEGACY_LONG_TERM ->
            SummaryItem.withCard(consentLengthType.getDisplayName(),
                ventLongTermSummaryService.getVentLongTermSummaryCard(applicationVersion)
            );
        case CATEGORY_123, CATEGORY_ABC -> throw emissionCategoryType.unsupportedOperation();
      };
    };
  }
}
