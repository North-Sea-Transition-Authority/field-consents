package uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal;

import uk.co.fivium.formlibrary.input.StringInput;

public class WithdrawalRequestForm {

  private final StringInput requestText;

  public WithdrawalRequestForm() {
    this.requestText = new StringInput("requestText", "reason for withdrawal");
  }

  public StringInput getRequestText() {
    return requestText;
  }

  public void setRequestText(String requestText) {
    this.requestText.setInputValue(requestText);
  }
}
