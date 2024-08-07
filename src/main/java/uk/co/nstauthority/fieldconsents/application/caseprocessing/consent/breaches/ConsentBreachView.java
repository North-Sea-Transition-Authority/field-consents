package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.breaches;

public record ConsentBreachView(
    String breachText,
    String addedByUser,
    String addedDateTime
) {}
