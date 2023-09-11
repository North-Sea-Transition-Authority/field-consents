package uk.co.nstauthority.fieldconsents.application.summary;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.ApplicationCaseProcessingController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.IndustryCaseProcessingController;
import uk.co.nstauthority.fieldconsents.application.tasklist.shared.ApplicationTaskListController;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.ApplicationAccessService;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationPermission;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.fieldconsents.workarea.WorkAreaController;

@Controller
@RequestMapping("applications/{applicationId}")
public class ApplicationSummaryController {

  private final ApplicationVersionService applicationVersionService;

  private final ApplicationSummaryService applicationSummaryService;

  private final ApplicationAccessService applicationAccessService;

  private final ApplicationService applicationService;

  ApplicationSummaryController(ApplicationVersionService applicationVersionService,
                               ApplicationSummaryService applicationSummaryService,
                               ApplicationAccessService applicationAccessService,
                               ApplicationService applicationService) {
    this.applicationVersionService = applicationVersionService;
    this.applicationSummaryService = applicationSummaryService;
    this.applicationAccessService = applicationAccessService;
    this.applicationService = applicationService;
  }

  @GetMapping("summary")
  @HasApplicationPermission(permissions = RolePermission.VIEW_FCS_APPLICATIONS)
  public ModelAndView getApplicationSummary(@PathVariable Integer applicationId,
                                            ServiceUserDetail user) {

    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    var userHasEditPermission = applicationAccessService
        .hasApplicationPermission(user, applicationVersion, RolePermission.EDIT_FCS_APPLICATIONS);

    if (ApplicationVersionStatus.IN_PROGRESS.equals(applicationVersion.getStatus())
        && userHasEditPermission) {
      return applicationVersion.isUpdateVersion()
          ? ReverseRouter.redirect(on(IndustryCaseProcessingController.class).getIndustryCaseProcessing(applicationId, null))
          : ReverseRouter.redirect(on(ApplicationTaskListController.class).getTaskList(applicationId));
    }

    var userHasRegulatorCaseProcessingPermission = applicationAccessService
        .hasApplicationPermission(user, applicationVersion,
            RolePermission.PROCESS_FCS_APPLICATIONS,
            RolePermission.ASSIGN_FCS_APPLICATIONS,
            RolePermission.TECHNICAL_REVIEW_FCS_APPLICATIONS);

    if (ApplicationVersionStatus.SUBMITTED.equals(applicationVersion.getStatus())) {
      if (userHasRegulatorCaseProcessingPermission) {
        return ReverseRouter.redirect(on(ApplicationCaseProcessingController.class)
            .getApplicationCaseProcessing(applicationId, null));
      } else if (userHasEditPermission) {
        return ReverseRouter.redirect(on(IndustryCaseProcessingController.class)
            .getIndustryCaseProcessing(applicationId, null));
      }
    }

    var pageTitle = ApplicationVersionStatus.IN_PROGRESS.equals(applicationVersion.getStatus())
        ? "Application summary"
        : applicationService.generateApplicationReference(applicationVersion);

    return applicationSummaryService.getApplicationSummaryModelAndView(
        applicationVersion,
        "fcs/application/applicationSummary",
        pageTitle,
        ReverseRouter.route(on(WorkAreaController.class).getWorkArea(null, null))
    );
  }
}
