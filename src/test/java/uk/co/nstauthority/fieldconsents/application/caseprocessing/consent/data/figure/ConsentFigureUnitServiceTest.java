package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.application.unit.ApplicationUnitService;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentUnit;
import uk.co.nstauthority.fieldconsents.production.ProductionUnit;

@ExtendWith(MockitoExtension.class)
class ConsentFigureUnitServiceTest {

  @Mock
  private ApplicationUnitService applicationUnitService;

  @InjectMocks
  @Spy
  private ConsentFigureUnitService consentFigureUnitService;

  @ParameterizedTest
  @EnumSource(value = ConsentLengthType.class, names = { "SHORT_TERM", "ANNUAL" }, mode = EnumSource.Mode.INCLUDE)
  void getConsentFigureUnitView_applicationTypeIsProductionAndConsentLengthTypeIsShortTermOrAnnual(
      ConsentLengthType consentLengthType
  ) {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);

    var consentFigureUnitView = mock(ConsentFigureUnitView.class);

    doReturn(consentFigureUnitView)
        .when(consentFigureUnitService)
        .getConsentFigureUnitViewForShortTermOrAnnualProductionApplication(applicationVersion);

    assertThat(consentFigureUnitService.getConsentFigureUnitView(applicationVersion, consentLengthType))
        .isEqualTo(consentFigureUnitView);
  }

  @Test
  void getConsentFigureUnitView_applicationTypeIsProductionAndConsentLengthTypeIsLongTerm() {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);

    var consentFigureUnitView = mock(ConsentFigureUnitView.class);

    doReturn(consentFigureUnitView)
        .when(consentFigureUnitService)
        .getConsentFigureUnitViewForLongTermProductionApplication(applicationVersion);

    assertThat(consentFigureUnitService.getConsentFigureUnitView(applicationVersion, ConsentLengthType.LONG_TERM))
        .isEqualTo(consentFigureUnitView);
  }

  @ParameterizedTest
  @EnumSource(value = ApplicationType.class, names = { "FLARE", "VENT" }, mode = EnumSource.Mode.INCLUDE)
  void getConsentFigureUnitView_applicationTypeIsFlareOrVent(ApplicationType applicationType) {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(applicationType);

    var consentFigureUnitView = mock(ConsentFigureUnitView.class);

    doReturn(consentFigureUnitView)
        .when(consentFigureUnitService)
        .getConsentFigureUnitViewForFlareOrVentApplication(applicationVersion);

    assertThat(consentFigureUnitService.getConsentFigureUnitView(applicationVersion, ConsentLengthType.SHORT_TERM))
        .isEqualTo(consentFigureUnitView);
  }

  @Test
  void getConsentFigureUnitViewForShortTermOrAnnualProductionApplication() {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);

    var productionAverageUnit = ProductionUnit.KSCM_PER_DAY;

    when(applicationUnitService.getProductionAverageUnit(applicationVersion)).thenReturn(productionAverageUnit);

    assertThat(consentFigureUnitService.getConsentFigureUnitViewForShortTermOrAnnualProductionApplication(applicationVersion))
        .isEqualTo(ConsentFigureUnitView.fromShortTermOrAnnualProductionApplication(productionAverageUnit));
  }

  @Test
  void getConsentFigureUnitViewForLongTermProductionApplication() {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);

    var productionOilUnit = ProductionUnit.KSCM_PER_MONTH;
    var productionGasUnit = ProductionUnit.KSCM_PER_MONTH;

    when(applicationUnitService.getProductionOilUnit(applicationVersion)).thenReturn(productionOilUnit);
    when(applicationUnitService.getProductionGasUnit(applicationVersion)).thenReturn(productionGasUnit);

    assertThat(consentFigureUnitService.getConsentFigureUnitViewForLongTermProductionApplication(applicationVersion))
        .isEqualTo(ConsentFigureUnitView.fromLongTermProductionApplication(productionOilUnit, productionGasUnit));
  }

  @Test
  void getConsentFigureUnitViewForFlareOrVentApplication_applicationTypeIsFlare() {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.FLARE);

    var flareAverageUnit = FlareVentUnit.TONNES_PER_DAY;

    when(applicationUnitService.getFlareAverageUnit(applicationVersion)).thenReturn(flareAverageUnit);

    assertThat(consentFigureUnitService.getConsentFigureUnitViewForFlareOrVentApplication(applicationVersion))
        .isEqualTo(ConsentFigureUnitView.fromFlareOrVentApplication(flareAverageUnit));
  }

  @Test
  void getConsentFigureUnitViewForFlareOrVentApplication_applicationTypeIsVent() {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.VENT);

    var ventAverageUnit = FlareVentUnit.TONNES_PER_DAY;

    when(applicationUnitService.getVentAverageUnit(applicationVersion)).thenReturn(ventAverageUnit);

    assertThat(consentFigureUnitService.getConsentFigureUnitViewForFlareOrVentApplication(applicationVersion))
        .isEqualTo(ConsentFigureUnitView.fromFlareOrVentApplication(ventAverageUnit));
  }

  @ParameterizedTest
  @EnumSource(value = ApplicationType.class, names = { "FLARE", "VENT" }, mode = EnumSource.Mode.EXCLUDE)
  void getConsentFigureUnitViewForFlareOrVentApplication_applicationTypeIsNotFlareOrVent(ApplicationType applicationType) {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(applicationType);

    assertThatThrownBy(() -> consentFigureUnitService.getConsentFigureUnitViewForFlareOrVentApplication(applicationVersion))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Unexpected ApplicationType: %s".formatted(applicationType));
  }
}
