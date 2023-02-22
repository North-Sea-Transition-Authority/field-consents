package uk.co.nstauthority.fieldconsents.application.tasklist.shared;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Collections;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.supportinginformation.SupportingInformationController;
import uk.co.nstauthority.fieldconsents.application.supportinginformation.SupportingInformationService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListItem;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListLabel;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListSection;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListSectionService;

@Service
public class AdditionalInformationTaskListSectionService implements TaskListSectionService<ApplicationVersion> {

  private final SupportingInformationService supportingInformationService;

  @Autowired
  public AdditionalInformationTaskListSectionService(SupportingInformationService supportingInformationService) {
    this.supportingInformationService = supportingInformationService;
  }

  @Override
  public Optional<TaskListSection> getSection(ApplicationVersion applicationVersion) {
    var items = Collections.singletonList(getSupportingInformationTaskListItem(applicationVersion));
    return Optional.of(new TaskListSection("Additional information", 30, items));
  }

  private TaskListItem getSupportingInformationTaskListItem(ApplicationVersion applicationVersion) {
    return new TaskListItem("Supporting information",
        TaskListLabel.notStartedOrCompleteByOptional(supportingInformationService.findSupportingInformation(applicationVersion)),
        ReverseRouter.route(on(SupportingInformationController.class).getSupportingInformationForm(
            applicationVersion.getApplication().getId())));
  }
}
