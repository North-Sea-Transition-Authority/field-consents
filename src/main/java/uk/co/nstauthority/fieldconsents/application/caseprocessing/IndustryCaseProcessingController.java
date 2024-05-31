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
import uk.co.nstauthority.fieldconsents.application.ApplicationContextService;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.ConsentService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.ConsentTabService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.ProductionConsentCheckResult;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.payment.PaymentsTabService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.update.request.ApplicationUpdateRequestViewService;
import uk.co.nstauthority.fieldconsents.application.summary.ApplicationSummaryService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationPermission;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationStatus;
import uk.co.nstauthority.fieldconsents.authorisation.IsMemberOfTeamType;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("applications/{applicationId}")
public class IndustryCaseProcessingController {

  private final ApplicationService applicationService;
  private final ApplicationContextService applicationContextService;
  private final ApplicationVersionService applicationVersionService;
  private final ApplicationSummaryService applicationSummaryService;
  private final CaseProcessingActionService caseProcessingActionService;
  private final CaseProcessingTabService caseProcessingTabService;
  private final ApplicationUpdateService applicationUpdateService;
  private final ApplicationUpdateRequestViewService applicationUpdateRequestViewService;
  private final PaymentsTabService paymentsTabService;
  private final ConsentTabService consentTabService;
  private final ConsentService consentService;

  @Autowired
  IndustryCaseProcessingController(
      ApplicationService applicationService,
      ApplicationContextService applicationContextService,
      ApplicationVersionService applicationVersionService,
      ApplicationSummaryService applicationSummaryService,
      CaseProcessingActionService caseProcessingActionService,
      CaseProcessingTabService caseProcessingTabService,
      ApplicationUpdateService applicationUpdateService,
      ApplicationUpdateRequestViewService applicationUpdateRequestViewService,
      PaymentsTabService paymentsTabService,
      ConsentTabService consentTabService,
      ConsentService consentService
  ) {
    this.applicationService = applicationService;
    this.applicationContextService = applicationContextService;
    this.applicationVersionService = applicationVersionService;
    this.applicationSummaryService = applicationSummaryService;
    this.caseProcessingActionService = caseProcessingActionService;
    this.caseProcessingTabService = caseProcessingTabService;
    this.applicationUpdateService = applicationUpdateService;
    this.applicationUpdateRequestViewService = applicationUpdateRequestViewService;
    this.paymentsTabService = paymentsTabService;
    this.consentTabService = consentTabService;
    this.consentService = consentService;
  }

  @GetMapping("industry-case-processing")
  @HasApplicationStatus(statuses = {
      ApplicationVersionStatus.IN_PROGRESS,
      ApplicationVersionStatus.AWAITING_PAYMENT,
      ApplicationVersionStatus.SUBMITTED,
      ApplicationVersionStatus.CONSENTED,
      ApplicationVersionStatus.WITHDRAWN
  })
  @HasApplicationPermission(permissions = {
      RolePermission.EDIT_FCS_APPLICATIONS,
      RolePermission.PAY_AND_SUBMIT_FCS_APPLICATIONS,
      RolePermission.VIEW_FCS_CONSENTS
  })
  @IsMemberOfTeamType(teamType = TeamType.INDUSTRY)
  public ModelAndView getIndustryCaseProcessing(
      @PathVariable Integer applicationId,
      @RequestParam(required = false) CaseProcessingTab tab,
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

    var caseProcessingTabs = caseProcessingTabService.getIndustryTabsAvailableToUser(user, applicationVersion);
    if (tab == null && !caseProcessingTabs.isEmpty()) {
      tab = caseProcessingTabs.getFirst();
    }

    var modelAndView = new ModelAndView("fcs/application/industryCaseProcessing")
        .addObject("selectedTab", tab)
        .addObject(
            "controllerUrl",
            ReverseRouter.route(on(IndustryCaseProcessingController.class)
                .getIndustryCaseProcessing(application.getId(), null, null))
        )
        .addObject("actionList", caseProcessingActionService.getUserActionViews(applicationVersion, user))
        .addObject("applicationContext", applicationContextService.getApplicationContext(applicationVersion))
        .addObject("caseProcessingTabs", caseProcessingTabs)
        .addObject("wideSummaryDisplay", WIDE_SUMMARY_DISPLAY.allowed(application.getType()))
        .addObject("pageTitle", applicationService.generateApplicationReference(applicationVersion))
        .addObject("isMigratedApplication", applicationService.isMigratedApplication(application));

    if (tab != null && caseProcessingTabs.contains(tab)) {
      switch (tab) {
        case CONSENT -> consentTabService.addConsentTabContentToModelAndView(applicationVersion, modelAndView);
        case PAYMENTS -> paymentsTabService.addPaymentsTabContentToModelAndView(application, modelAndView);
        case VIEW_APPLICATION -> applicationSummaryService.addSummarySectionsToModelAndView(applicationVersion, modelAndView);
        default -> {
        }
      }
    }

    if (applicationUpdateService.openApplicationUpdateExists(applicationVersion)) {
      modelAndView.addObject("applicationUpdateRequestView",
          applicationUpdateRequestViewService.getOpenApplicationUpdateRequestView(applicationVersion)
      );
    }

    if (consentService.shouldCheckProductionConsentExists(applicationVersion)) {
      var productionConsentCheckResult = consentService.checkProductionConsentExistsForInProgressApplication(applicationVersion);
      if (productionConsentCheckResult == ProductionConsentCheckResult.NOT_WITHIN_ACTIVE_CONSENT) {
        modelAndView.addObject("warning", productionConsentCheckResult.getWarning());
      }
    }

    return modelAndView;
  }
}
