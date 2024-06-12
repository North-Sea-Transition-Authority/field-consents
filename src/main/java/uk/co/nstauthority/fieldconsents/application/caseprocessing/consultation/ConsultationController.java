package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionGroup.CONSULTATIONS;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.ApplicationCaseProcessingController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.summary.ConsultationSummaryService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.ActionEndPoint;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@Controller
@RequestMapping("applications/{applicationId}/consultations")
@ActionEndPoint(CaseProcessingActionItem.CONSULTATIONS)
public class ConsultationController {

  private final ApplicationService applicationService;
  private final ApplicationVersionService applicationVersionService;
  private final CaseProcessingActionService caseProcessingActionService;
  private final ConsultationSummaryService consultationSummaryService;

  ConsultationController(
      ApplicationService applicationService,
      ApplicationVersionService applicationVersionService,
      CaseProcessingActionService caseProcessingActionService,
      ConsultationSummaryService consultationSummaryService
  ) {
    this.applicationService = applicationService;
    this.applicationVersionService = applicationVersionService;
    this.caseProcessingActionService = caseProcessingActionService;
    this.consultationSummaryService = consultationSummaryService;
  }

  @GetMapping
  public ModelAndView getConsultations(@PathVariable Integer applicationId, ServiceUserDetail user) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    var application = applicationVersion.getApplication();
    var actionList = caseProcessingActionService.getUserActionViewsForGroup(applicationVersion, user, CONSULTATIONS);
    var captionTitle = applicationService.getApplicationReference(
        applicationVersion,
        applicationVersion.getApplication().getType().getDisplayName() + " application"
    );

    return new ModelAndView("fcs/application/consultation/consultations")
        .addObject("captionTitle", captionTitle)
        .addObject("consultationSummaryItems", consultationSummaryService.getConsultationSummaryItems(application))
        .addObject("actionList", actionList)
        .addObject("backLinkUrl", ReverseRouter.route(on(ApplicationCaseProcessingController.class)
            .caseProcessing(applicationId, null, null)));
  }
}
