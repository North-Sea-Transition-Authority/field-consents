package uk.co.nstauthority.fieldconsents.production.gasinjection;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import jakarta.validation.Valid;
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
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.flags.ApplicationFlagService;
import uk.co.nstauthority.fieldconsents.application.flags.ApplicationFlagType;
import uk.co.nstauthority.fieldconsents.application.tasklist.shared.ApplicationTaskListController;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationPermission;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationStatus;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("applications/{applicationId}/gas-injection")
@HasApplicationStatus(statuses = ApplicationVersionStatus.IN_PROGRESS)
@HasApplicationPermission(permissions = RolePermission.EDIT_FCS_APPLICATIONS)
public class GasInjectionController {

  private final ApplicationVersionService applicationVersionService;

  private final GasInjectionService gasInjectionService;

  private final ApplicationFlagService applicationFlagService;

  @Autowired
  public GasInjectionController(ApplicationVersionService applicationVersionService,
                                GasInjectionService gasInjectionService,
                                ApplicationFlagService applicationFlagService) {
    this.applicationVersionService = applicationVersionService;
    this.gasInjectionService = gasInjectionService;
    this.applicationFlagService = applicationFlagService;
  }

  @GetMapping
  public ModelAndView getGasInjectionForm(@PathVariable Integer applicationId) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    return getGasInjectionModelAndView(applicationId)
        .addObject("form", gasInjectionService.getGasInjectionForm(applicationVersion));
  }

  private ModelAndView getGasInjectionModelAndView(Integer applicationId) {
    return new ModelAndView("fcs/production/gasInjectionForm")
        .addObject("cancelUrl",
            ReverseRouter.route(on(ApplicationTaskListController.class).getTaskList(applicationId)));
  }

  @PostMapping
  public ModelAndView saveGasInjectionForm(@PathVariable Integer applicationId,
                                           @Valid @ModelAttribute("form") GasInjectionForm form,
                                           BindingResult bindingResult) {
    if (bindingResult.hasErrors()) {
      return getGasInjectionModelAndView(applicationId);
    }

    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    applicationFlagService.deleteApplicationFlag(applicationVersion, ApplicationFlagType.WILL_GAS_BE_INJECTED);
    applicationFlagService.saveApplicationFlag(
        applicationVersion, ApplicationFlagType.WILL_GAS_BE_INJECTED, form.getWillGasBeInjected());

    return ReverseRouter.redirect(on(ApplicationTaskListController.class).getTaskList(applicationId));
  }
}
