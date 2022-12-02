package uk.co.nstauthority.fieldconsents.production.longterm;

import uk.co.fivium.formlibrary.input.DecimalInput;
import uk.co.nstauthority.fieldconsents.production.ProductionRowForm;

public class LongTermProductionYearForm extends ProductionRowForm {

  private String year;

  public LongTermProductionYearForm() {
  }

  public LongTermProductionYearForm(String year,
                                    DecimalInput oilMinValue,
                                    DecimalInput oilMaxValue,
                                    DecimalInput gasMinValue,
                                    DecimalInput gasMaxValue) {
    super(oilMinValue, oilMaxValue, gasMinValue, gasMaxValue);
    this.year = year;
  }

  public String getYear() {
    return year;
  }

  public void setYear(String year) {
    this.year = year;
  }
}
