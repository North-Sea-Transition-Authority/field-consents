package uk.co.nstauthority.fieldconsents.application.caseprocessing;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTypeFeature.WIDE_SUMMARY_DISPLAY;

import java.util.Optional;
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
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.summary.ConsultationSummaryService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationOrRegulatorRole;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationStatus;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.Role;

@Controller
@RequestMapping("applications/{applicationId}/consultation-case-processing")
@HasApplicationStatus(statuses = {
    ApplicationVersionStatus.IN_PROGRESS,
    ApplicationVersionStatus.AWAITING_PAYMENT,
    ApplicationVersionStatus.SUBMITTED,
    ApplicationVersionStatus.CONSENTED,
    ApplicationVersionStatus.WITHDRAWN,
    ApplicationVersionStatus.CLOSED
})
// should match RoleGroup.CONSULTEE_VIEW_CASE_PROCESSING_ROLES
@HasApplicationOrRegulatorRole(consulteeRoles = {Role.ALLOCATOR, Role.RESPONDER})
public class ConsulteeCaseProcessingController {

  private final ApplicationService applicationService;
  private final ApplicationContextService applicationContextService;
  private final ApplicationVersionService applicationVersionService;
  private final CaseProcessingActionService caseProcessingActionService;
  private final ConsultationService consultationService;
  private final CaseProcessingTabService caseProcessingTabService;
  private final ConsultationSummaryService consultationSummaryService;
  private final CaseProcessingControllerHelperService caseProcessingControllerHelperService;

  ConsulteeCaseProcessingController(
      ApplicationService applicationService,
      ApplicationContextService applicationContextService,
      ApplicationVersionService applicationVersionService,
      CaseProcessingActionService caseProcessingActionService,
      ConsultationService consultationService,
      CaseProcessingTabService caseProcessingTabService,
      ConsultationSummaryService consultationSummaryService,
      CaseProcessingControllerHelperService caseProcessingControllerHelperService
  ) {
    this.applicationService = applicationService;
    this.applicationContextService = applicationContextService;
    this.applicationVersionService = applicationVersionService;
    this.caseProcessingActionService = caseProcessingActionService;
    this.consultationService = consultationService;
    this.caseProcessingTabService = caseProcessingTabService;
    this.consultationSummaryService = consultationSummaryService;
    this.caseProcessingControllerHelperService = caseProcessingControllerHelperService;
  }

  @GetMapping
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

    var caseProcessingTabs = caseProcessingTabService.getConsulteeTabsAvailableToUser(user, latestApplicationVersion);
    if (tab == null && !caseProcessingTabs.isEmpty()) {
      tab = caseProcessingTabs.getFirst();
    }

    var modelAndView = new ModelAndView("fcs/application/consultation/caseProcessing")
        .addObject("selectedTab", tab)
        .addObject("controllerUrl", ReverseRouter.route(on(this.getClass()).caseProcessing(applicationId, null, null, null)))
        .addObject("actionList", caseProcessingActionService.getTopLevelActionItemViews(latestApplicationVersion, user))
        .addObject("applicationContext", applicationContextService.getApplicationContext(latestApplicationVersion))
        .addObject("caseProcessingTabs", caseProcessingTabs)
        .addObject("wideSummaryDisplay", WIDE_SUMMARY_DISPLAY.allowed(applicationType))
        .addObject("pageTitle", applicationService.generateApplicationReference(latestApplicationVersion));

    if (tab != null && caseProcessingTabs.contains(tab)) {
      switch (tab) {
        case CONSULTATIONS -> addConsultationSummaryItems(modelAndView, latestApplicationVersion, user);
        case VIEW_APPLICATION -> caseProcessingControllerHelperService.addSummarySectionsAndVersionOptionsToModelAndView(
            selectedApplicationVersion,
            modelAndView,
            user
        );
        default -> {
        }
      }
    }

    consultationService.findLatestOpenConsultation(latestApplicationVersion.getApplication())
        .map(ConsultationRequestView::from)
        .ifPresent(view -> modelAndView.addObject("consultationRequestView", view));

    return modelAndView;
  }

  private void addConsultationSummaryItems(ModelAndView modelAndView,
                                           ApplicationVersion applicationVersion,
                                           ServiceUserDetail user) {
    modelAndView.addObject("consultationSummaryItems", consultationSummaryService
        .getConsultationSummaryItemsForUser(applicationVersion.getApplication(), user));
  }

}
