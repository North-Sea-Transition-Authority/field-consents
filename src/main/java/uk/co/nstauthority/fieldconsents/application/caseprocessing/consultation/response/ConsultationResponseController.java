package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.response;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.EnumSet;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.fivium.fileuploadlibrary.FileUploadLibraryUtils;
import uk.co.fivium.formlibrary.input.StringInput;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.ConsulteeCaseProcessingController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.Consultation;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationRequestView;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.EiaRegsResponseType;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.HabitatsRegsResponseType;
import uk.co.nstauthority.fieldconsents.application.summary.ApplicationSummaryService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.ActionEndPoint;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBannerUtil;
import uk.co.nstauthority.fieldconsents.file.FieldConsentsFileService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.workarea.WorkAreaController;

@Controller
@RequestMapping("applications/{applicationId}/consultations/respond")
@ActionEndPoint(CaseProcessingActionItem.CONSULTATION_RESPONSE)
public class ConsultationResponseController {

  private static final String PAGE_TITLE = "Consultation response";

  private final ApplicationService applicationService;
  private final ApplicationVersionService applicationVersionService;
  private final ApplicationSummaryService applicationSummaryService;
  private final ConsultationResponseFormValidator validator;
  private final ConsultationService consultationService;
  private final FieldConsentsFileService fieldConsentsFileService;

  ConsultationResponseController(
      ApplicationService applicationService,
      ApplicationVersionService applicationVersionService,
      ApplicationSummaryService applicationSummaryService,
      ConsultationResponseFormValidator validator,
      ConsultationService consultationService,
      FieldConsentsFileService fieldConsentsFileService
  ) {
    this.applicationService = applicationService;
    this.applicationVersionService = applicationVersionService;
    this.applicationSummaryService = applicationSummaryService;
    this.validator = validator;
    this.consultationService = consultationService;
    this.fieldConsentsFileService = fieldConsentsFileService;
  }

  @GetMapping
  public ModelAndView getResponseForm(@PathVariable Integer applicationId) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    var consultation = consultationService.getLatestOpenConsultation(applicationVersion.getApplication());

    return getModelAndView(applicationVersion, consultation, ConsultationResponseForm.from(consultation));
  }

  @PostMapping
  ModelAndView submitResponseForm(
      @PathVariable Integer applicationId,
      @ModelAttribute("form") ConsultationResponseForm form,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes,
      ServiceUserDetail user
  ) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    var consultation = consultationService.getLatestOpenConsultation(applicationVersion.getApplication());

    validator.validate(form, bindingResult, applicationVersion);

    if (bindingResult.hasErrors()) {
      // TODO: https://jira.fivium.co.uk/browse/FDS-460
      var descriptionsByFileId = FileUploadLibraryUtils.getFileDescriptionsByFileId(form.documents());
      form.documents().clear();
      form.documents().addAll(fieldConsentsFileService.getUploadedFileForms(descriptionsByFileId.keySet()));
      form.documents().forEach(fileForm -> fileForm.setFileDescription(descriptionsByFileId.get(fileForm.getFileId())));

      return getModelAndView(applicationVersion, consultation, form);
    }

    consultationService.saveConsultationResponse(
        applicationVersion,
        consultation,
        user,
        form.habitatsRegsResponseType(),
        form.getHabitatsRegsDescription().map(StringInput::getInputValue).orElse(null),
        form.eiaRegsResponseType(),
        form.getEiaRegsDescription().map(StringInput::getInputValue).orElse(null),
        form.documents()
    );

    var applicationReference = applicationService.generateApplicationReference(applicationVersion);
    NotificationBannerUtil.addSuccessNotification(
        redirectAttributes,
        "Consultation response submitted for application %s".formatted(applicationReference)
    );

    return ReverseRouter.redirect(on(WorkAreaController.class).getWorkArea(null, null));
  }

  private ModelAndView getModelAndView(
      ApplicationVersion applicationVersion,
      Consultation consultation,
      ConsultationResponseForm form
  ) {
    var application = applicationVersion.getApplication();
    var applicationId = application.getId();
    var applicationReference = applicationService.generateApplicationReference(applicationVersion);
    var fileUploadAttributes = fieldConsentsFileService.fileUploadComponentAttributes(form.documents());

    var modelAndView = new ModelAndView("fcs/application/consultation/responseForm")
        .addObject("pageTitle", PAGE_TITLE)
        .addObject("form", form)
        .addObject("fileUploadAttributes", fileUploadAttributes)
        .addObject("applicationReference", applicationReference)
        .addObject("habitatsRegsRadioOptions", EnumSet.allOf(HabitatsRegsResponseType.class))
        .addObject("consultationSummaryView", ConsultationRequestView.from(consultation))
        .addObject("backLinkUrl", ReverseRouter.route(on(ConsulteeCaseProcessingController.class)
            .caseProcessing(applicationId, null, null)));

    applicationSummaryService.addSummarySectionsToModelAndView(applicationVersion, modelAndView);

    if (consultationService.requiresEiaRegsResponse(applicationVersion)) {
      modelAndView.addObject("eiaRegsRadioOptions", EnumSet.allOf(EiaRegsResponseType.class));
    }

    return modelAndView;
  }

}
