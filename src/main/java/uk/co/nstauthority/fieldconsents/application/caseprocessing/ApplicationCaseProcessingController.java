package uk.co.nstauthority.fieldconsents.application.caseprocessing;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.CaseAssignmentService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionService;
import uk.co.nstauthority.fieldconsents.application.summary.ApplicationSummaryService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.ActionEndPoint;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationPermission;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationStatus;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.fieldconsents.workarea.WorkAreaController;

@Controller
@RequestMapping("applications/{applicationId}")
public class ApplicationCaseProcessingController {

  private final ApplicationService applicationService;

  private final ApplicationVersionService applicationVersionService;

  private final ApplicationSummaryService applicationSummaryService;

  private final CaseProcessingActionService caseProcessingActionService;

  private final CaseAssignmentService caseAssignmentService;

  ApplicationCaseProcessingController(ApplicationService applicationService,
                                      ApplicationVersionService applicationVersionService,
                                      ApplicationSummaryService applicationSummaryService,
                                      CaseProcessingActionService caseProcessingActionService,
                                      CaseAssignmentService caseAssignmentService) {
    this.applicationService = applicationService;
    this.applicationVersionService = applicationVersionService;
    this.applicationSummaryService = applicationSummaryService;
    this.caseProcessingActionService = caseProcessingActionService;
    this.caseAssignmentService = caseAssignmentService;
  }

  @GetMapping("case-processing")
  @HasApplicationStatus(statuses = ApplicationVersionStatus.SUBMITTED)
  @HasApplicationPermission(permissions = RolePermission.PROCESS_FCS_APPLICATIONS)
  public ModelAndView getApplicationCaseProcessing(@PathVariable Integer applicationId,
                                                   ServiceUserDetail user) {

    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    return getApplicationSummaryModelAndView(applicationVersion, user);
  }

  private ModelAndView getApplicationSummaryModelAndView(ApplicationVersion applicationVersion,
                                                         ServiceUserDetail user) {

    var caseProcessingActions = caseProcessingActionService.getUserActionViews(applicationVersion, user);
    var pageTitle = applicationService.generateApplicationReference(applicationVersion);

    var modelAndView = applicationSummaryService.getApplicationSummaryModelAndView(
        applicationVersion,
        "fcs/application/applicationCaseProcessing",
        pageTitle,
        ReverseRouter.route(on(WorkAreaController.class).getWorkArea(null, null))
    );

    return modelAndView.addObject("caseProcessingActions", caseProcessingActions);
  }

  @PostMapping("take-ownership-case-officer")
  @ActionEndPoint(CaseProcessingActionItem.CASE_OFFICER_TAKE_OWNERSHIP)
  public ModelAndView takeOwnershipCaseOfficer(@PathVariable Integer applicationId,
                                               ServiceUserDetail user) {

    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    caseAssignmentService.assignCaseOfficer(applicationVersion, user);

    return ReverseRouter
        .redirect(on(ApplicationCaseProcessingController.class).getApplicationCaseProcessing(applicationId, null));
  }

  @PostMapping("release-ownership-case-officer")
  @ActionEndPoint(CaseProcessingActionItem.CASE_OFFICER_RELEASE_OWNERSHIP)
  public ModelAndView releaseOwnershipCaseOfficer(@PathVariable Integer applicationId) {

    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    caseAssignmentService.unassignCaseOfficer(applicationVersion);

    return ReverseRouter
        .redirect(on(ApplicationCaseProcessingController.class).getApplicationCaseProcessing(applicationId, null));
  }
}
