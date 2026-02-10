package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation;

import uk.co.nstauthority.fieldconsents.util.enumutil.Displayable;

public interface ConsultationResponseType extends Displayable {

  String getDisplayName();

  String getTextAreaDisplayText();

  String getTextAreaHintText(String applicationReference);

  String getTextAreaInputName();

  boolean isTextAreaInputRequired();

  boolean isSecretaryOfStateDecisionRequired();

}
