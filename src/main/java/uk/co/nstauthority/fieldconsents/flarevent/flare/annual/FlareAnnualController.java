package uk.co.nstauthority.fieldconsents.flarevent.flare.annual;

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
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationPermission;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationStatus;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("applications/{applicationId}/flare-annual")
@HasApplicationStatus(statuses = ApplicationVersionStatus.IN_PROGRESS)
@HasApplicationPermission(permissions = RolePermission.EDIT_FCS_APPLICATIONS)
public class FlareAnnualController {

  private final ApplicationVersionService applicationVersionService;

  private final FlareAnnualService flareAnnualService;

  private final FlareAnnualFormService flareAnnualFormService;

  private final ApplicationUnitService applicationUnitService;

  FlareAnnualController(ApplicationVersionService applicationVersionService,
                        FlareAnnualService flareAnnualService,
                        FlareAnnualFormService flareAnnualFormService,
                        ApplicationUnitService applicationUnitService) {
    this.applicationVersionService = applicationVersionService;
    this.flareAnnualService = flareAnnualService;
    this.flareAnnualFormService = flareAnnualFormService;
    this.applicationUnitService = applicationUnitService;
  }

  @GetMapping
  public ModelAndView getFlareAnnualForm(@PathVariable Integer applicationId) {
    ApplicationVersion applicationVersion =
        applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    FlareAnnualForm flareAnnualForm =
        flareAnnualService.getFlareAnnualForm(applicationVersion);

    var modelAndView = getFlareAnnualModelAndView(applicationId, flareAnnualForm);
    modelAndView.addObject("form", flareAnnualForm);
    return modelAndView;
  }

  private ModelAndView getFlareAnnualModelAndView(Integer applicationId, FlareAnnualForm flareAnnualForm) {
    ApplicationVersion applicationVersion =
        applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    String pageTitle = ConsentLengthType.ANNUAL.getDisplayName() + " " + flareAnnualForm.getYear();

    ModelAndView modelAndView = new ModelAndView("fcs/flare/flareAnnualForm");
    modelAndView
        .addObject("pageTitle", pageTitle)
        .addObject("categoryUnit",
            applicationUnitService.getFlareCategoryUnit(applicationVersion).getDisplayName())
        .addObject("submitUrl", ReverseRouter.route(on(FlareAnnualController.class)
            .saveFlareAnnualForm(applicationId, null, ReverseRouter.emptyBindingResult())))
        .addObject("cancelUrl", ReverseRouter.route(on(ApplicationTaskListController.class)
            .getTaskList(applicationId)));
    return modelAndView;
  }

  @PostMapping
  public ModelAndView saveFlareAnnualForm(@PathVariable Integer applicationId,
                                          @ModelAttribute("form") FlareAnnualForm form,
                                          BindingResult bindingResult) {

    bindingResult = flareAnnualFormService.validate(form, bindingResult);

    if (bindingResult.hasErrors()) {
      return getFlareAnnualModelAndView(applicationId, form);
    }

    flareAnnualService.saveFlareAnnual(
        applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId), form);

    return ReverseRouter.redirect(on(ApplicationTaskListController.class).getTaskList(applicationId));
  }

}
