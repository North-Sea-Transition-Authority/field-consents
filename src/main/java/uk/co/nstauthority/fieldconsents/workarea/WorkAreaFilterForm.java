package uk.co.nstauthority.fieldconsents.workarea;

import uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterForm;

public class WorkAreaFilterForm extends ApplicationDataFilterForm {

  String assetKey;

  public String getAssetKey() {
    return assetKey;
  }

  public void setAssetKey(String assetKey) {
    this.assetKey = assetKey;
  }
}
