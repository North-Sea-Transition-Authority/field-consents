package uk.co.nstauthority.fieldconsents.application.bulkcaseactions.bulkissueconsents;

import jakarta.servlet.http.HttpSession;
import java.io.Serial;
import java.io.Serializable;
import uk.co.nstauthority.fieldconsents.application.bulkcaseactions.BulkCaseActionSelectedApplicationsForm;

class BulkIssueConsentsSessionContext implements Serializable {

  @Serial
  private static final long serialVersionUID = 7918961448441310103L;
  private static final String SESSION_ATTRIBUTE = "bulkCaseActions-issueConsents";

  private transient HttpSession session;
  private BulkIssueConsentsSearchFiltersForm searchFiltersForm;
  private BulkCaseActionSelectedApplicationsForm selectedApplicationsForm;

  BulkIssueConsentsSessionContext(
      HttpSession session,
      BulkIssueConsentsSearchFiltersForm searchFiltersForm,
      BulkCaseActionSelectedApplicationsForm selectedApplicationsForm
  ) {
    this.session = session;
    this.searchFiltersForm = searchFiltersForm;
    this.selectedApplicationsForm = selectedApplicationsForm;
  }

  static BulkIssueConsentsSessionContext fromSession(HttpSession session) {
    var sessionContext = session.getAttribute(SESSION_ATTRIBUTE);

    if (sessionContext == null) {
      return new BulkIssueConsentsSessionContext(
          session,
          BulkIssueConsentsSearchFiltersForm.empty(),
          new BulkCaseActionSelectedApplicationsForm()
      );
    }

    if (sessionContext instanceof BulkIssueConsentsSessionContext sc) {
      sc.session = session;
      return sc;
    }

    throw new IllegalStateException("Expected session attribute [%s] to be of type [%s]"
        .formatted(SESSION_ATTRIBUTE, BulkIssueConsentsSessionContext.class.getSimpleName()));
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

  BulkIssueConsentsSearchFiltersForm getSearchFiltersForm() {
    return this.searchFiltersForm;
  }

  void setFilters(BulkIssueConsentsSearchFiltersForm filtersForm) {
    this.searchFiltersForm = filtersForm;
    this.save();
  }

  void clearFilters() {
    this.searchFiltersForm = BulkIssueConsentsSearchFiltersForm.empty();
    this.save();
  }

  private void save() {
    this.session.setAttribute(SESSION_ATTRIBUTE, this);
  }

}
