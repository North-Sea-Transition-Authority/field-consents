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
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.ConsentPreparationController;
import uk.co.nstauthority.fieldconsents.authorisation.ActionEndPoint;
import uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBannerUtil;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@Controller
@RequestMapping("/applications/{applicationId}/consent-data")
@ActionEndPoint(CaseProcessingActionItem.CONSENT_PREPARATION)
public class ConsentDataController {

  private final ApplicationService applicationService;
  private final ConsentDataService consentDataService;
  private final ConsentDataFormValidator validator;

  ConsentDataController(
      ApplicationService applicationService,
      ConsentDataService consentDataService,
      ConsentDataFormValidator validator
  ) {
    this.applicationService = applicationService;
    this.consentDataService = consentDataService;
    this.validator = validator;
  }

  @GetMapping
  public ModelAndView getConsentDataAndRedirect(@PathVariable Integer applicationId) {
    var application = applicationService.getApplicationById(applicationId);
    var consentDataOptional = consentDataService.findConsentData(application);

    if (consentDataOptional.isPresent()) {
      return ReverseRouter.redirect(on(ConsentPreparationController.class).viewDocumentInstances(applicationId));
    }

    return ReverseRouter.redirect(on(this.getClass()).editConsentData(applicationId));
  }

  @GetMapping("/edit")
  public ModelAndView editConsentData(@PathVariable Integer applicationId) {
    var application = applicationService.getApplicationById(applicationId);
    var form = consentDataService.findConsentData(application)
        .map(ConsentDataForm::from)
        .orElseGet(ConsentDataForm::empty);

    return modelAndView(form);
  }

  @PostMapping("/edit")
  public ModelAndView submitConsentData(
      @PathVariable Integer applicationId,
      @ModelAttribute("form") ConsentDataForm form,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes
  ) {
    validator.validate(form, bindingResult);

    if (bindingResult.hasErrors()) {
      return modelAndView(form);
    }

    var application = applicationService.getApplicationById(applicationId);

    consentDataService.saveConsentData(
        application,
        form.consentStartDate().getAsLocalDate().orElseThrow(),
        form.consentEndDate().getAsLocalDate().orElseThrow()
    );

    NotificationBannerUtil.addSuccessNotification(redirectAttributes, "Consent data saved");

    return ReverseRouter.redirect(on(ConsentPreparationController.class).viewDocumentInstances(applicationId));
  }

  private ModelAndView modelAndView(ConsentDataForm form) {
    return new ModelAndView("fcs/application/consent/data/consentDataForm")
        .addObject("pageTitle", "Edit consent data")
        .addObject("form", form);
  }

}
