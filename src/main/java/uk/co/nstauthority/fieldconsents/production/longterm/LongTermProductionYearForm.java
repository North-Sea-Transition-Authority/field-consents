package uk.co.nstauthority.fieldconsents.production.longterm;

import uk.co.fivium.formlibrary.input.DecimalInput;
import uk.co.nstauthority.fieldconsents.production.ProductionRowForm;
import uk.co.nstauthority.fieldconsents.production.ProductionUnit;

public class LongTermProductionYearForm extends ProductionRowForm {

  private String year;

  public LongTermProductionYearForm() {
  }

  public LongTermProductionYearForm(String year,
                                    ProductionUnit oilMinUnit, DecimalInput oilMinValue,
                                    ProductionUnit oilMaxUnit, DecimalInput oilMaxValue,
                                    ProductionUnit gasMinUnit, DecimalInput gasMinValue,
                                    ProductionUnit gasMaxUnit, DecimalInput gasMaxValue) {
    super(oilMinUnit, oilMinValue, oilMaxUnit, oilMaxValue, gasMinUnit, gasMinValue, gasMaxUnit, gasMaxValue);
    this.year = year;
  }

  public String getYear() {
    return year;
  }

  public void setYear(String year) {
    this.year = year;
  }
}
