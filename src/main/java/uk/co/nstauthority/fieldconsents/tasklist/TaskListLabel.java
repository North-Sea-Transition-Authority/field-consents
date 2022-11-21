package uk.co.nstauthority.fieldconsents.tasklist;

import java.util.Collection;
import java.util.Optional;

public enum TaskListLabel {
  NOT_STARTED,
  IN_PROGRESS,
  COMPLETED,
  BLOCKED;

  public static TaskListLabel notStartedOrCompleteByOptional(
      @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<?> optional) {
    return optional.isPresent() ? COMPLETED : NOT_STARTED;
  }

  public static TaskListLabel readyOrCompleteByCollection(Collection<?> collection) {
    return !collection.isEmpty() ? COMPLETED : NOT_STARTED;
  }
}