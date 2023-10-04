package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation;

public interface ConsultationResponseType {

  String getDisplayName();

  String getTextAreaDisplayText();

  String getTextAreaHintText(String applicationReference);

  String getTextAreaInputName();

  boolean isTextAreaInputRequired();

  boolean isSecretaryOfStateDecisionRequired();

}
