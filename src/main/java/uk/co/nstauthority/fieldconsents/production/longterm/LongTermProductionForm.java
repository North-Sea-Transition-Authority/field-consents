package uk.co.nstauthority.fieldconsents.production.longterm;

import java.util.List;

public class LongTermProductionForm {

  private List<LongTermProductionYearForm> longTermProductionYearForms;

  public LongTermProductionForm() {
  }

  public LongTermProductionForm(List<LongTermProductionYearForm> longTermProductionYearForms) {
    this.longTermProductionYearForms = longTermProductionYearForms;
  }

  public List<LongTermProductionYearForm> getLongTermProductionYearForms() {
    return longTermProductionYearForms;
  }

  public void setLongTermProductionYearForms(List<LongTermProductionYearForm> longTermProductionYearForms) {
    this.longTermProductionYearForms = longTermProductionYearForms;
  }

  public String getStartYear() {
    return this.longTermProductionYearForms.size() > 0 ? this.longTermProductionYearForms.get(0).getYear() : null;
  }

  public String getEndYear() {
    return this.longTermProductionYearForms.size() > 0
        ? this.longTermProductionYearForms.get(this.longTermProductionYearForms.size() - 1).getYear() : null;
  }
}
