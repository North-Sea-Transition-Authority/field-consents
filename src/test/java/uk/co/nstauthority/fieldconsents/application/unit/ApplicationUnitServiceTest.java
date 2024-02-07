package uk.co.nstauthority.fieldconsents.application.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthChangeEvent;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthDetails;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthTestUtil;
import uk.co.nstauthority.fieldconsents.flarevent.EmissionCategoryType;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentUnit;
import uk.co.nstauthority.fieldconsents.flarevent.flare.flares.FlareTestUtil;
import uk.co.nstauthority.fieldconsents.flarevent.vent.vents.VentTestUtil;
import uk.co.nstauthority.fieldconsents.production.ProductionUnit;

@ExtendWith(MockitoExtension.class)
class ApplicationUnitServiceTest {

  private static final String MISMATCHED_PRODUCTION_UNITS_EXCEPTION_MESSAGE =
      "Mismatched production units found. Cannot work out the unit for the averages.";

  private static final String MISMATCHED_UNITS_EXCEPTION_MESSAGE =
      "Mismatched %s category unit (%s). Cannot work out the unit for the averages.";

  private static final String UNHANDLED_PRODUCTION_UNITS_EXCEPTION_MESSAGE =
      "Unhandled production units found. Cannot work out the production average conversion factor.";

  @Mock
  private ApplicationUnitRepository applicationUnitRepository;

  @Mock
  private ApplicationVersionService applicationVersionService;

  @Mock
  private ConsentLengthService consentLengthService;

  private ApplicationUnitService applicationUnitService;

  private ApplicationVersion flareAppVersion;

  private ApplicationVersion ventAppVersion;

  private ApplicationVersion productionAppVersion;

  @BeforeEach
  void setUp() {
    applicationUnitService = new ApplicationUnitService(
        applicationUnitRepository,
        applicationVersionService,
        consentLengthService
    );
    flareAppVersion = FlareTestUtil.flareAppVersion;
    ventAppVersion = VentTestUtil.ventAppVersion;
    productionAppVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
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
        null,
        FlareVentUnit.KG_PER_CUBIC_METER,
        FlareVentUnit.MASS_PERCENTAGE,
        null,
        null,
        EmissionCategoryType.CATEGORY_ABC);
  }

  @Test
  void getOrCreateApplicationUnit() {
    when(applicationUnitRepository.findByApplicationVersion(flareAppVersion))
        .thenReturn(Optional.of(new ApplicationUnit(flareAppVersion, FlareVentUnit.TONNES_PER_MONTH,
            null, null, null, FlareVentUnit.KG_PER_CUBIC_METER,
            FlareVentUnit.MASS_PERCENTAGE, null, null,
            EmissionCategoryType.CATEGORY_123)));

    ApplicationUnit returnedApplicationUnit = applicationUnitService.getOrCreateApplicationUnit(flareAppVersion);

    assertApplicationUnit(returnedApplicationUnit,
        flareAppVersion,
        FlareVentUnit.TONNES_PER_MONTH,
        null,
        null,
        null,
        FlareVentUnit.KG_PER_CUBIC_METER,
        FlareVentUnit.MASS_PERCENTAGE,
        null,
        null,
        EmissionCategoryType.CATEGORY_123);
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
  void getFlareGasDensityUnit_notExists() {
    when(applicationUnitRepository.findByApplicationVersion(flareAppVersion)).thenReturn(Optional.empty());
    when(consentLengthService.getConsentLengthDetails(flareAppVersion))
        .thenReturn(ConsentLengthTestUtil.getConsentLengthDetailsForAnnual(flareAppVersion));

    assertThat(applicationUnitService.getFlareGasDensityUnit(flareAppVersion))
        .isEqualTo(FlareVentUnit.KG_PER_CUBIC_METER);
  }

  @Test
  void getFlareGasDensityUnit() {
    ApplicationUnit applicationUnit = new ApplicationUnit();
    applicationUnit.setApplicationVersion(flareAppVersion);
    applicationUnit.setFlareGasDensityUnit(FlareVentUnit.KG_PER_CUBIC_METER);
    when(applicationUnitRepository.findByApplicationVersion(flareAppVersion))
        .thenReturn(Optional.of(applicationUnit));

    assertThat(applicationUnitService.getFlareGasDensityUnit(flareAppVersion))
        .isEqualTo(FlareVentUnit.KG_PER_CUBIC_METER);
  }

  @Test
  void getFlareGasContentUnit_notExists() {
    when(applicationUnitRepository.findByApplicationVersion(flareAppVersion)).thenReturn(Optional.empty());
    when(consentLengthService.getConsentLengthDetails(flareAppVersion))
        .thenReturn(ConsentLengthTestUtil.getConsentLengthDetailsForAnnual(flareAppVersion));

    assertThat(applicationUnitService.getFlareGasContentUnit(flareAppVersion))
        .isEqualTo(FlareVentUnit.MASS_PERCENTAGE);
  }

  @Test
  void getFlareGasContentUnit() {
    ApplicationUnit applicationUnit = new ApplicationUnit();
    applicationUnit.setApplicationVersion(flareAppVersion);
    applicationUnit.setFlareGasContentUnit(FlareVentUnit.MASS_PERCENTAGE);
    when(applicationUnitRepository.findByApplicationVersion(flareAppVersion))
        .thenReturn(Optional.of(applicationUnit));

    assertThat(applicationUnitService.getFlareGasContentUnit(flareAppVersion))
        .isEqualTo(FlareVentUnit.MASS_PERCENTAGE);
  }

  @Test
  void getFlareAverageUnit_shortTerm() {
    when(applicationUnitRepository.findByApplicationVersion(flareAppVersion)).thenReturn(Optional.empty());
    when(consentLengthService.getConsentLengthDetails(flareAppVersion))
        .thenReturn(ConsentLengthTestUtil.getConsentLengthDetailsForShortTerm(flareAppVersion));

    assertThat(applicationUnitService.getFlareAverageUnit(flareAppVersion))
        .isEqualTo(FlareVentUnit.TONNES_PER_DAY);
  }

  @Test
  void getFlareAverageUnit_annual() {
    when(applicationUnitRepository.findByApplicationVersion(flareAppVersion)).thenReturn(Optional.empty());
    when(consentLengthService.getConsentLengthDetails(flareAppVersion))
        .thenReturn(ConsentLengthTestUtil.getConsentLengthDetailsForAnnual(flareAppVersion));

    assertThat(applicationUnitService.getFlareAverageUnit(flareAppVersion))
        .isEqualTo(FlareVentUnit.TONNES_PER_DAY);
  }

  @Test
  void getFlareAverageUnit_longTerm() {
    when(applicationUnitRepository.findByApplicationVersion(flareAppVersion)).thenReturn(Optional.empty());
    when(consentLengthService.getConsentLengthDetails(flareAppVersion))
        .thenReturn(ConsentLengthTestUtil.getConsentLengthDetailsForLongTerm(flareAppVersion));

    assertThat(applicationUnitService.getFlareAverageUnit(flareAppVersion))
        .isEqualTo(FlareVentUnit.TONNES_PER_DAY);
  }

  @Test
  void getFlareAverageUnit_manualTonnesPerMonth() {
    ApplicationUnit applicationUnit = new ApplicationUnit();
    applicationUnit.setApplicationVersion(flareAppVersion);
    applicationUnit.setFlareCategoryUnit(FlareVentUnit.TONNES_PER_MONTH);
    when(applicationUnitRepository.findByApplicationVersion(flareAppVersion))
        .thenReturn(Optional.of(applicationUnit));

    assertThat(applicationUnitService.getFlareAverageUnit(flareAppVersion))
        .isEqualTo(FlareVentUnit.TONNES_PER_DAY);
  }

  @ParameterizedTest
  @EnumSource(value = FlareVentUnit.class, mode = EnumSource.Mode.EXCLUDE, names = {"TONNES_PER_MONTH"})
  void getFlareAverageUnit_manualMismatchUnits(FlareVentUnit flareUnit) {
    ApplicationUnit applicationUnit = new ApplicationUnit();
    applicationUnit.setApplicationVersion(flareAppVersion);
    applicationUnit.setFlareCategoryUnit(flareUnit);
    when(applicationUnitRepository.findByApplicationVersion(flareAppVersion))
        .thenReturn(Optional.of(applicationUnit));

    assertThatThrownBy(() -> applicationUnitService.getFlareAverageUnit(flareAppVersion))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage(MISMATCHED_UNITS_EXCEPTION_MESSAGE.formatted("flare", flareUnit.name()));
  }

  @Test
  void getVentGasDensityUnit_notExists() {
    when(applicationUnitRepository.findByApplicationVersion(ventAppVersion)).thenReturn(Optional.empty());
    when(consentLengthService.getConsentLengthDetails(ventAppVersion))
        .thenReturn(ConsentLengthTestUtil.getConsentLengthDetailsForAnnual(ventAppVersion));

    assertThat(applicationUnitService.getVentGasDensityUnit(ventAppVersion))
        .isEqualTo(FlareVentUnit.KG_PER_CUBIC_METER);
  }

  @Test
  void getVentGasDensityUnit() {
    ApplicationUnit applicationUnit = new ApplicationUnit();
    applicationUnit.setApplicationVersion(ventAppVersion);
    applicationUnit.setVentGasDensityUnit(FlareVentUnit.KG_PER_CUBIC_METER);
    when(applicationUnitRepository.findByApplicationVersion(ventAppVersion))
        .thenReturn(Optional.of(applicationUnit));

    assertThat(applicationUnitService.getVentGasDensityUnit(ventAppVersion))
        .isEqualTo(FlareVentUnit.KG_PER_CUBIC_METER);
  }

  @Test
  void getVentGasContentUnit_notExists() {
    when(applicationUnitRepository.findByApplicationVersion(ventAppVersion)).thenReturn(Optional.empty());
    when(consentLengthService.getConsentLengthDetails(ventAppVersion))
        .thenReturn(ConsentLengthTestUtil.getConsentLengthDetailsForAnnual(ventAppVersion));

    assertThat(applicationUnitService.getVentGasContentUnit(ventAppVersion))
        .isEqualTo(FlareVentUnit.MASS_PERCENTAGE);
  }

  @Test
  void getVentGasContentUnit() {
    ApplicationUnit applicationUnit = new ApplicationUnit();
    applicationUnit.setApplicationVersion(ventAppVersion);
    applicationUnit.setVentGasContentUnit(FlareVentUnit.MASS_PERCENTAGE);
    when(applicationUnitRepository.findByApplicationVersion(ventAppVersion))
        .thenReturn(Optional.of(applicationUnit));

    assertThat(applicationUnitService.getVentGasContentUnit(ventAppVersion))
        .isEqualTo(FlareVentUnit.MASS_PERCENTAGE);
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
  void getVentAverageUnit_shortTerm() {
    when(applicationUnitRepository.findByApplicationVersion(ventAppVersion)).thenReturn(Optional.empty());
    when(consentLengthService.getConsentLengthDetails(ventAppVersion))
        .thenReturn(ConsentLengthTestUtil.getConsentLengthDetailsForShortTerm(ventAppVersion));

    assertThat(applicationUnitService.getVentAverageUnit(ventAppVersion))
        .isEqualTo(FlareVentUnit.TONNES_PER_DAY);
  }

  @Test
  void getVentAverageUnit_annual() {
    when(applicationUnitRepository.findByApplicationVersion(ventAppVersion)).thenReturn(Optional.empty());
    when(consentLengthService.getConsentLengthDetails(ventAppVersion))
        .thenReturn(ConsentLengthTestUtil.getConsentLengthDetailsForAnnual(ventAppVersion));

    assertThat(applicationUnitService.getVentAverageUnit(ventAppVersion))
        .isEqualTo(FlareVentUnit.TONNES_PER_DAY);
  }

  @Test
  void getVentAverageUnit_longTerm() {
    when(applicationUnitRepository.findByApplicationVersion(ventAppVersion)).thenReturn(Optional.empty());
    when(consentLengthService.getConsentLengthDetails(ventAppVersion))
        .thenReturn(ConsentLengthTestUtil.getConsentLengthDetailsForLongTerm(ventAppVersion));

    assertThat(applicationUnitService.getVentAverageUnit(ventAppVersion))
        .isEqualTo(FlareVentUnit.TONNES_PER_DAY);
  }

  @Test
  void getVentAverageUnit_manualTonnesPerMonth() {
    ApplicationUnit applicationUnit = new ApplicationUnit();
    applicationUnit.setApplicationVersion(ventAppVersion);
    applicationUnit.setVentCategoryUnit(FlareVentUnit.TONNES_PER_MONTH);
    when(applicationUnitRepository.findByApplicationVersion(ventAppVersion))
        .thenReturn(Optional.of(applicationUnit));

    assertThat(applicationUnitService.getVentAverageUnit(ventAppVersion))
        .isEqualTo(FlareVentUnit.TONNES_PER_DAY);
  }

  @ParameterizedTest
  @EnumSource(value = FlareVentUnit.class, mode = EnumSource.Mode.EXCLUDE, names = {"TONNES_PER_MONTH"})
  void getVentAverageUnit_manualMismatchUnits(FlareVentUnit ventUnit) {
    ApplicationUnit applicationUnit = new ApplicationUnit();
    applicationUnit.setApplicationVersion(ventAppVersion);
    applicationUnit.setVentCategoryUnit(ventUnit);
    when(applicationUnitRepository.findByApplicationVersion(ventAppVersion))
        .thenReturn(Optional.of(applicationUnit));

    assertThatThrownBy(() -> applicationUnitService.getVentAverageUnit(ventAppVersion))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage(MISMATCHED_UNITS_EXCEPTION_MESSAGE.formatted("vent", ventUnit.name()));
  }

  @Test
  void getEmissionCategoryType_productionAppVersion_unitsNotExists() {
    when(applicationUnitRepository.findByApplicationVersion(productionAppVersion)).thenReturn(Optional.empty());
    when(consentLengthService.getConsentLengthDetails(productionAppVersion))
        .thenReturn(ConsentLengthTestUtil.getConsentLengthDetailsForAnnual(productionAppVersion));
    assertThat(applicationUnitService.getEmissionCategoryType(productionAppVersion))
        .isNull();
  }

  @Test
  void getEmissionCategoryType_flareAppVersion_unitsNotExists() {
    when(applicationUnitRepository.findByApplicationVersion(flareAppVersion)).thenReturn(Optional.empty());
    when(consentLengthService.getConsentLengthDetails(flareAppVersion))
        .thenReturn(ConsentLengthTestUtil.getConsentLengthDetailsForAnnual(flareAppVersion));
    assertThat(applicationUnitService.getEmissionCategoryType(flareAppVersion))
        .isEqualTo(EmissionCategoryType.CATEGORY_ABC);
  }

  @Test
  void getEmissionCategoryType_ventAppVersion_unitsNotExists() {
    when(applicationUnitRepository.findByApplicationVersion(ventAppVersion)).thenReturn(Optional.empty());
    when(consentLengthService.getConsentLengthDetails(ventAppVersion))
        .thenReturn(ConsentLengthTestUtil.getConsentLengthDetailsForAnnual(ventAppVersion));
    assertThat(applicationUnitService.getEmissionCategoryType(ventAppVersion))
        .isEqualTo(EmissionCategoryType.CATEGORY_ABC);
  }

  @Test
  void getEmissionCategoryType_unitsExist() {
    ApplicationUnit applicationUnit = new ApplicationUnit();
    applicationUnit.setApplicationVersion(ventAppVersion);
    applicationUnit.setEmissionCategoryType(EmissionCategoryType.CATEGORY_123);
    when(applicationUnitRepository.findByApplicationVersion(ventAppVersion))
        .thenReturn(Optional.of(applicationUnit));

    assertThat(applicationUnitService.getEmissionCategoryType(ventAppVersion))
        .isEqualTo(EmissionCategoryType.CATEGORY_123);
  }

  @Test
  void getProductionOilUnit_notExists() {
    when(applicationUnitRepository.findByApplicationVersion(productionAppVersion)).thenReturn(Optional.empty());
    when(consentLengthService.getConsentLengthDetails(productionAppVersion))
        .thenReturn(ConsentLengthTestUtil.getConsentLengthDetailsForAnnual(productionAppVersion));
    assertThat(applicationUnitService.getProductionOilUnit(productionAppVersion))
        .isEqualTo(ProductionUnit.KSCM_PER_MONTH);
  }

  @Test
  void getProductionOilUnit() {
    ApplicationUnit applicationUnit = new ApplicationUnit();
    applicationUnit.setApplicationVersion(productionAppVersion);
    applicationUnit.setProductionOilUnit(ProductionUnit.KSCM_PER_MONTH);
    when(applicationUnitRepository.findByApplicationVersion(productionAppVersion))
        .thenReturn(Optional.of(applicationUnit));

    assertThat(applicationUnitService.getProductionOilUnit(productionAppVersion))
        .isEqualTo(ProductionUnit.KSCM_PER_MONTH);
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
  void getProductionAverageUnit_annual() {
    when(applicationUnitRepository.findByApplicationVersion(productionAppVersion)).thenReturn(Optional.empty());
    when(consentLengthService.getConsentLengthDetails(productionAppVersion))
        .thenReturn(ConsentLengthTestUtil.getConsentLengthDetailsForAnnual(productionAppVersion));

    assertThat(applicationUnitService.getProductionAverageUnit(productionAppVersion))
        .isEqualTo(ProductionUnit.KSCM_PER_DAY);
  }

  @Test
  void getProductionAverageUnit_shortTerm() {
    when(applicationUnitRepository.findByApplicationVersion(productionAppVersion)).thenReturn(Optional.empty());
    when(consentLengthService.getConsentLengthDetails(productionAppVersion))
        .thenReturn(ConsentLengthTestUtil.getConsentLengthDetailsForShortTerm(productionAppVersion));

    assertThat(applicationUnitService.getProductionAverageUnit(productionAppVersion))
        .isEqualTo(ProductionUnit.KSCM_PER_DAY);
  }

  @Test
  void getProductionAverageUnit_longTerm() {
    when(applicationUnitRepository.findByApplicationVersion(productionAppVersion)).thenReturn(Optional.empty());
    when(consentLengthService.getConsentLengthDetails(productionAppVersion))
        .thenReturn(ConsentLengthTestUtil.getConsentLengthDetailsForLongTerm(productionAppVersion));

    assertThatThrownBy(() -> applicationUnitService.getProductionAverageUnit(productionAppVersion))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage(MISMATCHED_PRODUCTION_UNITS_EXCEPTION_MESSAGE);
  }

  @Test
  void getProductionAverageUnit_manualScmPerMonth() {
    ApplicationUnit applicationUnit = new ApplicationUnit();
    applicationUnit.setApplicationVersion(productionAppVersion);
    applicationUnit.setProductionOilUnit(ProductionUnit.SCM_PER_MONTH);
    applicationUnit.setProductionGasUnit(ProductionUnit.SCM_PER_MONTH);
    when(applicationUnitRepository.findByApplicationVersion(productionAppVersion))
        .thenReturn(Optional.of(applicationUnit));

    assertThat(applicationUnitService.getProductionAverageUnit(productionAppVersion))
        .isEqualTo(ProductionUnit.SCM_PER_DAY);
  }

  @Test
  void getProductionAverageUnit_manualMigrationCaseScmOilKscmGasPerMonth() {
    ApplicationUnit applicationUnit = new ApplicationUnit();
    applicationUnit.setApplicationVersion(productionAppVersion);
    applicationUnit.setProductionOilUnit(ProductionUnit.SCM_PER_MONTH);
    applicationUnit.setProductionGasUnit(ProductionUnit.KSCM_PER_MONTH);
    when(applicationUnitRepository.findByApplicationVersion(productionAppVersion))
        .thenReturn(Optional.of(applicationUnit));

    assertThat(applicationUnitService.getProductionAverageUnit(productionAppVersion))
        .isEqualTo(ProductionUnit.KSCM_PER_DAY);
  }

  @ParameterizedTest
  @MethodSource("getMismatchProductionUnits")
  void getProductionAverageUnit_manualMismatchUnits(ProductionUnit oilUnit, ProductionUnit gasUnit) {
    ApplicationUnit applicationUnit = new ApplicationUnit();
    applicationUnit.setApplicationVersion(productionAppVersion);
    applicationUnit.setProductionOilUnit(oilUnit);
    applicationUnit.setProductionGasUnit(gasUnit);
    when(applicationUnitRepository.findByApplicationVersion(productionAppVersion))
        .thenReturn(Optional.of(applicationUnit));

    assertThatThrownBy(() -> applicationUnitService.getProductionAverageUnit(productionAppVersion))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage(MISMATCHED_PRODUCTION_UNITS_EXCEPTION_MESSAGE);
  }

  private static Stream<Arguments> getMismatchProductionUnits() {
    return Stream.of(
        Arguments.of(ProductionUnit.SCM_PER_MONTH, ProductionUnit.SCM_PER_DAY),
        Arguments.of(ProductionUnit.SCM_PER_MONTH, ProductionUnit.KSCM_PER_DAY),
        Arguments.of(ProductionUnit.KSCM_PER_MONTH, ProductionUnit.SCM_PER_DAY),
        Arguments.of(ProductionUnit.KSCM_PER_MONTH, ProductionUnit.KSCM_PER_DAY),
        Arguments.of(ProductionUnit.KSCM_PER_MONTH, ProductionUnit.SCM_PER_MONTH),
        Arguments.of(ProductionUnit.SCM_PER_DAY, ProductionUnit.KSCM_PER_DAY),
        Arguments.of(ProductionUnit.SCM_PER_DAY, ProductionUnit.SCM_PER_MONTH),
        Arguments.of(ProductionUnit.SCM_PER_DAY, ProductionUnit.KSCM_PER_MONTH),
        Arguments.of(ProductionUnit.KSCM_PER_DAY, ProductionUnit.SCM_PER_DAY),
        Arguments.of(ProductionUnit.KSCM_PER_DAY, ProductionUnit.SCM_PER_MONTH),
        Arguments.of(ProductionUnit.KSCM_PER_DAY, ProductionUnit.KSCM_PER_MONTH)
    );
  }

  @ParameterizedTest
  @MethodSource("getNonMigrationProductionUnits")
  void getProductionAverageConversionFactor_nonMigrationUnits(ProductionUnit productionUnit, ProductionUnit averageProductionUnit) {
    assertThat(applicationUnitService.getProductionAverageConversionFactor(productionUnit, averageProductionUnit)).isEqualTo(1);
  }

  @Test
  void getProductionAverageConversionFactor_migrationUnits() {
    var productionUnit = ProductionUnit.SCM_PER_MONTH;
    var averageProductionUnit = ProductionUnit.KSCM_PER_DAY;

    assertThat(applicationUnitService.getProductionAverageConversionFactor(productionUnit, averageProductionUnit))
        .isEqualTo(1000);
  }

  @ParameterizedTest
  @MethodSource("getUnhandledProductionUnits")
  void getProductionAverageConversionFactor_mismatchUnits(ProductionUnit productionUnit, ProductionUnit averageProductionUnit) {
    assertThatThrownBy(() -> applicationUnitService.getProductionAverageConversionFactor(productionUnit, averageProductionUnit))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage(UNHANDLED_PRODUCTION_UNITS_EXCEPTION_MESSAGE);
  }

  private static Stream<Arguments> getNonMigrationProductionUnits() {
    return Stream.of(
        Arguments.of(ProductionUnit.SCM_PER_MONTH, ProductionUnit.SCM_PER_DAY),
        Arguments.of(ProductionUnit.KSCM_PER_MONTH, ProductionUnit.KSCM_PER_DAY)
    );
  }

  private static Stream<Arguments> getUnhandledProductionUnits() {
    return Stream.of(
        Arguments.of(ProductionUnit.SCM_PER_MONTH, ProductionUnit.SCM_PER_MONTH),
        Arguments.of(ProductionUnit.SCM_PER_MONTH, ProductionUnit.KSCM_PER_MONTH),
        Arguments.of(ProductionUnit.KSCM_PER_MONTH, ProductionUnit.SCM_PER_DAY),
        Arguments.of(ProductionUnit.KSCM_PER_MONTH, ProductionUnit.SCM_PER_MONTH),
        Arguments.of(ProductionUnit.KSCM_PER_MONTH, ProductionUnit.KSCM_PER_MONTH),
        Arguments.of(ProductionUnit.SCM_PER_DAY, ProductionUnit.SCM_PER_DAY),
        Arguments.of(ProductionUnit.SCM_PER_DAY, ProductionUnit.KSCM_PER_DAY),
        Arguments.of(ProductionUnit.SCM_PER_DAY, ProductionUnit.SCM_PER_MONTH),
        Arguments.of(ProductionUnit.SCM_PER_DAY, ProductionUnit.KSCM_PER_MONTH),
        Arguments.of(ProductionUnit.KSCM_PER_DAY, ProductionUnit.SCM_PER_DAY),
        Arguments.of(ProductionUnit.KSCM_PER_DAY, ProductionUnit.KSCM_PER_DAY),
        Arguments.of(ProductionUnit.KSCM_PER_DAY, ProductionUnit.SCM_PER_MONTH),
        Arguments.of(ProductionUnit.KSCM_PER_DAY, ProductionUnit.KSCM_PER_MONTH)
    );
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
        null,
        FlareVentUnit.KG_PER_CUBIC_METER,
        FlareVentUnit.MASS_PERCENTAGE,
        null,
        null,
        EmissionCategoryType.CATEGORY_ABC);
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
        null,
        FlareVentUnit.KG_PER_CUBIC_METER,
        FlareVentUnit.MASS_PERCENTAGE,
        null,
        null,
        EmissionCategoryType.CATEGORY_ABC);
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
        null,
        null,
        null,
        FlareVentUnit.KG_PER_CUBIC_METER,
        FlareVentUnit.MASS_PERCENTAGE,
        EmissionCategoryType.CATEGORY_ABC);
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
        null,
        null,
        null,
        FlareVentUnit.KG_PER_CUBIC_METER,
        FlareVentUnit.MASS_PERCENTAGE,
        EmissionCategoryType.CATEGORY_ABC);
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
        ProductionUnit.KSCM_PER_DAY,
        ProductionUnit.KSCM_PER_DAY,
        null,
        null,
        null,
        null,
        null);
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
        ProductionUnit.KSCM_PER_MONTH,
        ProductionUnit.KSCM_PER_MONTH,
        null,
        null,
        null,
        null,
        null);
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
        ProductionUnit.KSCM_PER_MONTH,
        ProductionUnit.KSCM_PER_MONTH,
        null,
        null,
        null,
        null,
        null);
  }

  private void assertApplicationUnit(ApplicationUnit applicationUnit,
                                     ApplicationVersion applicationVersion,
                                     FlareVentUnit flareCategoryUnit,
                                     FlareVentUnit ventCategoryUnit,
                                     ProductionUnit productionOilUnit,
                                     ProductionUnit productionGasUnit,
                                     FlareVentUnit flareGasDensity,
                                     FlareVentUnit flareGasContent,
                                     FlareVentUnit ventGasDensity,
                                     FlareVentUnit ventGasContent,
                                     EmissionCategoryType emissionCategoryType) {

    assertThat(applicationUnit)
        .extracting(ApplicationUnit::getApplicationVersion,
            ApplicationUnit::getFlareCategoryUnit,
            ApplicationUnit::getVentCategoryUnit,
            ApplicationUnit::getProductionOilUnit,
            ApplicationUnit::getProductionGasUnit,
            ApplicationUnit::getFlareGasDensityUnit,
            ApplicationUnit::getFlareGasContentUnit,
            ApplicationUnit::getVentGasDensityUnit,
            ApplicationUnit::getVentGasContentUnit,
            ApplicationUnit::getEmissionCategoryType
        )
        .containsExactly(
            applicationVersion,
            flareCategoryUnit,
            ventCategoryUnit,
            productionOilUnit,
            productionGasUnit,
            flareGasDensity,
            flareGasContent,
            ventGasDensity,
            ventGasContent,
            emissionCategoryType
        );

  }

  @Test
  void onApplicationEvent_nonProductionForm() {
    ConsentLengthChangeEvent consentLengthChangeEvent = new ConsentLengthChangeEvent(
        consentLengthService,
        flareAppVersion.getId()
    );
    when(applicationVersionService.getApplicationVersionById(flareAppVersion.getId()))
        .thenReturn(flareAppVersion);

    applicationUnitService.onApplicationEvent(consentLengthChangeEvent);

    verifyNoInteractions(applicationUnitRepository);
  }

  @Test
  void onApplicationEvent_productionForm() {
    ConsentLengthChangeEvent consentLengthChangeEvent = new ConsentLengthChangeEvent(
        consentLengthService,
        productionAppVersion.getId()
    );
    when(applicationVersionService.getApplicationVersionById(productionAppVersion.getId()))
        .thenReturn(productionAppVersion);

    ConsentLengthDetails consentLengthDetails = ConsentLengthTestUtil.getConsentLengthDetailsForShortTerm(productionAppVersion);
    when(consentLengthService.getConsentLengthDetails(productionAppVersion))
        .thenReturn(consentLengthDetails);

    applicationUnitService.onApplicationEvent(consentLengthChangeEvent);

    verify(applicationUnitRepository, times(1)).deleteAllByApplicationVersion(productionAppVersion);
    verify(applicationUnitRepository, times(1)).save(any(ApplicationUnit.class));
    verifyNoMoreInteractions(applicationUnitRepository);
  }
}
