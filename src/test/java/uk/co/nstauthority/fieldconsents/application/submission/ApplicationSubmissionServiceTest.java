package uk.co.nstauthority.fieldconsents.application.submission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.tasklist.TaskListTestUtil.getConsentDetailsTaskListItemsWithLabel;
import static uk.co.nstauthority.fieldconsents.tasklist.TaskListTestUtil.getConsentDetailsTaskListSection;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.tasklist.shared.ApplicationTaskListService;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListLabel;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListSection;

@ExtendWith(MockitoExtension.class)
class ApplicationSubmissionServiceTest {

  @Mock
  private ApplicationTaskListService applicationTaskListService;

  private ApplicationSubmissionService applicationSubmissionService;

  private ApplicationVersion applicationVersion;

  private List<TaskListSection> taskListSections;

  @BeforeEach
  void setUp() {
    applicationSubmissionService = new ApplicationSubmissionService(applicationTaskListService);
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);

    taskListSections = new ArrayList<>();
    when(applicationTaskListService.getAllSections(applicationVersion)).thenReturn(taskListSections);
  }

  @Test
  void isSubmittable_allSectionsCompleted() {
    var taskListItems = getConsentDetailsTaskListItemsWithLabel(
        applicationVersion.getApplication().getId(), TaskListLabel.COMPLETED);

    taskListSections.add(getConsentDetailsTaskListSection(taskListItems));

    assertThat(applicationSubmissionService.isSubmittable(applicationVersion)).isTrue();
  }

  @Test
  void isSubmittable_withSectionsNotCompleted() {
    var taskListItems = getConsentDetailsTaskListItemsWithLabel(
        applicationVersion.getApplication().getId(), TaskListLabel.NOT_STARTED);

    taskListSections.add(getConsentDetailsTaskListSection(taskListItems));

    assertThat(applicationSubmissionService.isSubmittable(applicationVersion)).isFalse();
  }
}