package uk.co.nstauthority.fieldconsents.production.longterm;

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
import uk.co.nstauthority.fieldconsents.application.tasklist.shared.ApplicationTaskListController;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@Controller
@RequestMapping("applications/{applicationId}/long-term-production")
public class LongTermProductionController {

  private final ApplicationVersionService applicationVersionService;
  private final LongTermProductionService longTermProductionService;
  private final LongTermProductionFormValidator longTermProductionFormValidator;

  @Autowired
  public LongTermProductionController(ApplicationVersionService applicationVersionService,
                                      LongTermProductionService longTermProductionService,
                                      LongTermProductionFormValidator longTermProductionFormValidator) {
    this.applicationVersionService = applicationVersionService;
    this.longTermProductionService = longTermProductionService;
    this.longTermProductionFormValidator = longTermProductionFormValidator;
  }

  @GetMapping
  public ModelAndView getLongTermProductionRequestForm(@PathVariable Integer applicationId) {
    ApplicationVersion latestApplicationVersion =
        applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    LongTermProductionForm longTermProductionForm = longTermProductionService.getLongTermProductionForm(latestApplicationVersion);

    ModelAndView modelAndView = getLongTermProductionModelAndView(applicationId, longTermProductionForm);
    modelAndView.addObject("form", longTermProductionForm);

    return modelAndView;
  }

  private ModelAndView getLongTermProductionModelAndView(Integer applicationId, LongTermProductionForm longTermProductionForm) {
    ModelAndView modelAndView = new ModelAndView("fcs/production/longTermProductionForm");
    modelAndView.addObject("startYear", longTermProductionForm.getStartYear());
    modelAndView.addObject("endYear", longTermProductionForm.getEndYear());
    modelAndView.addObject("oilUnit", longTermProductionForm.getOilUnit().getDisplayName());
    modelAndView.addObject("gasUnit", longTermProductionForm.getGasUnit().getDisplayName());
    modelAndView
        .addObject("submitUrl", ReverseRouter.route(on(LongTermProductionController.class)
            .saveLongTermProductionDetails(applicationId, null, null)))
        .addObject("cancelUrl", ReverseRouter.route(on(ApplicationTaskListController.class)
            .getTaskList(applicationId)));

    return modelAndView;
  }

  @PostMapping
  public ModelAndView saveLongTermProductionDetails(@PathVariable Integer applicationId,
                                                    @ModelAttribute("form") LongTermProductionForm form,
                                                    BindingResult bindingResult) {

    longTermProductionFormValidator.validate(form, bindingResult);

    if (bindingResult.hasErrors()) {
      return getLongTermProductionModelAndView(applicationId, form);
    }

    longTermProductionService.saveLongTermProductionDetails(
        applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId), form);

    return ReverseRouter.redirect(on(ApplicationTaskListController.class).getTaskList(applicationId));
  }

}
