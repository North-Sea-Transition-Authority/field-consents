package uk.co.nstauthority.fieldconsents.tasklist;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.co.nstauthority.fieldconsents.tasklist.TaskListTestUtil.ADDITIONAL_ASSETS_TASK_LIST_ITEM;
import static uk.co.nstauthority.fieldconsents.tasklist.TaskListTestUtil.CONSENT_DETAILS_DISPLAY_ORDER;
import static uk.co.nstauthority.fieldconsents.tasklist.TaskListTestUtil.CONSENT_DETAILS_SECTION;
import static uk.co.nstauthority.fieldconsents.tasklist.TaskListTestUtil.CONSENT_LENGTH_TASK_LIST_ITEM;
import static uk.co.nstauthority.fieldconsents.tasklist.TaskListTestUtil.TASK_LIST_ITEM_URL;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskListSectionTest {

  private TaskListSection taskListSection;

  @ParameterizedTest
  @MethodSource("getCompleteTasklistLabels")
  void isCompleted_whenItemsCompletedOrUnlabeled(TaskListLabel taskListLabel) {
    var completedTaskListItem = new TaskListItem(CONSENT_LENGTH_TASK_LIST_ITEM, TaskListLabel.COMPLETED, TASK_LIST_ITEM_URL);
    var unlabelledTaskListItem = new TaskListItem(ADDITIONAL_ASSETS_TASK_LIST_ITEM, taskListLabel, TASK_LIST_ITEM_URL);

    taskListSection = new TaskListSection(CONSENT_DETAILS_SECTION, CONSENT_DETAILS_DISPLAY_ORDER, List.of(
        completedTaskListItem, unlabelledTaskListItem
    ));

    assertThat(taskListSection.isCompleted()).isTrue();
  }

  @ParameterizedTest
  @MethodSource("getIncompleteTasklistLabels")
  void isCompleted_whenItemsNotCompleted(TaskListLabel taskListLabel) {
    var completedTaskListItem = new TaskListItem(CONSENT_LENGTH_TASK_LIST_ITEM, TaskListLabel.COMPLETED, TASK_LIST_ITEM_URL);
    var unlabelledTaskListItem = new TaskListItem(ADDITIONAL_ASSETS_TASK_LIST_ITEM, taskListLabel, TASK_LIST_ITEM_URL);

    taskListSection = new TaskListSection(CONSENT_DETAILS_SECTION, CONSENT_DETAILS_DISPLAY_ORDER, List.of(
        completedTaskListItem, unlabelledTaskListItem
    ));

    assertThat(taskListSection.isCompleted()).isFalse();
  }

  private static Stream<Arguments> getCompleteTasklistLabels() {
    return Stream.of(
        Arguments.of(TaskListLabel.NO_LABEL),
        Arguments.of(TaskListLabel.COMPLETED)
    );
  }

  private static Stream<Arguments> getIncompleteTasklistLabels() {
    return Stream.of(
        Arguments.of(TaskListLabel.NOT_STARTED),
        Arguments.of(TaskListLabel.IN_PROGRESS),
        Arguments.of(TaskListLabel.BLOCKED)
    );
  }
}