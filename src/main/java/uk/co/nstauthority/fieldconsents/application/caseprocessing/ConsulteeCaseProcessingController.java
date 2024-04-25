package uk.co.nstauthority.fieldconsents.application.caseprocessing;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTypeFeature.WIDE_SUMMARY_DISPLAY;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.ALLOCATE_CONSULTATION;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.RESPOND_TO_CONSULTATION;

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
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationRequestView;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformation.FurtherInformationService;
import uk.co.nstauthority.fieldconsents.application.summary.ApplicationSummaryService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationPermission;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationStatus;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@Controller
@RequestMapping("applications/{applicationId}/consultation-case-processing")
@HasApplicationStatus(statuses = {
    ApplicationVersionStatus.IN_PROGRESS,
    ApplicationVersionStatus.SUBMITTED,
    ApplicationVersionStatus.CONSENTED,
    ApplicationVersionStatus.WITHDRAWN
})
@HasApplicationPermission(permissions = {ALLOCATE_CONSULTATION, RESPOND_TO_CONSULTATION})
public class ConsulteeCaseProcessingController {

  private final ApplicationService applicationService;
  private final ApplicationContextService applicationContextService;
  private final ApplicationVersionService applicationVersionService;
  private final ApplicationSummaryService applicationSummaryService;
  private final CaseProcessingActionService caseProcessingActionService;
  private final ConsultationService consultationService;
  private final CaseProcessingTabService caseProcessingTabService;
  private final FurtherInformationService furtherInformationService;

  ConsulteeCaseProcessingController(
      ApplicationService applicationService,
      ApplicationContextService applicationContextService,
      ApplicationVersionService applicationVersionService,
      ApplicationSummaryService applicationSummaryService,
      CaseProcessingActionService caseProcessingActionService,
      ConsultationService consultationService,
      CaseProcessingTabService caseProcessingTabService,
      FurtherInformationService furtherInformationService
  ) {
    this.applicationService = applicationService;
    this.applicationContextService = applicationContextService;
    this.applicationVersionService = applicationVersionService;
    this.applicationSummaryService = applicationSummaryService;
    this.caseProcessingActionService = caseProcessingActionService;
    this.consultationService = consultationService;
    this.caseProcessingTabService = caseProcessingTabService;
    this.furtherInformationService = furtherInformationService;
  }

  @GetMapping
  public ModelAndView caseProcessing(
      @PathVariable Integer applicationId,
      @RequestParam(required = false) CaseProcessingTab tab,
      ServiceUserDetail user
  ) {
    return getModelAndView(applicationId, tab, user);
  }

  private ModelAndView getModelAndView(Integer applicationId, CaseProcessingTab tab, ServiceUserDetail user) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    var applicationType = applicationVersion.getApplication().getType();

    var caseProcessingTabs = caseProcessingTabService.getConsulteeTabsAvailableToUser(user, applicationVersion);
    if (tab == null && !caseProcessingTabs.isEmpty()) {
      tab = caseProcessingTabs.get(0);
    }

    var modelAndView = new ModelAndView("fcs/application/consultation/caseProcessing")
        .addObject("selectedTab", tab)
        .addObject("controllerUrl", ReverseRouter.route(on(this.getClass()).caseProcessing(applicationId, null, null)))
        .addObject("actionList", caseProcessingActionService.getUserActionViews(applicationVersion, user))
        .addObject("applicationContext", applicationContextService.getApplicationContext(applicationVersion))
        .addObject("caseProcessingTabs", caseProcessingTabs)
        .addObject("wideSummaryDisplay", WIDE_SUMMARY_DISPLAY.allowed(applicationType))
        .addObject("pageTitle", applicationService.generateApplicationReference(applicationVersion));

    if (tab != null && caseProcessingTabs.contains(tab)) {
      switch (tab) {
        case FURTHER_INFORMATION -> addFurtherInformationAttributes(modelAndView, applicationVersion);
        case VIEW_APPLICATION -> applicationSummaryService.addSummarySectionsToModelAndView(applicationVersion, modelAndView);
        default -> {
        }
      }
    }

    consultationService.findLatestOpenConsultation(applicationVersion.getApplication())
        .map(ConsultationRequestView::from)
        .ifPresent(view -> modelAndView.addObject("consultationRequestView", view));

    return modelAndView;
  }

  private void addFurtherInformationAttributes(ModelAndView modelAndView, ApplicationVersion applicationVersion) {
    var consultations = consultationService.getConsultationsByApplication(applicationVersion.getApplication());
    var furtherInformation = furtherInformationService.getAllFurtherInformation(consultations);
    var furtherInformationViews = furtherInformationService.getFurtherInformationViews(furtherInformation);

    modelAndView.addObject("furtherInformationViews", furtherInformationViews);
  }

}
