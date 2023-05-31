package uk.co.nstauthority.fieldconsents.application.tasklist.shared;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.application.ApplicationContextService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.delete.DeleteApplicationController;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationPermission;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationStatus;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("applications/{applicationId}/task-list")
@HasApplicationStatus(statuses = ApplicationVersionStatus.IN_PROGRESS)
@HasApplicationPermission(permissions = RolePermission.EDIT_FCS_APPLICATIONS)
public class ApplicationTaskListController {

  private final ApplicationVersionService applicationVersionService;

  private final ApplicationTaskListService applicationTaskListService;

  private final ApplicationContextService applicationContextService;

  ApplicationTaskListController(ApplicationVersionService applicationVersionService,
                                ApplicationTaskListService applicationTaskListService,
                                ApplicationContextService applicationContextService) {
    this.applicationVersionService = applicationVersionService;
    this.applicationTaskListService = applicationTaskListService;
    this.applicationContextService = applicationContextService;
  }

  @GetMapping
  public ModelAndView getTaskList(@PathVariable Integer applicationId) {

    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    var sections = applicationTaskListService.getAllSections(applicationVersion);

    var applicationType = applicationVersion.getApplication().getType().getDisplayName();

    var applicationContext = applicationContextService.getApplicationContextJson(applicationVersion);

    return new ModelAndView("fcs/application/applicationTaskList")
        .addObject("pageTitle", applicationType + " application")
        .addObject("taskListSections", sections)
        .addObject("applicationContext", applicationContext)
        .addObject("deleteApplicationUrl", ReverseRouter.route(on(DeleteApplicationController.class)
            .getDeleteApplication(applicationId)));
  }
}
