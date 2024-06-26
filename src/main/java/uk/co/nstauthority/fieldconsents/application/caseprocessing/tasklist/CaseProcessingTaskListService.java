package uk.co.nstauthority.fieldconsents.application.caseprocessing.tasklist;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListItem;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListLabel;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListSection;

@Service
public class CaseProcessingTaskListService {

  private final CaseProcessingActionService caseProcessingActionService;

  CaseProcessingTaskListService(CaseProcessingActionService caseProcessingActionService) {
    this.caseProcessingActionService = caseProcessingActionService;
  }

  public List<TaskListSection> getTaskListSections(ApplicationVersion applicationVersion, ServiceUserDetail user) {
    // TODO: FCS-863 - avoid looking up actions which may not be applicable to the task list
    var actionItems = caseProcessingActionService.getUserActionItems(applicationVersion, user);
    return caseProcessingActionService.groupActionItemsByTaskListSection(actionItems)
        .entrySet()
        .stream()
        .map(entry -> getTaskListSection(applicationVersion, entry.getKey(), entry.getValue()))
        .sorted(Comparator.comparingInt(TaskListSection::displayOrder))
        .toList();
  }

  TaskListSection getTaskListSection(
      ApplicationVersion applicationVersion,
      CaseProcessingTaskListSection taskListSection,
      Collection<CaseProcessingActionItem> actionItems
  ) {
    var displayName = taskListSection.getDisplayName();
    var displayOrder = taskListSection.getDisplayOrder();
    var items = actionItems
        .stream()
        .sorted(Comparator.comparing(CaseProcessingActionItem::getDisplayOrder))
        .map(action -> getTaskListItemFromAction(applicationVersion, action))
        .toList();

    return new TaskListSection(displayName, displayOrder, items);
  }

  TaskListItem getTaskListItemFromAction(ApplicationVersion applicationVersion, CaseProcessingActionItem actionItem) {
    return new TaskListItem(
        actionItem.getDisplayName(),
        TaskListLabel.NO_LABEL,
        actionItem.getActionRedirectUrl(applicationVersion.getApplication().getId())
    );
  }

}
