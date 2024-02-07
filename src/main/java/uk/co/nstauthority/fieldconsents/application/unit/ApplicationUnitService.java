package uk.co.nstauthority.fieldconsents.application.unit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthChangeEvent;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.flarevent.EmissionCategoryType;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentUnit;
import uk.co.nstauthority.fieldconsents.production.ProductionUnit;

@Service
public class ApplicationUnitService implements ApplicationListener<ConsentLengthChangeEvent> {

  private static final String AVERAGE_UNIT_EXCEPTION_MESSAGE =
      "Mismatched %s category unit (%s). Cannot work out the unit for the averages.";

  private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationUnitService.class);

  private final ApplicationUnitRepository applicationUnitRepository;

  private final ApplicationVersionService applicationVersionService;

  private final ConsentLengthService consentLengthService;

  @Autowired
  ApplicationUnitService(ApplicationUnitRepository applicationUnitRepository,
                         ApplicationVersionService applicationVersionService,
                         ConsentLengthService consentLengthService) {
    this.applicationUnitRepository = applicationUnitRepository;
    this.applicationVersionService = applicationVersionService;
    this.consentLengthService = consentLengthService;
  }

  ApplicationUnit getOrCreateApplicationUnit(ApplicationVersion applicationVersion) {
    return applicationUnitRepository.findByApplicationVersion(applicationVersion)
        .orElseGet(() -> createApplicationUnit(applicationVersion));
  }

  public FlareVentUnit getFlareCategoryUnit(ApplicationVersion applicationVersion) {
    return getOrCreateApplicationUnit(applicationVersion).getFlareCategoryUnit();
  }

  public FlareVentUnit getFlareAverageUnit(ApplicationVersion applicationVersion) {
    var applicationUnit = getOrCreateApplicationUnit(applicationVersion);
    if (FlareVentUnit.TONNES_PER_MONTH.equals(applicationUnit.getFlareCategoryUnit())) {
      return FlareVentUnit.TONNES_PER_DAY;
    } else {
      throw new IllegalStateException(AVERAGE_UNIT_EXCEPTION_MESSAGE
          .formatted("flare", applicationUnit.getFlareCategoryUnit().name()));
    }
  }

  public FlareVentUnit getVentCategoryUnit(ApplicationVersion applicationVersion) {
    return getOrCreateApplicationUnit(applicationVersion).getVentCategoryUnit();
  }

  public FlareVentUnit getVentAverageUnit(ApplicationVersion applicationVersion) {
    var applicationUnit = getOrCreateApplicationUnit(applicationVersion);
    if (FlareVentUnit.TONNES_PER_MONTH.equals(applicationUnit.getVentCategoryUnit())) {
      return FlareVentUnit.TONNES_PER_DAY;
    } else {
      throw new IllegalStateException(AVERAGE_UNIT_EXCEPTION_MESSAGE
          .formatted("vent", applicationUnit.getVentCategoryUnit().name()));
    }
  }

  public FlareVentUnit getFlareGasDensityUnit(ApplicationVersion applicationVersion) {
    return getOrCreateApplicationUnit(applicationVersion).getFlareGasDensityUnit();
  }

  public FlareVentUnit getFlareGasContentUnit(ApplicationVersion applicationVersion) {
    return getOrCreateApplicationUnit(applicationVersion).getFlareGasContentUnit();
  }

  public FlareVentUnit getVentGasDensityUnit(ApplicationVersion applicationVersion) {
    return getOrCreateApplicationUnit(applicationVersion).getVentGasDensityUnit();
  }

  public FlareVentUnit getVentGasContentUnit(ApplicationVersion applicationVersion) {
    return getOrCreateApplicationUnit(applicationVersion).getVentGasContentUnit();
  }

  public EmissionCategoryType getEmissionCategoryType(ApplicationVersion applicationVersion) {
    return getOrCreateApplicationUnit(applicationVersion).getEmissionCategoryType();
  }

  public ProductionUnit getProductionOilUnit(ApplicationVersion applicationVersion) {
    return getOrCreateApplicationUnit(applicationVersion).getProductionOilUnit();
  }

  public ProductionUnit getProductionGasUnit(ApplicationVersion applicationVersion) {
    return getOrCreateApplicationUnit(applicationVersion).getProductionGasUnit();
  }

  public ProductionUnit getProductionAverageUnit(ApplicationVersion applicationVersion) {
    var applicationUnit = getOrCreateApplicationUnit(applicationVersion);
    var oilUnit = applicationUnit.getProductionOilUnit();
    var gasUnit = applicationUnit.getProductionGasUnit();
    ProductionUnit averageUnit;
    if (ProductionUnit.KSCM_PER_MONTH.equals(oilUnit) && ProductionUnit.KSCM_PER_MONTH.equals(gasUnit)) {
      averageUnit = ProductionUnit.KSCM_PER_DAY;
    } else if (ProductionUnit.SCM_PER_MONTH.equals(oilUnit) && ProductionUnit.SCM_PER_MONTH.equals(gasUnit)) {
      averageUnit = ProductionUnit.SCM_PER_DAY;
    // cater for migrated cases
    } else if (ProductionUnit.SCM_PER_MONTH.equals(oilUnit) && ProductionUnit.KSCM_PER_MONTH.equals(gasUnit)) {
      averageUnit = ProductionUnit.KSCM_PER_DAY;
    } else {
      throw new IllegalStateException("Mismatched production units found. Cannot work out the unit for the averages.");
    }
    return averageUnit;
  }

  /**
   * This function will only return anything other than 1 for migrated case units as these are the only cases where
   * the entered production data mismatches how we want to show the daily averages.
   *
   * @param productionUnit        The production unit in use (on the form data).
   * @param averageProductionUnit The unit in use for the averages.
   * @return The factor used to convert the production data into the unit in use for the averages.
   */
  public int getProductionAverageConversionFactor(ProductionUnit productionUnit, ProductionUnit averageProductionUnit) {
    if (ProductionUnit.KSCM_PER_MONTH.equals(productionUnit) && ProductionUnit.KSCM_PER_DAY.equals(averageProductionUnit)) {
      return 1;
    } else if (ProductionUnit.SCM_PER_MONTH.equals(productionUnit) && ProductionUnit.SCM_PER_DAY.equals(averageProductionUnit)) {
      return 1;
    } else if (ProductionUnit.SCM_PER_MONTH.equals(productionUnit) && ProductionUnit.KSCM_PER_DAY.equals(averageProductionUnit)) {
      return 1000;
    } else {
      throw new IllegalStateException(
          "Unhandled production units found. Cannot work out the production average conversion factor.");
    }
  }

  @Transactional
  public ApplicationUnit createApplicationUnit(ApplicationVersion applicationVersion) {
    ApplicationUnit applicationUnit;
    ApplicationType applicationType = applicationVersion.getApplication().getType();
    ConsentLengthType consentLength = consentLengthService.getConsentLengthDetails(applicationVersion).getConsentLength();

    switch (applicationType) {
      case PRODUCTION -> {
        ProductionUnit oilUnit = ProductionUnit.KSCM_PER_MONTH;
        ProductionUnit gasUnit = ProductionUnit.KSCM_PER_MONTH;
        if (consentLength.equals(ConsentLengthType.LONG_TERM)) {
          oilUnit = ProductionUnit.KSCM_PER_DAY;
          gasUnit = ProductionUnit.KSCM_PER_DAY;
        }
        applicationUnit = new ApplicationUnit(applicationVersion, null, null, oilUnit, gasUnit, null, null, null, null, null);
      }
      case FLARE ->
          applicationUnit = new ApplicationUnit(applicationVersion, FlareVentUnit.TONNES_PER_MONTH, null, null, null,
              FlareVentUnit.KG_PER_CUBIC_METER, FlareVentUnit.MASS_PERCENTAGE,
              null, null,
              EmissionCategoryType.CATEGORY_ABC);
      case VENT ->
          applicationUnit = new ApplicationUnit(applicationVersion, null, FlareVentUnit.TONNES_PER_MONTH, null, null,
              null, null,
              FlareVentUnit.KG_PER_CUBIC_METER, FlareVentUnit.MASS_PERCENTAGE,
              EmissionCategoryType.CATEGORY_ABC);
      default -> throw new IllegalStateException("Incorrect application type: " + applicationType);
    }

    applicationUnitRepository.save(applicationUnit);
    return applicationUnit;
  }

  @Transactional
  public void replaceApplicationUnit(ApplicationVersion applicationVersion) {
    applicationUnitRepository.deleteAllByApplicationVersion(applicationVersion);
    createApplicationUnit(applicationVersion);
  }

  @Override
  @Transactional
  public void onApplicationEvent(ConsentLengthChangeEvent event) {
    ApplicationVersion applicationVersion = applicationVersionService.getApplicationVersionById(event.getApplicationVersionId());

    ApplicationType applicationType = applicationVersion.getApplication().getType();

    if (applicationType.equals(ApplicationType.PRODUCTION)) {
      replaceApplicationUnit(applicationVersion);
      ConsentLengthType consentLength = consentLengthService.getConsentLengthDetails(applicationVersion).getConsentLength();

      LOGGER.debug("Old application units removed when consent length changed to {} for application version with id {}.",
          consentLength.getDisplayName(),
          applicationVersion.getId());
    }
  }
}
