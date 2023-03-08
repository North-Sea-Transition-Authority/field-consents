package uk.co.nstauthority.fieldconsents.application.summary.shared;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationContextService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTypeFeature;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.production.gasinjection.GasInjectionService;
import uk.co.nstauthority.fieldconsents.summary.SummaryDataView;
import uk.co.nstauthority.fieldconsents.summary.SummaryItem;
import uk.co.nstauthority.fieldconsents.summary.SummarySection;
import uk.co.nstauthority.fieldconsents.summary.SummarySectionService;

@Service
public class ConsentDetailsSummarySectionService implements SummarySectionService<ApplicationVersion> {

  private final ApplicationContextService applicationContextService;

  private final ConsentLengthService consentLengthService;

  private final GasInjectionService gasInjectionService;

  @Autowired
  ConsentDetailsSummarySectionService(ApplicationContextService applicationContextService,
                                      ConsentLengthService consentLengthService,
                                      GasInjectionService gasInjectionService) {
    this.applicationContextService = applicationContextService;
    this.consentLengthService = consentLengthService;
    this.gasInjectionService = gasInjectionService;
  }

  @Override
  public Optional<SummarySection> getSummarySection(ApplicationVersion applicationVersion) {

    List<SummaryItem<?>> summaryItems = new ArrayList<>();

    var applicationType =  applicationVersion.getApplication().getType();

    summaryItems.add(getApplicationContextSummaryItem(applicationVersion));

    summaryItems.add(getConsentDurationSummaryItem(applicationVersion));

    if (ApplicationTypeFeature.GAS_INJECTION.allowed(applicationType)) {
      summaryItems.add(getGasInjectionSummaryItem(applicationVersion));
    }

    return Optional.of(new SummarySection(10, summaryItems));
  }

  private SummaryItem<SummaryDataView> getApplicationContextSummaryItem(ApplicationVersion applicationVersion) {
    return SummaryItem.simpleSummaryItem("Application details",
        applicationContextService.getApplicationContextSummaryDataView(applicationVersion)
    );
  }

  private SummaryItem<SummaryDataView> getConsentDurationSummaryItem(ApplicationVersion applicationVersion) {
    return SummaryItem.simpleSummaryItem("Consent duration",
        consentLengthService.getConsentLengthSummaryDataView(applicationVersion)
    );
  }

  private SummaryItem<SummaryDataView> getGasInjectionSummaryItem(ApplicationVersion applicationVersion) {
    return SummaryItem.simpleSummaryItem("Gas injection",
        gasInjectionService.getGasInjectionSummaryDataView(applicationVersion)
    );
  }
}
