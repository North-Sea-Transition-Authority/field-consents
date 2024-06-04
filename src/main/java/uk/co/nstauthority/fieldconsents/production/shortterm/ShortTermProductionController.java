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
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.tasklist.shared.ApplicationTaskListController;
import uk.co.nstauthority.fieldconsents.application.unit.ApplicationUnitService;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationPermission;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationStatus;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("applications/{applicationId}/short-term-production")
@HasApplicationStatus(statuses = ApplicationVersionStatus.IN_PROGRESS)
@HasApplicationPermission(permissions = RolePermission.EDIT_FCS_APPLICATIONS)
public class ShortTermProductionController {

  private final ApplicationUnitService applicationUnitService;
  private final ApplicationVersionService applicationVersionService;
  private final ShortTermProductionService shortTermProductionService;
  private final ShortTermProductionFormValidator shortTermProductionFormValidator;

  @Autowired
  public ShortTermProductionController(ApplicationUnitService applicationUnitService,
                                       ApplicationVersionService applicationVersionService,
                                       ShortTermProductionService shortTermProductionService,
                                       ShortTermProductionFormValidator shortTermProductionFormValidator) {
    this.applicationUnitService = applicationUnitService;
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
    ApplicationVersion currentVersion =
        applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    modelAndView.addObject("requestYear", shortTermProductionForm.getYear());
    modelAndView.addObject("startDate", shortTermProductionForm.getStartDate());
    modelAndView.addObject("endDate", shortTermProductionForm.getEndDate());
    modelAndView.addObject("oilUnit", applicationUnitService.getProductionOilUnit(currentVersion).getDisplayName());
    modelAndView.addObject("gasUnit", applicationUnitService.getProductionGasUnit(currentVersion).getDisplayName());
    modelAndView
        .addObject("submitUrl", ReverseRouter.route(on(ShortTermProductionController.class)
            .saveShortTermProductionDetails(applicationId, null, ReverseRouter.emptyBindingResult())))
        .addObject("cancelUrl", ReverseRouter.route(on(ApplicationTaskListController.class)
            .getTaskList(applicationId, null)));
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
      return ReverseRouter.redirect(on(ApplicationTaskListController.class).getTaskList(applicationId, null));
    }
  }
}
