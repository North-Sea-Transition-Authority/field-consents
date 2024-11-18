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
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseHistoryTabContentService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.ConsentService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.ConsentTabService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.ProductionConsentCheckResult;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.breaches.ConsentBreachService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.issuing.approval.ConsentIssuingApprovalService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformation.FurtherInformationService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.payment.PaymentsTabService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.tasklist.CaseProcessingTaskListService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewSummaryView;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal.ApplicationWithdrawalService;
import uk.co.nstauthority.fieldconsents.application.licenceexpiry.LicenceExpiryService;
import uk.co.nstauthority.fieldconsents.application.summary.ApplicationSummaryService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationPermission;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationStatus;
import uk.co.nstauthority.fieldconsents.authorisation.IsMemberOfTeamType;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamService;

@Controller
@RequestMapping("applications/{applicationId}")
@HasApplicationStatus(statuses = {
    ApplicationVersionStatus.IN_PROGRESS,
    ApplicationVersionStatus.AWAITING_PAYMENT,
    ApplicationVersionStatus.SUBMITTED,
    ApplicationVersionStatus.CONSENTED,
    ApplicationVersionStatus.WITHDRAWN,
    ApplicationVersionStatus.CLOSED
})
@HasApplicationPermission(permissions = {
    RolePermission.PROCESS_FCS_APPLICATIONS,
    RolePermission.ASSIGN_FCS_APPLICATIONS,
    RolePermission.TECHNICAL_REVIEW_FCS_APPLICATIONS,
    RolePermission.AUTHORISE_FCS_CONSENTS,
    RolePermission.VIEW_FCS_CONSENTS,
})
@IsMemberOfTeamType(teamType = TeamType.REGULATOR)
public class ApplicationCaseProcessingController {

  // The list of permissions here must match the permissions used in @HasApplicationPermission above
  public static final RolePermission[] REGULATOR_PROCESSING_REQUIRED_PERMISSIONS = {
      RolePermission.PROCESS_FCS_APPLICATIONS,
      RolePermission.ASSIGN_FCS_APPLICATIONS,
      RolePermission.TECHNICAL_REVIEW_FCS_APPLICATIONS,
      RolePermission.AUTHORISE_FCS_CONSENTS,
      RolePermission.VIEW_FCS_CONSENTS
  };

  private final ApplicationService applicationService;
  private final ApplicationContextService applicationContextService;
  private final ApplicationVersionService applicationVersionService;
  private final ApplicationSummaryService applicationSummaryService;
  private final ApplicationWithdrawalService applicationWithdrawalService;
  private final CaseProcessingActionService caseProcessingActionService;
  private final CaseProcessingTaskListService caseProcessingTaskListService;
  private final CaseProcessingTabService caseProcessingTabService;
  private final CaseHistoryTabContentService caseHistoryTabContentService;
  private final TechnicalReviewService technicalReviewService;
  private final RegulatorTeamService regulatorTeamService;
  private final ConsultationService consultationService;
  private final FurtherInformationService furtherInformationService;
  private final PaymentsTabService paymentsTabService;
  private final ConsentTabService consentTabService;
  private final ConsentIssuingApprovalService consentIssuingApprovalService;
  private final ConsentService consentService;
  private final ConsentBreachService consentBreachService;
  private final CaseProcessingControllerHelperService caseProcessingControllerHelperService;
  private final LicenceExpiryService licenceExpiryService;

  @Autowired
  ApplicationCaseProcessingController(
      ApplicationService applicationService,
      ApplicationContextService applicationContextService,
      ApplicationVersionService applicationVersionService,
      ApplicationSummaryService applicationSummaryService,
      ApplicationWithdrawalService applicationWithdrawalService,
      CaseProcessingActionService caseProcessingActionService,
      CaseProcessingTaskListService caseProcessingTaskListService,
      CaseProcessingTabService caseProcessingTabService,
      CaseHistoryTabContentService caseHistoryTabContentService,
      TechnicalReviewService technicalReviewService,
      RegulatorTeamService regulatorTeamService,
      ConsultationService consultationService,
      FurtherInformationService furtherInformationService,
      PaymentsTabService paymentsTabService,
      ConsentTabService consentTabService,
      ConsentIssuingApprovalService consentIssuingApprovalService,
      ConsentService consentService,
      ConsentBreachService consentBreachService,
      CaseProcessingControllerHelperService caseProcessingControllerHelperService,
      LicenceExpiryService licenceExpiryService
  ) {
    this.applicationService = applicationService;
    this.applicationContextService = applicationContextService;
    this.applicationVersionService = applicationVersionService;
    this.applicationSummaryService = applicationSummaryService;
    this.applicationWithdrawalService = applicationWithdrawalService;
    this.caseProcessingActionService = caseProcessingActionService;
    this.caseProcessingTaskListService = caseProcessingTaskListService;
    this.caseProcessingTabService = caseProcessingTabService;
    this.caseHistoryTabContentService = caseHistoryTabContentService;
    this.technicalReviewService = technicalReviewService;
    this.regulatorTeamService = regulatorTeamService;
    this.consultationService = consultationService;
    this.furtherInformationService = furtherInformationService;
    this.paymentsTabService = paymentsTabService;
    this.consentTabService = consentTabService;
    this.consentIssuingApprovalService = consentIssuingApprovalService;
    this.consentService = consentService;
    this.consentBreachService = consentBreachService;
    this.caseProcessingControllerHelperService = caseProcessingControllerHelperService;
    this.licenceExpiryService = licenceExpiryService;
  }

  @GetMapping("case-processing")
  public ModelAndView caseProcessing(
      @PathVariable Integer applicationId,
      @RequestParam(value = "version", required = false) Integer requestedApplicationVersionId,
      @RequestParam(required = false) CaseProcessingTab tab,
      ServiceUserDetail user
  ) {
    var latestApplicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    var application = latestApplicationVersion.getApplication();
    var applicationType = application.getType();

    var selectedApplicationVersion = Optional.ofNullable(requestedApplicationVersionId)
        .map(avid -> caseProcessingControllerHelperService.getApplicationVersionForApplication(application, avid))
        .orElse(latestApplicationVersion);

    var caseProcessingTabs = caseProcessingTabService.getRegulatorTabsAvailableToUser(user, latestApplicationVersion);
    if (tab == null && !caseProcessingTabs.isEmpty()) {
      tab = caseProcessingTabs.getFirst();
    }

    var pageTitle = applicationService
        .getApplicationReference(latestApplicationVersion, applicationType.getDisplayName() + " application");

    var modelAndView = new ModelAndView("fcs/application/applicationCaseProcessing")
        .addObject("selectedTab", tab)
        .addObject("controllerUrl", ReverseRouter.route(on(this.getClass()).caseProcessing(applicationId, null, null,
            null
        )))
        .addObject("actionList", caseProcessingActionService.getTopLevelActionItemViews(latestApplicationVersion, user))
        .addObject("applicationContext", applicationContextService.getApplicationContext(latestApplicationVersion))
        .addObject("caseProcessingTabs", caseProcessingTabs)
        .addObject("wideSummaryDisplay", WIDE_SUMMARY_DISPLAY.allowed(applicationType))
        .addObject("pageTitle", pageTitle)
        .addObject("isMigratedApplication", applicationService.isMigratedApplication(application))
        .addObject("openWithdrawal",  applicationWithdrawalService
            .findOpenApplicationWithdrawal(latestApplicationVersion).isPresent())
        .addObject("isConsentBreached", consentBreachService
            .findConsentBreachByApplication(application).isPresent());

    if (ApplicationVersionStatus.SUBMITTED.equals(latestApplicationVersion.getStatus())) {
      modelAndView.addObject("consentIssuingApprovalSummaryView",
          consentIssuingApprovalService.getConsentIssuingApprovalSummaryView(application).orElse(null));
    }

    if (tab != null && caseProcessingTabs.contains(tab)) {
      switch (tab) {
        case CONSENT -> consentTabService.addConsentTabContentToModelAndView(latestApplicationVersion, modelAndView);
        case PAYMENTS -> paymentsTabService.addPaymentsTabContentToModelAndView(application, modelAndView);
        case CASE_HISTORY -> addCaseHistoryTab(modelAndView, latestApplicationVersion);
        case TASKS -> addTasksTab(modelAndView, latestApplicationVersion, user);
        case VIEW_APPLICATION -> applicationSummaryService.addSummarySectionsAndVersionOptionsToModelAndView(
            selectedApplicationVersion,
            modelAndView,
            user
        );
        default -> {
        }
      }
    }

    if (regulatorTeamService.isTechnicalReviewer(WebUserAccountId.from(user))) {
      technicalReviewService.findOpenTechnicalReview(latestApplicationVersion)
          .map(TechnicalReviewSummaryView::from)
          .ifPresent(view -> modelAndView.addObject("technicalReviewSummaryView", view));
    }

    if (regulatorTeamService.isCaseOfficer(WebUserAccountId.from(user))) {
      consultationService.findLatestOpenConsultation(application)
          .flatMap(furtherInformationService::findLatestOpenFurtherInformation)
          .map(furtherInformationService::getFurtherInformationView)
          .ifPresent(view -> modelAndView.addObject("furtherInformationView", view));
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

  private void addCaseHistoryTab(ModelAndView modelAndView, ApplicationVersion applicationVersion) {
    var caseHistoryEvents = caseHistoryTabContentService.getCaseHistoryTabContent(applicationVersion.getApplication());
    modelAndView.addObject("caseHistoryEvents", caseHistoryEvents);
  }

  private void addTasksTab(ModelAndView modelAndView, ApplicationVersion applicationVersion, ServiceUserDetail user) {
    var taskListSections = caseProcessingTaskListService.getTaskListSections(applicationVersion, user);
    modelAndView.addObject("taskListSections", taskListSections);
  }
}
