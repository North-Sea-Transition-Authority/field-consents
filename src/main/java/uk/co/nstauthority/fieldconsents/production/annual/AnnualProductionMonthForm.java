package uk.co.nstauthority.fieldconsents.production.annual;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;
import uk.co.fivium.formlibrary.input.DecimalInput;
import uk.co.nstauthority.fieldconsents.production.ProductionRowForm;

public class AnnualProductionMonthForm extends ProductionRowForm {

  private String month;

  public AnnualProductionMonthForm() {
  }

  public AnnualProductionMonthForm(String month,
                                   DecimalInput oilMinValue,
                                   DecimalInput oilMaxValue,
                                   DecimalInput gasMinValue,
                                   DecimalInput gasMaxValue) {
    super(oilMinValue, oilMaxValue, gasMinValue, gasMaxValue);
    this.month = month;
  }

  public String getMonth() {
    return month;
  }

  public void setMonth(String month) {
    this.month = month;
  }

  public void setMonth(Month month) {
    this.month = month.getDisplayName(TextStyle.FULL, Locale.ENGLISH);
  }
}
