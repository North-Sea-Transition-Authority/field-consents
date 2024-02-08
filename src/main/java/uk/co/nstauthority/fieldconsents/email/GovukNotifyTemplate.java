package uk.co.nstauthority.fieldconsents.email;

public enum GovukNotifyTemplate {

  CASE_ASSIGNED_TO_CASE_OFFICER("2b76efda-65ee-43c4-8365-9af47582fa78");

  private final String templateId;

  GovukNotifyTemplate(String templateId) {
    this.templateId = templateId;
  }

  public String getTemplateId() {
    return templateId;
  }
}
