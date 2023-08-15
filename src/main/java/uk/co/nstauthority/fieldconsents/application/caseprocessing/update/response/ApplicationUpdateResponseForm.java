package uk.co.nstauthority.fieldconsents.application.caseprocessing.update.response;

import uk.co.fivium.formlibrary.input.StringInput;

public record ApplicationUpdateResponseForm(
    ApplicationUpdateResponseType responseType,
    StringInput otherChangesDescription
) {

  public ApplicationUpdateResponseForm {
    otherChangesDescription = new StringInput("otherChangesDescription", "other changes description");
  }

  public static ApplicationUpdateResponseForm empty() {
    return new ApplicationUpdateResponseForm(null, null);
  }
}
