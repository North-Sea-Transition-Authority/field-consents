package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure;

import static uk.co.nstauthority.fieldconsents.formatting.DecimalFormatUtils.bigDecimalToFormattedString;

import uk.co.fivium.formlibrary.input.DecimalInput;

public class ConsentProductionFiguresInput {

  private final DecimalInput minOilInput = new DecimalInput("minOilInput", "minimum oil");
  private final DecimalInput maxOilInput = new DecimalInput("maxOilInput", "maximum oil");
  private final DecimalInput minGasInput = new DecimalInput("minGasInput", "minimum gas");
  private final DecimalInput maxGasInput = new DecimalInput("maxGasInput", "maximum gas");

  public static ConsentProductionFiguresInput withDefaultValuesFromDto(ConsentProductionFiguresDto consentProductionFiguresDto) {
    var consentProductionFiguresInput = new ConsentProductionFiguresInput();
    consentProductionFiguresInput.setInputValuesFromDto(consentProductionFiguresDto);
    return consentProductionFiguresInput;
  }

  public DecimalInput getMinOilInput() {
    return minOilInput;
  }

  public DecimalInput getMaxOilInput() {
    return maxOilInput;
  }

  public DecimalInput getMinGasInput() {
    return minGasInput;
  }

  public DecimalInput getMaxGasInput() {
    return maxGasInput;
  }

  public ConsentProductionFiguresDto getAsDtoOrThrow() {
    return new ConsentProductionFiguresDto(
        minOilInput.getAsBigDecimal().orElseThrow(),
        maxOilInput.getAsBigDecimal().orElseThrow(),
        minGasInput.getAsBigDecimal().orElseThrow(),
        maxGasInput.getAsBigDecimal().orElseThrow()
    );
  }

  public void setInputValuesFromDto(ConsentProductionFiguresDto consentProductionFiguresDto) {
    minOilInput.setInputValue(bigDecimalToFormattedString(consentProductionFiguresDto.minOil()));
    maxOilInput.setInputValue(bigDecimalToFormattedString(consentProductionFiguresDto.maxOil()));
    minGasInput.setInputValue(bigDecimalToFormattedString(consentProductionFiguresDto.minGas()));
    maxGasInput.setInputValue(bigDecimalToFormattedString(consentProductionFiguresDto.maxGas()));
  }
}
