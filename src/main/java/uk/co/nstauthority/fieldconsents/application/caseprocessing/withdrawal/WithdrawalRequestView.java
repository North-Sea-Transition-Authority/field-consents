package uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal;

public record WithdrawalRequestView(
    String applicationReference,
    String requestedByUser,
    String requestedByDateTime,
    String requestText
) {}
