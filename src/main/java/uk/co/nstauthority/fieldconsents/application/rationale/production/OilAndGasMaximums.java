package uk.co.nstauthority.fieldconsents.application.rationale.production;

import java.math.BigDecimal;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentData;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure.ConsentDataLongTermProductionFigures;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure.ConsentFigureUnitView;
import uk.co.nstauthority.fieldconsents.production.ProductionUnit;

public record OilAndGasMaximums(
    int year,
    BigDecimal oilMaximum,
    ProductionUnit oilUnit,
    BigDecimal gasMaximum,
    ProductionUnit gasUnit
) {

  public static OilAndGasMaximums from(int year, ConsentData consentData, ConsentFigureUnitView consentFigureUnitView) {
    return new OilAndGasMaximums(
        year,
        consentData.getShortTermOrAnnualProductionMaxOil(),
        consentFigureUnitView.productionOilUnit(),
        consentData.getShortTermOrAnnualProductionMaxGas(),
        consentFigureUnitView.productionGasUnit()
    );
  }

  public static OilAndGasMaximums from(
      ConsentDataLongTermProductionFigures longTermProductionFigures,
      ConsentFigureUnitView consentFigureUnitView
  ) {
    return new OilAndGasMaximums(
        longTermProductionFigures.getYear(),
        longTermProductionFigures.getMaxOil(),
        consentFigureUnitView.productionOilUnit(),
        longTermProductionFigures.getMaxGas(),
        consentFigureUnitView.productionGasUnit()
    );
  }

}
