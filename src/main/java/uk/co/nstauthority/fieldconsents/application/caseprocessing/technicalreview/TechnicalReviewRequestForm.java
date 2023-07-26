package uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview;

import uk.co.fivium.formlibrary.input.StringInput;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;

public class TechnicalReviewRequestForm {

  private WebUserAccountId technicalReviewerWuaId;

  private String deadlineDate;

  private String deadlineHours;

  private String deadlineMinutes;

  private final StringInput requestText;

  public TechnicalReviewRequestForm() {
    this.requestText = new StringInput("requestText", "note for the reviewer");
  }

  public WebUserAccountId getTechnicalReviewerWuaId() {
    return technicalReviewerWuaId;
  }

  public void setTechnicalReviewerWuaId(WebUserAccountId technicalReviewerWuaId) {
    this.technicalReviewerWuaId = technicalReviewerWuaId;
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

  public StringInput getRequestText() {
    return requestText;
  }

  public void setRequestText(String requestText) {
    getRequestText().setInputValue(requestText);
  }
}
