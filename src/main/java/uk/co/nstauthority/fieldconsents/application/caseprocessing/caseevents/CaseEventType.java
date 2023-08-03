package uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents;

public enum CaseEventType {

  APPLICATION_CREATED("Application created", "Created by", "Created on", null),
  APPLICATION_SUBMITTED("Application submitted", "Submitted by", "Submitted on", null),
  APPLICATION_WITHDRAWAL_REQUESTED("Application withdrawal requested", "Requested by", "Requested on", "Withdrawal request"),
  APPLICATION_WITHDRAWAL_RESPONDED("Application withdrawal responded", "Responded by", "Responded on", "Withdrawal response"),
  CASE_OFFICER_ASSIGNED("Case officer assigned", "Assigned by", "Assigned on", null, "Case officer"),
  CASE_OFFICER_OWNERSHIP_TAKEN("Ownership taken", "Case officer", "Assigned on", null),
  CASE_OFFICER_OWNERSHIP_RELEASED("Ownership released", "Released by", "Released on", null),
  CASE_NOTE_ADDED("Case note added", "Added by", "Added on", "Case note"),
  TECHNICAL_REVIEW_REQUESTED("Technical review requested", "Requested by",
      "Requested on", "Request details", "Technical reviewer"),
  TECHNICAL_REVIEW_COMPLETED("Technical review completed", "Completed by", "Completed on", "Technical review");

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
