package uk.co.nstauthority.fieldconsents.application.caseprocessing.revision;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.ApplicationCaseProcessingController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem;
import uk.co.nstauthority.fieldconsents.authorisation.ActionEndPoint;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@Controller
@RequestMapping("applications/{applicationId}/revision")
public class ApplicationRevisionController {

  private final ApplicationService applicationService;
  private final ApplicationVersionService applicationVersionService;

  ApplicationRevisionController(ApplicationService applicationService, ApplicationVersionService applicationVersionService) {
    this.applicationService = applicationService;
    this.applicationVersionService = applicationVersionService;
  }

  @GetMapping("/start")
  @ActionEndPoint(CaseProcessingActionItem.REVISE_CONSENT)
  public ModelAndView getStartRevision(@PathVariable Integer applicationId) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    var applicationReference = applicationService.generateApplicationReference(applicationVersion);

    return new ModelAndView("fcs/application/revision/startRevision")
        .addObject("applicationReference", applicationReference)
        .addObject("backLinkUrl", ReverseRouter.route(on(ApplicationCaseProcessingController.class)
            .caseProcessing(applicationId, null, null)));
  }
}
