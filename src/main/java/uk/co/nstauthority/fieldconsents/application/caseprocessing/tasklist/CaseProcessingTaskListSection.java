package uk.co.nstauthority.fieldconsents.application.caseprocessing.tasklist;

public enum CaseProcessingTaskListSection {

  CASE_TASKS("Case tasks", 10),
  OPTIONAL_CASE_TASKS("Optional case tasks", 20);

  private final String displayName;
  private final int displayOrder;

  CaseProcessingTaskListSection(String displayName, int displayOrder) {
    this.displayName = displayName;
    this.displayOrder = displayOrder;
  }

  public String getDisplayName() {
    return displayName;
  }

  public int getDisplayOrder() {
    return displayOrder;
  }

}
