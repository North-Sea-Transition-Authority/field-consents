package uk.co.nstauthority.fieldconsents.search;

import java.util.List;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterForm;

public class SearchFilterForm extends ApplicationDataFilterForm {

  List<AceFlagStatus> aceFlagStatuses;
  String fieldAssetKey;
  String terminalAssetKey;
  String consentStartYear;

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

  public String getConsentStartYear() {
    return consentStartYear;
  }

  public void setConsentStartYear(String consentStartYear) {
    this.consentStartYear = consentStartYear;
  }
}
