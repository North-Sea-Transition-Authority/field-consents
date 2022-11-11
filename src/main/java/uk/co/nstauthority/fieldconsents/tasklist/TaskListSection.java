package uk.co.nstauthority.fieldconsents.tasklist;

import java.util.List;

public record TaskListSection(
    String displayName,
    int displayOrder,
    List<TaskListItem> items
) { }
