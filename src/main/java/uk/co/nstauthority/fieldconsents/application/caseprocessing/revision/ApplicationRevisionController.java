package uk.co.nstauthority.fieldconsents.application.caseprocessing.revision;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.ApplicationCaseProcessingController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.IndustryCaseProcessingController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem;
import uk.co.nstauthority.fieldconsents.application.summary.ApplicationSummaryController;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.ActionEndPoint;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.TeamService;

@Controller
@RequestMapping("applications/{applicationId}/revision")
@ActionEndPoint(CaseProcessingActionItem.REVISE_CONSENT)
public class ApplicationRevisionController {

  private final ApplicationService applicationService;
  private final ApplicationVersionService applicationVersionService;
  private final ApplicationRevisionService applicationRevisionService;
  private final TeamService teamService;

  ApplicationRevisionController(
      ApplicationService applicationService,
      ApplicationVersionService applicationVersionService,
      ApplicationRevisionService applicationRevisionService,
      TeamService teamService
  ) {
    this.applicationService = applicationService;
    this.applicationRevisionService = applicationRevisionService;
    this.applicationVersionService = applicationVersionService;
    this.teamService = teamService;
  }

  @GetMapping("/start")
  public ModelAndView getStartRevision(@PathVariable Integer applicationId, ServiceUserDetail user) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    var applicationReference = applicationService.generateApplicationReference(applicationVersion);

    var regulatorUser = teamService.isRegulatorUser(user);
    var backLinkUrl = regulatorUser
        ? ReverseRouter.route(on(ApplicationCaseProcessingController.class)
        .caseProcessing(applicationId, null, null, null))
        : ReverseRouter.route(on(IndustryCaseProcessingController.class)
            .getIndustryCaseProcessing(applicationId, null, null, null));

    return new ModelAndView("fcs/application/revision/startRevision")
        .addObject("applicationReference", applicationReference)
        .addObject("backLinkUrl", backLinkUrl)
        .addObject("startRevisionUrl", ReverseRouter.route(on(ApplicationRevisionController.class)
            .startRevision(applicationId, null)));
  }

  @PostMapping("/start")
  public ModelAndView startRevision(@PathVariable Integer applicationId, ServiceUserDetail user) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    var newApplication = applicationRevisionService.startApplicationRevision(applicationVersion, user);

    return ReverseRouter.redirect(on(ApplicationSummaryController.class).getApplicationSummary(newApplication.getId(), null));
  }
}
