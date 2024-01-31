package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data;

import java.time.LocalDate;
import uk.co.fivium.formlibrary.input.ThreeFieldDateInput;

public record ConsentDataForm(
    ThreeFieldDateInput consentStartDate,
    ThreeFieldDateInput consentEndDate
) {

  public ConsentDataForm {
    consentStartDate = new ThreeFieldDateInput("consentStartDate", "consent start date");
    consentEndDate = new ThreeFieldDateInput("consentEndDate", "consent end date");
  }

  public static ConsentDataForm from(LocalDate startDate, LocalDate endDate) {
    var form = new ConsentDataForm(null, null);

    form.consentStartDate().setDate(startDate);
    form.consentEndDate().setDate(endDate);

    return form;
  }
}
