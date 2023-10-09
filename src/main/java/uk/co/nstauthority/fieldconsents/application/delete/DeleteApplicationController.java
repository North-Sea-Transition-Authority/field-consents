package uk.co.nstauthority.fieldconsents.application.delete;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import javax.annotation.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.summary.ApplicationSummaryService;
import uk.co.nstauthority.fieldconsents.application.tasklist.shared.ApplicationTaskListController;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationPermission;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationStatus;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBannerUtil;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.fieldconsents.workarea.WorkAreaController;

@Controller
@RequestMapping("applications/{applicationId}/delete-application")
@HasApplicationStatus(statuses = ApplicationVersionStatus.IN_PROGRESS)
@HasApplicationPermission(permissions = RolePermission.CREATE_FCS_APPLICATIONS)
public class DeleteApplicationController {

  public static final String PAGE_TITLE = "Are you sure you want to delete this draft application?";

  private final ApplicationVersionService applicationVersionService;

  private final ApplicationSummaryService applicationSummaryService;

  public DeleteApplicationController(ApplicationVersionService applicationVersionService,
                                     ApplicationSummaryService applicationSummaryService) {
    this.applicationVersionService = applicationVersionService;
    this.applicationSummaryService = applicationSummaryService;
  }

  @GetMapping
  public ModelAndView getDeleteApplication(@PathVariable Integer applicationId) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    var modelAndView = applicationSummaryService.getApplicationSummaryModelAndView(
        applicationVersion,
        "fcs/application/deleteApplication",
        PAGE_TITLE
    );

    return modelAndView
        .addObject("deleteUrl", ReverseRouter.route(on(DeleteApplicationController.class)
            .deleteApplication(applicationId, null)))
        .addObject("backLinkUrl", ReverseRouter.route(on(ApplicationTaskListController.class)
            .getTaskList(applicationId)));
  }

  @PostMapping
  public ModelAndView deleteApplication(@PathVariable Integer applicationId,
                                        @Nullable RedirectAttributes redirectAttributes) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    applicationVersionService.deleteApplicationVersion(applicationVersion);

    if (redirectAttributes != null) {
      NotificationBannerUtil.addSuccessNotification(
          redirectAttributes,
          "Draft application has been successfully deleted"
      );
    }

    return ReverseRouter.redirect(on(WorkAreaController.class).getWorkArea(null, null));
  }
}
