package uk.co.nstauthority.fieldconsents.validation;

class TestFormWithParentForm extends FieldOrderTestForm {

  private String nonParentField;

  public String getNonParentField() {
    return nonParentField;
  }

  public void setNonParentField(String nonParentField) {
    this.nonParentField = nonParentField;
  }
}
