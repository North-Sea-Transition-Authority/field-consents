package uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport;

import com.google.common.annotations.VisibleForTesting;
import java.util.List;

public class FlareReportForm {

  List<FlareReportMonthForm> flareReportMonthForms;

  @VisibleForTesting
  public FlareReportForm() {
  }

  public FlareReportForm(List<FlareReportMonthForm> flareReportMonthForms) {
    this.flareReportMonthForms = flareReportMonthForms;
  }

  public List<FlareReportMonthForm> getFlareReportMonthForms() {
    return flareReportMonthForms;
  }

  public void setFlareReportMonthForms(
      List<FlareReportMonthForm> flareReportMonthForms) {
    this.flareReportMonthForms = flareReportMonthForms;
  }

  public String getStartYear() {
    return this.flareReportMonthForms.isEmpty() ? null : this.flareReportMonthForms.get(0).getYear();
  }

  public String getEndYear() {
    return this.flareReportMonthForms.isEmpty()
        ? null : this.flareReportMonthForms.get(this.flareReportMonthForms.size() - 1).getYear();
  }

}
