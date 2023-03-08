package uk.co.nstauthority.fieldconsents.tasklist;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;

public enum TaskListLabel {
  NOT_STARTED,
  IN_PROGRESS,
  COMPLETED,
  BLOCKED,
  NO_LABEL;

  public static TaskListLabel notStartedOrCompleteByOptional(
      @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<?> optional) {
    return optional.isPresent() ? COMPLETED : NOT_STARTED;
  }

  public static TaskListLabel readyOrCompleteByCollection(Collection<?> collection) {
    return !collection.isEmpty() ? COMPLETED : NOT_STARTED;
  }

  public static <X> TaskListLabel getTaskListLabelFor(
      X applicationVersion,
      Predicate<X> dataCompletePredicate,
      Predicate<X> dataStartedPredicate) {
    if (dataCompletePredicate.test(applicationVersion)) {
      return TaskListLabel.COMPLETED;
    } else if (dataStartedPredicate.test(applicationVersion)) {
      return TaskListLabel.IN_PROGRESS;
    } else {
      return TaskListLabel.NOT_STARTED;
    }
  }
}