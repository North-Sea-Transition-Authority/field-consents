package uk.co.nstauthority.fieldconsents.flarevent.vent.shortterm;

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
@RequestMapping("applications/{applicationId}/vent-short-term")
public class VentShortTermController {

  private final ApplicationVersionService applicationVersionService;

  private final VentShortTermService ventShortTermService;

  private final VentShortTermFormService ventShortTermFormService;

  private final ApplicationUnitService applicationUnitService;

  VentShortTermController(ApplicationVersionService applicationVersionService,
                          VentShortTermService ventShortTermService,
                          VentShortTermFormService ventShortTermFormService,
                          ApplicationUnitService applicationUnitService) {
    this.applicationVersionService = applicationVersionService;
    this.ventShortTermService = ventShortTermService;
    this.ventShortTermFormService = ventShortTermFormService;
    this.applicationUnitService = applicationUnitService;
  }

  @GetMapping
  public ModelAndView getVentShortTermForm(@PathVariable Integer applicationId) {
    ApplicationVersion applicationVersion =
        applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    VentShortTermForm ventShortTermForm =
        ventShortTermService.getVentShortTermForm(applicationVersion);

    var modelAndView = getVentShortTermModelAndView(applicationId, ventShortTermForm);
    modelAndView.addObject("form", ventShortTermForm);
    return modelAndView;
  }

  private ModelAndView getVentShortTermModelAndView(Integer applicationId, VentShortTermForm ventShortTermForm) {
    ApplicationVersion applicationVersion =
        applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    ModelAndView modelAndView = new ModelAndView("fcs/vent/ventShortTermForm");
    modelAndView
        .addObject("startDate", ventShortTermForm.getStartDate())
        .addObject("endDate", ventShortTermForm.getEndDate())
        .addObject("categoryUnit",
            applicationUnitService.getVentCategoryUnit(applicationVersion).getDisplayName())
        .addObject("submitUrl", ReverseRouter.route(on(VentShortTermController.class)
            .saveVentShortTermForm(applicationId, null, ReverseRouter.emptyBindingResult())))
        .addObject("cancelUrl", ReverseRouter.route(on(ApplicationTaskListController.class)
            .getTaskList(applicationId)));
    return modelAndView;
  }

  @PostMapping
  public ModelAndView saveVentShortTermForm(@PathVariable Integer applicationId,
                                             @ModelAttribute("form") VentShortTermForm form,
                                             BindingResult bindingResult) {

    bindingResult = ventShortTermFormService.validate(form, bindingResult);

    if (bindingResult.hasErrors()) {
      return getVentShortTermModelAndView(applicationId, form);
    }

    ventShortTermService.saveVentShortTerm(
        applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId), form);

    return ReverseRouter.redirect(on(ApplicationTaskListController.class).getTaskList(applicationId));
  }

}
