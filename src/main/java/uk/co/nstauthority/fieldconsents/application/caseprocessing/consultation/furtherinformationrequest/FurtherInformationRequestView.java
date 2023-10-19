package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformationrequest;

public record FurtherInformationRequestView(
    String requestedAtTimestamp,
    String requestedByUser,
    String requestText
) {

}
