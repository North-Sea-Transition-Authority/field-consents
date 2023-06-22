package uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal;

import uk.co.fivium.formlibrary.input.StringInput;

public class WithdrawalResponseForm {

  private WithdrawalStatus responseStatus;

  private StringInput responseText;

  public WithdrawalResponseForm() {
    this.responseText = new StringInput("responseText", "the reason for rejecting the withdrawal request");
  }

  public WithdrawalStatus getResponseStatus() {
    return responseStatus;
  }

  public StringInput getResponseText() {
    return responseText;
  }

  public void setResponseStatus(WithdrawalStatus responseStatus) {
    this.responseStatus = responseStatus;
  }
}
