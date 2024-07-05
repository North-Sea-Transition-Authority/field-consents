package uk.co.nstauthority.fieldconsents.application.summary.flare;

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
import uk.co.nstauthority.fieldconsents.flarevent.category123.flare.annual.FlareAnnual123SummaryService;
import uk.co.nstauthority.fieldconsents.flarevent.category123.flare.flarereport.FlareReport123SummaryService;
import uk.co.nstauthority.fieldconsents.flarevent.category123.flare.flarereportgas.FlareReport123GasDataSummaryService;
import uk.co.nstauthority.fieldconsents.flarevent.category123.flare.shortterm.FlareShortTerm123SummaryService;
import uk.co.nstauthority.fieldconsents.flarevent.flare.annual.FlareAnnualService;
import uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport.FlareReportService;
import uk.co.nstauthority.fieldconsents.flarevent.flare.flarereportgas.FlareReportGasDataService;
import uk.co.nstauthority.fieldconsents.flarevent.flare.flares.FlareSummaryService;
import uk.co.nstauthority.fieldconsents.flarevent.flare.longterm.FlareLongTermSummaryService;
import uk.co.nstauthority.fieldconsents.flarevent.flare.shortterm.FlareShortTermService;
import uk.co.nstauthority.fieldconsents.summary.SummaryItem;
import uk.co.nstauthority.fieldconsents.summary.SummarySection;
import uk.co.nstauthority.fieldconsents.summary.SummarySectionService;

@Service
public class FlareInformationSummarySectionService implements SummarySectionService<ApplicationVersion> {
  private final ConsentLengthService consentLengthService;
  private final ApplicationUnitService applicationUnitService;
  private final FlareAnnualService flareAnnualService;
  private final FlareShortTermService flareShortTermService;
  private final FlareSummaryService flareSummaryService;
  private final FlareReportService flareReportService;
  private final FlareReportGasDataService flareReportGasDataService;
  private final FlareAnnual123SummaryService flareAnnual123SummaryService;
  private final FlareShortTerm123SummaryService flareShortTerm123SummaryService;
  private final FlareReport123SummaryService flareReport123SummaryService;
  private final FlareReport123GasDataSummaryService flareReport123GasDataSummaryService;
  private final FlareLongTermSummaryService flareLongTermSummaryService;

  @Autowired
  public FlareInformationSummarySectionService(ConsentLengthService consentLengthService,
                                               ApplicationUnitService applicationUnitService,
                                               FlareAnnualService flareAnnualService,
                                               FlareShortTermService flareShortTermService,
                                               FlareSummaryService flareSummaryService,
                                               FlareReportService flareReportService,
                                               FlareReportGasDataService flareReportGasDataService,
                                               FlareAnnual123SummaryService flareAnnual123SummaryService,
                                               FlareShortTerm123SummaryService flareShortTerm123SummaryService,
                                               FlareReport123SummaryService flareReport123SummaryService,
                                               FlareReport123GasDataSummaryService flareReport123GasDataSummaryService,
                                               FlareLongTermSummaryService flareLongTermSummaryService) {
    this.consentLengthService = consentLengthService;
    this.applicationUnitService = applicationUnitService;
    this.flareAnnualService = flareAnnualService;
    this.flareShortTermService = flareShortTermService;
    this.flareSummaryService = flareSummaryService;
    this.flareReportService = flareReportService;
    this.flareReportGasDataService = flareReportGasDataService;
    this.flareAnnual123SummaryService = flareAnnual123SummaryService;
    this.flareShortTerm123SummaryService = flareShortTerm123SummaryService;
    this.flareReport123SummaryService = flareReport123SummaryService;
    this.flareReport123GasDataSummaryService = flareReport123GasDataSummaryService;
    this.flareLongTermSummaryService = flareLongTermSummaryService;
  }

  @Override
  public Optional<SummarySection> getSummarySection(ApplicationVersion applicationVersion, ServiceUserDetail user) {

    if (!ApplicationType.FLARE.equals(applicationVersion.getApplication().getType())) {
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

    // flares and reports exist for
    // - all annual cases (category ABC and 123)
    // - only category ABC short term case
    // - no long term cases
    if (ConsentLengthType.ANNUAL.equals(consentLengthType)
        || (ConsentLengthType.SHORT_TERM.equals(consentLengthType)
            && EmissionCategoryType.CATEGORY_ABC.equals(emissionCategoryType))) {
      summaryItems.add(getFlaresSummaryItem(applicationVersion));
      summaryItems.add(getFlareReportSummaryItem(applicationVersion, emissionCategoryType));
      summaryItems.add(getFlareReportGasDataSummaryItem(applicationVersion, emissionCategoryType));
    }

    summaryItems.add(getFlareConsentSummaryItem(applicationVersion, consentLengthType, emissionCategoryType));

    return Optional.of(new SummarySection(20, summaryItems));
  }

  private SummaryItem getFlaresSummaryItem(ApplicationVersion applicationVersion) {
    return SummaryItem.withCards("Flares",
        flareSummaryService.getSummariesForFlares(applicationVersion)
    );
  }

  private SummaryItem getFlareReportSummaryItem(ApplicationVersion applicationVersion,
                                                EmissionCategoryType emissionCategoryType) {
    return SummaryItem.withCards("Flare report",
        switch (emissionCategoryType) {
          case CATEGORY_123 -> List.of(flareReport123SummaryService.getFlareReport123SummaryCard(applicationVersion));
          case CATEGORY_ABC -> flareReportService.getFlareReportSummaryCards(applicationVersion);
          case LEGACY_LONG_TERM -> throw emissionCategoryType.unsupportedOperation();
        }
    );
  }

  private SummaryItem getFlareReportGasDataSummaryItem(ApplicationVersion applicationVersion,
                                                       EmissionCategoryType emissionCategoryType) {
    return SummaryItem.withCards("Flare report gas properties",
        switch (emissionCategoryType) {
          case CATEGORY_123 ->
              List.of(flareReport123GasDataSummaryService.getFlareReport123GasDataSummaryCard(applicationVersion));
          case CATEGORY_ABC -> flareReportGasDataService.getFlareReportGasDataSummaryCards(applicationVersion);
          case LEGACY_LONG_TERM -> throw emissionCategoryType.unsupportedOperation();
        }
    );
  }

  private SummaryItem getFlareConsentSummaryItem(ApplicationVersion applicationVersion,
                                                 ConsentLengthType consentLengthType,
                                                 EmissionCategoryType emissionCategoryType) {

    return switch (consentLengthType) {
      case SHORT_TERM ->
          SummaryItem.withCard(consentLengthType.getDisplayName(),
              switch (emissionCategoryType) {
                case CATEGORY_123 -> flareShortTerm123SummaryService.getFlareShortTerm123SummaryCard(applicationVersion);
                case CATEGORY_ABC -> flareShortTermService.getFlareShortTermSummaryCard(applicationVersion);
                case LEGACY_LONG_TERM -> throw emissionCategoryType.unsupportedOperation();
              }
          );
      case ANNUAL ->
          SummaryItem.withCard(consentLengthType.getDisplayName(),
              switch (emissionCategoryType) {
                case CATEGORY_123 -> flareAnnual123SummaryService.getFlareAnnual123SummaryCard(applicationVersion);
                case CATEGORY_ABC -> flareAnnualService.getFlareAnnualSummaryCard(applicationVersion);
                case LEGACY_LONG_TERM -> throw emissionCategoryType.unsupportedOperation();
              }
          );
      case LONG_TERM -> switch (emissionCategoryType) {
        case LEGACY_LONG_TERM ->
            SummaryItem.withCard(consentLengthType.getDisplayName(),
                flareLongTermSummaryService.getFlareLongTermSummaryCard(applicationVersion)
            );
        case CATEGORY_123, CATEGORY_ABC -> throw emissionCategoryType.unsupportedOperation();
      };
    };
  }
}
