package uk.co.nstauthority.fieldconsents.flarevent.flare.annual;

import java.util.List;

public class FlareAnnualForm {

  List<FlareAnnualMonthForm> flareAnnualMonthForms;

  public FlareAnnualForm() {
  }

  public FlareAnnualForm(List<FlareAnnualMonthForm> flareAnnualMonthForms) {
    this.flareAnnualMonthForms = flareAnnualMonthForms;
  }

  public List<FlareAnnualMonthForm> getFlareAnnualMonthForms() {
    return flareAnnualMonthForms;
  }

  public void setFlareAnnualMonthForms(List<FlareAnnualMonthForm> flareAnnualMonthForms) {
    this.flareAnnualMonthForms = flareAnnualMonthForms;
  }

  public String getYear() {
    return this.flareAnnualMonthForms.isEmpty() ? null : this.flareAnnualMonthForms.get(0).getYear();
  }

}
