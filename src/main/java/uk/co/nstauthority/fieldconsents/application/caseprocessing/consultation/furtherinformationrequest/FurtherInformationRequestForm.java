package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformationrequest;


import jakarta.validation.constraints.NotEmpty;

public record FurtherInformationRequestForm(
    @NotEmpty(message = "Enter what further information you would like to request")
    String requestText
) {

  public static FurtherInformationRequestForm empty() {
    return new FurtherInformationRequestForm(null);
  }

}
