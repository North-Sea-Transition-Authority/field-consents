package uk.co.nstauthority.fieldconsents.application.eiadirection.projectpurpose;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

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
import uk.co.nstauthority.fieldconsents.application.eiadirection.EiaDirectionService;
import uk.co.nstauthority.fieldconsents.application.eiadirection.havesubmitted.HaveSubmittedController;
import uk.co.nstauthority.fieldconsents.application.tasklist.shared.ApplicationTaskListController;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationPermission;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationStatus;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("applications/{applicationId}/eia-direction/project-purpose")
@HasApplicationStatus(statuses = ApplicationVersionStatus.IN_PROGRESS)
@HasApplicationPermission(permissions = RolePermission.EDIT_FCS_APPLICATIONS)
public class ProjectPurposeController {

  private final ProjectPurposeFormValidator validator;
  private final ApplicationVersionService applicationVersionService;
  private final EiaDirectionService eiaDirectionService;

  ProjectPurposeController(ProjectPurposeFormValidator validator,
                           ApplicationVersionService applicationVersionService,
                           EiaDirectionService eiaDirectionService) {
    this.validator = validator;
    this.applicationVersionService = applicationVersionService;
    this.eiaDirectionService = eiaDirectionService;
  }

  @GetMapping
  public ModelAndView getForm(@PathVariable Integer applicationId) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    var form = eiaDirectionService
        .findEiaDirection(applicationVersion)
        .map(ProjectPurposeForm::from)
        .orElseGet(ProjectPurposeForm::empty);

    return getModelAndView(applicationId, form);
  }

  @PostMapping
  ModelAndView saveForm(@PathVariable Integer applicationId,
                        @ModelAttribute("form") ProjectPurposeForm form,
                        BindingResult bindingResult) {
    validator.validate(form, bindingResult);
    if (bindingResult.hasErrors()) {
      return getModelAndView(applicationId, form);
    }

    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);
    eiaDirectionService.updateEiaDirection(applicationVersion, form);

    if (Boolean.TRUE.equals(form.forPurposeOfEiaRegs())) {
      return ReverseRouter.redirect(on(HaveSubmittedController.class).getForm(applicationId));
    }

    return ReverseRouter.redirect(on(ApplicationTaskListController.class).getTaskList(applicationId));
  }

  private ModelAndView getModelAndView(Integer applicationId, ProjectPurposeForm form) {
    var taskListUrl = ReverseRouter.route(on(ApplicationTaskListController.class).getTaskList(applicationId));

    return new ModelAndView("fcs/application/eia-screening/project-purpose-form")
        .addObject("form", form)
        .addObject("cancelUrl", taskListUrl)
        .addObject("backLinkUrl", taskListUrl);
  }

}
