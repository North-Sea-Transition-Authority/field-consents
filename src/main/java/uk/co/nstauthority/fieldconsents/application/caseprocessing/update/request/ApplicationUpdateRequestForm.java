package uk.co.nstauthority.fieldconsents.application.caseprocessing.update.request;

import uk.co.fivium.formlibrary.input.StringInput;

public class ApplicationUpdateRequestForm {

  private final StringInput requestText;

  private String deadlineDate;

  private String deadlineHours;

  private String deadlineMinutes;

  public ApplicationUpdateRequestForm() {
    this.requestText = new StringInput("requestText", "application update request details");
  }

  public StringInput getRequestText() {
    return requestText;
  }

  public void setRequestText(String requestText) {
    getRequestText().setInputValue(requestText);
  }

  public String getDeadlineDate() {
    return deadlineDate;
  }

  public void setDeadlineDate(String deadlineDate) {
    this.deadlineDate = deadlineDate;
  }

  public String getDeadlineHours() {
    return deadlineHours;
  }

  public void setDeadlineHours(String deadlineHours) {
    this.deadlineHours = deadlineHours;
  }

  public String getDeadlineMinutes() {
    return deadlineMinutes;
  }

  public void setDeadlineMinutes(String deadlineMinutes) {
    this.deadlineMinutes = deadlineMinutes;
  }
}
