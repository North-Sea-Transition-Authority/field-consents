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
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.production.ProductionUnit;

@Controller
@RequestMapping("applications/{applicationId}/annual-production")
public class AnnualProductionController {

  public static final String PRODUCTION_YEAR = "2022";

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
    ModelAndView modelAndView = getAnnualProductionModelAndView(applicationId);
    ApplicationVersion currentVersion = applicationVersionService.getLatestApplicationVersionOrError(applicationId);
    AnnualProductionForm annualProductionForm = annualProductionService.getAnnualProductionForm(currentVersion, PRODUCTION_YEAR);
    modelAndView.addObject("form", annualProductionForm);
    return modelAndView;
  }

  private ModelAndView getAnnualProductionModelAndView(Integer applicationId) {
    ModelAndView modelAndView = new ModelAndView("fcs/production/annualProductionForm");
    modelAndView.addObject("requestYear", PRODUCTION_YEAR);
    modelAndView.addObject("oilUnit", ProductionUnit.SCM_PER_MONTH.getDisplayName());
    modelAndView.addObject("gasUnit", ProductionUnit.KSCM_PER_MONTH.getDisplayName());
    modelAndView.addObject("submitUrl", ReverseRouter.route(
        on(AnnualProductionController.class)
            .saveAnnualProductionDetails(applicationId, null, ReverseRouter.emptyBindingResult())));
    return modelAndView;
  }

  @PostMapping
  public ModelAndView saveAnnualProductionDetails(@PathVariable Integer applicationId,
                                                  @ModelAttribute("form") AnnualProductionForm form,
                                                  BindingResult bindingResult) {

    annualProductionFormValidator.validate(form, bindingResult);

    if (bindingResult.hasErrors()) {
      return getAnnualProductionModelAndView(applicationId);
    } else {
      annualProductionService.saveAnnualProductionDetails(
          applicationVersionService.getLatestApplicationVersionOrError(applicationId), form);
      return new ModelAndView("fcs/startapplication/productionApplicationTaskList");
    }
  }
}
