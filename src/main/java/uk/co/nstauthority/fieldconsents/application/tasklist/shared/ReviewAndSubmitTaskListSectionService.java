package uk.co.nstauthority.fieldconsents.application.tasklist.shared;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.summary.ApplicationSummaryController;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListItem;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListLabel;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListSection;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListSectionService;

@Service
public class ReviewAndSubmitTaskListSectionService implements TaskListSectionService<ApplicationVersion> {

  public static final String REVIEW_SUBMIT_ITEM_NAME = "Review and submit";

  @Override
  public Optional<TaskListSection> getSection(ApplicationVersion applicationVersion) {

    List<TaskListItem> items = new ArrayList<>();

    items.add(
        new TaskListItem(REVIEW_SUBMIT_ITEM_NAME,
            TaskListLabel.NO_LABEL,
            ReverseRouter.route(on(ApplicationSummaryController.class).getSummary(
                applicationVersion.getApplication().getId())))
    );

    return Optional.of(new TaskListSection(REVIEW_SUBMIT_ITEM_NAME, 40, items));
  }
}
