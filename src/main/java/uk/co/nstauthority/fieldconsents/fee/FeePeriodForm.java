package uk.co.nstauthority.fieldconsents.fee;

import java.util.HashMap;
import java.util.Map;
import uk.co.fivium.formlibrary.input.ThreeFieldDateInput;

public class FeePeriodForm {

  private ThreeFieldDateInput startDateInput = new ThreeFieldDateInput("startDateInput", "start date");
  private Map<String, String> feeLineAmountsByMnemonic = new HashMap<>();

  public ThreeFieldDateInput getStartDateInput() {
    return startDateInput;
  }

  public void setStartDateInput(ThreeFieldDateInput startDateInput) {
    this.startDateInput = startDateInput;
  }

  public Map<String, String> getFeeLineAmountsByMnemonic() {
    return feeLineAmountsByMnemonic;
  }

  public void setFeeLineAmountsByMnemonic(Map<String, String> feeLineAmountsByMnemonic) {
    this.feeLineAmountsByMnemonic = feeLineAmountsByMnemonic;
  }
}
