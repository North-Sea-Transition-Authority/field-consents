package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure;

import java.math.BigDecimal;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentData;

public record ConsentProductionFiguresDto(
    BigDecimal minOil,
    BigDecimal maxOil,
    BigDecimal minGas,
    BigDecimal maxGas
) {

  public static ConsentProductionFiguresDto fromShortTermOrAnnualConsentProductionFigures(ConsentData consentData) {
    return new ConsentProductionFiguresDto(
        consentData.getShortTermOrAnnualProductionMinOil(),
        consentData.getShortTermOrAnnualProductionMaxOil(),
        consentData.getShortTermOrAnnualProductionMinGas(),
        consentData.getShortTermOrAnnualProductionMaxGas()
    );
  }

  public static ConsentProductionFiguresDto fromConsentDataLongTermProductionFigures(
      ConsentDataLongTermProductionFigures consentDataLongTermProductionFigures
  ) {
    return new ConsentProductionFiguresDto(
        consentDataLongTermProductionFigures.getMinOil(),
        consentDataLongTermProductionFigures.getMaxOil(),
        consentDataLongTermProductionFigures.getMinGas(),
        consentDataLongTermProductionFigures.getMaxGas()
    );
  }
}
