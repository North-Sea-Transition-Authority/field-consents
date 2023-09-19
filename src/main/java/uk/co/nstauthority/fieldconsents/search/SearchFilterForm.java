package uk.co.nstauthority.fieldconsents.search;

import java.util.List;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterForm;

public class SearchFilterForm extends ApplicationDataFilterForm {

  List<AceFlagStatus> aceFlagStatuses;
  String fieldAssetKey;

  @Override
  public void clearFilter() {
    super.clearFilter();
    aceFlagStatuses = null;
    fieldAssetKey = null;
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
}
