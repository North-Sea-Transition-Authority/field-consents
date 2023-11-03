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

  ConsultationController(
      ApplicationService applicationService,
      ApplicationVersionService applicationVersionService,
      CaseProcessingActionService caseProcessingActionService
  ) {
    this.applicationService = applicationService;
    this.applicationVersionService = applicationVersionService;
    this.caseProcessingActionService = caseProcessingActionService;
  }

  @GetMapping
  public ModelAndView getConsultations(@PathVariable Integer applicationId, ServiceUserDetail user) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    var actionList = caseProcessingActionService.getUserActionViewsForGroup(applicationVersion, user, CONSULTATIONS);

    return new ModelAndView("fcs/application/consultation/consultations")
        .addObject("applicationReference", applicationService.generateApplicationReference(applicationVersion))
        .addObject("consultationSummaryItems", null) // TODO: FCS-454
        .addObject("actionList", actionList)
        .addObject("backLinkUrl", ReverseRouter.route(on(ApplicationCaseProcessingController.class)
            .caseProcessing(applicationId, null, null)));
  }
}
