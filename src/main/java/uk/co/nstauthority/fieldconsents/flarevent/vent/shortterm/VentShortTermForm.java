package uk.co.nstauthority.fieldconsents.flarevent.vent.shortterm;

import java.util.List;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;

public class VentShortTermForm {

  List<VentShortTermMonthForm> ventShortTermMonthForms;

  public VentShortTermForm() {
  }

  public VentShortTermForm(List<VentShortTermMonthForm> ventShortTermMonthForms) {
    this.ventShortTermMonthForms = ventShortTermMonthForms;
  }

  public List<VentShortTermMonthForm> getVentShortTermMonthForms() {
    return ventShortTermMonthForms;
  }

  public void setVentShortTermMonthForms(List<VentShortTermMonthForm> ventShortTermMonthForms) {
    this.ventShortTermMonthForms = ventShortTermMonthForms;
  }

  public String getStartDate() {
    return this.ventShortTermMonthForms.isEmpty()
        ? null
        : DateUtils.format(ventShortTermMonthForms.get(0).getStartDate(), DateUtils.SHORT_DATE);
  }

  public String getEndDate() {
    return this.ventShortTermMonthForms.isEmpty()
        ? null
        : DateUtils.format(ventShortTermMonthForms.get(ventShortTermMonthForms.size() - 1).getEndDate(),
        DateUtils.SHORT_DATE);
  }

}
