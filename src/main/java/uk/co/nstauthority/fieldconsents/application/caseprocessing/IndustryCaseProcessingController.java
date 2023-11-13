package uk.co.nstauthority.fieldconsents.application.caseprocessing;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTypeFeature.WIDE_SUMMARY_DISPLAY;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.payment.PaymentsTabService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.update.request.ApplicationUpdateRequestViewService;
import uk.co.nstauthority.fieldconsents.application.summary.ApplicationSummaryService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationPermission;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationStatus;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("applications/{applicationId}")
public class IndustryCaseProcessingController {

  private final ApplicationService applicationService;
  private final ApplicationVersionService applicationVersionService;
  private final ApplicationSummaryService applicationSummaryService;
  private final CaseProcessingActionService caseProcessingActionService;
  private final CaseProcessingTabService caseProcessingTabService;
  private final ApplicationUpdateService applicationUpdateService;
  private final ApplicationUpdateRequestViewService applicationUpdateRequestViewService;
  private final PaymentsTabService paymentsTabService;

  @Autowired
  IndustryCaseProcessingController(
      ApplicationService applicationService,
      ApplicationVersionService applicationVersionService,
      ApplicationSummaryService applicationSummaryService,
      CaseProcessingActionService caseProcessingActionService,
      CaseProcessingTabService caseProcessingTabService,
      ApplicationUpdateService applicationUpdateService,
      ApplicationUpdateRequestViewService applicationUpdateRequestViewService,
      PaymentsTabService paymentsTabService
  ) {
    this.applicationService = applicationService;
    this.applicationVersionService = applicationVersionService;
    this.applicationSummaryService = applicationSummaryService;
    this.caseProcessingActionService = caseProcessingActionService;
    this.caseProcessingTabService = caseProcessingTabService;
    this.applicationUpdateService = applicationUpdateService;
    this.applicationUpdateRequestViewService = applicationUpdateRequestViewService;
    this.paymentsTabService = paymentsTabService;
  }

  @GetMapping("industry-case-processing")
  @HasApplicationStatus(statuses = {
      ApplicationVersionStatus.IN_PROGRESS,
      ApplicationVersionStatus.AWAITING_PAYMENT,
      ApplicationVersionStatus.SUBMITTED
  })
  @HasApplicationPermission(permissions = {
      RolePermission.EDIT_FCS_APPLICATIONS,
      RolePermission.PAY_AND_SUBMIT_FCS_APPLICATIONS
  })
  public ModelAndView getIndustryCaseProcessing(
      @PathVariable Integer applicationId,
      @RequestParam(defaultValue = "view-application") CaseProcessingTab tab,
      ServiceUserDetail user
  ) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    return getApplicationSummaryModelAndView(applicationVersion, tab, user);
  }

  private ModelAndView getApplicationSummaryModelAndView(
      ApplicationVersion applicationVersion,
      CaseProcessingTab tab,
      ServiceUserDetail user
  ) {
    var application = applicationVersion.getApplication();
    var modelAndView = new ModelAndView("fcs/application/industryCaseProcessing")
        .addObject("selectedTab", tab)
        .addObject(
            "controllerUrl",
            ReverseRouter.route(on(IndustryCaseProcessingController.class)
                .getIndustryCaseProcessing(application.getId(), null, null))
        )
        .addObject("actionList", caseProcessingActionService.getUserActionViews(applicationVersion, user))
        .addObject("caseProcessingTabs", caseProcessingTabService.getTabsAvailableToUser(user))
        .addObject("wideSummaryDisplay", WIDE_SUMMARY_DISPLAY.allowed(application.getType()))
        .addObject("pageTitle", applicationService.generateApplicationReference(applicationVersion));

    if (CaseProcessingTab.PAYMENTS.equals(tab)) {
      paymentsTabService.addPaymentsTabContentToModelAndView(applicationVersion, modelAndView);
    } else {
      applicationSummaryService.addSummarySectionsToModelAndView(applicationVersion, modelAndView);
    }

    if (applicationUpdateService.openApplicationUpdateExists(applicationVersion)) {
      modelAndView.addObject("applicationUpdateRequestView",
          applicationUpdateRequestViewService.getOpenApplicationUpdateRequestView(applicationVersion)
      );
    }

    return modelAndView;
  }
}
