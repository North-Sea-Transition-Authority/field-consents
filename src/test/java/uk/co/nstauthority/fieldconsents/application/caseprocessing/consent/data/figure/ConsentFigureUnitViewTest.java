package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentUnit;
import uk.co.nstauthority.fieldconsents.production.ProductionUnit;

class ConsentFigureUnitViewTest {

  @Test
  void fromShortTermOrAnnualProductionApplication() {
    var productionAverageUnit = ProductionUnit.KSCM_PER_DAY;

    assertThat(ConsentFigureUnitView.fromShortTermOrAnnualProductionApplication(productionAverageUnit))
        .isEqualTo(new ConsentFigureUnitView(productionAverageUnit, productionAverageUnit, null));
  }

  @Test
  void fromLongTermProductionApplication() {
    var productionOilUnit = ProductionUnit.KSCM_PER_MONTH;
    var productionGasUnit = ProductionUnit.KSCM_PER_MONTH;

    assertThat(ConsentFigureUnitView.fromLongTermProductionApplication(productionOilUnit, productionGasUnit))
        .isEqualTo(new ConsentFigureUnitView(productionOilUnit, productionGasUnit, null));
  }

  @Test
  void fromEmissionApplication() {
    var emissionUnit = FlareVentUnit.TONNES_PER_DAY;

    assertThat(ConsentFigureUnitView.fromEmissionApplication(emissionUnit))
        .isEqualTo(new ConsentFigureUnitView(null, null, emissionUnit));
  }
}
