package uk.co.nstauthority.fieldconsents.application.caseprocessing.update.request;

public record ApplicationUpdateRequestView(
    String requestedByUser,
    String requestedByDateTime,
    String requestText,
    String deadlineDate
) {}
