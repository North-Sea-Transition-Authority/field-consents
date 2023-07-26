package uk.co.nstauthority.fieldconsents.application.caseprocessing;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.CaseProcessingTab.CASE_HISTORY;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.CaseProcessingTab.VIEW_APPLICATION;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseHistoryTabContentService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewSummaryView;
import uk.co.nstauthority.fieldconsents.application.summary.ApplicationSummaryService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationPermission;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationStatus;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.fieldconsents.workarea.WorkAreaController;

@Controller
@RequestMapping("applications/{applicationId}")
@HasApplicationPermission(permissions = {
    RolePermission.PROCESS_FCS_APPLICATIONS,
    RolePermission.ASSIGN_FCS_APPLICATIONS,
    RolePermission.TECHNICAL_REVIEW_FCS_APPLICATIONS})
public class ApplicationCaseProcessingController {

  private final ApplicationService applicationService;

  private final ApplicationVersionService applicationVersionService;

  private final ApplicationSummaryService applicationSummaryService;

  private final CaseProcessingActionService caseProcessingActionService;

  private final CaseProcessingTabService caseProcessingTabService;

  private final CaseHistoryTabContentService caseHistoryTabContentService;

  private final TechnicalReviewService technicalReviewService;

  ApplicationCaseProcessingController(ApplicationService applicationService,
                                      ApplicationVersionService applicationVersionService,
                                      ApplicationSummaryService applicationSummaryService,
                                      CaseProcessingActionService caseProcessingActionService,
                                      CaseProcessingTabService caseProcessingTabService,
                                      CaseHistoryTabContentService caseHistoryTabContentService,
                                      TechnicalReviewService technicalReviewService) {
    this.applicationService = applicationService;
    this.applicationVersionService = applicationVersionService;
    this.applicationSummaryService = applicationSummaryService;
    this.caseProcessingActionService = caseProcessingActionService;
    this.caseProcessingTabService = caseProcessingTabService;
    this.caseHistoryTabContentService = caseHistoryTabContentService;
    this.technicalReviewService = technicalReviewService;
  }

  @GetMapping("case-processing")
  @HasApplicationStatus(statuses = ApplicationVersionStatus.SUBMITTED)
  public ModelAndView getApplicationCaseProcessing(@PathVariable Integer applicationId,
                                                   ServiceUserDetail user) {
    return renderCaseProcessingOnTab(applicationId, user, VIEW_APPLICATION);
  }

  @GetMapping("case-history")
  @HasApplicationStatus(statuses = ApplicationVersionStatus.SUBMITTED)
  public ModelAndView getCaseHistoryTab(@PathVariable Integer applicationId, ServiceUserDetail user) {
    return renderCaseProcessingOnTab(applicationId, user, CASE_HISTORY);
  }

  @GetMapping("view-application")
  @HasApplicationStatus(statuses = ApplicationVersionStatus.SUBMITTED)
  public ModelAndView getViewApplicationTab(@PathVariable Integer applicationId, ServiceUserDetail user) {
    return renderCaseProcessingOnTab(applicationId, user, VIEW_APPLICATION);
  }

  private ModelAndView renderCaseProcessingOnTab(@PathVariable Integer applicationId,
                                                 ServiceUserDetail user,
                                                 CaseProcessingTab caseProcessingTab) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    var caseProcessingActions = caseProcessingActionService.getUserActionViews(applicationVersion, user);
    var pageTitle = applicationService.generateApplicationReference(applicationVersion);

    var modelAndView = applicationSummaryService.getApplicationSummaryModelAndView(
        applicationVersion,
        "fcs/application/applicationCaseProcessing",
        pageTitle,
        ReverseRouter.route(on(WorkAreaController.class).getWorkArea(null, null))
    );

    technicalReviewService.findOpenTechnicalReview(applicationVersion)
        .ifPresent(technicalReview -> modelAndView
            .addObject("technicalReviewSummaryView", TechnicalReviewSummaryView.from(technicalReview))
        );

    modelAndView.addObject("actionList", caseProcessingActions)
        .addObject("caseProcessingTabs", caseProcessingTabService.getTabsAvailableToUser(user))
        .addObject("applicationId", applicationId)
        .addObject("selectedTab", caseProcessingTab.getValue())
        .addObject("caseHistoryEvents",
            caseHistoryTabContentService.getCaseHistoryTabContent(applicationVersion.getApplication()));

    return modelAndView;
  }
}
