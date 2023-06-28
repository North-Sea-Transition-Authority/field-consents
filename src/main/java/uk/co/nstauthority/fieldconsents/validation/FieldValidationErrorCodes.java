package uk.co.nstauthority.fieldconsents.validation;

public enum FieldValidationErrorCodes {

  BEFORE_TODAY(".beforeToday"),
  BEFORE_SOME_DATE_TIME(".beforeDateTime"),
  INVALID(".invalid"),
  REQUIRED(".required");

  private final String code;

  FieldValidationErrorCodes(String code) {
    this.code = code;
  }

  public String getCode() {
    return code;
  }

  public String errorCode(String fieldName) {
    return fieldName + this.getCode();
  }
}
