package uk.co.nstauthority.fieldconsents.application.caseprocessing;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTypeFeature.WIDE_SUMMARY_DISPLAY;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.application.ApplicationContextService;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.ConsentService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.ConsentTabService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.ProductionConsentCheckResult;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.breaches.ConsentBreachService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.payment.PaymentsTabService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.update.request.ApplicationUpdateRequestViewService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal.ApplicationWithdrawalService;
import uk.co.nstauthority.fieldconsents.application.licenceexpiry.LicenceExpiryService;
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
public class IndustryCaseProcessingController {

  // The list of permissions here must match the permissions used in @HasApplicationPermission above
  public static final RolePermission[] INDUSTRY_PROCESSING_REQUIRED_PERMISSIONS = {
      RolePermission.EDIT_FCS_APPLICATIONS,
      RolePermission.PAY_AND_SUBMIT_FCS_APPLICATIONS,
      RolePermission.VIEW_FCS_CONSENTS
  };

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
  private final ApplicationWithdrawalService applicationWithdrawalService;
  private final ConsentBreachService consentBreachService;
  private final CaseProcessingControllerHelperService caseProcessingControllerHelperService;
  private final LicenceExpiryService licenceExpiryService;

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
      ConsentService consentService,
      ApplicationWithdrawalService applicationWithdrawalService,
      ConsentBreachService consentBreachService,
      CaseProcessingControllerHelperService caseProcessingControllerHelperService,
      LicenceExpiryService licenceExpiryService
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
    this.applicationWithdrawalService = applicationWithdrawalService;
    this.consentBreachService = consentBreachService;
    this.caseProcessingControllerHelperService = caseProcessingControllerHelperService;
    this.licenceExpiryService = licenceExpiryService;
  }

  @GetMapping("industry-case-processing")
  public ModelAndView getIndustryCaseProcessing(
      @PathVariable Integer applicationId,
      @RequestParam(value = "version", required = false) Integer requestedApplicationVersionId,
      @RequestParam(required = false) CaseProcessingTab tab,
      ServiceUserDetail user
  ) {
    var latestApplicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    var application = latestApplicationVersion.getApplication();

    var selectedApplicationVersion = Optional.ofNullable(requestedApplicationVersionId)
        .map(avid -> caseProcessingControllerHelperService.getApplicationVersionForApplication(application, avid))
        .orElse(latestApplicationVersion);

    var caseProcessingTabs = caseProcessingTabService.getIndustryTabsAvailableToUser(user, latestApplicationVersion);
    if (tab == null && !caseProcessingTabs.isEmpty()) {
      tab = caseProcessingTabs.getFirst();
    }

    var modelAndView = new ModelAndView("fcs/application/industryCaseProcessing")
        .addObject("selectedTab", tab)
        .addObject(
            "controllerUrl",
            ReverseRouter.route(on(IndustryCaseProcessingController.class)
                .getIndustryCaseProcessing(application.getId(), null, null, null))
        )
        .addObject("actionList", caseProcessingActionService.getUserActionViews(latestApplicationVersion, user))
        .addObject("applicationContext", applicationContextService.getApplicationContext(latestApplicationVersion))
        .addObject("caseProcessingTabs", caseProcessingTabs)
        .addObject("wideSummaryDisplay", WIDE_SUMMARY_DISPLAY.allowed(application.getType()))
        .addObject("pageTitle", applicationService.generateApplicationReference(latestApplicationVersion))
        .addObject("isMigratedApplication", applicationService.isMigratedApplication(application))
        .addObject("openWithdrawal", applicationWithdrawalService
            .findOpenApplicationWithdrawal(latestApplicationVersion).isPresent())
        .addObject("isConsentBreached", consentBreachService.findConsentBreachByApplication(application).isPresent());

    if (tab != null && caseProcessingTabs.contains(tab)) {
      switch (tab) {
        case CONSENT -> consentTabService.addConsentTabContentToModelAndView(latestApplicationVersion, modelAndView);
        case PAYMENTS -> paymentsTabService.addPaymentsTabContentToModelAndView(application, modelAndView);
        case VIEW_APPLICATION -> applicationSummaryService.addSummarySectionsAndVersionOptionsToModelAndView(
            selectedApplicationVersion,
            modelAndView,
            user
        );
        default -> {
        }
      }
    }

    if (applicationUpdateService.openApplicationUpdateExists(latestApplicationVersion)) {
      modelAndView.addObject("applicationUpdateRequestView",
          applicationUpdateRequestViewService.getOpenApplicationUpdateRequestView(latestApplicationVersion)
      );
    }

    if (consentService.shouldCheckProductionConsentExists(latestApplicationVersion)) {
      var productionConsentCheckResult = consentService
          .checkProductionConsentExistsForInProgressApplication(latestApplicationVersion);
      if (productionConsentCheckResult == ProductionConsentCheckResult.NOT_WITHIN_ACTIVE_CONSENT) {
        modelAndView.addObject("warning", productionConsentCheckResult.getWarning());
      }
    }

    modelAndView.addObject("expiringLicences",
        licenceExpiryService.getLicencesExpiringDuringConsentPeriod(latestApplicationVersion));

    return modelAndView;
  }
}
