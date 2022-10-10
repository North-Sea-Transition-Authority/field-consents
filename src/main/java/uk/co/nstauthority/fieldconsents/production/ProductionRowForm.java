package uk.co.nstauthority.fieldconsents.production;

import uk.co.fivium.formlibrary.input.DecimalInput;

public class ProductionRowForm {

  private ProductionUnit oilMinUnit;
  private DecimalInput oilMinValue = new DecimalInput("oilMinValue", "Minimum oil");
  private ProductionUnit oilMaxUnit;
  private DecimalInput oilMaxValue = new DecimalInput("oilMaxValue", "Maximum oil");
  private ProductionUnit gasMinUnit;
  private DecimalInput gasMinValue = new DecimalInput("gasMinValue", "Minimum gas");
  private ProductionUnit gasMaxUnit;
  private DecimalInput gasMaxValue = new DecimalInput("gasMaxValue", "Maximum gas");

  public ProductionRowForm() {
  }

  public ProductionRowForm(ProductionUnit oilMinUnit, DecimalInput oilMinValue,
                           ProductionUnit oilMaxUnit, DecimalInput oilMaxValue,
                           ProductionUnit gasMinUnit, DecimalInput gasMinValue,
                           ProductionUnit gasMaxUnit, DecimalInput gasMaxValue) {
    this.oilMinUnit = oilMinUnit;
    this.oilMinValue = oilMinValue;
    this.oilMaxUnit = oilMaxUnit;
    this.oilMaxValue = oilMaxValue;
    this.gasMinUnit = gasMinUnit;
    this.gasMinValue = gasMinValue;
    this.gasMaxUnit = gasMaxUnit;
    this.gasMaxValue = gasMaxValue;
  }

  public ProductionUnit getOilMinUnit() {
    return oilMinUnit;
  }

  public void setOilMinUnit(ProductionUnit oilMinUnit) {
    this.oilMinUnit = oilMinUnit;
  }

  public DecimalInput getOilMinValue() {
    return oilMinValue;
  }

  public void setOilMinValue(DecimalInput oilMinValue) {
    this.oilMinValue = oilMinValue;
  }

  public ProductionUnit getOilMaxUnit() {
    return oilMaxUnit;
  }

  public void setOilMaxUnit(ProductionUnit oilMaxUnit) {
    this.oilMaxUnit = oilMaxUnit;
  }

  public DecimalInput getOilMaxValue() {
    return oilMaxValue;
  }

  public void setOilMaxValue(DecimalInput oilMaxValue) {
    this.oilMaxValue = oilMaxValue;
  }

  public ProductionUnit getGasMinUnit() {
    return gasMinUnit;
  }

  public void setGasMinUnit(ProductionUnit gasMinUnit) {
    this.gasMinUnit = gasMinUnit;
  }

  public DecimalInput getGasMinValue() {
    return gasMinValue;
  }

  public void setGasMinValue(DecimalInput gasMinValue) {
    this.gasMinValue = gasMinValue;
  }

  public ProductionUnit getGasMaxUnit() {
    return gasMaxUnit;
  }

  public void setGasMaxUnit(ProductionUnit gasMaxUnit) {
    this.gasMaxUnit = gasMaxUnit;
  }

  public DecimalInput getGasMaxValue() {
    return gasMaxValue;
  }

  public void setGasMaxValue(DecimalInput gasMaxValue) {
    this.gasMaxValue = gasMaxValue;
  }

  public void setOilUnits(ProductionUnit oilUnit) {
    setOilMinUnit(oilUnit);
    setOilMaxUnit(oilUnit);
  }

  public void setGasUnits(ProductionUnit gasUnit) {
    setGasMinUnit(gasUnit);
    setGasMaxUnit(gasUnit);
  }

}
