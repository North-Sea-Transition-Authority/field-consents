package uk.co.nstauthority.fieldconsents.email;

public enum GovukNotifyTemplate {

  CASE_ASSIGNED_TO_CASE_OFFICER("520086fe-dfb3-4110-a6b2-c29f35fbc46a"),
  CASE_RELEASED_BY_CASE_OFFICER("4745d25f-7787-4bfe-be94-126d0e7997e4");

  private final String templateId;

  GovukNotifyTemplate(String templateId) {
    this.templateId = templateId;
  }

  public String getTemplateId() {
    return templateId;
  }
}
