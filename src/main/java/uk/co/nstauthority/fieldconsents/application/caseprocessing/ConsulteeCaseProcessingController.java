package uk.co.nstauthority.fieldconsents.application.caseprocessing;

import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.ALLOCATE_CONSULTATION;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.RESPOND_TO_CONSULTATION;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationRequestView;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformationrequest.FurtherInformationRequestService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformationrequest.FurtherInformationRequestView;
import uk.co.nstauthority.fieldconsents.application.summary.ApplicationSummaryService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationPermission;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationStatus;

@Controller
@RequestMapping("applications/{applicationId}/consultation-case-processing")
@HasApplicationStatus(statuses = ApplicationVersionStatus.SUBMITTED)
@HasApplicationPermission(permissions = {ALLOCATE_CONSULTATION, RESPOND_TO_CONSULTATION})
public class ConsulteeCaseProcessingController {

  private final ApplicationService applicationService;
  private final ApplicationVersionService applicationVersionService;
  private final ApplicationSummaryService applicationSummaryService;
  private final CaseProcessingActionService caseProcessingActionService;
  private final ConsultationService consultationService;
  private final FurtherInformationRequestService furtherInformationRequestService;

  ConsulteeCaseProcessingController(
      ApplicationService applicationService,
      ApplicationVersionService applicationVersionService,
      ApplicationSummaryService applicationSummaryService,
      CaseProcessingActionService caseProcessingActionService,
      ConsultationService consultationService,
      FurtherInformationRequestService furtherInformationRequestService
  ) {
    this.applicationService = applicationService;
    this.applicationVersionService = applicationVersionService;
    this.applicationSummaryService = applicationSummaryService;
    this.caseProcessingActionService = caseProcessingActionService;
    this.consultationService = consultationService;
    this.furtherInformationRequestService = furtherInformationRequestService;
  }

  @GetMapping
  public ModelAndView getApplicationCaseProcessing(@PathVariable Integer applicationId, ServiceUserDetail user) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    var pageTitle = applicationService.generateApplicationReference(applicationVersion);

    var modelAndView = applicationSummaryService.getApplicationSummaryModelAndView(
        applicationVersion,
        "fcs/application/consultation/caseProcessing",
        pageTitle
    );

    var consultationOptional = consultationService.findLatestOpenConsultation(applicationVersion.getApplication());

    consultationOptional
        .map(ConsultationRequestView::from)
        .ifPresent(view -> modelAndView.addObject("consultationRequestView", view));

    consultationOptional
        .flatMap(furtherInformationRequestService::findLatestOpenFurtherInformationRequest)
        .map(FurtherInformationRequestView::from)
        .ifPresent(view -> modelAndView.addObject("furtherInformationRequestView", view));

    var actionList = caseProcessingActionService.getUserActionViews(applicationVersion, user);
    modelAndView.addObject("actionList", actionList);

    return modelAndView;
  }

}
