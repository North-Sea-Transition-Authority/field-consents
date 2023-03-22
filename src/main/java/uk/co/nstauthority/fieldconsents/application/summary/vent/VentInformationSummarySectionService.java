package uk.co.nstauthority.fieldconsents.application.summary.vent;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthDetails;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.flarevent.vent.annual.VentAnnualService;
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

  private final VentAnnualService ventAnnualService;

  private final VentShortTermService ventShortTermService;

  private final VentSummaryService ventSummaryService;

  private final VentReportService ventReportService;

  private final VentReportGasDataService ventReportGasDataService;

  @Autowired
  public VentInformationSummarySectionService(ConsentLengthService consentLengthService,
                                              VentAnnualService ventAnnualService,
                                              VentShortTermService ventShortTermService,
                                              VentSummaryService ventSummaryService,
                                              VentReportService ventReportService,
                                              VentReportGasDataService ventReportGasDataService) {
    this.consentLengthService = consentLengthService;
    this.ventAnnualService = ventAnnualService;
    this.ventShortTermService = ventShortTermService;
    this.ventSummaryService = ventSummaryService;
    this.ventReportService = ventReportService;
    this.ventReportGasDataService = ventReportGasDataService;
  }

  @Override
  public Optional<SummarySection> getSummarySection(ApplicationVersion applicationVersion) {

    if (!ApplicationType.VENT.equals(applicationVersion.getApplication().getType())) {
      return Optional.empty();
    }

    var consentLengthDetailsOptional =
        consentLengthService.findConsentLengthDetails(applicationVersion);

    if (consentLengthDetailsOptional.isEmpty()) {
      return Optional.empty();
    }

    ConsentLengthDetails consentLengthDetails = consentLengthDetailsOptional.get();

    var summaryItems = List.of(
        getVentsSummaryItem(applicationVersion),
        getVentReportSummaryItem(applicationVersion),
        getVentReportGasDataSummaryItem(applicationVersion),
        getVentConsentSummaryItem(applicationVersion, consentLengthDetails)
    );

    return Optional.of(new SummarySection(20, summaryItems));
  }

  private SummaryItem getVentsSummaryItem(ApplicationVersion applicationVersion) {
    return SummaryItem.withCards("Vents",
        ventSummaryService.getSummariesForVents(applicationVersion)
    );
  }

  private SummaryItem getVentReportSummaryItem(ApplicationVersion applicationVersion) {
    return SummaryItem.withCards("Vent report",
        ventReportService.getVentReportSummaryCards(applicationVersion)
    );
  }


  private SummaryItem getVentReportGasDataSummaryItem(ApplicationVersion applicationVersion) {
    return SummaryItem.withCards("Vent report gas properties",
        ventReportGasDataService.getVentReportGasDataSummaryCards(applicationVersion)
    );
  }

  private SummaryItem getVentConsentSummaryItem(ApplicationVersion applicationVersion,
                                                ConsentLengthDetails consentLengthDetails) {

    var consentLengthType = consentLengthDetails.getConsentLength();

    return switch (consentLengthType) {
      case SHORT_TERM ->
          SummaryItem.withCard(consentLengthType.getDisplayName(),
              ventShortTermService.getVentShortTermSummaryCard(applicationVersion)
          );
      case ANNUAL ->
          SummaryItem.withCard(consentLengthType.getDisplayName(),
              ventAnnualService.getVentAnnualSummaryCard(applicationVersion)
          );
      default ->
          throw new RuntimeException("Incorrect consent length type: " + consentLengthType);
    };
  }
}
