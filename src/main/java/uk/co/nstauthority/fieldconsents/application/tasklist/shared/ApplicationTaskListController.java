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
import uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateRequestViewService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateService;
import uk.co.nstauthority.fieldconsents.application.delete.DeleteApplicationController;
import uk.co.nstauthority.fieldconsents.application.summary.ApplicationSummaryController;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationPermission;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationStatus;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.fieldconsents.workarea.WorkAreaController;

@Controller
@RequestMapping("applications/{applicationId}/task-list")
@HasApplicationStatus(statuses = ApplicationVersionStatus.IN_PROGRESS)
@HasApplicationPermission(permissions = RolePermission.EDIT_FCS_APPLICATIONS)
public class ApplicationTaskListController {

  private final ApplicationVersionService applicationVersionService;

  private final ApplicationTaskListService applicationTaskListService;

  private final ApplicationContextService applicationContextService;

  private final ApplicationUpdateService applicationUpdateService;

  private final ApplicationUpdateRequestViewService applicationUpdateRequestViewService;

  private final ApplicationService applicationService;

  ApplicationTaskListController(ApplicationVersionService applicationVersionService,
                                ApplicationTaskListService applicationTaskListService,
                                ApplicationContextService applicationContextService,
                                ApplicationUpdateService applicationUpdateService,
                                ApplicationUpdateRequestViewService applicationUpdateRequestViewService,
                                ApplicationService applicationService) {
    this.applicationVersionService = applicationVersionService;
    this.applicationTaskListService = applicationTaskListService;
    this.applicationContextService = applicationContextService;
    this.applicationUpdateService = applicationUpdateService;
    this.applicationUpdateRequestViewService = applicationUpdateRequestViewService;
    this.applicationService = applicationService;
  }

  @GetMapping
  public ModelAndView getTaskList(@PathVariable Integer applicationId) {

    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    var sections = applicationTaskListService.getAllSections(applicationVersion);

    var applicationType = applicationVersion.getApplication().getType().getDisplayName();

    var applicationContext = applicationContextService.getApplicationContextJson(applicationVersion);

    var modelAndView = new ModelAndView("fcs/application/applicationTaskList")
        .addObject("pageTitle", applicationType + " application")
        .addObject("taskListSections", sections)
        .addObject("applicationContext", applicationContext)
        .addObject("deleteApplicationUrl", ReverseRouter.route(on(DeleteApplicationController.class)
            .getDeleteApplication(applicationId)));

    String backLinkUrl;
    if (applicationUpdateService.openApplicationUpdateExists(applicationVersion)) {
      modelAndView.addObject("applicationUpdateRequestView",
          applicationUpdateRequestViewService.getOpenApplicationUpdateRequestView(applicationVersion));
      backLinkUrl = ReverseRouter.route(on(ApplicationSummaryController.class).getApplicationSummary(applicationId, null));
    } else {
      backLinkUrl = ReverseRouter.route(on(WorkAreaController.class).getWorkArea(null, null));
    }

    return modelAndView
        .addObject("applicationReference", applicationService.getApplicationReference(applicationVersion))
        .addObject("backLinkUrl", backLinkUrl);
  }
}
