package uk.co.nstauthority.fieldconsents.application.tasklist.flare;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.flarevent.flare.FlareController;
import uk.co.nstauthority.fieldconsents.flarevent.flare.FlareService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListItem;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListLabel;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListSection;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListSectionService;

@Service
public class FlareInformationTaskListSectionService implements TaskListSectionService<ApplicationVersion> {

  private final FlareService flareService;

  FlareInformationTaskListSectionService(FlareService flareService) {
    this.flareService = flareService;
  }

  @Override
  public Optional<TaskListSection> getSection(ApplicationVersion applicationVersion) {

    if (applicationVersion.getApplication().getType() != ApplicationType.FLARE) {
      return Optional.empty();
    }

    var flares = flareService.getFlaresForApplicationVersion(applicationVersion);

    var flaresUrl = flares.isEmpty()
        ? ReverseRouter.route(on(FlareController.class).addFlare(applicationVersion.getApplication().getId()))
        : ReverseRouter.route(on(FlareController.class).viewFlaresSummary(applicationVersion.getApplication().getId()));

    var items = List.of(
        new TaskListItem("Flares",
            TaskListLabel.readyOrCompleteByCollection(flares),
            flaresUrl),
        new TaskListItem("Flare report",
            TaskListLabel.BLOCKED,
            // TODO: FCS-189: Update this with route of Flare report screen
            ReverseRouter.route(on(FlareController.class).viewFlaresSummary(applicationVersion.getApplication().getId())))
    );

    return Optional.of(new TaskListSection("Flare information", 20, items));
  }
}
