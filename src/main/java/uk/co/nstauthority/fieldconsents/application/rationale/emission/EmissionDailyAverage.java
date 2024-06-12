package uk.co.nstauthority.fieldconsents.application.rationale.emission;

import java.math.BigDecimal;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentData;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure.ConsentDataLongTermEmissionFigures;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure.ConsentFigureUnitView;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentUnit;

public record EmissionDailyAverage(
    ApplicationType applicationType,
    int year,
    BigDecimal emissionDailyAverage,
    FlareVentUnit emissionAverageUnit
) {

  public static EmissionDailyAverage from(int year, ConsentData consentData, ConsentFigureUnitView consentFigureUnitView) {
    return new EmissionDailyAverage(
        consentData.getApplication().getType(),
        year,
        consentData.getEmissionDailyAverage(),
        consentFigureUnitView.emissionUnit()
    );
  }

  public static EmissionDailyAverage from(
      ConsentDataLongTermEmissionFigures longTermEmissionFigures,
      ConsentFigureUnitView consentFigureUnitView
  ) {
    return new EmissionDailyAverage(
        longTermEmissionFigures.getApplication().getType(),
        longTermEmissionFigures.getYear(),
        longTermEmissionFigures.getDailyAverage(),
        consentFigureUnitView.emissionUnit()
    );
  }
}
