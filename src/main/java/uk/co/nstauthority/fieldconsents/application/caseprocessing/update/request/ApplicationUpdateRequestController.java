package uk.co.nstauthority.fieldconsents.application.caseprocessing.update.request;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem.APPLICATION_UPDATE_REQUEST;

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
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformation.FurtherInformationService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateService;
import uk.co.nstauthority.fieldconsents.application.summary.ApplicationSummaryService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.ActionEndPoint;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBannerUtil;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@Controller
@RequestMapping("applications/{applicationId}/application-update-request")
@ActionEndPoint(APPLICATION_UPDATE_REQUEST)
public class ApplicationUpdateRequestController {

  static final String REQUEST_PAGE_TITLE = "Request application update";

  private final ApplicationService applicationService;
  private final ApplicationVersionService applicationVersionService;
  private final ApplicationUpdateService applicationUpdateService;
  private final ApplicationSummaryService applicationSummaryService;
  private final ConsultationService consultationService;
  private final FurtherInformationService furtherInformationService;
  private final ApplicationUpdateRequestFormValidator applicationUpdateRequestFormValidator;

  ApplicationUpdateRequestController(
      ApplicationService applicationService,
      ApplicationVersionService applicationVersionService,
      ApplicationUpdateService applicationUpdateService,
      ApplicationSummaryService applicationSummaryService,
      ConsultationService consultationService,
      FurtherInformationService furtherInformationService,
      ApplicationUpdateRequestFormValidator applicationUpdateRequestFormValidator
  ) {
    this.applicationService = applicationService;
    this.applicationVersionService = applicationVersionService;
    this.applicationUpdateService = applicationUpdateService;
    this.applicationSummaryService = applicationSummaryService;
    this.consultationService = consultationService;
    this.furtherInformationService = furtherInformationService;
    this.applicationUpdateRequestFormValidator = applicationUpdateRequestFormValidator;
  }

  @GetMapping
  public ModelAndView getApplicationUpdateRequest(@PathVariable Integer applicationId,
                                                  ServiceUserDetail user) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    var form = applicationUpdateService.getApplicationUpdateRequestForm(applicationVersion);
    return getModelAndView(applicationVersion, form, user);
  }

  @PostMapping
  public ModelAndView sendApplicationUpdateRequest(
      @PathVariable Integer applicationId,
      @ModelAttribute("form") ApplicationUpdateRequestForm form,
      BindingResult bindingResult,
      ServiceUserDetail user,
      RedirectAttributes redirectAttributes
  ) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    applicationUpdateRequestFormValidator.validate(form, bindingResult);

    if (bindingResult.hasErrors()) {
      return getModelAndView(applicationVersion, form, user);
    }

    var deadlineInstant = DateUtils.datePickerWithTimeStringToInstant(
        form.getDeadlineDate(),
        form.getDeadlineHours(),
        form.getDeadlineMinutes()
    );

    applicationUpdateService.saveApplicationUpdateRequest(
        applicationVersion,
        deadlineInstant,
        form.getRequestText().getInputValue(),
        user
    );

    NotificationBannerUtil.addSuccessNotification(redirectAttributes, "Application update request sent to operator");

    return ReverseRouter.redirect(on(ApplicationUpdateController.class).getApplicationUpdates(applicationId, null));
  }

  private ModelAndView getModelAndView(ApplicationVersion applicationVersion,
                                       ApplicationUpdateRequestForm form,
                                       ServiceUserDetail user) {
    var applicationId = applicationVersion.getApplication().getId();
    var applicationReference = applicationService.generateApplicationReference(applicationVersion);

    var modelAndView = new ModelAndView("fcs/application/update/applicationUpdateRequest")
        .addObject("pageTitle", REQUEST_PAGE_TITLE)
        .addObject("form", form)
        .addObject("applicationReference", applicationReference)
        .addObject("backLinkUrl", ReverseRouter.route(on(ApplicationUpdateController.class)
            .getApplicationUpdates(applicationId, null)));

    applicationSummaryService.addSummarySectionsToModelAndView(applicationVersion, modelAndView, user);

    consultationService
        .findLatestOpenConsultation(applicationVersion.getApplication())
        .flatMap(furtherInformationService::findLatestOpenFurtherInformation)
        .map(furtherInformationService::getFurtherInformationView)
        .ifPresent(view -> modelAndView.addObject("furtherInformationView", view));

    return modelAndView;
  }
}
