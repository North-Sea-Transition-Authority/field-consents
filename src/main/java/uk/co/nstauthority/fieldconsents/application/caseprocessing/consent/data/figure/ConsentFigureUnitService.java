package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure;

import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.application.unit.ApplicationUnitService;

@Service
public class ConsentFigureUnitService {

  private final ApplicationUnitService applicationUnitService;

  ConsentFigureUnitService(ApplicationUnitService applicationUnitService) {
    this.applicationUnitService = applicationUnitService;
  }

  public ConsentFigureUnitView getConsentFigureUnitView(
      ApplicationVersion applicationVersion,
      ConsentLengthType consentLengthType
  ) {
    return switch (applicationVersion.getApplication().getType()) {
      case PRODUCTION -> switch (consentLengthType) {
        case SHORT_TERM, ANNUAL -> getConsentFigureUnitViewForShortTermOrAnnualProductionApplication(applicationVersion);
        case LONG_TERM -> getConsentFigureUnitViewForLongTermProductionApplication(applicationVersion);
      };
      case FLARE, VENT -> getConsentFigureUnitViewForEmissionApplication(applicationVersion);
    };
  }

  ConsentFigureUnitView getConsentFigureUnitViewForShortTermOrAnnualProductionApplication(ApplicationVersion applicationVersion) {
    var productionAverageUnit = applicationUnitService.getProductionAverageUnit(applicationVersion);

    return ConsentFigureUnitView.fromShortTermOrAnnualProductionApplication(productionAverageUnit);
  }

  ConsentFigureUnitView getConsentFigureUnitViewForLongTermProductionApplication(ApplicationVersion applicationVersion) {
    var productionOilUnit = applicationUnitService.getProductionOilUnit(applicationVersion);
    var productionGasUnit = applicationUnitService.getProductionGasUnit(applicationVersion);

    return ConsentFigureUnitView.fromLongTermProductionApplication(productionOilUnit, productionGasUnit);
  }

  ConsentFigureUnitView getConsentFigureUnitViewForEmissionApplication(ApplicationVersion applicationVersion) {
    var applicationType = applicationVersion.getApplication().getType();

    var emissionAverageUnit = switch (applicationType) {
      case FLARE -> applicationUnitService.getFlareAverageUnit(applicationVersion);
      case VENT -> applicationUnitService.getVentAverageUnit(applicationVersion);
      default -> throw new IllegalStateException("Unexpected ApplicationType: %s".formatted(applicationType));
    };

    return ConsentFigureUnitView.fromEmissionApplication(emissionAverageUnit);
  }
}
