package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data;

public record ConsentDataByFieldIdTestImpl(
    Integer fieldId,
    ConsentData consentData
) implements ConsentDataForFieldId {

  @Override
  public Integer getFieldId() {
    return fieldId;
  }

  @Override
  public ConsentData getConsentData() {
    return consentData;
  }
}
