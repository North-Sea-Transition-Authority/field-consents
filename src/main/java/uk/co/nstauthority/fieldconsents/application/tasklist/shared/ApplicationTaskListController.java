package uk.co.nstauthority.fieldconsents.application.tasklist.shared;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.application.ApplicationContextService;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.update.request.ApplicationUpdateRequestViewService;
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

  private final ApplicationService applicationService;
  private final ApplicationVersionService applicationVersionService;
  private final ApplicationTaskListService applicationTaskListService;
  private final ApplicationContextService applicationContextService;
  private final ApplicationUpdateService applicationUpdateService;
  private final ApplicationUpdateRequestViewService applicationUpdateRequestViewService;

  ApplicationTaskListController(
      ApplicationService applicationService,
      ApplicationVersionService applicationVersionService,
      ApplicationTaskListService applicationTaskListService,
      ApplicationContextService applicationContextService,
      ApplicationUpdateService applicationUpdateService,
      ApplicationUpdateRequestViewService applicationUpdateRequestViewService
  ) {
    this.applicationService = applicationService;
    this.applicationVersionService = applicationVersionService;
    this.applicationTaskListService = applicationTaskListService;
    this.applicationContextService = applicationContextService;
    this.applicationUpdateService = applicationUpdateService;
    this.applicationUpdateRequestViewService = applicationUpdateRequestViewService;
  }

  @GetMapping
  public ModelAndView getTaskList(@PathVariable Integer applicationId) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    var sections = applicationTaskListService.getAllSections(applicationVersion);
    var applicationType = applicationVersion.getApplication().getType().getDisplayName();
    var applicationContext = applicationContextService.getApplicationContext(applicationVersion);
    var applicationReference = applicationService.getApplicationReference(applicationVersion);

    var modelAndView = new ModelAndView("fcs/application/applicationTaskList")
        .addObject("pageTitle", applicationType + " application")
        .addObject("taskListSections", sections)
        .addObject("applicationContext", applicationContext)
        .addObject("applicationReference", applicationReference)
        .addObject("deleteApplicationUrl", ReverseRouter.route(on(DeleteApplicationController.class)
            .getDeleteApplication(applicationId)));

    if (applicationUpdateService.openApplicationUpdateExists(applicationVersion)) {
      modelAndView.addObject("applicationUpdateRequestView",
          applicationUpdateRequestViewService.getOpenApplicationUpdateRequestView(applicationVersion));
    }

    return modelAndView;
  }
}
