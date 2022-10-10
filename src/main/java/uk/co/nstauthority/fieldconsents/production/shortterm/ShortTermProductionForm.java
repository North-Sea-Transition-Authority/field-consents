package uk.co.nstauthority.fieldconsents.production.shortterm;

import java.util.List;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;

public class ShortTermProductionForm {

  private List<ShortTermProductionMonthForm> shortTermProductionMonthForms;

  public ShortTermProductionForm() {
  }

  public ShortTermProductionForm(List<ShortTermProductionMonthForm> shortTermProductionMonthForms) {
    this.shortTermProductionMonthForms = shortTermProductionMonthForms;
  }

  public String getYear() {
    return shortTermProductionMonthForms.get(0).getYear();
  }

  public String getStartDate() {
    return DateUtils.format(shortTermProductionMonthForms.get(0).getStartDate(), DateUtils.SHORT_DATE);
  }

  public String getEndDate() {
    return DateUtils.format(
        shortTermProductionMonthForms.get(shortTermProductionMonthForms.size() - 1).getEndDate(),
        DateUtils.SHORT_DATE
    );
  }

  public List<ShortTermProductionMonthForm> getShortTermProductionMonthForms() {
    return shortTermProductionMonthForms;
  }

  public void setShortTermProductionMonthForms(
      List<ShortTermProductionMonthForm> shortTermProductionMonthForms) {
    this.shortTermProductionMonthForms = shortTermProductionMonthForms;
  }
}
