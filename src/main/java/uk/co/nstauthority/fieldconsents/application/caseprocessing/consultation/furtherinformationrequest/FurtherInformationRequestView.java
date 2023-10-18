package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformationrequest;

public record FurtherInformationRequestView(String requestText) {

  public static FurtherInformationRequestView from(FurtherInformationRequest furtherInformationRequest) {
    return new FurtherInformationRequestView(furtherInformationRequest.getRequestText());
  }

}
