package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data;

import uk.co.fivium.formlibrary.input.ThreeFieldDateInput;

public record ConsentDataForm(
    ThreeFieldDateInput consentStartDate,
    ThreeFieldDateInput consentEndDate
) {

  public ConsentDataForm {
    consentStartDate = new ThreeFieldDateInput("consentStartDate", "consent start date");
    consentEndDate = new ThreeFieldDateInput("consentEndDate", "consent end date");
  }

  public static ConsentDataForm empty() {
    return new ConsentDataForm(null, null);
  }

  public static ConsentDataForm from(ConsentData consentData) {
    var form = ConsentDataForm.empty();
    form.consentStartDate().setDate(consentData.getConsentStartDate());
    form.consentEndDate().setDate(consentData.getConsentEndDate());

    return form;
  }

}
