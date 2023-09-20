package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation;

import uk.co.nstauthority.fieldconsents.formatting.DateUtils;

public record ConsultationRequestView(String deadline) {

  public static ConsultationRequestView from(Consultation consultation) {
    var formattedDeadline = DateUtils.format(consultation.getRequestDeadline(), DateUtils.DATE_TIME);
    return new ConsultationRequestView(formattedDeadline);
  }

}
