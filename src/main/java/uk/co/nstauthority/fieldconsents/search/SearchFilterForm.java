package uk.co.nstauthority.fieldconsents.search;

import java.util.List;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterForm;

public class SearchFilterForm extends ApplicationDataFilterForm {

  String submittedYear;
  List<AceFlagStatus> aceFlagStatuses;
  String fieldAssetKey;
  String terminalAssetKey;

  public String getSubmittedYear() {
    return submittedYear;
  }

  public void setSubmittedYear(String submittedYear) {
    this.submittedYear = submittedYear;
  }

  public List<AceFlagStatus> getAceFlagStatuses() {
    return aceFlagStatuses;
  }

  public void setAceFlagStatuses(List<AceFlagStatus> aceFlagStatuses) {
    this.aceFlagStatuses = aceFlagStatuses;
  }

  public String getFieldAssetKey() {
    return fieldAssetKey;
  }

  public void setFieldAssetKey(String fieldKey) {
    this.fieldAssetKey = fieldKey;
  }

  public String getTerminalAssetKey() {
    return terminalAssetKey;
  }

  public void setTerminalAssetKey(String terminalAssetKey) {
    this.terminalAssetKey = terminalAssetKey;
  }
}
