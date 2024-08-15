package uk.co.nstauthority.fieldconsents.application.bulkcaseactions.assigncaseofficer;

import jakarta.servlet.http.HttpSession;
import java.io.Serial;
import java.io.Serializable;
import uk.co.nstauthority.fieldconsents.application.bulkcaseactions.BulkCaseActionSelectedApplicationsForm;

class BulkAssignCaseOfficerSessionContext implements Serializable {

  @Serial
  private static final long serialVersionUID = -5986988972169710816L;
  private static final String SESSION_ATTRIBUTE = "bulkCaseActions-assignCaseOfficer";

  private transient HttpSession session;
  private BulkAssignCaseOfficerSearchFiltersForm searchFiltersForm;
  private BulkCaseActionSelectedApplicationsForm selectedApplicationsForm;

  BulkAssignCaseOfficerSessionContext(
      HttpSession session,
      BulkAssignCaseOfficerSearchFiltersForm searchFiltersForm,
      BulkCaseActionSelectedApplicationsForm selectedApplicationsForm
  ) {
    this.session = session;
    this.searchFiltersForm = searchFiltersForm;
    this.selectedApplicationsForm = selectedApplicationsForm;
  }

  static BulkAssignCaseOfficerSessionContext fromSession(HttpSession session) {
    var sessionContext = session.getAttribute(SESSION_ATTRIBUTE);

    if (sessionContext == null) {
      return new BulkAssignCaseOfficerSessionContext(
          session,
          BulkAssignCaseOfficerSearchFiltersForm.empty(),
          new BulkCaseActionSelectedApplicationsForm()
      );
    }

    if (sessionContext instanceof BulkAssignCaseOfficerSessionContext sc) {
      sc.session = session;
      return sc;
    }

    throw new IllegalStateException("Expected session attribute [%s] to be of type [%s]"
        .formatted(SESSION_ATTRIBUTE, BulkAssignCaseOfficerSessionContext.class.getSimpleName()));
  }

  BulkCaseActionSelectedApplicationsForm getSelectedApplicationsForm() {
    return this.selectedApplicationsForm;
  }

  void setSelectedApplicationsForm(BulkCaseActionSelectedApplicationsForm selectedApplicationsForm) {
    this.selectedApplicationsForm = selectedApplicationsForm;
    this.save();
  }

  void clearSelectedApplications() {
    this.selectedApplicationsForm = new BulkCaseActionSelectedApplicationsForm();
    this.save();
  }

  BulkAssignCaseOfficerSearchFiltersForm getSearchFiltersForm() {
    return this.searchFiltersForm;
  }

  void setFilters(BulkAssignCaseOfficerSearchFiltersForm filtersForm) {
    this.searchFiltersForm = filtersForm;
    this.save();
  }

  void clearFilters() {
    this.searchFiltersForm = BulkAssignCaseOfficerSearchFiltersForm.empty();
    this.save();
  }

  private void save() {
    this.session.setAttribute(SESSION_ATTRIBUTE, this);
  }

}