package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformation.request;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import jakarta.validation.Valid;
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
import uk.co.nstauthority.fieldconsents.application.caseprocessing.ConsulteeCaseProcessingController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.Consultation;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationRequestView;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformation.FurtherInformationService;
import uk.co.nstauthority.fieldconsents.application.summary.ApplicationSummaryService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.ActionEndPoint;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBannerUtil;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.workarea.WorkAreaController;

@Controller
@RequestMapping("applications/{applicationId}/consultations/further-information/request")
@ActionEndPoint(CaseProcessingActionItem.CONSULTATION_FURTHER_INFORMATION_REQUEST)
public class FurtherInformationRequestController {

  private static final String PAGE_TITLE = "Request further information";

  private final ApplicationService applicationService;
  private final ApplicationVersionService applicationVersionService;
  private final ApplicationSummaryService applicationSummaryService;
  private final ConsultationService consultationService;
  private final FurtherInformationService furtherInformationService;

  FurtherInformationRequestController(
      ApplicationService applicationService,
      ApplicationVersionService applicationVersionService,
      ApplicationSummaryService applicationSummaryService,
      ConsultationService consultationService,
      FurtherInformationService furtherInformationService
  ) {
    this.applicationService = applicationService;
    this.applicationVersionService = applicationVersionService;
    this.applicationSummaryService = applicationSummaryService;
    this.consultationService = consultationService;
    this.furtherInformationService = furtherInformationService;
  }

  @GetMapping
  public ModelAndView getRequestForm(@PathVariable Integer applicationId) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    var application = applicationVersion.getApplication();
    var consultation = consultationService.getLatestOpenConsultation(application);

    return getModelAndView(applicationVersion, consultation, FurtherInformationRequestForm.empty());
  }

  @PostMapping
  ModelAndView submitRequestForm(
      @PathVariable Integer applicationId,
      @Valid @ModelAttribute("form") FurtherInformationRequestForm form,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes,
      ServiceUserDetail user
  ) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    var application = applicationVersion.getApplication();
    var consultation = consultationService.getLatestOpenConsultation(application);

    if (bindingResult.hasErrors()) {
      return getModelAndView(applicationVersion, consultation, form);
    }

    furtherInformationService.saveFurtherInformationRequest(consultation, user, form.requestText());

    var applicationReference = applicationService.generateApplicationReference(applicationVersion);
    var notificationBannerMessage = "Further information requested for application %s".formatted(applicationReference);
    NotificationBannerUtil.addSuccessNotification(redirectAttributes, notificationBannerMessage);

    return ReverseRouter.redirect(on(WorkAreaController.class).getWorkArea(null, null));
  }

  private ModelAndView getModelAndView(
      ApplicationVersion applicationVersion,
      Consultation consultation,
      FurtherInformationRequestForm form
  ) {
    var applicationId = applicationVersion.getApplication().getId();
    var applicationReference = applicationService.generateApplicationReference(applicationVersion);
    var backLinkUrl = ReverseRouter.route(on(ConsulteeCaseProcessingController.class)
        .caseProcessing(applicationId, null, null));

    var modelAndView = new ModelAndView("fcs/application/consultation/further-information/requestForm")
        .addObject("pageTitle", PAGE_TITLE)
        .addObject("backLinkUrl", backLinkUrl)
        .addObject("applicationReference", applicationReference)
        .addObject("consultationRequestView", ConsultationRequestView.from(consultation))
        .addObject("form", form);

    applicationSummaryService.addSummarySectionsToModelAndView(applicationVersion, modelAndView);

    return modelAndView;
  }

}
