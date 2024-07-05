package uk.co.nstauthority.fieldconsents.application.summary.production;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthDetails;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.production.summary.ProductionSummaryService;
import uk.co.nstauthority.fieldconsents.summary.SummaryItem;
import uk.co.nstauthority.fieldconsents.summary.SummarySection;
import uk.co.nstauthority.fieldconsents.summary.SummarySectionService;

@Service
public class ProductionInformationSummarySectionService implements SummarySectionService<ApplicationVersion> {

  private final ConsentLengthService consentLengthService;

  private final ProductionSummaryService productionSummaryService;

  @Autowired
  ProductionInformationSummarySectionService(ConsentLengthService consentLengthService,
                                             ProductionSummaryService productionSummaryService) {
    this.consentLengthService = consentLengthService;
    this.productionSummaryService = productionSummaryService;
  }

  @Override
  public Optional<SummarySection> getSummarySection(ApplicationVersion applicationVersion, ServiceUserDetail user) {
    List<SummaryItem> summaryItems = new ArrayList<>();

    if (!ApplicationType.PRODUCTION.equals(applicationVersion.getApplication().getType())) {
      return Optional.empty();
    }

    var consentLengthDetailsOptional =
        consentLengthService.findConsentLengthDetails(applicationVersion);

    if (consentLengthDetailsOptional.isEmpty()) {
      return Optional.empty();
    }

    ConsentLengthDetails consentLengthDetails = consentLengthDetailsOptional.get();

    summaryItems.add(getProductionConsentSummaryItem(applicationVersion, consentLengthDetails));

    return Optional.of(new SummarySection(20, summaryItems));
  }

  private SummaryItem getProductionConsentSummaryItem(ApplicationVersion applicationVersion,
                                                      ConsentLengthDetails consentLengthDetails) {

    var consentLengthType = consentLengthDetails.getConsentLength();

    return switch (consentLengthType) {
      case SHORT_TERM -> SummaryItem.withCard(consentLengthType.getDisplayName(),
          productionSummaryService.getShortTermConsentSummaryCard(applicationVersion)
      );
      case ANNUAL -> SummaryItem.withCard(consentLengthType.getDisplayName(),
          productionSummaryService.getAnnualConsentSummaryCard(applicationVersion)
      );
      case LONG_TERM -> SummaryItem.withCard(consentLengthType.getDisplayName(),
          productionSummaryService.getLongTermConsentSummaryCard(applicationVersion)
      );
    };
  }
}
