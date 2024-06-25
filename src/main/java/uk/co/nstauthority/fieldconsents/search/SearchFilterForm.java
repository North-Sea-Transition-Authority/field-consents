package uk.co.nstauthority.fieldconsents.search;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterForm;

public class SearchFilterForm extends ApplicationDataFilterForm implements Serializable {

  @Serial
  private static final long serialVersionUID = 6383972498362872287L;

  List<AceFlagStatus> aceFlagStatuses;
  String fieldAssetKey;
  String terminalAssetKey;
  String consentStartYear;
  String consentEndYear;

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

  public String getConsentEndYear() {
    return consentEndYear;
  }

  public void setConsentEndYear(String consentEndYear) {
    this.consentEndYear = consentEndYear;
  }
}
