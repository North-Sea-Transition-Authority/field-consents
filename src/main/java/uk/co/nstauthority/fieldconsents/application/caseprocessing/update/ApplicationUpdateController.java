package uk.co.nstauthority.fieldconsents.application.caseprocessing.update;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.APPLICATION_UPDATE_REQUEST;

import java.time.Clock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.ApplicationCaseProcessingController;
import uk.co.nstauthority.fieldconsents.application.summary.ApplicationSummaryService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.ActionEndPoint;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBannerUtil;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@Controller
@RequestMapping("applications/{applicationId}")
public class ApplicationUpdateController {

  static final String REQUEST_PAGE_TITLE = "Request application update";

  private final ApplicationService applicationService;

  private final ApplicationVersionService applicationVersionService;

  private final ApplicationUpdateService applicationUpdateService;

  private final ApplicationSummaryService applicationSummaryService;

  private final ApplicationUpdateRequestFormValidator applicationUpdateRequestFormValidator;

  private final Clock clock;

  @Autowired
  public ApplicationUpdateController(ApplicationService applicationService,
                                     ApplicationVersionService applicationVersionService,
                                     ApplicationUpdateService applicationUpdateService,
                                     ApplicationSummaryService applicationSummaryService,
                                     ApplicationUpdateRequestFormValidator applicationUpdateRequestFormValidator,
                                     Clock clock) {
    this.applicationService = applicationService;
    this.applicationVersionService = applicationVersionService;
    this.applicationUpdateService = applicationUpdateService;
    this.applicationSummaryService = applicationSummaryService;
    this.applicationUpdateRequestFormValidator = applicationUpdateRequestFormValidator;
    this.clock = clock;
  }

  @GetMapping("application-update-request")
  @ActionEndPoint(APPLICATION_UPDATE_REQUEST)
  public ModelAndView getApplicationUpdateRequest(@PathVariable Integer applicationId) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    var applicationUpdateRequestForm =
        applicationUpdateService.getApplicationUpdateRequestForm(applicationVersion);

    var modelAndView = getApplicationUpdateRequestModelAndView(applicationVersion);
    modelAndView.addObject("form", applicationUpdateRequestForm);

    return modelAndView;
  }

  private ModelAndView getApplicationUpdateRequestModelAndView(ApplicationVersion applicationVersion) {
    var applicationReference = applicationService.generateApplicationReference(applicationVersion);
    var applicationId = applicationVersion.getApplication().getId();

    var modelAndView = applicationSummaryService.getApplicationSummaryModelAndView(
        applicationVersion,
        "fcs/application/update/applicationUpdateRequest",
        REQUEST_PAGE_TITLE,
        ReverseRouter.route(on(ApplicationCaseProcessingController.class)
            .getApplicationCaseProcessing(applicationId, null))
    );

    return modelAndView.addObject("applicationReference", applicationReference);
  }

  @PostMapping("application-update-request")
  @ActionEndPoint(APPLICATION_UPDATE_REQUEST)
  public ModelAndView sendApplicationUpdateRequest(@PathVariable Integer applicationId,
                                                   @ModelAttribute("form") ApplicationUpdateRequestForm form,
                                                   BindingResult bindingResult,
                                                   ServiceUserDetail user,
                                                   RedirectAttributes redirectAttributes) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    applicationUpdateRequestFormValidator.validate(form, bindingResult);

    if (bindingResult.hasErrors()) {
      return getApplicationUpdateRequestModelAndView(applicationVersion);
    }

    var deadlineInstant = DateUtils.datePickerWithTimeStringToInstant(
        form.getDeadlineDate(), form.getDeadlineHours(), form.getDeadlineMinutes(), clock);

    applicationUpdateService.saveApplicationUpdateRequest(applicationVersion, deadlineInstant,
        form.getRequestText().getInputValue(), user);

    NotificationBannerUtil.addSuccessNotification(redirectAttributes,
        "Application update request sent to operator");

    return ReverseRouter
        .redirect(on(ApplicationCaseProcessingController.class).getApplicationCaseProcessing(applicationId, null));
  }
}
