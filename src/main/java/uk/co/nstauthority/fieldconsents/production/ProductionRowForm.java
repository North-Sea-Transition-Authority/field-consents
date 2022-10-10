package uk.co.nstauthority.fieldconsents.production;

import uk.co.fivium.formlibrary.input.DecimalInput;

public class ProductionRowForm {

  private DecimalInput oilMinValue = new DecimalInput("oilMinValue", "Minimum oil");

  private DecimalInput oilMaxValue = new DecimalInput("oilMaxValue", "Maximum oil");

  private DecimalInput gasMinValue = new DecimalInput("gasMinValue", "Minimum gas");

  private DecimalInput gasMaxValue = new DecimalInput("gasMaxValue", "Maximum gas");

  public ProductionRowForm() {
  }

  public ProductionRowForm(DecimalInput oilMinValue, DecimalInput oilMaxValue, DecimalInput gasMinValue,
                           DecimalInput gasMaxValue) {
    this.oilMinValue = oilMinValue;
    this.oilMaxValue = oilMaxValue;
    this.gasMinValue = gasMinValue;
    this.gasMaxValue = gasMaxValue;
  }

  public DecimalInput getOilMinValue() {
    return oilMinValue;
  }

  public void setOilMinValue(DecimalInput oilMinValue) {
    this.oilMinValue = oilMinValue;
  }

  public DecimalInput getOilMaxValue() {
    return oilMaxValue;
  }

  public void setOilMaxValue(DecimalInput oilMaxValue) {
    this.oilMaxValue = oilMaxValue;
  }

  public DecimalInput getGasMinValue() {
    return gasMinValue;
  }

  public void setGasMinValue(DecimalInput gasMinValue) {
    this.gasMinValue = gasMinValue;
  }

  public DecimalInput getGasMaxValue() {
    return gasMaxValue;
  }

  public void setGasMaxValue(DecimalInput gasMaxValue) {
    this.gasMaxValue = gasMaxValue;
  }
}
