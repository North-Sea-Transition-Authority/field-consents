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
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseHistoryTabContentService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.ConsentTabService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.issuing.approval.ConsentIssuingApprovalService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformation.FurtherInformationService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.payment.PaymentsTabService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.tasklist.CaseProcessingTaskListService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewSummaryView;
import uk.co.nstauthority.fieldconsents.application.summary.ApplicationSummaryService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationPermission;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationStatus;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamService;

@Controller
@RequestMapping("applications/{applicationId}")
@HasApplicationStatus(statuses = {
    ApplicationVersionStatus.IN_PROGRESS,
    ApplicationVersionStatus.SUBMITTED,
    ApplicationVersionStatus.COMPLETED,
    ApplicationVersionStatus.WITHDRAWN
})
@HasApplicationPermission(permissions = {
    RolePermission.PROCESS_FCS_APPLICATIONS,
    RolePermission.ASSIGN_FCS_APPLICATIONS,
    RolePermission.TECHNICAL_REVIEW_FCS_APPLICATIONS,
    RolePermission.VIEW_FCS_CONSENTS,
    RolePermission.AUTHORISE_FCS_CONSENTS,
})
public class ApplicationCaseProcessingController {

  private final ApplicationService applicationService;
  private final ApplicationContextService applicationContextService;
  private final ApplicationVersionService applicationVersionService;
  private final ApplicationSummaryService applicationSummaryService;
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

  @Autowired
  ApplicationCaseProcessingController(
      ApplicationService applicationService,
      ApplicationContextService applicationContextService,
      ApplicationVersionService applicationVersionService,
      ApplicationSummaryService applicationSummaryService,
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
      ConsentIssuingApprovalService consentIssuingApprovalService
  ) {
    this.applicationService = applicationService;
    this.applicationContextService = applicationContextService;
    this.applicationVersionService = applicationVersionService;
    this.applicationSummaryService = applicationSummaryService;
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
  }

  @GetMapping("case-processing")
  public ModelAndView caseProcessing(
      @PathVariable Integer applicationId,
      @RequestParam(defaultValue = "tasks") CaseProcessingTab tab,
      ServiceUserDetail user
  ) {
    return renderCaseProcessingOnTab(applicationId, tab, user);
  }

  private ModelAndView renderCaseProcessingOnTab(Integer applicationId, CaseProcessingTab tab, ServiceUserDetail user) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    var application = applicationVersion.getApplication();
    var applicationType = application.getType();
    var consentIssuingApprovalSummaryView = consentIssuingApprovalService.getConsentIssuingApprovalSummaryView(application)
        .orElse(null);

    var modelAndView = new ModelAndView("fcs/application/applicationCaseProcessing")
        .addObject("selectedTab", tab)
        .addObject("controllerUrl", ReverseRouter.route(on(this.getClass()).caseProcessing(applicationId, null, null)))
        .addObject("actionList", caseProcessingActionService.getUserActionViews(applicationVersion, user))
        .addObject("applicationContext", applicationContextService.getApplicationContext(applicationVersion))
        .addObject("caseProcessingTabs", caseProcessingTabService.getTabsAvailableToUser(user, applicationVersion))
        .addObject("wideSummaryDisplay", WIDE_SUMMARY_DISPLAY.allowed(applicationType))
        .addObject("pageTitle", applicationService.generateApplicationReference(applicationVersion))
        .addObject("consentIssuingApprovalSummaryView", consentIssuingApprovalSummaryView);

    switch (tab) {
      case CONSENT -> consentTabService.addConsentTabContentToModelAndView(application, modelAndView);
      case PAYMENTS -> paymentsTabService.addPaymentsTabContentToModelAndView(applicationVersion, modelAndView);
      case CASE_HISTORY -> addCaseHistoryTab(modelAndView, applicationVersion);
      case TASKS -> addTasksTab(modelAndView, applicationVersion, user);
      default -> applicationSummaryService.addSummarySectionsToModelAndView(applicationVersion, modelAndView);
    }

    if (regulatorTeamService.isTechnicalReviewer(WebUserAccountId.from(user))) {
      technicalReviewService.findOpenTechnicalReview(applicationVersion)
          .map(TechnicalReviewSummaryView::from)
          .ifPresent(view -> modelAndView.addObject("technicalReviewSummaryView", view));
    }

    if (regulatorTeamService.isCaseOfficer(WebUserAccountId.from(user))) {
      consultationService.findLatestOpenConsultation(application)
          .flatMap(furtherInformationService::findLatestOpenFurtherInformation)
          .map(furtherInformationService::getFurtherInformationView)
          .ifPresent(view -> modelAndView.addObject("furtherInformationView", view));
    }

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
