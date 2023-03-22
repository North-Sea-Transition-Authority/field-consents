package uk.co.nstauthority.fieldconsents.application.supportinginformation;

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
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationTypeFeature;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.tasklist.shared.ApplicationTaskListController;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@Controller
@RequestMapping("applications/{applicationId}/supporting-information")
public class SupportingInformationController {

  private final ApplicationService applicationService;

  private final ApplicationVersionService applicationVersionService;

  private final SupportingInformationService supportingInformationService;

  private final SupportingInformationFormValidator supportingInformationFormValidator;

  @Autowired
  public SupportingInformationController(ApplicationService applicationService,
                                         ApplicationVersionService applicationVersionService,
                                         SupportingInformationService supportingInformationService,
                                         SupportingInformationFormValidator supportingInformationFormValidator) {
    this.applicationService = applicationService;
    this.applicationVersionService = applicationVersionService;
    this.supportingInformationService = supportingInformationService;
    this.supportingInformationFormValidator = supportingInformationFormValidator;
  }


  @GetMapping
  public ModelAndView getSupportingInformationForm(@PathVariable Integer applicationId) {
    ApplicationVersion applicationVersion = applicationVersionService
        .getLatestApplicationVersionByApplicationId(applicationId);

    SupportingInformationForm supportingInformationForm = supportingInformationService
        .getSupportingInformationForm(applicationVersion);

    var modelAndView = getSupportingInformationModelAndView(applicationId);
    modelAndView.addObject("form", supportingInformationForm);
    return modelAndView;
  }

  private ModelAndView getSupportingInformationModelAndView(Integer applicationId) {
    var modelAndView = new ModelAndView("fcs/application/supportingInformationForm");
    ApplicationType applicationType = applicationService.getApplicationById(applicationId).getType();

    boolean erapInformationAllowed = ApplicationTypeFeature.ERAP_SUPPORTING_INFORMATION.allowed(applicationType);
    if (erapInformationAllowed) {
      modelAndView
          .addObject("applicationType",
              applicationType.equals(ApplicationType.FLARE)
                  ? "flaring"
                  : "venting"
      );
    }
    modelAndView
        .addObject("erapInformationAllowed", erapInformationAllowed)
        .addObject("submitUrl", ReverseRouter.route(on(SupportingInformationController.class)
            .saveSupportingInformation(applicationId, null, null)))
        .addObject("cancelUrl", ReverseRouter.route(on(ApplicationTaskListController.class)
            .getTaskList(applicationId)));

    return modelAndView;
  }

  @PostMapping
  public ModelAndView saveSupportingInformation(@PathVariable Integer applicationId,
                                                @ModelAttribute("form") SupportingInformationForm form,
                                                BindingResult bindingResult) {

    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    form.setApplicationVersion(applicationVersion);

    supportingInformationFormValidator.validate(form, bindingResult);

    if (bindingResult.hasErrors()) {
      return getSupportingInformationModelAndView(applicationId);
    }

    supportingInformationService.saveSupportingInformation(
        applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId), form);

    return ReverseRouter.redirect(on(ApplicationTaskListController.class).getTaskList(applicationId));
  }
}
