package uk.co.nstauthority.fieldconsents.validation;

public class ErrorItem {
  private int displayOrder;

  private String fieldName;

  private String errorMessage;

  public ErrorItem() {
  }

  public ErrorItem(int displayOrder, String fieldName, String errorMessage) {
    this.displayOrder = displayOrder;
    this.fieldName = fieldName;
    this.errorMessage = errorMessage;
  }

  public int getDisplayOrder() {
    return displayOrder;
  }

  public void setDisplayOrder(int displayOrder) {
    this.displayOrder = displayOrder;
  }

  public String getFieldName() {
    return fieldName;
  }

  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }
}
