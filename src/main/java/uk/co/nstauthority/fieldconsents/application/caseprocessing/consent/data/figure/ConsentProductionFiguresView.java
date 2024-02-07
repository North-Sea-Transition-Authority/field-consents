package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure;

import static uk.co.nstauthority.fieldconsents.formatting.DecimalFormatUtils.bigDecimalToFormattedString;

public record ConsentProductionFiguresView(
    String minOil,
    String maxOil,
    String minGas,
    String maxGas
) {

  public static ConsentProductionFiguresView from(ConsentProductionFiguresDto consentProductionFiguresDto) {
    return new ConsentProductionFiguresView(
        bigDecimalToFormattedString(consentProductionFiguresDto.minOil()),
        bigDecimalToFormattedString(consentProductionFiguresDto.maxOil()),
        bigDecimalToFormattedString(consentProductionFiguresDto.minGas()),
        bigDecimalToFormattedString(consentProductionFiguresDto.maxGas())
    );
  }
}
