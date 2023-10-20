package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformation;

public record FurtherInformationView(
    String requestedAtTimestamp,
    String requestedByUser,
    String requestText
) {

}
