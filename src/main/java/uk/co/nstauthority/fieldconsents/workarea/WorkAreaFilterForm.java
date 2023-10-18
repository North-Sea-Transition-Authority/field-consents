package uk.co.nstauthority.fieldconsents.workarea;

import uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterForm;

public class WorkAreaFilterForm extends ApplicationDataFilterForm {

  String assetKey;
  Long caseOfficerWuaId;

  public String getAssetKey() {
    return assetKey;
  }

  public void setAssetKey(String assetKey) {
    this.assetKey = assetKey;
  }

  public Long getCaseOfficerWuaId() {
    return caseOfficerWuaId;
  }

  public void setCaseOfficerWuaId(Long caseOfficerWuaId) {
    this.caseOfficerWuaId = caseOfficerWuaId;
  }
}
