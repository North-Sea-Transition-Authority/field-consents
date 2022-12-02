package uk.co.nstauthority.fieldconsents.flarevent.flare.shortterm;

import java.util.List;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;

public class FlareShortTermForm {

  List<FlareShortTermMonthForm> flareShortTermMonthForms;

  public FlareShortTermForm() {
  }

  public FlareShortTermForm(List<FlareShortTermMonthForm> flareShortTermMonthForms) {
    this.flareShortTermMonthForms = flareShortTermMonthForms;
  }

  public List<FlareShortTermMonthForm> getFlareShortTermMonthForms() {
    return flareShortTermMonthForms;
  }

  public void setFlareShortTermMonthForms(List<FlareShortTermMonthForm> flareShortTermMonthForms) {
    this.flareShortTermMonthForms = flareShortTermMonthForms;
  }

  public String getStartDate() {
    return this.flareShortTermMonthForms.isEmpty()
        ? null
        : DateUtils.format(flareShortTermMonthForms.get(0).getStartDate(), DateUtils.SHORT_DATE);
  }

  public String getEndDate() {
    return this.flareShortTermMonthForms.isEmpty()
        ? null
        : DateUtils.format(flareShortTermMonthForms.get(flareShortTermMonthForms.size() - 1).getEndDate(),
        DateUtils.SHORT_DATE);
  }

}
