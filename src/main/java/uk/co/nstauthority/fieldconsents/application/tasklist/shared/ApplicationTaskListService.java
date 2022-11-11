package uk.co.nstauthority.fieldconsents.application.tasklist.shared;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListSection;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListSectionService;

@Service
public class ApplicationTaskListService {
  private final List<TaskListSectionService<ApplicationVersion>> taskListSectionServices;

  ApplicationTaskListService(List<TaskListSectionService<ApplicationVersion>> taskListSectionServices) {
    this.taskListSectionServices = taskListSectionServices;
  }

  public List<TaskListSection> getAllSections(ApplicationVersion applicationVersion) {
    return taskListSectionServices.stream()
        .map(tlss -> tlss.getSection(applicationVersion))
        .flatMap(Optional::stream)
        .sorted(Comparator.comparing(TaskListSection::displayOrder))
        .toList();
  }
}
