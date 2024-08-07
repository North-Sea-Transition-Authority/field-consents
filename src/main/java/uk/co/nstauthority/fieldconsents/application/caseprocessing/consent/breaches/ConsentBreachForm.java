package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.breaches;

import uk.co.fivium.formlibrary.input.StringInput;

public class ConsentBreachForm {

  private final StringInput consentBreachText;

  public ConsentBreachForm() {
    this.consentBreachText = new StringInput("consentBreachText", "details of the breach");
  }

  public StringInput getConsentBreachText() {
    return consentBreachText;
  }

  public void setConsentBreachText(String consentBreachText) {
    this.consentBreachText.setInputValue(consentBreachText);
  }

  public static ConsentBreachForm from(ConsentBreach consentBreach) {
    var consentBreachForm = new ConsentBreachForm();
    consentBreachForm.setConsentBreachText(consentBreach.getBreachText());
    return consentBreachForm;
  }
}
