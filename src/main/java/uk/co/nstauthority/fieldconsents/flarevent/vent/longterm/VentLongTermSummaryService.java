package uk.co.nstauthority.fieldconsents.flarevent.vent.longterm;

import java.util.Comparator;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.unit.ApplicationUnitService;
import uk.co.nstauthority.fieldconsents.flarevent.EmissionLongTermYear;
import uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionConsentSummaryService;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;

@Service
public class VentLongTermSummaryService {

  private final VentLongTermYearRepository ventLongTermYearRepository;

  private final ApplicationUnitService applicationUnitService;

  private final EmissionConsentSummaryService emissionConsentSummaryService;

  @Autowired
  VentLongTermSummaryService(VentLongTermYearRepository ventLongTermYearRepository,
                             ApplicationUnitService applicationUnitService,
                             EmissionConsentSummaryService emissionConsentSummaryService) {
    this.ventLongTermYearRepository = ventLongTermYearRepository;
    this.applicationUnitService = applicationUnitService;
    this.emissionConsentSummaryService = emissionConsentSummaryService;
  }

  private List<VentLongTermYear> getVentLongTermYears(ApplicationVersion applicationVersion) {
    return ventLongTermYearRepository.findAllByApplicationVersion(applicationVersion)
        .stream()
        .sorted(Comparator.comparing(EmissionLongTermYear::getYear))
        .toList();
  }

  public SummaryCard getVentLongTermSummaryCard(ApplicationVersion applicationVersion) {

    var ventLongTermYears = getVentLongTermYears(applicationVersion);

    if (ventLongTermYears.isEmpty()) {
      return SummaryCard.emptySummaryCard();
    }

    var categoryUnit = applicationUnitService.getVentCategoryUnit(applicationVersion);

    return emissionConsentSummaryService.getLongTermConsentSummaryCard(ventLongTermYears, categoryUnit);
  }
}
