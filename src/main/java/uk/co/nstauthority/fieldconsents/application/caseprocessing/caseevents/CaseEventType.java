package uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents;

public enum CaseEventType {

  APPLICATION_CREATED("Application created", "Created by", "Created on", null),
  PAYMENT_COMPLETED("Payment completed", "Paid by", "Paid on", "Payment amount"),
  APPLICATION_SUBMITTED("Application submitted", "Submitted by", "Submitted on", null),
  APPLICATION_AUTOMATICALLY_SUBMITTED(
      "Application automatically submitted",
      "Automatically submitted by",
      "Automatically submitted on",
      null,
      "Previous revision submitted by"
  ),
  APPLICATION_DELETED("Application deleted", "Deleted by", "Deleted on", null),
  APPLICATION_UPDATE_REQUESTED("Application update requested", "Requested by", "Requested on", "Update request"),
  APPLICATION_UPDATE_STARTED("Application update started", "Started by", "Started on", null),
  APPLICATION_UPDATE_SUBMITTED("Application update submitted", "Submitted by", "Submitted on", "Update response"),
  DRAFT_APPLICATION_UPDATE_DELETED("Draft application update deleted", "Deleted by", "Deleted on", null),
  APPLICATION_WITHDRAWAL_REQUESTED("Application withdrawal requested", "Requested by", "Requested on", "Withdrawal request"),
  APPLICATION_WITHDRAWAL_RESPONDED("Application withdrawal responded", "Responded by", "Responded on", "Withdrawal response"),
  CASE_OFFICER_ASSIGNED("Case officer assigned", "Assigned by", "Assigned on", null, "Case officer"),
  CASE_OFFICER_OWNERSHIP_TAKEN("Ownership taken", "Case officer", "Assigned on", null),
  CASE_OFFICER_OWNERSHIP_RELEASED("Ownership released", "Released by", "Released on", null),
  CASE_NOTE_ADDED("Case note added", "Added by", "Added on", "Case note"),
  TECHNICAL_REVIEW_REQUESTED("Technical review requested", "Requested by",
      "Requested on", "Request details", "Technical reviewer"),
  TECHNICAL_REVIEW_REASSIGNED("Technical review reassigned", "Assigned by", "Assigned on", null, "Technical reviewer"),
  TECHNICAL_REVIEW_COMPLETED("Technical review completed", "Completed by", "Completed on", "Technical review"),
  CONSULTATION_REQUESTED("Consultation requested", "Requested by", "Requested on", "Deadline"),
  CONSULTATION_ASSIGNED("Consultation assigned", "Assigned by", "Assigned on", "Deadline", "Responder"),
  CONSULTATION_REASSIGNED("Consultation reassigned", "Assigned by", "Assigned on", "Deadline", "Responder"),
  CONSULTATION_RESPONDED("Consultation responded", "Responded by", "Responded at", "Responded"),
  FURTHER_INFORMATION_REQUEST_OPENED("Further information requested", "Requested by", "Requested at", "Request text"),
  FURTHER_INFORMATION_REQUEST_CLOSED("Further information responded", "Responded by", "Responded at", "Response text"),
  CAM_ASSIGNED("CAM assigned", "Assigned by", "Assigned on", null, "CAM"),
  CASE_OFFICER_REASSIGNED("Case officer reassigned", "Assigned by", "Assigned on", null, "Case officer"),
  CAM_REASSIGNED("CAM reassigned", "Assigned by", "Assigned on", null, "CAM"),
  APPROVED_FOR_ISSUE("Marked as ready to grant and issue", "Marked by", "Marked on", null, null),
  UNAPPROVED_FOR_ISSUE("Unmarked as ready to grant and issue", "Unmarked by", "Unmarked on", null, null),
  BREACH_RECORDED("Consent breach recorded", "Recorded by", "Recorded on", null, null),
  BREACH_REMOVED("Consent breach removed", "Removed by", "Removed on", null, null)
  ;

  private final String caseEventHeader;
  private final String caseEventUserLabel;
  private final String caseEventDateTimeLabel;
  private final String caseEventTextLabel;
  private final String otherEventUserLabel;

  CaseEventType(String caseEventHeader,
                String caseEventUserLabel,
                String caseEventDateTimeLabel,
                String caseEventTextLabel) {
    this(caseEventHeader, caseEventUserLabel, caseEventDateTimeLabel, caseEventTextLabel, null);
  }

  CaseEventType(String caseEventHeader,
                String caseEventUserLabel,
                String caseEventDateTimeLabel,
                String caseEventTextLabel,
                String otherEventUserLabel) {
    this.caseEventHeader = caseEventHeader;
    this.caseEventUserLabel = caseEventUserLabel;
    this.caseEventDateTimeLabel = caseEventDateTimeLabel;
    this.caseEventTextLabel = caseEventTextLabel;
    this.otherEventUserLabel = otherEventUserLabel;
  }

  public String getCaseEventHeader() {
    return caseEventHeader;
  }

  public String getCaseEventUserLabel() {
    return caseEventUserLabel;
  }

  public String getCaseEventDateTimeLabel() {
    return caseEventDateTimeLabel;
  }

  public String getCaseEventTextLabel() {
    return caseEventTextLabel;
  }

  public String getOtherEventUserLabel() {
    return otherEventUserLabel;
  }
}
