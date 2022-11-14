package uk.co.nstauthority.fieldconsents.application.tasklist.vent;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.flarevent.vent.VentController;
import uk.co.nstauthority.fieldconsents.flarevent.vent.VentService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListItem;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListLabel;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListSection;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListSectionService;

@Service
public class VentInformationTaskListSectionService implements TaskListSectionService<ApplicationVersion> {

  private final VentService ventService;

  public VentInformationTaskListSectionService(VentService ventService) {
    this.ventService = ventService;
  }

  @Override
  public Optional<TaskListSection> getSection(ApplicationVersion applicationVersion) {

    if (applicationVersion.getApplication().getType() != ApplicationType.VENT) {
      return Optional.empty();
    }

    var vents = ventService.getVentsForApplicationVersion(applicationVersion);

    var ventsUrl = vents.isEmpty()
        ? ReverseRouter.route(on(VentController.class).addVent(applicationVersion.getApplication().getId()))
        : ReverseRouter.route(on(VentController.class).viewVentsSummary(applicationVersion.getApplication().getId()));

    var items = List.of(
        new TaskListItem("Vents",
            TaskListLabel.readyOrCompleteByCollection(vents),
            ventsUrl)
    );

    return Optional.of(new TaskListSection("Vent information", 20, items));
  }
}
