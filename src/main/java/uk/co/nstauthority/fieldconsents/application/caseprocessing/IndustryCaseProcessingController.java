package uk.co.nstauthority.fieldconsents.application.caseprocessing;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateRequestViewService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateService;
import uk.co.nstauthority.fieldconsents.application.summary.ApplicationSummaryService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationPermission;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationStatus;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.fieldconsents.workarea.WorkAreaController;

@Controller
@RequestMapping("applications/{applicationId}")
public class IndustryCaseProcessingController {

  private final ApplicationService applicationService;

  private final ApplicationVersionService applicationVersionService;

  private final ApplicationSummaryService applicationSummaryService;

  private final CaseProcessingActionService caseProcessingActionService;

  private final ApplicationUpdateService applicationUpdateService;

  private final ApplicationUpdateRequestViewService applicationUpdateRequestViewService;

  @Autowired
  IndustryCaseProcessingController(ApplicationService applicationService,
                                   ApplicationVersionService applicationVersionService,
                                   ApplicationSummaryService applicationSummaryService,
                                   CaseProcessingActionService caseProcessingActionService,
                                   ApplicationUpdateService applicationUpdateService,
                                   ApplicationUpdateRequestViewService applicationUpdateRequestViewService) {
    this.applicationService = applicationService;
    this.applicationVersionService = applicationVersionService;
    this.applicationSummaryService = applicationSummaryService;
    this.caseProcessingActionService = caseProcessingActionService;
    this.applicationUpdateService = applicationUpdateService;
    this.applicationUpdateRequestViewService = applicationUpdateRequestViewService;
  }

  @GetMapping("industry-case-processing")
  @HasApplicationStatus(statuses = {
      ApplicationVersionStatus.IN_PROGRESS,
      ApplicationVersionStatus.AWAITING_PAYMENT,
      ApplicationVersionStatus.SUBMITTED
  })
  @HasApplicationPermission(permissions = RolePermission.EDIT_FCS_APPLICATIONS)
  public ModelAndView getIndustryCaseProcessing(@PathVariable Integer applicationId,
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
        "fcs/application/industryCaseProcessing",
        pageTitle,
        ReverseRouter.route(on(WorkAreaController.class).getWorkArea(null, null))
    );

    if (applicationUpdateService.openApplicationUpdateExists(applicationVersion)) {
      modelAndView.addObject("applicationUpdateRequestView",
          applicationUpdateRequestViewService.getOpenApplicationUpdateRequestView(applicationVersion)
      );
    }

    return modelAndView.addObject("actionList", caseProcessingActions);
  }
}
