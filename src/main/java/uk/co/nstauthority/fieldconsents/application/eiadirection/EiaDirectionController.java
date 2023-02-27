package uk.co.nstauthority.fieldconsents.application.eiadirection;

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
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.tasklist.shared.ApplicationTaskListController;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@Controller
@RequestMapping("applications/{applicationId}/eia-direction")
public class EiaDirectionController {

  private final ApplicationVersionService applicationVersionService;

  private final EiaDirectionService eiaDirectionService;

  private final EiaDirectionFormValidator formValidator;

  private final EiaDirectionFormService eiaDirectionFormService;

  @Autowired
  EiaDirectionController(ApplicationVersionService applicationVersionService,
                                EiaDirectionService eiaDirectionService,
                                EiaDirectionFormValidator formValidator,
                                EiaDirectionFormService eiaDirectionFormService) {
    this.applicationVersionService = applicationVersionService;
    this.eiaDirectionService = eiaDirectionService;
    this.formValidator = formValidator;
    this.eiaDirectionFormService = eiaDirectionFormService;
  }

  @GetMapping
  public ModelAndView getEiaDirectionForm(@PathVariable Integer applicationId) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    var eiaDirectionForm = eiaDirectionService.getEiaDirectionForm(applicationVersion);

    return getEiaDirectionModelAndView(applicationId, eiaDirectionForm.getSatId())
        .addObject("form", eiaDirectionForm);
  }

  private ModelAndView getEiaDirectionModelAndView(Integer applicationId, Integer satId) {
    var modelAndView = new ModelAndView("fcs/application/eiaDirectionForm");
    return modelAndView
        .addObject("prefilledEiaDirectionRef", eiaDirectionFormService.getPrefilledEiaDirectionRef(satId))
        .addObject("cancelUrl", ReverseRouter.route(on(ApplicationTaskListController.class)
            .getTaskList(applicationId)));
  }

  @PostMapping
  ModelAndView saveEiaDirectionForm(@PathVariable Integer applicationId,
                                    @ModelAttribute("form") EiaDirectionForm form,
                                    BindingResult bindingResult) {
    formValidator.validate(form, bindingResult);

    if (bindingResult.hasErrors()) {
      return getEiaDirectionModelAndView(applicationId, form.getSatId());
    }

    eiaDirectionService.saveEiaDirection(
        applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId), form);

    return ReverseRouter.redirect(on(ApplicationTaskListController.class).getTaskList(applicationId));
  }
}
