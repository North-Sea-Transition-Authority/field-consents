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
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.tasklist.shared.ApplicationTaskListController;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationPermission;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationStatus;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("applications/{applicationId}/consent-length")
@HasApplicationStatus(statuses = ApplicationVersionStatus.IN_PROGRESS)
@HasApplicationPermission(permissions = RolePermission.EDIT_FCS_APPLICATIONS)
public class ConsentLengthController {

  private final ConsentLengthService consentLengthService;
  private final ApplicationVersionService applicationVersionService;
  private final ConsentLengthFormValidator consentLengthFormValidator;
  private final ConsentLengthControllerHelperService consentLengthHelperService;

  @Autowired
  public ConsentLengthController(
      ConsentLengthService consentLengthService,
      ApplicationVersionService applicationVersionService,
      ConsentLengthFormValidator consentLengthFormValidator,
      ConsentLengthControllerHelperService consentLengthHelperService
  ) {
    this.consentLengthService = consentLengthService;
    this.applicationVersionService = applicationVersionService;
    this.consentLengthFormValidator = consentLengthFormValidator;
    this.consentLengthHelperService = consentLengthHelperService;
  }

  @GetMapping
  public ModelAndView getConsentLengthForm(@PathVariable Integer applicationId) {
    ModelAndView modelAndView = getConsentLengthFormModelAndView(applicationId);
    ApplicationVersion currentVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    ConsentLengthForm consentLengthForm = consentLengthService.getConsentLengthForm(currentVersion);

    modelAndView.addObject("form", consentLengthForm);
    return modelAndView;
  }

  private ModelAndView getConsentLengthFormModelAndView(Integer applicationId) {
    var modelAndView = new ModelAndView("fcs/application/consentLengthForm");
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    var application = applicationVersion.getApplication();

    var isRevision = application.isRevision();
    modelAndView.addObject("applicationIsRevision", isRevision);

    if (isRevision) {
      var consentLengthDetails = consentLengthService.getConsentLengthDetails(applicationVersion);

      modelAndView.addObject("consentLengthView", ConsentLengthView.from(consentLengthDetails));
    }

    modelAndView.addObject("consentTypes", consentLengthHelperService.getConsentTypesMap(application));
    modelAndView.addObject("annualConsentYears", consentLengthHelperService.getAnnualConsentYearsMap());
    modelAndView.addObject("longTermStartYears", consentLengthHelperService.getLongTermConsentYearsMap());
    modelAndView.addObject(
        "submitUrl",
        ReverseRouter.route(on(ConsentLengthController.class)
            .saveConsentLengthDetails(applicationId, null, ReverseRouter.emptyBindingResult()))
    );
    modelAndView.addObject(
        "cancelUrl",
        ReverseRouter.route(on(ApplicationTaskListController.class).getTaskList(application.getId(), null))
    );
    return modelAndView;
  }

  @PostMapping
  public ModelAndView saveConsentLengthDetails(@PathVariable Integer applicationId,
                                               @ModelAttribute("form") ConsentLengthForm form,
                                               BindingResult bindingResult) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    consentLengthFormValidator.validate(form, bindingResult, applicationVersion);

    if (bindingResult.hasErrors()) {
      return getConsentLengthFormModelAndView(applicationId);
    } else {
      consentLengthService.saveConsentLengthDetails(applicationVersion, form);
      return ReverseRouter.redirect(on(ApplicationTaskListController.class).getTaskList(applicationId, null));
    }
  }
}
