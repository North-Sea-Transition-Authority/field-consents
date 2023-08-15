package uk.co.nstauthority.fieldconsents.application.caseprocessing.update;

public record ApplicationUpdateRequestView(
    String requestedByUser,
    String requestedByDateTime,
    String requestText,
    String deadlineDate
) {}
