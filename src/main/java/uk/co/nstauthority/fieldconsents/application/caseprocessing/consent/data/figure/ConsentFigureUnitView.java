package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure;

import uk.co.nstauthority.fieldconsents.flarevent.FlareVentUnit;
import uk.co.nstauthority.fieldconsents.production.ProductionUnit;

public record ConsentFigureUnitView(
    ProductionUnit productionOilUnit,
    ProductionUnit productionGasUnit,
    FlareVentUnit emissionAverageUnit
) {

  public static ConsentFigureUnitView fromShortTermOrAnnualProductionApplication(ProductionUnit productionAverageUnit) {
    return new ConsentFigureUnitView(productionAverageUnit, productionAverageUnit, null);
  }

  public static ConsentFigureUnitView fromLongTermProductionApplication(
      ProductionUnit productionOilUnit,
      ProductionUnit productionGasUnit
  ) {
    return new ConsentFigureUnitView(productionOilUnit, productionGasUnit, null);
  }

  public static ConsentFigureUnitView fromEmissionApplication(FlareVentUnit emissionAverageUnit) {
    return new ConsentFigureUnitView(null, null, emissionAverageUnit);
  }
}
