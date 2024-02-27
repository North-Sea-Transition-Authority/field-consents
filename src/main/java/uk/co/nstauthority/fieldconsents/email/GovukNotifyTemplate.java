package uk.co.nstauthority.fieldconsents.email;

public enum GovukNotifyTemplate {

  CASE_ASSIGNED_TO_CASE_OFFICER("520086fe-dfb3-4110-a6b2-c29f35fbc46a"),
  CASE_RELEASED_BY_CASE_OFFICER("4745d25f-7787-4bfe-be94-126d0e7997e4"),
  APPLICATION_UPDATE_REQUEST("24157852-9555-40dd-9c37-f3acb3b99f58"),
  APPLICATION_UPDATE_RESPONSE("fc85ab07-8f3b-40b2-bb78-27492b7e9bbc"),
  TECHNICAL_REVIEW_REQUEST("a34ae148-1866-48d6-9078-e99c25588bdc"),
  TECHNICAL_REVIEW_RESPONSE("1211434a-c9b3-4ecb-b579-849e24be2404");

  private final String templateId;

  GovukNotifyTemplate(String templateId) {
    this.templateId = templateId;
  }

  public String getTemplateId() {
    return templateId;
  }
}
