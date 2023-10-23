package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformation.response;

import jakarta.validation.constraints.NotEmpty;

public record FurtherInformationResponseForm(
    @NotEmpty(message = "Enter a response")
    String responseText
) {

  public static FurtherInformationResponseForm empty() {
    return new FurtherInformationResponseForm(null);
  }

}
