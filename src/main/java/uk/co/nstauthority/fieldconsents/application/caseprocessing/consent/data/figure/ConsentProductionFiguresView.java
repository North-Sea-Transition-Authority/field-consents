package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure;

import static uk.co.nstauthority.fieldconsents.formatting.DecimalFormatUtils.bigDecimalToFormattedString;

import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentData;

public record ConsentProductionFiguresView(
    String minOil,
    String maxOil,
    String minGas,
    String maxGas
) {

  public static ConsentProductionFiguresView fromShortTermOrAnnualConsentProductionFigures(ConsentData consentData) {
    return new ConsentProductionFiguresView(
        bigDecimalToFormattedString(consentData.getShortTermOrAnnualProductionMinOil()),
        bigDecimalToFormattedString(consentData.getShortTermOrAnnualProductionMaxOil()),
        bigDecimalToFormattedString(consentData.getShortTermOrAnnualProductionMinGas()),
        bigDecimalToFormattedString(consentData.getShortTermOrAnnualProductionMaxGas())
    );
  }

  public static ConsentProductionFiguresView fromConsentProductionLongTermFigures(
      ConsentProductionLongTermFigures consentProductionLongTermFigures
  ) {
    return new ConsentProductionFiguresView(
        bigDecimalToFormattedString(consentProductionLongTermFigures.getMinOil()),
        bigDecimalToFormattedString(consentProductionLongTermFigures.getMaxOil()),
        bigDecimalToFormattedString(consentProductionLongTermFigures.getMinGas()),
        bigDecimalToFormattedString(consentProductionLongTermFigures.getMaxGas())
    );
  }
}
