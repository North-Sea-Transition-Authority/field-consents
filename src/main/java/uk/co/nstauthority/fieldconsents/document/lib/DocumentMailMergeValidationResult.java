package uk.co.nstauthority.fieldconsents.document.lib;

public record DocumentMailMergeValidationResult(boolean isValid, String errorMessage) {

  public static DocumentMailMergeValidationResult valid() {
    return new DocumentMailMergeValidationResult(true, null);
  }

  public static DocumentMailMergeValidationResult invalid(String errorMessage) {
    return new DocumentMailMergeValidationResult(false, errorMessage);
  }
}
