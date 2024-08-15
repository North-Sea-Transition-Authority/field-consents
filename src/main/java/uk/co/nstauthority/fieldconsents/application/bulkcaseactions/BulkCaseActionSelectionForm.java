package uk.co.nstauthority.fieldconsents.application.bulkcaseactions;

record BulkCaseActionSelectionForm(String selectedAction) {

  public BulkCaseAction getAction() {
    return BulkCaseAction.valueOf(selectedAction);
  }

}
