package uk.co.nstauthority.fieldconsents.application.unit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentUnit;
import uk.co.nstauthority.fieldconsents.production.ProductionUnit;

@Service
public class ApplicationUnitService {

  private final ApplicationUnitRepository applicationUnitRepository;

  private final ConsentLengthService consentLengthService;

  @Autowired
  ApplicationUnitService(ApplicationUnitRepository applicationUnitRepository,
                         ConsentLengthService consentLengthService) {
    this.applicationUnitRepository = applicationUnitRepository;
    this.consentLengthService = consentLengthService;
  }

  ApplicationUnit getOrCreateApplicationUnit(ApplicationVersion applicationVersion) {
    return applicationUnitRepository.findByApplicationVersion(applicationVersion)
        .orElseGet(() -> createApplicationUnit(applicationVersion));
  }

  public FlareVentUnit getFlareCategoryUnit(ApplicationVersion applicationVersion) {
    return getOrCreateApplicationUnit(applicationVersion).getFlareCategoryUnit();
  }

  public FlareVentUnit getVentCategoryUnit(ApplicationVersion applicationVersion) {
    return getOrCreateApplicationUnit(applicationVersion).getVentCategoryUnit();
  }

  public ProductionUnit getProductionOilUnit(ApplicationVersion applicationVersion) {
    return getOrCreateApplicationUnit(applicationVersion).getProductionOilUnit();
  }

  public ProductionUnit getProductionGasUnit(ApplicationVersion applicationVersion) {
    return getOrCreateApplicationUnit(applicationVersion).getProductionGasUnit();
  }

  @Transactional
  public ApplicationUnit createApplicationUnit(ApplicationVersion applicationVersion) {
    ApplicationUnit applicationUnit;
    ApplicationType applicationType = applicationVersion.getApplication().getType();
    ConsentLengthType consentLength = consentLengthService.getConsentLengthDetails(applicationVersion).getConsentLength();

    switch (applicationType) {
      case PRODUCTION -> {
        ProductionUnit oilUnit = ProductionUnit.SCM_PER_MONTH;
        ProductionUnit gasUnit = ProductionUnit.KSCM_PER_MONTH;
        if (consentLength.equals(ConsentLengthType.LONG_TERM)) {
          oilUnit = ProductionUnit.SCM_PER_DAY;
          gasUnit = ProductionUnit.KSCM_PER_DAY;
        }
        applicationUnit = new ApplicationUnit(applicationVersion, null, null, oilUnit, gasUnit);
      }
      case FLARE ->
          applicationUnit = new ApplicationUnit(applicationVersion, FlareVentUnit.TONNES_PER_MONTH, null, null, null);
      case VENT ->
          applicationUnit = new ApplicationUnit(applicationVersion, null, FlareVentUnit.TONNES_PER_MONTH, null, null);
      default -> throw new RuntimeException("Incorrect application type: " + applicationType);
    }

    applicationUnitRepository.save(applicationUnit);
    return applicationUnit;
  }

}
