package uk.co.nstauthority.fieldconsents.flarevent.flare.shortterm;

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
import uk.co.nstauthority.fieldconsents.application.unit.ApplicationUnitService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@Controller
@RequestMapping("applications/{applicationId}/flare-short-term")
public class FlareShortTermController {

  private final ApplicationVersionService applicationVersionService;

  private final FlareShortTermService flareShortTermService;

  private final FlareShortTermFormService flareShortTermFormService;

  private final ApplicationUnitService applicationUnitService;

  FlareShortTermController(ApplicationVersionService applicationVersionService,
                           FlareShortTermService flareShortTermService,
                           FlareShortTermFormService flareShortTermFormService,
                           ApplicationUnitService applicationUnitService) {
    this.applicationVersionService = applicationVersionService;
    this.flareShortTermService = flareShortTermService;
    this.flareShortTermFormService = flareShortTermFormService;
    this.applicationUnitService = applicationUnitService;
  }

  @GetMapping
  public ModelAndView getFlareShortTermForm(@PathVariable Integer applicationId) {
    ApplicationVersion applicationVersion =
        applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    FlareShortTermForm flareShortTermForm =
        flareShortTermService.getFlareShortTermForm(applicationVersion);

    var modelAndView = getFlareShortTermModelAndView(applicationId, flareShortTermForm);
    modelAndView.addObject("form", flareShortTermForm);
    return modelAndView;
  }

  private ModelAndView getFlareShortTermModelAndView(Integer applicationId, FlareShortTermForm flareShortTermForm) {
    ApplicationVersion applicationVersion =
        applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    ModelAndView modelAndView = new ModelAndView("fcs/flare/flareShortTermForm");
    modelAndView
        .addObject("startDate", flareShortTermForm.getStartDate())
        .addObject("endDate", flareShortTermForm.getEndDate())
        .addObject("categoryUnit",
            applicationUnitService.getFlareCategoryUnit(applicationVersion).getDisplayName())
        .addObject("submitUrl", ReverseRouter.route(on(FlareShortTermController.class)
            .saveFlareShortTermForm(applicationId, null, ReverseRouter.emptyBindingResult())))
        .addObject("cancelUrl", ReverseRouter.route(on(ApplicationTaskListController.class)
            .getTaskList(applicationId)));
    return modelAndView;
  }

  @PostMapping
  public ModelAndView saveFlareShortTermForm(@PathVariable Integer applicationId,
                                             @ModelAttribute("form") FlareShortTermForm form,
                                             BindingResult bindingResult) {

    bindingResult = flareShortTermFormService.validate(form, bindingResult);

    if (bindingResult.hasErrors()) {
      return getFlareShortTermModelAndView(applicationId, form);
    }

    flareShortTermService.saveFlareShortTerm(
        applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId), form);

    return ReverseRouter.redirect(on(ApplicationTaskListController.class).getTaskList(applicationId));
  }

}
