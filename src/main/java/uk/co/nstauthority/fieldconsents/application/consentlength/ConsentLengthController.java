package uk.co.nstauthority.fieldconsents.application.consentlength;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.workarea.WorkAreaController;

@Controller
@RequestMapping("applications/{applicationId}/consent-length")
public class ConsentLengthController {

  private final ApplicationService applicationService;
  private final ConsentLengthService consentLengthService;
  private final ApplicationVersionService applicationVersionService;
  private final ConsentLengthFormValidator consentLengthFormValidator;
  private final ConsentLengthControllerHelperService consentLengthHelperService;

  @Autowired
  public ConsentLengthController(ApplicationService applicationService,
                                 ConsentLengthService consentLengthService,
                                 ApplicationVersionService applicationVersionService,
                                 ConsentLengthFormValidator consentLengthFormValidator,
                                 ConsentLengthControllerHelperService consentLengthHelperService) {
    this.applicationService = applicationService;
    this.consentLengthService = consentLengthService;
    this.applicationVersionService = applicationVersionService;
    this.consentLengthFormValidator = consentLengthFormValidator;
    this.consentLengthHelperService = consentLengthHelperService;
  }

  @GetMapping
  public ModelAndView getConsentLengthForm(@PathVariable Integer applicationId) {
    ModelAndView modelAndView = getConsentLengthFormModelAndView(applicationId);
    ApplicationVersion currentVersion = applicationVersionService.getApplicationVersionById(applicationId);
    ConsentLengthForm consentLengthForm = consentLengthService.getConsentLengthForm(currentVersion);

    modelAndView.addObject("form", consentLengthForm);
    return modelAndView;
  }

  private ModelAndView getConsentLengthFormModelAndView(Integer applicationId) {
    ModelAndView modelAndView = new ModelAndView("fcs/application/consentLengthForm");
    Application application = applicationService.getApplicationById(applicationId);
    modelAndView.addObject("consentTypes", consentLengthHelperService.getConsentTypesMap(application));
    modelAndView.addObject("annualConsentYears", consentLengthHelperService.getAnnualConsentYearsMap());
    modelAndView.addObject("longTermStartYears", consentLengthHelperService.getLongTermConsentYearsMap());
    modelAndView.addObject(
        "submitUrl",
        ReverseRouter.route(on(ConsentLengthController.class)
            .saveConsentLengthDetails(applicationId, null, ReverseRouter.emptyBindingResult()))
    );
    // TODO: Change this to route to the appropriate task-list once FCS-210, FCS-211, FCS-212 have been completed
    modelAndView.addObject("cancelUrl", ReverseRouter.route(on(WorkAreaController.class).getWorkArea()));
    return modelAndView;
  }

  @PostMapping
  public ModelAndView saveConsentLengthDetails(@PathVariable Integer applicationId,
                                               @ModelAttribute("form") ConsentLengthForm form,
                                               BindingResult bindingResult) {
    consentLengthFormValidator.validate(form, bindingResult);

    if (bindingResult.hasErrors()) {
      return getConsentLengthFormModelAndView(applicationId);
    } else {
      ApplicationVersion currentVersion = applicationVersionService.getApplicationVersionById(applicationId);
      consentLengthService.saveConsentLengthDetails(currentVersion, form);
      return new ModelAndView("fcs/startapplication/productionApplicationTaskList");
    }
  }
}
