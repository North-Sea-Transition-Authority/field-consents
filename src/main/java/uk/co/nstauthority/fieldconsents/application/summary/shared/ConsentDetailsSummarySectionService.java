package uk.co.nstauthority.fieldconsents.application.summary.shared;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationContextService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTypeFeature;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.assets.AssetSummaryService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.production.gasinjection.GasInjectionService;
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

  @Autowired
  ConsentDetailsSummarySectionService(ApplicationContextService applicationContextService,
                                      ConsentLengthService consentLengthService,
                                      GasInjectionService gasInjectionService,
                                      ApplicationAssetService applicationAssetService,
                                      AssetSummaryService assetSummaryService) {
    this.applicationContextService = applicationContextService;
    this.consentLengthService = consentLengthService;
    this.gasInjectionService = gasInjectionService;
    this.applicationAssetService = applicationAssetService;
    this.assetSummaryService = assetSummaryService;
  }

  @Override
  public Optional<SummarySection> getSummarySection(ApplicationVersion applicationVersion) {

    List<SummaryItem> summaryItems = new ArrayList<>();

    var primaryAsset = applicationAssetService.getPrimaryAsset(applicationVersion);
    var applicationType =  applicationVersion.getApplication().getType();

    summaryItems.add(getApplicationContextSummaryItem(applicationVersion));

    summaryItems.add(getConsentDurationSummaryItem(applicationVersion));

    if (ApplicationTypeFeature.SECONDARY_ASSETS.allowed(applicationType) && primaryAsset.isField()) {
      summaryItems.add(getAdditionalAssetsSummaryItem(applicationVersion));
    }

    if (ApplicationTypeFeature.GAS_INJECTION.allowed(applicationType)) {
      summaryItems.add(getGasInjectionSummaryItem(applicationVersion));
    }

    return Optional.of(new SummarySection(10, summaryItems));
  }

  private SummaryItem getApplicationContextSummaryItem(ApplicationVersion applicationVersion) {
    return SummaryItem.withGroup("Application details",
        applicationContextService.getApplicationContextSummaryGroup(applicationVersion)
    );
  }

  private SummaryItem getConsentDurationSummaryItem(ApplicationVersion applicationVersion) {
    return SummaryItem.withGroup("Consent duration",
        consentLengthService.getConsentLengthSummaryGroup(applicationVersion)
    );
  }

  private SummaryItem getAdditionalAssetsSummaryItem(ApplicationVersion applicationVersion) {
    return SummaryItem.withGroups("Additional fields and licences",
        assetSummaryService.getAdditionalAssetsSummaryGroups(applicationVersion)
    );
  }

  private SummaryItem getGasInjectionSummaryItem(ApplicationVersion applicationVersion) {
    return SummaryItem.withGroup("Gas injection",
        gasInjectionService.getGasInjectionSummaryGroup(applicationVersion)
    );
  }
}
