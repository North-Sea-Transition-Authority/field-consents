package uk.co.nstauthority.fieldconsents.production.shortterm;

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
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@Controller
@RequestMapping("applications/{applicationId}/short-term-production")
public class ShortTermProductionController {

  private final ApplicationVersionService applicationVersionService;
  private final ShortTermProductionService shortTermProductionService;
  private final ShortTermProductionFormValidator shortTermProductionFormValidator;

  @Autowired
  public ShortTermProductionController(ApplicationVersionService applicationVersionService,
                                       ShortTermProductionService shortTermProductionService,
                                       ShortTermProductionFormValidator shortTermProductionFormValidator) {
    this.applicationVersionService = applicationVersionService;
    this.shortTermProductionService = shortTermProductionService;
    this.shortTermProductionFormValidator = shortTermProductionFormValidator;
  }

  @GetMapping
  public ModelAndView getShortTermProductionRequestForm(@PathVariable Integer applicationId) {
    ApplicationVersion currentVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    ShortTermProductionForm shortTermProductionForm = shortTermProductionService.getShortTermProductionForm(currentVersion);
    ModelAndView modelAndView = getShortTermProductionModelAndView(applicationId, shortTermProductionForm);

    modelAndView.addObject("form", shortTermProductionForm);
    return modelAndView;
  }

  private ModelAndView getShortTermProductionModelAndView(Integer applicationId,
                                                          ShortTermProductionForm shortTermProductionForm) {
    ModelAndView modelAndView = new ModelAndView("fcs/production/shortTermProductionForm");

    modelAndView.addObject("requestYear", shortTermProductionForm.getYear());
    modelAndView.addObject("startDate", shortTermProductionForm.getStartDate());
    modelAndView.addObject("endDate", shortTermProductionForm.getEndDate());
    modelAndView.addObject("oilUnit", shortTermProductionForm.getOilUnit().getDisplayName());
    modelAndView.addObject("gasUnit", shortTermProductionForm.getGasUnit().getDisplayName());
    modelAndView.addObject("submitUrl", ReverseRouter.route(
        on(ShortTermProductionController.class)
            .saveShortTermProductionDetails(applicationId, null, ReverseRouter.emptyBindingResult()))
    );
    return modelAndView;
  }

  @PostMapping
  public ModelAndView saveShortTermProductionDetails(@PathVariable Integer applicationId,
                                                     @ModelAttribute("form") ShortTermProductionForm form,
                                                     BindingResult bindingResult) {
    shortTermProductionFormValidator.validate(form, bindingResult);

    if (bindingResult.hasErrors()) {
      return getShortTermProductionModelAndView(applicationId, form);
    } else {
      ApplicationVersion currentVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
      shortTermProductionService.saveShortTermProductionDetails(currentVersion, form);
      return new ModelAndView("fcs/production/applicationSubmitted");
    }
  }
}
