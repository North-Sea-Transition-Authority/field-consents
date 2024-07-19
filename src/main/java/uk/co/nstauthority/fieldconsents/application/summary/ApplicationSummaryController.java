package uk.co.nstauthority.fieldconsents.application.summary;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus.IN_PROGRESS;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.ApplicationCaseProcessingController.REGULATOR_PROCESSING_REQUIRED_PERMISSIONS;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.ConsulteeCaseProcessingController.CONSULTEE_PROCESSING_REQUIRED_PERMISSIONS;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.IndustryCaseProcessingController.INDUSTRY_PROCESSING_REQUIRED_PERMISSIONS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.VIEW_FCS_APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.VIEW_FCS_CONSENTS;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.ApplicationCaseProcessingController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.ConsulteeCaseProcessingController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.IndustryCaseProcessingController;
import uk.co.nstauthority.fieldconsents.application.tasklist.shared.ApplicationTaskListController;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.ApplicationAccessService;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationPermission;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.TeamService;

@Controller
@RequestMapping("applications/{applicationId}")
public class ApplicationSummaryController {

  private final ApplicationVersionService applicationVersionService;
  private final ApplicationAccessService applicationAccessService;
  private final TeamService teamService;

  ApplicationSummaryController(
      ApplicationVersionService applicationVersionService,
      ApplicationAccessService applicationAccessService,
      TeamService teamService
  ) {
    this.applicationVersionService = applicationVersionService;
    this.applicationAccessService = applicationAccessService;
    this.teamService = teamService;
  }

  @GetMapping("summary")
  @HasApplicationPermission(permissions = { VIEW_FCS_APPLICATIONS, VIEW_FCS_CONSENTS })
  public ModelAndView getApplicationSummary(@PathVariable Integer applicationId, ServiceUserDetail user) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    return getCaseProcessingModelAndView(applicationVersion, user);
  }

  private ModelAndView getCaseProcessingModelAndView(ApplicationVersion applicationVersion, ServiceUserDetail user) {
    var applicationId = applicationVersion.getApplication().getId();
    var isFirstVersion = applicationVersion.isFirstVersion();

    if (isRegulatorCaseProcessingUser(user, applicationVersion)) {
      return ReverseRouter.redirect(on(ApplicationCaseProcessingController.class)
          .caseProcessing(applicationId, null, null, null));
    }

    if (isConsulteeCaseProcessingUser(user, applicationVersion)) {
      return ReverseRouter.redirect(on(ConsulteeCaseProcessingController.class)
          .caseProcessing(applicationId, null, null, null));
    }

    // Industry users can access the task-list of applications in progress,
    // otherwise they'll be redirected to the case processing screen
    if (isIndustryCaseProcessingUser(user, applicationVersion)) {
      var applicationStatus = applicationVersion.getStatus();
      if (isFirstVersion && IN_PROGRESS.equals(applicationStatus)) {
        return ReverseRouter.redirect(on(ApplicationTaskListController.class).getTaskList(applicationId, null));
      }
      return ReverseRouter.redirect(on(IndustryCaseProcessingController.class)
          .getIndustryCaseProcessing(applicationId, null, null, null));
    }

    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported user type");
  }

  // The list of role permissions used here should 'always' match the list
  // used in @HasApplicationPermission on ApplicationCaseProcessingController
  private boolean isRegulatorCaseProcessingUser(ServiceUserDetail userDetail, ApplicationVersion applicationVersion) {
    return teamService.isRegulatorUser(userDetail)
        && applicationAccessService.hasApplicationPermission(
            userDetail,
            applicationVersion,
            REGULATOR_PROCESSING_REQUIRED_PERMISSIONS
      );
  }

  // The list of role permissions used here should 'always' match the list
  // used in @HasApplicationPermission on ConsulteeCaseProcessingController
  private boolean isConsulteeCaseProcessingUser(ServiceUserDetail userDetail, ApplicationVersion applicationVersion) {
    return teamService.isConsulteeUser(userDetail)
        && applicationAccessService.hasApplicationPermission(
            userDetail,
            applicationVersion,
            CONSULTEE_PROCESSING_REQUIRED_PERMISSIONS
    );
  }

  // The list of role permissions used here should 'always' match the list
  // used in @HasApplicationPermission on IndustryCaseProcessingController
  private boolean isIndustryCaseProcessingUser(ServiceUserDetail userDetail, ApplicationVersion applicationVersion) {
    return teamService.isIndustryUser(userDetail)
        && applicationAccessService.hasApplicationPermission(
            userDetail,
            applicationVersion,
            INDUSTRY_PROCESSING_REQUIRED_PERMISSIONS
    );
  }
}
