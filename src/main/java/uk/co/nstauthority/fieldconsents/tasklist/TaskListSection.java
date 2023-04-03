package uk.co.nstauthority.fieldconsents.tasklist;

import java.util.EnumSet;
import java.util.List;

public record TaskListSection(
    String displayName,
    int displayOrder,
    List<TaskListItem> items
) {

  public boolean isCompleted() {
    var completedLabels = EnumSet.of(TaskListLabel.NO_LABEL, TaskListLabel.COMPLETED);

    return items()
        .stream()
        .map(TaskListItem::label)
        .allMatch(completedLabels::contains);
  }
}
