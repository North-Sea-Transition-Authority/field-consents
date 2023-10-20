package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformation.request;


import jakarta.validation.constraints.NotEmpty;

record FurtherInformationRequestForm(
    @NotEmpty(message = "Enter what further information you would like to request")
    String requestText
) {

  static FurtherInformationRequestForm empty() {
    return new FurtherInformationRequestForm(null);
  }

}
