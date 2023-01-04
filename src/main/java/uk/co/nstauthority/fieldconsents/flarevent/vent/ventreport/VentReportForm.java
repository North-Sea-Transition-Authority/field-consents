package uk.co.nstauthority.fieldconsents.flarevent.vent.ventreport;

import com.google.common.annotations.VisibleForTesting;
import java.util.List;

public class VentReportForm {

  List<VentReportMonthForm> ventReportMonthForms;

  @VisibleForTesting
  public VentReportForm() {
  }

  public VentReportForm(List<VentReportMonthForm> ventReportMonthForms) {
    this.ventReportMonthForms = ventReportMonthForms;
  }

  public List<VentReportMonthForm> getVentReportMonthForms() {
    return ventReportMonthForms;
  }

  public void setVentReportMonthForms(
      List<VentReportMonthForm> ventReportMonthForms) {
    this.ventReportMonthForms = ventReportMonthForms;
  }

  public String getStartYear() {
    return this.ventReportMonthForms.isEmpty() ? null : this.ventReportMonthForms.get(0).getYear();
  }

  public String getEndYear() {
    return this.ventReportMonthForms.isEmpty()
        ? null : this.ventReportMonthForms.get(this.ventReportMonthForms.size() - 1).getYear();
  }

}
