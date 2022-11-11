package uk.co.nstauthority.fieldconsents.application.tasklist.shared;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthController;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListItem;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListLabel;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListSection;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListSectionService;

@Service
public class ConsentDetailsTaskListSectionService implements TaskListSectionService<ApplicationVersion> {

  private final ConsentLengthService consentLengthService;

  ConsentDetailsTaskListSectionService(ConsentLengthService consentLengthService) {
    this.consentLengthService = consentLengthService;
  }

  @Override
  public Optional<TaskListSection> getSection(ApplicationVersion applicationVersion) {

    var items = List.of(
        new TaskListItem("Consent length",
            TaskListLabel.notStartedOrCompleteByOptional(consentLengthService.getConsentLengthDetails(applicationVersion)),
            ReverseRouter.route(on(ConsentLengthController.class).getConsentLengthForm(applicationVersion.getId())))
    );

    return Optional.of(new TaskListSection("Consent details", 10, items));
  }
}
