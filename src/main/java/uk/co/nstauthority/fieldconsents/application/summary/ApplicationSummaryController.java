package uk.co.nstauthority.fieldconsents.application.summary;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Set;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.application.ApplicationTypeFeature;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.submission.ApplicationSubmissionController;
import uk.co.nstauthority.fieldconsents.application.submission.ApplicationSubmissionService;
import uk.co.nstauthority.fieldconsents.application.tasklist.shared.ApplicationTaskListController;
import uk.co.nstauthority.fieldconsents.authentication.UserDetailService;
import uk.co.nstauthority.fieldconsents.authorisation.ApplicationAccessService;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationPermission;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationStatus;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("applications/{applicationId}/summary")
@HasApplicationStatus(statuses = ApplicationVersionStatus.IN_PROGRESS)
@HasApplicationPermission(permissions = RolePermission.EDIT_FCS_APPLICATIONS)
public class ApplicationSummaryController {

  private final ApplicationVersionService applicationVersionService;

  private final ApplicationSummaryService applicationSummaryService;

  private final ApplicationSubmissionService applicationSubmissionService;

  private final UserDetailService userDetailService;

  private final ApplicationAccessService applicationAccessService;

  ApplicationSummaryController(ApplicationVersionService applicationVersionService,
                               ApplicationSummaryService applicationSummaryService,
                               ApplicationSubmissionService applicationSubmissionService,
                               UserDetailService userDetailService,
                               ApplicationAccessService applicationAccessService) {
    this.applicationVersionService = applicationVersionService;
    this.applicationSummaryService = applicationSummaryService;
    this.applicationSubmissionService = applicationSubmissionService;
    this.userDetailService = userDetailService;
    this.applicationAccessService = applicationAccessService;
  }

  @GetMapping
  public ModelAndView getSummary(@PathVariable Integer applicationId) {

    var user = userDetailService.getUserDetail();

    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    var summarySections = applicationSummaryService.getSummarySections(applicationVersion);

    var wideSummaryDisplay =
        ApplicationTypeFeature.WIDE_SUMMARY_DISPLAY.allowed(applicationVersion.getApplication().getType());

    var userHasSubmitPermission = applicationAccessService.hasApplicationPermission(
        user, applicationVersion, Set.of(RolePermission.SUBMIT_FCS_APPLICATIONS)
    );

    return new ModelAndView("fcs/application/applicationSummary")
        .addObject("pageTitle", "Check your answers before submitting")
        .addObject("summarySections", summarySections)
        .addObject("accordionId", applicationVersion.getId())
        .addObject("wideSummaryDisplay", wideSummaryDisplay)
        .addObject("submitUrl", ReverseRouter.route(on(ApplicationSubmissionController.class)
            .submitApplication(applicationId)))
        .addObject("cancelUrl", ReverseRouter.route(on(ApplicationTaskListController.class)
            .getTaskList(applicationId)))
        .addObject("isSubmittable", applicationSubmissionService.isSubmittable(applicationVersion))
        .addObject("userHasSubmitPermission", userHasSubmitPermission);

  }
}
