package uk.co.nstauthority.fieldconsents.flarevent.vent.annual;

import java.util.List;

public class VentAnnualForm {

  List<VentAnnualMonthForm> ventAnnualMonthForms;

  public VentAnnualForm() {
  }

  public VentAnnualForm(List<VentAnnualMonthForm> ventAnnualMonthForms) {
    this.ventAnnualMonthForms = ventAnnualMonthForms;
  }

  public List<VentAnnualMonthForm> getVentAnnualMonthForms() {
    return ventAnnualMonthForms;
  }

  public void setVentAnnualMonthForms(List<VentAnnualMonthForm> ventAnnualMonthForms) {
    this.ventAnnualMonthForms = ventAnnualMonthForms;
  }

  public String getYear() {
    return this.ventAnnualMonthForms.isEmpty() ? null : this.ventAnnualMonthForms.get(0).getYear();
  }

}
