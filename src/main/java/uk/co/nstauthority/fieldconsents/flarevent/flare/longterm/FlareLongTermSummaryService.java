package uk.co.nstauthority.fieldconsents.flarevent.flare.longterm;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.unit.ApplicationUnitService;
import uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionConsentSummaryService;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.util.ApplicationFigureComparators;

@Service
public class FlareLongTermSummaryService {

  private final FlareLongTermYearRepository flareLongTermYearRepository;

  private final ApplicationUnitService applicationUnitService;

  private final EmissionConsentSummaryService emissionConsentSummaryService;

  @Autowired
  FlareLongTermSummaryService(FlareLongTermYearRepository flareLongTermYearRepository,
                              ApplicationUnitService applicationUnitService,
                              EmissionConsentSummaryService emissionConsentSummaryService) {
    this.flareLongTermYearRepository = flareLongTermYearRepository;
    this.applicationUnitService = applicationUnitService;
    this.emissionConsentSummaryService = emissionConsentSummaryService;
  }

  private List<FlareLongTermYear> getFlareLongTermYears(ApplicationVersion applicationVersion) {
    return flareLongTermYearRepository.findAllByApplicationVersion(applicationVersion)
        .stream()
        .sorted(ApplicationFigureComparators.emissionLongTermYear())
        .toList();
  }

  public SummaryCard getFlareLongTermSummaryCard(ApplicationVersion applicationVersion) {

    var flareLongTermYears = getFlareLongTermYears(applicationVersion);

    if (flareLongTermYears.isEmpty()) {
      return SummaryCard.emptySummaryCard();
    }

    var categoryUnit = applicationUnitService.getFlareCategoryUnit(applicationVersion);

    return emissionConsentSummaryService.getLongTermConsentSummaryCard(flareLongTermYears, categoryUnit);
  }
}
