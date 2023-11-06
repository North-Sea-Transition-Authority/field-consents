package uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents;

public enum CaseEventType {

  APPLICATION_CREATED("Application created", "Created by", "Created on", null),
  PAYMENT_COMPLETED("Payment completed", "Paid by", "Paid on", "Payment amount"),
  APPLICATION_SUBMITTED("Application submitted", "Submitted by", "Submitted on", null),
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
  TECHNICAL_REVIEW_COMPLETED("Technical review completed", "Completed by", "Completed on", "Technical review"),
  CONSULTATION_REQUESTED("Consultation requested", "Requested by", "Requested on", "Deadline"),
  CONSULTATION_ASSIGNED("Consultation assigned", "Assigned by", "Assigned on", "Deadline", "Responder"),
  CONSULTATION_REASSIGNED("Consultation re-assigned", "Re-assigned by", "Assigned on", "Deadline", "Responder"),
  CONSULTATION_RESPONDED("Consultation responded", "Responded by", "Responded at", "Responded"),
  FURTHER_INFORMATION_REQUEST_OPENED("Further information requested", "Requested by", "Requested at", "Request text"),
  FURTHER_INFORMATION_REQUEST_CLOSED("Further information responded", "Responded by", "Responded at", "Response text")
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
