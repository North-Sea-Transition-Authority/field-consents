package uk.co.nstauthority.fieldconsents.application.tasklist.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.tasklist.shared.ReviewAndSubmitTaskListSectionService.REVIEW_SUBMIT_ITEM_NAME;
import static uk.co.nstauthority.fieldconsents.tasklist.TaskListTestUtil.assertTaskListItem;
import static uk.co.nstauthority.fieldconsents.tasklist.TaskListTestUtil.assertTaskListSection;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.summary.ApplicationSummaryController;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListItem;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListLabel;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListSection;


@ExtendWith(MockitoExtension.class)
class ReviewAndSubmitTaskListSectionServiceTest {

  private ReviewAndSubmitTaskListSectionService reviewAndSubmitTaskListSectionService;

  private ApplicationVersion applicationVersion;


  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.FLARE);
    reviewAndSubmitTaskListSectionService = new ReviewAndSubmitTaskListSectionService();
  }

  @Test
  void getSection() {
    Optional<TaskListSection> taskListSectionOptional = reviewAndSubmitTaskListSectionService.getSection(applicationVersion);

    assertThat(taskListSectionOptional).isNotEmpty();
    TaskListSection taskListSection = taskListSectionOptional.get();

    assertTaskListSection(taskListSection, REVIEW_SUBMIT_ITEM_NAME, 40);

    List<TaskListItem> taskListItems = taskListSection.items();

    assertThat(taskListItems).hasSize(1);

    assertTaskListItem(
        taskListItems.get(0),
        REVIEW_SUBMIT_ITEM_NAME,
        TaskListLabel.NO_LABEL,
        ReverseRouter.route(on(ApplicationSummaryController.class).getReviewAndSubmit(
            applicationVersion.getApplication().getId(), null))
    );
  }
}
