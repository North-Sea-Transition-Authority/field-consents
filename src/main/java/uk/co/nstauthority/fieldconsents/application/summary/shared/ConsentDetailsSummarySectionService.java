package uk.co.nstauthority.fieldconsents.application.summary.shared;

import java.util.ArrayList;
import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationContextService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTypeFeature;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.assets.AssetSummaryService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.application.rationale.flare.ApplicationRationaleFlareService;
import uk.co.nstauthority.fieldconsents.application.rationale.production.ApplicationRationaleProductionService;
import uk.co.nstauthority.fieldconsents.application.rationale.vent.ApplicationRationaleVentService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.production.gasinjection.GasInjectionService;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.summary.SummaryItem;
import uk.co.nstauthority.fieldconsents.summary.SummarySection;
import uk.co.nstauthority.fieldconsents.summary.SummarySectionService;

@Service
public class ConsentDetailsSummarySectionService implements SummarySectionService<ApplicationVersion> {

  private final ApplicationContextService applicationContextService;
  private final ConsentLengthService consentLengthService;
  private final GasInjectionService gasInjectionService;
  private final ApplicationAssetService applicationAssetService;
  private final AssetSummaryService assetSummaryService;
  private final ApplicationRationaleFlareService applicationRationaleFlareService;
  private final ApplicationRationaleVentService applicationRationaleVentService;
  private final ApplicationRationaleProductionService applicationRationaleProductionService;
  private final LicenceDetailsSummaryCardService licenceDetailsSummaryCardService;

  ConsentDetailsSummarySectionService(
      ApplicationContextService applicationContextService,
      ConsentLengthService consentLengthService,
      GasInjectionService gasInjectionService,
      ApplicationAssetService applicationAssetService,
      AssetSummaryService assetSummaryService,
      ApplicationRationaleFlareService applicationRationaleFlareService,
      ApplicationRationaleVentService applicationRationaleVentService,
      ApplicationRationaleProductionService applicationRationaleProductionService,
      LicenceDetailsSummaryCardService licenceDetailsSummaryCardService
  ) {
    this.applicationContextService = applicationContextService;
    this.consentLengthService = consentLengthService;
    this.gasInjectionService = gasInjectionService;
    this.applicationAssetService = applicationAssetService;
    this.assetSummaryService = assetSummaryService;
    this.applicationRationaleFlareService = applicationRationaleFlareService;
    this.applicationRationaleVentService = applicationRationaleVentService;
    this.applicationRationaleProductionService = applicationRationaleProductionService;
    this.licenceDetailsSummaryCardService = licenceDetailsSummaryCardService;
  }

  @Override
  public Optional<SummarySection> getSummarySection(ApplicationVersion applicationVersion, ServiceUserDetail user) {
    var summaryItems = new ArrayList<SummaryItem>();

    getApplicationContextSummaryItem(applicationVersion).ifPresent(summaryItems::add);
    getApplicationRationaleSummaryItem(applicationVersion).ifPresent(summaryItems::add);
    getConsentDurationSummaryItem(applicationVersion).ifPresent(summaryItems::add);
    getAdditionalAssetsSummaryItem(applicationVersion).ifPresent(summaryItems::add);
    getGasInjectionSummaryItem(applicationVersion).ifPresent(summaryItems::add);

    if (summaryItems.isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(new SummarySection(10, summaryItems));
  }

  Optional<SummaryItem> getApplicationRationaleSummaryItem(ApplicationVersion applicationVersion) {
    var summaryCard = switch (applicationVersion.getApplication().getType()) {
      case FLARE -> applicationRationaleFlareService.getSummaryCard(applicationVersion);
      case VENT -> applicationRationaleVentService.getSummaryCard(applicationVersion);
      case PRODUCTION -> applicationRationaleProductionService.getSummaryCard(applicationVersion);
    };

    return Optional.of(SummaryItem.withCard("Application rationale", summaryCard));
  }

  Optional<SummaryItem> getApplicationContextSummaryItem(ApplicationVersion applicationVersion) {
    var summaryCards = new ArrayList<SummaryCard>();

    summaryCards.add(applicationContextService.getApplicationContextSummaryCard(applicationVersion));
    licenceDetailsSummaryCardService.getSummaryCard(applicationVersion).ifPresent(summaryCards::add);

    return Optional.of(SummaryItem.withCards("Application details", summaryCards));
  }

  Optional<SummaryItem> getConsentDurationSummaryItem(ApplicationVersion applicationVersion) {
    return Optional.of(SummaryItem.withCard("Consent duration",
        consentLengthService.getConsentLengthSummaryCard(applicationVersion)
    ));
  }

  Optional<SummaryItem> getAdditionalAssetsSummaryItem(ApplicationVersion applicationVersion) {
    var applicationType = applicationVersion.getApplication().getType();

    if (!ApplicationTypeFeature.SECONDARY_ASSETS.allowed(applicationType)) {
      return Optional.empty();
    }

    if (!applicationAssetService.getPrimaryAsset(applicationVersion).isField()) {
      return Optional.empty();
    }

    return Optional.of(SummaryItem.withCards("Additional fields and licences",
        assetSummaryService.getAdditionalAssetsSummaryCards(applicationVersion)
    ));
  }

  Optional<SummaryItem> getGasInjectionSummaryItem(ApplicationVersion applicationVersion) {
    var applicationType = applicationVersion.getApplication().getType();

    if (!ApplicationTypeFeature.GAS_INJECTION.allowed(applicationType)) {
      return Optional.empty();
    }

    return Optional.of(SummaryItem.withCard("Gas injection",
        gasInjectionService.getGasInjectionSummaryCard(applicationVersion)
    ));
  }
}
