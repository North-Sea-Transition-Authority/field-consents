package uk.co.nstauthority.fieldconsents.tasklist;

import java.util.Optional;

public interface TaskListSectionService<T> {

  Optional<TaskListSection> getSection(T section);

}
