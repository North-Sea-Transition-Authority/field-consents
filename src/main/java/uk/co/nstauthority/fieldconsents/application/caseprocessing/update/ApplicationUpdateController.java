package uk.co.nstauthority.fieldconsents.application.caseprocessing.update;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionGroup.APPLICATION_UPDATES;

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
@RequestMapping("applications/{applicationId}/application-updates")
@ActionEndPoint(CaseProcessingActionItem.APPLICATION_UPDATES)
public class ApplicationUpdateController {

  private final ApplicationService applicationService;
  private final ApplicationVersionService applicationVersionService;
  private final CaseProcessingActionService caseProcessingActionService;
  private final ApplicationUpdateSummaryService applicationUpdateSummaryService;

  ApplicationUpdateController(
      ApplicationService applicationService,
      ApplicationVersionService applicationVersionService,
      CaseProcessingActionService caseProcessingActionService,
      ApplicationUpdateSummaryService applicationUpdateSummaryService
  ) {
    this.applicationService = applicationService;
    this.applicationVersionService = applicationVersionService;
    this.caseProcessingActionService = caseProcessingActionService;
    this.applicationUpdateSummaryService = applicationUpdateSummaryService;
  }

  @GetMapping
  public ModelAndView getApplicationUpdates(@PathVariable Integer applicationId, ServiceUserDetail user) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    var actionList = caseProcessingActionService.getUserActionViewsForGroup(applicationVersion, user, APPLICATION_UPDATES);
    var application = applicationVersion.getApplication();
    var applicationUpdateSummaryItems = applicationUpdateSummaryService.getApplicationUpdateSummaryItems(application);
    var captionTitle = applicationService.getApplicationReference(
        applicationVersion,
        applicationVersion.getApplication().getType().getDisplayName() + " application"
    );

    return new ModelAndView("fcs/application/update/applicationUpdates")
        .addObject("captionTitle", captionTitle)
        .addObject("applicationUpdateSummaryItems", applicationUpdateSummaryItems)
        .addObject("actionList", actionList)
        .addObject("backLinkUrl", ReverseRouter.route(on(ApplicationCaseProcessingController.class)
            .caseProcessing(applicationId, null, null)));
  }

}
