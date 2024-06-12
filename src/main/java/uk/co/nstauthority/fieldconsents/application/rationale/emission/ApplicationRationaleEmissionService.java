package uk.co.nstauthority.fieldconsents.application.rationale.emission;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentData;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentDataService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure.ConsentDataLongTermEmissionFiguresService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure.ConsentFigureUnitService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;

@Service
public class ApplicationRationaleEmissionService {

  private final Clock clock;
  private final ApplicationVersionService applicationVersionService;
  private final ConsentDataService consentDataService;
  private final ConsentLengthService consentLengthService;
  private final ConsentFigureUnitService consentFigureUnitService;
  private final ConsentDataLongTermEmissionFiguresService consentDataLongTermEmissionFiguresService;

  ApplicationRationaleEmissionService(
      Clock clock,
      ApplicationVersionService applicationVersionService,
      ConsentDataService consentDataService,
      ConsentLengthService consentLengthService,
      ConsentFigureUnitService consentFigureUnitService,
      ConsentDataLongTermEmissionFiguresService consentDataLongTermEmissionFiguresService
  ) {
    this.clock = clock;
    this.applicationVersionService = applicationVersionService;
    this.consentDataService = consentDataService;
    this.consentLengthService = consentLengthService;
    this.consentFigureUnitService = consentFigureUnitService;
    this.consentDataLongTermEmissionFiguresService = consentDataLongTermEmissionFiguresService;
  }

  public Optional<EmissionDailyAverage> findEmissionDailyAverage(ApplicationVersion applicationVersion) {
    var currentYear = LocalDate.now(clock).getYear();
    return consentDataService.getConsentDataForYearAndApplicationVersionPrimaryAssetAndApplicationType(
            currentYear,
            applicationVersion
        )
        .stream()
        .max(Comparator.comparing(ConsentData::getConsentStartDate))
        .map(consentData -> getEmissionDailyAverage(currentYear, consentData));
  }

  EmissionDailyAverage getEmissionDailyAverage(Integer currentYear, ConsentData consentData) {
    var application = consentData.getApplication();
    var applicationType = application.getType();

    if (!(ApplicationType.FLARE == applicationType || ApplicationType.VENT == applicationType)) {
      throw new IllegalArgumentException(
          "Consent data [%s] is not for a flare/vent application. The application [%s] is of type %s"
              .formatted(consentData.getId(), application.getId(), applicationType));
    }

    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(application.getId());
    var consentLengthType = consentLengthService.getConsentLengthDetails(applicationVersion).getConsentLength();
    var consentFigureUnitView = consentFigureUnitService.getConsentFigureUnitView(applicationVersion, consentLengthType);

    return switch (consentLengthType) {
      case SHORT_TERM, ANNUAL -> EmissionDailyAverage.from(currentYear, consentData, consentFigureUnitView);
      case LONG_TERM -> consentDataLongTermEmissionFiguresService.getConsentDataLongTermEmissionFiguresList(application)
          .stream()
          .filter(figures -> figures.getYear().equals(currentYear))
          .findFirst()
          .map(figures -> EmissionDailyAverage.from(figures, consentFigureUnitView))
          .orElse(null);
    };
  }
}
