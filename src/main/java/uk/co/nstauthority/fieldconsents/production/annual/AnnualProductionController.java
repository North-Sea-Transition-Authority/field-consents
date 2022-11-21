package uk.co.nstauthority.fieldconsents.production.annual;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

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
import uk.co.nstauthority.fieldconsents.application.tasklist.shared.ApplicationTaskListController;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@Controller
@RequestMapping("applications/{applicationId}/annual-production")
public class AnnualProductionController {

  private final AnnualProductionService annualProductionService;
  private final ApplicationVersionService applicationVersionService;
  private final AnnualProductionFormValidator annualProductionFormValidator;

  public AnnualProductionController(AnnualProductionService annualProductionService,
                                    ApplicationVersionService applicationVersionService,
                                    AnnualProductionFormValidator annualProductionFormValidator) {
    this.annualProductionService = annualProductionService;
    this.applicationVersionService = applicationVersionService;
    this.annualProductionFormValidator = annualProductionFormValidator;
  }

  @GetMapping
  public ModelAndView getAnnualProductionRequestForm(@PathVariable Integer applicationId) {
    ApplicationVersion currentVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    AnnualProductionForm annualProductionForm = annualProductionService.getAnnualProductionForm(currentVersion);
    ModelAndView modelAndView = getAnnualProductionModelAndView(applicationId, annualProductionForm);
    modelAndView.addObject("form", annualProductionForm);
    return modelAndView;
  }

  private ModelAndView getAnnualProductionModelAndView(Integer applicationId, AnnualProductionForm annualProductionForm) {
    ModelAndView modelAndView = new ModelAndView("fcs/production/annualProductionForm");
    modelAndView.addObject("requestYear", annualProductionForm.getYear());
    modelAndView.addObject("oilUnit", annualProductionForm.getOilUnit().getDisplayName());
    modelAndView.addObject("gasUnit", annualProductionForm.getGasUnit().getDisplayName());
    modelAndView
        .addObject("submitUrl", ReverseRouter.route(on(AnnualProductionController.class)
            .saveAnnualProductionDetails(applicationId, null, ReverseRouter.emptyBindingResult())))
        .addObject("cancelUrl", ReverseRouter.route(on(ApplicationTaskListController.class)
            .getTaskList(applicationId)));
    return modelAndView;
  }

  @PostMapping
  public ModelAndView saveAnnualProductionDetails(@PathVariable Integer applicationId,
                                                  @ModelAttribute("form") AnnualProductionForm form,
                                                  BindingResult bindingResult) {

    annualProductionFormValidator.validate(form, bindingResult);

    if (bindingResult.hasErrors()) {
      return getAnnualProductionModelAndView(applicationId, form);
    } else {
      annualProductionService.saveAnnualProductionDetails(
          applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId), form);
      return ReverseRouter.redirect(on(ApplicationTaskListController.class).getTaskList(applicationId));
    }
  }
}
