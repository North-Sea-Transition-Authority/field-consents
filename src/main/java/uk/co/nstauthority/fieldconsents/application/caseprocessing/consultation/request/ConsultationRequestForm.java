package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.request;

record ConsultationRequestForm(
    String deadlineDate,
    String deadlineHours,
    String deadlineMinutes
) {

  static ConsultationRequestForm empty() {
    return new ConsultationRequestForm(null, null, null);
  }

}
