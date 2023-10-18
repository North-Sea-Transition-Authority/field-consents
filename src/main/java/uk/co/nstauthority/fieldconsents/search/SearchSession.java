package uk.co.nstauthority.fieldconsents.search;

import java.io.Serial;
import java.io.Serializable;

public class SearchSession implements Serializable {

  private SearchFilterForm searchFilterForm;

  @Serial
  private static final long serialVersionUID = 1464606383785439779L;

  private boolean searchInvoked;

  public SearchSession(SearchFilterForm searchFilterForm) {
    this.searchFilterForm = searchFilterForm;
  }

  public boolean hasSearchBeenInvoked() {
    return searchInvoked;
  }

  public void clearSession() {
    searchFilterForm.clearFilter();
    searchFilterForm.setAceFlagStatuses(null);
    searchFilterForm.setFieldAssetKey(null);
    searchFilterForm.setTerminalAssetKey(null);
    searchFilterForm.setConsentStartYear(null);
    searchFilterForm.setLicenceReference(null);
    searchInvoked = false;
  }

  public void update(SearchFilterForm searchFilterForm) {
    this.searchFilterForm = searchFilterForm;
    searchInvoked = true;
  }

  public SearchFilterForm getSearchFilterForm() {
    return searchFilterForm;
  }
}
