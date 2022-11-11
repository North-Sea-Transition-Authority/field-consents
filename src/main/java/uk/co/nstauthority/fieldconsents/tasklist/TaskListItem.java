package uk.co.nstauthority.fieldconsents.tasklist;

public record TaskListItem(
    String displayName,
    TaskListLabel label,
    String labelHint,
    String actionUrl) {

  public TaskListItem(
      String displayName,
      TaskListLabel label,
      String actionUrl) {
    this(displayName, label, null, actionUrl);
  }

}
