package uk.co.nstauthority.fieldconsents.application.caseprocessing.closure;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.CLOSE_APPLICATION;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.ApplicationCaseProcessingController;
import uk.co.nstauthority.fieldconsents.application.summary.ApplicationSummaryService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.ActionEndPoint;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBannerUtil;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.workarea.WorkAreaController;

@Controller
@RequestMapping("applications/{applicationId}/close")
public class ApplicationClosureController {

  private static final String PAGE_TITLE = "Are you sure you want to close this application?";

  private final ApplicationVersionService applicationVersionService;
  private final ApplicationSummaryService applicationSummaryService;
  private final ApplicationService applicationService;

  ApplicationClosureController(
      ApplicationVersionService applicationVersionService,
      ApplicationSummaryService applicationSummaryService,
      ApplicationService applicationService
  ) {
    this.applicationVersionService = applicationVersionService;
    this.applicationSummaryService = applicationSummaryService;
    this.applicationService = applicationService;
  }

  @GetMapping
  @ActionEndPoint(CLOSE_APPLICATION)
  public ModelAndView getConfirmation(@PathVariable Integer applicationId, ServiceUserDetail user) {
    var modelAndView = new ModelAndView("fcs/application/closureForm")
        .addObject("pageTitle", PAGE_TITLE)
        .addObject(
            "backLinkUrl",
            ReverseRouter.route(on(ApplicationCaseProcessingController.class).caseProcessing(applicationId, null, null, null))
        );

    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    applicationSummaryService.addSummarySectionsToModelAndView(applicationVersion, modelAndView, user);

    return modelAndView;
  }

  @PostMapping
  @ActionEndPoint(CLOSE_APPLICATION)
  ModelAndView closeApplication(@PathVariable Integer applicationId, RedirectAttributes redirectAttributes) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    applicationVersionService.getAllNonDeletedApplicationVersionsByApplicationId(applicationId)
            .forEach(applicationVersionService::closeApplicationVersion);

    var applicationReference = applicationService.generateApplicationReference(applicationVersion);

    NotificationBannerUtil.addSuccessNotification(
        redirectAttributes,
        "Application %s has been successfully closed".formatted(applicationReference)
    );

    return ReverseRouter.redirect(on(WorkAreaController.class).getWorkArea(null, null));
  }
}
