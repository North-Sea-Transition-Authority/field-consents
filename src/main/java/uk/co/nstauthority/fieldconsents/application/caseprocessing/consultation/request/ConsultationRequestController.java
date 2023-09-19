package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.request;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

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
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.ActionEndPoint;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBannerUtil;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.workarea.WorkAreaController;

@Controller
@RequestMapping("applications/{applicationId}/request-consultation")
@ActionEndPoint(CaseProcessingActionItem.CASE_OFFICER_REQUEST_CONSULTATION)
public class ConsultationRequestController {

  private final ApplicationService applicationService;
  private final ApplicationVersionService applicationVersionService;
  private final ConsultationRequestFormValidator validator;
  private final ConsultationService consultationService;

  ConsultationRequestController(
      ApplicationService applicationService,
      ApplicationVersionService applicationVersionService,
      ConsultationRequestFormValidator validator,
      ConsultationService consultationService
  ) {
    this.applicationService = applicationService;
    this.applicationVersionService = applicationVersionService;
    this.validator = validator;
    this.consultationService = consultationService;
  }

  @GetMapping
  public ModelAndView getConsultationRequestForm(@PathVariable Integer applicationId) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    return getModelAndView(applicationVersion, ConsultationRequestForm.empty());
  }

  @PostMapping
  ModelAndView submitConsultationRequestForm(
      @PathVariable Integer applicationId,
      @ModelAttribute("form") ConsultationRequestForm form,
      ServiceUserDetail userDetail,
      RedirectAttributes redirectAttributes,
      BindingResult bindingResult
  ) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    validator.validate(form, bindingResult);

    if (bindingResult.hasErrors()) {
      return getModelAndView(applicationVersion, form);
    }

    var deadline = DateUtils.datePickerWithTimeStringToInstant(
        form.deadlineDate(),
        form.deadlineHours(),
        form.deadlineMinutes()
    );

    consultationService.requestConsultation(applicationVersion, deadline, userDetail);

    var consultationTeam = consultationService.getConsultationTeam();
    var notificationBannerMessage = "Consultation request has been sent to %s".formatted(consultationTeam.getDisplayName());
    NotificationBannerUtil.addSuccessNotification(redirectAttributes, notificationBannerMessage);

    return ReverseRouter.redirect(on(WorkAreaController.class).getWorkArea(null, null));
  }

  private ModelAndView getModelAndView(ApplicationVersion applicationVersion, ConsultationRequestForm form) {
    var applicationReference = applicationService.generateApplicationReference(applicationVersion);
    var consultationTeam = consultationService.getConsultationTeam();
    var pageTitle = "Request consultation from %s".formatted(consultationTeam.getDisplayName());

    var applicationId = applicationVersion.getApplication().getId();
    var backLinkUrl = ReverseRouter.route(on(ApplicationCaseProcessingController.class)
        .getApplicationCaseProcessing(applicationId, null));

    return new ModelAndView("fcs/application/consultation/requestForm")
        .addObject("backLinkUrl", backLinkUrl)
        .addObject("pageTitle", pageTitle)
        .addObject("applicationReference", applicationReference)
        .addObject("form", form);
  }

}
