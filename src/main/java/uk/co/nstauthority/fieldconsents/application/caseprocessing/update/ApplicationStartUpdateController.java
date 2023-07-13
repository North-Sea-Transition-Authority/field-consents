package uk.co.nstauthority.fieldconsents.application.caseprocessing.update;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.OPERATOR_UPDATE_APPLICATION;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.summary.ApplicationSummaryController;
import uk.co.nstauthority.fieldconsents.application.tasklist.shared.ApplicationTaskListController;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.ActionEndPoint;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@Controller
@RequestMapping("applications/{applicationId}/update")
public class ApplicationStartUpdateController {

  private final ApplicationService applicationService;
  private final ApplicationVersionService applicationVersionService;
  private final ApplicationUpdateService applicationUpdateService;

  @Autowired
  public ApplicationStartUpdateController(ApplicationService applicationService,
                                          ApplicationVersionService applicationVersionService,
                                          ApplicationUpdateService applicationUpdateService) {
    this.applicationService = applicationService;
    this.applicationVersionService = applicationVersionService;
    this.applicationUpdateService = applicationUpdateService;
  }

  @GetMapping
  @ActionEndPoint(OPERATOR_UPDATE_APPLICATION)
  public ModelAndView updateApplicationEntryPoint(@PathVariable Integer applicationId) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    if (ApplicationVersionStatus.IN_PROGRESS.equals(applicationVersion.getStatus()) && applicationVersion.getVersion() > 1) {
      return ReverseRouter.redirect(on(ApplicationTaskListController.class).getTaskList(applicationId));
    } else if (ApplicationVersionStatus.SUBMITTED.equals(applicationVersion.getStatus())) {
      return ReverseRouter.redirect(on(ApplicationStartUpdateController.class).renderStartUpdate(applicationId));
    } else {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, """
          Application version id [%d] with status [%s] and version [%d] expected conditions with one of the following:
          1. Status is %s and application version is greater than [1]
          2. Status is %s""".formatted(
              applicationVersion.getId(),
              applicationVersion.getStatus().name(),
              applicationVersion.getVersion(),
              ApplicationVersionStatus.IN_PROGRESS.name(),
              ApplicationVersionStatus.SUBMITTED.name()
      ));
    }
  }

  @GetMapping("/start")
  @ActionEndPoint(OPERATOR_UPDATE_APPLICATION)
  public ModelAndView renderStartUpdate(@PathVariable Integer applicationId) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    var applicationReference = applicationService.generateApplicationReference(applicationVersion);

    return new ModelAndView("fcs/application/startApplicationUpdate")
        .addObject("startActionUrl",
            ReverseRouter.route(on(ApplicationStartUpdateController.class).startUpdate(applicationId, null)))
        .addObject("applicationReference", applicationReference)
        .addObject("backLinkUrl",
            ReverseRouter.route(on(ApplicationSummaryController.class)
                .getApplicationSummary(applicationId, null)));
  }

  @PostMapping("/start")
  @ActionEndPoint(OPERATOR_UPDATE_APPLICATION)
  public ModelAndView startUpdate(@PathVariable Integer applicationId,
                                  ServiceUserDetail user) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    applicationUpdateService.startApplicationUpdate(applicationVersion, user);
    return ReverseRouter.redirect(on(ApplicationTaskListController.class).getTaskList(applicationId));
  }
}
