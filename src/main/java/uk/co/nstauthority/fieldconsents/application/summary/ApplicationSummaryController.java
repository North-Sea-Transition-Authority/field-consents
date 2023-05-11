package uk.co.nstauthority.fieldconsents.application.summary;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTypeFeature;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.submission.ApplicationSubmissionController;
import uk.co.nstauthority.fieldconsents.application.submission.ApplicationSubmissionService;
import uk.co.nstauthority.fieldconsents.application.tasklist.shared.ApplicationTaskListController;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.AccessibleByServiceUsers;
import uk.co.nstauthority.fieldconsents.authorisation.ApplicationAccessService;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationPermission;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationStatus;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.fieldconsents.workarea.WorkAreaController;

@Controller
@RequestMapping("applications/{applicationId}")
@AccessibleByServiceUsers
public class ApplicationSummaryController {

  private final ApplicationVersionService applicationVersionService;

  private final ApplicationSummaryService applicationSummaryService;

  private final ApplicationSubmissionService applicationSubmissionService;

  private final ApplicationAccessService applicationAccessService;

  private final ApplicationService applicationService;

  ApplicationSummaryController(ApplicationVersionService applicationVersionService,
                               ApplicationSummaryService applicationSummaryService,
                               ApplicationSubmissionService applicationSubmissionService,
                               ApplicationAccessService applicationAccessService,
                               ApplicationService applicationService) {
    this.applicationVersionService = applicationVersionService;
    this.applicationSummaryService = applicationSummaryService;
    this.applicationSubmissionService = applicationSubmissionService;
    this.applicationAccessService = applicationAccessService;
    this.applicationService = applicationService;
  }

  @GetMapping("review-and-submit")
  @HasApplicationStatus(statuses = ApplicationVersionStatus.IN_PROGRESS)
  @HasApplicationPermission(permissions = RolePermission.EDIT_FCS_APPLICATIONS)
  public ModelAndView getReviewAndSubmit(@PathVariable Integer applicationId,
                                         ServiceUserDetail user) {

    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    var summarySections = applicationSummaryService.getSummarySections(applicationVersion);

    var wideSummaryDisplay =
        ApplicationTypeFeature.WIDE_SUMMARY_DISPLAY.allowed(applicationVersion.getApplication().getType());

    var userHasSubmitPermission = applicationAccessService.hasApplicationPermission(
        user, applicationVersion, RolePermission.SUBMIT_FCS_APPLICATIONS
    );

    return new ModelAndView("fcs/application/reviewAndSubmit")
        .addObject("pageTitle", "Check your answers before submitting")
        .addObject("summarySections", summarySections)
        .addObject("accordionId", applicationVersion.getId())
        .addObject("wideSummaryDisplay", wideSummaryDisplay)
        .addObject("submitUrl", ReverseRouter.route(on(ApplicationSubmissionController.class)
            .submitApplication(applicationId)))
        .addObject("backLinkUrl", ReverseRouter.route(on(ApplicationTaskListController.class)
            .getTaskList(applicationId)))
        .addObject("isSubmittable", applicationSubmissionService.isSubmittable(applicationVersion))
        .addObject("userHasSubmitPermission", userHasSubmitPermission);
  }

  @GetMapping("summary")
  @HasApplicationPermission(permissions = RolePermission.VIEW_FCS_APPLICATIONS)
  public ModelAndView getApplicationSummary(@PathVariable Integer applicationId,
                                            ServiceUserDetail user) {

    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    var userHasEditPermission = applicationAccessService.hasApplicationPermission(
        user, applicationVersion, RolePermission.EDIT_FCS_APPLICATIONS
    );

    if (ApplicationVersionStatus.IN_PROGRESS.equals(applicationVersion.getStatus())
        && userHasEditPermission) {
      return ReverseRouter.redirect(on(ApplicationTaskListController.class).getTaskList(applicationId));
    }

    return getApplicationSummaryModelAndView(applicationVersion);
  }

  private ModelAndView getApplicationSummaryModelAndView(ApplicationVersion applicationVersion) {

    var summarySections = applicationSummaryService.getSummarySections(applicationVersion);

    var wideSummaryDisplay =
        ApplicationTypeFeature.WIDE_SUMMARY_DISPLAY.allowed(applicationVersion.getApplication().getType());

    var pageTitle = ApplicationVersionStatus.IN_PROGRESS.equals(applicationVersion.getStatus())
        ? "Application summary"
        : applicationService.generateApplicationReference(applicationVersion);

    return new ModelAndView("fcs/application/applicationSummary")
        .addObject("pageTitle", pageTitle)
        .addObject("summarySections", summarySections)
        .addObject("accordionId", applicationVersion.getId())
        .addObject("wideSummaryDisplay", wideSummaryDisplay)
        .addObject("backLinkUrl", ReverseRouter.route(on(WorkAreaController.class).getWorkArea(null)));
  }
}
