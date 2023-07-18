package uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents;

public enum CaseEventType {

  APPLICATION_CREATED("Application created", "Created by", "Created on", null),
  APPLICATION_SUBMITTED("Application submitted", "Submitted by", "Submitted on", null),
  APPLICATION_WITHDRAWAL_REQUESTED("Application withdrawal requested", "Requested by", "Requested on", "Withdrawal request"),
  APPLICATION_WITHDRAWAL_RESPONDED("Application withdrawal responded", "Responded by", "Responded on", "Withdrawal response"),
  CASE_OFFICER_ASSIGNED("Application assigned", "Assigned to", "Assigned on", null),
  CASE_NOTE_ADDED("Case note added", "Added by", "Added on", "Case note"),
  TECHNICAL_REVIEW_REQUESTED("Technical review requested", "Requested by", "Requested on", "Request details"),
  TECHNICAL_REVIEW_COMPLETED("Technical review completed", "Completed by", "Completed on", "Technical review");

  private final String caseEventHeader;

  private final String caseEventUserLabel;

  private final String caseEventDateTimeLabel;

  private final String caseEventTextLabel;

  CaseEventType(String caseEventHeader,
                String caseEventUserLabel,
                String caseEventDateTimeLabel,
                String caseEventTextLabel) {
    this.caseEventHeader = caseEventHeader;
    this.caseEventUserLabel = caseEventUserLabel;
    this.caseEventDateTimeLabel = caseEventDateTimeLabel;
    this.caseEventTextLabel = caseEventTextLabel;
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
}
