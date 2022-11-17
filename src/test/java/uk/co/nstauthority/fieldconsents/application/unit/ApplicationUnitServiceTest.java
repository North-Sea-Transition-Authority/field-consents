package uk.co.nstauthority.fieldconsents.application.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthTestUtil;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentUnit;
import uk.co.nstauthority.fieldconsents.flarevent.flare.FlareTestUtil;
import uk.co.nstauthority.fieldconsents.flarevent.vent.VentTestUtil;
import uk.co.nstauthority.fieldconsents.production.ProductionUnit;

@ExtendWith(MockitoExtension.class)
class ApplicationUnitServiceTest {

  @Mock
  private ApplicationUnitRepository applicationUnitRepository;

  @Mock
  private ConsentLengthService consentLengthService;

  private ApplicationUnitService applicationUnitService;

  private ApplicationVersion flareAppVersion;

  private ApplicationVersion ventAppVersion;

  private ApplicationVersion productionAppVersion;

  @BeforeEach
  void setUp() {
    applicationUnitService = new ApplicationUnitService(applicationUnitRepository, consentLengthService);
    flareAppVersion = FlareTestUtil.flareAppVersion;
    ventAppVersion = VentTestUtil.ventAppVersion;
    productionAppVersion = ApplicationTestUtil.getApplicationVersionWithType(ApplicationType.PRODUCTION);
  }

  @Test
  void getOrCreateApplicationUnit_notExists() {
    when(applicationUnitRepository.findByApplicationVersion(flareAppVersion)).thenReturn(Optional.empty());
    when(consentLengthService.getConsentLengthDetails(flareAppVersion))
        .thenReturn(ConsentLengthTestUtil.getConsentLengthDetailsForAnnual(flareAppVersion));

    ApplicationUnit returnedApplicationUnit = applicationUnitService.getOrCreateApplicationUnit(flareAppVersion);

    assertApplicationUnit(returnedApplicationUnit,
        flareAppVersion,
        FlareVentUnit.TONNES_PER_MONTH,
        null,
        null,
        null);
  }

  @Test
  void getOrCreateApplicationUnit() {
    when(applicationUnitRepository.findByApplicationVersion(flareAppVersion))
        .thenReturn(Optional.of(new ApplicationUnit(flareAppVersion, FlareVentUnit.TONNES_PER_MONTH,
            null, null, null)));

    ApplicationUnit returnedApplicationUnit = applicationUnitService.getOrCreateApplicationUnit(flareAppVersion);

    assertApplicationUnit(returnedApplicationUnit,
        flareAppVersion,
        FlareVentUnit.TONNES_PER_MONTH,
        null,
        null,
        null);
  }

  @Test
  void getFlareCategoryUnit_notExists() {
    when(applicationUnitRepository.findByApplicationVersion(flareAppVersion)).thenReturn(Optional.empty());
    when(consentLengthService.getConsentLengthDetails(flareAppVersion))
        .thenReturn(ConsentLengthTestUtil.getConsentLengthDetailsForAnnual(flareAppVersion));
    assertThat(applicationUnitService.getFlareCategoryUnit(flareAppVersion))
        .isEqualTo(FlareVentUnit.TONNES_PER_MONTH);
  }

  @Test
  void getFlareCategoryUnit() {
    ApplicationUnit applicationUnit = new ApplicationUnit();
    applicationUnit.setApplicationVersion(flareAppVersion);
    applicationUnit.setFlareCategoryUnit(FlareVentUnit.TONNES_PER_MONTH);
    when(applicationUnitRepository.findByApplicationVersion(flareAppVersion))
        .thenReturn(Optional.of(applicationUnit));

    assertThat(applicationUnitService.getFlareCategoryUnit(flareAppVersion))
        .isEqualTo(FlareVentUnit.TONNES_PER_MONTH);
  }

  @Test
  void getVentCategoryUnit_notExists() {
    when(applicationUnitRepository.findByApplicationVersion(ventAppVersion)).thenReturn(Optional.empty());
    when(consentLengthService.getConsentLengthDetails(ventAppVersion))
        .thenReturn(ConsentLengthTestUtil.getConsentLengthDetailsForAnnual(ventAppVersion));
    assertThat(applicationUnitService.getVentCategoryUnit(ventAppVersion))
        .isEqualTo(FlareVentUnit.TONNES_PER_MONTH);
  }

  @Test
  void getVentCategoryUnit() {
    ApplicationUnit applicationUnit = new ApplicationUnit();
    applicationUnit.setApplicationVersion(ventAppVersion);
    applicationUnit.setVentCategoryUnit(FlareVentUnit.TONNES_PER_MONTH);
    when(applicationUnitRepository.findByApplicationVersion(ventAppVersion))
        .thenReturn(Optional.of(applicationUnit));

    assertThat(applicationUnitService.getVentCategoryUnit(ventAppVersion))
        .isEqualTo(FlareVentUnit.TONNES_PER_MONTH);
  }

  @Test
  void getProductionOilUnit_notExists() {
    when(applicationUnitRepository.findByApplicationVersion(productionAppVersion)).thenReturn(Optional.empty());
    when(consentLengthService.getConsentLengthDetails(productionAppVersion))
        .thenReturn(ConsentLengthTestUtil.getConsentLengthDetailsForAnnual(productionAppVersion));
    assertThat(applicationUnitService.getProductionOilUnit(productionAppVersion))
        .isEqualTo(ProductionUnit.SCM_PER_MONTH);
  }

  @Test
  void getProductionOilUnit() {
    ApplicationUnit applicationUnit = new ApplicationUnit();
    applicationUnit.setApplicationVersion(productionAppVersion);
    applicationUnit.setProductionOilUnit(ProductionUnit.SCM_PER_MONTH);
    when(applicationUnitRepository.findByApplicationVersion(productionAppVersion))
        .thenReturn(Optional.of(applicationUnit));

    assertThat(applicationUnitService.getProductionOilUnit(productionAppVersion))
        .isEqualTo(ProductionUnit.SCM_PER_MONTH);
  }

  @Test
  void getProductionGasUnit_notExists() {
    when(applicationUnitRepository.findByApplicationVersion(productionAppVersion)).thenReturn(Optional.empty());
    when(consentLengthService.getConsentLengthDetails(productionAppVersion))
        .thenReturn(ConsentLengthTestUtil.getConsentLengthDetailsForAnnual(productionAppVersion));
    assertThat(applicationUnitService.getProductionGasUnit(productionAppVersion))
        .isEqualTo(ProductionUnit.KSCM_PER_MONTH);
  }

  @Test
  void getProductionGasUnit() {
    ApplicationUnit applicationUnit = new ApplicationUnit();
    applicationUnit.setApplicationVersion(productionAppVersion);
    applicationUnit.setProductionGasUnit(ProductionUnit.KSCM_PER_MONTH);
    when(applicationUnitRepository.findByApplicationVersion(productionAppVersion))
        .thenReturn(Optional.of(applicationUnit));

    assertThat(applicationUnitService.getProductionGasUnit(productionAppVersion))
        .isEqualTo(ProductionUnit.KSCM_PER_MONTH);
  }

  @Test
  void createApplicationUnit_flareAnnual() {
    when(consentLengthService.getConsentLengthDetails(flareAppVersion))
        .thenReturn(ConsentLengthTestUtil.getConsentLengthDetailsForAnnual(flareAppVersion));

    ApplicationUnit returnedApplicationUnit = applicationUnitService.createApplicationUnit(flareAppVersion);

    verify(applicationUnitRepository, times(1)).save(returnedApplicationUnit);

    assertApplicationUnit(returnedApplicationUnit,
        flareAppVersion,
        FlareVentUnit.TONNES_PER_MONTH,
        null,
        null,
        null);
  }

  @Test
  void createApplicationUnit_flareShortTerm() {
    when(consentLengthService.getConsentLengthDetails(flareAppVersion))
        .thenReturn(ConsentLengthTestUtil.getConsentLengthDetailsForShortTerm(flareAppVersion));

    ApplicationUnit returnedApplicationUnit = applicationUnitService.createApplicationUnit(flareAppVersion);

    verify(applicationUnitRepository, times(1)).save(returnedApplicationUnit);

    assertApplicationUnit(returnedApplicationUnit,
        flareAppVersion,
        FlareVentUnit.TONNES_PER_MONTH,
        null,
        null,
        null);
  }

  @Test
  void createApplicationUnit_ventAnnual() {
    when(consentLengthService.getConsentLengthDetails(ventAppVersion))
        .thenReturn(ConsentLengthTestUtil.getConsentLengthDetailsForAnnual(ventAppVersion));

    ApplicationUnit returnedApplicationUnit = applicationUnitService.createApplicationUnit(ventAppVersion);

    verify(applicationUnitRepository, times(1)).save(returnedApplicationUnit);

    assertApplicationUnit(returnedApplicationUnit,
        ventAppVersion,
        null,
        FlareVentUnit.TONNES_PER_MONTH,
        null,
        null);
  }

  @Test
  void createApplicationUnit_ventShortTerm() {
    when(consentLengthService.getConsentLengthDetails(ventAppVersion))
        .thenReturn(ConsentLengthTestUtil.getConsentLengthDetailsForShortTerm(ventAppVersion));

    ApplicationUnit returnedApplicationUnit = applicationUnitService.createApplicationUnit(ventAppVersion);

    verify(applicationUnitRepository, times(1)).save(returnedApplicationUnit);

    assertApplicationUnit(returnedApplicationUnit,
        ventAppVersion,
        null,
        FlareVentUnit.TONNES_PER_MONTH,
        null,
        null);
  }

  @Test
  void createApplicationUnit_productionLongTerm() {
    when(consentLengthService.getConsentLengthDetails(productionAppVersion))
        .thenReturn(ConsentLengthTestUtil.getConsentLengthDetailsForLongTerm(productionAppVersion));

    ApplicationUnit returnedApplicationUnit = applicationUnitService.createApplicationUnit(productionAppVersion);

    verify(applicationUnitRepository, times(1)).save(returnedApplicationUnit);

    assertApplicationUnit(returnedApplicationUnit,
        productionAppVersion,
        null,
        null,
        ProductionUnit.SCM_PER_DAY,
        ProductionUnit.KSCM_PER_DAY);
  }

  @Test
  void createApplicationUnit_productionAnnual() {
    when(consentLengthService.getConsentLengthDetails(productionAppVersion))
        .thenReturn(ConsentLengthTestUtil.getConsentLengthDetailsForAnnual(productionAppVersion));

    ApplicationUnit returnedApplicationUnit = applicationUnitService.createApplicationUnit(productionAppVersion);

    verify(applicationUnitRepository, times(1)).save(returnedApplicationUnit);

    assertApplicationUnit(returnedApplicationUnit,
        productionAppVersion,
        null,
        null,
        ProductionUnit.SCM_PER_MONTH,
        ProductionUnit.KSCM_PER_MONTH);
  }

  @Test
  void createApplicationUnit_productionShortTerm() {
    when(consentLengthService.getConsentLengthDetails(productionAppVersion))
        .thenReturn(ConsentLengthTestUtil.getConsentLengthDetailsForShortTerm(productionAppVersion));

    ApplicationUnit returnedApplicationUnit = applicationUnitService.createApplicationUnit(productionAppVersion);

    verify(applicationUnitRepository, times(1)).save(returnedApplicationUnit);

    assertApplicationUnit(returnedApplicationUnit,
        productionAppVersion,
        null,
        null,
        ProductionUnit.SCM_PER_MONTH,
        ProductionUnit.KSCM_PER_MONTH);
  }

  private void assertApplicationUnit(ApplicationUnit applicationUnit,
                                     ApplicationVersion applicationVersion,
                                     FlareVentUnit flareCategoryUnit,
                                     FlareVentUnit ventCategoryUnit,
                                     ProductionUnit productionOilUnit,
                                     ProductionUnit productionGasUnit) {

    assertThat(applicationUnit)
        .extracting(ApplicationUnit::getApplicationVersion,
            ApplicationUnit::getFlareCategoryUnit,
            ApplicationUnit::getVentCategoryUnit,
            ApplicationUnit::getProductionOilUnit,
            ApplicationUnit::getProductionGasUnit
        )
        .containsExactly(
            applicationVersion,
            flareCategoryUnit,
            ventCategoryUnit,
            productionOilUnit,
            productionGasUnit
        );

  }

}
