package uk.co.nstauthority.fieldconsents.flarevent;

public enum EmissionCategoryType {
  CATEGORY_ABC,
  CATEGORY_123,
  LEGACY_LONG_TERM;

  public RuntimeException unsupportedOperation() {
    return new RuntimeException("Unsupported operation for %s".formatted(this.name()));
  }
}
