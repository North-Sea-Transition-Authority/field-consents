package uk.co.nstauthority.fieldconsents.teams;

import uk.co.nstauthority.fieldconsents.util.enumutil.Displayable;

public enum Role implements Displayable {

  ACCESS_MANAGER(
      "Access manager",
      "Add, remove and update members of this team"
  ),
  VIEWER(
      "Viewer",
      "View applications and consents"
  ),
  EDITOR(
      "Editor",
      "Edit applications for the organisation group"
  ),
  SUBMITTER(
      "Submitter",
      "Pay and submit and edit applications for the organisation group"
  ),
  CREATOR(
      "Creator",
      "Start and edit applications for the organisation group"
  ),
  FINANCE_ADMINISTRATOR(
      "Finance administrator",
      "Pay and submit applications for the organisation group"
  ),
  CONSENT_RECIPIENT(
      "Consent recipient",
      "View consents for the organisation group and will receive a notification upon consent issue"
  ),
  ALLOCATOR(
      "Allocator",
      "View applications and allocate consultation requests to a responder"
  ),
  RESPONDER(
      "Responder",
      "View applications and respond to consent approvals"
  ),
  INDUSTRY_ACCESS_MANAGER(
      "Industry access manager",
      "Create and edit any industry team"
  ),
  DOCUMENT_TEMPLATE_MANAGER(
      "Document template manager",
      "Manage document templates for case output letters and consents"
  ),
  CASE_OFFICER(
      "Case officer",
      "Process applications and run technical reviews and consultations"
  ),
  CASE_MANAGER(
      "Case manager",
      "View all applications and consents and assign case officers"
  ),
  // TODO: FCS-751 split this into two roles
  CONSENTS_AND_AUTHORISATIONS_MANAGER(
      "Consents and authorisation manager",
      "Authorise consents and manage fee periods"
  ),
  TECHNICAL_REVIEWER(
      "Technical reviewer",
      "Perform technical reviews and view all applications and consents"
  ),
  ;

  private final String displayName;
  private final String description;

  Role(String displayName, String description) {
    this.displayName = displayName;
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  @Override
  public String getDisplayName() {
    return displayName;
  }
}
