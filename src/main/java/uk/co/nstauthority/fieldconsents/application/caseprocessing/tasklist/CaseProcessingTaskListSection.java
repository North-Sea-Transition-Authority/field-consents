package uk.co.nstauthority.fieldconsents.application.caseprocessing.tasklist;

import uk.co.nstauthority.fieldconsents.util.enumutil.Displayable;

public enum CaseProcessingTaskListSection implements Displayable {

  CASE_TASKS("Case tasks", 10),
  OPTIONAL_CASE_TASKS("Optional case tasks", 20);

  private final String displayName;
  private final int displayOrder;

  CaseProcessingTaskListSection(String displayName, int displayOrder) {
    this.displayName = displayName;
    this.displayOrder = displayOrder;
  }

  @Override
  public String getDisplayName() {
    return displayName;
  }

  @Override
  public int getDisplayOrder() {
    return displayOrder;
  }

}
