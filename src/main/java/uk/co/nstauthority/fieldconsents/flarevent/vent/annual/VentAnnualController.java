package uk.co.nstauthority.fieldconsents.flarevent.vent.annual;

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
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.application.tasklist.shared.ApplicationTaskListController;
import uk.co.nstauthority.fieldconsents.application.unit.ApplicationUnitService;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationStatus;
import uk.co.nstauthority.fieldconsents.authorisation.role.grouped.UserCanEditApplication;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@Controller
@RequestMapping("applications/{applicationId}/vent-annual")
@HasApplicationStatus(statuses = ApplicationVersionStatus.IN_PROGRESS)
@UserCanEditApplication
public class VentAnnualController {

  private final ApplicationVersionService applicationVersionService;

  private final VentAnnualService ventAnnualService;

  private final VentAnnualFormService ventAnnualFormService;

  private final ApplicationUnitService applicationUnitService;

  VentAnnualController(ApplicationVersionService applicationVersionService,
                       VentAnnualService ventAnnualService,
                       VentAnnualFormService ventAnnualFormService,
                       ApplicationUnitService applicationUnitService) {
    this.applicationVersionService = applicationVersionService;
    this.ventAnnualService = ventAnnualService;
    this.ventAnnualFormService = ventAnnualFormService;
    this.applicationUnitService = applicationUnitService;
  }

  @GetMapping
  public ModelAndView getVentAnnualForm(@PathVariable Integer applicationId) {
    ApplicationVersion applicationVersion =
        applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    VentAnnualForm ventAnnualForm =
        ventAnnualService.getVentAnnualForm(applicationVersion);

    var modelAndView = getVentAnnualModelAndView(applicationId, ventAnnualForm);
    modelAndView.addObject("form", ventAnnualForm);
    return modelAndView;
  }

  private ModelAndView getVentAnnualModelAndView(Integer applicationId, VentAnnualForm ventAnnualForm) {
    ApplicationVersion applicationVersion =
        applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    String pageTitle = ConsentLengthType.ANNUAL.getDisplayName() + " " + ventAnnualForm.getYear();

    ModelAndView modelAndView = new ModelAndView("fcs/vent/ventAnnualForm");
    modelAndView
        .addObject("pageTitle", pageTitle)
        .addObject("categoryUnit",
            applicationUnitService.getVentCategoryUnit(applicationVersion).getDisplayName())
        .addObject("submitUrl", ReverseRouter.route(on(VentAnnualController.class)
            .saveVentAnnualForm(applicationId, null, ReverseRouter.emptyBindingResult())))
        .addObject("cancelUrl", ReverseRouter.route(on(ApplicationTaskListController.class)
            .getTaskList(applicationId, null)));
    return modelAndView;
  }

  @PostMapping
  public ModelAndView saveVentAnnualForm(@PathVariable Integer applicationId,
                                          @ModelAttribute("form") VentAnnualForm form,
                                          BindingResult bindingResult) {

    bindingResult = ventAnnualFormService.validate(form, bindingResult);

    if (bindingResult.hasErrors()) {
      return getVentAnnualModelAndView(applicationId, form);
    }

    ventAnnualService.saveVentAnnual(
        applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId), form);

    return ReverseRouter.redirect(on(ApplicationTaskListController.class).getTaskList(applicationId, null));
  }

}
