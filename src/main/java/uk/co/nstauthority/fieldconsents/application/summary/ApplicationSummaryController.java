package uk.co.nstauthority.fieldconsents.application.summary;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.ALLOCATE_CONSULTATION;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.ASSIGN_FCS_APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.EDIT_FCS_APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.PROCESS_FCS_APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.RESPOND_TO_CONSULTATION;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.SUBMIT_FCS_APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.TECHNICAL_REVIEW_FCS_APPLICATIONS;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.ApplicationCaseProcessingController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.ConsulteeCaseProcessingController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.IndustryCaseProcessingController;
import uk.co.nstauthority.fieldconsents.application.tasklist.shared.ApplicationTaskListController;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.ApplicationAccessService;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationPermission;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("applications/{applicationId}")
public class ApplicationSummaryController {

  private final ApplicationVersionService applicationVersionService;
  private final ApplicationSummaryService applicationSummaryService;
  private final ApplicationAccessService applicationAccessService;
  private final ApplicationService applicationService;

  ApplicationSummaryController(
      ApplicationVersionService applicationVersionService,
      ApplicationSummaryService applicationSummaryService,
      ApplicationAccessService applicationAccessService,
      ApplicationService applicationService
  ) {
    this.applicationVersionService = applicationVersionService;
    this.applicationSummaryService = applicationSummaryService;
    this.applicationAccessService = applicationAccessService;
    this.applicationService = applicationService;
  }

  @GetMapping("summary")
  @HasApplicationPermission(permissions = RolePermission.VIEW_FCS_APPLICATIONS)
  public ModelAndView getApplicationSummary(@PathVariable Integer applicationId, ServiceUserDetail user) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    return switch (applicationVersion.getStatus()) {
      case IN_PROGRESS -> getInProgressModelAndView(applicationVersion, user);
      case AWAITING_PAYMENT -> getAwaitingPaymentModelAndView(applicationVersion, user);
      case SUBMITTED -> getSubmittedModelAndView(applicationVersion, user);
      default -> getSummaryModelAndView(applicationVersion);
    };
  }

  private ModelAndView getSummaryModelAndView(ApplicationVersion applicationVersion) {
    var pageTitle = ApplicationVersionStatus.IN_PROGRESS.equals(applicationVersion.getStatus())
        ? "Application summary"
        : applicationService.generateApplicationReference(applicationVersion);

    return applicationSummaryService.getApplicationSummaryModelAndView(
        applicationVersion,
        "fcs/application/applicationSummary",
        pageTitle
    );
  }

  private ModelAndView getSubmittedModelAndView(ApplicationVersion applicationVersion, ServiceUserDetail user) {
    var applicationId = applicationVersion.getApplication().getId();

    if (isRegulatorCaseProcessingUser(user, applicationVersion)) {
      return ReverseRouter.redirect(on(ApplicationCaseProcessingController.class)
          .caseProcessing(applicationId, null, null));
    }

    if (isConsulteeCaseProcessingUser(user, applicationVersion)) {
      return ReverseRouter.redirect(on(ConsulteeCaseProcessingController.class)
          .caseProcessing(applicationId, null, null));
    }

    if (isIndustryCaseProcessingUser(user, applicationVersion)) {
      return ReverseRouter.redirect(on(IndustryCaseProcessingController.class)
          .getIndustryCaseProcessing(applicationId, null));
    }

    return getSummaryModelAndView(applicationVersion);
  }

  private ModelAndView getAwaitingPaymentModelAndView(ApplicationVersion applicationVersion, ServiceUserDetail user) {
    var applicationId = applicationVersion.getApplication().getId();

    if (!applicationAccessService.hasApplicationPermission(user, applicationVersion, SUBMIT_FCS_APPLICATIONS)) {
      return getSummaryModelAndView(applicationVersion);
    }

    return ReverseRouter.redirect(on(IndustryCaseProcessingController.class).getIndustryCaseProcessing(applicationId, null));
  }

  private ModelAndView getInProgressModelAndView(ApplicationVersion applicationVersion, ServiceUserDetail user) {
    var applicationId = applicationVersion.getApplication().getId();

    if (!isIndustryCaseProcessingUser(user, applicationVersion)) {
      return getSummaryModelAndView(applicationVersion);
    }

    if (applicationVersion.isUpdateVersion()) {
      return ReverseRouter.redirect(on(IndustryCaseProcessingController.class).getIndustryCaseProcessing(applicationId, null));
    }

    return ReverseRouter.redirect(on(ApplicationTaskListController.class).getTaskList(applicationId));
  }

  private boolean isRegulatorCaseProcessingUser(ServiceUserDetail userDetail, ApplicationVersion applicationVersion) {
    return applicationAccessService.hasApplicationPermission(
        userDetail,
        applicationVersion,
        PROCESS_FCS_APPLICATIONS,
        ASSIGN_FCS_APPLICATIONS,
        TECHNICAL_REVIEW_FCS_APPLICATIONS
    );
  }

  private boolean isConsulteeCaseProcessingUser(ServiceUserDetail userDetail, ApplicationVersion applicationVersion) {
    return applicationAccessService.hasApplicationPermission(
        userDetail,
        applicationVersion,
        ALLOCATE_CONSULTATION,
        RESPOND_TO_CONSULTATION
    );
  }

  private boolean isIndustryCaseProcessingUser(ServiceUserDetail userDetail, ApplicationVersion applicationVersion) {
    return applicationAccessService.hasApplicationPermission(userDetail, applicationVersion, EDIT_FCS_APPLICATIONS);
  }

}
