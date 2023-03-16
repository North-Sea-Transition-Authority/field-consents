package uk.co.nstauthority.fieldconsents.application.summary.flare;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthDetails;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.flarevent.flare.annual.FlareAnnualService;
import uk.co.nstauthority.fieldconsents.flarevent.flare.flares.FlareSummaryService;
import uk.co.nstauthority.fieldconsents.flarevent.flare.shortterm.FlareShortTermService;
import uk.co.nstauthority.fieldconsents.summary.SummaryItem;
import uk.co.nstauthority.fieldconsents.summary.SummarySection;
import uk.co.nstauthority.fieldconsents.summary.SummarySectionService;

@Service
public class FlareInformationSummarySectionService implements SummarySectionService<ApplicationVersion> {

  private final ConsentLengthService consentLengthService;

  private final FlareAnnualService flareAnnualService;

  private final FlareShortTermService flareShortTermService;

  private final FlareSummaryService flareSummaryService;

  @Autowired
  public FlareInformationSummarySectionService(ConsentLengthService consentLengthService,
                                               FlareAnnualService flareAnnualService,
                                               FlareShortTermService flareShortTermService,
                                               FlareSummaryService flareSummaryService) {
    this.consentLengthService = consentLengthService;
    this.flareAnnualService = flareAnnualService;
    this.flareShortTermService = flareShortTermService;
    this.flareSummaryService = flareSummaryService;
  }


  @Override
  public Optional<SummarySection> getSummarySection(ApplicationVersion applicationVersion) {

    if (!ApplicationType.FLARE.equals(applicationVersion.getApplication().getType())) {
      return Optional.empty();
    }

    var consentLengthDetailsOptional =
        consentLengthService.findConsentLengthDetails(applicationVersion);

    if (consentLengthDetailsOptional.isEmpty()) {
      return Optional.empty();
    }

    ConsentLengthDetails consentLengthDetails = consentLengthDetailsOptional.get();

    List<SummaryItem> summaryItems = new ArrayList<>();

    summaryItems.add(getFlaresSummaryItem(applicationVersion));

    return Optional.of(new SummarySection(20, summaryItems));
  }

  private SummaryItem getFlaresSummaryItem(ApplicationVersion applicationVersion) {
    return SummaryItem.withCards("Flares",
        flareSummaryService.getSummariesForFlares(applicationVersion)
    );
  }
}
