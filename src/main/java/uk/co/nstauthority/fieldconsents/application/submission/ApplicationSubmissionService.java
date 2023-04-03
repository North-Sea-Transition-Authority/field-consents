package uk.co.nstauthority.fieldconsents.application.submission;

import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.tasklist.shared.ApplicationTaskListService;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListSection;

@Service
public class ApplicationSubmissionService {
  private final ApplicationTaskListService applicationTaskListService;

  public ApplicationSubmissionService(ApplicationTaskListService applicationTaskListService) {
    this.applicationTaskListService = applicationTaskListService;
  }

  public boolean isSubmittable(ApplicationVersion applicationVersion) {
    return applicationTaskListService.getAllSections(applicationVersion)
        .stream()
        .allMatch(TaskListSection::isCompleted);
  }
}
