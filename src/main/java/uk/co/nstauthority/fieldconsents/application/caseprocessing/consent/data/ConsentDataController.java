package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data;

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
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure.ConsentFigureUnitService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.preparation.ConsentPreparationController;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.authorisation.ActionEndPoint;
import uk.co.nstauthority.fieldconsents.document.FieldConsentsDocumentInstanceService;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBannerUtil;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@Controller
@RequestMapping("/applications/{applicationId}/consent-data")
@ActionEndPoint(CaseProcessingActionItem.CONSENT_PREPARATION)
public class ConsentDataController {

  private final ApplicationService applicationService;
  private final ApplicationVersionService applicationVersionService;
  private final ConsentDataService consentDataService;
  private final ConsentDataFormValidator consentDataFormValidator;
  private final ConsentFigureUnitService consentFigureUnitService;
  private final ConsentLengthService consentLengthService;
  private final FieldConsentsDocumentInstanceService fieldConsentsDocumentInstanceService;

  ConsentDataController(
      ApplicationService applicationService,
      ApplicationVersionService applicationVersionService,
      ConsentDataService consentDataService,
      ConsentDataFormValidator consentDataFormValidator,
      ConsentFigureUnitService consentFigureUnitService,
      ConsentLengthService consentLengthService,
      FieldConsentsDocumentInstanceService fieldConsentsDocumentInstanceService
  ) {
    this.applicationService = applicationService;
    this.applicationVersionService = applicationVersionService;
    this.consentDataService = consentDataService;
    this.consentDataFormValidator = consentDataFormValidator;
    this.consentFigureUnitService = consentFigureUnitService;
    this.consentLengthService = consentLengthService;
    this.fieldConsentsDocumentInstanceService = fieldConsentsDocumentInstanceService;
  }

  @GetMapping
  public ModelAndView getConsentDataAndRedirect(@PathVariable Integer applicationId) {
    var application = applicationService.getApplicationById(applicationId);
    var consentDataOptional = consentDataService.findConsentData(application);

    if (consentDataOptional.isPresent()) {
      return ReverseRouter.redirect(on(ConsentPreparationController.class).viewConsentPreparationPage(applicationId));
    }

    return ReverseRouter.redirect(on(this.getClass()).editConsentData(applicationId));
  }

  @GetMapping("/edit")
  public ModelAndView editConsentData(@PathVariable Integer applicationId) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    var consentLengthDetails = consentLengthService.getConsentLengthDetails(applicationVersion);
    var consentLengthType = consentLengthDetails.getConsentLength();

    var form = consentDataService.getPrefilledConsentDataForm(applicationVersion, consentLengthDetails);

    return modelAndView(applicationVersion, consentLengthType, form);
  }

  @PostMapping("/edit")
  public ModelAndView submitConsentData(
      @PathVariable Integer applicationId,
      @ModelAttribute("form") ConsentDataForm form,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes
  ) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    var application = applicationVersion.getApplication();

    var consentLengthType = consentLengthService.getConsentLengthDetails(applicationVersion).getConsentLength();

    consentDataFormValidator.validate(form, application, consentLengthType, bindingResult);

    if (bindingResult.hasErrors()) {
      return modelAndView(applicationVersion, consentLengthType, form);
    }

    if (consentDataService.findConsentData(application).isEmpty()) {
      fieldConsentsDocumentInstanceService.createDocumentInstancesForApplication(application);
    }

    consentDataService.saveConsentData(application, consentLengthType, form);

    NotificationBannerUtil.addSuccessNotification(redirectAttributes, "Consent data saved");

    return ReverseRouter.redirect(on(ConsentPreparationController.class).viewConsentPreparationPage(applicationId));
  }

  private ModelAndView modelAndView(
      ApplicationVersion applicationVersion,
      ConsentLengthType consentLengthType,
      ConsentDataForm form
  ) {
    return new ModelAndView("fcs/application/consent/data/consentDataForm")
        .addObject("pageTitle", "Edit consent data")
        .addObject("form", form)
        .addObject("applicationType", applicationVersion.getApplication().getType())
        .addObject("consentLengthType", consentLengthType)
        .addObject(
            "consentFigureUnitView",
            consentFigureUnitService.getConsentFigureUnitView(applicationVersion, consentLengthType)
        );
  }
}
